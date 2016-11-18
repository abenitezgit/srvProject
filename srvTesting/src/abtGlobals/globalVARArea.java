/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package abtGlobals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import mypkg.oracleDB;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;
import srvabtviewer.ttModuleControl;

/**
 *
 * @author ABT
 */
public class globalVARArea {
    private Logger logger = Logger.getLogger("MyLog");  
    private FileHandler fh;
    private Timer timertxp;
    private Timer timerproc;

    
    //Variables Globales de Conexion al MetaData SRV
    private String cfg_srv_DBType;
    private String cfg_srv_hbTableName;
    private String [] cfg_srv_hbConfFile;
    
    //Variables Globales de Propias del Servicio
    private String cfg_srv_ID;
    private String cfg_srv_Name;
    private String cfg_srv_Online;
    private String cfg_srv_Txp_Control;
    private String cfg_srv_Txp_Keep;
    private String cfg_srv_Txp_Proc;
    private String cfg_srv_Log_Active;
    private String cfg_srv_Log_Dir;
    private String cfg_srv_Log_Name;
    private String cfg_srv_Max_thProcess;
    private String [] cfg_srv_type_proc;
    private String [] cfg_srv_host_aut;
    private String cfg_host_name;  //HostName Local

    
    //Variables para el Control de Acceso a Parametros y BD SrvViewer
    private boolean  isGetSrvConf		=false;
    private boolean  isGetSrvConfDB             =false;
    private boolean  isGetTypeProc              =false;
    private boolean  isGetHostActive            =false;
	
    //Variable de Control Modulo TimerTask Keep Alive 
    private boolean  keepAliveOnTimer           =false;
    private boolean  keepAliveChanging          =false;
    private boolean  keepAliveRunning           =false;

    //Variables de Control Modulo TimerTask ExecProc
    private boolean  execProcOnTimer            =false;
    private boolean  execProcChanging           =false;
    private boolean  execProcRunning            =false;
    private String   execProcStatus		="offline";
    private ArrayList<String> numExecThread;

    //Variables de Control del Modulo de Control
    private boolean  modControlOnTimer          =false;
    private boolean  modControlChanging         =false;
    private boolean  modControlRunning          =false;
    private String [][] gThUsedxProc;

    //Variables de Control del Servicio
    private String   gErrorMessages;
    private int      gErrorNumber;
   
    //Variable de Control Accesos a BD
    private oracleDB connOraKeep;
    private oracleDB connOraProc;
    private boolean  oraKeepConnected           =false;
    private boolean  oraProcConnected           =false;

   
    //Getter & Setter : Variables Globales de Propias del Servicio

    public Timer getTimertxp() {
        return timertxp;
    }

    public void setTimertxp(Timer timertxp) {
        this.timertxp = timertxp;
    }

    public Timer getTimerproc() {
        return timerproc;
    }

    public void setTimerproc(Timer timerproc) {
        this.timerproc = timerproc;
    }
    
    

    public String[][] getgThUsedxProc() {
        return gThUsedxProc;
    }

    public void setgThUsedxProc(String[][] gThUsedxProc) {
        this.gThUsedxProc = gThUsedxProc;
    }
    
    public String getCfg_srv_Log_Name() {
        return cfg_srv_Log_Name;
    }

    public void setCfg_srv_Log_Name(String cfg_srv_Log_Name) {
        this.cfg_srv_Log_Name = cfg_srv_Log_Name;
    }

    public String[] getCfg_srv_host_aut() {
        return cfg_srv_host_aut;
    }

    public void setCfg_srv_host_aut(String[] cfg_srv_host_aut) {
        this.cfg_srv_host_aut = cfg_srv_host_aut;
    }
    
    public String getCfg_host_name() {
        return cfg_host_name;
    }

    public void setCfg_host_name(String cfg_host_name) {
        this.cfg_host_name = cfg_host_name;
    }
    
