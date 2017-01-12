/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;

import utilities.globalAreaData;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import utilities.srvRutinas;
import org.apache.log4j.Logger;

/**
 *
 * @author andresbenitez
 */
public class srvMonitor {
    static globalAreaData gDatos;
    static srvRutinas gSub;
    
    //Carga Clase log4
    static Logger logger = Logger.getLogger("srv.monitor");
    
    public srvMonitor() {
        /*
            El constructor solo se ejecuta cuando la clase es instanciada desde otra.
            Cuando la clase posee un main() principal de ejecución, el constructor  no
            es considerado.
        */        
    }   

    public static void main(String[] args) throws IOException {
        try {
        	//Logger.getRootLogger().setLevel(Level.OFF); 
        	
            //Instancia las Clases
            logger.info("Iniciando srvMonitor...");
            gDatos  = new globalAreaData();
            gSub = new srvRutinas(gDatos);

            if (gDatos.getServerStatus().isIsLoadParam()) {
                if (gDatos.getServerStatus().isIsLoadRutinas()) {
                    Timer mainTimer = new Timer("thMain");
                    mainTimer.schedule(new mainTimerTask(), 2000, gDatos.getServerInfo().getTxpMain());
                    logger.info("Scheduling MainTask cada: "+ gDatos.getServerInfo().getTxpMain()/1000 + " segundos");
                    logger.info("Server: "+ gDatos.getServerInfo().getSrvID());
                    logger.info("Listener Port: " + gDatos.getServerInfo().getSrvPort());
                    logger.info("Metadata Type: " + gDatos.getServerInfo().getDbType());
                } else {
                    logger.error("Error cargando Rutinas...abortando modulo.");
                }
            } else { 
                logger.error("Error cargando globalAreaData...abortando modulo.");
            }
        } catch (Exception e) {
            logger.error("Error en modulo principal: "+e.getMessage()+ " ");
        }
    }
    
    static class mainTimerTask extends TimerTask {
        //Declare los Thread de cada proceso
        //
    	Thread thAsignaTask 	= new thAsignaTask(gDatos); //TimerTask
        Thread thKeep 			= new thKeepAliveServices(gDatos); //TimerTask
        Thread thInscribeTask 	= new thInscribeTask(gDatos); //TimerTask
        Thread thUpdateStatusDB = new thUpdateStatusDB(gDatos); //TimerTask
        Thread thSocket;
        
        Map<String , Boolean> mapThread = new TreeMap<>();
        
        //Constructor de la clase
        public mainTimerTask() {
        }

