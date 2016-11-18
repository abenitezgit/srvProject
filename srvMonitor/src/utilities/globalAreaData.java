/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import dataClass.Agenda;
import dataClass.ETL;
import dataClass.Grupo;
import dataClass.Interval;
import dataClass.PoolProcess;
import dataClass.ServerStatus;
import dataClass.ServerInfo;
import dataClass.ServiceStatus;
import dataClass.TaskProcess;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    private List<ServiceStatus> lstServiceStatus = new ArrayList<>();
    private List<Agenda> lstShowAgendas = new ArrayList<>();
    private List<Agenda> lstActiveAgendas = new ArrayList<>();
    private List<Grupo> lstActiveGrupos = new ArrayList<>();
    private List<ETL> lstETLConf = new ArrayList<>();
    private List<Interval> lstInterval = new ArrayList<>();
    private List<PoolProcess> lstPoolProcess = new ArrayList<>();
    private Map<String,TaskProcess> mapTask = new HashMap<>();

    /**
     * Declaraciones de Getter ans Setter
     * @return 
     */
    
    public ServerInfo getServerInfo() {
        return serverInfo;
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

    public List<ServiceStatus> getLstServiceStatus() {
        return lstServiceStatus;
    }

    public void setLstServiceStatus(List<ServiceStatus> lstServiceStatus) {
        this.lstServiceStatus = lstServiceStatus;
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

    public synchronized List<Grupo> getLstActiveGrupos() {
        return lstActiveGrupos;
    }

    public synchronized void setLstActiveGrupos(List<Grupo> lstActiveGrupos) {
        this.lstActiveGrupos = lstActiveGrupos;
    }

    public List<ETL> getLstETLConf() {
        return lstETLConf;
    }

    public void setLstETLConf(List<ETL> lstETLConf) {
        this.lstETLConf = lstETLConf;
    }

    public List<Interval> getLstInterval() {
        return lstInterval;
    }

    public void setLstInterval(List<Interval> lstInterval) {
        this.lstInterval = lstInterval;
    }

    public List<PoolProcess> getLstPoolProcess() {
        return lstPoolProcess;
    }

    public void setLstPoolProcess(List<PoolProcess> lstPoolProcess) {
        this.lstPoolProcess = lstPoolProcess;
    }
    
    /**
     * Metodos Personalizados
     * @param pool 
     */
    public synchronized void addMapTask(Grupo grupo) {
    	String mapID = grupo.getGrpID()+"|"+grupo.getNumSecExec();
    	//getMapTask().put(mapID, grupo);
    }
    
    public synchronized void inscribePoolProcess(PoolProcess pool) {
        try {
            if (lstPoolProcess.isEmpty()) {
                lstPoolProcess.add(pool);
                logger.info("Se agregó nuevo process: "+pool.getGrpID()+" "+pool.getProcID()+" "+pool.getIntervalID());
            } else {
                if (pool.getTypeProc().equals("ETL")) {
                    if (lstPoolProcess
                            .stream()
                            .filter(p -> p.getProcID().equals(pool.getProcID())&&p.getIntervalID()
                            .equals(pool.getIntervalID()))
                            .collect(Collectors.toList()).isEmpty()) {
                        lstPoolProcess.add(pool);
                    }
                } else {
                    if (lstPoolProcess
                            .stream()
                            .filter(p -> p.getProcID().equals(pool.getProcID()))
                            .collect(Collectors.toList()).isEmpty()) {
                        lstPoolProcess.add(pool);
                        logger.info("Se agregó nuevo process: "+pool.getGrpID()+" "+pool.getProcID()+" "+pool.getIntervalID());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error en inscribePoolProcess..."+e.getMessage());
        }
    }
    
    public synchronized void updateLstPoolProcessInterval(PoolProcess pool) {
        List<PoolProcess> tmp = lstPoolProcess.stream().filter(p -> p.getProcID().equals(pool.getProcID())&&p.getIntervalID().equals(pool.getIntervalID())).collect(Collectors.toList());
        
        if (tmp.isEmpty()) {
            lstPoolProcess.add(pool);
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
    
    public synchronized void updateLstInterval(Interval interval) {
    
        List<Interval> lstTemp1 = this.lstInterval.stream().filter(p -> p.getETLID().equals(interval.getETLID())).collect(Collectors.toList());
        List<Interval> lstTemp2 = lstTemp1.stream().filter(p -> p.getIntervalID().equals(interval.getIntervalID())).collect(Collectors.toList());
        
        int numIntervals = lstTemp2.size();
        
        if (numIntervals==0) {
            lstInterval.add(interval);
        }
    }
    
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
    
    public synchronized void updateStatusLstActiveGrupos(int posArray, String status) { 
    	Grupo group = new Grupo();
    	group = lstActiveGrupos.get(posArray);
    	group.setStatus(status);
    	lstActiveGrupos.set(posArray, group);
    }

    public synchronized void updateLstActiveGrupos(Grupo grupo) {
        
        List<Grupo> lstTempGrupos = this.lstActiveGrupos.stream().filter(p -> p.getGrpID().equals(grupo.getGrpID())).collect(Collectors.toList());
        
        int numTempGrupos = lstTempGrupos.size();
        boolean isSecFound = false;
        
        for (int i=0; i<numTempGrupos; i++) {
            if (lstTempGrupos.get(i).getNumSecExec().equals(grupo.getNumSecExec())) {
                isSecFound = true;
            }
        }
        
        if (!isSecFound) {
            this.lstActiveGrupos.add(grupo);
        }
    }

    public void updateLstServiceStatus(ServiceStatus serviceStatus) {
        int numItems = lstServiceStatus.size();
        boolean itemFound = false;
        ServiceStatus myServiceStatus;
        
        for (int i=0; i<numItems; i++) {
            if (lstServiceStatus.get(i).getSrvID().equals(serviceStatus.getSrvID())) {
               myServiceStatus = lstServiceStatus.get(i);
               myServiceStatus.setSrvEnable(serviceStatus.getSrvEnable());
               myServiceStatus.setLstAssignedTypeProc(serviceStatus.getLstAssignedTypeProc());
               lstServiceStatus.set(i, myServiceStatus);
               itemFound = true;
            }
        }
        
        if (!itemFound) {
            lstServiceStatus.add(serviceStatus);
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
            fileConf.load(new FileInputStream("/Users/andresbenitez/Documents/Developer/EclipseProjects/ABT/srvMonitor/src/utilities/srvMonitor.properties"));

            /**
             * Recupera Valores de Operacion del Servicio
             */
            serverInfo.setSrvID(fileConf.getProperty("srvID"));
            serverInfo.setTxpMain(Integer.valueOf(fileConf.getProperty("txpMain")));
            serverInfo.setTxpAgendas(Integer.valueOf(fileConf.getProperty("txpAgendas")));
            serverInfo.setTxpETL(Integer.valueOf(fileConf.getProperty("txpETL")));
            serverInfo.setTxpKeep(Integer.valueOf(fileConf.getProperty("txpKeep")));
            serverInfo.setTxpSocket(Integer.valueOf(fileConf.getProperty("txpSocket")));
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
