/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;

import java.sql.Connection;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import dataClass.Agenda;
import dataClass.Grupo;
import dataClass.TaskProcess;
import utilities.globalAreaData;
//import utilities.hbaseDB;
import utilities.mysqlDB;
import utilities.oracleDB;
import utilities.sqlDB;

/**
 *
 * @author andresbenitez
 */
public class MetaData {
    static Logger logger = Logger.getLogger("MetaData");
    globalAreaData gDatos;
    
    private oracleDB oraConn;
    private sqlDB sqlConn;
    //private hbaseDB hbConn;
    private mysqlDB myConn; 
    
    public MetaData (globalAreaData m) {
        gDatos = m;
        
        switch (gDatos.getServerInfo().getDbType()) {
            case "ORA":
                try {
                    oraConn = new oracleDB(gDatos.getServerInfo().getDbHost(), gDatos.getServerInfo().getDbName(), gDatos.getServerInfo().getDbPort(), gDatos.getServerInfo().getDbUser(), gDatos.getServerInfo().getDbPass());
                    oraConn.conectar();
                    if (oraConn.getConnStatus()) {
                        gDatos.getServerStatus().setIsValMetadataConnect(true);
                    }
                    else {
                        gDatos.getServerStatus().setIsValMetadataConnect(false);
                    }
                } catch (Exception e) {
                    logger.error("Error de conexion a MetaData: "+e.getMessage());
                    gDatos.getServerStatus().setIsValMetadataConnect(false);
                }
                break;
            case "mySQL":
                try {
                    myConn = new mysqlDB(gDatos.getServerInfo().getDbHost(), gDatos.getServerInfo().getDbName(), gDatos.getServerInfo().getDbPort(), gDatos.getServerInfo().getDbUser(), gDatos.getServerInfo().getDbPass());
                    myConn.conectar();
                    if (myConn.getConnStatus()) {
                        gDatos.getServerStatus().setIsValMetadataConnect(true);
                    }
                    else {
                        gDatos.getServerStatus().setIsValMetadataConnect(false);
                    }
                } catch (Exception e) {
                    logger.error("Error de conexion a MetaData: "+e.getMessage());
                    gDatos.getServerStatus().setIsValMetadataConnect(false);
                }
                break;
            case "SQL":
                break;
            case "HBASE":
                break;
            default:
                break;
        }
    }
    
    public Connection getConnection() {
    	switch (gDatos.getServerInfo().getDbType()) {
    		case "ORA":
    			return oraConn.getConexion();
    		case "SQL":
    			return sqlConn.getConexion();
    		case "mySQL":
    			return myConn.getConexion();
			default:
				return null;
    	} 
    }
    
    public boolean isConnected() {
        boolean connected = false;
        try  {
            switch (gDatos.getServerInfo().getDbType()) {
                case "ORA":
                    connected = oraConn.getConnStatus();
                    break;
                case "SQL":
                    connected = sqlConn.getConnStatus();
                    break;
                case "mySQL":
                    connected = myConn.getConnStatus();
                    break;
                default:
                    connected = false;
            }
            return connected;
        } catch (Exception e) {
            return connected;
        }
    }
    
    public int executeQuery(String vSQL) {
    	int result = 0;
        switch (gDatos.getServerInfo().getDbType()) {
        	case "mySQL":
        			result = myConn.execute(vSQL);
        		break;
    		default:
    			break;
        }
        return result;
    }
    
    public Object getQuery(String vSQL) {
        switch (gDatos.getServerInfo().getDbType()) {
            case "ORA":
                try {
                    ResultSet rs = oraConn.consultar(vSQL);
                    return rs;
                } catch (Exception e) {
                    logger.error("Error de Ejecucion SQL: "+ vSQL+ " details: "+ e.getMessage());
                }
                break;
            case "mySQL":
                try {
                    ResultSet rs = myConn.consultar(vSQL);
                    return rs;
                } catch (Exception e) {
                    logger.error("Error de Ejecucion SQL: "+ vSQL+ " details: "+ e.getMessage());
                }
                break;
            case "SQL":
                break;
            case "HBASE":
                break;
            default:
                break;
        }
        return null;
    }
    
