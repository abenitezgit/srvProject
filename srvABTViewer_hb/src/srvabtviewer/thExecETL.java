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
public class thExecETL extends Thread{
    private globalVARArea gDatos;
    
    public thExecETL(globalVARArea m) {
        gDatos = m;
    }
    
    
    public void run() {
    
        
            try {
                System.out.println("*********************Ejecutando Th-ETL: "+this.getName()+" "+ this.getState());
                this.sleep(10000);
                System.out.println("*********************Terminando Th-ETL: "+this.getName());
                this.finalize();
                
            } catch (InterruptedException ex) {
                Logger.getLogger(thExecETL.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Throwable ex) {
                Logger.getLogger(thExecETL.class.getName()).log(Level.SEVERE, null, ex);
            }
        
    }
    
}
