/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;
//import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import utilities.globalAreaData;
import java.net.* ;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import dataClass.ServiceStatus;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thKeepAliveServices extends Thread {
    static srvRutinas gSub;
    static globalAreaData gDatos;
    static Logger logger = Logger.getLogger("thKeepAliveServices");
    
    //Carga constructor para inicializar los datos
    public thKeepAliveServices(globalAreaData m) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
    
        
    @Override
    public void run() {
        Timer timerMain = new Timer("thSubKeep");
        timerMain.schedule(new mainKeepTask(), 10000, gDatos.getServerInfo().getTxpKeep());
        logger.info("Se ha agendado thKeepAlive cada 10 segundos");
    }
    
    
    static class mainKeepTask extends TimerTask {
    
        public mainKeepTask() {
        }

        @Override
        public void run() {

        try {
        	logger.info("Inicia Thread thKeepAlive...");
            /*
                Recupera los servicios registrados en lista "serviceStatus"
            */
            
            int numServices = gDatos.getMapServiceStatus().size();
            
            if (numServices>0) {
                /*
                    Para cada servicio registrado se procedera a monitoreo via socket
                    ejecutando el getStatus
                */
                int numServiceOnline = 0;
                int numServiceOffline = 0;
                
                Map<String, ServiceStatus> vMapServiceStatus = new TreeMap<>(gDatos.getMapServiceStatus());
                for (Map.Entry<String, ServiceStatus> entry : vMapServiceStatus.entrySet()) {
                    String srvHost = entry.getValue().getSrvHost();
                    int srvPort = entry.getValue().getSrvPort();
                    
                    Socket skCliente;
                    try {
                        skCliente = new Socket(srvHost, srvPort);
                
                        OutputStream aux = skCliente.getOutputStream(); 
                        DataOutputStream flujo= new DataOutputStream( aux );
                        ObjectOutputStream objOutput = new ObjectOutputStream(aux);
                        
                        String dataSend = gSub.sendPing();

                        logger.info("Generando (tx-ping) hacia Server : "+entry.getValue().getSrvID()+" Server: "+srvHost+" Port:"+srvPort);

                        objOutput.writeObject(dataSend);
                        //flujo.writeUTF( dataSend ); 

                        InputStream inpStr = skCliente.getInputStream();
                        //DataInputStream dataInput = new DataInputStream(inpStr);
                        ObjectInputStream objInput = new ObjectInputStream(inpStr);
                        
                        //String response = dataInput.readUTF();
                        String response = (String) objInput.readObject();

                        logger.info("Recibiendo (rx-ping) desde Server: "+entry.getValue().getSrvID()+" resp: "+response);
                        JSONObject jHeader = new JSONObject(response);
                        
                        if (jHeader.getString("result").equals("OK")) {
                            gDatos.getServerStatus().setIsSocketServerActive(true);
                            numServiceOnline++;
                        } else {
                            if (jHeader.getString("result").equals("error")) {
                                gDatos.getServerStatus().setIsSocketServerActive(false);
                                numServiceOffline++;
                            }
                        }
                        
                        logger.info("Servicios Monitoreados srvOnline: "+numServiceOnline);
                        logger.info("Servicios Monitoreados srvOffline: "+numServiceOffline);
                        
                        flujo.close();
                        aux.close();
                        objInput.close();
                        objOutput.close();
                        skCliente.close();
                        
                    } catch (IOException e) {
                        numServiceOffline++;
                        gDatos.getServerStatus().setIsSocketServerActive(false);
                        logger.error("Error conectando a socket servicio cliente...: "+entry.getValue().getSrvID()+" err: "+ e.getMessage());
                    };
                }
            } else {
                logger.warn("No hay servicios registrados para monitorear...");
            }
            
            logger.info("Termino Thread thKeepAlive...");
        } catch (Exception e) {
    		logger.error("Error desconocido en Thread thKeepAlive: "+e.getMessage());
        	}
        }
    }
}
