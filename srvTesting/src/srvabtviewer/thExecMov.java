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
public class thExecMov extends Thread{
    private globalVARArea gDatos;
    
    public thExecMov(globalVARArea m) {
        gDatos = m;
    }
    
    
    public void run() {
    
        
        if (gDatos.isOraKeepConnected()) {
                //Ejecutando Th-OSP
                
            try {
                System.out.println("*********************Ejecutando Th-Mov: "+this.getName()+" "+ this.getState());
                this.sleep(10000);
                System.out.println("*********************Terminando Th-Mov: "+this.getName());
                this.finalize();
            } catch (InterruptedException ex) {
                Logger.getLogger(thExecMov.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Throwable ex) {
                Logger.getLogger(thExecMov.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        } else {
            //No hay conexión con ABT
        }
    }
    
}
