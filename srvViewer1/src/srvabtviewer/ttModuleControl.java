/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvabtviewer;

import abtGlobals.globalVARArea;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import mypkg.hbaseDB;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author ABT
 */
public class ttModuleControl {
    static String cCLASS_NAME = "ttModuleControl";
    private globalVARArea   gDatos;
    Timer                   timerControl;
    Timer                   timerKeep;
    Timer                   timerExec;
    int                     TxP_Control;
    int                     TxP_Keep;
    int                     TxP_Exec;
    int                     classErrorNum;
    String                  classErrorMsg;

    
    public ttModuleControl(globalVARArea m) throws IOException {  //Constructor
        String cMETHOD_NAME = "ttModuleControl";
        
        gDatos      = m;
        timerControl= new Timer();
        
        //Informa Global Area
        gDatos.setModControlOnTimer(true);
        
        //AutoInicia Modulo Control
        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Scheduling TH-mainProc()");
        timerControl.schedule(new mainProc(), Integer.parseInt(gDatos.getCfg_srv_Txp_Control()), Integer.parseInt(gDatos.getCfg_srv_Txp_Control()) * 1000);
    }
    
    //Hilo Principal: Se encarga de Lanzar los Hilos de los procesos:
    //      KeepAlive   : Valida cambios a nivel de configuración del servicio general
    //      MainExec    : Valida la ejecucion de procesos inscritos. Lanza hilos de procesos respectivos
    //+++++++++++++++++++++
    //Por ser un TimerTask se ejecuta en forma automativa el metodo run()
    class mainProc extends TimerTask {
        srv_rutinas gRutinas = new srv_rutinas(gDatos);
        String cMETHOD_NAME = "mainProc";
        boolean isHBConnect;
        hbaseDB connHBproc;
        int statusCodeProc;
        String statusMessageProc;
        
        //Constructor de la Clase
        public mainProc() {
            try {
                connHBproc = new hbaseDB(gDatos.getCfg_glb_metaDataConfPath(),"HBConf2");
                
                isHBConnect=true;
                
                statusCodeProc=0;
                statusMessageProc=null;
            
            } catch (Exception e) {
                isHBConnect=false;
                statusCodeProc = 99;
                statusMessageProc = cCLASS_NAME+" - "+e.getMessage();
            }
        
        }
        
