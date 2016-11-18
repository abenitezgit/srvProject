/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;

import dataClass.PoolProcess;
import org.apache.log4j.Logger;
import utilities.dataAccess;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thExecETL extends Thread{
	boolean isConnectDBSource;
	boolean isConnectDBDest;
    PoolProcess pool = new PoolProcess();
    static srvRutinas gSub;
    static globalAreaData gDatos;
    Logger logger = Logger.getLogger("thExecETL");
    
    public thExecETL(globalAreaData m, PoolProcess poolProcess) {
        this.pool = poolProcess;
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
    
    @Override
    public void run() {
        logger.info("Iniciando ejecución ETL:"+pool.getProcID()+" "+pool.getIntervalID());
        
        if (isValidDataParam()) {
        	
        	/**
        	 * Establece conexion hacia BD Origen
        	 */
            dataAccess sConn = new dataAccess(gDatos);
            sConn.setDbType((String) pool.getParams().get("sDBType"));
            sConn.setDbHost((String) pool.getParams().get("sIP"));
            sConn.setDbPort((String) pool.getParams().get("sDBPort"));
            sConn.setDbName((String) pool.getParams().get("sDbName"));
            sConn.setDbUser((String) pool.getParams().get("sUserName"));
            sConn.setDbPass((String) pool.getParams().get("sUserPass"));
            
            sConn.conectar();
            
            if (sConn.isConnected()) {
                System.out.println("Connected!!");
                isConnectDBSource= true;
            } else {
                System.out.println("NO Connected!!");
                isConnectDBSource = false;
            }
            
            /**
             * Establece conexion hacia BD Destino
             */
            dataAccess dConn = new dataAccess(gDatos);
            dConn.setDbType((String) pool.getParams().get("dDBType"));
            dConn.setDbHost((String) pool.getParams().get("dIP"));
            dConn.setDbPort((String) pool.getParams().get("dDBPort"));
            dConn.setDbName((String) pool.getParams().get("dDbName"));
            dConn.setDbUser((String) pool.getParams().get("dUserName"));
            dConn.setDbPass((String) pool.getParams().get("dUserPass"));
            
            gDatos.setFinishedPoolProcess(pool, "Success");
        } else {
            logger.error("Error en lectura de parámetros de entrada.");
            gDatos.setFinishedPoolProcess(pool, "Error");
        }
        
        logger.info("Finalizando ejecución ETL:"+pool.getProcID()+" "+pool.getIntervalID());

    }
    
    public boolean isValidDataParam() {
        boolean isValid = true;
        
        try {
            if (pool.getGrpID()==null || pool.getProcID()==null || pool.getIntervalID()==null || pool.getNumSecExec()==null ||
                pool.getParams()==null) {
                isValid = false;
            }
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}