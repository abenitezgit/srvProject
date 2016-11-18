/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;

import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;

import dataClass.TaskProcess;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thExecMOV extends Thread{
    static srvRutinas gSub;
    static globalAreaData gDatos;
    TaskProcess task = new TaskProcess();
    @SuppressWarnings("unused")
	private JSONObject params = new JSONObject();
    //private String procID = null;
    
    public thExecMOV(globalAreaData m, TaskProcess task) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
        this.task = task;
    }
    
    @Override
    public void run() {
        System.out.println("Ejecutando MOV");
       
    
    }
        
    //Rutinas internas de Control
    //
    
    
    
}