    public String getCfg_srv_ID() {
        return cfg_srv_ID;
    }

    public void setCfg_srv_ID(String cfg_srv_ID) {
        this.cfg_srv_ID = cfg_srv_ID;
    }

    public String getCfg_srv_Online() {
        return cfg_srv_Online;
    }

    public void setCfg_srv_Online(String cfg_srv_Online) {
        this.cfg_srv_Online = cfg_srv_Online;
    }

    public String getCfg_srv_Txp_Control() {
        return cfg_srv_Txp_Control;
    }

    public void setCfg_srv_Txp_Control(String cfg_srv_Txp_Control) {
        this.cfg_srv_Txp_Control = cfg_srv_Txp_Control;
    }

    public String getCfg_srv_Txp_Keep() {
        return cfg_srv_Txp_Keep;
    }

    public void setCfg_srv_Txp_Keep(String cfg_srv_Txp_Keep) {
        this.cfg_srv_Txp_Keep = cfg_srv_Txp_Keep;
    }

    public String getCfg_srv_Txp_Proc() {
        return cfg_srv_Txp_Proc;
    }

    public void setCfg_srv_Txp_Proc(String cfg_srv_Txp_Proc) {
        this.cfg_srv_Txp_Proc = cfg_srv_Txp_Proc;
    }

    public String getCfg_srv_Log_Active() {
        return cfg_srv_Log_Active;
    }

    public void setCfg_srv_Log_Active(String cfg_srv_Log_Active) {
        this.cfg_srv_Log_Active = cfg_srv_Log_Active;
    }

    public String getCfg_srv_Log_Dir() {
        return cfg_srv_Log_Dir;
    }

    public void setCfg_srv_Log_Dir(String cfg_srv_Log_Dir) {
        this.cfg_srv_Log_Dir = cfg_srv_Log_Dir;
    }

    public String getCfg_srv_Max_thProcess() {
        return cfg_srv_Max_thProcess;
    }

    public void setCfg_srv_Max_thProcess(String cfg_srv_Max_thProcess) {
        this.cfg_srv_Max_thProcess = cfg_srv_Max_thProcess;
    }
	
    
    //Getter & Setter : Variables para el Control de Acceso a Parametros y BD ABTViewer

    public boolean isIsGetHostActive() {
        return isGetHostActive;
    }

    public void setIsGetHostActive(boolean isGetHostActive) {
        this.isGetHostActive = isGetHostActive;
    }
    
    public String [] getCfg_srv_type_proc() {
        return cfg_srv_type_proc;
    }

    public void setCfg_srv_type_proc(String [] cfg_srv_type_proc) {
        this.cfg_srv_type_proc = cfg_srv_type_proc;
    }
    
    public boolean isIsGetTypeProc() {
        return isGetTypeProc;
    }

    public void setIsGetTypeProc(boolean isGetTypeProc) {
        this.isGetTypeProc = isGetTypeProc;
    }

    public boolean isIsGetAbtConfDB() {
        return isGetSrvConfDB;
    }

    public void setIsGetAbtConfDB(boolean isGetSrvConfDB) {
        this.isGetSrvConfDB = isGetSrvConfDB;
    }

    public boolean isIsGetAbtConf() {
        return isGetSrvConf;
    }

    public void setIsGetAbtConf(boolean isGetSrvConf) {
        this.isGetSrvConf = isGetSrvConf;
    }

    
    //Getter & Setter : Variable de Control Modulo TimerTask Keep Alive

    public boolean isKeepAliveOnTimer() {
        return keepAliveOnTimer;
    }

    public void setKeepAliveOnTimer(boolean keepAliveOnTimer) {
        this.keepAliveOnTimer = keepAliveOnTimer;
    }

    public boolean isKeepAliveChanging() {
        return keepAliveChanging;
    }

    public void setKeepAliveChanging(boolean keepAliveChanging) {
        this.keepAliveChanging = keepAliveChanging;
    }

