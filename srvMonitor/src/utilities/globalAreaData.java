/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import dataClass.ActiveTypeProc;
import dataClass.Agenda;
import dataClass.AssignedTypeProc;
import dataClass.ETL;
import dataClass.Grupo;
import dataClass.Interval;
import dataClass.ServerStatus;
import dataClass.ServerInfo;
import dataClass.ServiceStatus;
import dataClass.TaskProcess;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

/**
 *
 * @author andresbenitez
 */
public class globalAreaData {    

    Logger logger = Logger.getLogger("globalAreaData");
    
    //Referencia Data Class
    
    private ServerInfo serverInfo = new ServerInfo();
    private ServerStatus serverStatus = new ServerStatus();
    
    private List<Agenda> lstShowAgendas = new ArrayList<>();
    private List<Agenda> lstActiveAgendas = new ArrayList<>();
    private List<ETL> lstETLConf = new ArrayList<>();
    
    private Map<String, TaskProcess> mapTask = new TreeMap<>();
    private Map<String, ServiceStatus> mapServiceStatus = new TreeMap<>();
    private Map<String, AssignedTypeProc> mapAssignedTypeProc = new TreeMap<>();
    private Map<String, ActiveTypeProc> mapActiveTypeProc = new TreeMap<>();
    private Map<String, Grupo> mapGrupo = new TreeMap<>();
    private Map<String, Interval> mapInterval = new TreeMap<>();

    /**
     * Declaraciones de Getter ans Setter
     * @return 
     */
    
    
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public Map<String, Interval> getMapInterval() {
		return mapInterval;
	}

	public void setMapInterval(Map<String, Interval> mapInterval) {
		this.mapInterval = mapInterval;
	}

	public Map<String, Grupo> getMapGrupo() {
		return mapGrupo;
	}

	public void setMapGrupo(Map<String, Grupo> mapGrupo) {
		this.mapGrupo = mapGrupo;
	}

	public Map<String, ActiveTypeProc> getMapActiveTypeProc() {
		return mapActiveTypeProc;
	}

	public void setMapActiveTypeProc(Map<String, ActiveTypeProc> mapActiveTypeProc) {
		this.mapActiveTypeProc = mapActiveTypeProc;
	}

	public Map<String, AssignedTypeProc> getMapAssignedTypeProc() {
		return mapAssignedTypeProc;
	}

	public void setMapAssignedTypeProc(Map<String, AssignedTypeProc> mapAssignedTypeProc) {
		this.mapAssignedTypeProc = mapAssignedTypeProc;
	}

	public Map<String, ServiceStatus> getMapServiceStatus() {
		return mapServiceStatus;
	}

	public void setMapServiceStatus(Map<String, ServiceStatus> mapServiceStatus) {
		this.mapServiceStatus = mapServiceStatus;
	}

	public synchronized Map<String, TaskProcess> getMapTask() {
		return mapTask;
	}

	public synchronized void setMapTask(Map<String, TaskProcess> mapTask) {
		this.mapTask = mapTask;
	}

	public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(ServerStatus serverStatus) {
        this.serverStatus = serverStatus;
    }

    public List<Agenda> getLstShowAgendas() {
        return lstShowAgendas;
    }

    public void setLstShowAgendas(List<Agenda> lstShowAgendas) {
        this.lstShowAgendas = lstShowAgendas;
    }

    public List<Agenda> getLstActiveAgendas() {
        return lstActiveAgendas;
    }

    public void setLstActiveAgendas(List<Agenda> lstActiveAgendas) {
        this.lstActiveAgendas = lstActiveAgendas;
    }

    public List<ETL> getLstETLConf() {
        return lstETLConf;
    }

    public void setLstETLConf(List<ETL> lstETLConf) {
        this.lstETLConf = lstETLConf;
    }

    /**
     * Metodos Personalizados
     * @param pool 
     */
    public void updateStatusMapTask(String keyMapTask, String status) {
    	try {
    		getMapTask().get(keyMapTask).setSrvID("srv00001");
    		
    		getMapTask().get(keyMapTask).setStatus(status);
    		getMapTask().get(keyMapTask).setUpdateTime(getDateNow());
    	} catch (Exception e) {
    		logger.error("Error en updateStatusMapTask...: "+e.getMessage());
    	}
    	
    }
    
    public synchronized void updateMapGrupo(Grupo grupo) throws Exception {
    	//Actualiza la lista global de MapGrupos a partir de un Map
		if (!getMapGrupo().containsKey(grupo.getGrpID())) {
			getMapGrupo().put(grupo.getGrpID(), grupo);
		}
    	
    }
    
