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
    static Logger logger = Logger.getLogger("thUpdateStatusDB");
    
    //Carga constructor para inicializar los datos
    public thUpdateStatusDB(globalAreaData m) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
    
        
    @Override
    public void run() {
        Timer timerMain = new Timer("thSubUpdateStatus");
        timerMain.schedule(new mainKeepTask(), 1000, 10000);
        logger.info("Se ha agendado thSubUpdateStatus cada 10 segundos");
    }
    
    
    static class mainKeepTask extends TimerTask {
    
        public mainKeepTask() {
        }

        @Override
        public void run() {

        try {
        	logger.info("Inicia Thread thSubUpdateStatus...");

        	/**
        	 * Establece conexion a Metadata
        	 */
        	MetaData md = new MetaData(gDatos);
        	
        	if (md.isConnected()) {
        		logger.info("Conectado a MD!");
        		/**
        		 * Actualiza Estado de las Task
        		 */
        		logger.info("Total Task informados: "+gDatos.getMapTask().size());
        		
//        		Map<String, TaskProcess> myMapTask = new TreeMap<>(gDatos.getMapTask());
//                for (Map.Entry<String, TaskProcess> entry : myMapTask.entrySet()) {
//                	if (entry.getValue().getStatus().equals("Finished")) {
//                		if (isGrupoFinished(entry.getValue().getGrpID(), entry.getValue().getNumSecExec())) {
//                			updateMDGrupoFinished(entry.getValue().getGrpID(), entry.getValue().getNumSecExec());
//                			gDatos.getMapTask().remove(entry.getKey());
//                		}
//                	}
//                }
                
                
                /*
                 * Actualiza Estado de los Intervalos
                 * 
                 */

            	logger.info("Total de intervalos para actualizar: "+gDatos.getMapInterval().size());
            	
            	Map<String, Interval> myMapInterval = new TreeMap<>(gDatos.getMapInterval());
            	for (Map.Entry<String, Interval> entry : myMapInterval.entrySet()) {
            		
            		/**
            		 * Busca si este IntevalID existe en DB
            		 */
            		logger.info("Buscando Intervalo: "+entry.getKey());

        			String etlID	  = entry.getValue().getETLID();
            		String intervalID = entry.getValue().getIntervalID();
            		
            		boolean swExiste = false;
            		
            		String vSQL = md.getSqlFindInterval(etlID, numSecExec, intervalID);
            		ResultSet rs = (ResultSet) md.getQuery(vSQL);
            		if (rs!=null) {
            			if (rs.next()) {
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

            			PreparedStatement psUpdate = null;
        				psUpdate = md.getConnection().prepareStatement(
        						"update tb_etlInterval set " +
								" status=?," +
								" ustatus=?," +
								" fecins=?," +
								" fecupdate=?," +
								" fecIni=?," +
								" fecFin=?," +
								" rowsLoad=?, " +
								" rowsRead=? "  +
								" where etlID='"+etlID+"'" + 
								" and intervalID='"+intervalID+"'");
                    				
        				psUpdate.setString(1, entry.getValue().getStatus());
        				psUpdate.setString(2, entry.getValue().getuStatus());
        				psUpdate.setString(3, entry.getValue().getFecIns());
        				psUpdate.setString(4, entry.getValue().getFecUpdate());
        				psUpdate.setString(5, entry.getValue().getFecIni());
        				psUpdate.setString(6, entry.getValue().getFecFin());
        				psUpdate.setInt(7, entry.getValue().getRowsLoad());
        				psUpdate.setInt(8, entry.getValue().getRowsRead());
                    				
        				int valor = psUpdate.executeUpdate();
        				psUpdate.close();
                    				
        				logger.info("Se actualizó intervalo en MD: "+entry.getKey());
        				//Eliminando Intervalo del MapInterval si su estado es Finished
        				if (entry.getValue().getStatus().equals("Finished")) {
        					gDatos.getMapInterval().remove(entry.getKey());
        				}
                    				
            		} else {
            			
        				PreparedStatement psInsertar = null;
        				psInsertar =  md.getConnection().prepareStatement(
								"insert into tb_etlInterval "
								+ " ( " 
								+ " etlID, numsecexec, intervalID, fecIns, fecUpdate, status, ustatus, rowsLoad, rowsRead, intentos, fecIni, fecFin" 
								+ " ) VALUES ( " 
								+ " ?,?,?,?,?,?,?,?,?,?,?,? ) "
								);
        				psInsertar.setString(1, entry.getValue().getETLID());
        				psInsertar.setString(2, entry.getValue().getNumSecExec());
        				psInsertar.setString(3, entry.getValue().getIntervalID());
        				psInsertar.setString(4, entry.getValue().getFecIns());
        				psInsertar.setString(5, entry.getValue().getFecUpdate());
        				psInsertar.setString(6, entry.getValue().getStatus());
        				psInsertar.setString(7, entry.getValue().getuStatus());
        				psInsertar.setInt(8, entry.getValue().getRowsLoad());
        				psInsertar.setInt(9, entry.getValue().getRowsRead());
        				psInsertar.setInt(10, entry.getValue().getIntentos());
        				psInsertar.setString(11, entry.getValue().getFecIni());
        				psInsertar.setString(12, entry.getValue().getFecFin());

    					int valor = psInsertar.executeUpdate();
                					
    					logger.info("Se insertó intervalo en MD: "+entry.getKey());

        			} 
            	} //end for
        	} else { //end if is md connected 
        		logger.error("No es posible conctarse a MD");
        	}
        	md.closeConnection();
        	
        	logger.info("Termino Thread thSubUpdateStatus...");
    	} catch (Exception e) {
    		logger.error("Error desconocido en thUpdateStatus "+e.getMessage());
    	}
        }
        
        private void updateMDGrupoFinished(String vGrpID, String vNumSecExec) {
        	MetaData md = new MetaData(gDatos);
        	String vSql = "update tb_group set status='Sleeping', uStatus='Finished', LastNumSecExec='"+vNumSecExec+"' " +
    				"  uFechaExec = Now() " +
        			"where " +
    				"  grpID='"+ vGrpID + "' ";
        	int result = md.executeQuery(vSql);
        	logger.info("Finalizado el Grupo: "+vGrpID+":"+vNumSecExec+ " result: "+result);
        }
        
        private boolean isGrupoFinished(String vGrpID, String vNumSecExec) {
        	boolean isFinished = true;
    		Map<String, TaskProcess> myMapTask = new TreeMap<>(gDatos.getMapTask());
            for (Map.Entry<String, TaskProcess> entry : myMapTask.entrySet()) {
            	if (entry.getValue().getGrpID().equals(vGrpID)&&entry.getValue().getNumSecExec().equals(vNumSecExec)) {
	            	if (!entry.getValue().getStatus().equals("Finished")) {
	            		isFinished = false;
	            		break;
	            	}
            	}
            }
            return isFinished;
        }
    }
}