    public void closeConnection() {
        switch (gDatos.getServerInfo().getDbType()) {
            case "ORA":
                try {
                    oraConn.closeConexion();
                } catch (Exception e) {
                    logger.error("Error Cerrando Conexion: "+e.getMessage());
                    gDatos.getServerStatus().setIsValMetadataConnect(false);
                }
                break;
            case "mySQL":
                try {
                    myConn.closeConexion();
                } catch (Exception e) {
                    logger.error("Error Cerrando Conexion: "+e.getMessage());
                    gDatos.getServerStatus().setIsValMetadataConnect(false);
                }
                break;
            case "SQL":
                break;
            case "HBASE":
                break;
            default:
                break;
        }
    }
    
    public String getSqlFindAgeActive(String iteratorMinute, String posmonth, String posdayOfMonth, String posdayOfWeek, String posweekOfYear, String posweekOfMonth, String posIteratorHour, String posIteratorMinute) {
    	String vSQL=null;
    	switch (gDatos.getServerInfo().getDbType()) {
    		case "ORA":
	            vSQL = "select "+iteratorMinute+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from tb_diary where "
	                    + "     ageEnable=1 "
	                    + "     and substr(month,"+posmonth+",1) = '1'"
	                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
	                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
	                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
	                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
	                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'"
	                    + "     and substr(minute,"+posIteratorMinute +",1) = '1'";
    			break;
    		case "SQL":
	            vSQL = "select "+iteratorMinute+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from tb_diary where "
	                    + "     ageEnable=1 "
	                    + "     and substr(month,"+posmonth+",1) = '1'"
	                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
	                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
	                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
	                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
	                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'"
	                    + "     and substr(minute,"+posIteratorMinute +",1) = '1'";
    			break;
    		case "mySQL":
	            vSQL = "select "+iteratorMinute+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from tb_diary where "
	                    + "     ageEnable=1 "
	                    + "     and substr(month,"+posmonth+",1) = '1'"
	                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
	                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
	                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
	                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
	                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'"
	                    + "     and substr(minute,"+posIteratorMinute +",1) = '1'";
    			break;
    		default:
    			break;
    	}
    	return vSQL;
    }

    
    public String getSqlFindAgeShow(String iteratorHour, String posmonth, String posdayOfMonth, String posdayOfWeek, String posweekOfYear, String posweekOfMonth, String posIteratorHour) {
    	String vSQL=null;
    	switch (gDatos.getServerInfo().getDbType()) {
    		case "ORA":
	            vSQL = "select "+iteratorHour+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from tb_diary where "
	                    + "     ageEnable=1 "
	                    + "     and substr(month,"+posmonth+",1) = '1'"
	                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
	                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
	                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
	                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
	                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'";
    			break;
    		case "SQL":
	            vSQL = "select "+iteratorHour+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from tb_diary where "
	                    + "     ageEnable=1 "
	                    + "     and substr(month,"+posmonth+",1) = '1'"
	                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
	                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
	                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
	                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
	                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'";
    			break;
    		case "mySQL":
	            vSQL = "select "+iteratorHour+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from tb_diary where "
	                    + "     ageEnable=1 "
	                    + "     and substr(month,"+posmonth+",1) = '1'"
	                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
	                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
	                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
	                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
	                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'";
    			break;
    		default:
    			break;
    	}
    	return vSQL;
    }
    
    public String getSqlFindInterval(String etlID, String numSecExec, String intervalID) {
    	String vSQL=null;
    	switch (gDatos.getServerInfo().getDbType()) {
    		case "ORA":
    			break;
    		case "SQL":
    			break;
    		case "mySQL":
    				vSQL =  " select ETLID, numSecExec, INTERVALID, FECINS, FECUPDATE, " +
    						" 		 STATUS, USTATUS, rowsLoad, rowsRead, intentos, " +
    						"        fecIni, fecFin " +
    						" from " +
    						" 		 tb_etlInterval " +
    						" where " +
    						" 		 etlID='" + etlID + "' " +
    						"		 and numSecExec='" + numSecExec + "' " +
							"		 and intervalID='" + intervalID + "' ";
    						
    			break;
    		default:
    			break;
    	}
    	return vSQL;
    }
    
