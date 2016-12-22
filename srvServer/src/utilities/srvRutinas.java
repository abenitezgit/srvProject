/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import dataClass.AssignedTypeProc;
import dataClass.Interval;
import dataClass.ServiceStatus;
import dataClass.TaskProcess;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author andresbenitez
 */
public class srvRutinas {
    /**
     * Clase de Traspaso de Datos
     */
    static globalAreaData gDatos;
        
    //Carga Clase log4
    Logger logger = Logger.getLogger("srvRutinas");    
    
    //Constructor de la clase
    //
    public srvRutinas(globalAreaData m) {
        try {
            gDatos = m;
            gDatos.getServiceStatus().setIsLoadRutinas(true);
        } catch (Exception e) {
            gDatos.getServiceStatus().setIsLoadRutinas(false);
        }
    }
    
    
    public void sysOutln(Object obj) {
        System.out.println(obj);
    }
    
    public String getDateNow() {
        try {
            //Extrae Fecha de Hoy
            //
            Date today;
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(formatter.getTimeZone());
            today = new Date();
            return formatter.format(today);  
        } catch (Exception e) {
            return null;
        }
    }

    public String getDateNow(String xformat) {
        try {
            //Extrae Fecha de Hoy
            //
            Date today;
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat(xformat);
            //System.out.println(formatter.getTimeZone());
            today = new Date();
            return formatter.format(today);  
        } catch (Exception e) {
            return null;
        }
    }
    
    public String sendError(int errCode, String errMesg) {
        JSONObject jData = new JSONObject();
        JSONObject jHeader = new JSONObject();
    
        jData.put("errMesg", errMesg);
        jData.put("errCode", errCode);
        
        jHeader.put("data",jData);
        jHeader.put("result", "error");
            
        return jHeader.toString();
    }
    
    public String sendError(int errCode) {
        String errMesg;
        
        switch (errCode) {
            case 90: 
                    errMesg = "error de entrada";
                    break;
            case 80: 
                    errMesg = "servicio offlne";
                    break;
            case 60:
                    errMesg = "TX no autorizada";
                    break;
            case 10:
                    errMesg = "procID ya se encuentra en poolProcess...";
                    break;
            default: 
                    errMesg = "error desconocido";
                    break;
        }
        
        JSONObject jData = new JSONObject();
        JSONObject jHeader = new JSONObject();
    
        jData.put("errMesg", errMesg);
        jData.put("errCode", errCode);
        
        jHeader.put("data",jData);
        jHeader.put("result", "error");
            
        return jHeader.toString();
    }
    
    public String sendOkTX() {
        
        JSONObject jData = new JSONObject();
        JSONObject jHeader = new JSONObject();
        
        jHeader.put("data", jData);
        jHeader.put("result", "OK");
            
        return jHeader.toString();
    }
    
    public static double getProcessCpuLoad() throws Exception {

        
        MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
        ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

        if (list.isEmpty())     return Double.NaN;

        Attribute att = (Attribute)list.get(0);
        Double value  = (Double)att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0)      return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        return ((int)(value * 1000) / 10.0);
    }
    
//    public List<PoolProcess> genLstProcessOfPool(String typeProc) {
//        try {
//            List<PoolProcess> lstProcess = gDatos.getLstPoolProcess().stream().filter(p -> p.getTypeProc().equals(typeProc)).collect(Collectors.toList());
//            
//            if (!lstProcess.isEmpty()) {
//            
//            }
//            return lstProcess;
//        } catch (Exception e) {
//            logger.error("Error generando lista de procesos: "+typeProc);
//            return null;
//        }
//    }

