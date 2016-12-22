/*
 * To change this license header, choose License Headers in Project Properties.    
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;
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
public class thMonitorSocket extends Thread {
    static srvRutinas gSub;
    static globalAreaData gDatos;
    static boolean isSocketActive;
    Logger logger = Logger.getLogger("thServerSocket");
    
    //Carga constructor para inicializar los datos
    public thMonitorSocket(globalAreaData m) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
        isSocketActive  = true;
    }
    
    @Override
    public void run() {
        try {
            logger.info("Iniciando Listener Thread Monitor Server port: " + gDatos.getServerInfo().getSrvPort());
            ServerSocket skServidor = new ServerSocket(gDatos.getServerInfo().getSrvPort());
            String inputData;
            String outputData = null;
            String dRequest;
            String dAuth;
            JSONObject jHeader;
            JSONObject jData;
            //int result;
            
            while (isSocketActive) {
                Socket skCliente = skServidor.accept();
                InputStream inpStr = skCliente.getInputStream();
                DataInputStream dataInput = new DataInputStream( inpStr );
                ObjectInputStream objInput = new ObjectInputStream(inpStr);
                
                //Espera Entrada
                //
                try {
                    //inputData  = dataInput.readUTF();
                	inputData  = (String) objInput.readObject();
                    
                    jHeader = new JSONObject(inputData);
                    jData = jHeader.getJSONObject("data");
                    
                    dAuth = jHeader.getString("auth");
                    dRequest = jHeader.getString("request");

                    if (dAuth.equals(gDatos.getServerInfo().getAuthKey())) {

                        switch (dRequest) {
                            case "keepAlive":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                if (gSub.updateStatusService(jData.getJSONObject("serviceStatus"))) {
                                	outputData = gSub.sendServiceInfo(jData.getJSONObject("serviceStatus").getString("srvID"));
                                } else {
                                	outputData = gSub.sendError(10);
                                }
                                break;
                            case "getDate":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = gSub.sendDate();
                                break;
                            case "getGroups":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = gSub.sendGroups();
                                break;
                            case "getTask":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = gSub.sendTask();
                                break;
                            case "getGroup":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = gSub.sendGroup();
                                break;
                            case "getStatus":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = gSub.sendStatusServices();
                                break;
                            case "getGroupsActive":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = gSub.sendGroupActives();
                                break;
                            case "getAgeShow":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = gSub.sendAgeShow();
                                break;
                            case "putExecOSP":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = gSub.sendOkTX();
                                break;
                            case "sendPing":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = "OK";
                                break;
                            case "getFTPServices":
                            	logger.info("Recibiendo TX("+dRequest+"): "+jData.toString());
                                outputData = gSub.sendFTPservices("*");
                                break;
                            default:
                                outputData = gSub.sendError(99, "Error Desconocido...");
                        }
                    } else {
                        outputData = gSub.sendError(60);
                    }
                } catch (IOException | JSONException e) {
                    outputData = gSub.sendError(90);
                } catch (Exception ex) {
                    outputData = gSub.sendError(90);
                }
                     
                //Envia Respuesta
                //
                OutputStream outStr = skCliente.getOutputStream();
                //DataOutputStream dataOutput = new DataOutputStream(outStr);
                ObjectOutputStream ObjOutput = new ObjectOutputStream(outStr); 
                
                //logger.info("Enviando RX: "+outputData);
                
                if (outputData==null) {
                    //dataOutput.writeUTF("{}");
                    ObjOutput.writeObject("{}");
                } else {
                    //dataOutput.writeUTF(outputData);
                    ObjOutput.writeObject(outputData);
                }
                
                //Cierra Todas las conexiones
                //
                inpStr.close();
                dataInput.close();
                ObjOutput.close();
                objInput.close();
                skCliente.close();
            }
            skServidor.close();
        
        } catch (NumberFormatException | IOException e) {
            logger.error("Error general en MonitoSocket: "+e.getMessage());
        }
    }
}
