/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;
import utilities.globalAreaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import dataClass.ETL;
import dataClass.Interval;
import dataClass.TaskProcess;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thUpdateStatusDB extends Thread {
    static srvRutinas gSub;
    static globalAreaData gDatos;
    static Logger logger = Logger.getLogger("srv.updateStatusDB");
    
    //Carga constructor para inicializar los datos
    public thUpdateStatusDB(globalAreaData m) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
    
        
    @Override
    public void run() {
        Timer timerMain = new Timer("thSubUpdateStatus");
        timerMain.schedule(new mainKeepTask(), 1000, gDatos.getServerInfo().getTxpUpdateMD());
        logger.info("Se ha agendado thSubUpdateStatus cada "+gDatos.getServerInfo().getTxpUpdateMD());
    }
    
    
    static class mainKeepTask extends TimerTask {
    
        public mainKeepTask() {
        }

        @Override
        public void run() {

        try {
        	logger.info("Inicia Thread thSubUpdateStatus...");

    		/**
    		 * Actualiza Estado de las Task
    		 */
    		logger.info("Actualizando las Task en MD...");
            logger.info("Total de tareas informadas: "+ gDatos.getMapTask().size());
            logger.info("Total tareas en estado Ready: "+ gSub.getNumTaskStatus(gDatos.getMapTask(),"Ready"));
            logger.info("Total tareas en estado Running: "+ gSub.getNumTaskStatus(gDatos.getMapTask(),"Running"));
            logger.info("Total tareas en estado Abort: "+ gSub.getNumTaskStatus(gDatos.getMapTask(),"Abort"));
            logger.info("Total tareas en estado Finished: "+ gSub.getNumTaskStatus(gDatos.getMapTask(),"Finished"));

    		
    		if (gDatos.getMapTask().size()>0) {
    		
        		Map<String, TaskProcess> myMapTask = new TreeMap<>(gDatos.getMapTask());
                for (Map.Entry<String, TaskProcess> entry : myMapTask.entrySet()) {
                	
                	logger.info("Analizando la Tarea: "+entry.getKey());
                	logger.info("Status actual: "+entry.getValue().getStatus());
                	
                	switch (entry.getValue().getStatus()) {
                		case "Finished":
                			logger.info("Validando si todas las Tareas del Grupo finalizaron...");
	                		if (isGrupoFinished(entry.getValue().getGrpID(), entry.getValue().getNumSecExec())) {
	                			updateMDGrupoFinished(entry.getValue().getGrpID(), entry.getValue().getNumSecExec());
	                			gDatos.getMapTask().remove(entry.getKey());
	                		}
	                		break;
                		case "Running":
                			logger.info("Validando si tiempo de ejecucion del grupo esta expirado...");

                			logger.info("Valida que status grupo ejecucion este en Running...");
                			updateMDGrupoStatus(entry.getValue().getGrpID(), entry.getValue().getNumSecExec(),"Running");
                			
                			break;
                		case "Ready":
                			logger.info("Validando si tiempo de ejecucion del grupo esta expirado...");
                			break;
                		case "Pending":
                			logger.info("Validando si tiempo de ejecucion del grupo esta expirado...");
                			break;
            			default:
            				updateMDGrupoStatus(entry.getValue().getGrpID(), entry.getValue().getNumSecExec(),entry.getValue().getStatus());
                			
                	}
                	
                	/**
                	 * Actualiza intervalos informados para cada task del tipo ETL
                	 */
                	switch (entry.getValue().getTypeProc()) {
                		case "ETL":
                			
                			logger.info("Validando la actualización de intervalos del proceso...");
                			String etlString = gSub.serializeObjectToJSon(entry.getValue().getParams(), false);
                			ETL etl = new ETL();
                			etl = (ETL) gSub.serializeJSonStringToObject(etlString, ETL.class);
                			
                			//Recupera el MapInterval
                			Map<String, Interval> myMapInterval = new TreeMap<>(etl.getMapInterval());
                			
                			logger.info("Total intervalos del proceso: "+myMapInterval.size());
                			
                			for (Map.Entry<String, Interval> entryInt : myMapInterval.entrySet()) {
                				updateMDInterval(etl.getETLID(), entryInt.getValue());
                			}
                			
                			break;
            			default:
            				break;
                	}
                }
                
        	} else { //end if exist MapTask
        		logger.warn("No hay Tareas para Actualizar");
        	}
    		
    		
    		/**
    		 * Actualiza status desde MD hacia local TASK
    		 */
    		
    		logger.info("Validando grupos de ejecuciones registradas en MD");
    		actualizaStatusFromMD();
    		
        	
        	logger.info("Termino Thread thSubUpdateStatus...");
    	} catch (Exception e) {
    		logger.error("Error desconocido en thUpdateStatus "+e.getMessage());
    	}
        }
        
        private void actualizaStatusFromMD() {
        	metaData mdConn = new metaData(gDatos);
        	String vGrpID;
        	String vNumSecExec;
        	try {
        		mdConn.openConnection();
        		if (mdConn.isConnected()) {
        			
        			/**
        			 * Primera Etapa: Valida los grupos de ejecución
        			 */
        			
        			String vSql = mdConn.getSqlFindGrpExecHuerfans();
        			ResultSet rs = (ResultSet) mdConn.getQuery(vSql);
        			if (rs!=null) {
        				while (rs.next()) {
							vGrpID= rs.getString("grpID");
							vNumSecExec = rs.getString("numSecExec");
        					switch (rs.getString("status")) {
	        					case "Ready":
        							if (!isExistTaskToGrpExec(vGrpID, vNumSecExec)) {
        								vSql = mdConn.genSqlUpdateStatusAbortGrpExec(vGrpID, vNumSecExec);
        								int result = mdConn.executeQuery(vSql);
        								if (result==1) {
        									logger.info("Se abortó grupo en MD: "+ vGrpID + ":" + vNumSecExec);
        								}
        							} else {
        								//Valida tiempo de expiración
        							}
	        						break;
	        					case "Pending":
        							if (!isExistTaskToGrpExec(vGrpID, vNumSecExec)) {
        								vSql = mdConn.genSqlUpdateStatusAbortGrpExec(vGrpID, vNumSecExec);
        								int result = mdConn.executeQuery(vSql);
        								if (result==1) {
        									logger.info("Se abortó grupo en MD: "+ vGrpID + ":" + vNumSecExec);
        								}
        							} else {
        								//Valida tiempo de expiración
        							}
	        						break;
	    						default:
	    							break;
        					}
        				}
        				rs.close();
        				
        				
        			}
        			mdConn.closeConnection();
        		}
        		
        	} catch (Exception e) {
        		logger.error("Error en actualizaStatusMD...: "+e.getMessage());
        		if (mdConn.isConnected()) {
        			mdConn.closeConnection();
        		}
        	}
        }
        
        private boolean isExistTaskToGrpExec(String vGrpID, String vNumSecExec) {
        	boolean isExist=false;
        	try {
        		Map<String, TaskProcess> myTask = new TreeMap<>(gDatos.getMapTask());
        		for (Map.Entry<String, TaskProcess> entry : myTask.entrySet()) {
        			String mapGrpID = entry.getValue().getGrpID();
        			String mapNumSecExec = entry.getValue().getNumSecExec();
        			if (mapGrpID.equals(vGrpID)&&mapNumSecExec.equals(vNumSecExec)) {
        				isExist = true;
        				break;
        			}
        			
        		}
        		
        		return isExist;
        	} catch (Exception e) {
        		logger.error("Error en isExistTaskToGrpExec...: "+e.getMessage());
        		return false;
        	}
        }
        
        private void updateMDInterval(String etlID, Interval interval) {
        	metaData mdConn = new metaData(gDatos);
        	try {
	        	
	        	
	    		/**
	    		 * Busca si este IntevalID existe en DB
	    		 */
        		mdConn.openConnection();
        		if (mdConn.isConnected()) {
	
		    		boolean swExiste = false;
		    		
		    		String vSQL = mdConn.getSqlFindInterval(etlID, interval.getIntervalID());
		    		String vStatus="";
		    		String vNumSecExec="";
		    		ResultSet rs = (ResultSet) mdConn.getQuery(vSQL);
		    		if (rs!=null) {
		    			if (rs.next()) {
		    				vStatus = rs.getString("STATUS");
		    				vNumSecExec = rs.getString("NUMSECEXEC");
		    				rs.close();
		    				swExiste = true;
		    			} else {
		        			//No existe en MD
		        			//se debe insertar
		    			}
		    		} else {
		    			//No existe en MD
		    			//se debe insertar
		    		}
		    		
		    		if (swExiste) {
		    			
		    			if (!interval.getStatus().equals(vStatus)||!interval.getNumSecExec().equals(vNumSecExec)) {
		    			
			    			logger.info("Actualizando Intervalo: "+ etlID + ":" + interval.getIntervalID());
			
			    			PreparedStatement psUpdate = null;
							psUpdate = mdConn.getConnection().prepareStatement(
									"update tb_etlInterval set " +
									" numSecExec=?," +
									" status=?," +
									" ustatus=?," +
									" fecins=?," +
									" fecupdate=?," +
									" fecIni=?," +
									" fecFin=?," +
									" rowsLoad=?, " +
									" rowsRead=? "  +
									" where etlID='"+etlID+"' " + 
									" and intervalID='"+interval.getIntervalID()+"' " +
									" and status!='Finished'");
			            				
							psUpdate.setString(1, interval.getNumSecExec());
							psUpdate.setString(2, interval.getStatus());
							psUpdate.setString(3, interval.getuStatus());
							psUpdate.setString(4, interval.getFecIns());
							psUpdate.setString(5, interval.getFecUpdate());
							psUpdate.setString(6, interval.getFecIni());
							psUpdate.setString(7, interval.getFecFin());
							psUpdate.setInt(8, interval.getRowsLoad());
							psUpdate.setInt(9, interval.getRowsRead());
			            				
							int valor = psUpdate.executeUpdate();
							
							if (valor!=1) {
								logger.error("No pudo actualizar status intervalo: "+ etlID + ":" + interval.getIntervalID());
							}
							
							psUpdate.close();
		    			}
		            				
		    		} else {
		    			
		    			logger.info("Insertando intervalo:  "+ etlID + ":" + interval.getIntervalID());
		    			
						PreparedStatement psInsertar = null;
						psInsertar =  mdConn.getConnection().prepareStatement(
								"insert into tb_etlInterval "
								+ " ( " 
								+ " etlID, numsecexec, intervalID, fecIns, fecUpdate, status, ustatus, rowsLoad, rowsRead, intentos, fecIni, fecFin" 
								+ " ) VALUES ( " 
								+ " ?,?,?,?,?,?,?,?,?,?,?,? ) "
								);
						psInsertar.setString(1, etlID);
						psInsertar.setString(2, interval.getNumSecExec());
						psInsertar.setString(3, interval.getIntervalID());
						psInsertar.setString(4, interval.getFecIns());
						psInsertar.setString(5, interval.getFecUpdate());
						psInsertar.setString(6, interval.getStatus());
						psInsertar.setString(7, interval.getuStatus());
						psInsertar.setInt(8, interval.getRowsLoad());
						psInsertar.setInt(9, interval.getRowsRead());
						psInsertar.setInt(10, interval.getIntentos());
						psInsertar.setString(11, interval.getFecIni());
						psInsertar.setString(12, interval.getFecFin());
		
						int valor = psInsertar.executeUpdate();
						
						if (valor!=1) {
							logger.error("Error insertando intervalo: "+ etlID + ":" + interval.getIntervalID());
						}
					} 
		        	mdConn.closeConnection();
        		} else {
        			logger.error("No es posible conectarse a metadata en updateMDInterval");
        		}
        	} catch (Exception e) {
        		logger.error("Error en updateMDInterval...: "+ e.getMessage());
        		if (mdConn.isConnected()) {
        			mdConn.closeConnection();
        		}
        	}
        }
        
        private void updateMDGrupoStatus(String vGrpID, String vNumSecExec, String status) {
        	metaData mdConn = new metaData(gDatos);
        	try {
        		mdConn.openConnection();
        		if (mdConn.isConnected()) { 
		        	String vSql = "update tb_grpExec set " +
		        					" status='"+ status + "', " +
		        					" fecUpdate= Now() "+ 
				        			"where " +
				    				"  grpID='"+ vGrpID + "' " +
				        			"  and numSecExec = '"+ vNumSecExec + "'";
		        	int result = mdConn.executeQuery(vSql);
		        	
		        	mdConn.closeConnection();
        		} else {
        			logger.error("No es posible conectarse a metadata en updateMDGrupoStatus");
        		}
        	} catch (Exception e) {
        		logger.error("Error en updateMDGrupoStatus...: "+e.getMessage());
        		if (mdConn.isConnected()) {
        			mdConn.closeConnection();
        		}
        	}
        }
        
        private void updateMDGrupoFinished(String vGrpID, String vNumSecExec) {
        	metaData mdConn = new metaData(gDatos);
        	try {
	        	mdConn.openConnection();
	        	if (mdConn.isConnected()) {
		        	String vSql = "update tb_grpExec " +
		        					" set status='Finished', fecUpdate = NOW() " +
				        			"where " +
				    				"  grpID='"+ vGrpID + "' " +
				        			"  and numSecExec = '"+ vNumSecExec + "'";
		        	int result = mdConn.executeQuery(vSql);

		        	/**
		        	 * Registra Termino en Bitacora
		        	 */
		        	
		        	mdConn.closeConnection();
		        	
		        	logger.info("Finalizado el Grupo: "+vGrpID+":"+vNumSecExec+ " result: "+result);
	        	} else {
	        		logger.error("No es posible conectarse a metadata en updateMDGrupoFinished");
	        	}
        	} catch (Exception e) {
        		logger.error("Error en updateMDGrupoFinished...: "+ e.getMessage());
        		if (mdConn.isConnected()) {
        			mdConn.closeConnection();
        		}
        	}
        }
        
        private boolean isGrupoFinished(String vGrpID, String vNumSecExec) {
        	boolean isFinished = true;
    		Map<String, TaskProcess> myMapTask = new TreeMap<>(gDatos.getMapTask());
            for (Map.Entry<String, TaskProcess> entry : myMapTask.entrySet()) {
            	if (entry.getValue().getGrpID().equals(vGrpID)&&entry.getValue().getNumSecExec().equals(vNumSecExec)) {
	            	if (!entry.getValue().getStatus().equals("Finished")
	            			&&!entry.getValue().getStatus().equals("Error")
	            			&&!entry.getValue().getStatus().equals("Abort")
	            			) {
	            		isFinished = false;
	            		break;
	            	}
            	}
            }
            return isFinished;
        }
    }
}
