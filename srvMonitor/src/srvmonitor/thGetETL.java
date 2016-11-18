/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;

import dataClass.ETL;
import dataClass.EtlMatch;
import dataClass.Interval;
import dataClass.PoolProcess;
import dataClass.ServiceStatus;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thGetETL extends Thread{
    globalAreaData gDatos;
    srvRutinas gSub;
    static Logger logger = Logger.getLogger("thGetETL");
    MetaData conn;
    
    public thGetETL(globalAreaData m) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
        try{
            conn = new MetaData(gDatos);
        } catch (Exception e) {
            logger.error("Error conectando a Metadata..."+e.getMessage());
        }
    }
    
    @Override
    public void run() {
        logger.info("Iniciando Ciclo Ejecución Thread ETL...");
        
        ETL etl;
        EtlMatch etlMatch;
        List<EtlMatch> lstEtlMatch;
        Interval interval;
        
        //INITCIO ETAPA 1
        //Buscando programaciones de ETL con sus respexitvos Match de Campos
        //
        logger.info("Buscando programaciones de ETL en MetaData.");
        
        String vSQL =   "select  cfg.ETLID ETLID, cfg.ETLDESC ETLDESC, cfg.ETLENABLE ETLENABLE, cli.CLIDESC CLIDESC,\n" +
                        "        cfg.ETLINTERVALFIELDKEY FIELDKEY, cfg.ETLINTERVALFIELDKEYTYPE FIELDTYPE, cfg.ETLINTERVALTIMEGAP TIMEGAP, cfg.ETLINTERVALTIMEGENINTERVAL TIMEGEN,\n" +
                        "        cfg.ETLINTERVALTIMEPERIOD TIMEPERIOD, cfg.ETLINTERVALUNITMEASURE UNITMEASURE, cfg.ETLQUERYWHEREACTIVE WHEREACTIVE, cfg.ETLQUERYBODY QUERYBODY,\n" +
                        "        cfg.ETLSOURCETBNAME STBNAME,  cfg.ETLDESTTBNAME DTBNAME, cfg.ETLLASTNUMSECEXEC LASTNUMSECEXEC,\n" +
                        "        srv.SERVERIP SIP, \n" +
                        "        db.DBDESC SDBDESC, db.DBNAME SDBNAME, db.DBTYPE SDBTYPE, db.DBPORT SDBPORT, db.DBINSTANCE SDBINSTANCE, db.DBFILECONF SDBCONF, db.DBJDBCSTRING SDBJDBC,\n" +
                        "        usr.USERNAME SUSERNAME, usr.USERPASS SUSERPASS, usr.USERTYPE SUSERTYPE,\n" +
                        "        srvD.SERVERIP DIP,\n" +
                        "        dbD.DBDESC DDBDESC, dbD.DBNAME DDBNAME, dbD.DBTYPE DDBTYPE, dbD.DBPORT DDBPORT, dbD.DBINSTANCE DDBINSTANCE, dbD.DBFILECONF DDBCONF, dbD.DBJDBCSTRING DDBJDBC,\n" +
                        "        usrD.USERNAME DUSERNAME, usrD.USERPASS DUSERPASS, usrD.USERTYPE DUSERTYPE\n" +
                        "from\n" +
                        "  tb_etlConf cfg,\n" +
                        "  tb_servidor srv,\n" +
                        "  tb_basedatos db,\n" +
                        "  tb_cliente cli,\n" +
                        "  TB_USERS usr,\n" +
                        "  TB_SERVIDOR srvD,\n" +
                        "  TB_BASEDATOS dbD,\n" +
                        "  TB_USERS usrD\n" +
                        "where\n" +
                        "  cfg.ETLCLIID = cli.CLIID\n" +
                        "  And cfg.ETLSourceServerID = srv.SERVERID\n" +
                        "  And cfg.ETLSourceDBID = db.DBID\n" +
                        "  And cfg.ETLSOURCEUSERID = usr.USERID\n" +
                        "  And cfg.ETLDESTSERVERID = srvD.SERVERID\n" +
                        "  And cfg.ETLDESTDBID = dbD.DBID\n" +
                        "  And cfg.ETLDESTUSERID = usrD.USERID\n" +
                        "order by\n" +
                        "  ETLID";
        
        if (conn.isConnected()) {
            //Ejecuta Query de Consulta
            //
            logger.debug("Ejecutando Query: "+vSQL);
            
            ResultSet rs = (ResultSet) conn.getQuery(vSQL);
            if (rs!=null) {
                etl = new ETL();
                try {
                    logger.info("Recorriendo Resulset de Respuesta...");
                    
                    int numRecords=0;
                    
                    while (rs.next()) {
                        numRecords++;
                        
                        if (rs.getString("ETLID")!=null) {
                            etl.setETLID(rs.getString("ETLID")); 
                        }
                        if (rs.getString("ETLDESC")!=null) {
                            etl.setETLDesc(rs.getString("ETLDESC")); 
                        }
                        if (rs.getString("ETLENABLE")!=null) {
                            etl.setETLEnable(rs.getInt("ETLENABLE")); 
                        }
                        if (rs.getString("CLIDESC")!=null) {
                            etl.setCliDesc(rs.getString("CLIDESC")); 
                        }
                        if (rs.getString("FIELDKEY")!=null) {
                            etl.setFIELDKEY(rs.getString("FIELDKEY")); 
                        }
                        if (rs.getString("FIELDTYPE")!=null) {
                            etl.setFIELDTYPE(rs.getString("FIELDTYPE")); 
                        }
                        if (rs.getString("TIMEGAP")!=null) {
                            etl.setTIMEGAP(rs.getInt("TIMEGAP")); 
                        }
                        if (rs.getString("TIMEGEN")!=null) {
                            etl.setTIMEGEN(rs.getInt("TIMEGEN")); 
                        }
                        if (rs.getString("TIMEPERIOD")!=null) {
                            etl.setTIMEPERIOD(rs.getInt("TIMEPERIOD")); 
                        }
                        if (rs.getString("UNITMEASURE")!=null) {
                            etl.setUNITMEASURE(rs.getString("UNITMEASURE")); 
                        }
                        if (rs.getString("WHEREACTIVE")!=null) {
                            etl.setWHEREACTIVE(rs.getInt("WHEREACTIVE")); 
                        }
                        if (rs.getString("QUERYBODY")!=null) {
                            etl.setQUERYBODY(rs.getString("QUERYBODY")); 
                        }
                        if (rs.getString("STBNAME")!=null) {
                            etl.setSTBNAME(rs.getString("STBNAME")); 
                        }
                        if (rs.getString("DTBNAME")!=null) {
                            etl.setDTBNAME(rs.getString("DTBNAME")); 
                        }
                        if (rs.getString("SIP")!=null) {
                            etl.setSIP(rs.getString("SIP")); 
                        }
                        if (rs.getString("SDBNAME")!=null) {
                            etl.setSDBNAME(rs.getString("SDBNAME")); 
                        }
                        if (rs.getString("SDBDESC")!=null) {
                            etl.setSDBDESC(rs.getString("SDBDESC")); 
                        }
                        if (rs.getString("SDBTYPE")!=null) {
                            etl.setSDBTYPE(rs.getString("SDBTYPE")); 
                        }
                        if (rs.getString("SDBPORT")!=null) {
                            etl.setSDBPORT(rs.getString("SDBPORT")); 
                        }
                        if (rs.getString("SDBINSTANCE")!=null) {
                            etl.setSDBINSTANCE(rs.getString("SDBINSTANCE")); 
                        }
                        if (rs.getString("SDBCONF")!=null) {
                            etl.setSDBCONF(rs.getString("SDBCONF")); 
                        }
                        if (rs.getString("SDBJDBC")!=null) {
                            etl.setSDBJDBC(rs.getString("SDBJDBC")); 
                        }
                        if (rs.getString("SUSERNAME")!=null) {
                            etl.setSUSERNAME(rs.getString("SUSERNAME")); 
                        }
                        if (rs.getString("SUSERPASS")!=null) {
                            etl.setSUSERPASS(rs.getString("SUSERPASS")); 
                        }
                        if (rs.getString("SUSERTYPE")!=null) {
                            etl.setSUSERTYPE(rs.getString("SUSERTYPE")); 
                        }
                        if (rs.getString("DIP")!=null) {
                            etl.setDIP(rs.getString("DIP")); 
                        }
                        if (rs.getString("DDBDESC")!=null) {
                            etl.setDDBDESC(rs.getString("DDBDESC")); 
                        }
                        if (rs.getString("SDBNAME")!=null) {
                            etl.setDDBNAME(rs.getString("SDBNAME")); 
                        }
                        if (rs.getString("DDBTYPE")!=null) {
                            etl.setDDBTYPE(rs.getString("DDBTYPE")); 
                        }
                        if (rs.getString("DDBPORT")!=null) {
                            etl.setDDBPORT(rs.getString("DDBPORT")); 
                        }
                        if (rs.getString("DDBINSTANCE")!=null) {
                            etl.setDDBINSTANCE(rs.getString("DDBINSTANCE")); 
                        }
                        if (rs.getString("DDBCONF")!=null) {
                            etl.setDDBCONF(rs.getString("DDBCONF")); 
                        }
                        if (rs.getString("DDBJDBC")!=null) {
                            etl.setDDBJDBC(rs.getString("DDBJDBC")); 
                        }
                        if (rs.getString("DUSERNAME")!=null) {
                            etl.setDUSERNAME(rs.getString("DUSERNAME")); 
                        }
                        if (rs.getString("DUSERPASS")!=null) {
                            etl.setDUSERPASS(rs.getString("DUSERPASS")); 
                        }
                        if (rs.getString("DUSERTYPE")!=null) {
                            etl.setDUSERTYPE(rs.getString("DUSERTYPE")); 
                        }
                        if (rs.getString("LASTNUMSECEXEC")!=null) {
                            etl.setLASTNUMSECEXEC(rs.getString("LASTNUMSECEXEC")); 
                        }
                        
                        logger.debug("Se crea objeto ETL: "+gSub.serializeObjectToJSon(etl, false));
                        
                        //Recupera detalle de Match de Campos para este ETL
                        String vSQL2 =  "select \n" +
                                        "  ETLORDER, ETLSOURCEFIELD, ETLSOURCELENGTH, ETLSOURCETYPE,\n" +
                                        "  ETLDESTFIELD, ETLDESTLENGTH, ETLDESTTYPE\n" +
                                        "from \n" +
                                        "  tb_etlMatch\n" +
                                        "where\n" +
                                        "  ETLID='"+ etl.getETLID()  +"'\n" +
                                        "  And ETLENABLE=1 order by ETLORDER";
                        try {
                            logger.info("Buscando match de campos para ETL: "+etl.getETLID());
                            logger.debug("Eejcutando Query de busqueda: "+vSQL2);
                            
                            ResultSet rs2 = (ResultSet) conn.getQuery(vSQL2);
                            if (rs2!=null) {
                                lstEtlMatch = new ArrayList<>();
                                int numRecordMatch=0;
                                
                                logger.info("Recorriendo resultset de respuesta...");
                                
                                while (rs2.next()) {
                                    numRecordMatch++;
                                    etlMatch = new EtlMatch();
                                    if (rs2.getString("ETLORDER")!=null) {
                                        etlMatch.setEtlOrder(rs2.getInt("ETLORDER"));
                                    }
                                    if (rs2.getString("ETLSOURCEFIELD")!=null) {
                                        etlMatch.setEtlSourceField(rs2.getString("ETLSOURCEFIELD"));
                                    }
                                    if (rs2.getString("ETLSOURCELENGTH")!=null) {
                                        etlMatch.setEtlSourceLength(rs2.getInt("ETLSOURCELENGTH"));
                                    }
                                    if (rs2.getString("ETLSOURCETYPE")!=null) {
                                        etlMatch.setEtlSourceType(rs2.getString("ETLSOURCETYPE"));
                                    }
                                    if (rs2.getString("ETLDESTFIELD")!=null) {
                                        etlMatch.setEtlDestField(rs2.getString("ETLDESTFIELD"));
                                    }
                                    if (rs2.getString("ETLDESTLENGTH")!=null) {
                                        etlMatch.setEtlDestLength(rs2.getInt("ETLDESTLENGTH"));
                                    }
                                    if (rs2.getString("ETLDESTTYPE")!=null) {
                                        etlMatch.setEtlDestType(rs2.getString("ETLDESTTYPE"));
                                    }
                                    etl.getLstEtlMatch().add(etlMatch);
                                }
                                rs2.close();
                            } else {
                                logger.info("No hay match de campos para esta configuracion de ETL: "+etl.getETLID());
                            }
                        } catch (Exception e) {
                            logger.error("Error Ejecutando extraccion de Match de campos para ETL: "+etl.getETLID());
                        }
                        //Agraga a la lista de ETL solo las configuraciones que tienen match asignado
                        if (!etl.getLstEtlMatch().isEmpty()) {
                            logger.info("Actualizando lista lstETLConf con ETL: "+etl.getETLID());
                            gDatos.updateLstEtlConf(etl);
                        } else {
                            logger.error("No estan definidos los match de campos para ETL: "+etl.getETLID());
                        }
                    }
                    rs.close();
                    logger.info("Se econtraron: "+numRecords+ "configuraciones de ETL");
                    
                    logger.debug("Detalle List ETL: "+gSub.serializeObjectToJSon(gDatos.getLstETLConf(), true));
                    
                } catch (SQLException | IOException ex) {
                    logger.error("Error recorriendo recorset Query..."+ex.getMessage());
                }
            } else {
                logger.info("No se recuperaron datos de ETL");
            }
        } else {
            logger.error("Error no se pudo conectar a Metadata");
        }
        
        //FINALIZA ETAPA 1 (Busqueda de Configuraciones de ETL con sus Match de Campos)
        
        

        
        //INICIO ETAPA 2
        //Recuperando desde BD Intervalos Pendientes
        //
        logger.info("Buscando Intervalos Pendientes en MetaData...");
        
        vSQL =  "select\n" +
                "  ETLID, INTERVALID, FECINS, FECUPDATE, STATUS, USTATUS, NUMEXEC\n" +
                "from\n" +
                "  tb_etlinterval\n" +
                "where\n" +
                "  status='FinishedOLD'\n" +
                "order by\n" +
                "  ETLID,\n" +
                "  INTERVALID";
        try {
            logger.debug("Ejecutando query de consulta: "+vSQL);
            
            ResultSet rs = (ResultSet) conn.getQuery(vSQL);
            if (rs!=null) {
                logger.debug("Recorriendo resultset de respuesta...");
                
                int numIntervalsBD=0;
                
                while (rs.next()) {
                    interval = new Interval();
                    numIntervalsBD++;
                    if (rs.getString("ETLID")!=null) {
                        interval.setETLID(rs.getString("ETLID"));
                    }
                    if (rs.getString("INTERVALID")!=null) {
                        interval.setIntervalID(rs.getString("INTERVALID"));
                    }
                    if (rs.getString("FECINS")!=null) {
                        interval.setFechaIns(rs.getString("FECINS"));
                    }
                    if (rs.getString("FECUPDATE")!=null) {
                        interval.setFechaUpdate(rs.getString("FECUPDATE"));
                    }
                    if (rs.getString("STATUS")!=null) {
                        interval.setStatus(rs.getString("STATUS"));
                    }
                    if (rs.getString("NUMEXEC")!=null) {
                        interval.setNumExec(rs.getInt("NUMEXEC"));
                    }
                    gDatos.updateLstInterval(interval);
                }
                rs.close();
                logger.info("Se recuperaron: "+numIntervalsBD+ " intervalos Pendientes desde Metadata");
                
            } else {
                logger.debug("No hay intervalos pendientes en BD");
            }
        } catch (Exception e) {
            logger.error("Error recuperando Intevalos desde BD..."+ e.getMessage());
        }
        
        
//        try {
//            logger.debug("Detalle List interval: "+ gSub.serializeObjectToJSon(gDatos.getLstInterval().get(0), true));
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(thGetETL.class.getName()).log(Level.SEVERE, null, ex);
//        }
        


        //INICIO ETAPA 3
        //Actualliza los Intervalos Faltantes para cada configuracion de ETL recuperada
        //
        logger.info("Generando Intervalos Faltantes de Inscripcion...");
        
        
        //Extre Fecha Actual
        Date today;
        Date fecGap;
        Date fecIni;
        Date fecItera;
        Date fecIntervalIni;
        Date fecIntervalFin;

        int MinItera;
        int HoraItera;
        int DiaItera;
        int MesItera;
        int AnoItera;

        long numInterval;
        String localIntervalID;
        String todayChar;


        //Setea Fecha Actual
        //
        today = new Date();
        
        //recorre lista de ETL para 
        
        int numEtlConf = gDatos.getLstETLConf().size();
        for (int it=0; it<numEtlConf; it++) {
            //Variables del Objeto ETL
            //
            int vTimeGap = gDatos.getLstETLConf().get(it).getTIMEGAP();
            int vTimePeriod = gDatos.getLstETLConf().get(it).getTIMEPERIOD();
            int vTimeGen = gDatos.getLstETLConf().get(it).getTIMEGEN();
            String vUnitMeasure = gDatos.getLstETLConf().get(it).getUNITMEASURE();
            String vETLID = gDatos.getLstETLConf().get(it).getETLID();
            
            //Setea Fecha GAP - Desface de tiempo en extraccion
            //
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MINUTE, -(vTimeGap+vTimePeriod));
            fecGap = c.getTime();

            //Setea Fecha Inicio Inscripcion/Revision de Intervalos
            //

            c.setTime(today);
            c.add(Calendar.MINUTE, -vTimeGen);
            fecIni = c.getTime();

            logger.debug("Datos del ETLID: "+vETLID);
            logger.debug("Fecha Actual: "+ today);
            logger.debug("Fecha GAP   : "+ fecGap);
            logger.debug("Fecha IniIns: "+ fecIni);
        
            fecItera = fecIni;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdfToday = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String IntervalIni;
            String IntervalFin;
            List<String> qualifier = new ArrayList<>();
        
            while (fecItera.compareTo(fecGap) < 0) {
                //Crea Objecto Interval
                //
                interval = new Interval();

                //Extrae Intervalo para Fecha fecItera
                //
                c.setTime(fecItera);
                AnoItera = c.get(Calendar.YEAR);
                MesItera = c.get(Calendar.MONTH);
                DiaItera = c.get(Calendar.DAY_OF_MONTH);
                HoraItera = c.get(Calendar.HOUR_OF_DAY);
                MinItera = c.get(Calendar.MINUTE);

                //Valida si el intervalo de extraccion (cETL_INTERVALUNIDAD) es por:
                //  Minutos     : 0
                //  Horas       : 1
                //  Dias        : 2
                //  Semanas     : 3
                //  Mensuales   : 4
                //  Anuales     : 5

                switch (vUnitMeasure) {
                    case "MINUTE":
                        fecIntervalIni = null;
                        fecIntervalFin = null;
                        numInterval = 60/vTimePeriod;
                        for (int i=1;i<=numInterval;i++) {
                            c.set(AnoItera, MesItera, DiaItera, HoraItera, (i)*vTimePeriod,0);
                            fecIntervalFin = c.getTime();
                            if (fecIntervalFin.compareTo(fecItera) >0 ) {
                                c.set(AnoItera, MesItera, DiaItera, HoraItera, (i-1)*vTimePeriod,0);
                                fecIntervalIni = c.getTime();
                                break;
                            }
                        }
                        c.setTime(fecItera);
                        c.add(Calendar.MINUTE, vTimePeriod);
                        fecItera = c.getTime();


                        IntervalIni = sdf.format(fecIntervalIni);
                        IntervalFin = sdf.format(fecIntervalFin);
                        localIntervalID = IntervalIni+'-'+IntervalFin;

                        logger.info("Datos de Inicio de Extraccion de Intervalos...");
                        logger.info("Fecha inicio intervalo: "+ fecIntervalIni);
                        logger.info("Fecha fin Intervalo: "+fecIntervalFin);
                        logger.info("IntervalID generado: "+localIntervalID);

                        interval.setETLID(vETLID);
                        interval.setIntervalID(localIntervalID);
                        interval.setStatus("initialised");
                        interval.setNumExec(0);
                        interval.setFechaIns(sdfToday.format(today));
                        interval.setFechaUpdate(sdfToday.format(today));
                        interval.setEtlConf(gDatos.getLstETLConf().get(it));

                        logger.info("Actualizando lista de Intervalos...");
                        gDatos.updateLstInterval(interval);

                        break;

                    case "HOUR":
                        fecIntervalIni = null;
                        fecIntervalFin = null;
                        numInterval = 24/vTimePeriod;
                        for (int i=1;i<=numInterval;i++) {
                            c.set(AnoItera, MesItera, DiaItera, (i)*vTimePeriod, 0, 0);
                            fecIntervalFin = c.getTime();
                            if (fecIntervalFin.compareTo(fecItera) >0 ) {
                                c.set(AnoItera, MesItera, DiaItera, (i-1)*vTimePeriod, 0, 0);
                                fecIntervalIni = c.getTime();
                                break;
                            }
                        }
                        c.setTime(fecItera);
                        c.add(Calendar.HOUR_OF_DAY, vTimePeriod);
                        fecItera = c.getTime();

                        IntervalIni = sdf.format(fecIntervalIni);
                        IntervalFin = sdf.format(fecIntervalFin);
                        localIntervalID = IntervalIni+'-'+IntervalFin;                    


                        logger.info("Datos de Inicio de Extraccion de Intervalos...");
                        logger.info("Fecha inicio intervalo: "+ fecIntervalIni);
                        logger.info("Fecha fin Intervalo: "+fecIntervalFin);
                        logger.info("IntervalID generado: "+localIntervalID);

                        interval.setETLID(vETLID);
                        interval.setIntervalID(localIntervalID);
                        interval.setStatus("initialised");
                        interval.setNumExec(0);
                        interval.setFechaIns(sdfToday.format(today));
                        interval.setFechaUpdate(sdfToday.format(today));
                        interval.setEtlConf(gDatos.getLstETLConf().get(it));

                        logger.info("Actualizando lista de Intervalos...");
                        gDatos.updateLstInterval(interval);                    

                        break;

                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    default:
                }
            }
        }
        
        //Asigna Intervalos a los Pool de Procesos de Cada Servicio Activo en base a su disponibilidad
        //y carga asignada.
        
        logger.info("Asignando Intervalos a Servicios Incritos...");
        
        List<Interval> lstInitialInterval = gDatos.getLstInterval().stream().filter(p -> p.getStatus().equals("initialised")).collect(Collectors.toList());
        
        int numIntervalsInitial = lstInitialInterval.size();
        
        logger.debug("Total Intervalos Initial: "+numIntervalsInitial);
        
        if (numIntervalsInitial>0) {
            List<ServiceStatus> lstFindServiceStatus = gDatos.getLstServiceStatus().stream().filter(p -> p.isSrvActive()&&p.getNumThreadActives()<p.getNumProcMax()).collect(Collectors.toList());
            int numFindServices = lstFindServiceStatus.size();
            if (numFindServices>0) {
                logger.debug("Servicios disponibles para ejecutar procesos: "+numFindServices);
                
                List<ServiceStatus> lstOKServiceStatus = new ArrayList<>();
                
                //Busca Servicios inscritos que tengan disponibilidad de thread a nivel de tipo de procesos
                //para ejecutar intervalos ETL
                //
                for (int i=0; i<numFindServices; i++) {
                    int numAssignedTypeProc;
                    try {
                        numAssignedTypeProc = gDatos.getLstServiceStatus().get(i).getLstAssignedTypeProc().stream().filter(p -> p.getTypeProc().equals("ETL")).collect(Collectors.toList()).get(0).getMaxThread();
                    } catch (Exception e) {
                        numAssignedTypeProc=0;
                    }
                    int numUsedTypeProc;
                    try {
                        numUsedTypeProc = gDatos.getLstServiceStatus().get(i).getLstActiveTypeProc().stream().filter(p -> p.getTypeProc().equals("ETL")).collect(Collectors.toList()).get(0).getUsedThread();
                    } catch (Exception e) {
                        numUsedTypeProc=0;
                    }
                    
                    if (numUsedTypeProc<numAssignedTypeProc) {
                        lstOKServiceStatus.add(gDatos.getLstServiceStatus().get(i));
                    }
                }
                
                for (int i=0; i<lstOKServiceStatus.size(); i++) {
                    logger.info("Servicio Activo: "+lstOKServiceStatus.get(i).getSrvID());
                }
                
                int numOKServices = lstOKServiceStatus.size();
                if (numOKServices > 0) {
                    //Asigna Intervalos a Servicios disponibles
                    //(se debe evaluar bien la lógica de asignación de procesos)
                    //
                    
                    //Para cada intervalo Sleeping se le asignará un Servicio disponible
                    
                    PoolProcess pool;
                    int nextIndexServiceAssigned = 0;
                    for (int i=0; i<numIntervalsInitial; i++) {
                        
                        //Instancia Objecto pool
                        //
                        pool = new PoolProcess();
                        
                        //Asigna Servicio RoundRobin
                        //
                        if (nextIndexServiceAssigned<numOKServices) {
                            pool.setSrvID(lstOKServiceStatus.get(nextIndexServiceAssigned).getSrvID());
                            nextIndexServiceAssigned++;
                        } else {
                            nextIndexServiceAssigned=0;
                            pool.setSrvID(lstOKServiceStatus.get(nextIndexServiceAssigned).getSrvID());
                        }                      
                        
                        pool.setTypeProc("ETL");
                        pool.setProcID(lstInitialInterval.get(i).getETLID());
                        pool.setIntervalID(lstInitialInterval.get(i).getIntervalID());
                        pool.setInsTime(gSub.getDateNow("yyyy-MM-dd HH:mm:ss"));
                        pool.setUpdateTime(gSub.getDateNow("yyyy-MM-dd HH:mm:ss"));
                        pool.setStatus("Assigned");
                        
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("etlConf", lstInitialInterval.get(i).getEtlConf());
                        
                        pool.setParams(params);
                        
                        logger.info("Asignado Interval: "+pool.getIntervalID() + " a Servicio: "+pool.getSrvID());
                        
                        try {
                            logger.debug("pool: "+gSub.serializeObjectToJSon(pool, true));
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(thGetETL.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        gDatos.inscribePoolProcess(pool);
                    }
                    
                
                } else {
                    logger.info("No hay Servicios disponibles para ejecutar Intervalos de ETL");
                }
                
            } else {
                logger.warn("No hay Servicios Activos o Disponibles para Inscribir: "+numIntervalsInitial+" Intervalos");
            }
        } else {
            logger.info("No hay Intervalos a Procesar");
        }
        
        logger.info("Se completa la asignacion de Intervalos. Total en pool: "+gDatos.getLstPoolProcess().size());

        try {
            for (int i=0; i<gDatos.getLstPoolProcess().size(); i++) {
                PoolProcess myPool = new PoolProcess();
                myPool = gDatos.getLstPoolProcess().get(i);
                
                /*
                myPool.setSrvID(gDatos.getLstPoolProcess().get(i).getSrvID());
                myPool.setEndTime(gDatos.getLstPoolProcess().get(i).getEndTime());
                myPool.setErrMesg(gDatos.getLstPoolProcess().get(i).getErrMesg());
                myPool.setErrNum(gDatos.getLstPoolProcess().get(i).getErrNum());
                myPool.setInsTime(gDatos.getLstPoolProcess().get(i).getInsTime());
                myPool.setIntervalID(gDatos.getLstPoolProcess().get(i).getIntervalID());
                myPool.setProcID(gDatos.getLstPoolProcess().get(i).getProcID());
                myPool.setStartTime(gDatos.getLstPoolProcess().get(i).getStartTime());
                myPool.setStatus(gDatos.getLstPoolProcess().get(i).getStatus());
                myPool.setTypeProc(gDatos.getLstPoolProcess().get(i).getTypeProc());
                myPool.setUpdateTime(gDatos.getLstPoolProcess().get(i).getUpdateTime());
                myPool.setuStatus(gDatos.getLstPoolProcess().get(i).getuStatus());
                */
                
                logger.debug("Detalle Lista poolProcess: "+gSub.serializeObjectToJSon(myPool, true));
            }
            
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(thGetETL.class.getName()).log(Level.SEVERE, null, ex);
        }
   
        logger.info("Terminando Ciclo Ejecución Thread ETL");
    }
}
