/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.sql.Connection;
import java.sql.ResultSet;
import org.apache.log4j.Logger;
import utilities.globalAreaData;
import utilities.mysqlDB;
import utilities.oracleDB;
import utilities.sqlDB;

/**
 *
 * @author andresbenitez
 */
public class dataAccess {
    static Logger logger = Logger.getLogger("DataAccess");
    globalAreaData gDatos;
    
    private oracleDB oraConn;
    private sqlDB sqlConn;
    //private hbaseDB hbConn;
    private mysqlDB myConn;
    
    private String dbType;
    private String dbHost;
    private String dbPort;
    private String dbName;
    private String dbUser;
    private String dbPass;
    private String dbInstance;
    
    public dataAccess (globalAreaData m) {
        gDatos = m;
    }
     
    public void conectar() {
        switch (dbType) {
            case "ORA":
                try {
                    oraConn = new oracleDB(dbHost, dbName, dbPort, dbUser, dbPass);
                    oraConn.conectar();
                    if (oraConn.getConnStatus()) {
                        //gDatos.getServerStatus().setIsValMetadataConnect(true);
                    }
                    else {
                        //gDatos.getServerStatus().setIsValMetadataConnect(false);
                    }
                } catch (Exception e) {
                    logger.error("Error de conexion a Base de Datos: "+e.getMessage());
                    //gDatos.getServerStatus().setIsValMetadataConnect(false);
                }
                break;
            case "MYSQL":
                try {
            		myConn = new mysqlDB(dbHost, dbName, String.valueOf(dbPort), dbUser, dbPass);
                    myConn.conectar();
                    if (myConn.getConnStatus()) {
                        //gDatos.getServerStatus().setIsValMetadataConnect(true);
                    }
                    else {
                        //gDatos.getServerStatus().setIsValMetadataConnect(false);
                    }
                } catch (Exception e) {
                    logger.error("Error de conexion a MetaData: "+e.getMessage());
                    //gDatos.getServerStatus().setIsValMetadataConnect(false);
                }
                break;
            case "SQL":
            	try {
        			sqlConn = new sqlDB(dbHost, dbName, dbPort, dbUser, dbPass);
        			sqlConn.conectar();
        			if (sqlConn.getConnStatus()) {
        				
        			} else {
        				
        			}
            	} catch (Exception e) {
                    logger.error("Error de conexion a MetaData: "+e.getMessage());
            	}
                break;
            case "HBASE":
                break;
            default:
                break;
        }
    }
    
    public boolean isConnected() {
        boolean connected = false;
        try  {
            switch (dbType) {
                case "ORA":
                    connected = oraConn.getConnStatus();
                    break;
                case "SQL":
                    connected = sqlConn.getConnStatus();
                    break;
                case "MYSQL":
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

    public Connection getConnection() {
        switch (dbType) {
        	case "ORA":
        		return oraConn.getConexion();
        	case "MYSQL":
        		return myConn.getConexion();
        	case "SQL":
        		return sqlConn.getConexion();
    		default:
    			return null;
        }
    }
    
    public int execQuery(String vSQL) {
        switch (dbType) {
            case "ORA":
                try {
                    return oraConn.execute(vSQL);
                } catch (Exception e) {
                    logger.error("Error de Ejecucion SQL: "+ vSQL+ " details: "+ e.getMessage());
                }
                break;
            case "MYSQL":
                try {
                    return myConn.execute(vSQL);
                } catch (Exception e) {
                    logger.error("Error de Ejecucion SQL: "+ vSQL+ " details: "+ e.getMessage());
                }
                break;
            case "SQL":
            	try {
            		return sqlConn.execute(vSQL);
            	} catch (Exception e) {
            		logger.error("Error de Ejecucion SQL: "+ vSQL+ " details: "+ e.getMessage());
            	}
                break;
            case "HBASE":
                return 0;
            default:
            	return 0;
        }
        return 0;
    }
    
    
    public Object getQuery(String vSQL) {
        switch (dbType) {
            case "ORA":
                try {
                    ResultSet rs = oraConn.consultar(vSQL);
                    return rs;
                } catch (Exception e) {
                    logger.error("Error de Ejecucion SQL: "+ vSQL+ " details: "+ e.getMessage());
                }
                break;
            case "MYSQL":
                try {
                    ResultSet rs = myConn.consultar(vSQL);
                    return rs;
                } catch (Exception e) {
                    logger.error("Error de Ejecucion SQL: "+ vSQL+ " details: "+ e.getMessage());
                }
                break;
            case "SQL":
            	try {
            		ResultSet rs = sqlConn.consultar(vSQL);
            		return rs;
            	} catch (Exception e) {
            		logger.error("Error de Ejecucion SQL: "+ vSQL+ " details: "+ e.getMessage());
            	}
                break;
            case "HBASE":
                break;
            default:
                break;
        }
        return null;
    }
    
    public void closeConnection() {
        switch (dbType) {
            case "ORA":
                try {
                    oraConn.closeConexion();
                } catch (Exception e) {
                    logger.error("Error Cerrando Conexion: "+e.getMessage());
                    //gDatos.getServerStatus().setIsValMetadataConnect(false);
                }
                break;
            case "MYSQL":
                try {
                    myConn.closeConexion();
                } catch (Exception e) {
                    logger.error("Error Cerrando Conexion: "+e.getMessage());
                    //gDatos.getServerStatus().setIsValMetadataConnect(false);
                }
                break;
            case "SQL":
                try {
                    sqlConn.closeConexion();
                } catch (Exception e) {
                    logger.error("Error Cerrando Conexion: "+e.getMessage());
                    //gDatos.getServerStatus().setIsValMetadataConnect(false);
                }
                break;
            case "HBASE":
                break;
            default:
                break;
        }
    }
    
    /**
     * Getter and Setter.
     */

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    public void setDbPass(String dbPass) {
        this.dbPass = dbPass;
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

	public String getDbInstance() {
		return dbInstance;
	}

	public void setDbInstance(String dbInstance) {
		this.dbInstance = dbInstance;
	}
    
}
