/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;

import dataClass.TaskProcess;

import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thExecOSP extends Thread{
    static srvRutinas gSub;
    static globalAreaData gDatos;
    JSONObject params = new JSONObject();
    private String procID = null;
    TaskProcess taskProcess = new TaskProcess();
    Logger logger = Logger.getLogger("thExecOSP");
    
    public thExecOSP(globalAreaData m, TaskProcess taskProcess) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
        this.taskProcess = taskProcess;
        //this.params = poolProcess.getParams();
    }
    
    @Override
    public void run() {
        logger.info("Ejecutando OSP: "+procID);
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(thExecOSP.class.getName()).log(Level.SEVERE, null, ex);
        }

        taskProcess.setEndTime(gSub.getDateNow());
        taskProcess.setStatus("Finished");
        
        gDatos.getMapTask().put(taskProcess.getProcID()+":"+taskProcess.getNumSecExec(), taskProcess);
        //gDatos.setStatusFinished(poolProcess);
    
    }
    
    
}
