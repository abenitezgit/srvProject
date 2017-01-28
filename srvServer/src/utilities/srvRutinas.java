/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import dataClass.AssignedTypeProc;
import dataClass.ETL;
import dataClass.Interval;
import dataClass.MOV;
import dataClass.ServiceStatus;
import dataClass.TaskProcess;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
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
            gDatos.getServiceStatus().setLoadRutinas(true);
        } catch (Exception e) {
            gDatos.getServiceStatus().setLoadRutinas(false);
        }
    }
    
    public String getDateNow() {
        try {
            //Extrae Fecha de Hoy
            //
            Date today;
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
    
    public String sendStatusService()  {
        try {
            
            gDatos.getServiceStatus().setSrvUpdateTime(getDateNow());
            
            // Se genera la salida de la lista 
            JSONObject jHeader = new JSONObject();
            JSONObject jData = new JSONObject();
            
            gDatos.getServiceStatus().setMapAssignedTypeProc(gDatos.getMapAssignedTypeProc());
            gDatos.getServiceStatus().setMapTask(gDatos.getMapTask());
            
            String serviceStatusString = serializeObjectToJSon(gDatos.getServiceStatus(), false).toString();
            JSONObject jo = new JSONObject(serviceStatusString);
            
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
    	try {
	    	ServiceStatus serviceStatus = new ServiceStatus();
			serviceStatus = (ServiceStatus) serializeJSonStringToObject(jData.getString("serviceStatus"), ServiceStatus.class);
			
			logger.info("Actualizando statusService desde srvMonitor...");
	    	
	    	/**
	    	 * Actualiza el Map AssignedTypeProc
	    	 */
			
	    	logger.info("Actualiza Map AssignedTypeProc");
	    	gDatos.setMapAssignedTypeProc(serviceStatus.getMapAssignedTypeProc());
	    	
	    	/**
	    	 * Actualiza las Task Asignadas
	    	 */
	    	logger.info("Actualizando Local MapTask");
	    	
	        for (Map.Entry<String, TaskProcess> entry : serviceStatus.getMapTask().entrySet()) {
	        	TaskProcess newTask = new TaskProcess();
	        	String key = entry.getKey();
	        	newTask = entry.getValue();
	        	
	        	//Valida si la Task ya esta creada
	        	if (gDatos.getMapTask().containsKey(key)) {
	        		/**
	        		 * Si existe la Task actualizar su status solo si fue abortada, suspendida
	        		 * y si no esta en ejecución
	        		 */
	        		TaskProcess updateTask = new TaskProcess();
	        		updateTask = gDatos.getMapTask().get(key);
	        		
	        		if (newTask.getStatus().equals("Abort")) {
	        			//Solo si viene una instruccion de abortar desde el modulo central
	        			
	        		} else {
	        			//Si no viene una instruccionn abortar deben primar los status locales ante los recibidos
	        			//por lo tanto solo deben actualizarse los Mapas e Items de los procesos correspondientes
	        			//determinados por el type de Proceso asociado a la TASK
	        			switch (newTask.getTypeProc()) {
	        				case "ETL":
	        					String newEtlString = serializeObjectToJSon(newTask.getParams(), false);
	        					ETL newEtl = new ETL();
	        					newEtl = (ETL) serializeJSonStringToObject(newEtlString, ETL.class);
	        					
	        					String updateEtlString = serializeObjectToJSon(updateTask.getParams(), false);
	        					ETL updateEtl = new ETL();
	        					updateEtl = (ETL) serializeJSonStringToObject(updateEtlString, ETL.class);
	        					
	        					Map<String, Interval> newMapInterval = new TreeMap<>(newEtl.getMapInterval());
	        					Map<String, Interval> updateMapInterval = new TreeMap<>(updateEtl.getMapInterval());
	        					
	        					//Actualiza el Map de Intervalos solo con los nuevos
	        					for (Map.Entry<String, Interval> newInt : newMapInterval.entrySet()) {
	        						if (!updateMapInterval.containsKey(newInt.getKey())) {
	        							updateMapInterval.put(newInt.getKey(), newInt.getValue());
	        						}
	        					}
	        					updateEtl.setMapInterval(updateMapInterval);
	        					updateTask.setParams(updateEtl);
	        					logger.info("Se actualizaron los intervalos del Task: "+key);
	        					break;
	    					default:
	    						break;
	        			}
	        			
	        		}
	        	} else {
	        		/**
	        		 * No existe, hay que agregarla
	        		 */
	        		gDatos.getMapTask().put(key, newTask);
	        		logger.info("Se ingreso nuevo Task: "+key);
	        	}
	        }

        	/**
        	 * Valida si las task informadas como Finished ya fueron registras por srvMonitor y eliminadas
        	 */
        	Map<String, TaskProcess> newTask = new TreeMap<>(serviceStatus.getMapTask());
        	Map<String, TaskProcess> updateTask = new TreeMap<>(gDatos.getMapTask());
        	for (Map.Entry<String, TaskProcess> entry : updateTask.entrySet()) {
        		if (newTask!=null) {
        			if (!newTask.containsKey(entry.getKey())) {
            			if (entry.getValue().getStatus().equals("Finished")) {
            				logger.info("Eliminando Task: "+ entry.getKey());
            				gDatos.getMapTask().remove(entry.getKey());
            			}
        			}
        		} else {
        			if (entry.getValue().getStatus().equals("Finished")) {
        				logger.info("Eliminando Task: "+ entry.getKey());
        				gDatos.getMapTask().remove(entry.getKey());
        			}
        		}
        	}
	        
	    	/**
	    	 * Analizar este tipo de actualizacion por posibles perdidas de sincronizacion
	    	 */
	    	logger.info("Actualiza serviceStatus global");
	    	gDatos.setServiceStatus(serviceStatus);
    	} catch (Exception e) {
    		logger.error("Error en updateStatusService...: "+e.getMessage());
    	}
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
    
    public synchronized void updateServiceStatistics() {
    	try {
    		
    	} catch (Exception e) {
    		logger.error("Error en updateServiceStatistics...: "+e.getMessage());
    	}
    }

    public synchronized void updateStatusTask(String keyTask, String status) {
        try {
            gDatos.getMapTask().get(keyTask).setStatus(status);
            gDatos.getMapTask().get(keyTask).setUpdateTime(getDateNow());
        } catch (Exception e) {
            logger.error("Error en updateStatusTask...: "+e.getMessage());
        }
    }
    
    public boolean isConnectedDB(Object object, String procType, dataAccess sConn, dataAccess dConn) {
    	try {
    		boolean isConnectDBSource;
    		boolean isConnectDBDest;
    		
    		String sourceDBName=null;
    		String sourceDBType=null;
    		String sourceIP=null;
    		String sourceDBPort=null;
    		String sourceUserName=null;
    		String sourceUserPass=null;
    		String sourceDBInstance=null;

    		String destDBName=null;
    		String destDBType=null;
    		String destIP=null;
    		String destDBPort=null;
    		String destUserName=null;
    		String destUserPass=null;
    		String destDBInstance=null;

    		switch (procType) {
	    		case "ETL":
	    			ETL etl = new ETL();
	    			etl = (ETL) object;
	    			sourceDBName = etl.getSDBNAME();
	    			sourceDBType = etl.getSDBTYPE();
	    			sourceIP = etl.getSIP();
	    			sourceDBPort = etl.getSDBPORT();
	    			sourceUserName = etl.getSUSERNAME();
	    			sourceUserPass = etl.getSUSERPASS();
	    			sourceDBInstance = etl.getSDBINSTANCE();
	    			
	    			destDBName = etl.getDDBNAME();
	    			destDBType = etl.getDDBTYPE();
	    			destIP = etl.getDIP();
	    			destDBPort = etl.getDDBPORT();
	    			destUserName = etl.getDUSERNAME();
	    			destUserPass = etl.getDUSERPASS();
	    			destDBInstance = etl.getDDBINSTANCE();
	    			break;
	    		case "MOV":
	    			MOV mov = new MOV();
	    			mov = (MOV) object;
	    			sourceDBName = mov.getSDBNAME();
	    			sourceDBType = mov.getSDBTYPE();
	    			sourceIP = mov.getSIP();
	    			sourceDBPort = mov.getSDBPORT();
	    			sourceUserName = mov.getSUSERNAME();
	    			sourceUserPass = mov.getSUSERPASS();
	    			sourceDBInstance = mov.getSDBINSTANCE();
	    			
	    			destDBName = mov.getDDBNAME();
	    			destDBType = mov.getDDBTYPE();
	    			destIP = mov.getDIP();
	    			destDBPort = mov.getDDBPORT();
	    			destUserName = mov.getDUSERNAME();
	    			destUserPass = mov.getDUSERPASS();
	    			destDBInstance = mov.getDDBINSTANCE();
	    			break;
				default:
					break;
    		}
    		
    		
        	/**
        	 * Establece conexion hacia BD Origen
        	 */
        	logger.info("Establece conexión a Base Origen: " + sourceDBName);
        	logger.info("instance: "+sourceDBInstance);
        	logger.info("dbType: " + sourceDBType);
        	logger.info("IP: " + sourceIP);
        	logger.info("Port: " + sourceDBPort);
        	logger.info("UserName: " + sourceUserName);
        	logger.debug("Pass: " + sourceUserPass);
        	
            sConn.setDbType(sourceDBType);
            sConn.setDbHost(sourceIP);
            sConn.setDbPort(sourceDBPort);
            sConn.setDbName(sourceDBName);
            sConn.setDbUser(sourceUserName);
            sConn.setDbPass(sourceUserPass);
            if (sourceDBType.equals("SQL")&&!sourceDBInstance.equals("default")) {
            	sConn.setDbName(sourceIP+"\\"+sourceDBInstance);
            }
            
            sConn.conectar();
            
            if (sConn.isConnected()) {
                logger.info("Base origen connected!");
                isConnectDBSource= true;
            } else {
            	logger.info("Base origen NO connected!");
                isConnectDBSource = false;
            }

            /**
             * Establece conexion hacia BD Destino
             */
            logger.info("Establece conexión a Base Destino: " + destDBName);
            logger.info("instance: "+destDBInstance);
        	logger.info("dbType: " + destDBType);
        	logger.info("IP: " + destIP);
        	logger.info("Port: " + destDBPort);
        	logger.info("UserName: " + destUserName);
        	logger.debug("Pass: " + destUserPass);
            
            dConn.setDbType(destDBType);
            dConn.setDbHost(destIP);
            dConn.setDbPort(destDBPort);
            dConn.setDbName(destDBName);
            dConn.setDbUser(destUserName);
            dConn.setDbPass(destUserPass);
            if (destDBInstance.equals("SQL")&&!sourceDBInstance.equals("default")) {
            	dConn.setDbHost(destIP+"\\"+destDBInstance);
            }
           
            dConn.conectar();
            
            if (dConn.isConnected()) {
                logger.info("Base Destino connected!");
                isConnectDBDest= true;
            } else {
            	logger.info("Base Destino NO connected!");
            	isConnectDBDest = false;
            }

    		if (isConnectDBSource&&isConnectDBDest) {
    			return true;
    		} else {
    			return false;
    		}
    		
    	} catch (Exception e) {
    		logger.error("Error modulo isConnectedDB...: "+e.getMessage());
    		return false;
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
