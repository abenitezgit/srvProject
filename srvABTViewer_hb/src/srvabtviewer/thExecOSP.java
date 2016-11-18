package srvabtviewer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import abtGlobals.globalVARArea;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ABT
 */
public class thExecOSP extends Thread{
    private globalVARArea gDatos;
    
    public thExecOSP(globalVARArea m) {
        gDatos = m;
    }
    
    
    public void run() {
    
        
        if (gDatos.isOraKeepConnected()) {
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
            //No hay conexión con ABT
        }
    }
    
}
