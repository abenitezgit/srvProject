/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import dataClass.ActiveTypeProc;
import dataClass.AssignedTypeProc;
import dataClass.ServiceInfo;
import dataClass.ServiceStatus;
import dataClass.TaskProcess;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import static utilities.srvRutinas.gDatos;

/**
 *
 * @author andresbenitez
 */
public class globalAreaData {
    /**
     * Clase de Logger4
     */
    Logger logger = Logger.getLogger("globalAreaData");
    
    /**
     * Clases de Estructuras de Datos
     */
    ServiceInfo serviceInfo = new ServiceInfo();
    ServiceStatus serviceStatus = new ServiceStatus();
    
    public ExecutorService mainExecThread = Executors.newFixedThreadPool(10);
    public ExecutorService processExecThread;
    
    private Map<String, TaskProcess> mapTask = new TreeMap<String, TaskProcess>();
    private Map<String, AssignedTypeProc> mapAssignedTypeProc = new TreeMap<>();
    private Map<String, ActiveTypeProc> mapActiveTypeProc = new TreeMap<>();

    /**
     * Getter And Setter Area
     * @param typeProc 
     */
    
    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

	public Map<String, ActiveTypeProc> getMapActiveTypeProc() {
		return mapActiveTypeProc;
	}

	public void setMapActiveTypeProc(Map<String, ActiveTypeProc> mapActiveTypeProc) {
		this.mapActiveTypeProc = mapActiveTypeProc;
	}

	public ExecutorService getMainExecThread() {
		return mainExecThread;
	}

	public void setMainExecThread(ExecutorService mainExecThread) {
		this.mainExecThread = mainExecThread;
	}

	public ExecutorService getProcessExecThread() {
		return processExecThread;
	}

	public void setProcessExecThread(ExecutorService processExecThread) {
		this.processExecThread = processExecThread;
	}

	public Map<String, AssignedTypeProc> getMapAssignedTypeProc() {
		return mapAssignedTypeProc;
	}

	public void setMapAssignedTypeProc(Map<String, AssignedTypeProc> mapAssignedTypeProc) {
		this.mapAssignedTypeProc = mapAssignedTypeProc;
	}

	public Map<String, TaskProcess> getMapTask() {
		return mapTask;
	}

	public void setMapTask(Map<String, TaskProcess> mapTask) {
		this.mapTask = mapTask;
	}

	public synchronized void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public synchronized void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
    
    //Procimientos y/p Metodos uilitarios
    //
    
    public String getFechaNow() {
            //Extrae Fecha de Hoy
            //
            Date today;
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            today = new Date();
            
            return formatter.format(today);
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
    
    public synchronized void setRunningTaskProcess(String keyTaskProcess) {
        try {
        	TaskProcess taskProcess = new TaskProcess();
        	taskProcess = gDatos.getMapTask().get(keyTaskProcess);
        	
        	if (gDatos.getMapTask().get(keyTaskProcess).getStatus().equals("Assigned")) {
        		taskProcess.setStatus("Running");
        		gDatos.getMapTask().replace(keyTaskProcess, taskProcess);
        	}
        } catch (Exception e) {
            logger.error("Error en setRunningTaskProcess: "+e.getMessage());
        }
    
    }
    
    public int getFreeThreadServices() {
        try {
            return serviceStatus.getNumProcMax()-serviceStatus.getNumProcRunning();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public globalAreaData() {
            Properties fileConf = new Properties();
            
            try {
                logger.info("Iniciando globalAreaData...");

                //Parametros del File Properties
                //
                
                //fileConf.load(new FileInputStream("/Users/andresbenitez/Documents/Apps/NetBeansProjects3/srvServer/src/utilities/srvServer.properties"));
                //String propertiesPath = this.getClass().getClassLoader().getResource("utilities").getPath();
                String propertiesName = "srvServer.properties";
                //String filePath = propertiesPath + propertiesName;

                fileConf.load(new FileInputStream(propertiesName));

                serviceInfo.setSrvID(fileConf.getProperty("srvID"));
                serviceInfo.setTxpMain(Integer.valueOf(fileConf.getProperty("txpMain")));
                serviceInfo.setAuthKey(fileConf.getProperty("authKey"));
                serviceInfo.setMonPort(Integer.valueOf(fileConf.getProperty("monPort")));
                serviceInfo.setMonPortBack(Integer.valueOf(fileConf.getProperty("monPortBack")));
                serviceInfo.setSrvHost(fileConf.getProperty("srvHost"));
                serviceInfo.setSrvMonHost(fileConf.getProperty("srvMonHost"));
                serviceInfo.setSrvMonHostBack(fileConf.getProperty("srvMonHostBack"));
                serviceInfo.setSrvPort(Integer.valueOf(fileConf.getProperty("srvPort")));
                
                serviceStatus.setSrvID(fileConf.getProperty("srvID"));
                serviceStatus.setNumProcMax(Integer.valueOf(fileConf.getProperty("numProcMax")));
                serviceStatus.setSrvEnable(1);
                serviceStatus.setSrvActive(true);
                serviceStatus.setActivePrimaryMonHost(true);
                serviceStatus.setSocketServerActive(false);
                serviceStatus.setConnectMonHost(false);
                serviceStatus.setAssignedTypeProc(false);
                serviceStatus.setNumProcRunning(0);
                serviceStatus.setNumProcReady(0);
                serviceStatus.setNumProcFinished(0);
                serviceStatus.setNumProcActive(0);
                serviceStatus.setSrvHost(serviceInfo.getSrvHost());
                serviceStatus.setSrvPort(serviceInfo.getSrvPort());
                serviceStatus.setSrvStartTime(getFechaNow());
                serviceStatus.setLoadParam(true);
                
                processExecThread = Executors.newFixedThreadPool(serviceStatus.getNumProcMax());
                
                logger.info("Se ha iniciado correctamente la globalAreaData...");
                
            } catch (IOException | NumberFormatException e) {
                serviceStatus.setLoadParam(false);
                logger.error(" Error general: "+e.getMessage());
            }
    }
}
