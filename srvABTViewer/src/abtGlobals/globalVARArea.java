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
import java.util.logging.Level;
import java.util.logging.Logger;
import mypkg.oracleDB;
import srvabtviewer.ttModuleControl;

/**
 *
 * @author ABT
 */
public class globalVARArea {
    //Variables Globales de Conexion al MetaData ABT
    private String cfg_abtDBName;
    private String cfg_abtDBUser;
    private String cfg_abtDBPass;
    private String cfg_abtDBPort;
    private String cfg_abtDBHost;
    private String cfg_abtDBType;
    
    //Variables Globales de Propias del Servicio
    private String cfg_srv_ID;
    private String cfg_srv_Desc;
    private String cfg_srv_Online;
    private String cfg_srv_Txp_Control;
    private String cfg_srv_Txp_Keep;
    private String cfg_srv_Txp_Proc;
    private String cfg_srv_Log_Active;
    private String cfg_srv_Log_Dir;
    private String cfg_srv_Max_thProcess;
    private ArrayList<String> cfg_srv_type_proc;
    private String cfg_host_name;
    
    //Variables para el Control de Acceso a Parametros y BD ABTViewer
    private boolean  isGetAbtConf		=false;
    private boolean  isGetAbtConnDB		=false;
    private boolean  isGetAbtConfDB             =false;
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

    //Variables de Control del Servicio
    private String   gErrorMessages;
    private int      gErrorNumber;
   
    //Variable de Control Accesos a BD
    private oracleDB connOraKeep;
    private oracleDB connOraProc;
    private boolean  oraKeepConnected           =false;
    private boolean  oraProcConnected           =false;
    

    public String getCfg_abtDBType() {
        return cfg_abtDBType;
    }

    //Getter & Setter : Variables Globales de Conexion al MetaData ABT
    public void setCfg_abtDBType(String cfg_abtDBType) {    
        this.cfg_abtDBType = cfg_abtDBType;
    }

    public String getCfg_abtDBName() {
        return cfg_abtDBName;
    }

    public void setCfg_abtDBName(String cfg_abtDBName) {
        this.cfg_abtDBName = cfg_abtDBName;
    }

    public String getCfg_abtDBUser() {
        return cfg_abtDBUser;
    }

    public void setCfg_abtDBUser(String cfg_abtDBUser) {
        this.cfg_abtDBUser = cfg_abtDBUser;
    }

    public String getCfg_abtDBPass() {
        return cfg_abtDBPass;
    }

    public void setCfg_abtDBPass(String cfg_abtDBPass) {
        this.cfg_abtDBPass = cfg_abtDBPass;
    }

    public String getCfg_abtDBPort() {
        return cfg_abtDBPort;
    }

    public void setCfg_abtDBPort(String cfg_abtDBPort) {
        this.cfg_abtDBPort = cfg_abtDBPort;
    }

    public String getCfg_abtDBHost() {
        return cfg_abtDBHost;
    }

    public void setCfg_abtDBHost(String cfg_abtDBHost) {
        this.cfg_abtDBHost = cfg_abtDBHost;
    }

    
    //Getter & Setter : Variables Globales de Propias del Servicio

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

    public String getCfg_srv_Desc() {
        return cfg_srv_Desc;
    }

    public void setCfg_srv_Desc(String cfg_srv_Desc) {
        this.cfg_srv_Desc = cfg_srv_Desc;
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
    
    public ArrayList<String> getCfg_srv_type_proc() {
        return cfg_srv_type_proc;
    }

    public void setCfg_srv_type_proc(ArrayList<String> cfg_srv_type_proc) {
        this.cfg_srv_type_proc = cfg_srv_type_proc;
    }
    
    public boolean isIsGetTypeProc() {
        return isGetTypeProc;
    }

    public void setIsGetTypeProc(boolean isGetTypeProc) {
        this.isGetTypeProc = isGetTypeProc;
    }

    public boolean isIsGetAbtConfDB() {
        return isGetAbtConfDB;
    }

    public void setIsGetAbtConfDB(boolean isGetAbtConfDB) {
        this.isGetAbtConfDB = isGetAbtConfDB;
    }

    public boolean isIsGetAbtConf() {
        return isGetAbtConf;
    }

    public void setIsGetAbtConf(boolean isGetAbtConf) {
        this.isGetAbtConf = isGetAbtConf;
    }

    public boolean isIsGetAbtConnDB() {
        return isGetAbtConnDB;
    }

    public void setIsGetAbtConnDB(boolean isGetAbtConnDB) {
        this.isGetAbtConnDB = isGetAbtConnDB;
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
    
    public globalVARArea() { //Constrctor Carga Datos Iniciales
        Properties abtConf = new Properties();
       
        isGetAbtConf = false;
        try {
            abtConf.load(new FileInputStream("D:\\Apps\\NetBeansProjects2\\jsrvins\\src\\abt\\abt.properties"));
            //
            // Recupera Parametros Base de Datos ABT
            cfg_abtDBName           = abtConf.getProperty("DBName");
            cfg_abtDBUser           = abtConf.getProperty("DBUser");
            cfg_abtDBPass           = abtConf.getProperty("DBPass");
            cfg_abtDBPort           = abtConf.getProperty("DBPort");
            cfg_abtDBHost           = abtConf.getProperty("DBHost");
            cfg_abtDBType           = abtConf.getProperty("DBType");
            
            //Recupera Parametros del Servicio
            cfg_srv_ID              = abtConf.getProperty("SRV_ID");
            cfg_srv_Desc            = abtConf.getProperty("SRV_DESC");
            cfg_srv_Online          = abtConf.getProperty("SRV_ONLINE");
            cfg_srv_Txp_Control     = abtConf.getProperty("SRV_TXP_CONTROL");
            cfg_srv_Txp_Keep        = abtConf.getProperty("SRV_TXP_KEEP");
            cfg_srv_Txp_Proc        = abtConf.getProperty("SRV_TXP_PROC");
            cfg_srv_Log_Active      = abtConf.getProperty("SRV_LOG_ACTIVE");
            cfg_srv_Log_Dir         = abtConf.getProperty("SRV_LOG_DIR");
            cfg_srv_Max_thProcess   = abtConf.getProperty("SRV_MAX_THPROCESS");
            
            isGetAbtConf            = true;
            
            try {
                cfg_host_name = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                cfg_host_name = "NOT_FOUND";
                Logger.getLogger(ttModuleControl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (FileNotFoundException e) {
            gErrorMessages  = e.getMessage();
            gErrorNumber    = 99;
        } catch (IOException e) {
            gErrorMessages  = e.getMessage();
            gErrorNumber    = 99;
        }
    }
}
