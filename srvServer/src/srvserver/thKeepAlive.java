/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;
import utilities.globalAreaData;
import java.io.* ; 
import java.net.* ;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thKeepAlive extends Thread {
    static srvRutinas gSub;
    static globalAreaData gDatos;
    Logger logger = Logger.getLogger("thKeepAlive");
    
    /**
     * Variables Locales
     */
    String 	srvHost;
    int 	srvPort;
    String  srvName;
    boolean isPrimary;
    
    //Carga constructor para inicializar los datos
    public thKeepAlive(globalAreaData m) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
    
    @Override
    public void run() {
    	logger.info("Iniciando thKeepAlive");
    	logger.info("Status Active Monitor Host: "+gDatos.getServiceStatus().isActivePrimaryMonHost());
    	
    	isPrimary = gDatos.getServiceStatus().isActivePrimaryMonHost();
    	if (isPrimary) {
    		srvHost = gDatos.getServiceInfo().getSrvMonHost();
    		srvPort = gDatos.getServiceInfo().getMonPort();
    		srvName = "Primario";
    	} else {
    		srvHost = gDatos.getServiceInfo().getSrvMonHostBack();
    		srvPort = gDatos.getServiceInfo().getMonPortBack();
    		srvName = "Backup";
    	}
    	
        try {
            Socket skCliente = new Socket(srvHost, srvPort);
            
            OutputStream aux = skCliente.getOutputStream(); 
            DataOutputStream flujo= new DataOutputStream( aux );
            ObjectOutputStream objOutput = new ObjectOutputStream(aux);
            String dataSend = gSub.sendStatusService();
            
            logger.info("Generando (tx) hacia Server Monitor "+ srvName + ": "+dataSend);
            
            objOutput.writeObject(dataSend);
            //flujo.writeUTF( dataSend ); 
            
            InputStream inpStr = skCliente.getInputStream();
            DataInputStream dataInput = new DataInputStream(inpStr);
            ObjectInputStream objInput = new ObjectInputStream(inpStr);
            
            String response = (String) objInput.readObject();
            //String response = dataInput.readUTF();
            
            logger.info("Recibiendo (rx)...: "+response);
            JSONObject jHeader = new JSONObject(response);

            try {
                if (jHeader.getString("result").equals("OK")) {
                    JSONObject jData = jHeader.getJSONObject("data");
                    logger.info("Actualiza statusService...");
                    gSub.updateStatusService(jData);
                } else {
                    if (jHeader.getString("result").equals("error")) {
                        JSONObject jData = jHeader.getJSONObject("data");
                        logger.error("Error result: "+jData.getInt("errCode")+ " " +jData.getString("errMesg"));
                    }
                }
                gDatos.getServiceStatus().setConnectMonHost(true);
            } catch (JSONException e) {
                logger.error("Error en formato de respuesta");
            }
            aux.close();
            flujo.close();
            inpStr.close();
            dataInput.close();
            skCliente.close();
            logger.info("Finalizando thKeepAlive");
        } catch (NumberFormatException | IOException | ClassNotFoundException e) {
            gDatos.getServiceStatus().setActivePrimaryMonHost(!isPrimary);
            gDatos.getServiceStatus().setConnectMonHost(false);
            logger.error(" Error conexion a server de monitoreo "+ srvName + ": "+ e.getMessage());
        }
    }
}