    public boolean isKeepAliveRunning() {
        return keepAliveRunning;
    }

    public void setKeepAliveRunning(boolean keepAliveRunning) {
        this.keepAliveRunning = keepAliveRunning;
    }
    
    
    //Getter & Setter : Variables de Control Modulo TimerTask ExecProc

    public ArrayList<String> getNumExecThread() {
        return numExecThread;
    }

    public void setNumExecThread(ArrayList<String> numExecThread) {
        this.numExecThread = numExecThread;
    }

    public boolean isExecProcOnTimer() {
        return execProcOnTimer;
    }

    public void setExecProcOnTimer(boolean execProcOnTimer) {
        this.execProcOnTimer = execProcOnTimer;
    }

    public boolean isExecProcChanging() {
        return execProcChanging;
    }

    public void setExecProcChanging(boolean execProcChanging) {
        this.execProcChanging = execProcChanging;
    }

    public boolean isExecProcRunning() {
        return execProcRunning;
    }

    public void setExecProcRunning(boolean execProcRunning) {
        this.execProcRunning = execProcRunning;
    }

    public String getExecProcStatus() {
        return execProcStatus;
    }

    public void setExecProcStatus(String execProcStatus) {
        this.execProcStatus = execProcStatus;
    }
    
    
    //Getter & Setter : Variables de Control del Modulo de Control

    public boolean isModControlOnTimer() {
        return modControlOnTimer;
    }

    public void setModControlOnTimer(boolean modControlOnTimer) {
        this.modControlOnTimer = modControlOnTimer;
    }

    public boolean isModControlChanging() {
        return modControlChanging;
    }

    public void setModControlChanging(boolean modControlChanging) {
        this.modControlChanging = modControlChanging;
    }

    public boolean isModControlRunning() {
        return modControlRunning;
    }

    public void setModControlRunning(boolean modControlRunning) {
        this.modControlRunning = modControlRunning;
    }
    
    
    //Getter & Setter : Variables de Control del Servicio

    public String getgErrorMessages() {
        return gErrorMessages;
    }

    public void setgErrorMessages(String gErrorMessages) {
        this.gErrorMessages = gErrorMessages;
    }

    public int getgErrorNumber() {
        return gErrorNumber;
    }

    public void setgErrorNumber(int gErrorNumber) {
        this.gErrorNumber = gErrorNumber;
    }

    
    //Getter & Setter : Variable de Control Accesos a BD

    public oracleDB getConnOraKeep() {
        return connOraKeep;
    }

    public void setConnOraKeep(oracleDB connOraKeep) {
        this.connOraKeep = connOraKeep;
    }

    public oracleDB getConnOraProc() {
        return connOraProc;
    }

    public void setConnOraProc(oracleDB connOraProc) {
        this.connOraProc = connOraProc;
    }

    public boolean isOraKeepConnected() {
        return oraKeepConnected;
    }

    public void setOraKeepConnected(boolean oraKeepConnected) {
        this.oraKeepConnected = oraKeepConnected;
    }

    public boolean isOraProcConnected() {
        return oraProcConnected;
    }

    public void setOraProcConnected(boolean oraProcConnected) {
        this.oraProcConnected = oraProcConnected;
    }

    public String getCfg_srv_DBType() {
        return cfg_srv_DBType;
    }

    public void setCfg_srv_DBType(String cfg_srv_DBType) {
        this.cfg_srv_DBType = cfg_srv_DBType;
    }

    public String getCfg_srv_hbTableName() {
        return cfg_srv_hbTableName;
    }

    public void setCfg_srv_hbTableName(String cfg_srv_hbTableName) {
        this.cfg_srv_hbTableName = cfg_srv_hbTableName;
    }

    public String[] getCfg_srv_hbConfFile() {
        return cfg_srv_hbConfFile;
    }

