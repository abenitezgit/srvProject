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
public class thServerSocket extends Thread {
    static srvRutinas gSub;
    static globalAreaData gDatos;
    static boolean isSocketActive = true;
    Logger logger = Logger.getLogger("thServerSocket");
   
    
    //Carga constructor para inicializar los datos
    public thServerSocket(globalAreaData m) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
            
    @Override
    public void run() {
        try {
            logger.info("Starting Listener Thread Service Server port: " + gDatos.getServiceInfo().getSrvPort());
            String inputData;
            String outputData;
            String dRequest;
            String dAuth;
            JSONObject jHeader;
            JSONObject jData;
            
            ServerSocket skServidor = new ServerSocket(gDatos.getServiceInfo().getSrvPort());
            
            while (isSocketActive) {
                Socket skCliente = skServidor.accept();
                InputStream inpStr = skCliente.getInputStream();
                DataInputStream dataInput = new DataInputStream( inpStr );
                ObjectInputStream objInput = new ObjectInputStream(inpStr);
                
                //Recibe Data Input (request)
                //
                //Espera Entrada
                //
                try {
                    //inputData  = dataInput.readUTF();
                	inputData = (String) objInput.readObject();
                    //gSub.sysOutln(inputData);
                    
                    logger.info("Recibiendo TX: "+ inputData);
                    jHeader = new JSONObject(inputData);
                    jData = jHeader.getJSONObject("data");
                    
                    dAuth = jHeader.getString("auth");
                    dRequest = jHeader.getString("request");
                    
                    if (dAuth.equals(gDatos.getServiceInfo().getAuthKey())) {

                        switch (dRequest) {
                            case "getStatus":
                                outputData = gSub.sendStatusService();
                                break;
                            case "getDate":
                                outputData = gSub.sendDate();
                                break;
                            case "getList":
                                outputData = ""; //gSub.sendList(jData);
                                break;
                            case "updateVar":
                                outputData = ""; //gSub.updateVar(jData);
                                break;
                            case "ping":
                                outputData = gSub.sendOkTX();
                                break;
                            default:
                                outputData = gSub.sendError(99,"Error desconocido..");
                        }
                    } else {
                        outputData = gSub.sendError(60);
                    }
                } catch (IOException | JSONException | ClassNotFoundException e) {
                    outputData = gSub.sendError(90);
                }

                
                //Envia Respuesta
                //
                OutputStream outStr = skCliente.getOutputStream();
                //DataOutputStream dataOutput = new DataOutputStream(outStr);
                ObjectOutputStream objOutput = new ObjectOutputStream(outStr);
                
                //logger.info("Enviando respuesta: "+ outputData);
                logger.info("Enviando respuesta: "+ objOutput);
                
                if (outputData==null) {
                    //dataOutput.writeUTF("{}");
                    objOutput.writeObject("{}");
                } else {
                    //dataOutput.writeUTF(outputData);
                    objOutput.writeObject(outputData);
                }
                
                //Cierra Todas las conexiones
                //
                inpStr.close();
                dataInput.close();
                objOutput.close();
                objInput.close();
                skCliente.close();
            }
            skServidor.close();
        
        } catch (NumberFormatException | IOException e) {
            logger.error(e.getMessage());
        }
    }
}