        @SuppressWarnings("deprecation")
		@Override
        public void run() { 
    	try {
            logger.info("Iniciando MainTimerTask de srvMonitor...");
            
            /**
             * Valida Conexion a MetaData
             */
            validaMetadataConnect();

            
            /**
             * Revisando Thread de Modulos Activos
             */
            
            validaThreadsModulosActivos();
            
            /*
            Se aplicara validacion modular de procesos, ya que se encuentran en un bucle infinito.
            */
            
            //Levanta Thread Socket Server
            //
            try {
                if (!mapThread.get("thMonitorSocket")) {
                	logger.info("Iniciando Thread thMonitorSocket....normal...");
                    thSocket = new thMonitorSocket(gDatos);
                    thSocket.setName("thMonitorSocket");
                    thSocket.start();
                } 
            } catch (Exception e) {
                mapThread.replace("thMonitorSocket", false);
                logger.error("Error al Iniciar socket monitor server "+ thSocket.getName() + " : "+e.getMessage());
                if (thSocket.isAlive()) {
                	thSocket.destroy();
                }
            }
            
            //Levanta TimerTask KeepAlive
            //
            try {
                if (!mapThread.get("thSubKeep")) {
                	logger.info(" Iniciando Thread thSubKeep....");
                    thKeep = new thKeepAliveServices(gDatos);
                    thKeep.setName("thSubKeep");
                    thKeep.start();
                } 
            } catch (Exception e) {
            	mapThread.replace("thSubKeep", false);
                logger.error("Error al Iniciar thread: "+ thKeep.getName());
                if (thKeep.isAlive()) {
                	thKeep.destroy();
                }
            }
            
            //Levanta TimerTask AsignaTask
            //
            try {
                if (!mapThread.get("thSubAsignaTask")) {
                	logger.info(" Iniciando Thread thSubAsignaTask....");
                	thAsignaTask.setName("thSubAsignaTask");
                    thAsignaTask.start();
                } 
            } catch (Exception e) {
            	mapThread.replace("thSubAsignaTask", false);
                logger.error("Error al Iniciar thread: "+ thAsignaTask.getName());
                if (thAsignaTask.isAlive()) {
                	thAsignaTask.destroy();
                }
            }            
            
            
            //Levanta TimerTask para revision de Agendas y Procesos que deberan ejecutarse
            //
            try {
                if (!mapThread.get("thSubInscribeTask")) {
                	logger.info(" Iniciando Thread thSubInscribeTask....normal...");
                    thInscribeTask = new thInscribeTask(gDatos);  
                    thInscribeTask.setName("thSubInscribeTask");
                    thInscribeTask.start();
                } 
            } catch (Exception e) {
                mapThread.replace("thSubInscribeTask", false);
                logger.error("Error al Iniciar Thread thSubInscribeTask "+ thInscribeTask.getName());
                if (thInscribeTask.isAlive()) {
                	thInscribeTask.destroy();
                }
            }

            //Levanta TimerTask Update DB
            //
            try {
                if (!mapThread.get("thSubUpdateStatus")) {
                	logger.info(" Iniciando Thread thSubUpdateStatus....normal...");
                	thUpdateStatusDB = new thUpdateStatusDB(gDatos);  
                	thUpdateStatusDB.setName("thSubUpdateStatus");
                	thUpdateStatusDB.start();
                } 
            } catch (Exception e) {
                mapThread.replace("thUpdateStatusDB", false);
                logger.error("Error al Iniciar Thread thUpdateStatusDB "+ thUpdateStatusDB.getName());
                if (thUpdateStatusDB.isAlive()) {
                	thUpdateStatusDB.destroy();
                }
            }
                
            logger.info("Terminando MainTimerTask de srvMonitor...");
        } catch (Exception e) {
        	logger.error("Error inesperado en modulo principal srvMonitor: "+e.getMessage());
        	}
        }
        
        private void validaThreadsModulosActivos() {
            logger.info("Revisando Threads de Modulos Activos...");
            
        	mapThread.put("thMonitorSocket", false);
        	mapThread.put("thSubKeep", false);
        	mapThread.put("thSubInscribeTask", false);
        	mapThread.put("thSubAsignaTask", false);
        	mapThread.put("thSubUpdateStatus", false);
            
            //Thread tr = Thread.currentThread();
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            //System.out.println("Current Thread: "+tr.getName()+" ID: "+tr.getId());
            for ( Thread t : threadSet){
                //System.out.println("Thread :"+t+":"+"state:"+t.getState()+" ID: "+t.getId());
                if (t.getName().equals("thMonitorSocket")) {
                	mapThread.replace("thMonitorSocket", true);
                }
                if (t.getName().equals("thSubKeep")) {
                	mapThread.replace("thSubKeep", true);
                }
                if (t.getName().equals("thSubInscribeTask")) {
                	mapThread.replace("thSubInscribeTask", true);
                }
                if (t.getName().equals("thSubAsignaTask")) {
                	mapThread.put("thSubAsignaTask", true);
                }
                if (t.getName().equals("thSubUpdateStatus")) {
                	mapThread.put("thSubUpdateStatus", true);
                }
            }
            
            /**
             * Informa Threads encontrados
             */
            for (Map.Entry<String, Boolean> entry : mapThread.entrySet()) {
                logger.info("Thread: " + entry.getKey() + ", valor=" + entry.getValue());
            }
        }
        
        private void validaMetadataConnect() {
            try {
                metaData mdConn = new metaData(gDatos);
                mdConn.openConnection();
                if (mdConn.isConnected()) {
                	gDatos.getServerStatus().setIsValMetadataConnect(true);
                	mdConn.closeConnection();
                    logger.info("Se ha validado conexion a MetaData");
                } else {
                	gDatos.getServerStatus().setIsValMetadataConnect(false);
                	logger.error("No se ha podido validar conexion a MetaData");
                }
            } catch (Exception e) {
            	gDatos.getServerStatus().setIsValMetadataConnect(false);
                logger.error("No se ha podido validar conexion a MetaData.."+e.getMessage());
            }
        }
    }
}
