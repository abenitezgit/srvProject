/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvabtviewer;

import abtGlobals.globalVARArea;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import mypkg.oracleDB;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
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
        
        gDatos      = m;
        timerControl= new Timer();
        
        //Informa Global Area
        gDatos.setModControlOnTimer(true);
        
        //AutoInicia Modulo Control
        gDatos.writeLog(0, "Creando TH-Control");
        
        
        
        
        timerControl.schedule(new mainProc(), Integer.parseInt(gDatos.getCfg_srv_Txp_Control()), Integer.parseInt(gDatos.getCfg_srv_Txp_Control()) * 1000);
    }
    
    
    class mainKeep extends TimerTask {
        String [] srvHost;
        String [] srvProc;
        
        public void run() {
            //Informa Global Area
            gDatos.setKeepAliveRunning(true);
            gDatos.writeLog(0, "Running TH-Keep cada "+ gDatos.getCfg_srv_Txp_Keep() + " segundos");
            
            try {  //Inicio consulta a HB
                
                Configuration hcfg = HBaseConfiguration.create();
                String [] arrayConf = gDatos.getCfg_srv_hbConfFile();
                for (int i=0; i< arrayConf.length; i++  ) {
                    hcfg.addResource(new Path(arrayConf[i]));
                }
                HBaseAdmin hadmin = new HBaseAdmin( hcfg );
                HTablePool pool = new HTablePool(hcfg,100000);
                HTableInterface hTable = pool.getTable(gDatos.getCfg_srv_hbTableName());
                
                gDatos.writeLog(0, "TH-Keep Conectado a HBase");
                
                Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,new BinaryComparator(Bytes.toBytes(gDatos.getCfg_srv_ID())));

                Scan scan = new Scan();
                scan.setFilter(filter1);
                scan.setCaching(1);
                
                ResultScanner HBResult;

                HBResult = hTable.getScanner(scan);
                
                if (HBResult!=null) {

                    Result row = HBResult.next();  //Extrae la Primera Fila
                    
                    gDatos.writeLog(0, "TH-Keep: Comparando Parametros de Control");

                    for (KeyValue kv : row.raw()) {
                        String cf = new String(kv.getFamily());
                        String ca = new String(kv.getQualifier());
                        String key = new String(Bytes.toStringBinary ( kv.getKey(), 2, kv.getRowLength() ));
                        String val = new String(Bytes.toStringBinary( kv.getValue()));
                        
                        String [] vlist;
                        
                        switch (cf) {
                            case "srv":
                                switch (ca) {
                                    case "name":
                                        if (!gDatos.getCfg_srv_Name().equals(val)) {
                                            gDatos.writeLog(1, "Th-Keep: Changing value for SRV_NAME. Old: "+ gDatos.getCfg_srv_Name()+ " New: "+ val);
                                            gDatos.setCfg_srv_Name(val);
                                        }
                                        break;
                                    case "online":
                                        if (!gDatos.getCfg_srv_Online().equals(val)) {
                                            gDatos.writeLog(1, "Th-Keep: Changing value for SRV_ONLINE. Old: "+ gDatos.getCfg_srv_Online()+ " New: "+ val);
                                            gDatos.setCfg_srv_Online(val);
                                            gDatos.setExecProcChanging(true);

                                        }
                                        break;
                                    case "txpKeep":
                                        if (!gDatos.getCfg_srv_Txp_Keep().equals(val)) {
                                            gDatos.writeLog(1, "Th-Keep: Changing value for TXP-KEEP. Old: "+ gDatos.getCfg_srv_Txp_Keep()+ " New: "+ val);
                                            gDatos.setCfg_srv_Txp_Keep(val);
                                            gDatos.setKeepAliveChanging(true);
                                            
                                        }
                                        break;
                                    case "txpProc":
                                        if (!gDatos.getCfg_srv_Txp_Proc().equals(val)) {
                                            gDatos.writeLog(1, "Th-Keep: Changing value for TXP-PROC. Old: "+ gDatos.getCfg_srv_Txp_Proc()+ " New: "+ val);
                                            gDatos.setCfg_srv_Txp_Proc(val);
                                            gDatos.setExecProcChanging(true);

                                        }
                                        break;
                                    case "logActive":
                                        if (!gDatos.getCfg_srv_Log_Active().equals(val)) {
                                            gDatos.writeLog(1, "Th-Keep: Changing value for LOG_ACTIVE. Old: "+ gDatos.getCfg_srv_Log_Active()+ " New: "+ val);
                                            gDatos.setCfg_srv_Log_Active(val);

                                        }
                                        break;
                                    case "logDir":
                                        if (!gDatos.getCfg_srv_Log_Dir().equals(val)) {
                                            gDatos.writeLog(1, "Th-Keep: Changing value for LOG_DIR. Old: "+ gDatos.getCfg_srv_Log_Dir()+ " New: "+ val);
                                            gDatos.setCfg_srv_Log_Dir(val);

                                        }
                                        break;
                                    case "maxthproc":
                                        if (!gDatos.getCfg_srv_Max_thProcess().equals(val)) {
                                            gDatos.writeLog(1, "Th-Keep: Changing value for MAX_THPROC. Old: "+ gDatos.getCfg_srv_Max_thProcess()+ " New: "+ val);
                                            gDatos.setCfg_srv_Max_thProcess(val);

                                        }
                                        break;
                                    case "typeProc":
                                        vlist = val.split(",");
                                        for (int i=0; i<vlist.length; i++) {
                                            if (!gDatos.getCfg_srv_type_proc()[i].equals(vlist[i])) {
                                                gDatos.writeLog(1, "Th-Keep: Changing value for TYPE-PROC. Old: "+ gDatos.getCfg_srv_type_proc()[i]+ " New: "+ val);
                                                gDatos.setCfg_srv_type_proc(vlist);
                                            }
                                        }
                                        gDatos.setExecProcChanging(true);
                                        break;
                                    case "hostaut":
                                        vlist = val.split(",");
                                        for (int i=0; i<vlist.length; i++) {
                                            if (!gDatos.getCfg_srv_host_aut()[i].equals(vlist[i])) {
                                                gDatos.writeLog(1, "Th-Keep: Changing value for HOST_AUTH. Old: "+ gDatos.getCfg_srv_host_aut()[i]+ " New: "+ val);
                                                gDatos.setCfg_srv_host_aut(vlist);
                                            }
                                        }
                                        gDatos.setExecProcChanging(true);
                                        break;
                                        
                                    case "logName":
                                        if (!gDatos.getCfg_srv_Log_Name().equals(val)) {
                                            gDatos.writeLog(1, "Th-Keep: Changing value for LOG_NAME. Old: "+ gDatos.getCfg_srv_Log_Name()+ " New: "+ val);
                                            gDatos.setCfg_srv_Log_Name(val);
                                        }
                                    default:
                                        break;
                                }
                            default:
                                break;
                        }
                    }
                        
                } else {
                    gDatos.writeLog(2, "No Existe ID servicio Asociado");
                }
                
            
                //Cerrando conexiones a BD-HBASE
                HBResult.close();
                hTable.close();
                pool.close();
                hadmin.close();
                

                //Informa Global Area
                gDatos.writeLog(0, "Finished TH-Keep");
                gDatos.setKeepAliveRunning(false);
            } catch (Exception e) {
                gDatos.writeLog(2, "Error en TH-Keep: "+ e.getMessage());
                gDatos.setKeepAliveRunning(false);
            }
        }
    }
    
    class mainExec extends TimerTask {
        
        public boolean isServerAuth() {
            
            try {
            
                boolean serverAuth = false;

                int numHost = gDatos.getCfg_srv_host_aut().length;


                for (int i=0; i<numHost; i++) {
                    if (gDatos.getCfg_srv_host_aut()[i].equals(gDatos.getCfg_host_name())) {
                        serverAuth = true;
                    }
                }
                return(serverAuth);
            } catch (Exception e) {
                gDatos.writeLog(2, "Error Validando Server Auth");
                return(false);
            }
            
        }
        
        public void run() {
            //Informa Global Area
            gDatos.setExecProcRunning(true);
            gDatos.writeLog(0, "Running TH-ExecProc cada " + gDatos.getCfg_srv_Txp_Proc() + " segundos");
           
            if (gDatos.getCfg_srv_Online().equals("1")) {
                if (isServerAuth()) {
                    
                    //Valida Tipos de Procesos que pueden ser ejecutados
                    String [] dataProc = null;
                    String [] vProc = null;
                    String [] vPriority = null;
                    String [] vMaxThread = null;
                    
                    int numTypeProc = gDatos.getCfg_srv_type_proc().length;
                    
                    if (numTypeProc>0) {
                    
                        for (int i=0; i<numTypeProc; i++) {
                            dataProc        = gDatos.getCfg_srv_type_proc()[i].split(":");
                            vProc[i]        = dataProc[0];
                            vPriority[i]    = dataProc[1];
                            vMaxThread[i]   = dataProc[2];
                            
                            switch (vProc[i]) {
                                case "ETL":
                                    if ( Integer.parseInt(vMaxThread[i])>Integer.parseInt(gDatos.getgThUsedxProc()[i][i])) {
                                        gDatos.getgThUsedxProc()[i][i] = String.valueOf(Integer.parseInt(gDatos.getgThUsedxProc()[i][i])+1);
                                        Thread th1 = new thExecETL(gDatos);
                                        th1.start();
                                    } else {
                                        gDatos.writeLog(1, "No existen ThRead disponibles para Proc "+ vProc[i] + "Used: "+ gDatos.getgThUsedxProc()[i][i]);
                                    }
                                    
                                    break;
                            
                            
                                default:
                                    break;
                            }
                        }
                    } else {
                        gDatos.writeLog(1, "No existen procesos asociados a este Servicio");
                    }
                } else {
                    gDatos.writeLog(1, "Servidor no esta Autorizado para Ejecutar Procesos");
                }
            } else {
                gDatos.writeLog(1, "ServiceViewer esta inabilitado para Ejecutar Procesos");
            }
                
            gDatos.writeLog(1, "Finished Th-ExecProc");
            
            //Informa Global Area
            gDatos.setExecProcRunning(false);
        }
    }

    
    class mainProc extends TimerTask {
        
        public void run() {
            
            
            //Informa Global Area
            gDatos.writeLog(0, "Running TH-Control cada "+ gDatos.getCfg_srv_Txp_Control()+ " segundos");
            gDatos.setModControlRunning(true);
            
            //Validando Ejecución del TH-Keep ALIVE
            
            if (gDatos.isKeepAliveRunning()) {
                //No hacer Nada: Esta ene ejecucion
            } else { 
                if (gDatos.isKeepAliveOnTimer()) {
                    if (gDatos.isKeepAliveChanging()) {
                        //Botar el ThRead
                        //
                        gDatos.writeLog(1, "Killing TH-Keep");
                        timerKeep.cancel();

                        //Informa Global Area
                        gDatos.setKeepAliveOnTimer(false);
                        gDatos.setKeepAliveChanging(false);
                        gDatos.setKeepAliveRunning(false);
                    } else {
                        //No hacer nada: No ha cambiado y esta agendado
                    }
                } else {
                    // Agendar
                    // Inicia un Nuevo THread
                    gDatos.writeLog(1, "Creando TH-Keep");
                    timerKeep   = new Timer();
                    timerKeep.schedule(new mainKeep(), Integer.parseInt(gDatos.getCfg_srv_Txp_Keep()), Integer.parseInt(gDatos.getCfg_srv_Txp_Keep())*1000);
                
                    //Informa Global Area
                    gDatos.setKeepAliveOnTimer(true);
                }
            } 

            
            
            //Validando Ejecución del THREAD EXEC PROC
            
            if (gDatos.isExecProcRunning()) {
                //No hacer Nada: Esta ene ejecucion
            } else { 
                if (gDatos.isExecProcOnTimer()) {
                    if (gDatos.isExecProcChanging()) {
                        //Botar el ThRead
                        //
                        gDatos.writeLog(1, "Killing TH-ExecProc");
                        timerExec.cancel();

                        //Informa Global Area
                        gDatos.setExecProcOnTimer(false);
                        gDatos.setExecProcChanging(false);
                        gDatos.setExecProcRunning(false);
                    } else {
                        //No hacer nada: No ha cambiado y esta agendado
                    }
                } else {
                    // Agendar
                    // Inicia un Nuevo THread
                    gDatos.writeLog(1, "Creando TH-ExecProc");
                    timerExec   = new Timer();
                    timerExec.schedule(new mainExec(), Integer.parseInt(gDatos.getCfg_srv_Txp_Proc()), Integer.parseInt(gDatos.getCfg_srv_Txp_Proc())*1000);
                    
                
                    //Informa Global Area
                    gDatos.setExecProcOnTimer(true);
                }
            } 


        //Informa Global Area
        gDatos.writeLog(0, "Finished TH-Control");
        gDatos.setModControlRunning(false);
        }
    }
}