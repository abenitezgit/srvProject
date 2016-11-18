/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvabtviewer;

import abtGlobals.globalVARArea;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import mypkg.oracleDB;

/**
 *
 * @author ABT
 */
public class ttModuleControl {
    private globalVARArea  gDatos;
    private oracleDB        conn = new oracleDB();
    Timer                   timerControl;
    Timer                   timerKeep;
    Timer                   timerExec;
    int                     TxP_Control;
    int                     TxP_Keep;
    int                     TxP_Exec;
    
    public ttModuleControl(globalVARArea m) {  //Constructor
        gDatos      = m;
        timerControl= new Timer();
        
        //Informa Global Area
        gDatos.setModControlOnTimer(true);
        
        //AutoInicia Modulo Control
        System.out.println("INI: Incia Modulo control...");
        timerControl.schedule(new mainProc(), Integer.parseInt(gDatos.getCfg_srv_Txp_Control()), Integer.parseInt(gDatos.getCfg_srv_Txp_Control()) * 1000);
    }
    
    
    class mainKeep extends TimerTask {

        public void run() {
            //Informa Global Area
            gDatos.setKeepAliveRunning(true);
            
            try {  //Inicio consulta a BD
                String vSQL =   "select \n" +
                    "    S.SRV_DESC SRV_DESC,\n" +
                    "    S.SRV_ONLINE SRV_ONLINE,\n" +
                    "    S.SRV_TXP_ABT SRV_TXP_ABT,\n" +
                    "    S.SRV_TXP_PROC SRV_TXP_PROC,\n" +
                    "    D.DB_INSTANCE DB_INSTANCE,\n" +
                    "    D.DB_PORT DB_PORT,\n" +
                    "    S.SRV_DB_USER SRV_DB_USER,\n" +
                    "    S.SRV_DB_PASS SRV_DB_PASS,\n" +
                    "    S.SRV_LOG_ACTIVE SRV_LOG_ACTIVE,\n" +
                    "    S.SRV_LOG_DIR SRV_LOG_DIR,\n" +
                    "    H.HOST_NAME HOST_NAME\n" +
                    "from \n" +
                    "    process2.tma_srv_cfg s,\n" +
                    "    process2.tma_host_cfg h,\n" +
                    "    process2.tma_db_cfg d\n" +
                    "where\n" +
                    "    S.SRV_DB_ABT=D.DB_ID and\n" +
                    "    D.DB_HOST_ID=H.HOST_ID and\n" +
                    "    S.SRV_ID='"+gDatos.getCfg_srv_ID()+"' ";
                ResultSet rs = conn.consultar(vSQL);
                boolean rowFound=false;
                if (rs!=null) { //Valida si encontro registro
                    try { //Lectura del recordset
                        while (rs.next()) { //Recorre registro con Parametros
                            rowFound=true;
                            if (!gDatos.getCfg_srv_Desc().equals(rs.getString("SRV_DESC"))) {
                                gDatos.setCfg_srv_Desc(rs.getString("SRV_DESC"));}
                                //gDatos.setKeepAliveChanging(true);
                            if (!gDatos.getCfg_srv_Online().equals(rs.getString("SRV_ONLINE"))) {
                                gDatos.setCfg_srv_Online(rs.getString("SRV_ONLINE"));
                                gDatos.setExecProcChanging(true);}
                            if (!gDatos.getCfg_srv_Txp_Keep().equals(rs.getString("SRV_TXP_ABT"))) {
                                gDatos.setCfg_srv_Txp_Keep(rs.getString("SRV_TXP_ABT"));
                                gDatos.setKeepAliveChanging(true);}
                            if (!gDatos.getCfg_srv_Txp_Proc().equals(rs.getString("SRV_TXP_PROC"))) {
                                gDatos.setCfg_srv_Txp_Proc(rs.getString("SRV_TXP_PROC"));
                                gDatos.setExecProcChanging(true);}
                            if (!gDatos.getCfg_abtDBName().equals(rs.getString("DB_INSTANCE"))) {
                                gDatos.setCfg_abtDBName(rs.getString("DB_INSTANCE"));}
                            if (!gDatos.getCfg_abtDBPort().equals(rs.getString("DB_PORT"))) {
                                gDatos.setCfg_abtDBPort(rs.getString("DB_PORT"));}
                            if (!gDatos.getCfg_abtDBUser().equals(rs.getString("SRV_DB_USER"))) {
                                gDatos.setCfg_abtDBUser(rs.getString("SRV_DB_USER"));}
                            if (!gDatos.getCfg_abtDBPass().equals(rs.getString("SRV_DB_PASS"))) {
                                gDatos.setCfg_abtDBPass(rs.getString("SRV_DB_PASS"));}
                            if (!gDatos.getCfg_srv_Log_Active().equals(rs.getString("SRV_LOG_ACTIVE"))) {
                                gDatos.setCfg_srv_Log_Active(rs.getString("SRV_LOG_ACTIVE"));}
                            if (!gDatos.getCfg_srv_Log_Dir().equals(rs.getString("SRV_LOG_DIR"))) {
                                gDatos.setCfg_srv_Log_Dir(rs.getString("SRV_LOG_DIR"));}
                            if (!gDatos.getCfg_abtDBHost().equals(rs.getString("HOST_NAME"))) {
                                gDatos.setCfg_abtDBHost(rs.getString("HOST_NAME"));}
                        }
                        rs.close();
                        if (rowFound) {
                            gDatos.setIsGetAbtConfDB(true);
                            String vSQL2="select\n" +
                                        "  typ.srv_tproc,\n" +
                                        "  typ.srv_order_pri,\n" +
                                        "  typ.srv_min_thread\n" +
                                        "from \n" +
                                        "  process2.tma_srv_typeproc typ,\n" +
                                        "  process2.tma_srv_cfg srv\n" +
                                        "where\n" +
                                        "  srv.srv_id=typ.srv_id\n" +
                                        "  and typ.srv_active=1\n" +
                                        "  and srv.srv_id='SRV00001'\n" +
                                        "order by\n" +
                                        "  typ.srv_order_pri,\n" +
                                        "  typ.srv_min_thread";
                            try {
                                gDatos.setIsGetTypeProc(false);
                                ResultSet rs2 = conn.consultar(vSQL2);
                                if (rs2!=null) {
                                    boolean rowFoundProc=false;
                                    ArrayList<String> array = new ArrayList<>();
                                    while (rs2.next()) {
                                        rowFoundProc=true;
                                        array.add(rs2.getString(1)+","+rs2.getString(2)+","+rs2.getString(3));
                                    }
                                    if (rowFoundProc) {
                                        gDatos.setCfg_srv_type_proc(array);
                                        gDatos.setIsGetTypeProc(true);
                                        try {
                                            gDatos.setIsGetHostActive(false);
                                            String vSQL3="select\n" +
                                                        "  h.host_name host_name\n" +
                                                        "from\n" +
                                                        "  process2.tma_srv_authost a,\n" +
                                                        "  process2.tma_host_cfg h\n" +
                                                        "where\n" +
                                                        "  a.srv_host_id = h.host_id\n" +
                                                        "  and a.srv_active='1'\n" +
                                                        "  and a.srv_id='"+gDatos.getCfg_srv_ID()+"'";
                                            ResultSet rs3 = conn.consultar(vSQL3);
                                            if (rs3!=null) {
                                                while (rs3.next()) {
                                                    if (rs3.getString("HOST_NAME").equals(gDatos.getCfg_host_name())) {
                                                        gDatos.setIsGetHostActive(true);
                                                    }
                                                }
                                                rs3.close();
                                            } else {
                                                //No tiene HOST Autorizado para Ejecutar TH-Exec
                                            }
                                            
                                        }catch (Exception e) {
                                        
                                        }
                                    }
                                } else { 
                                    //No tiene Procesos asociados para ejecución

                                }
                                rs2.close();
                            } catch (Exception e) {
                                //Error en lectura de Procesos Asociados
                            }
                        } else { 
                            //No Encontró Servicio ID
                        }
                    } catch (Exception e) {
                        gDatos.setgErrorMessages(e.getMessage());
                        gDatos.setgErrorNumber(99);
                        gDatos.setKeepAliveChanging(false);
                        System.out.println("Error Keep: "+e.getMessage());
                    }
                }
            } catch (Exception e) {
                gDatos.setgErrorMessages(e.getMessage());
                gDatos.setgErrorNumber(99);
                gDatos.setKeepAliveChanging(false);
                System.out.println("Error Keep: "+e.getMessage());
            }
            
            
            System.out.println("    Running Keep cada "+gDatos.getCfg_srv_Txp_Keep()+" segundos...");
            
            //Informa Global Area
            gDatos.setKeepAliveRunning(false);
        }
    }
    
