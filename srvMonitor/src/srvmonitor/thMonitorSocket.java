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
            
            while (isSocketActive) {
                Socket skCliente = skServidor.accept();
                InputStream inpStr = skCliente.getInputStream();
                //DataInputStream dataInput = new DataInputStream( inpStr );
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
                    	logger.info("Recibiendo TX("+ dRequest +"): "+ jData.toString());
                        switch (dRequest) {
                            case "keepAlive":
                                if (gSub.updateStatusService(jData.getJSONObject("serviceStatus"))) {
                                	outputData = gSub.sendServiceInfo(jData.getJSONObject("serviceStatus").getString("srvID"));
                                } else {
                                	outputData = gSub.sendError(10);
                                }
                                break;
                            case "getDate":
                                outputData = gSub.sendDate();
                                break;
                            case "getGroups":
                                outputData = gSub.sendGroups();
                                break;
                            case "getTask":
                                outputData = gSub.sendTask();
                                break;
                            case "getGroup":
                                outputData = gSub.sendGroup();
                                break;
                            case "getStatus":
                                outputData = gSub.sendStatusServices();
                                break;
                            case "getGroupsActive":
                                outputData = gSub.sendGroupActives();
                                break;
                            case "getAgeShow":
                                outputData = gSub.sendAgeShow();
                                break;
                            case "putExecOSP":
                                outputData = gSub.sendOkTX();
                                break;
                            case "sendPing":
                                outputData = "OK";
                                break;
                            case "getFTPServices":
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