//    public synchronized String updatePoolProcess(JSONObject jData) throws IOException {
//        try {
//            
//            /**
//             * Actualiza informacion que es recibida desde srvMonitor
//             * Debe verificar nuevas asignaciones de procesos, detenciones, etc.
//             */
//            
//            logger.debug("Data recibida para updatePoolProcess: "+jData.toString());
//            
//            JSONArray jArray = jData.getJSONArray("poolProcess");
//            
//            int numItems = jArray.length();
//
//            if (numItems>0) {
// 
//                PoolProcess poolProcess = new PoolProcess();
//                
//                @SuppressWarnings("unused")
//				List<PoolProcess> lstPoolProcess = new ArrayList<>();
//
//                logger.info("Se han recibido: "+jArray.length()+" asignaciones de Ejecución de Procesos.");
//            
//                //Para cada proceso recibido
//                //
//                for (int i=0; i<jArray.length(); i++) {
//                    poolProcess = (PoolProcess) serializeJSonStringToObject(jArray.getJSONObject(i).toString(), PoolProcess.class);
//
//                    //Extrae posicion en lista actual si es que existe
//                    //
//                    int indexPool;
//                    if (poolProcess.getTypeProc().equals("ETL")) {
//                        indexPool = gDatos.getIndexOfPoolProcess(poolProcess.getProcID(), poolProcess.getIntervalID());
//                    } else {
//                        indexPool = gDatos.getIndexOfPoolProcess(poolProcess.getProcID());
//                    }
//                    
//                    /**
//                     * Valida informacion de status que posee el proceso
//                     */
//                    switch (poolProcess.getStatus()) {
//                        case "Assigned":
//                            /**
//                             * Valida Si ya se encuentra en pool.
//                             *
//                             */
//                            if (indexPool!=-1) {
//                                /**
//                                 * Generar acciones de actualizacion
//                                 * Pendientes.
//                                 * (por ahora no hay accion si ya existe)
//                                 */
//                            
//                            } else {
//                                /**
//                                 * No se encontro en pool
//                                 * Se ingresará como nuevo.
//                                 */
//                                poolProcess.setStatus("Ready");
//                                poolProcess.setUpdateTime(getDateNow());
//                                gDatos.updateLstPoolProcess(indexPool, poolProcess, false);
//                            }
//                            break;
//                        case "Stopped":
//                            /**
//                             * Valida Si ya se encuentra en pool.
//                             *
//                             */
//                            if (indexPool!=-1) {
//                                /**
//                                 * Si actual status=Ready, se marcara como Released para ser devuelta al pool de control
//                                 * del srvMOnitor.
//                                 */
//                            
//                            } else {
//                                /**
//                                 * No se encontro en pool
//                                 * No hay accion
//                                 * Hubo algun problema de perdida del registro.
//                                 */
//                            }
//                            break;
//                        
//                    } //Fin switch()
//                    
//                } //Fin for()
//                //logger.info("Total de Ejecuciones de Procesos en lista PoolProcess: " + gDatos.getServiceStatus().getLstPoolProcess().size());
//            }
//            return sendOkTX();
//        } catch (JSONException | IOException e) {
//            logger.error("Error updatePoolProcess: "+e.getMessage());                                                                                                                                                                                                                                                                                                             return sendError(10, e.getMessage());
//        }
//    
//    }
     
    public String sendStatusService()  {
        //
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            
            gDatos.getServiceStatus().setSrvUpdateTime(getDateNow());
            
            // Se genera la salida de la lista 
            JSONObject jHeader = new JSONObject();
            JSONObject jData = new JSONObject();
            
            gDatos.getServiceStatus().setMapAssignedTypeProc(gDatos.getMapAssignedTypeProc());
            gDatos.getServiceStatus().setMapTask(gDatos.getMapTask());
            gDatos.getServiceStatus().setMapInterval(gDatos.getMapInteval());
            
            JSONObject jo = new JSONObject(mapper.writeValueAsString(gDatos.getServiceStatus()));
            
            jData.put("serviceStatus", jo);
            
            jHeader.put("data", jData);            
            jHeader.put("auth", gDatos.getServiceInfo().getAuthKey());
            jHeader.put("request", "keepAlive");
            
            
            return jHeader.toString();
        } catch (JSONException | IOException e) {
            return sendError(10,e.getMessage());
        }
    }
    
    public synchronized void updateStatusService(JSONObject jData) {
    	
    	ServiceStatus serviceStatus = new ServiceStatus();
    	TaskProcess taskProcess 	= new TaskProcess();
    	Map<String, Interval> vMapInterval = new TreeMap<>();
    	
    	try {
    		logger.info("Serializando objeto serviceStatus");
			serviceStatus = (ServiceStatus) serializeJSonStringToObject(jData.getString("serviceStatus"), ServiceStatus.class);
		} catch (IOException e) {
			logger.error("Error Serializando objeto serviceStatus");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	/*
    	 * Actualiza Mapa de Intervalos
    	 * genera una copia de la lista extraida desde el objeto serviceStatus
    	 */
    	vMapInterval = new TreeMap<>(serviceStatus.getMapInterval());
    	
    	//recorre la lista extraida par actualizar la global
    	for (Map.Entry<String, Interval> entryInt : vMapInterval.entrySet()) {
    		//la key del MapInterval es procID+intervalID
    		if (gDatos.getMapInteval().containsKey(entryInt.getKey())) {
    			//Actualiza los MapInterval en ejecución siempre y cuandon vengan
    			//con status de Abort o Pause. Y solo actualiza interval que están em Ready
    			String vStatus = entryInt.getValue().getStatus();
    			if (vStatus.equals("Abort")||vStatus.equals("Pause")) {
    				if (gDatos.getMapInteval().get(entryInt.getKey()).getStatus().equals("Ready")) {
    					gDatos.getMapInteval().get(entryInt.getKey()).setStatus(vStatus);
    					gDatos.getMapInteval().get(entryInt.getKey()).setFecUpdate(getDateNow());
    				}
    			}
    		} else {
    			gDatos.getMapInteval().put(entryInt.getKey(), entryInt.getValue());
    		}
    	}
    	
    	/**
    	 * Actualiza el Map AssignedTypeProc
    	 */
    	logger.info("Actualiza Map AssignedTypeProc");
    	gDatos.setMapAssignedTypeProc(serviceStatus.getMapAssignedTypeProc());
    	
    	/**
    	 * Analizar este tipo de actualizacion por posibles perdidas de sincronizacion
    	 */
    	logger.info("Actualiza serviceStatus global");
    	gDatos.setServiceStatus(serviceStatus);
    	
    	/**
    	 * Actualiza las Task Asignadas
    	 */
    	logger.info("Actualiza MapTask");
        for (Map.Entry<String, TaskProcess> entry : serviceStatus.getMapTask().entrySet()) {
        	String key = entry.getKey();
        	taskProcess = entry.getValue();
        	
        	//Valida si la Task ya esta creada
        	if (gDatos.getMapTask().containsKey(key)) {
        		/**
        		 * Si existe la Task actualizar su status solo si fue abortada, suspendida
        		 * y si no esta en ejecución
        		 */
        		if (!gDatos.getMapTask().get(key).getStatus().equals("Running")&&!gDatos.getMapTask().get(key).getStatus().equals("Finished")) {
        			gDatos.getMapTask().replace(key, taskProcess);
        		}
        	} else {
        		/**
        		 * No existe, hay que agregarla
        		 */
        		gDatos.getMapTask().put(key, taskProcess);
        	}
        	
            //try {
				logger.info("Actualizada Task: " + entry.getKey()); // + ", valor=" + serializeObjectToJSon(gDatos.getMapTask().get(key), false));
			//} catch (IOException e) {
				// TODO Auto-generated catch block
				//logger.error("Error desplegando desserializacion objeto taskProcess");
				//e.printStackTrace();
			//}
        }
    }
    
    public synchronized void updateAssignedProcess(JSONObject jData) {
        try {
            AssignedTypeProc assignedTypeProc = new AssignedTypeProc();
            List<AssignedTypeProc> lstAssignedTypeProc = new ArrayList<>();
            
            JSONArray jArray = jData.getJSONArray("assignedTypeProc");
            int numItems = jArray.length();
            
            logger.info("Se han recibido: "+numItems+" asignaciones de Tipos de Procesos.");
            
            if (numItems>0) {

                for (int i=0; i<numItems; i++) {
                    assignedTypeProc = (AssignedTypeProc) serializeJSonStringToObject(jArray.get(i).toString(), AssignedTypeProc.class);
                    lstAssignedTypeProc.add(assignedTypeProc);
                }

                /**
                 * Actualiza la lista sobre el objeto serviceStatus
                 */
                gDatos.getServiceStatus().setLstAssignedTypeProc(lstAssignedTypeProc);
                gDatos.getServiceStatus().setIsAssignedTypeProc(true);
            } else {
                gDatos.getServiceStatus().setIsAssignedTypeProc(false);
            }
        } catch (JSONException | IOException e) {
        	gDatos.getServiceStatus().setIsAssignedTypeProc(false);
            logger.error("Error actualizando Asignacion de Procesos: " + e.getMessage());
        }
    }
    
    public String sendPoolProcess() {
        try {

            return null;
        } catch (Exception e) {
            return sendError(1, "error en send pool process: "+e.getMessage());
        }
    }
    
    public int enqueProcess(JSONObject jData) {
        try {
            return 0;
        } catch (Exception e) {
            return 1;
        }
    }
    
    public void DELupdateActiveProcess(String inputData) {
       
    }
    
    public String sendDate() {
        try {
            JSONObject jo = new JSONObject();
            JSONArray ja = new JSONArray();
            JSONObject mainjo = new JSONObject();

            jo.put("fecha", getDateNow("yyyy-MM-dd HH:mm:ss"));
            ja.put(jo);
            mainjo.put("data", ja);
            mainjo.put("result", "OK");
            return mainjo.toString();
        } catch (Exception e) {
            return sendError(99, e.getMessage());
        }
    }
    
    public String serializeObjectToJSon (Object object, boolean formated) throws IOException {
        org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
        
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, formated);
        
        return mapper.writeValueAsString(object);
    }
    
    @SuppressWarnings("unchecked")
	public Object serializeJSonStringToObject (String parseJson, @SuppressWarnings("rawtypes") Class className) throws IOException {
        org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
        
        return mapper.readValue(parseJson, className);
    }    
}
