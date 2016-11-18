/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;

import dataClass.Agenda;
import dataClass.ETL;
import dataClass.EtlMatch;
import dataClass.Ftp;
import dataClass.Grupo;
import dataClass.Process;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thActiveGroups extends Thread{
    static srvRutinas gSub;
    static globalAreaData gDatos;
    MetaData metadata;
    
//Carga Clase log4
    static Logger logger = Logger.getLogger("thActiveGroups");   
    
    public thActiveGroups(globalAreaData m) {
        try {
            gDatos = m;
            gSub = new srvRutinas(gDatos);
            metadata = new MetaData(gDatos);
        } catch (Exception e) {
            logger.error("Error en Constructor: "+e.getMessage());
        }
    }
    
    @Override
    public void run() {
        /*
            Recupera Parametros Fecha Actual
        */
        logger.info("Buscando Agendas Activas");

        
        //Setea Calendario en Base al TimeZone
        //
        String[] ids = TimeZone.getAvailableIDs(-4 * 60 * 60 * 1000);
        String clt = ids[0];
        SimpleTimeZone tz = new SimpleTimeZone(-3 * 60 * 60 * 1000, clt);
        tz.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        tz.setEndRule(Calendar.AUGUST, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        Calendar calendar = new GregorianCalendar(tz);

        int year       = calendar.get(Calendar.YEAR);
        int month      = calendar.get(Calendar.MONTH); // Jan = 0, dec = 11
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); 
        int dayOfWeek  = calendar.get(Calendar.DAY_OF_WEEK);
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int weekOfMonth= calendar.get(Calendar.WEEK_OF_MONTH);

        int findHour    = gDatos.getServerInfo().getAgeShowHour();  //Cantidad de Horas definidas para la muestra de las agendas
        int findMinutes = gDatos.getServerInfo().getAgeGapMinute(); //GAP en minutos para encontrar agendas que deberían haberse activado

        //Genera las variables de Posicion a comparar con las guardadas en la base de datos
        
        //Ajusta Semana del Mes para que no retorne una semana 5
        if (weekOfMonth==5) {
            weekOfMonth = 4;
        }
        
        String posmonth = String.valueOf(month+1);
        String posdayOfMonth = String.valueOf(dayOfMonth);
        String posdayOfWeek = String.valueOf(dayOfWeek);
        String posweekOfYear = String.valueOf(weekOfYear);
        String posweekOfMonth = String.valueOf(weekOfMonth);
        
        
        Calendar iteratorCalendar;
        String vSQL;
        String iteratorHour;
        String iteratorMinute;
        String posIteratorHour;
        String posIteratorMinute;
        String numSecExec = "0";
        
        
        /*
        Inicializa Lista de Agendas
        */
        gDatos.getLstShowAgendas().clear();     //Lista para el muestreo de agendas
        gDatos.getLstActiveAgendas().clear();   //Lista para las agendas que deben activar grupos de procesos
        
        //Data Class
        Agenda agenda;  //La clase de datos para agenda
        
        
        /**
         * Busca Todas las agenda en un rango de findHour (12) horas antes y después de la hora actual
         */
        logger.info("Buscando Agendas para Monitoreo...");
        
        for (int i=-findHour; i<=findHour; i++) {
            iteratorCalendar = new GregorianCalendar(tz);
            
            //Posiciona el iteratorCalendar tantas horas atrás como definido en findHour
            //Y extrae la hora correspondiente
            //
            iteratorCalendar.add(Calendar.HOUR_OF_DAY, i);
            iteratorHour = String.valueOf(iteratorCalendar.get(Calendar.HOUR_OF_DAY));
            posIteratorHour = String.valueOf(Integer.valueOf(iteratorHour)+1);
            
            vSQL = "select "+iteratorHour+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from tb_diary where "
                    + "     ageEnable=1 "
                    + "     and substr(month,"+posmonth+",1) = '1'"
                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'";
            try {
                try (ResultSet rs = (ResultSet) metadata.getQuery(vSQL)) {
                    if (rs!=null) {
                        while (rs.next()) {
                            agenda = new Agenda();
                            agenda.setHoraAgenda(rs.getString("horaAgenda"));
                            agenda.setAgeID(rs.getString("ageID"));
                            agenda.setMonth(rs.getString("month"));
                            agenda.setDayOfMonth(rs.getString("dayOfMonth"));
                            agenda.setWeekOfYear(rs.getString("weekOfYear"));
                            agenda.setWeekOfMonth(rs.getString("weekOfMonth"));
                            agenda.setHourOfDay(rs.getString("hourOfDay"));
                            gDatos.getLstShowAgendas().add(agenda);
                        }
                        rs.close();
                    } else {
                        //Si para la hora buscada no encuentra agenda genera un objeto vacio
                        //solo con el valor de la hora correspondiente
                        //esto es para poder mostrar que en esa hora no hay agenda
                        agenda = new Agenda();
                        agenda.setHoraAgenda(iteratorHour);
                        agenda.setAgeID("");
                        agenda.setMonth("");
                        agenda.setDayOfMonth("");
                        agenda.setWeekOfYear("");
                        agenda.setWeekOfMonth("");
                        agenda.setHourOfDay("");
                        gDatos.getLstShowAgendas().add(agenda);
                    }
                }            
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }
        
        logger.info("Se encontraron: "+gDatos.getLstShowAgendas().size()+ " Agendas para Monitoreo..");
        
        
        /**
         * Busca Todas las agendas que deberían ser activadas en el periodo de findMinutes (5) minutos de holgura
         */
        logger.info("Buscando Agendas Activas...");

        //Vuelve a inicializar las variables para realizar la busqueda de agendas que activaran grupos
        //
        iteratorCalendar = new GregorianCalendar(tz);
        iteratorHour = String.valueOf(iteratorCalendar.get(Calendar.HOUR_OF_DAY));
        posIteratorHour = String.valueOf(Integer.valueOf(iteratorHour)+1);
        
        logger.debug("iteratorHour: "+iteratorHour);
        logger.debug("posIteratorHour: "+posIteratorHour);
        
        for (int i=-findMinutes; i<=0; i++) {
            iteratorCalendar = new GregorianCalendar(tz);
            iteratorCalendar.add(Calendar.MINUTE, i);
            iteratorMinute = String.valueOf(iteratorCalendar.get(Calendar.MINUTE));
            posIteratorMinute = String.valueOf(Integer.valueOf(iteratorMinute)+1);
            
            //Para cada agenda que se encuentra en el periodo buscado se le asignara un numero unico de secuencia de ejecucion
            //Esto para no confundir las mismas agendas en periodos de ejecución buscados en el gap
            //Este numero de ejecucuion identificara a cada procesos y grupo respectivo
            //
            numSecExec = String.format("%04d", year)+String.format("%02d", month+1)+String.format("%02d", dayOfMonth)+String.format("%02d", Integer.valueOf(iteratorHour))+String.format("%02d", Integer.valueOf(iteratorMinute));
            
            /**
             * Busca Agendas correspondientes al minuto exacto del iterador del ciclo for
             */
            vSQL = "select "+iteratorMinute+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from tb_diary where "
                    + "     ageEnable=1 "
                    + "     and substr(month,"+posmonth+",1) = '1'"
                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'"
                    + "     and substr(minute,"+posIteratorMinute +",1) = '1'";
            try {
            	logger.debug("Query Agenda: "+vSQL);
                try (ResultSet rs = (ResultSet) metadata.getQuery(vSQL)) {
                    if (rs!=null) {
                        while (rs.next()) {
                            agenda = new Agenda();
                            agenda.setHoraAgenda(rs.getString("horaAgenda"));
                            agenda.setAgeID(rs.getString("ageID"));
                            agenda.setMonth(rs.getString("month"));
                            agenda.setDayOfMonth(rs.getString("dayOfMonth"));
                            agenda.setWeekOfYear(rs.getString("weekOfYear"));
                            agenda.setWeekOfMonth(rs.getString("weekOfMonth"));
                            agenda.setHourOfDay(rs.getString("hourOfDay"));
                            agenda.setNumSecExec(numSecExec);
                            gDatos.updateLstActiveAgendas(agenda);
                        }
                        rs.close();
                    }
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }
        
        logger.info("Se han encontrado: "+gDatos.getLstActiveAgendas().size()+" Potenciales Agendas para activar Grupos.");
        
        /**
         * Busca para todas las agendas activas los grupos y procesos asignados
         */
        
        logger.info("Buscando Grupos de Procesos asociados a las agendas activas.");
        
        int numAgeActives = gDatos.getLstActiveAgendas().size();
        Grupo grupo = null;
        Process process = null;
        //gDatos.getLstActiveGrupos().clear();
        
        for (int i=0; i<numAgeActives; i++) {
        	/**
        	 * Realiza los ajustes de la query en base al motor de la metadata
        	 * 
        	 * Recupera los Grupos correspondientes a la agenda potencial siempre que el grupo este enable, el cliente este enable y el
        	 */
        	vSQL = gSub.getSqlFindGroup(
        				gDatos.getServerInfo().getDbType(), 
        				gDatos.getLstActiveAgendas().get(i).getAgeID(), 
        				gDatos.getLstActiveAgendas().get(i).getNumSecExec());
        
        	/**
        	 * Ejecuta consulta a Metadata
        	 */
        	logger.debug("Query: "+vSQL);
        	
            try (ResultSet rs = (ResultSet) metadata.getQuery(vSQL)) {
                if (rs!=null) {
                	String vGRPID="";
                    while (rs.next()) {
                    	if (!vGRPID.equals(rs.getString("GRPID"))) {
                    		if (!vGRPID.equals("")) {
                    			gDatos.updateLstActiveGrupos(grupo);
                    		}
                            grupo = new Grupo();
                            grupo.setGrpID(rs.getString("GRPID"));
                            grupo.setGrpDESC(rs.getString("GRPDESC"));
                            grupo.setGrpCLIID(rs.getString("CLIID"));
                            grupo.setGrpHORID(rs.getString("HORID"));
                            grupo.setGrpUFechaExec(rs.getString("UFECHAEXEC"));
                            grupo.setuStatus(rs.getString("USTATUS"));
                            grupo.setStatus(rs.getString("STATUS"));
                            grupo.setNumSecExec(gDatos.getLstActiveAgendas().get(i).getNumSecExec());
                            grupo.setLastNumSecExec(rs.getString("LASTNUMSECEXEC"));
                            vGRPID = rs.getString("GRPID");
                    	}
                        process = new Process();
                        process.setProcID(rs.getString("PROCID"));
                        process.setnOrder(rs.getInt("NORDER"));
                        process.setCritical(rs.getInt("CRITICAL"));
                        process.setType(rs.getString("TYPE"));
                    	
                        //Recupera Detalle del Proceso
                        try {
                        	ResultSet rsProc;
                            switch (process.getType()) {
                                case "FTP":
                                	Ftp ftp;
                                    String vSQLproc = "select * from tb_ftp where ftpID='"+process.getProcID()+"'";
                                    rsProc = (ResultSet) metadata.getQuery(vSQLproc);
                                    if (rsProc!=null) {
                                        while (rsProc.next()) {
                                            ftp = new Ftp();
                                            ftp.setID(rsProc.getString("FTPID"));
                                            ftp.setDesc(rsProc.getString("FTPID"));
                                            ftp.setSourceServerID(rsProc.getString("SRVSOURCEID"));
//                                            map.put("ftpID", rsProc.getString("FTPID"));
//                                            map.put("FTPDESC", rsProc.getString("FTPDESC"));
//                                            map.put("SRVSOURCEID", rsProc.getString("SRVSOURCEID"));
//                                            map.put("SRVDESTID", rsProc.getString("SRVDESTID"));
//                                            map.put("PATTERNFIND", rsProc.getString("PATTERNFIND"));
//                                            map.put("USEPATTERNFIND", rsProc.getString("USEPATTERNFIND"));
//                                            map.put("FILESOURCENAME", rsProc.getString("FILESOURCENAME"));
//                                            map.put("FILESOURCEDEST", rsProc.getString("FILESOURCEDEST"));
//                                            map.put("USERSOURCEID", rsProc.getString("USERSOURCEID"));
//                                            map.put("USERDESTID", rsProc.getString("USERDESTID"));
//                                            map.put("PATHSOURCE", rsProc.getString("PATHSOURCE"));
//                                            map.put("PATHDEST", rsProc.getString("PATHDEST"));
//                                            map.put("FTPTYPE", rsProc.getString("FTPTYPE"));
//                                            map.put("FTPENABLE", rsProc.getString("ENABLE"));
                                            process.setParams(ftp);
                                        }
                                        rsProc.close();
                                    } else {
                                        logger.error("No hay detalle para el proceso: "+process.getProcID());
                                    }
                                    break;
                                case "ETL":
                                	ETL etl = null;
                                    logger.info("Buscando programaciones de ETL en MetaData.");
                                    
                                    vSQL = 		"select  cfg.ETLID ETLID, cfg.ETLDESC ETLDESC, cfg.ETLENABLE ETLENABLE, cli.CLIDESC CLIDESC,\n" +
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
                                                "  tb_server srv,\n" +
                                                "  tb_dbase db,\n" +
                                                "  tb_client cli,\n" +
                                                "  TB_USER usr,\n" +
                                                "  TB_server srvD,\n" +
                                                "  TB_dbase dbD,\n" +
                                                "  TB_USER usrD\n" +
                                                "where\n" +
                                                "  cfg.ETLCLIID = cli.CLIID\n" +
                                                "  And cfg.ETLSourceServerID = srv.SERVERID\n" +
                                                "  And cfg.ETLSourceDBID = db.DBID\n" +
                                                "  And cfg.ETLSOURCEUSERID = usr.USERID\n" +
                                                "  And cfg.ETLDESTSERVERID = srvD.SERVERID\n" +
                                                "  And cfg.ETLDESTDBID = dbD.DBID\n" +
                                                "  And cfg.ETLDESTUSERID = usrD.USERID\n" +
                                                "  And cfg.ETLID='"+ process.getProcID() +"' \n" +
                                                "order by\n" +
                                                "  ETLID";
                                    rsProc = (ResultSet) metadata.getQuery(vSQL);
                                    if (rsProc!=null) {
                                        while (rsProc.next()) {
                                        	etl = new ETL();
                                            if (rsProc.getString("ETLID")!=null) {
                                                etl.setETLID(rsProc.getString("ETLID")); 
                                            }
                                            if (rsProc.getString("ETLDESC")!=null) {
                                                etl.setETLDesc(rsProc.getString("ETLDESC")); 
                                            }
                                            if (rsProc.getString("ETLENABLE")!=null) {
                                                etl.setETLEnable(rsProc.getInt("ETLENABLE")); 
                                            }
                                            if (rsProc.getString("CLIDESC")!=null) {
                                                etl.setCliDesc(rsProc.getString("CLIDESC")); 
                                            }
                                            if (rsProc.getString("FIELDKEY")!=null) {
                                                etl.setFIELDKEY(rsProc.getString("FIELDKEY")); 
                                            }
                                            if (rsProc.getString("FIELDTYPE")!=null) {
                                                etl.setFIELDTYPE(rsProc.getString("FIELDTYPE")); 
                                            }
                                            if (rsProc.getString("TIMEGAP")!=null) {
                                                etl.setTIMEGAP(rsProc.getInt("TIMEGAP")); 
                                            }
                                            if (rsProc.getString("TIMEGEN")!=null) {
                                                etl.setTIMEGEN(rsProc.getInt("TIMEGEN")); 
                                            }
                                            if (rsProc.getString("TIMEPERIOD")!=null) {
                                                etl.setTIMEPERIOD(rsProc.getInt("TIMEPERIOD")); 
                                            }
                                            if (rsProc.getString("UNITMEASURE")!=null) {
                                                etl.setUNITMEASURE(rsProc.getString("UNITMEASURE")); 
                                            }
                                            if (rsProc.getString("WHEREACTIVE")!=null) {
                                                etl.setWHEREACTIVE(rsProc.getInt("WHEREACTIVE")); 
                                            }
                                            if (rsProc.getString("QUERYBODY")!=null) {
                                                etl.setQUERYBODY(rsProc.getString("QUERYBODY")); 
                                            }
                                            if (rsProc.getString("STBNAME")!=null) {
                                                etl.setSTBNAME(rsProc.getString("STBNAME")); 
                                            }
                                            if (rsProc.getString("DTBNAME")!=null) {
                                                etl.setDTBNAME(rsProc.getString("DTBNAME")); 
                                            }
                                            if (rsProc.getString("SIP")!=null) {
                                                etl.setSIP(rsProc.getString("SIP")); 
                                            }
                                            if (rsProc.getString("SDBNAME")!=null) {
                                                etl.setSDBNAME(rsProc.getString("SDBNAME")); 
                                            }
                                            if (rsProc.getString("SDBDESC")!=null) {
                                                etl.setSDBDESC(rsProc.getString("SDBDESC")); 
                                            }
                                            if (rsProc.getString("SDBTYPE")!=null) {
                                                etl.setSDBTYPE(rsProc.getString("SDBTYPE")); 
                                            }
                                            if (rsProc.getString("SDBPORT")!=null) {
                                                etl.setSDBPORT(rsProc.getString("SDBPORT")); 
                                            }
                                            if (rsProc.getString("SDBINSTANCE")!=null) {
                                                etl.setSDBINSTANCE(rsProc.getString("SDBINSTANCE")); 
                                            }
                                            if (rsProc.getString("SDBCONF")!=null) {
                                                etl.setSDBCONF(rsProc.getString("SDBCONF")); 
                                            }
                                            if (rsProc.getString("SDBJDBC")!=null) {
                                                etl.setSDBJDBC(rsProc.getString("SDBJDBC")); 
                                            }
                                            if (rsProc.getString("SUSERNAME")!=null) {
                                                etl.setSUSERNAME(rsProc.getString("SUSERNAME")); 
                                            }
                                            if (rsProc.getString("SUSERPASS")!=null) {
                                                etl.setSUSERPASS(rsProc.getString("SUSERPASS")); 
                                            }
                                            if (rsProc.getString("SUSERTYPE")!=null) {
                                                etl.setSUSERTYPE(rsProc.getString("SUSERTYPE")); 
                                            }
                                            if (rsProc.getString("DIP")!=null) {
                                                etl.setDIP(rsProc.getString("DIP")); 
                                            }
                                            if (rsProc.getString("DDBDESC")!=null) {
                                                etl.setDDBDESC(rsProc.getString("DDBDESC")); 
                                            }
                                            if (rsProc.getString("SDBNAME")!=null) {
                                                etl.setDDBNAME(rsProc.getString("SDBNAME")); 
                                            }
                                            if (rsProc.getString("DDBTYPE")!=null) {
                                                etl.setDDBTYPE(rsProc.getString("DDBTYPE")); 
                                            }
                                            if (rsProc.getString("DDBPORT")!=null) {
                                                etl.setDDBPORT(rsProc.getString("DDBPORT")); 
                                            }
                                            if (rsProc.getString("DDBINSTANCE")!=null) {
                                                etl.setDDBINSTANCE(rsProc.getString("DDBINSTANCE")); 
                                            }
                                            if (rsProc.getString("DDBCONF")!=null) {
                                                etl.setDDBCONF(rsProc.getString("DDBCONF")); 
                                            }
                                            if (rsProc.getString("DDBJDBC")!=null) {
                                                etl.setDDBJDBC(rsProc.getString("DDBJDBC")); 
                                            }
                                            if (rsProc.getString("DUSERNAME")!=null) {
                                                etl.setDUSERNAME(rsProc.getString("DUSERNAME")); 
                                            }
                                            if (rsProc.getString("DUSERPASS")!=null) {
                                                etl.setDUSERPASS(rsProc.getString("DUSERPASS")); 
                                            }
                                            if (rsProc.getString("DUSERTYPE")!=null) {
                                                etl.setDUSERTYPE(rsProc.getString("DUSERTYPE")); 
                                            }
                                            if (rsProc.getString("LASTNUMSECEXEC")!=null) {
                                                etl.setLASTNUMSECEXEC(rsProc.getString("LASTNUMSECEXEC")); 
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
                                                
                                                ResultSet rs2 = (ResultSet) metadata.getQuery(vSQL2);
                                                if (rs2!=null) {
                                                    List<EtlMatch> lstEtlMatch = new ArrayList<>();
                                                    EtlMatch etlMatch;
                                                    
                                                    logger.info("Recorriendo resultset de respuesta...");
                                                    
                                                    while (rs2.next()) {
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
                                            
                                        } //Fin While
                                        rsProc.close();
                                        
                                        
                                    } else {
                                        logger.error("No hay detalle para el proceso: "+process.getProcID());
                                    }
                                    process.setParams(etl);
                                	break;
                                default:
                                	process.setParams("{}");
                                    break;
                            }
                        } catch (Exception e) {
                            logger.error("Error buscando detalle del proceso: "+ process.getProcID());
                        }
                        
                        /**
                         * Agrega el proceso al grupo correspondiente
                         */
                        grupo.getLstProcess().add(process);
                    } // END while (rs.next())
                    /**
                     * Asigna el ultimo regsitro leido a la lista correspondiente si es que existen datos
                     */
            		if (!vGRPID.equals("")) {
            			gDatos.updateLstActiveGrupos(grupo);
            		}

                } else {
                	logger.info("No hay Grupos asociados a Agendas Potencialmente activas");
                }
            } catch (Exception e) {
                logger.error("Error ejecutando query busca Grupos Activos..."+e.getMessage());
            }
        }
        
        logger.info("Se encontraron: "+gDatos.getLstActiveGrupos().size()+" Grupos para Activar...");
        
        for (int i=0; i<gDatos.getLstActiveGrupos().size(); i++) {
		    //logger.info("Grupos para activar: "+ gSub.serializeObjectToJSon(gDatos.getLstActiveGrupos().get(i), true));
		    logger.info("Grupos para activar: "+ gDatos.getLstActiveGrupos().get(i).getGrpID() + " : " + gDatos.getLstActiveGrupos().get(i).getNumSecExec());
		}

        logger.info("Finaliza busquenda agendas activas...");
        
        
        /**
         * 
         * Actualiza la lista de PoolProcess con la información de grupo, proceso y detalle del proceso
         */
        
        
        //Inicia Recorriendo la lista de Grupos Activos y validando si cada proceso interno se encuentra o no en la poolProcess
        //para no tener que ir a buscar su información nuevamente a la base de datos.
        //La llave de acceso a busquedas es el grpID, procID, numSecExec
        
        /**
         * Inicia asignacion de grupos de procesos a pool de servicios para ser enviados a server de procesos.
         */
        
        int numLstGrupos = gDatos.getLstActiveGrupos().size();
        
        if (numLstGrupos>0) {
            for (int i=0; i<numLstGrupos; i++) {
                String grpID = gDatos.getLstActiveGrupos().get(i).getGrpID();
                int numProcAssigned = gDatos.getLstActiveGrupos().get(i).getLstProcess().size();
                for (int j=0; j<numProcAssigned; j++) {
                    String procID = gDatos.getLstActiveGrupos().get(i).getLstProcess().get(j).getProcID();
                }
            }
        } else {
            logger.info("No hay Grupos asociados a Agendas activas.");
        }
        
        metadata.closeConnection();
    }
}