    public synchronized void updateMapGroup(Map<String, Grupo> vMapGrupo) throws Exception {
		//Actualiza la lista global de MapGrupos a partir de un Map
    	
    	for (Map.Entry<String, Grupo> entry : vMapGrupo.entrySet()) {
    		if (!getMapGrupo().containsKey(entry.getKey())) {
    			getMapGrupo().put(entry.getKey(), entry.getValue());
    		}
    	}
    }
    
    public synchronized void updateMapIntervalFromMD(Map<String, Interval> vMapInterval) {
    	/**
    	 * Actualiza las lista generica de MapInterval con la lista recuperada
    	 * desde metadata
    	 */
    	Interval interval;
    	if (vMapInterval.size()>0) {
    		for (Map.Entry<String, Interval> entry : vMapInterval.entrySet()) {
    			interval = new Interval();
    			interval = entry.getValue();
    					
    			if (getMapInterval().containsKey(entry.getKey())) {
    				//Si existe valida si cambia su estado en algunos casos
    				if (entry.getValue().getStatus().equals("Ready")) {
    					//Si viene un Ready valida que no este en ejecucion el global
    					if (!getMapInterval().get(entry.getKey()).getStatus().equals("Running")) {
    						//Actualiza el global con estado Ready
    						getMapInterval().get(entry.getKey()).setStatus("Ready");
    						getMapInterval().get(entry.getKey()).setFecUpdate(getDateNow());
    					}
    				}
    				
    			} else {
    				//Si no existe lo ingresa al map global
    				getMapInterval().put(entry.getKey(), entry.getValue());
    			}
    		}
    	}
    }
    
    
    public synchronized void addMapTask(String key, TaskProcess taskProcess) {
    	try {
    		mapTask.put(key, taskProcess);
    	} catch (Exception e) {
    		logger.error("Error en addMapTask para key: "+key+" err: "+e.getMessage());
    	}
    }
    
    public synchronized void replaceMapTask(String key, TaskProcess taskProcess) {
    	try {
    		if (mapTask.containsKey(key)) {
    			mapTask.replace(key, taskProcess);
    		} else {
    			logger.error("Error en replaceMapTask para key: "+key+" err: key no existe");
    		}
    	} catch (Exception e) {
    		logger.error("Error en replaceMapTask para key: "+key+" err: "+e.getMessage());
    	}
    }
    
    public int getIndexOfETLConf(String procID) {
        int index=-1;
        try {
            if (!lstETLConf.isEmpty()) {
                for (int i=0; i<lstETLConf.size(); i++) {
                    if (lstETLConf.get(i).getETLID().equals(procID)) {
                        index = i;
                        break;
                    }
                }
            }
            return index;
        } catch (Exception e) {
            return -1;
        }
    }
    
//    public synchronized void updateLstInterval(Interval interval) {
//    
//        List<Interval> lstTemp1 = this.lstInterval.stream().filter(p -> p.getETLID().equals(interval.getETLID())).collect(Collectors.toList());
//        List<Interval> lstTemp2 = lstTemp1.stream().filter(p -> p.getIntervalID().equals(interval.getIntervalID())).collect(Collectors.toList());
//        
//        int numIntervals = lstTemp2.size();
//        
//        if (numIntervals==0) {
//            lstInterval.add(interval);
//        }
//    }
    
    public synchronized void updateLstEtlConf(ETL etl) {
        try {
            if (lstETLConf.stream().filter(p -> p.getETLID().equals(etl.getETLID())).collect(Collectors.toList()).isEmpty()) {
                lstETLConf.add(etl);
            }
        } catch (Exception e) {
            logger.error("Error updateLstEtlConf..."+e.getMessage());
        }
    }
    