    public String getSqlFindProcess(String vGrpID) {
    	String vSQL="";
    	switch (gDatos.getServerInfo().getDbType()) {
    	case "ORA":
            vSQL =  "";
    		break;
    	case "SQL":
    		break;
    	case "mySQL":
    		vSQL = 	" select grpID, procID, nOrder, critical, type " +
					" from " +
					"	tb_procGroup " +
					" where " +
					"	grpID='"+vGrpID+"' " +
					"	and enable='1' " +
					" order by " +
					"	norder asc ";
    		break;
		default:
			vSQL="";
			break;
    	}
    	
    	return vSQL;

    }
    
    public String getSqlFindDependences(String vGrpID) {
    	String vSQL="";
    	switch (gDatos.getServerInfo().getDbType()) {
    	case "ORA":
            vSQL =  "";
    		break;
    	case "SQL":
    		break;
    	case "mySQL":
    		vSQL = 	" select " +
                    "  GRPID, PROCHIJO, PROCPADRE, CRITICAL " + 
                    " from  " +
                    "  tb_procDepend " +
                    " where " +
                    "  GRPID='"+ vGrpID  +"' " +
                    " order by PROCHIJO, PROCPADRE ";
    		break;
		default:
			vSQL="";
			break;
    	}
    	
    	return vSQL;

    }
    
    public String getSqlFindETLHuerfanInterval(String ETLID) {
    	switch (gDatos.getServerInfo().getDbType()) {
    	case "ORA":
            return  null;
    	case "SQL":
            return null;
    	case "mySQL":
    		return	"select " +
                    "  ETLID, INTERVALID, " + 
                    "  FECINS, FECUPDATE, " +
                    "  STATUS, USTATUS, " +
                    "  ROWSLOAD, ROWSREAD, " +
                    "  INTENTOS, FECINI, FECFIN " +
                    "from  " +
                    "  tb_etlInterval " +
                    "where " +
                    "  ETLID='"+ ETLID  +"' " +
                    "  And STATUS='Ready' " + 
                    "  And numSecExec is null";
		default:
			return null;
    	}    	
    }


    public String getSqlFindETLMatch(String ETLID) {
    	String vSQL="";
    	switch (gDatos.getServerInfo().getDbType()) {
    	case "ORA":
            vSQL =  "";
    		break;
    	case "SQL":
    		break;
    	case "mySQL":
    		vSQL = 	"select " +
                    "  ETLORDER, ETLSOURCEFIELD, ETLSOURCELENGTH, ETLSOURCETYPE, " +
                    "  ETLDESTFIELD, ETLDESTLENGTH, ETLDESTTYPE " +
                    "from  " +
                    "  tb_etlMatch " +
                    "where " +
                    "  ETLID='"+ ETLID  +"' " +
                    "  And ETLENABLE=1 order by ETLORDER";
    		break;
		default:
			vSQL="";
			break;
    	}
    	
    	return vSQL;
    }

    
    public String getSqlFindETL(String procID) {
    	String vSQL="";
    	switch (gDatos.getServerInfo().getDbType()) {
    	case "ORA":
            vSQL =  "";
    		break;
    	case "SQL":
    		break;
    	case "mySQL":
    		vSQL = 	"select  cfg.ETLID ETLID, cfg.ETLDESC ETLDESC, cfg.ETLENABLE ETLENABLE, cli.CLIDESC CLIDESC, " +
                    "        cfg.ETLINTERVALFIELDKEY FIELDKEY, cfg.ETLINTERVALFIELDKEYTYPE FIELDTYPE, cfg.ETLINTERVALTIMEGAP TIMEGAP, cfg.ETLINTERVALTIMEGENINTERVAL TIMEGEN, " +
                    "        cfg.ETLINTERVALTIMEPERIOD TIMEPERIOD, cfg.ETLINTERVALUNITMEASURE UNITMEASURE, cfg.ETLQUERYWHEREACTIVE WHEREACTIVE, cfg.ETLQUERYBODY QUERYBODY, " +
                    "        cfg.ETLSOURCETBNAME STBNAME,  cfg.ETLDESTTBNAME DTBNAME, cfg.ETLLASTNUMSECEXEC LASTNUMSECEXEC, " +
                    "        srv.SERVERIP SIP,  " +
                    "        db.DBDESC SDBDESC, db.DBNAME SDBNAME, db.DBTYPE SDBTYPE, db.DBPORT SDBPORT, db.DBINSTANCE SDBINSTANCE, db.DBFILECONF SDBCONF, db.DBJDBCSTRING SDBJDBC, " +
                    "        usr.USERNAME SUSERNAME, usr.USERPASS SUSERPASS, usr.USERTYPE SUSERTYPE, " +
                    "        srvD.SERVERIP DIP, " +
                    "        dbD.DBDESC DDBDESC, dbD.DBNAME DDBNAME, dbD.DBTYPE DDBTYPE, dbD.DBPORT DDBPORT, dbD.DBINSTANCE DDBINSTANCE, dbD.DBFILECONF DDBCONF, dbD.DBJDBCSTRING DDBJDBC, " +
                    "        usrD.USERNAME DUSERNAME, usrD.USERPASS DUSERPASS, usrD.USERTYPE DUSERTYPE " +
                    "from " +
                    "  tb_etlConf cfg, " +
                    "  tb_server srv, " +
                    "  tb_dbase db, " +
                    "  tb_client cli, " +
                    "  TB_USER usr, " +
                    "  TB_server srvD, " +
                    "  TB_dbase dbD, " +
                    "  TB_USER usrD " +
                    "where " +
                    "  cfg.ETLCLIID = cli.CLIID " +
                    "  And cfg.ETLSourceServerID = srv.SERVERID " +
                    "  And cfg.ETLSourceDBID = db.DBID " +
                    "  And cfg.ETLSOURCEUSERID = usr.USERID " +
                    "  And cfg.ETLDESTSERVERID = srvD.SERVERID " +
                    "  And cfg.ETLDESTDBID = dbD.DBID " +
                    "  And cfg.ETLDESTUSERID = usrD.USERID " +
                    "  And cfg.ETLID='"+ procID +"'  " +
                    "order by " +
                    "  ETLID";
    		break;
		default:
			vSQL="";
			break;
    	}
    	
    	return vSQL;
    }
    
