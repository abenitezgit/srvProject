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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.table.DefaultTableModel;
import mypkg.hbaseDB;
import mypkg.oracleDB;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger; 
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author ABT
 */
public class globalVARArea {
    private Logger logger = Logger.getLogger("srvViewer");  
    //private FileHandler fh;  

    
    //Variables Globales
    private String cfg_glb_DBType;          //Tipo Motor BD srvConf para el KeepAlive
    private String cfg_glb_hbTableName;     //Tabla de Configuración srvConf
    private String [] cfg_glb_hbConfFile;   //Archivos de Configuración de HBASe
    private String cfg_glb_hostName;        //HostName Local
    
    //Variables Globales de Conexion a Metadata de HBASE
    private String cfg_glb_metaDataConfPath; //Metadata de configuracion para Hbase
    private Configuration cfg_glb_hcfg  = HBaseConfiguration.create();
    private int cfg_glb_hbRowsRead; //Para retorno de filas de una consulta en hbase
    
    
    //Variable Globales de BD de Conexion a srvViewer
    private String cfg_db_Host;
    private String cfg_db_Name;
    private String cfg_db_User;
    private String cfg_db_Pass;
    private String cfg_db_Port;
    private String cfg_db_Table;
    
    
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
    private String [] cfg_srv_host_auth;
    private String cfg_srv_FecInicio;
    
    
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

    //Variables de Control del Modulo de Control
    private boolean  modControlOnTimer          =false;
    private boolean  modControlChanging         =false;
    private boolean  modControlRunning          =false;
    private List<String> gThUsedxProc = new ArrayList<>();
    

    //Variables de Control del Servicio
    private String   gErrorMessages;
    private int      gErrorNumber;
   
    //Variable de Control Accesos a BD
    private oracleDB connOraKeep;
    private oracleDB connOraProc;
    private boolean  oraKeepConnected           =false;
    private boolean  oraProcConnected           =false;

   
    //Getter & Setter : Variables Globales de Propias del Servicio

    public int getCfg_glb_hbRowsRead() {
        return cfg_glb_hbRowsRead;
    }
    

    public String getCfg_glb_metaDataConfPath() {
        return cfg_glb_metaDataConfPath;
    }

    public void setCfg_glb_metaDataConfPath(String cfg_glb_metaDataConfPath) {
        this.cfg_glb_metaDataConfPath = cfg_glb_metaDataConfPath;
    }
    
    public String getCfg_srv_FecInicio() {
        return cfg_srv_FecInicio;
    }
    
    public List<String> getgThUsedxProc() {
        return gThUsedxProc;
    }

    public void setgThUsedxProc(List<String> gThUsedxProc) {
        this.gThUsedxProc = gThUsedxProc;
    }
    
    public String getCfg_srv_Log_Name() {
        return cfg_srv_Log_Name;
    }

    public void setCfg_srv_Log_Name(String cfg_srv_Log_Name) {
        this.cfg_srv_Log_Name = cfg_srv_Log_Name;
    }

    public String[] getCfg_srv_host_auth() {
        return cfg_srv_host_auth;
    }

    public void setCfg_srv_host_auth(String[] cfg_srv_host_auth) {
        this.cfg_srv_host_auth = cfg_srv_host_auth;
    }
    
    public String getCfg_glb_hostName() {
        return cfg_glb_hostName;
    }