    class mainExec extends TimerTask {
        
        public boolean isDownMaxThread(String vTypeProc, String vMinThread) {
            boolean AcceptedThread=true;
            ArrayList<String> array = gDatos.getNumExecThread();
            try {
                if (array!=null) {
                    int rows = array.size();
                    if (rows>0) {
                        int it=0;
                        while (it<rows) {
                            StringTokenizer fields = new StringTokenizer(array.get(it), ",");
                            String lTypeProc = fields.nextToken();
                            String lPriority = fields.nextToken();
                            if (lTypeProc.equals(vTypeProc)) {
                                int lnumPriority = Integer.parseInt(lPriority);
                                int vnumPriority = Integer.parseInt(vMinThread);
                                if (lnumPriority>=vnumPriority) {
                                    AcceptedThread=false;
                                }
                            }
                            it++;
                        }
                    } else {
                        //AcceptedThread=true;
                    }
                } else {
                    //AcceptedThread=true;
                }
            return AcceptedThread;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
            
        }

        public void run() {
            //Informa Global Area
            gDatos.setExecProcRunning(true);
           
            if (gDatos.getCfg_srv_Online().equals("1")) {
                if (gDatos.isIsGetTypeProc()) {
                    ArrayList<String> array = gDatos.getCfg_srv_type_proc();
                    int rows= array.size();
                    if (rows>0) {
                        int it=0;
                        while (it<rows) {
                            StringTokenizer fields = new StringTokenizer(array.get(it), ",");
                            String vTypeProc = fields.nextToken();
                            String vPriority = fields.nextToken();
                            String vMinThread = fields.nextToken();
                            if (isDownMaxThread(vTypeProc,vMinThread)) {
                                //Hay Thread disponibles para ejecutar el proceso
                                System.out.println("Hay THread para procesos: "+vTypeProc);
                                try {
                                    System.out.println(InetAddress.getLocalHost().getHostName());
                                } catch (UnknownHostException ex) {
                                    Logger.getLogger(ttModuleControl.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                switch (vTypeProc) {
                                    case "OSP": {
                                        Thread th1 = new thExecOSP(gDatos);
                                        th1.start();
                                    }
                                }
                            } else {
                                System.out.println("No hay Thread disponibles para: "+vTypeProc);
                            }
                            it++;
                        }
                    } else {
                        System.out.println("No hay procesos asignados");
                    }
                }
            } else {
                System.out.println("    Proceso está INACTIVO");
            }
            
            System.out.println("    Running Exec cada "+gDatos.getCfg_srv_Txp_Proc()+" segundos...");
            
            //int randomNum = 1 + (int)(Math.random()*30);
            
            //System.out.println("Random: "+randomNum);
            //Actualiza Global Area
            //gDatos.setCfg_srv_Txp_Keep(Integer.toString(randomNum));
            //gDatos.setKeepAliveChanging(true);
            
            
            //Informa Global Area
            gDatos.setExecProcRunning(false);
        }
    }

    
    class mainProc extends TimerTask {

        public void connectDBAbt() {
            gDatos.setIsGetAbtConnDB(false);
            gDatos.setOraKeepConnected(false);
            try {
                conn.setDbName(gDatos.getCfg_abtDBName());
                conn.setDbUser(gDatos.getCfg_abtDBUser());
                conn.setDbPass(gDatos.getCfg_abtDBPass());
                conn.setHostIp(gDatos.getCfg_abtDBHost());
                conn.setDbPort(gDatos.getCfg_abtDBPort());
                conn.conectar();
                gDatos.setConnOraKeep(conn);
                gDatos.setIsGetAbtConnDB(true);
                gDatos.setOraKeepConnected(true);
            } catch (Exception e) {
                gDatos.setgErrorMessages(e.getMessage());
                gDatos.setgErrorNumber(99);
            }
        }
        
        public void run() {
            //Informa Global Area
            System.out.println("Running Modulo control...");
            gDatos.setModControlRunning(true);
            
            
            if (!gDatos.isOraKeepConnected()) {
                connectDBAbt();
                System.out.println("    Conecta DB...");
            } else {
                if (!gDatos.isKeepAliveOnTimer()) {
                    // Inicia un Nuevo THread
                    System.out.println("Inicio TH-Keep...");
                    timerKeep   = new Timer();
                    timerKeep.schedule(new mainKeep(), Integer.parseInt(gDatos.getCfg_srv_Txp_Keep()), Integer.parseInt(gDatos.getCfg_srv_Txp_Keep())*1000);
                
                    //Informa Global Area
                    gDatos.setKeepAliveOnTimer(true);
                    gDatos.setKeepAliveChanging(false);
                
                } else {  //Esta Schedulado
                    if (!gDatos.isKeepAliveRunning()) {
                        if (!gDatos.isKeepAliveChanging()) {
                            //Ho hace nada
                        } else { //Sus Parametros han cambiado
                            //
                            System.out.println("Cancelando TH-Keep...");
                            timerKeep.cancel();
                            
                            //Informa Global Area
                            gDatos.setKeepAliveOnTimer(false);
                        }
                    
                    } else {  //Esta en Ejecucion
                        //No hace nada
                    }
                }


                if (!gDatos.isExecProcOnTimer()) {
                    if (gDatos.getCfg_srv_Online().equals("1")) {  //Servicio puede levantar TH-Exec si está en 1
                        if (gDatos.isIsGetAbtConfDB()) {
                            if (gDatos.isIsGetTypeProc()) {
                                // Inicia un Nuevo THread
                                
                                System.out.println("Inicio TH-Exec...");
                                timerExec = new Timer();
                                timerExec.schedule(new mainExec(), Integer.parseInt(gDatos.getCfg_srv_Txp_Proc()), Integer.parseInt(gDatos.getCfg_srv_Txp_Proc())*1000);

                                //Informa Global Area
                                gDatos.setExecProcOnTimer(true);
                                gDatos.setExecProcChanging(false);
                            } else {
                                System.out.println("Aun no tiene procesos asociados");
                            }
                        } else {
                            System.out.println("Aun no trae los datos de ABT DB");
                        }
                    } else {
                        System.out.println("Inhabilitado el TH-Exec");
                    }

                } else {  //Esta Schedulado
                    if (!gDatos.isExecProcRunning()) {
                        if (!gDatos.isExecProcChanging()) {
                            //Ho hace nada
                        } else { //Sus Parametros han cambiado
                            //
                            System.out.println("Cancelando TH-Exec...");
                            timerExec.cancel();

                            //Informa Global Area
                            gDatos.setExecProcOnTimer(false);
                        }

                    } else {  //Esta en Ejecucion
                        //No hace nada
                    }
                }
            }     
        //Informa Global Area
        System.out.println("Finaliza Modulo control...");
        gDatos.setModControlRunning(false);
    }
}
}