    public void setCfg_srv_hbConfFile(String[] cfg_srv_hbConfFile) {
        this.cfg_srv_hbConfFile = cfg_srv_hbConfFile;
    }

    public String getCfg_srv_Name() {
        return cfg_srv_Name;
    }

    public void setCfg_srv_Name(String cfg_srv_Name) {
        this.cfg_srv_Name = cfg_srv_Name;
    }

    public boolean isIsGetSrvConf() {
        return isGetSrvConf;
    }

    public void setIsGetSrvConf(boolean isGetSrvConf) {
        this.isGetSrvConf = isGetSrvConf;
    }

    public boolean isIsGetSrvConfDB() {
        return isGetSrvConfDB;
    }

    public void setIsGetSrvConfDB(boolean isGetSrvConfDB) {
        this.isGetSrvConfDB = isGetSrvConfDB;
    }
    
    public void activaLog() throws IOException {

        try {

        // This block configure the logger with handler and formatter
        fh = new FileHandler(cfg_srv_Log_Dir + cfg_srv_Log_Name, true);

        logger.addHandler(fh);

        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        } catch (IOException e) {

        }
    
    } 

    public void writeLog(int Type, String Message) {

    // the following statement is used to log any messages
    switch (Type) {
        case 0:
            logger.info(Message);
            break;
        case 1:
            logger.warning(Message);
            break;
        case 2:
            logger.severe(Message);
            break;
        default:
            break;
        }
    }

    
    public globalVARArea() { //Constrctor Carga Datos Iniciales
        Properties srvConf = new Properties();
        timertxp = new Timer();
        timerproc = new Timer();

  
        isGetSrvConf = false;
        try {
            srvConf.load(new FileInputStream("/Users/andresbenitez/Documents/app/ABTViewer3/HBConf.properties"));
            //
            // Recupera Parametros Base de Datos SrvViewer
            cfg_srv_DBType           = srvConf.getProperty("DBType");
            
            if (cfg_srv_DBType.equals("HBASE")) {
                cfg_srv_hbConfFile = srvConf.getProperty("HBConf").split(",");
                cfg_srv_hbTableName = srvConf.getProperty("HBTable");
            
            } 
                    
            //Recupera Parametros del Servicio
            cfg_srv_ID              = srvConf.getProperty("SRV_ID");
            cfg_srv_Name            = srvConf.getProperty("SRV_DESC");
            cfg_srv_Online          = srvConf.getProperty("SRV_ONLINE");
            cfg_srv_Txp_Control     = srvConf.getProperty("SRV_TXP_CONTROL");
            cfg_srv_Txp_Keep        = srvConf.getProperty("SRV_TXP_KEEP");
            cfg_srv_Txp_Proc        = srvConf.getProperty("SRV_TXP_PROC");
            cfg_srv_Log_Active      = srvConf.getProperty("SRV_LOG_ACTIVE");
            cfg_srv_Log_Dir         = srvConf.getProperty("SRV_LOG_DIR");
            cfg_srv_Max_thProcess   = srvConf.getProperty("SRV_MAX_THPROCESS");
            cfg_srv_type_proc       = srvConf.getProperty("SRV_TYPE_PROC").split(",");
            cfg_srv_host_aut        = srvConf.getProperty("SRV_HOST_AUT").split(",");
            cfg_srv_Log_Name        = srvConf.getProperty("SRV_LOG_NAME");
            
            try {
                cfg_host_name = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                cfg_host_name = "NOT_FOUND";
                isGetSrvConf = false;
                Logger.getLogger(ttModuleControl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            isGetSrvConf            = true;
            
        } catch (FileNotFoundException e) {
            isGetSrvConf = false;
            gErrorMessages  = e.getMessage();
            gErrorNumber    = 99;
        } catch (IOException e) {
            isGetSrvConf = false;
            gErrorMessages  = e.getMessage();
            gErrorNumber    = 99;
        }
    }
}