    public void setCfg_glb_hostName(String cfg_glb_hostName) {
        this.cfg_glb_hostName = cfg_glb_hostName;
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

    public String getCfg_glb_DBType() {
        return cfg_glb_DBType;
    }

    public void setCfg_glb_DBType(String cfg_glb_DBType) {
        this.cfg_glb_DBType = cfg_glb_DBType;
    }

    public String getCfg_glb_hbTableName() {
        return cfg_glb_hbTableName;
    }

    public void setCfg_glb_hbTableName(String cfg_glb_hbTableName) {
        this.cfg_glb_hbTableName = cfg_glb_hbTableName;
    }

    public String[] getCfg_glb_hbConfFile() {
        return cfg_glb_hbConfFile;
    }

    public void setCfg_glb_hbConfFile(String[] cfg_glb_hbConfFile) {
        this.cfg_glb_hbConfFile = cfg_glb_hbConfFile;
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

    public void writeLog(int Type, String vThClassName, String vMethodName, String Message) {

    // the following statement is used to log any messages
    switch (Type) {
        case 0:
            logger.info(vThClassName +" - "+ vMethodName + " - " + Message);
            break;
        case 1:
            //logger.warning(Message);
            logger.warn(vThClassName +" - "+ vMethodName + " - " + Message);
            break;
        case 2:
            logger.error(vThClassName +" - "+ vMethodName + " - " + Message);
            break;
        default:
            break;
        }
    }

    
    public void writeLog(int Type, String vThClassName, String Message) {

    // the following statement is used to log any messages
    switch (Type) {
        case 0:
            logger.info(vThClassName +" - "+ Message);
            break;
        case 1:
            //logger.warning(Message);
            logger.warn(vThClassName +" - "+ Message);
            break;
        case 2:
            logger.error(vThClassName +" - "+ Message);
            break;
        default:
            break;
        }
    }
    
    public void writeLog(int Type, String Message) {

    // the following statement is used to log any messages
    switch (Type) {
        case 0:
            logger.info(Message);
            break;
        case 1:
            //logger.warning(Message);
            logger.warn(Message);
            break;
        case 2:
            logger.error(Message);
            break;
        default:
            break;
        }
    }

    public HTableInterface getConnectTable(String vHBTable) {
        try {
            
            HTablePool pool = new HTablePool(cfg_glb_hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            return(hTable);
            
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(hbaseDB.class.getName()).log(Level.SEVERE, null, e);
            return(null);
        }
    }
    
    public boolean putDataRows(HTableInterface hTable, String rowKey, List<String> qualifier) {
        boolean isPutData = false;
        try {
            List<String> cf_cols = new ArrayList<>();
            List<String> cq_cols = new ArrayList<>();
            List<String> vu_cols = new ArrayList<>();
        
            //Separa las Columnas Familias de las Columnas Qualifier y sus valores
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(",");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
                vu_cols.add(qa_paso[2]);
            }

            //Parametros de la conexion
            //
            Put p = new Put(Bytes.toBytes(rowKey));
            
            //Set valores a insertar / modificar
            //
            for (int cols=0; cols<qualifier.size(); cols++) {
                p.add(Bytes.toBytes(cf_cols.get(cols)), Bytes.toBytes(cq_cols.get(cols)),Bytes.toBytes(vu_cols.get(cols)));
            }
            
            hTable.put(p);
        
            isPutData = true;
            
            
            return(isPutData);
        } catch (Exception e) {
            System.out.println("Error General: "+e.getMessage());
            return(false);
        }
    }
    
    public boolean putDataRows(HTableInterface hTable, List<String> rowKey, List<String> qualifier) {
        boolean isPutData = false;
        try {
            List<String> cf_cols = new ArrayList<>();
            List<String> cq_cols = new ArrayList<>();
            List<String> vu_cols = new ArrayList<>();
        
            //Separa las Columnas Familias de las Columnas Qualifier y sus valores
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(":");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }            
            
            
            return(isPutData);
        } catch (Exception e) {
            return(false);
        }
    }
    
    public DefaultTableModel getRsQuery(HTableInterface hTable, List<String> qualifier, byte[] prefix, List<Filter> filters){
        try {
            DefaultTableModel model = new DefaultTableModel();
            Vector<Vector<Object>>  data    = new  Vector<>();
            Vector<String>          cq_cols = new Vector<>();
            List<String>            cf_cols = new ArrayList<>();
            
            //Separa las Columnas Familias de las Columnas Qualifier
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(":");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }
            
            //Genera la consulta a la base de datos
            //
            FilterList fList = new FilterList( FilterList.Operator.MUST_PASS_ALL, filters);
            
            Scan scan = new Scan(prefix);
            scan.setFilter(fList);
            scan.setCaching(10000);
            
            //Ingresa los Filtros de Columnas de Familia y Qualifiers
            //
            for (int i=0; i<cf_cols.size(); i++) {
                scan.addColumn(Bytes.toBytes(cf_cols.get(i)), Bytes.toBytes(cq_cols.get(i)));
            }            
            
            //Genera la Consulta a la BD
            //
            ResultScanner HBResult = hTable.getScanner(scan);
            
            //Extrae la DATA del Scanner realizado
            //
            List<Object> list_cq =  new ArrayList<>();
            List<Object> list_key = new ArrayList<>();
            List<Object> list_val = new ArrayList<>();
            
            String cf;
            String cq;
            String key;
            String val;
            
            Result rowIntervals = HBResult.next();
            
            //Inicializa el contador de registros leídos
            cfg_glb_hbRowsRead = 0;
            
            while (rowIntervals!=null) {
                
                Vector<Object> row = new Vector<>();
                list_key.clear();
                list_cq.clear();
                list_val.clear();

                for (KeyValue kv : rowIntervals.raw()) {
                    cf = new String(kv.getFamily());
                    cq = new String(kv.getQualifier());
                    key = Bytes.toStringBinary ( kv.getKey(), 2, kv.getRowLength() );
                    val = Bytes.toStringBinary( kv.getValue());
                    
                    list_key.add(key);
                    list_cq.add(cq);
                    list_val.add(val);
                }
                
                //Compara con Matriz de Columnna para Generar Registro Definitivo
                int pos=0;
                for (int i=0; i<cq_cols.size(); i++) {
                    pos = getPosInArray(list_cq, cq_cols.get(i));
                    if (pos>=0) {
                        row.add(list_val.get(pos).toString());
                    } else {
                        row.add("");
                    }
                }
                data.add(row);
                cfg_glb_hbRowsRead++;
                rowIntervals = HBResult.next();
            }
            
            model.setDataVector(data , cq_cols);
            
            return(model);
        
        } catch (Exception e) {
            System.out.println("Error general");
            return(null);
        }
    }
    
