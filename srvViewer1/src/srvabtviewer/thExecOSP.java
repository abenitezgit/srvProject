package srvabtviewer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import abtGlobals.globalVARArea;
import java.util.logging.Level;
import java.util.logging.Logger;
import mypkg.hbaseDB;

/**
 *
 * @author ABT
 */
public class thExecOSP extends Thread{
    private String cCLASS_NAME = "thExecOSP";
    private globalVARArea gDatos;
    private srv_rutinas gRutinas;
    private hbaseDB connHB;
    boolean isHBConnect;
    
    public thExecOSP(globalVARArea m) {
        try {
            gDatos = m;
            gRutinas = new srv_rutinas(gDatos);
            connHB = new hbaseDB(gDatos.getCfg_glb_metaDataConfPath(),"HBConf2");
            isHBConnect=true;
        } catch (Exception e) {
            isHBConnect=false;
            gDatos.writeLog(2, cCLASS_NAME, "Error: "+e.getMessage());
        }
        
    }
    
    
    public void run() {
    
        
        if (isHBConnect) {
            //Ejecutando Th-OSP
                
            try {
                System.out.println("*********************Ejecutando Th-OSP: "+this.getName()+" "+ this.getState());
                this.sleep(10000);
                System.out.println("*********************Terminando Th-OSP: "+this.getName());
                this.finalize();
            } catch (InterruptedException ex) {
                Logger.getLogger(thExecOSP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Throwable ex) {
                Logger.getLogger(thExecOSP.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        } else {
            gDatos.writeLog(2, cCLASS_NAME, "sin conexion a HBASE");
        }
    }
    
}