    public String getSqlFindGroupNew(Agenda agenda) {
    	String vSQL="";
    	switch (gDatos.getServerInfo().getDbType()) {
	    	case "ORA":
	    		break;
	    	case "SQL":
	    		break;
	    	case "mySQL":
	    		vSQL = 	"select gr.GRPID, gr.GRPDESC, date_format(gr.UFECHAEXEC,'%Y-%m-%d %H:%i:%s') UFECHAEXEC, "+
						"		gr.USTATUS, gr.STATUS, gr.LASTNUMSECEXEC,  gr.CLIID, ha.HORID " +
						"from " +    
					   "	tb_group gr, "+
					   "	tb_schedDiary ha,"+    
					   "	tb_client cl, "+   
					   "	TB_schedule ho, "+   
					   "	TB_diary ag "+   
					   "where "+                          
					   "	gr.HORID = ho.HORID "+    
					   "	and ha.HORID = ho.HORID "+    
					   "	and ha.AGEID = ag.AGEID "+   
					   "	and gr.CLIID = cl.CLIID "+   
					   "	and gr.HORID = ha.HORID "+   
					   "	and ha.HORINCLUSIVE=1 "+   
					   "	and gr.ENABLE =1 "+   
					   "	and gr.LASTNUMSECEXEC < '"+ agenda.getNumSecExec() +"' "+    
					   "	and ha.AGEID='"+ agenda.getAgeID() +"' "+   
					   "	and cl.ENABLE = 1 "+   
					   "	and ag.AGEENABLE = 1 "+   
					   "	and ho.HORENABLE = 1 "+   
					   "	and ha.HORENABLE = 1";
	    		break;
			default:
				break;
    	}
    	return vSQL;
    }
    
    public boolean ifExistRowKey(String vSQL) {
    	boolean isExistRow = false;
    	switch (gDatos.getServerInfo().getDbType()) {
    		case "ORA":
    			break;
    		case "SQL":
    			break;
    		case "mySQL":
    			try {
    				isExistRow = myConn.isExistRowKey(vSQL);
    			} catch (Exception e) {
    				logger.error("Error en ifExistRowKey...: "+e.getMessage());
    			}
    			break;
    		default:
    			break;
    	}
    	return isExistRow;
    }

    
    public String getSqlInsGrupoExec(String vGroupID, String vNumSecExec) {
    	switch (gDatos.getServerInfo().getDbType()) {
    		case "ORA":
    			return null;
    		case "SQL":
    			return null;
    		case "mySQL":
    			return  " insert into tb_grpExec (grpID, numSecExec, status, uFecIns, uFecExec, typeExec) " +
    					"	values ( " +
						" '"+ vGroupID + "', " + 
						" '"+ vNumSecExec + "', " +
						" 'Pending', " +
						" Now(), " +
						" NULL, " +
						" 'Auto')";
			default:
				return null;
    	}
    }
    
