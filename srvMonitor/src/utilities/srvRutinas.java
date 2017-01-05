/*
 * To change this license header, choose License Headers in Project Properties.nnn
 */
package utilities;

import dataClass.AssignedTypeProc;
import dataClass.ETL;
import dataClass.Interval;
import dataClass.ServiceStatus;
import dataClass.TaskProcess;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import srvmonitor.MetaData;

/**
 *
 * @author andresbenitez
 */
public class srvRutinas {
    globalAreaData gDatos;
    Logger logger = Logger.getLogger("srvRutinas");
    
    /**
     * 
     * Inicia Constructor
     * @param m 
     */

    public srvRutinas(globalAreaData m) {
        try {
            gDatos = m;
            gDatos.getServerStatus().setIsLoadRutinas(true);
        } catch (Exception e) {
            gDatos.getServerStatus().setIsLoadRutinas(false);
        }
    }
    
    
    /**
     * 
     * Rutinas Publicas
     * @return 
     */
    
    public String sendGroups() {
        JSONObject jHeader = new JSONObject();
        JSONObject jData = new JSONObject();
        //List<Grupo> lstGroup = new ArrayList<>();
        
		try {
			
			JSONArray jArray = new JSONArray(serializeObjectToJSon(gDatos.getMapGrupo(), false));
			
			//String vGroups = serializeObjectToJSon(gDatos.getLstActiveGrupos(), false);
			//System.out.println("Groups: "+vGroups);
			
			//jArray.put(vGroups);
			jData.put("groups", jArray);
	        jHeader.put("data",jData);
	        jHeader.put("result", "OK");
	            
	        return jHeader.toString();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    	
    }
    
    public String sendGroup() {
    
	    JSONObject jHeader = new JSONObject();
	    JSONObject jData = new JSONObject();
	    //List<Grupo> lstGroup = new ArrayList<>();
	    
		try {
			
			JSONObject jo = new JSONObject(serializeObjectToJSon(gDatos.getMapGrupo(), false));
			
			jData.put("group", jo);
	        jHeader.put("data",jData);
	        jHeader.put("result", "OK");
	            
	        return jHeader.toString();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }

    public String sendTask() {
        JSONObject jHeader = new JSONObject();
        JSONObject jData = new JSONObject();
        //List<Grupo> lstGroup = new ArrayList<>();
        
		try {
			
			JSONObject jo = new JSONObject(serializeObjectToJSon(gDatos.getMapTask(), false));
			//JSONObject joInterval = new JSONObject(serializeObjectToJSon(gDatos.getMapInterval(), false));
			
			//String vGroups = serializeObjectToJSon(gDatos.getLstActiveGrupos(), false);
			//System.out.println("Groups: "+vGroups);
			
			//jArray.put(vGroups);
			//jData.put("interval", joInterval);
			jData.put("task", jo);
	        jHeader.put("data",jData);
	        jHeader.put("result", "OK");
	            
	        return jHeader.toString();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }

    
    public String sendFTPservices(String ftpID) {
        JSONObject jData = new JSONObject();
        JSONObject jHeader = new JSONObject();
        JSONObject jo;
        JSONArray ja = new JSONArray();
        MetaData metadata = new MetaData(gDatos);
        try {
            if (metadata.isConnected()) {
                String vSQL = "select * from process.tb_ftp where ftpEnable=1 order by ftpID";
                ResultSet rs = (ResultSet) metadata.getQuery(vSQL);
                if (rs!=null) {
                    while (rs.next()) {
                        jo = new JSONObject();
                        jo.put("ftpID",rs.getString("ftpID"));
                        jo.put("ftpDESC", rs.getString("ftpDESC"));
                        jo.put("fileSourceName", rs.getString("fileSourceName"));
                        ja.put(jo);
                    }
                }
                jData.put("details", ja);
                jHeader.put("data", jData);
                jHeader.put("request", "OK");
                return jHeader.toString();
            } else {
                logger.error("Se perdió conexión a Metadata");
                return sendError(0);
            }
            
        } catch (SQLException | JSONException e) {
            return sendError(0);
        }
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
            System.out.println(formatter.getTimeZone());
            today = new Date();
            return formatter.format(today);  
        } catch (Exception e) {
            return null;
        }
    }
        
    public String sendPing() {
        JSONObject jData = new JSONObject();
        JSONObject jHeader = new JSONObject();
            
        jHeader.put("data",jData);
        jHeader.put("auth",gDatos.getServerInfo().getAuthKey());
        jHeader.put("request", "ping");
            
        return jHeader.toString();
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
    
    public String getSqlFindGroup(String dbType, String ageID, String numSecExec) {
    	String vSQL="";
    	switch (dbType) {
    	case "ORA":
            vSQL =  "select gr.GRPID, gr.GRPDESC, to_char(gr.UFECHAEXEC,'rrrr-mm-dd hh24:mi:ss') UFECHAEXEC, gr.USTATUS, gr.STATUS, gr.LASTNUMSECEXEC,  gr.CLIID, ha.HORID, " +
            		"pr.PROCID, pr.NORDER, pr.CRITICAL, pr.TYPE " +
                    "from " +
                    "  process.tb_group gr, " +
                    "  process.tb_schedDiary ha, " +
                    "  process.tb_client cl, " +
                    "  process.TB_schedule ho, " +
                    "  process.TB_diary ag, " +
                    "  process.TB_PROCGROUP pr " +
                    "where " +
                    "  gr.GRPID = pr.GRPID " +
                    "  and gr.HORID = ho.HORID " +
                    "  and ha.HORID = ho.HORID " +
                    "  and ha.AGEID = ag.AGEID " +
                    "  and gr.CLIID = cl.CLIID " +
                    "  and gr.HORID = ha.HORID " +
                    "  and ha.HORINCLUSIVE=1 " +
                    "  and gr.ENABLE =1 " +
                    "  and gr.LASTNUMSECEXEC < '"+numSecExec+"' " +
                    "  and ha.AGEID='"+ageID+"' " +
                    "  and cl.ENABLE = 1 " +
                    "  and ag.AGEENABLE = 1 " +
                    "  and ho.HORENABLE = 1 " +
                    "  and ha.HORENABLE = 1 " +
                    "  and pr.ENABLE = 1";
    		break;
    	case "SQL":
    		break;
    	case "mySQL":
    		vSQL = 	"select gr.GRPID, gr.GRPDESC, date_format(gr.UFECHAEXEC,'%Y-%m-%d %H:%i:%s') UFECHAEXEC, gr.USTATUS, gr.STATUS, gr.LASTNUMSECEXEC,  gr.CLIID, ha.HORID, " +
    				"pr.PROCID, pr.NORDER, pr.CRITICAL, pr.TYPE  " +
                    "from " +
                    "  tb_group gr, " +
                    "  tb_schedDiary ha, " +
                    "  tb_client cl, " +
                    "  TB_schedule ho, " +
                    "  TB_diary ag, " +
                    "  TB_procGroup pr " +
                    "where " +
                    "  gr.GRPID = pr.GRPID " +
                    "  and gr.HORID = ho.HORID " +
                    "  and ha.HORID = ho.HORID " +
                    "  and ha.AGEID = ag.AGEID " +
                    "  and gr.CLIID = cl.CLIID " +
                    "  and gr.HORID = ha.HORID " +
                    "  and ha.HORINCLUSIVE=1 " +
                    "  and gr.ENABLE =1 " +
                    "  and gr.LASTNUMSECEXEC < '"+numSecExec+"' " +
                    "  and ha.AGEID='"+ageID+"' " +
                    "  and cl.ENABLE = 1 " +
                    "  and ag.AGEENABLE = 1 " +
                    "  and ho.HORENABLE = 1 " +
                    "  and ha.HORENABLE = 1 " +
                    "  and pr.ENABLE = 1";
    		break;
		default:
			vSQL="";
			break;
    	}
    	
    	return vSQL;
    }

    public String sendOkTX() {
        
        JSONObject jData = new JSONObject();
        JSONObject jHeader = new JSONObject();
        
        jHeader.put("data", jData);
        jHeader.put("result", "OK");
            
        return jHeader.toString();
    }
    
    public String sendStatusServices() {
        try {
            JSONObject jData = new JSONObject();
            JSONObject jHeader = new JSONObject();
            JSONArray jArray;
            
            jArray = new JSONArray(serializeObjectToJSon(gDatos.getMapServiceStatus(), false));
            
            jData.put("servicios", jArray);
            jHeader.put("data",jData);
            jHeader.put("result", "OK");

            return jHeader.toString();
        } catch (IOException | JSONException e) {
            return sendError(0,e.getMessage());
        }
    }
    
    public int getNumTaskStatus(Map<String, TaskProcess> mapTask, String status) {
    	try {
    		int numItems=0;
    		
    		for (Map.Entry<String, TaskProcess> entry : mapTask.entrySet()) {
    			if (entry.getValue().getStatus().equals(status)) {
    				numItems++;
    			}
    		}
    		
    		return numItems;
    	} catch (Exception e ) {
    		logger.error("Error en getNumTaskStatus...: "+e.getMessage());
    		return 0;
    	}
    	
    }
    
    public boolean updateStatusService(JSONObject jData) {
        try {
            ServiceStatus serviceStatus;
            
            serviceStatus = (ServiceStatus) serializeJSonStringToObject(jData.toString(), ServiceStatus.class);
            String srvID = serviceStatus.getSrvID();
            
            logger.info("Actualizando ServiceStatus del servicio: "+srvID);
            logger.info("Total de Asignaciones de Proceso informadas: "+serviceStatus.getMapAssignedTypeProc().size());
            logger.info("Total de tareas informadas: "+serviceStatus.getMapTask().size());
            logger.info("Total tareas en estado Ready: "+getNumTaskStatus(serviceStatus.getMapTask(),"Ready"));
            logger.info("Total tareas en estado Running: "+getNumTaskStatus(serviceStatus.getMapTask(),"Running"));
            logger.info("Total tareas en estado Abort: "+getNumTaskStatus(serviceStatus.getMapTask(),"Abort"));
            logger.info("Total tareas en estado Finished: "+getNumTaskStatus(serviceStatus.getMapTask(),"Finished"));
            
            /**
             * Actualizando la lista de Assigned Type Proc si la global esta vacia y hay datos 
             * desde el servicio
             */            
            for (Map.Entry<String, AssignedTypeProc> entry : serviceStatus.getMapAssignedTypeProc().entrySet()) {
            	if (!gDatos.getMapAssignedTypeProc().containsKey(entry.getKey())) {
            		gDatos.getMapAssignedTypeProc().put(entry.getKey(), entry.getValue());
            	}
            }
            
	    	/**
	    	 * Actualiza las Task informados por el servicio
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
	        		
	        		/*
	        		 * Actualiza status global informada por los servicios
	        		 * Actualiza task solo si cambio el status informado
	        		 */
	        		if (!newTask.getStatus().equals(updateTask.getStatus())) {
		        		updateTask.setEndTime(newTask.getEndTime());
		        		updateTask.setErrMesg(newTask.getErrMesg());
		        		updateTask.setErrNum(newTask.getErrNum());
		        		updateTask.setInsTime(newTask.getInsTime());
		        		updateTask.setStartTime(newTask.getStartTime());
		        		updateTask.setStatus(newTask.getStatus());
		        		updateTask.setUpdateTime(newTask.getUpdateTime());
		        		updateTask.setuStatus(newTask.getuStatus());
	        		}
	        		
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
	        					
	        					//Actualiza los status de todos los intervalos informados
	        					for (Map.Entry<String, Interval> newInt : newMapInterval.entrySet()) {
	        						if (updateMapInterval.containsKey(newInt.getKey())) {
	        							updateMapInterval.replace(newInt.getKey(), newInt.getValue());
	        						}
	        					}
	        					updateEtl.setMapInterval(updateMapInterval);
	        					updateTask.setParams(updateEtl);
	        					break;
	    					default:
	    						break;
	        			}
	        			
	        		}
	        	} else {
	        		/**
	        		 * No existe, error porque el monitor perdio el task que habia asignado al servicio
	        		 */
	        		logger.error("Task "+ key + " informado por " + srvID + " no se encuentra en srvMonitor");
	        	}
	        	
	            //try {
					logger.info("Actualizada Task: " + entry.getKey()); // + ", valor=" + serializeObjectToJSon(gDatos.getMapTask().get(key), false));
				//} catch (IOException e) {
					// TODO Auto-generated catch block
					//logger.error("Error desplegando desserializacion objeto taskProcess");
					//e.printStackTrace();
				//}
	        }

            
            logger.info("Actualizando Servicio informado: "+srvID+ " Status: "+serviceStatus.getSrvEnable());
            
            if (gDatos.getMapServiceStatus().containsKey(srvID)) {
            	/**
            	 * Existe el servicios inscrito
            	 * Se actualiza
            	 */
            	gDatos.getMapServiceStatus().replace(srvID, serviceStatus);
            } else {
            	/**
            	 * No existe, se procede a crear
            	 */
            	gDatos.getMapServiceStatus().put(srvID, serviceStatus);
            }
            
            logger.debug("Updated serviceStatus: "+serializeObjectToJSon(serviceStatus, false));
            logger.debug("Updated AssignedTypeProc: "+serializeObjectToJSon(gDatos.getMapAssignedTypeProc(), false));
            logger.debug("Updated MapTask: "+serializeObjectToJSon(gDatos.getMapTask(), false));
            
            return true;
        } catch (JSONException | IOException e) {
        	logger.error("Error actualizando status informado por servicio "+e.getMessage());
            return false;
        } catch (Exception e) {
        	logger.error("Error actualizando status informado por servicio "+e.getMessage());
            return false;        	
        }
    }
    
    public String sendGroupActives() throws JSONException, IOException {
        JSONObject jData = new JSONObject();
        JSONObject jHeader = new JSONObject();
    	JSONArray groupsActive = new JSONArray(serializeObjectToJSon(gDatos.getLstActiveAgendas(), false));
    	
        jData.put("groupsActive", groupsActive);
        jHeader.put("data",jData);
        jHeader.put("result", "OK");
        
        return jHeader.toString();
    	
    }

    public String sendAgeShow() throws JSONException, IOException {
        JSONObject jData = new JSONObject();
        JSONObject jHeader = new JSONObject();
    	JSONArray AgeShow = new JSONArray(serializeObjectToJSon(gDatos.getLstShowAgendas(), false));
    	
        jData.put("agendas", AgeShow);
        jHeader.put("data",jData);
        jHeader.put("result", "OK");
        
        return jHeader.toString();
    	
    }
    
    
    public String sendServiceInfo(String srvID)  {
        try {
            JSONObject jData = new JSONObject();
            JSONObject jHeader = new JSONObject();
            ServiceStatus serviceStatus = new ServiceStatus();
            Map<String, AssignedTypeProc> mapAssignedTypeProc = new TreeMap<>();
            Map<String, TaskProcess> mapTask = new TreeMap<>();
            Map<String, AssignedTypeProc> sendMapAssignedTypeProc = new TreeMap<>();
            Map<String, TaskProcess> sendMapTask = new TreeMap<>();
            Map<String, Interval> sendMapInterval = new TreeMap<>();

            
            /**
             * Extrae las asigcaciones de tipos de procesos desde la base de datos
             */
            if (getMDprocAssigned(srvID)) {
            
            	logger.debug("Extraccion de AssignedTypeProc desde Metadata exitosa");
            	logger.debug(srvID+" exist mapServiceStatus: "+gDatos.getMapServiceStatus().containsKey(srvID));
            	
            	
	            if (gDatos.getMapServiceStatus().containsKey(srvID)) {
	            	serviceStatus = gDatos.getMapServiceStatus().get(srvID);
	            	mapTask = new TreeMap<String, TaskProcess>(gDatos.getMapTask());
	            	mapAssignedTypeProc = new TreeMap<String, AssignedTypeProc>(gDatos.getMapAssignedTypeProc());
	            	
	            	
	            	/**
	            	 * Actualiza las Asignaciones de Procesos
	            	 */
	                for (Map.Entry<String, AssignedTypeProc> entry : mapAssignedTypeProc.entrySet()) {
	                	if (entry.getKey().substring(0,8).equals(srvID)) {
	                		sendMapAssignedTypeProc.put(entry.getKey(), entry.getValue());
	                	}
	                }
	                
	                serviceStatus.setMapAssignedTypeProc(sendMapAssignedTypeProc);
	                
	                /**
	                 * Actualiza las asignaciones de Task
	                 */
	                //logger.debug("mapTask: "+serializeObjectToJSon(mapTask, false));
	                for (Map.Entry<String, TaskProcess> entry : mapTask.entrySet()) {
	            		if (entry.getValue().getSrvID().equals(srvID)) {
	            			sendMapTask.put(entry.getKey(), entry.getValue());
	            		}
	                }
	                
	                serviceStatus.setMapTask(sendMapTask);
	                
	                /**
	                 * Actualiza las asignaciones de Intervalos
	                 */
	                //serviceStatus.setMapInterval(gDatos.getMapInterval());
	                
	            }
            }
            
            gDatos.getMapServiceStatus().replace(srvID, serviceStatus);
            
            String serviceString = serializeObjectToJSon(gDatos.getMapServiceStatus().get(srvID), false);
            
            jData.put("serviceStatus", serviceString);
            jHeader.put("data",jData);
            jHeader.put("result", "OK");
            
            return jHeader.toString();
        } catch (IOException | JSONException e) {
        	logger.error("Error IOException or JSONException desconocido en método sendServiceInfo: "+e.getMessage());
            return sendError(1,e.getMessage());
        } catch (Exception e) {
        	logger.error("Error Exception desconocido en método sendServiceInfo: "+e.getMessage());
            return sendError(1,e.getMessage());
        }
    }
    
    public String sendDate() {
        try {
            JSONObject jData = new JSONObject();
            //JSONArray ja = new JSONArray();
            JSONObject jHeader = new JSONObject();

            jData.put("fecha", getDateNow("yyyy-MM-dd HH:mm:ss"));
            jHeader.put("data", jData);
            jHeader.put("result", "OK");
            return jHeader.toString();
        } catch (Exception e) {
            return sendError(99, e.getMessage());
        }
    }
                  
    public boolean getMDprocAssigned(String srvID)  {
        try {
        	logger.info("Buscando AssignedTypeProc desde Metadata para servicio: "+srvID);
            MetaData metadata = new MetaData(gDatos);
            if (gDatos.getServerStatus().isIsValMetadataConnect()) {
                JSONArray ja;
                AssignedTypeProc assignedTypeProc = new AssignedTypeProc();

                String vSQL = "select srvID, srvDesc, srvEnable, srvTypeProc "
                        + "     from tb_services "
                		+ "     where srvID = '"+srvID+"'"
                        + "     order by srvID";
                try (ResultSet rs = (ResultSet) metadata.getQuery(vSQL)) {
                    if (rs!=null) {
                        while (rs.next()) {

                            ja = new JSONArray(rs.getString("srvTypeProc"));
                            
                            logger.info("Se encontraron "+ja.length()+" AssignedTypeProc para servicio "+srvID);
                            
                            //Ejemplo de Lista JSON de retorno
                            /* [ 
                            	 {"typeProc":"OSP","priority":3,"maxThread":3},
                            	 {"typeProc":"ETL","priority":3,"maxThread":2},
                            	 {"typeProc":"FTP","priority":1,"maxThread":2},
                            	 {"typeProc":"LOR","priority":2,"maxThread":2}
                               ]
                            
                            */
                            
                            /**
                             * Se genera el map correspondiente
                             */
                            for (int i=0; i<ja.length(); i++) {
                            	assignedTypeProc = (AssignedTypeProc) serializeJSonStringToObject(ja.get(i).toString(), AssignedTypeProc.class);
                            	gDatos.getMapAssignedTypeProc().put(srvID+":"+assignedTypeProc.getTypeProc(), assignedTypeProc);
                            }
                        }
                    } else {
                    	logger.error("No hay AssignedTypeProc para servico "+srvID);
                    }
                    rs.close();
                }
                metadata.closeConnection();
                logger.info("Asignación recuperadas desde metadata : "+serializeObjectToJSon(gDatos.getMapAssignedTypeProc(), false));
            } else {
            	logger.warn("No fue validada la conexiona metadata recuperando AssignedTypeProc");
            	logger.info("Se usaran las asignaciones existentes : "+serializeObjectToJSon(gDatos.getMapAssignedTypeProc(), false));
            }
            
            return true;
        } catch (SQLException | JSONException | IOException e) {
            logger.error("Error recuperando AssignedTypeProc. "+ e.getMessage());
            return false;
        }
    }
    
    public String serializeObjectToJSon (Object object, boolean formated) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, formated);

            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("Error serializeObjectToJson: "+e.getMessage());
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
	public Object serializeJSonStringToObject (String parseJson, @SuppressWarnings("rawtypes") Class className) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(parseJson, className);
        } catch (Exception e) {
            logger.error("Error serializeJSonStringToObject: "+e.getMessage());
            return null;
        }
    }        
}
