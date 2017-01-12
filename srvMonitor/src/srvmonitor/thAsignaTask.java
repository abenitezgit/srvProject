/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;

import java.util.List;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import dataClass.Dependence;
import dataClass.Grupo;
import dataClass.TaskProcess;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thAsignaTask extends Thread{
    globalAreaData gDatos;
    srvRutinas mylib;
    static Logger logger = Logger.getLogger("thAsignaTask");
    Map<String,TaskProcess> mapTask = new TreeMap<>(); //Local MapTask

    public thAsignaTask(globalAreaData m) {
        gDatos = m;
        mylib= new srvRutinas(gDatos);
    }
    
    @Override
    public void run() {
        Timer timerMain = new Timer("thSubAsignaTask");
        timerMain.schedule(new mainTask(), 10000, gDatos.getServerInfo().getTxpAsigna());
        
    }
    
    class mainTask extends TimerTask{
    	metaData md;

        @Override
        public void run() {
	    	try {
	            logger.info("Inicio Proceso Asigna Tareas...");
	            
	            /**
	             * Establece conexión a Metadata
	             */
	            try {
	            	md = new metaData(gDatos);
	            	md.openConnection();
	            } catch (Exception e) {
	            	logger.error("No pudo conectarse a MetaData");
	            }
	            
	            /*
	             * Como los Task ya fueron creados en thread InscribeTask
	             * se debe validar la dependencia de los process dependiente de un grupo
	             * para liberar una task y cambiar estado desde Pending a Ready
	             */
	            
	            /*
	             * Valida Servicios Registrados para realizar asignacion de procesos
	             */
	            if (true) {
	            //if (gDatos.getMapServiceStatus().size()>0) {
	            	
	            	logger.info("Se encontraron "+gDatos.getMapServiceStatus().size()+ " servicios registrados");
	            	
	            	/*
	            	 * Al recorrer el MapTask se debe validar que cada tipo de proceso pueda ser ejecutado
	            	 * en un servicio inscrito.
	            	 * Se debe comparar la lista de Assigned y lista de ActiveType 
	            	 */
	            	
	            	if (gDatos.getMapTask().size()>0) {
	            		
	        			//Extrae una copia de MapTask global
	            		Map<String, TaskProcess> vMapTask = new TreeMap<>(gDatos.getMapTask());
	        			for (Map.Entry<String, TaskProcess> entry : vMapTask.entrySet()) {
		        			 /*
		        			  * Analiza por status de TaskProcess para determinar la accion a realizar
		        			  */
        					 logger.info("Analizando Task: "+entry.getKey()+ " status: "+entry.getValue().getStatus());
		        			 switch (entry.getValue().getStatus()) {
		            			 case "Pending":
		            				 if (isFinishedDependences(entry.getValue())) {
		            					 gDatos.updateStatusMapTask(entry.getKey(), "Ready");
		            					 updateStatusReadyGrupoExec(entry.getValue());
		            					 logger.info("Se Asigno Task: "+entry.getKey());
		            				 }
		            				 break;
		        				 default:
		        					 break;
		        			 }
	            		 }
	            		
	            	} //else no hay tareas mapeadas
	            } else {//else no hay servicios registrados
	            	logger.info("Aun no hay servicios registrados para asignar TaskProcess");
	            }
            
	            md.closeConnection();
            
	            logger.info("Termino Proceso Asigna Tareas");
            
	    	} catch (Exception e ) {
	    		logger.error("Error en Proceso Asigna Tareas...: "+e.getMessage());
	    	}
        } //fin run()
        
	    private boolean isFinishedDependences(TaskProcess task) {
	    	try {
	    		boolean isFree=true;
	    		Dependence dependence;
	    		List<Dependence> lstDependences = new ArrayList<>();
	    		lstDependences = gDatos.getMapGrupo().get(task.getGrpID()).getLstDepend();
	    		for (int i=0; i<lstDependences.size(); i++) {
	    			dependence = new Dependence();
	    			dependence = lstDependences.get(i);
	    			
	    			if (dependence.getProcHijo().equals(task.getProcID())) {
	    				if (dependence.getCritical()==0) {
	    					isFree = isFree&&true;
	    				} else {
	    					if (dependence.getProcPadre().equals("00000000")) {
	    						isFree = isFree&&true;
	    					} else {
		    					String keyMap = dependence.getProcPadre()+":"+task.getNumSecExec();
		    					switch (gDatos.getMapTask().get(keyMap).getStatus()) {
		    						case "Finished":
		    							isFree = isFree&&true;
		    							break;
	    							default:
	    								isFree = isFree&&false;
		    					}
	    					}
	    				}
	    			}
	    		}
	    		return isFree;
	    	} catch (Exception e) {
	    		logger.error("Error en isFinishedDependences...: "+e.getMessage());
	    		return false;
	    	}
	    }
        
        private void updateStatusReadyGrupoExec(TaskProcess taskProcess) {
        	try {
	        	String vSQL;
	        	switch (gDatos.getServerInfo().getDbType()) {
	    			case "ORA":
	    				break;
	    			case "SQL":
	    				break;
	    			case "mySQL":
	    				/*
	    				 * Solo pone el grupoExec en Ready si estaba en Pending
	    				 * 
	    				 */
	    				vSQL = "select status from tb_grpExec where grpID='"+taskProcess.getGrpID()+"' and numSecExec='"+taskProcess.getNumSecExec()+"'";
	    				ResultSet rs = (ResultSet) md.getQuery(vSQL);
	    				if (rs!=null) {
	    					if (rs.next()) {
	    						if (rs.getString("status").equals("Pending")) {
	    							vSQL = "update tb_grpExec set " +
											" status='Ready' " +
	    									" where " +
											" grpID='"+taskProcess.getGrpID()+"' " +
	    									" and numSecExec='"+taskProcess.getNumSecExec()+"'";
	    							int result = md.executeQuery(vSQL);
	    							if (result==1) {
	    								logger.info("El grupoExec "+taskProcess.getGrpID()+":"+taskProcess.getNumSecExec()+" paso a estado Ready");
	    							} else {
	    								logger.error("No pudo actualizar a Ready el grupoExec: "+taskProcess.getGrpID()+":"+taskProcess.getNumSecExec());
	    							}
	    						}
	    					}
	    				}
	    				break;
	    			default:
	    				break;
	        	}
        	} catch (Exception e) {
        		logger.error("Error en updateStatusReadyGrupoExec...:"+e.getMessage());
        	}
        }
    } //fin mainTask() class 
}
