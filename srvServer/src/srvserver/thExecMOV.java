/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;

import dataClass.MOV;
import dataClass.TaskProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;


import utilities.dataAccess;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thExecMOV extends Thread{
    TaskProcess taskProcess = new TaskProcess();
    String keyTask;
    String procType;
    
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
	//SQLServerPreparedStatement psInsertar;
	dataAccess sConn = new dataAccess(gDatos);
	dataAccess dConn = new dataAccess(gDatos);

    Logger logger = Logger.getLogger("srv.execMOV");
    
    public thExecMOV(globalAreaData m, TaskProcess taskProcess) {
        this.taskProcess = taskProcess;
        keyTask = taskProcess.getProcID()+":"+taskProcess.getNumSecExec();
        procType = taskProcess.getTypeProc();
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
    
    @Override
    public void run() {
    	String errMesg = "";
    	boolean statusExit=true;
    	try {
    		
    		
    		logger.info("Iniciando ejecución MOV:" + keyTask);
    		
    		gSub.updateStatusTask(keyTask, "Running");
    		
    		if (isValidDataParam()) {
    			
    			//Serializa Objetos
    			String movString = gSub.serializeObjectToJSon(taskProcess.getParams(), false);
    			MOV mov = (MOV) gSub.serializeJSonStringToObject(movString, MOV.class);
    			
    			if (gSub.isConnectedDB(mov, procType, sConn, dConn)) {
    				
    				/**
    				 * Genera Query de Extracción
    				 */
    				vSQLSource = genQueryExtract(mov);
    				
        			//Ejecuta Borrado de data del intervalo
					//if swDelete esta activo
	        			String vQueryDelete = genSqlQueryDeleteDbSource(mov);
	        			logger.info("Query de borrado: "+vQueryDelete);
	        			int rowsDeleted = dConn.execQuery(vQueryDelete);
	        			logger.info("Total filas borradas para tabla "+mov.getDTBNAME()+" :"+ rowsDeleted);

        			//Ejecuta Query de Extraccion de Datos
        			String vQueryExtract = genSqlQueryDbSource(mov);
        			logger.info("Query de extraccion: "+vQueryExtract);
        			ResultSet rsExtract = (ResultSet) sConn.getQuery(vQueryExtract);

    				int rowsLoad=0;
    				int rowsRead=0;

        			if (rsExtract!=null) {
        				int numShows=2000;
        				int iteShows=2000;
        				
        				genColumnBind(mov);
        				
        				while (rsExtract.next()) {
        					rowsRead++;
        					try {
	        					psInsertar = genPrepareStatement(mov, dConn, rsExtract);
	        					
	        					if (psInsertar.executeUpdate()==1) {
	        						rowsLoad++;
	        					}
        					} catch (Exception e) {
        						String vMesg = "Error insertando fila en tabla : "+mov.getDTBNAME();
        						vMesg = vMesg + "...: "+e.getMessage();
    							logger.error(vMesg);
        					}
        					
        					if (rowsRead==numShows) {
								logger.info("Filas leídas   MOV "+mov.getMovID()+ ": "+rowsRead);
								logger.info("Filas cargadas MOV "+mov.getMovID()+ ": "+rowsLoad);

								mov.setRowsRead(rowsRead);
								mov.setRowsLoad(rowsLoad);
								
					    		gDatos.getMapTask().get(keyTask).setParams(mov);
					    		gDatos.getMapTask().get(keyTask).setUpdateTime(gSub.getDateNow());

								
        						numShows=numShows + iteShows;
        					}
        				}
        				rsExtract.close();
        				
						mov.setRowsRead(rowsRead);
						mov.setRowsLoad(rowsLoad);

						gDatos.getMapTask().get(keyTask).setParams(mov);
			    		gDatos.getMapTask().get(keyTask).setUpdateTime(gSub.getDateNow());
			    		gDatos.getMapTask().get(keyTask).setStatus("Finished");
			    		gDatos.getMapTask().get(keyTask).setuStatus("Success");
			    		gDatos.getMapTask().get(keyTask).setEndTime(gSub.getDateNow());
			    		
	        			logger.info("Finalizando carga de MOV: "+mov.getMovID());
	        			logger.info("Se leyeron: "+rowsRead+" filas para la tabla: "+mov.getDTBNAME());
	        			logger.info("Se cargaron: "+rowsLoad+" filas para la tabla: "+mov.getDTBNAME());

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
    			logger.info("Termino Exitoso ejecucion MOV:"+keyTask);
        		gDatos.getMapTask().get(keyTask).setStatus("Finished");
        		gDatos.getMapTask().get(keyTask).setuStatus("Success");
        		gDatos.getMapTask().get(keyTask).setUpdateTime(gSub.getDateNow());

    		} else {
    			logger.error("Termino Erroneo ejecucion MOV:"+keyTask+ " Mesg: "+ errMesg);
        		gDatos.getMapTask().get(keyTask).setStatus("Finished");
        		gDatos.getMapTask().get(keyTask).setuStatus("Error");
        		gDatos.getMapTask().get(keyTask).setUpdateTime(gSub.getDateNow());

    		}
    		
    	} catch (Exception e) {
    		logger.error("Error en thExecMOV...: "+e.getMessage());
    		gDatos.getMapTask().get(keyTask).setStatus("Finished");
    		gDatos.getMapTask().get(keyTask).setuStatus("Error");
    		gDatos.getMapTask().get(keyTask).setUpdateTime(gSub.getDateNow());

    	}
    } //fin run()
    
	private PreparedStatement genPrepareStatement(MOV mov, dataAccess dConn, ResultSet rs)  {
		try {
			String pSql = "insert into "
					+ mov.getDTBNAME()
					+ " ( " 
					+ columnNames 
					+ " ) VALUES ( " 
					+ bindVariables	+ " ) ";
			
			logger.debug("Insertando fila: "+pSql);
			
			psInsertar =  dConn.getConnection().prepareStatement(pSql);
			
			String colValues="";
			
			colValues = "(";
			
			for (int nf=0; nf<mov.getLstMovMatch().size(); nf++) {
				switch (mov.getLstMovMatch().get(nf).getSourceType()) {
					case "varchar":
						psInsertar.setString(nf+1, rs.getString(nf+1));
						colValues = colValues + "'"+ rs.getString(nf+1) +"',";
						break;
					case "int":
						psInsertar.setInt(nf+1, rs.getInt(nf+1));
						colValues = colValues + "'"+ rs.getInt(nf+1) +"',";
						break;
					case "date":
						psInsertar.setDate(nf+1, rs.getDate(nf+1));
						colValues = colValues + "'"+ rs.getDate(nf+1) +"',";
						break;
					case "nvarchar":
						psInsertar.setString(nf+1, rs.getString(nf+1));
						colValues = colValues + "'"+ rs.getString(nf+1) +"',";
						break;
					default:
						psInsertar.setString(nf+1, rs.getString(nf+1));
						colValues = colValues + "'"+ rs.getString(nf+1) +"',";
				}
			}
			
			colValues = colValues + ")";
			
			logger.debug("values: "+colValues);
			
			return psInsertar;
		} catch (Exception e) {
			logger.error("Error en genPrepareStatement: "+e.getMessage());
			return null;
		}
    }
    
    private void genColumnBind(MOV mov) {
    	
    	//Inicializa variables globales
    	
		columnNames = new StringBuilder();
		bindVariables = new StringBuilder();
		
		for (int nf=0; nf<mov.getLstMovMatch().size(); nf++) {
			if (nf>0) {
				columnNames.append(", ");
				bindVariables.append(", ");
			}
	       columnNames.append(mov.getLstMovMatch().get(nf).getDestField());
	       bindVariables.append('?');
		}
    }
    
    private String genSqlQueryDbSource(MOV mov) {
    	String vQuery = new String(vSQLSource);
    	
    	//Validar si existe Where activo para agregarlo aqui
    	//
    	
    	return vQuery;
    }
    
    private String genSqlQueryDeleteDbSource(MOV mov) {
    	String vQuery;
    	    	
    	vQuery = " delete from "+ mov.getDTBNAME();
    	
    	return vQuery;
    }
    
    private String genQueryExtract(MOV mov) {
    	try {
        	//Genera Query de Select DB Source y Dest
        	vSQLSource = "select ";
        	vSQLDest = "select ";
        	boolean first = true;
        	for (int i=0; i<mov.getLstMovMatch().size(); i++) {
        		if (first) {
        			first = false;
        		} else {
        			vSQLSource = vSQLSource + ",";
        			vSQLDest = vSQLDest + ",";
        		}
        		switch (mov.getLstMovMatch().get(i).getDestType()) {
        			case "F":
        				vSQLSource = vSQLSource + mov.getLstMovMatch().get(i).getSourceField();
        				break;
        			case "T":
        				vSQLSource = vSQLSource + (char)34 + mov.getLstMovMatch().get(i).getSourceField() + (char)34;
        				break;
    				default:
    					vSQLSource = vSQLSource + mov.getLstMovMatch().get(i).getSourceField();
    					break;
        		}
        		vSQLDest = vSQLDest + mov.getLstMovMatch().get(i).getDestField();
        	}
        	//Agrega el From
        	
        	vSQLSource = vSQLSource + " From " + mov.getSTBNAME();
        	vSQLDest = vSQLDest + " From " + mov.getDTBNAME();
        	
        	//Agrega Where key de extracción
        	
        	//vSQLSource = vSQLSource + " Where ";
        	vSQLDest = vSQLDest + " Where 1=2";
        	
        	logger.info("Query global de extracción generada: "+vSQLSource);
        	
    		return vSQLSource;
    	} catch (Exception e) {
    		logger.error("Error generando Query Extraccion..."+e.getMessage());
    		return null;
    	}
    }
        
    public boolean isValidDataParam() {
        boolean isValid = true;
        
        try {
            if (taskProcess.getGrpID()==null || taskProcess.getProcID()==null || taskProcess.getNumSecExec()==null ||
            		taskProcess.getParams()==null) {
            }
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}