    public synchronized void updateLstActiveAgendas(Agenda agenda) {
        //Agrega nueva agenda a Lista de Agendas
        //Solo si AgeID y el numSecExec no esta
        try {
            if (this.lstActiveAgendas.stream().filter(p -> p.getAgeID().equals(agenda.getAgeID())&&p.getNumSecExec()==agenda.getNumSecExec()).collect(Collectors.toList()).isEmpty()) {
                this.lstActiveAgendas.add(agenda);
            }
        } catch (Exception e) {
            logger.error("Error updateLstActiveAgendas..."+e.getMessage());
        }
    }
    
//    public synchronized void updateStatusLstActiveGrupos(int posArray, Grupo group) {
//    	switch (group.getStatus()) {
//    		case "Pending":
//    			if (lstActiveGrupos.get(posArray).getStatus().equals("Sleeping")) {
//    				lstActiveGrupos.set(posArray, group);
//    			}
//    			break;
//    		case "Assigned":
//    			if (lstActiveGrupos.get(posArray).getStatus().equals("Pending")) {
//    				lstActiveGrupos.set(posArray, group);
//    			}
//    			break;
//    	}
//    	
//    }

//    public synchronized void updateLstActiveGrupos(Grupo grupo) {
//        
//        List<Grupo> lstTempGrupos = this.lstActiveGrupos.stream().filter(p -> p.getGrpID().equals(grupo.getGrpID())).collect(Collectors.toList());
//        
//        int numTempGrupos = lstTempGrupos.size();
//        boolean isSecFound = false;
//        
//        for (int i=0; i<numTempGrupos; i++) {
//            if (lstTempGrupos.get(i).getNumSecExec().equals(grupo.getNumSecExec())) {
//                isSecFound = true;
//            }
//        }
//        
//        if (!isSecFound) {
//            this.lstActiveGrupos.add(grupo);
//        }
//    }

//    public void updateLstServiceStatus(ServiceStatus serviceStatus) {
//        int numItems = lstServiceStatus.size();
//        boolean itemFound = false;
//        ServiceStatus myServiceStatus;
//        
//        for (int i=0; i<numItems; i++) {
//            if (lstServiceStatus.get(i).getSrvID().equals(serviceStatus.getSrvID())) {
//               myServiceStatus = lstServiceStatus.get(i);
//               myServiceStatus.setSrvEnable(serviceStatus.getSrvEnable());
//               myServiceStatus.setLstAssignedTypeProc(serviceStatus.getLstAssignedTypeProc());
//               lstServiceStatus.set(i, myServiceStatus);
//               itemFound = true;
//            }
//        }
//        
//        if (!itemFound) {
//            lstServiceStatus.add(serviceStatus);
//        }
//    }
    
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
    
    /**
     * 
     * Declaracion del Constructor
     * 
     */
    

    public globalAreaData() {
        Properties fileConf = new Properties();

        try {

        	/**
        	 * Lee archivo de properties
        	 */
            //String propertiesPath = this.getClass().getClassLoader().getResource("utilities").getPath();
            String propertiesName = "srvMonitor.properties";
            //String filePath = propertiesPath + propertiesName;

            fileConf.load(new FileInputStream(propertiesName));

            /**
             * Recupera Valores de Operacion del Servicio
             */
            serverInfo.setSrvID(fileConf.getProperty("srvID"));
            serverInfo.setTxpMain(Integer.valueOf(fileConf.getProperty("txpMain")));
            serverInfo.setTxpKeep(Integer.valueOf(fileConf.getProperty("txpKeep")));
            serverInfo.setTxpSocket(Integer.valueOf(fileConf.getProperty("txpSocket")));
            serverInfo.setTxpInscribe(Integer.valueOf(fileConf.getProperty("txpInscribe")));
            serverInfo.setTxpAsigna(Integer.valueOf(fileConf.getProperty("txpAsigna")));
            serverInfo.setSrvPort(Integer.valueOf(fileConf.getProperty("srvPort")));
            serverInfo.setAgeShowHour(Integer.valueOf(fileConf.getProperty("ageShowHour")));
            serverInfo.setAgeGapMinute(Integer.valueOf(fileConf.getProperty("ageGapMinute")));
            serverInfo.setAuthKey(fileConf.getProperty("authKey"));
            
            /**
             * Recupera Valores de acceso a Metadata
             */
            serverInfo.setDbType(fileConf.getProperty("dbType"));
            serverInfo.setDbHost(fileConf.getProperty("dbHost"));
            serverInfo.setDbPort(fileConf.getProperty("dbPort"));
            serverInfo.setDbUser(fileConf.getProperty("dbUser"));
            serverInfo.setDbPass(fileConf.getProperty("dbPass"));
            serverInfo.setDbName(fileConf.getProperty("dbName"));
            serverInfo.setDbInstance(fileConf.getProperty("dbInstance"));
            serverInfo.setDbJDBCDriver(fileConf.getProperty("dbJDBCDriver"));

            /**
             * Setea valores de operacion
             */
            serverStatus.setSrvActive(true);
            serverStatus.setIsValMetadataConnect(false);
            serverStatus.setIsGetAgendaActive(false);
            serverStatus.setIsThreadETLActive(false);

            //Extrae Fecha de Hoy
            //
            Date today;
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //System.out.println(formatter.getTimeZone());
            today = new Date();
            
            serverStatus.setSrvStartTime(formatter.format(today));
            serverStatus.setIsLoadParam(true);
            
            
        } catch (IOException | NumberFormatException e) {
            serverStatus.setIsLoadParam(false);
            logger.error("Error en constructor: "+e.getMessage());
        }
    }
}