        public void run() {
            try {
                
                if (isHBConnect) {
            
                    gRutinas.updateSrvStatus(connHBproc, "MCO","Running");
                    statusCodeProc = connHBproc.getStatusCode();
                    statusMessageProc = connHBproc.getStatusMessage();
                    
                    if (statusCodeProc==0) {

                        //Informa Global Area que mainProc esta en ejecucion
                        //
                        gDatos.setModControlRunning(true);

                        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Running Modulo de Control");


                        //Inicia Control de Ejecución del proceso KeepAlive (mainKeep())
                        //Valida en la Global Area si el proceso se ha reportado en modo Running
                        //
                        if (gDatos.isKeepAliveRunning()) {
                            //La Global Area reporta proceso en estado ejecuciob
                            //
                            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "TH-KeepAlive is Running");
                        } else { 
                            if (gDatos.isKeepAliveOnTimer()) {
                                if (gDatos.isKeepAliveChanging()) {
                                    //Botar el ThRead
                                    //
                                    gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "TH-KeepAlive is Killing");
                                    timerKeep.cancel();

                                    //Informa Global Area
                                    gDatos.setKeepAliveOnTimer(false);
                                    gDatos.setKeepAliveChanging(false);
                                    gDatos.setKeepAliveRunning(false);
                                } else {
                                    //No hacer nada: No ha cambiado y esta agendado
                                    gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "TH-KeepAlive is Waiting");
                                }
                            } else {
                                // Agendar
                                // Inicia un Nuevo THread
                                gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Start Schedule mainKeep()");
                                timerKeep   = new Timer();
                                timerKeep.schedule(new mainKeep(), Integer.parseInt(gDatos.getCfg_srv_Txp_Keep()), Integer.parseInt(gDatos.getCfg_srv_Txp_Keep())*1000);

                                //Informa Global Area
                                gDatos.setKeepAliveOnTimer(true);
                            }
                        } 

                        //Inicia Control de Ejecución del Ejecutor de Procesos (mainExec())
                        //Valida en la Global Area si el proceso se ha reportado en modo Running
                        //
                        if (gDatos.isExecProcRunning()) {
                            //No hacer Nada: Esta ene ejecucion
                            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "TH-ExecProc is Running");
                        } else { 
                            if (gDatos.isExecProcOnTimer()) {
                                if (gDatos.isExecProcChanging()) {
                                    //Botar el ThRead
                                    //
                                    gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "TH-ExecProc is Killing");
                                    timerExec.cancel();

                                    //Informa Global Area
                                    gDatos.setExecProcOnTimer(false);
                                    gDatos.setExecProcChanging(false);
                                    gDatos.setExecProcRunning(false);
                                } else {
                                    //No hacer nada: No ha cambiado y esta agendado
                                    gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "TH-ExecProc is Waiting");
                                }
                            } else {
                                // Agendar
                                // Inicia un Nuevo THread
                                //
                                gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Start Schedule mainExec()");
                                timerExec   = new Timer();
                                timerExec.schedule(new mainExec(), Integer.parseInt(gDatos.getCfg_srv_Txp_Proc()), Integer.parseInt(gDatos.getCfg_srv_Txp_Proc())*1000);


                                //Informa Global Area - Proceso Agendado
                                //
                                gDatos.setExecProcOnTimer(true);
                            }
                        } 

                        //Informa Global Area
                        
                        gDatos.setModControlRunning(false);

                        gRutinas.updateSrvStatus(connHBproc, "MCO","Sleeping");
                        statusCodeProc = connHBproc.getStatusCode();
                        statusMessageProc = connHBproc.getStatusMessage();
                        
                        if (statusCodeProc==0) {
                            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Finished Modulo de Control");
                        } else {
                            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Finished Modulo de Control con Errores - "+statusMessageProc);
                        }
                    } else {
                        gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "no puede actualizar status conf - "+statusMessageProc);
                    }
                } else {
                    gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "sin conexion a HBASE - "+statusMessageProc);
                }
            } catch (Exception e) {
                try {
                    gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Error: "+e.getMessage());
                    gDatos.setModControlRunning(false);
                    gRutinas.updateSrvStatus(connHBproc, "MCO","Sleeping");
                } catch (IOException ex) {
                    Logger.getLogger(ttModuleControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    class mainKeep extends TimerTask {
        srv_rutinas gRutinas = new srv_rutinas(gDatos);
        String cMETHOD_NAME = "mainKeep";
        hbaseDB connHBkeep;
        boolean isHBConnect;
        int statusCodeKeep;
        String statusMessageKeep;        
        
        public mainKeep() {
            //Establece la Conexion a la base de Datos HBase
            //
            try {
                connHBkeep = new hbaseDB(gDatos.getCfg_glb_metaDataConfPath(),"HBConf2");
                
                isHBConnect=true;
                
                statusCodeKeep=0;
                statusMessageKeep=null;
            
            } catch (Exception e) {
                isHBConnect=false;
                statusCodeKeep = 99;
                statusMessageKeep = cCLASS_NAME+" - "+e.getMessage();
            }
        }
        
        public void run() {
            try {
                
                if (isHBConnect) {
            
                    gRutinas.updateSrvStatus(connHBkeep, "KAL","Running");
                    statusCodeKeep = connHBkeep.getStatusCode();
                    statusMessageKeep = connHBkeep.getStatusMessage();
                    
                    if (statusCodeKeep==0) {            

                        //Informa Global Area
                        gDatos.setKeepAliveRunning(true);
                        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Running TH-Keep cada "+ gDatos.getCfg_srv_Txp_Keep() + " segundos");
            

                        //Establece el Filtro de Busqueda para el ID del Servicio
                        //
                        Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,new BinaryComparator(Bytes.toBytes(gDatos.getCfg_srv_ID())));

                        List<Filter> filters = new ArrayList<Filter>();

                        filters.add(filter1);
                
                        //Establece la Lista de Columnas a Recuperar

                        List<String> qualifier = new ArrayList<>();

                        qualifier.add("srv:hostval");
                        qualifier.add("srv:logActive");
                        qualifier.add("srv:logDir");
                        qualifier.add("srv:name");
                        qualifier.add("srv:online");
                        qualifier.add("srv:txpControl");
                        qualifier.add("srv:txpKeep");
                        qualifier.add("srv:txpProc");
                        qualifier.add("srv:typeProc");
                
                        //Genera Consulta a Base de Datos

                        DefaultTableModel dtmData = connHBkeep.getRsQuery(gDatos.getCfg_glb_hbTableName(), qualifier, filters);
                
                        if (dtmData.getRowCount()!=0) {
                            String val;
                            String [] vlist;
                            for (int cols=0; cols<dtmData.getColumnCount(); cols++) {
                                switch (dtmData.getColumnName(cols)) {
                                    case "name":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        if (!gDatos.getCfg_srv_Name().equals(val)) {
                                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for SRV_NAME. Old: "+ gDatos.getCfg_srv_Name()+ " New: "+ val);
                                            gDatos.setCfg_srv_Name(val);
                                        }
                                        break;
                                    case "online":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        if (!gDatos.getCfg_srv_Online().equals(val)) {
                                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for SRV_ONLINE. Old: "+ gDatos.getCfg_srv_Online()+ " New: "+ val);
                                            gDatos.setCfg_srv_Online(val);
                                        }
                                        break;
                                    case "txpKeep":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        if (!gDatos.getCfg_srv_Txp_Keep().equals(val)) {
                                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for TXP-KEEP. Old: "+ gDatos.getCfg_srv_Txp_Keep()+ " New: "+ val);
                                            gDatos.setCfg_srv_Txp_Keep(val);
                                            gDatos.setKeepAliveChanging(true);

                                        }
                                        break;
                                    case "txpProc":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        if (!gDatos.getCfg_srv_Txp_Proc().equals(val)) {
                                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for TXP-PROC. Old: "+ gDatos.getCfg_srv_Txp_Proc()+ " New: "+ val);
                                            gDatos.setCfg_srv_Txp_Proc(val);
                                            gDatos.setExecProcChanging(true);

                                        }
                                        break;
                                    case "logActive":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        if (!gDatos.getCfg_srv_Log_Active().equals(val)) {
                                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for LOG_ACTIVE. Old: "+ gDatos.getCfg_srv_Log_Active()+ " New: "+ val);
                                            gDatos.setCfg_srv_Log_Active(val);
                                        }
                                        break;
                                    case "logDir":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        if (!gDatos.getCfg_srv_Log_Dir().equals(val)) {
                                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for LOG_DIR. Old: "+ gDatos.getCfg_srv_Log_Dir()+ " New: "+ val);
                                            gDatos.setCfg_srv_Log_Dir(val);
                                        }
                                        break;
                                    case "maxthproc":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        if (!gDatos.getCfg_srv_Max_thProcess().equals(val)) {
                                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for MAX_THPROC. Old: "+ gDatos.getCfg_srv_Max_thProcess()+ " New: "+ val);
                                            gDatos.setCfg_srv_Max_thProcess(val);
                                        }
                                        break;
                                    case "typeProc":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        vlist = val.split(",");
                                        for (int i=0; i<vlist.length; i++) {
                                            if (!gDatos.getCfg_srv_type_proc()[i].equals(vlist[i])) {
                                                gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for TYPE-PROC. Old: "+ gDatos.getCfg_srv_type_proc()[i]+ " New: "+ val);
                                                gDatos.setCfg_srv_type_proc(vlist);
                                            }
                                        }
                                        break;
                                    case "hostaut":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        vlist = val.split(",");
                                        for (int i=0; i<vlist.length; i++) {
                                            if (!gDatos.getCfg_srv_host_auth()[i].equals(vlist[i])) {
                                                gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for HOST_AUTH. Old: "+ gDatos.getCfg_srv_host_auth()[i]+ " New: "+ val);
                                                gDatos.setCfg_srv_host_auth(vlist);
                                            }
                                        }
                                        break;

                                    case "logName":
                                        val = dtmData.getValueAt(0, cols).toString();
                                        if (!gDatos.getCfg_srv_Log_Name().equals(val)) {
                                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Changing value for LOG_NAME. Old: "+ gDatos.getCfg_srv_Log_Name()+ " New: "+ val);
                                            gDatos.setCfg_srv_Log_Name(val);
                                        }
                                    default:
                                        break;
                                }
                            }
                        } else {
                            gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "No Existe ID servicio Asociado");
                        }
                    } else {
                        gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "no puede actualizar status conf - "+statusMessageKeep);
                    }
                } else {
                    gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "sin conexion a HBASE - "+statusMessageKeep);
                }
                
                //Informa Global Area

                gDatos.setKeepAliveRunning(false);

                gRutinas.updateSrvStatus(connHBkeep, "KAL","Sleeping");
                statusCodeKeep = connHBkeep.getStatusCode();
                statusMessageKeep = connHBkeep.getStatusMessage();

                if (statusCodeKeep==0) {
                    gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Finished TH-Keep");
                } else {
                    gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Finished TH-Keep con Errores - "+statusMessageKeep);
                }                

            } catch (Exception e) {
                try {
                    gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Error: "+ e.getMessage());
                    gDatos.setKeepAliveRunning(false);
                    gRutinas.updateSrvStatus(connHBkeep, "KAL","Sleeping");
                } catch (IOException ex) {
                    Logger.getLogger(ttModuleControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    class mainExec extends TimerTask {
        srv_rutinas gRutinas = new srv_rutinas(gDatos);
        String cMETHOD_NAME = "mainExec";
        hbaseDB connHBexec;
        boolean isHBConnect;
        int statusCodeExec;
        String statusMessageExec;            
        
        public mainExec() {
            //Establece la Conexion a la base de Datos HBase
            //
            try {                
                connHBexec = new hbaseDB(gDatos.getCfg_glb_metaDataConfPath(),"HBConf2");
                
                isHBConnect=true;
                
                statusCodeExec=0;
                statusMessageExec=null;
            
            } catch (Exception e) {
                isHBConnect=false;
                statusCodeExec = 99;
                statusMessageExec = cCLASS_NAME+" - "+e.getMessage();
            }
        
        }
        
        public void run() {
            try {
                
                if (isHBConnect) {
            
                    gRutinas.updateSrvStatus(connHBexec, "EXE","Running");
                    statusCodeExec = connHBexec.getStatusCode();
                    statusMessageExec = connHBexec.getStatusMessage();
                    
                    if (statusCodeExec==0) {                       

                        //Informa Global Area
                        gDatos.setExecProcRunning(true);
                        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Running TH-ExecProc cada " + gDatos.getCfg_srv_Txp_Proc() + " segundos");

                        if (gDatos.getCfg_srv_Online().equals("1")) {
                            if (gRutinas.isServerAuth()) {

                                //Valida Tipos de Procesos que pueden ser ejecutados
                                String [] dataProc = null;
                                String [] vProc = new String[10];
                                String [] vPriority = new String[10];
                                String [] vMaxThread = new String[10];

                        
                                //Recupera la cantidad de Tipos de Procesos informados
                                //
                                int numTypeProc = gDatos.getCfg_srv_type_proc().length;
                        
                        
                                //Valida si hay Tipos de Procesos asociados al servicio
                                if (numTypeProc>0) {

                                    for (int i=0; i<numTypeProc; i++) {
                                        dataProc        = gDatos.getCfg_srv_type_proc()[i].split(":");
                                        vProc[i]        = dataProc[0];
                                        vPriority[i]    = dataProc[1];
                                        vMaxThread[i]   = dataProc[2];
                                    }
                                
                                    //Ordena Lista de Procesos por Prioridad
                                    //Metodo de la Burbuja
                                    //
                                    String vPasoProc;
                                    String vPasoPri;
                                    String vPasoThRead;

                                    for (int i=0; i<numTypeProc; i++) {
                                        for (int j=0; j<numTypeProc-1; j++) {
                                            int act = Integer.parseInt(vPriority[j]);
                                            int pos = Integer.parseInt(vPriority[j+1]);
                                            if (act>pos) {
                                                vPasoProc = vProc[j];
                                                vPasoPri = vPriority[j];
                                                vPasoThRead = vMaxThread[j];

                                                vProc[j] = vProc[j+1];
                                                vPriority[j] = vPriority[j+1];
                                                vMaxThread[j] = vMaxThread[j+1];

                                                vProc[j+1] = vPasoProc;
                                                vPriority[j+1] = vPasoPri;
                                                vMaxThread[j+1] = vPasoThRead;
                                            }
                                        }
                                    }
                                    
                                    int MaxTh;
                                
                                    for (int i=0; i<numTypeProc; i++) {
                                        switch (vProc[i]) {
                                            case "ETL":
                                                    if (vMaxThread[i]!=null) {
                                                        MaxTh = Integer.parseInt(vMaxThread[i]);
                                                    } else { 
                                                        MaxTh = 0;
                                                    }
                                        
                                                if ( MaxTh > gRutinas.getUsedThreadProc("ETL")) {
                                                    try {
                                                        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Priority: "+ vPriority[i]);
                                                        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Th Used: "+ gRutinas.getUsedThreadProc("ETL"));
                                                        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Starting TH-ExecETL");
                                                        if (gRutinas.increaseThreadProc("ETL")) {
                                                            Thread th1 = new thExecETL(gDatos);
                                                            th1.start();
                                                        } else {
                                                            gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "No es posible Start TH-ExecETL");
                                                        }
                                                    } catch (IOException ex) {
                                                        Logger.getLogger(ttModuleControl.class.getName()).log(Level.SEVERE, null, ex);
                                                        gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "IOExection :" + ex.getMessage());
                                                    }
                                                } else {
                                                    gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "No existen ThRead disponibles para Proc "+ vProc[i] + " Used: "+ gRutinas.getUsedThreadProc("ETL"));
                                                }

                                                break;
                                            case "OSP":
                                                    if (vMaxThread[i]!=null) {
                                                        MaxTh = Integer.parseInt(vMaxThread[i]);
                                                    } else { 
                                                        MaxTh = 0;
                                                    }
                                        
                                                if ( MaxTh > gRutinas.getUsedThreadProc("OSP")) {
                                                    try {
                                                        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Priority: "+ vPriority[i]);
                                                        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Th Used: "+ gRutinas.getUsedThreadProc("OSP"));
                                                        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Starting TH-ExecOSP");
                                                        if (gRutinas.increaseThreadProc("OSP")) {
                                                            Thread th1 = new thExecOSP(gDatos);
                                                            th1.start();
                                                        } else {
                                                            gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "No es posible Start TH-ExecOSP");
                                                        }
                                                    } catch (Exception ex) {
                                                        Logger.getLogger(ttModuleControl.class.getName()).log(Level.SEVERE, null, ex);
                                                        gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Exection :" + ex.getMessage());
                                                    }
                                                } else {
                                                    gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "No existen ThRead disponibles para Proc "+ vProc[i] + " Used: "+ gRutinas.getUsedThreadProc("OSP"));
                                                }

                                                break;

                                            default:
                                                break;
                                        }
                                    }
                                } else {
                                    gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "No existen procesos asociados a este Servicio");
                                }
                            } else {
                                gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Servidor "+ gDatos.getCfg_glb_hostName() +"no esta Autorizado para Ejecutar Procesos");
                            }
                        } else {
                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Servicio esta inabilitado para Ejecutar Procesos");
                        }
                    } else {
                        gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "no puede actualizar status conf - "+statusMessageExec);
                    }
                } else {
                    gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "sin conexion a HBASE - "+statusMessageExec);
                }

                //Informa Global Area

                gDatos.setExecProcRunning(false);

                gRutinas.updateSrvStatus(connHBexec, "EXE","Sleeping");
                statusCodeExec = connHBexec.getStatusCode();
                statusMessageExec = connHBexec.getStatusMessage();

                if (statusCodeExec==0) {
                    gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Finished Th-ExecProc");
                } else {
                    gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Finished Th-ExecProc con Errores - "+statusMessageExec);
                }                
                
            } catch (Exception e) {
                try {
                    gDatos.setExecProcRunning(false);
                    gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Error general " + e.getMessage());
                    gRutinas.updateSrvStatus(connHBexec, "EXE","Sleeping");
                } catch (IOException ex) {
                    Logger.getLogger(ttModuleControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}