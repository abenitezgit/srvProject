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
public class thExecETL2 extends Thread{
	boolean isConnectDBSource;
	boolean isConnectDBDest;
    TaskProcess taskProcess = new TaskProcess();
    static srvRutinas gSub;
    static globalAreaData gDatos;
    Map<String, Interval> mapInterval;
    ETL etl = new ETL();
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


    Logger logger = Logger.getLogger("thExecETL");
    
    public thExecETL2(globalAreaData m, TaskProcess taskProcess) {
        this.taskProcess = taskProcess;
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
    
    @Override
    public void run() {
        logger.info("Iniciando ejecución ETL:"+taskProcess.getProcID()+" "+taskProcess.getNumSecExec());
        
        if (isValidDataParam()) {
        	
            String etlString="";
			try {
				etlString = gSub.serializeObjectToJSon(taskProcess.getParams(), false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
            
            try {
				etl = (ETL) gSub.serializeJSonStringToObject(etlString, ETL.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        	/**
        	 * Establece conexion hacia BD Origen
        	 */
        	logger.info("Establece conexión a Base Origen: "+etl.getSDBNAME());
        	logger.info("dbType: "+etl.getSDBTYPE());
        	logger.info("IP: "+etl.getSIP());
        	logger.info("Port: "+etl.getSDBPORT());
        	logger.info("UserName: "+etl.getSUSERNAME());
        	logger.debug("Pass: "+etl.getSUSERPASS());
        	
            dataAccess sConn = new dataAccess(gDatos);
            
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
            
            dataAccess dConn = new dataAccess(gDatos);
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
            
	            /**
	             * Genera nuevos intervalos faltantes
	             */
	            
	            logger.info("Inscribiendo intervalos faltantes para el Task: "+taskProcess.getProcID()+":"+taskProcess.getNumSecExec());
	        	if (genNewIntervals()) {
	        		
	        		//Genera informacion de log de total de intervalos por status
	        		//Inicializa los contadores
	        		gNumIntError=0;
	        		gNumIntFinished=0;
	        		gNumIntReady=0;
	        		gNumIntRunning=0;
	        		displayStatusIntervals();
	        		
	        		
	        		//Analiza Intervalos que están Running para revisar si existe task asociados
	        		//o deberán marcarse con error
	        		if (gNumIntRunning>0) {
	        			
	        		}
	        		
	        		
	        		//Si existen intervalos en Ready prepara su ejecución
	        		if (gNumIntReady>0) {
	        			String vSQLSource = genQueryExtract();
	        			if (vSQLSource!=null) {
	        					//Recorre lista de MapInterval buscando intervalos en estado Ready 
	        					//para su ejecución
	        				
	        					//Contador para quebrar ciclo con determinado numero de procesos a 
	        					//ejecutar
			    	        	int maxInterval=10000;
			    	        	int numInterval=0;
			    	        	
			    	        	Map<String, Interval> myMapInterval = new TreeMap<>(gDatos.getMapInteval());
			    	        	for (Map.Entry<String, Interval> entry : myMapInterval.entrySet()) {
			    	        		
			    	        		//Control para el break del for
			    	        		numInterval++;
			    	        		if (numInterval>maxInterval) {
			    	        			break;
			    	        		}
			    	        		
			    	        		//Extrae status y numSec del intervalo actual
			    	        		String vStatus = entry.getValue().getStatus();
			    	        		String vNumSec = entry.getValue().getNumSecExec();
			    	        		
			    	        		//Solo ejecutará intervalos que le pertenecen al numero de secuencia 
			    	        		//del Task actual
			    	        		if (vStatus.equals("Ready")&&vNumSec.equals(taskProcess.getNumSecExec())) {

			    	        			int rowsLoad=0;
		    	        				int rowsRead=0;

			    	        			updateStatusInterval(entry.getKey(), "Running");
			    	        			
			    	        			String IntervalFecIni = entry.getValue().getFecIni();
			    	        			String IntervalFecFin = entry.getValue().getFecFin();
			    	        			
			    	        			//Ejecuta Borrado de data del intervalo
			    	        			String vQueryDelete = genSqlQueryDeleteDbSource(IntervalFecIni, IntervalFecFin);
			    	        			int rowsDeleted = dConn.execQuery(vQueryDelete);
			    	        			logger.info("Total filas borradas para intevalo "+entry.getKey()+" :"+rowsDeleted);
			    	        			
			    	        			String vQueryExtract = genSqlQueryDbSource(IntervalFecIni,IntervalFecFin);
			    	        			ResultSet rsExtract = (ResultSet) dConn.getQuery(vQueryExtract);
			    	        			if (rsExtract!=null) {
			    	        				
			    	        				int numShows=2000;
			    	        				int iteShows=2000;
			    	        				
			    	        				genColumnBind();
			    	        				
			    	        				try {
				    	        				while (rsExtract.next()) {
				    	        					rowsRead++;
				    	        					
				    	        					try {
					    	        					psInsertar = genPrepareStatement(dConn, rsExtract);
					    	        					
					    	        					if (psInsertar.executeUpdate()==1) {
					    	        						rowsLoad++;
					    	        					}
				    	        					} catch (Exception e) {
				    	        						String vMesg = "Error insertando fila en intervalo: "+entry.getKey();
				    	        						vMesg = vMesg + "...: "+e.getMessage();
			    	        							logger.error(vMesg);
				    	        					}
				    	        					
				    	        					if (rowsRead==numShows) {
				    	        						//Muestra parcial filas cargadas
														logger.info("Filas leídas   Etl-Interval "+entry.getKey()+ ": "+rowsRead);
														logger.info("Filas cargadas Etl-Interval "+entry.getKey()+ ": "+rowsLoad);
							    						gDatos.getMapInteval().get(entry.getKey()).setRowsLoad(rowsLoad);
							    						gDatos.getMapInteval().get(entry.getKey()).setRowsRead(rowsRead);
							    						
							    						gDatos.getMapTask().replace(taskProcess.getProcID()+":"+taskProcess.getNumSecExec(), taskProcess);

				    	        						numShows=numShows + iteShows;
				    	        					}
				    	        				}
				    	        				rsExtract.close();
				    	        				
			    	        				} catch (Exception e) {
		    	        						String vMesg = "Error recorriendo recordset de extraccion: "+entry.getKey();
		    	        						vMesg = vMesg + "...: "+e.getMessage();
	    	        							logger.error(vMesg);
			    	        				}
			    	        			}
			    	        			
			    	        			//Termino de Ejecucion de cada intervalo cargado
			    	        			logger.info("Finalizando caga de Etl-Interval: "+entry.getKey());
			    						logger.info("Se leyeron: "+rowsRead+" filas para el intervalo: "+entry.getKey());
			    						logger.info("Se cargaron: "+rowsLoad+" filas para el intervalo: "+entry.getKey());
			    	        			
			    	        			updateStatusInterval(entry.getKey(), "Finished");
			    	        			
			    	        		}
			    	        	}
	        				
			    	        	//Termino Normal de Ejecucion de Todos los intervalos encontrados
			    	        	String keyTask = taskProcess.getProcID()+":"+taskProcess.getNumSecExec();
			    	        	updateStatusTask(keyTask, "Finished");
	        				
	        				
	        			} else {
	        				logger.error("No se pudo generar Query de Extraccion");
	        				
		    	        	String keyTask = taskProcess.getProcID()+":"+taskProcess.getNumSecExec();
		    	        	updateStatusTask(keyTask, "Error");
	        			}
	        		}
	        	} else {
	        		String vMesg="Error en Task: "+taskProcess.getProcID()+":"+taskProcess.getNumSecExec();
	        		vMesg = vMesg + "...: No pudo generar intervalos para el proceso ETL";
	        		logger.error(vMesg);
	        		
    	        	String keyTask = taskProcess.getProcID()+":"+taskProcess.getNumSecExec();
    	        	updateStatusTask(keyTask, "Error");
	        	}
            } else {
            	String vMesg = "Error en Task: "+taskProcess.getProcID()+":"+taskProcess.getNumSecExec();
            	if (!isConnectDBDest) {
            		vMesg = vMesg + "...: No pudo conectarse a base Destino";
            	}
            	if (!isConnectDBSource) {
            		vMesg = vMesg + "...: No pudo conectarse a base Origen";
            	}
            	logger.error(vMesg);
            	
            	String keyTask = taskProcess.getProcID()+":"+taskProcess.getNumSecExec();
            	updateStatusTask(keyTask, "Error");
            }
        } else {
        	String vMesg = "Error en Task: "+taskProcess.getProcID()+":"+taskProcess.getNumSecExec();
        	vMesg = vMesg + "...: No pudo realizar valiacion de parametros del ETL";

        	String keyTask = taskProcess.getProcID()+":"+taskProcess.getNumSecExec();
        	updateStatusTask(keyTask, "Error");
        }
    } //fin run()
    
    private void updateStatusTask(String keyTask, String vStatus) {
    	gDatos.getMapTask().get(keyTask).setStatus(vStatus);
    	gDatos.getMapTask().get(keyTask).setuStatus(vStatus);
    	gDatos.getMapTask().get(keyTask).setUpdateTime(gSub.getDateNow());
    	if (vStatus.equals("Finished")) {
    		gDatos.getMapTask().get(keyTask).setEndTime(gSub.getDateNow());
    	}
    }
    
    private PreparedStatement genPrepareStatement(dataAccess dConn, ResultSet rs) throws SQLException {
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
    
    private void genColumnBind() {
		columnNames = new StringBuilder();
		bindVariables = new StringBuilder();
		
		int totalCols=0;
		for (int nf=0; nf<etl.getLstEtlMatch().size(); nf++) {
			if (nf>0) {
				columnNames.append(", ");
				bindVariables.append(", ");
			}
	       columnNames.append(etl.getLstEtlMatch().get(nf).getEtlDestField());
	       bindVariables.append('?');
	       totalCols = nf+1;
		}
    }
    
    private void updateStatusInterval(String keyInterval, String vStatus) {
    	try {
	    	/** 
	    	 * Actualiza Status del Interval y TaskProcess
	    	 */
	    	gDatos.getMapInteval().get(keyInterval).setFecIns(gSub.getDateNow());
	    	gDatos.getMapInteval().get(keyInterval).setFecUpdate(gSub.getDateNow());
	    	gDatos.getMapInteval().get(keyInterval).setStatus(vStatus);
	    	
	    	taskProcess.setUpdateTime(gSub.getDateNow());
	    	taskProcess.setStartTime(gSub.getDateNow());
	    	
	    	gDatos.getMapTask().replace(taskProcess.getProcID()+":"+taskProcess.getNumSecExec(), taskProcess);
	    	
    	} catch (Exception e) {
    		String vMesg= "Error actualizando status Interval: "+keyInterval;
    		vMesg = vMesg + "...: "+e.getMessage();
    		logger.error(vMesg);
    	}
    }
    
    private String getFecQueryIni(String fecIntervalIni) {
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
    
    private String getFecQueryFin(String fecIntervalFin) {
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
    
    private String getFecQueryDelIni(String fecIntervalIni) {
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

    
    private String getFecQueryDelFin(String fecIntervalFin) {
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
    
    private String genSqlQueryDbSource(String vIntervalFecIni, String vIntervalFecFin) {
    	String vQuery = new String(vSQLSource);
    	String fecQueryIni = getFecQueryIni(vIntervalFecIni);
    	String fecQueryFin = getFecQueryFin(vIntervalFecFin);
    	
    	vQuery = vQuery + etl.getFIELDKEY() + " >= " + fecQueryIni;
    	vQuery = vQuery + " and " + etl.getFIELDKEY() + " < " + fecQueryFin;
    	
    	return vQuery;
    }
    
    private String genSqlQueryDeleteDbSource(String vIntervalFecIni, String vIntervalFecFin) {
    	String vQuery;
    	
    	String fecQueryIni = getFecQueryDelIni(vIntervalFecIni);
    	String fecQueryFin = getFecQueryDelFin(vIntervalFecFin);
    	
    	vQuery = " delete from "+ etl.getDTBNAME();
    	vQuery = vQuery + " where " + etl.getFIELDKEY() + " >= " + fecQueryIni;
    	vQuery = vQuery + "  and " + etl.getFIELDKEY() + " < " + fecQueryFin;
    	
    	return vQuery;
    }
    
    private boolean genNewIntervals() {
    	try {
	        //Extre Fecha Actual
	        Date today;
	        Date fecGap;
	        Date fecIni;
	        Date fecItera;
	        Date fecIntervalIni;
	        Date fecIntervalFin;
	
	        int MinItera;
	        int HoraItera;
	        int DiaItera;
	        int MesItera;
	        int AnoItera;
	
	        long numInterval;
	        String localIntervalID;
	        String todayChar;


	        //Setea Fecha Actual
	        //
	        today = new Date();
	        
	        //Variables del Objeto ETL
	        //
	        int vTimeGap = etl.getTIMEGAP();
	        int vTimePeriod = etl.getTIMEPERIOD();
	        int vTimeGen = etl.getTIMEGEN();
	        String vUnitMeasure = etl.getUNITMEASURE();
	        String vETLID = etl.getETLID();
            
	        //Setea Fecha GAP - Desface de tiempo en extraccion
	        //
	        Calendar c = Calendar.getInstance();
	        c.add(Calendar.MINUTE, -(vTimeGap+vTimePeriod));
	        fecGap = c.getTime();

	        //Setea Fecha Inicio Inscripcion/Revision de Intervalos
	        //
	
	        c.setTime(today);
	        c.add(Calendar.MINUTE, -vTimeGen);
	        fecIni = c.getTime();

	        logger.info("Datos del ETLID: "+vETLID);
	        logger.info("Fecha Actual: "+ today);
	        logger.info("Fecha GAP   : "+ fecGap);
	        logger.info("Fecha IniIns: "+ fecIni);
        
	        fecItera = fecIni;
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	        SimpleDateFormat sdfToday = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        String IntervalIni;
	        String IntervalFin;
	        Interval interval;
	        String keyMap;
        
	        while (fecItera.compareTo(fecGap) < 0) {
	            //Crea Objecto Interval
	            //
	            interval = new Interval();
	
	            //Extrae Intervalo para Fecha fecItera
	            //
	            c.setTime(fecItera);
	            AnoItera = c.get(Calendar.YEAR);
	            MesItera = c.get(Calendar.MONTH);
	            DiaItera = c.get(Calendar.DAY_OF_MONTH);
	            HoraItera = c.get(Calendar.HOUR_OF_DAY);
	            MinItera = c.get(Calendar.MINUTE);
	
	            //Valida si el intervalo de extraccion (cETL_INTERVALUNIDAD) es por:
	            //  Minutos     : 0
	            //  Horas       : 1
	            //  Dias        : 2
	            //  Semanas     : 3
	            //  Mensuales   : 4
	            //  Anuales     : 5
	
	            switch (vUnitMeasure) {
	                case "MINUTE":
	                    fecIntervalIni = null;
	                    fecIntervalFin = null;
	                    numInterval = 60/vTimePeriod;
	                    for (int i=1;i<=numInterval;i++) {
	                        c.set(AnoItera, MesItera, DiaItera, HoraItera, (i)*vTimePeriod,0);
	                        fecIntervalFin = c.getTime();
	                        if (fecIntervalFin.compareTo(fecItera) >0 ) {
	                            c.set(AnoItera, MesItera, DiaItera, HoraItera, (i-1)*vTimePeriod,0);
	                            fecIntervalIni = c.getTime();
	                            break;
	                        }
	                    }
	                    c.setTime(fecItera);
	                    c.add(Calendar.MINUTE, vTimePeriod);
	                    fecItera = c.getTime();
	
	
	                    IntervalIni = sdf.format(fecIntervalIni);
	                    IntervalFin = sdf.format(fecIntervalFin);
	                    localIntervalID = IntervalIni+'-'+IntervalFin;
	
//	                    logger.debug("Datos de Inicio de Extraccion de Intervalos...");
//	                    logger.debug("Fecha inicio intervalo: "+ fecIntervalIni);
//	                    logger.debug("Fecha fin Intervalo: "+fecIntervalFin);
//	                    logger.debug("IntervalID generado: "+localIntervalID);
	
	                    interval.setETLID(vETLID);
	                    interval.setIntervalID(localIntervalID);
	                    interval.setStatus("Ready");
	                    interval.setNumSecExec(taskProcess.getNumSecExec());
	                    interval.setFecIns(sdfToday.format(today));
	                    interval.setFecUpdate(sdfToday.format(today));
	                    interval.setFecIni(IntervalIni);
	                    interval.setFecFin(IntervalFin);
	
	                    keyMap = interval.getETLID()+":"+interval.getIntervalID();
	                    if (!gDatos.getMapInteval().containsKey(keyMap)) {
	                    	gDatos.getMapInteval().put(keyMap, interval);
	                    	logger.info("Se inscribió intrevalID: "+keyMap);
	                    }
	
	                    break;
	
	                    case "HOUR":
	                        fecIntervalIni = null;
	                        fecIntervalFin = null;
	                        numInterval = 24/vTimePeriod;
	                        for (int i=1;i<=numInterval;i++) {
	                            c.set(AnoItera, MesItera, DiaItera, (i)*vTimePeriod, 0, 0);
	                            fecIntervalFin = c.getTime();
	                            if (fecIntervalFin.compareTo(fecItera) >0 ) {
	                                c.set(AnoItera, MesItera, DiaItera, (i-1)*vTimePeriod, 0, 0);
	                                fecIntervalIni = c.getTime();
	                                break;
	                            }
	                        }
	                        c.setTime(fecItera);
	                        c.add(Calendar.HOUR_OF_DAY, vTimePeriod);
	                        fecItera = c.getTime();
	
	                        IntervalIni = sdf.format(fecIntervalIni);
	                        IntervalFin = sdf.format(fecIntervalFin);
	                        localIntervalID = IntervalIni+'-'+IntervalFin;                    
	
	
//	                        logger.debug("Datos de Inicio de Extraccion de Intervalos...");
//	                        logger.debug("Fecha inicio intervalo: "+ fecIntervalIni);
//	                        logger.debug("Fecha fin Intervalo: "+fecIntervalFin);
//	                        logger.debug("IntervalID generado: "+localIntervalID);
	
	                        interval.setETLID(vETLID);
	                        interval.setIntervalID(localIntervalID);
	                        interval.setStatus("Ready");
	                        interval.setNumSecExec(taskProcess.getNumSecExec());
	                        interval.setFecIns(sdfToday.format(today));
	                        interval.setFecUpdate(sdfToday.format(today));
	                        interval.setFecIni(IntervalIni);
	                        interval.setFecFin(IntervalFin);
	
		                    keyMap = interval.getETLID()+":"+interval.getIntervalID();
		                    if (!gDatos.getMapInteval().containsKey(keyMap)) {
		                    	gDatos.getMapInteval().put(keyMap, interval);
		                    	logger.info("Se inscribió intrevalID: "+keyMap);
		                    }
	
	                        break;
	
	                    case "2":
	                    case "3":
	                    case "4":
	                    case "5":
	                    default:
	                } //end switch
	            } //end while
	        return true;
    	} catch (Exception e) {
    		logger.error("Error generando intervalos faltantes..."+e.getMessage());
    		return false;
    	}
    }
    
    private void displayStatusIntervals() {
    	Map<String, Interval> myMapInterval = new TreeMap<>(gDatos.getMapInteval());

        for (Map.Entry<String, Interval> entry : myMapInterval.entrySet()) {
            switch (entry.getValue().getStatus()) {
            	case "Finished":
            		gNumIntFinished++;
            		break;
            	case "Ready":
            		gNumIntReady++;
            		break;
            	case "Error":
            		gNumIntError++;
            		break;
            	case "Running":
            		gNumIntRunning++;
            }
        }
        logger.info("Total Intervalos en MapInterval: "+gDatos.getMapInteval().size());
        logger.info("Intervalos Finished: "+gNumIntFinished);
        logger.info("Intervalos Ready: "+gNumIntReady);
        logger.info("Intervalos Error: "+gNumIntError);
        logger.info("Intervalos Running: "+gNumIntRunning);
    }
    
    private String genQueryExtract() {
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