    public DefaultTableModel getRsQuery(HTableInterface hTable, List<String> qualifier, List<Filter> filters){

        try {
            DefaultTableModel model = new DefaultTableModel();
            Vector<Vector<Object>>  data    = new  Vector<>();
            Vector<String>          cq_cols = new Vector<>();
            List<String>            cf_cols = new ArrayList<>();
            
            //Separa las Columnas Familias de las Columnas Qualifier
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(":");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }
            
            //Genera la consulta a la base de datos
            //
            FilterList fList = new FilterList( FilterList.Operator.MUST_PASS_ALL, filters);

            Scan scan = new Scan();
            scan.setFilter(fList);
            scan.setCaching(10000);
            
            //Ingresa los Filtros de Columnas de Familia y Qualifiers
            //
            for (int i=0; i<cf_cols.size(); i++) {
                scan.addColumn(Bytes.toBytes(cf_cols.get(i)), Bytes.toBytes(cq_cols.get(i)));
            }            

            ResultScanner HBResult = hTable.getScanner(scan);

            //Extrae la DATA del Scanner realizado
            //
            List<Object> list_cq =  new ArrayList<>();
            List<Object> list_key = new ArrayList<>();
            List<Object> list_val = new ArrayList<>();
            
            String cf;
            String cq;
            String key;
            String val;
            
            Result rowIntervals = HBResult.next();
            
            //Inicializa el contador de registros leídos
            cfg_glb_hbRowsRead = 0;
            
            while (rowIntervals!=null) {
                
                Vector<Object> row = new Vector<>();
                list_key.clear();
                list_cq.clear();
                list_val.clear();

                for (KeyValue kv : rowIntervals.raw()) {
                    cf = new String(kv.getFamily());
                    cq = new String(kv.getQualifier());
                    key = Bytes.toStringBinary ( kv.getKey(), 2, kv.getRowLength() );
                    val = Bytes.toStringBinary( kv.getValue());
                    
                    list_key.add(key);
                    list_cq.add(cq);
                    list_val.add(val);
                }
                
                //Compara con Matriz de Columnna para Generar Registro Definitivo
                int pos=0;
                for (int i=0; i<cq_cols.size(); i++) {
                    pos = getPosInArray(list_cq, cq_cols.get(i));
                    if (pos>=0) {
                        row.add(list_val.get(pos).toString());
                    } else {
                        row.add("");
                    }
                }
                data.add(row);
                cfg_glb_hbRowsRead++;
                rowIntervals = HBResult.next();
            }
            
            model.setDataVector(data , cq_cols);
            
            return(model);

        } catch (Exception e) {
            System.out.println("Error general");
            return(null);
        }
    }

    public DefaultTableModel getRsQuery(HTableInterface hTable, List<String> qualifier, String StartRow, String StopRow, List<Filter> filters){
        try {
            DefaultTableModel model = new DefaultTableModel();
            Vector<Vector<Object>>  data    = new  Vector<>();
            Vector<String>          cq_cols = new Vector<>();
            List<String>            cf_cols = new ArrayList<>();
            
            //Separa las Columnas Familias de las Columnas Qualifier
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(":");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }
            
            //Genera la consulta a la base de datos
            //
            FilterList fList = new FilterList( FilterList.Operator.MUST_PASS_ALL, filters);
        
            Scan scan = new Scan();
            scan.setFilter(fList);
            scan.setCaching(10000);
            
            //Ingresa los Filtros de Columnas de Familia y Qualifiers
            //
            for (int i=0; i<cf_cols.size(); i++) {
                scan.addColumn(Bytes.toBytes(cf_cols.get(i)), Bytes.toBytes(cq_cols.get(i)));
            }               
            
            //Agrega los Start y Stop del SCAN
            scan.setStartRow(Bytes.toBytes(StartRow));
            scan.setStopRow(Bytes.toBytes(StopRow));

            ResultScanner HBResult = hTable.getScanner(scan);
            
            //Extrae la DATA del Scanner realizado
            //
            List<Object> list_cq =  new ArrayList<>();
            List<Object> list_key = new ArrayList<>();
            List<Object> list_val = new ArrayList<>();
            
            String cf;
            String cq;
            String key;
            String val;
            
            Result rowIntervals = HBResult.next();
            
            while (rowIntervals!=null) {
                
                Vector<Object> row = new Vector<>();
                list_key.clear();
                list_cq.clear();
                list_val.clear();

                for (KeyValue kv : rowIntervals.raw()) {
                    cf = new String(kv.getFamily());
                    cq = new String(kv.getQualifier());
                    key = Bytes.toStringBinary ( kv.getKey(), 2, kv.getRowLength() );
                    val = Bytes.toStringBinary( kv.getValue());
                    
                    list_key.add(key);
                    list_cq.add(cq);
                    list_val.add(val);
                }
                
                //Compara con Matriz de Columnna para Generar Registro Definitivo
                int pos=0;
                for (int i=0; i<cq_cols.size(); i++) {
                    pos = getPosInArray(list_cq, cq_cols.get(i));
                    if (pos>=0) {
                        row.add(list_val.get(pos).toString());
                    } else {
                        row.add("");
                    }
                }
                data.add(row);
                rowIntervals = HBResult.next();
            }
            
            model.setDataVector(data , cq_cols);
            
            return(model);
        
        } catch (Exception e) {
            System.out.println("Error general");
            return(null);
        }
    
    }
    
    public boolean isKeyValueExist(HTableInterface hTable, String rowKey) throws IOException {
        boolean isExist = false;
    
        try {
            Get get = new Get(rowKey.getBytes());
            Result rs = hTable.get(get);
            
            if (!rs.isEmpty()) {
                isExist = true;
            } 
            
            return(isExist);
        } catch (IOException e) {
            return(isExist);
        } catch (Exception e) {
            return(isExist);
        }
    }    
    
    public globalVARArea(String metaDataConfPath) { //Constructor Carga Datos Iniciales
        Properties srvConf = new Properties();
        cfg_glb_metaDataConfPath = metaDataConfPath;
        String hbParamConf;
  
        isGetSrvConf = false;
        try {
            //Extrae Fecha de Hoy
        
            Date today;
            String output;
            SimpleDateFormat formatter;

            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            today = new Date();
            output = formatter.format(today);
            
            cfg_srv_FecInicio = output;
            
            srvConf.load(new FileInputStream(metaDataConfPath));
            //
            // Recupera Parametros Base de Datos SrvViewer en HBAse
            cfg_glb_DBType = srvConf.getProperty("DBType");
            hbParamConf = srvConf.getProperty("HBConfVar");
            
            if (cfg_glb_DBType.equals("HBASE")) {
                cfg_glb_hbConfFile = srvConf.getProperty(hbParamConf).split(",");
                cfg_glb_hbTableName = srvConf.getProperty("HBTable");
                for (int i=0; i< cfg_glb_hbConfFile.length; i++  ) {
                    cfg_glb_hcfg.addResource(new Path(cfg_glb_hbConfFile[i]));
                }
            }
            
            //Recupera Parametros de Conexion a BD en ORA y SQL
            if (cfg_glb_DBType.equals("ORA")) {
                cfg_db_Host = srvConf.getProperty("ORA_DBHost");
                cfg_db_Name = srvConf.getProperty("ORA_DBName");
                cfg_db_User = srvConf.getProperty("ORA_DBUser");
                cfg_db_Pass = srvConf.getProperty("ORA_DBPass");
                cfg_db_Port = srvConf.getProperty("ORA_DBPort");
                cfg_db_Table = srvConf.getProperty("ORA_DBTable");

            }
            
            if (cfg_glb_DBType.equals("SQL")) {
                cfg_db_Host = srvConf.getProperty("SQL_DBHost");
                cfg_db_Name = srvConf.getProperty("SQL_DBName");
                cfg_db_User = srvConf.getProperty("SQL_DBUser");
                cfg_db_Pass = srvConf.getProperty("SQL_DBPass");
                cfg_db_Port = srvConf.getProperty("SQL_DBPort");
                cfg_db_Table = srvConf.getProperty("SQL_DBTable");

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
            cfg_srv_host_auth       = srvConf.getProperty("SRV_HOST_AUTH").split(",");
            cfg_srv_Log_Name        = srvConf.getProperty("SRV_LOG_NAME");
            
            try {
                cfg_glb_hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                cfg_glb_hostName = "NOT_FOUND";
                isGetSrvConf = false;
                //Logger.getLogger(ttModuleControl.class.getName()).log(Level.SEVERE, null, ex);
                logger.error(ex.getMessage());
            }
            
            //Activa logs
            //
            PropertyConfigurator.configure("log4j.properties");
            
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
    
    private int getPosInArray(List<Object>  lista, String valor) {
        int PosInArray = -1;
        
        for (int i=0; i<lista.size();i++) {
            if (lista.get(i).toString().equals(valor)) {
                PosInArray = i;
                break;
            }
        }
        return(PosInArray);
    }
}
