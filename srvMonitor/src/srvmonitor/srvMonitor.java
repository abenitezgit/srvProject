/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;

import utilities.globalAreaData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
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
    static Logger logger = Logger.getLogger("srvMonitor");
    
    public srvMonitor() {
        /*
            El constructor solo se ejecuta cuando la clase es instanciada desde otra.
            Cuando la clase posee un main() principal de ejecución, el constructor  no
            es considerado.
        */        
    }   

    public static void main(String[] args) throws IOException {
        try {
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
    	Thread thInscribe = new thInscribeGroup(gDatos); //TimerTask
        Thread thKeep = new thKeepAliveServices(gDatos); //TimerTask
        Thread thAgendas;
        Thread thSocket;
        
        //Constructor de la clase
        public mainTimerTask() {
        }

        @Override
        public void run() { 
            logger.info("Ejecutando MainTimerTask...");

            Map<String , Boolean> mapThread = new HashMap<>();
        	mapThread.put("thMonitorSocket", false);
        	mapThread.put("thSubKeep", false);
        	mapThread.put("thActiveGroups", false);
        	mapThread.put("thSubInscribeGroup", false);
            
            logger.info("Revisando Threads de Modulos Activos...");
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
                if (t.getName().equals("thActiveGroups")) {
                	mapThread.replace("thActiveGroups", true);
                }
                if (t.getName().equals("thSubInscribeGroup")) {
                	mapThread.put("thSubInscribeGroup", true);
                }
            }
            
            /**
             * Informa Threads encontrados
             */
            for (Map.Entry<String, Boolean> entry : mapThread.entrySet()) {
                logger.info("Thread: " + entry.getKey() + ", valor=" + entry.getValue());
            }
            /*
            Se aplicara validacion modular de procesos, ya que se encuentran en un bucle infinito.
            */
            
            //Levanta Socket Server
            //
            try {
                if (!mapThread.get("thMonitorSocket")) {
                    thSocket = new thMonitorSocket(gDatos);
                    thSocket.setName("thMonitorSocket");
                    thSocket.start();
                    logger.info("Iniciando thMonitor Server....normal...");
                } 
            } catch (Exception e) {
                mapThread.replace("thMonitorSocket", false);
                logger.error("Error al Iniciar socket monitor server "+ thSocket.getName() + " : "+e.getMessage());
            }
            
            //Levanta KeepAlive
            //
            try {
                if (!mapThread.get("thSubKeep")) {
                    thKeep = new thKeepAliveServices(gDatos);
                    thKeep.setName("thSubKeep");
                    thKeep.start();
                    logger.info(" Iniciando thread KeepAlive....");
                } 
            } catch (Exception e) {
            	mapThread.replace("thSubKeep", false);
                logger.error("Error al Iniciar thread: "+ thKeep.getName());
            }
            
            //Levanta IncribeGroups
            //
            try {
                if (!mapThread.get("thSubInscribeGroup")) {
                    thInscribe.setName("thSubInscribeGroup");
                    thInscribe.start();
                    logger.info(" Iniciando thread thInscribe....");
                } 
            } catch (Exception e) {
            	mapThread.replace("thSubInscribeGroup", false);
                logger.error("Error al Iniciar thread: "+ thInscribe.getName());
            }            
            
            
            //Levanta Thread para revision de Agendas y Procesos que deberan ejecutarse
            //
            
            if (gDatos.getServerStatus().isIsValMetadataConnect()) {
                //Levanta Thread Busca Agendas Activas
                //
                try {
                    if (!mapThread.get("thActiveGroups")) {
                        thAgendas = new thActiveGroups(gDatos);  
                        thAgendas.setName("thActiveGroups");
                        thAgendas.start();
                        logger.info(" Iniciando thActiveGroups....normal...");
                    } 
                } catch (Exception e) {
                    mapThread.replace("thActiveGroups", false);
                    logger.error("Error al Iniciar Thread Agendas "+ thAgendas.getName());
                }
            } else {
                logger.warn("No es posble conectarse a MetaData...");
            }
        }
    }
}
