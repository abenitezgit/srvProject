/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import utilities.globalAreaData;
import java.net.* ;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.json.JSONObject;
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
        timerMain.schedule(new mainKeepTask(), 1000, 10000);
        logger.info("Se ha agendado thKeepAlive cada 10 segundos");
    }
    
    
    static class mainKeepTask extends TimerTask {
    
        public mainKeepTask() {
        }

        @Override
        public void run() {
            /**
             * Valida Conexion a MetaData
             */
            try {
                MetaData metadata = new MetaData(gDatos);
                if (gDatos.getServerStatus().isIsValMetadataConnect()) {
                    metadata.closeConnection();
                }
            } catch (Exception e) {
                logger.error("No se ha podido validar conexion a MetaData.."+e.getMessage());
            }
            
            
            /*
                Recupera los servicios registrados en lista "serviceStatus"
            */
            
            JSONObject jData;
            int numServices = gDatos.getLstServiceStatus().size();
            logger.info("Inicia thKeepAlive...");
            
            if (numServices>0) {
                /*
                    Para cada servicio registrado se procedera a monitoreo via socket
                    ejecutando el getStatus
                */
                int numServiceOnline = 0;
                int numServiceOffline = 0;
                for (int i=0; i<numServices; i++) {
                    jData = new JSONObject(gDatos.getLstServiceStatus().get(i));
                    String srvHost = jData.getString("srvHost");
                    int srvPort = jData.getInt("srvPort");
                    
                    Socket skCliente;
                    try {
                        skCliente = new Socket(srvHost, srvPort);
                
                        OutputStream aux = skCliente.getOutputStream(); 
                        DataOutputStream flujo= new DataOutputStream( aux ); 
                        String dataSend = gSub.sendPing();

                        logger.info("Generando (tx-ping) hacia Server Monitoreado: "+jData.getString("srvID"));

                        flujo.writeUTF( dataSend ); 

                        InputStream inpStr = skCliente.getInputStream();
                        DataInputStream dataInput = new DataInputStream(inpStr);
                        String response = dataInput.readUTF();

                        logger.info("Recibiendo (rx)...: "+response);
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
                        
                        logger.debug("srvOnline: "+numServiceOnline);
                        logger.debug("srvOffline: "+numServiceOffline);
                        
                        flujo.close();
                        aux.close();
                        skCliente.close();
                        
                    } catch (IOException e) {
                        numServiceOffline++;
                        gDatos.getServerStatus().setIsSocketServerActive(false);
                        logger.error("Error conectando a socket servicio cliente..."+ e.getMessage());
                    };
                    
                    logger.debug("Servicio: "+jData.getString("srvID")+ " "+srvHost+" "+srvPort);
                
                }
            } else {
                logger.warn("Aun no hay servicios registrados para monitorear...");
            }
            
            logger.info("Finaliza thKeepAlive...");
        }
    }
}
