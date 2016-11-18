/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;

import utilities.globalAreaData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class SrvServer {
    static globalAreaData gDatos;
    static srvRutinas gSub ;
    
    //Carga Clase log4
    static Logger logger = Logger.getLogger("srvServer");
    
    public SrvServer() {
        /*
            El constructor solo se ejecuta cuando la clase es instanciada desde otra.
            Cuando la clase posee un main() principal de ejecución, el constructor  no
            es considerado.
        */
    }   

    public static void main(String[] args) throws IOException {
        try {
        	
        	
            //Instancia las Clases
            logger.info("Iniciando srvServer...");
            gDatos = new globalAreaData();
            gSub = new srvRutinas(gDatos);

            if (gDatos.getServiceStatus().isIsLoadParam()) {
                if (gDatos.getServiceStatus().isIsLoadRutinas()) {
                    Timer mainTimer = new Timer("thMain");
                    mainTimer.schedule(new mainTimerTask(), 2000, gDatos.getServiceInfo().getTxpMain());
                    logger.info("Agendando mainTimerTask cada "+gDatos.getServiceInfo().getTxpMain()+ " segundos...");
                    logger.info("Server: "+ gDatos.getServiceInfo().getSrvID());
                    logger.info("Listener Port: " + gDatos.getServiceInfo().getSrvPort());
                    logger.info("Maximo Procesos: " +  gDatos.getServiceStatus().getNumProcMax());
                } else {
                    logger.error("Error cargando Rutinas...abortando modulo.");
                }
            } else {
                logger.error("Error cargando globalAreaData...abortando modulo.");
            }
        } catch (Exception e) {
            logger.error("Error en modulo principal: "+e.getMessage());
        }
    }
    
    static class mainTimerTask extends TimerTask {
        
        //Declare los Thread de cada proceso
        //
        Thread thRunProc = new thRunProcess(gDatos);
        Thread thSocket = new thServerSocket(gDatos);
        Thread thKeep; // = new thKeepAlive(gDatos);
        
        //Constructor de la clase
        public mainTimerTask() {
        }
        
        @Override
        public void run() {
            logger.info("Iniciando Revision Modulo Principal: "+gSub.getDateNow("yyyy-MM-dd HH:mm:ss"));
            
            /**
             * Valida la ejecución de los 3 Thread principales
             * thread: thServerSocket - Levanta Socket para recibir comandos desde srvMonitor
             * thread: thKeepAlive - Se conecta a srvMonitor primario o backup para reportar status del servicio y
             * 			procesos en ejecucion.
             * thread: thSubRunProcess: valida la cola de procesos e ejecuta los thread correspondientes
             */
            
        	Map<String , Boolean> mapThread = new HashMap<>();
        	mapThread.put("thServerSocket", false);
        	mapThread.put("thKeepAlive", false);
        	mapThread.put("thSubRunProcess", false);
        	
            logger.info("Revisando Threads Principales del Servicio...");
            
            //Thread tr = Thread.currentThread();
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            //System.out.println("TOTAL de Threads del Servicio: "+threadSet.size());
            for ( Thread t : threadSet){
                if (t.getName().equals("thServerSocket")) {
                    mapThread.replace("thServerSocket", true);
                }
                if (t.getName().equals("thKeepAlive")) {
                    mapThread.replace("thKeepAlive", true);
                }
                if (t.getName().equals("thSubRunProcess")) {
                    mapThread.replace("thSubRunProcess", true);
                }
            }
                
            /**
             * Informa Threads encontrados
             */
            for (Map.Entry<String, Boolean> entry : mapThread.entrySet()) {
                logger.info("Thread: " + entry.getKey() + ", valor=" + entry.getValue());
            }
                        
            //Levanta Socket Server
            //
            try {
                if (!mapThread.get("thServerSocket")) {
                    thSocket.setName("thServerSocket");
                    thSocket.start();
                    logger.info("Levantando thSocket Server");
                } 
            } catch (Exception e) {
            	mapThread.replace("thServerSocket", false);
                logger.error("No se ha podido Levantar socket server "+ thSocket.getName()+" "+e.getMessage());
            }
            
            //Levanta KeepAlive
            //
            try {
                if (!mapThread.get("thKeepAlive")) {
                    thKeep = new thKeepAlive(gDatos);
                    thKeep.setName("thKeepAlive");
                    logger.info("Levantando thread KeepAlive");
                    thKeep.start();
                } 
            } catch (Exception e) {
            	mapThread.replace("thKeepAlive", false);
                logger.error("No se ha podido Levantar thread: "+ thKeep.getName()+ " "+e.getMessage());
            }

            /**
             * Solo ejecuta Thread de runProcess si hay asignados procesos al servicio
             */
            if (gDatos.getServiceStatus().isIsAssignedTypeProc()) {
                //Levanta thRunProcess Monitorearo por los Subprocesos del TimerTask
                //TimerTask: thSubRunProcess
                //Al Agendar el Thread principal Muere y queda solo el Hijo.
                try {
                    if (!mapThread.get("thRunProcess")) {
                        thRunProc.setName("thRunProcess");
                        logger.info("Agendando thread RunProcess");
                        thRunProc.start();
                    } 
                } catch (Exception e) {
                    logger.error("No se ha podido Agendar thread: "+ thRunProc.getName()+ " "+e.getMessage());
                }
            } else {
                logger.warn("Aun no hay tipos de procesos asignados al servicio.");
            }
            
            logger.info("Finalizando Revision Modulo Principal: "+gSub.getDateNow("yyyy-MM-dd HH:mm:ss"));
        }
    }
}