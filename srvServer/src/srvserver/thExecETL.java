/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;

import dataClass.ETL;
import dataClass.Interval;
import dataClass.TaskProcess;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import utilities.dataAccess;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thExecETL extends Thread{
    TaskProcess taskProcess = new TaskProcess();
    static srvRutinas gSub;
    static globalAreaData gDatos;
    String vSQLSource;
    String vSQLDest;
    int totalNewInterval;
	int gNumIntFinished;
	int gNumIntReady;
	int gNumIntError;
	int gNumIntRunning;
	StringBuilder columnNames;
	StringBuilder bindVariables;
	PreparedStatement psInsertar;
	dataAccess sConn = new dataAccess(gDatos);
	dataAccess dConn = new dataAccess(gDatos);

    Logger logger = Logger.getLogger("srv.execEtl");
    Logger logint = Logger.getLogger("srv.etlInterval");
    
    public thExecETL(globalAreaData m, TaskProcess taskProcess) {
        this.taskProcess = taskProcess;
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
    
    
    private boolean isConnectedDB(ETL etl) {
    	try {
    		boolean isConnectDBSource;
    		boolean isConnectDBDest;
    		
    		
        	/**
        	 * Establece conexion hacia BD Origen
        	 */
        	logger.info("Establece conexión a Base Origen: "+etl.getSDBNAME());
        	logger.info("dbType: "+etl.getSDBTYPE());
        	logger.info("IP: "+etl.getSIP());
        	logger.info("Port: "+etl.getSDBPORT());
        	logger.info("UserName: "+etl.getSUSERNAME());
        	logger.debug("Pass: "+etl.getSUSERPASS());
        	
            sConn.setDbType(etl.getSDBTYPE());
            sConn.setDbHost(etl.getSIP());
            sConn.setDbPort(etl.getSDBPORT());
            sConn.setDbName(etl.getSDBNAME());
            sConn.setDbUser(etl.getSUSERNAME());
            sConn.setDbPass(etl.getSUSERPASS());
            
            sConn.conectar();
            
            if (sConn.isConnected()) {
                logger.info("Base origen connected!");
                isConnectDBSource= true;
            } else {
            	logger.info("Base origen NO connected!");
                isConnectDBSource = false;
            }

            /**
             * Establece conexion hacia BD Destino
             */
            logger.info("Establece conexión a Base Destino: "+etl.getDDBNAME());
        	logger.info("dbType: "+etl.getDDBTYPE());
        	logger.info("IP: "+etl.getDIP());
        	logger.info("Port: "+etl.getDDBPORT());
        	logger.info("UserName: "+etl.getDUSERNAME());
        	logger.debug("Pass: "+etl.getDUSERPASS());
            
            dConn.setDbType(etl.getDDBTYPE());
            dConn.setDbHost(etl.getDIP());
            dConn.setDbPort(etl.getDDBPORT());
            dConn.setDbName(etl.getDDBNAME());
            dConn.setDbUser(etl.getDUSERNAME());
            dConn.setDbPass(etl.getDUSERPASS());
           
            dConn.conectar();
            
            if (dConn.isConnected()) {
                logger.info("Base Destino connected!");
                isConnectDBDest= true;
            } else {
            	logger.info("Base Destino NO connected!");
            	isConnectDBDest = false;
            }

    		if (isConnectDBSource&&isConnectDBDest) {
    			return true;
    		} else {
    			return false;
    		}
    		
    	} catch (Exception e) {
    		logger.error("Error modulo isConnectedDB...: "+e.getMessage());
    		return false;
    	}
    }
    
    private Map<String, Interval> genExecInterval(ETL etl) throws Exception {
    	Map<String, Interval> mapInterval = new TreeMap<>(etl.getMapInterval());
    	Map<String, Interval> execInterval = new TreeMap<>();
    	
    	for (Map.Entry<String, Interval> mapInt : mapInterval.entrySet()) {
    		//Aplica Criterio para entregar intervalos a ejecutar
    		if (mapInt.getValue().getStatus().equals("Ready")) {
    			execInterval.put(mapInt.getKey(), mapInt.getValue());
    		}
    	}
    	return execInterval;
    }
    
    
    @Override
    public void run() {
    	String errMesg = "";
    	try {
    		boolean statusExit=true;
    		
    		logger.info("Iniciando ejecución ETL:"+taskProcess.getProcID()+" "+taskProcess.getNumSecExec());
    		
    		gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setStatus("Running");
    		gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setUpdateTime(gSub.getDateNow());
    		
    		if (isValidDataParam()) {
    			
    			//Serializa Objetos
    			String etlString = gSub.serializeObjectToJSon(taskProcess.getParams(), false);
    			ETL etl = (ETL) gSub.serializeJSonStringToObject(etlString, ETL.class);
    			
    			if (isConnectedDB(etl)) {
    				
    				//Encuentra los intervalos con alguna politica para ser ejecutados
    				//y los deja en Map local execInterval
    				Map<String, Interval> execInterval = new TreeMap<>(genExecInterval(etl));
    				
    				if (execInterval.size()>0) {
    					
    					//Genera la base de la query de extraccion
    					//despues le añade el where
    					vSQLSource = genQueryExtract(etl);
    					
    					for (Map.Entry<String, Interval> execInt : execInterval.entrySet()) {
    						//Recupera key y objeto
    						String keyMapInterval = execInt.getKey();
    						Interval interval = execInt.getValue();
    						
    						//actualiza status interval a Running
    						logint.info("Procesando Intervalo: "+taskProcess.getProcID()+":"+taskProcess.getIntervalID());
    						updateIntervalStatus(taskProcess, etl, interval, "Running");
    						//updateTaskStatus(taskProcess, etl, "Running");
    						
    	        			//Ejecuta Borrado de data del intervalo
    						//if swDelete esta activo
	    	        			String vQueryDelete = genSqlQueryDeleteDbSource(etl, interval);
	    	        			int rowsDeleted = dConn.execQuery(vQueryDelete);
	    	        			//logger.info("Total filas borradas para intevalo "+keyMapInterval+" :"+ rowsDeleted);
	    	        			logint.info("Total filas borradas para intevalo "+keyMapInterval+" :"+ rowsDeleted);
	    	        			
    	        			//Ejecuta Query de Extraccion de Datos
    	        			String vQueryExtract = genSqlQueryDbSource(etl, interval);
    	        			ResultSet rsExtract = (ResultSet) dConn.getQuery(vQueryExtract);

	        				int rowsLoad=0;
	        				int rowsRead=0;

    	        			if (rsExtract!=null) {
    	        				int numShows=2000;
    	        				int iteShows=2000;
    	        				genColumnBind(etl);
    	        				while (rsExtract.next()) {
    	        					rowsRead++;
    	        					try {
	    	        					psInsertar = genPrepareStatement(etl, dConn, rsExtract);
	    	        					
	    	        					if (psInsertar.executeUpdate()==1) {
	    	        						rowsLoad++;
	    	        					}
    	        					} catch (Exception e) {
    	        						String vMesg = "Error insertando fila en intervalo: "+keyMapInterval;
    	        						vMesg = vMesg + "...: "+e.getMessage();
	        							//logger.error(vMesg);
	        							logint.error(vMesg);
    	        					}
    	        					
    	        					if (rowsRead==numShows) {
    	        						//Muestra parcial filas cargadas
										//logger.info("Filas leídas   Etl-Interval "+keyMapInterval+ ": "+rowsRead);
										//logger.info("Filas cargadas Etl-Interval "+keyMapInterval+ ": "+rowsLoad);
										logint.info("Filas leídas   Etl-Interval "+keyMapInterval+ ": "+rowsRead);
										logint.info("Filas cargadas Etl-Interval "+keyMapInterval+ ": "+rowsLoad);
										
										updateIntervalStatusRows(taskProcess, etl, interval, rowsRead, rowsLoad);
										
    	        						numShows=numShows + iteShows;
    	        					}
    	        				}
    	        				rsExtract.close();
    	        			}
    	        			
    	        			//Termino de Ejecucion de cada intervalo cargado
    	        			//logger.info("Finalizando caga de Etl-Interval: "+keyMapInterval);
    						//logger.info("Se leyeron: "+rowsRead+" filas para el intervalo: "+keyMapInterval);
    						//logger.info("Se cargaron: "+rowsLoad+" filas para el intervalo: "+keyMapInterval);

    	        			logint.info("Finalizando caga de Etl-Interval: "+keyMapInterval);
    	        			logint.info("Se leyeron: "+rowsRead+" filas para el intervalo: "+keyMapInterval);
    	        			logint.info("Se cargaron: "+rowsLoad+" filas para el intervalo: "+keyMapInterval);

    						
    						updateIntervalStatus(taskProcess, etl, interval, "Finished");
    						//updateTaskStatus(taskProcess, etl, "Finished");
    						
    					}
    					
    				} else {
    					logger.warn("No hay intervalos pendientes para ejecutar por Task: "+taskProcess.getProcID()+" "+taskProcess.getNumSecExec());
    				}

    			} else {
        			statusExit=false;
        			errMesg="No es posible conectarse a las Bases de Datos";    				
    			}
    			
    		} else {
    			statusExit=false;
    			errMesg="No se pudo validar DataParam()";
    		}
    		
    		//Terminando Ejecucion de ETL
    		
    		if (statusExit) {
    			logger.info("Termino Exitoso ejecucion ETL:"+taskProcess.getProcID()+" "+taskProcess.getNumSecExec());
        		gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setStatus("Finished");
        		gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setUpdateTime(gSub.getDateNow());

    		} else {
    			logger.error("Termino Erroneo ejecucion ETL:"+taskProcess.getProcID()+" "+taskProcess.getNumSecExec()+ " Mesg: "+ errMesg);
        		gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setStatus("Error");
        		gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setUpdateTime(gSub.getDateNow());

    		}
    		
    	} catch (Exception e) {
    		logger.error("Error en thExecETL3...: "+e.getMessage());
    		gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setStatus("Error");
    		gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setUpdateTime(gSub.getDateNow());

    	}
    } //fin run()
    
    private void updateIntervalStatusRows(TaskProcess task, ETL etl, Interval interval, int rowsRead, int rowsLoad) throws Exception {
    	interval.setRowsLoad(rowsLoad);
    	interval.setRowsRead(rowsRead);
    	interval.setFecUpdate(gSub.getDateNow());
    	etl.getMapInterval().put(interval.getIntervalID(), interval);

    	String keyMap = task.getProcID()+":"+task.getNumSecExec();
    	gDatos.getMapTask().get(keyMap).setUpdateTime(gSub.getDateNow());
    	gDatos.getMapTask().get(keyMap).setParams(etl);
    }
    
    private void updateTaskStatus(TaskProcess task, ETL etl, String status) throws Exception {
    	task.setStatus(status);
    	task.setUpdateTime(gSub.getDateNow());
    	task.setParams(etl);
    	String keyMap = task.getProcID()+":"+task.getNumSecExec();
    	gDatos.getMapTask().put(keyMap, task);
    }
    
    private void updateIntervalStatus(TaskProcess task, ETL etl, Interval interval, String status) throws Exception{
    	interval.setStatus(status);
    	interval.setFecUpdate(gSub.getDateNow());
    	etl.getMapInterval().put(interval.getIntervalID(), interval);
    	String keyMap = task.getProcID()+":"+task.getNumSecExec();
    	gDatos.getMapTask().get(keyMap).setParams(etl);
    	gDatos.getMapTask().get(keyMap).setUpdateTime(gSub.getDateNow());
    	
    }
    
    private PreparedStatement genPrepareStatement(ETL etl, dataAccess dConn, ResultSet rs) throws SQLException {
		String pSql = "insert into "
				+ etl.getDTBNAME()
				+ " ( " 
				+ columnNames 
				+ " ) VALUES ( " 
				+ bindVariables	+ " ) ";
		
		psInsertar =  dConn.getConnection().prepareStatement(pSql);
		
		for (int nf=0; nf<etl.getLstEtlMatch().size(); nf++) {
			switch (etl.getLstEtlMatch().get(nf).getEtlSourceType()) {
				case "varchar":
					psInsertar.setString(nf+1, rs.getString(nf+1));
					break;
				case "int":
					psInsertar.setInt(nf+1, rs.getInt(nf+1));
					break;
				case "date":
					psInsertar.setDate(nf+1, rs.getDate(nf+1));
					break;
				default:
					psInsertar.setString(nf+1, rs.getString(nf+1));
			}
		}
		
		return psInsertar;
    }
    
    private void genColumnBind(ETL etl) {
    	
    	//Inicializa variables globales
    	
		columnNames = new StringBuilder();
		bindVariables = new StringBuilder();
		
		for (int nf=0; nf<etl.getLstEtlMatch().size(); nf++) {
			if (nf>0) {
				columnNames.append(", ");
				bindVariables.append(", ");
			}
	       columnNames.append(etl.getLstEtlMatch().get(nf).getEtlDestField());
	       bindVariables.append('?');
		}
    }
    
    private String getFecQueryIni(ETL etl, String fecIntervalIni) {
    	switch (etl.getFIELDTYPE()) {
        	case "date":
        		return fnConvertFecInterval(fecIntervalIni,"date",etl.getSDBTYPE());
        	case "int":
        		return fnConvertFecInterval(fecIntervalIni,"int",etl.getSDBTYPE());
        	case "varchar":
        		return fnConvertFecInterval(fecIntervalIni,"varchar",etl.getSDBTYPE());
        	default:
        		return "";
    	}
    }
    
    private String getFecQueryFin(ETL etl, String fecIntervalFin) {
    	switch (etl.getFIELDTYPE()) {
        	case "date":
        		return fnConvertFecInterval(fecIntervalFin,"date",etl.getSDBTYPE());
        	case "int":
        		return fnConvertFecInterval(fecIntervalFin,"int",etl.getSDBTYPE());
        	case "varchar":
        		return fnConvertFecInterval(fecIntervalFin,"varchar",etl.getSDBTYPE());
        	default:
        		return "";
    	}
    }
    
    private String getFecQueryDelIni(ETL etl, String fecIntervalIni) {
    	switch (etl.getFIELDTYPE()) {
        	case "date":
        		return fnConvertFecInterval(fecIntervalIni,"date",etl.getDDBTYPE());
        	case "int":
        		return fnConvertFecInterval(fecIntervalIni,"int",etl.getDDBTYPE());
        	case "varchar":
        		return fnConvertFecInterval(fecIntervalIni,"varchar",etl.getDDBTYPE());
        	default:
        		return "";
    	}
    }

    
    private String getFecQueryDelFin(ETL etl, String fecIntervalFin) {
    	switch (etl.getFIELDTYPE()) {
        	case "date":
        		return fnConvertFecInterval(fecIntervalFin,"date",etl.getDDBTYPE());
        	case "int":
        		return fnConvertFecInterval(fecIntervalFin,"int",etl.getDDBTYPE());
        	case "varchar":
        		return fnConvertFecInterval(fecIntervalFin,"varchar",etl.getDDBTYPE());
        	default:
        		return "";
    	}
    }
    
    private String genSqlQueryDbSource(ETL etl, Interval interval) {
    	String vQuery = new String(vSQLSource);
    	String fecQueryIni = getFecQueryIni(etl, interval.getFecIni());
    	String fecQueryFin = getFecQueryFin(etl, interval.getFecFin());
    	
    	vQuery = vQuery + etl.getFIELDKEY() + " >= " + fecQueryIni;
    	vQuery = vQuery + " and " + etl.getFIELDKEY() + " < " + fecQueryFin;
    	
    	return vQuery;
    }
    
    private String genSqlQueryDeleteDbSource(ETL etl, Interval interval) {
    	String vQuery;
    	
    	String fecQueryIni = getFecQueryDelIni(etl, interval.getFecIni());
    	String fecQueryFin = getFecQueryDelFin(etl, interval.getFecFin());
    	
    	vQuery = " delete from "+ etl.getDTBNAME();
    	vQuery = vQuery + " where " + etl.getFIELDKEY() + " >= " + fecQueryIni;
    	vQuery = vQuery + "  and " + etl.getFIELDKEY() + " < " + fecQueryFin;
    	
    	return vQuery;
    }
    
    private String genQueryExtract(ETL etl) {
    	try {
        	//Genera Query de Select DB Source y Dest
        	vSQLSource = "select ";
        	vSQLDest = "select ";
        	boolean first = true;
        	for (int i=0; i<etl.getLstEtlMatch().size(); i++) {
        		if (first) {
        			first = false;
        		} else {
        			vSQLSource = vSQLSource + ",";
        			vSQLDest = vSQLDest + ",";
        		}
        		switch (etl.getLstEtlMatch().get(i).getEtlDestType()) {
        			case "F":
        				vSQLSource = vSQLSource + etl.getLstEtlMatch().get(i).getEtlSourceField();
        				break;
        			case "T":
        				vSQLSource = vSQLSource + (char)34 + etl.getLstEtlMatch().get(i).getEtlSourceField() + (char)34;
        				break;
    				default:
    					vSQLSource = vSQLSource + etl.getLstEtlMatch().get(i).getEtlSourceField();
    					break;
        		}
        		vSQLDest = vSQLDest + etl.getLstEtlMatch().get(i).getEtlDestField();
        	}
        	//Agrega el From
        	
        	vSQLSource = vSQLSource + " From " + etl.getSTBNAME();
        	vSQLDest = vSQLDest + " From " + etl.getDTBNAME();
        	
        	//Agrega Where key de extracción
        	
        	vSQLSource = vSQLSource + " Where ";
        	vSQLDest = vSQLDest + " Where 1=2";
        	
        	logger.info("Query global de extracción generada: "+vSQLSource);
        	
    		return vSQLSource;
    	} catch (Exception e) {
    		logger.error("Error generando Query Extraccion..."+e.getMessage());
    		return null;
    	}
    }
    
    private String fnConvertFecInterval(String fecha, String dataType, String dbType) {
    	String dateConvert = "";
    	
    	switch (dbType) {
    		case "MYSQL":
    			switch (dataType) {
    				case "date":
    						dateConvert = "STR_TO_DATE("+fecha+",'%Y%m%d%H%i')";
    					break;
    				case "int":
    						dateConvert = "UNIX_TIMESTAMP('"+fecha+"')";
    					break;
    				case "varchar":
						break;
    			}
    			break;
    		case "SQL":
				break;
    		case "ORACLE":
    			break;
    	}
    	
    	return dateConvert;
    }
    
    public boolean isValidDataParam() {
        boolean isValid = true;
        
        try {
            if (taskProcess.getGrpID()==null || taskProcess.getProcID()==null || taskProcess.getIntervalID()==null || taskProcess.getNumSecExec()==null ||
            		taskProcess.getParams()==null) {
                //isValid = false;
            }
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}