    public String getSqlFindGrupoExec(String vGroupID, String vNumSecExec) {
    	switch (gDatos.getServerInfo().getDbType()) {
    		case "ORA":
    			return null;
    		case "SQL":
    			return null;
    		case "mySQL":
    			return  "select " +
    					"	grpID " +
						" from " +
						" 	tb_grpExec " +
						" where " +
						" 	grpID='"+ vGroupID + "' " +
						"	and numSecExec = '"+ vNumSecExec + "' ";
			default:
				return null;
    	}
    }
    
    public String getSqlFindGroup(String ageID) {
    	String vSQL="";
    	switch (gDatos.getServerInfo().getDbType()) {
    	case "ORA":
            vSQL =  "select gr.GRPID, gr.GRPDESC, gr.CLIID, cl.cliDesc, ho.horDesc " +
                    "from " +
                    "  process.tb_group gr, " +
                    "  process.tb_schedDiary ha, " +
                    "  process.tb_client cl, " +
                    "  process.TB_schedule ho, " +
                    "  process.TB_diary ag " +
                    "where " +
                    "  gr.HORID = ho.HORID " +
                    "  and ha.HORID = ho.HORID " +
                    "  and ha.AGEID = ag.AGEID " +
                    "  and gr.CLIID = cl.CLIID " +
                    "  and gr.HORID = ha.HORID " +
                    "  and ha.HORINCLUSIVE=1 " +
                    "  and gr.ENABLE =1 " +
                    "  and ha.AGEID='"+ageID+"' " +
                    "  and cl.ENABLE = 1 " +
                    "  and ag.AGEENABLE = 1 " +
                    "  and ho.HORENABLE = 1 " +
                    "  and ha.HORENABLE = 1 ";
    		break;
    	case "SQL":
            vSQL =  "select gr.GRPID, gr.GRPDESC, gr.CLIID, cl.cliDesc, ho.horDesc " +
                    "from " +
                    "  tb_group gr, " +
                    "  tb_schedDiary ha, " +
                    "  tb_client cl, " +
                    "  TB_schedule ho, " +
                    "  TB_diary ag " +
                    "where " +
                    "  gr.HORID = ho.HORID " +
                    "  and ha.HORID = ho.HORID " +
                    "  and ha.AGEID = ag.AGEID " +
                    "  and gr.CLIID = cl.CLIID " +
                    "  and gr.HORID = ha.HORID " +
                    "  and ha.HORINCLUSIVE=1 " +
                    "  and gr.ENABLE =1 " +
                    "  and ha.AGEID='"+ageID+"' " +
                    "  and cl.ENABLE = 1 " +
                    "  and ag.AGEENABLE = 1 " +
                    "  and ho.HORENABLE = 1 " +
                    "  and ha.HORENABLE = 1 ";
    		break;
    	case "mySQL":
            vSQL =  "select gr.GRPID, gr.GRPDESC, gr.CLIID, cl.cliDesc, ho.horDesc " +
                    "from " +
                    "  tb_group gr, " +
                    "  tb_schedDiary ha, " +
                    "  tb_client cl, " +
                    "  TB_schedule ho, " +
                    "  TB_diary ag " +
                    "where " +
                    "  gr.HORID = ho.HORID " +
                    "  and ha.HORID = ho.HORID " +
                    "  and ha.AGEID = ag.AGEID " +
                    "  and gr.CLIID = cl.CLIID " +
                    "  and gr.HORID = ha.HORID " +
                    "  and ha.HORINCLUSIVE=1 " +
                    "  and gr.ENABLE =1 " +
                    "  and ha.AGEID='"+ageID+"' " +
                    "  and cl.ENABLE = 1 " +
                    "  and ag.AGEENABLE = 1 " +
                    "  and ho.HORENABLE = 1 " +
                    "  and ha.HORENABLE = 1 ";
    		break;
		default:
			vSQL="";
			break;
    	}
    	
    	return vSQL;
    }

}
