/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;

import dataClass.Ftp;
import dataClass.PoolProcess;
import java.util.Map;
import org.apache.log4j.Logger;
import utilities.globalAreaData;
import utilities.srvRutinas;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author andresbenitez
 */
public class thExecFTP extends Thread{
    PoolProcess pool = new PoolProcess();
    myFtpClass ftp = new myFtpClass();
    static srvRutinas gSub;
    static globalAreaData gDatos;
    static Logger logger = Logger.getLogger("thExecFTP");
    
    public thExecFTP(globalAreaData m, PoolProcess pool) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
        this.pool = pool;
        
    }
    
    @Override
    public void run() {
        logger.info("Iniciando Proceso FTP");
        
        try {
                
            if (ftp.isValidParam(pool.getParams())) {


            }
        
            logger.info("Finalizando Proceso FTP Exitoso");
        } catch (Exception e) {
            logger.error("Finalizando Proceso FTP con error:  "+e.getMessage());
        }
    }
    
    
    class myFtpClass extends FTPClient {
        Ftp dftp = new Ftp();
        boolean isConnectedFtpSource;
        boolean isConnectedFtpDest;
        boolean isFtpSource;
        boolean isFtpDest;
        
        public boolean isValidParam(Map<String, Object> map) {
            try {
                dftp.setID((String) map.get("ftpID"));
                dftp.setDesc((String) map.get("ftpDesc"));
                dftp.setDestFile((String) map.get("ftpDestFile"));
                dftp.setDestIsFtp((boolean) map.get("ftpDestIsFtp"));
                dftp.setDestIsPattern((boolean) map.get("ftpDestIsPattern"));
                dftp.setDestPassID((String) map.get("ftpDestPassID"));
                dftp.setDestPath((String) map.get("ftpDestPath"));
                dftp.setDestServerID((String) map.get("ftpDestServerID"));
                dftp.setDestUserID((String) map.get("ftpDestUserID"));
                dftp.setSourceFile((String) map.get("ftpSourceFile"));
                dftp.setSourceIsFtp((boolean) map.get("ftpSourceIsFtp"));
                dftp.setSourceIsPattern((boolean) map.get("ftpSourceIsPattern"));
                dftp.setSourcePassID((String) map.get("ftpSourcePassID"));
                dftp.setSourcePath((String) map.get("ftpSourcePath"));
                dftp.setSourceServerID((String) map.get("ftpSourceServerID"));
                dftp.setSourceUserID((String) map.get("ftpSourceUserID"));
                return true;
            } catch (Exception e) {
                logger.error("Error en isValidParam..."+e.getMessage());
                return false;
            }
        }

        public boolean isIsConnectedFtpSource() {
            return isConnectedFtpSource;
        }

        public void setIsConnectedFtpSource(boolean isConnectedFtpSource) {
            this.isConnectedFtpSource = isConnectedFtpSource;
        }

        public boolean isIsConnectedFtpDest() {
            return isConnectedFtpDest;
        }

        public void setIsConnectedFtpDest(boolean isConnectedFtpDest) {
            this.isConnectedFtpDest = isConnectedFtpDest;
        }

        public boolean isIsFtpSource() {
            return isFtpSource;
        }

        public void setIsFtpSource(boolean isFtpSource) {
            this.isFtpSource = isFtpSource;
        }

        public boolean isIsFtpDest() {
            return isFtpDest;
        }

        public void setIsFtpDest(boolean isFtpDest) {
            this.isFtpDest = isFtpDest;
        }
        
        
    }
    
}
