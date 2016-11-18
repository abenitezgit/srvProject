/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import dataClass.Grupo;
import dataClass.Process;
import dataClass.TaskProcess;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thInscribeGroup extends Thread{
    globalAreaData gDatos;
    srvRutinas mylib;
    static Logger logger = Logger.getLogger("thInscribeGroup");

    public thInscribeGroup(globalAreaData m) {
        gDatos = m;
        mylib= new srvRutinas(gDatos);
    }
    
    @Override
    public void run() {
        Timer timerMain = new Timer("thSubInscribeGroup");
        timerMain.schedule(new mainTask(), 1000, 5000);
        
    }
    
    class mainTask extends TimerTask{

        @Override
        public void run() {
            logger.info("Inscribiendo Grupos...");
            
            //recore lista global de grupos para crear pool de task para asignarlos a los servidores
            //de proceso correspondientes.
            //gDatos.getLstActiveGrupos()
            
            logger.info("Grupos: "+gDatos.getLstActiveGrupos().size());
            
            logger.info("Services: "+gDatos.getLstServiceStatus().size());
            
            //Busca Grupos en estado de proceso: Sleeping
            
            //List<Grupo> tmpLstGruposSleeping = new ArrayList<>();
            
            //tmpLstGruposSleeping = gDatos.getLstActiveGrupos()
            //        .stream()
            //        .filter(p -> p.getStatus().equals("Sleeping"))
            //        .collect(Collectors.toList());
            
            //Recorre Cada Grupo Sleeping buscando sus procesos
            List<Process> lstProcess = new ArrayList<>();
            TaskProcess task;
            Process process;
            String vGroupID;
            String vNumSecExec;
            String vStatus;
            
            for (int i=0; i<gDatos.getLstActiveGrupos().size(); i++) {
            	//Extrae el GrupoID y el numSecExec
            	vGroupID = gDatos.getLstActiveGrupos().get(i).getGrpID();
            	vNumSecExec = gDatos.getLstActiveGrupos().get(i).getNumSecExec();
            	vStatus = gDatos.getLstActiveGrupos().get(i).getStatus();
            	
            	//Limpia la lista de procesos por cada grupo encontrado
            	lstProcess.clear();
            	
            	//Extrae la lista de procesos del grupo iterando
            	lstProcess = gDatos.getLstActiveGrupos().get(i).getLstProcess();
            	
            	
            	switch (vStatus) {
            		case "Sleeping":
                    	//Recorre cada procesos para generar el TaskProcess correspondiente
                    	int numProcess = lstProcess.size();
                    	for (int it=0; it<numProcess; it++) {
                    		process = new Process();
                    		process = lstProcess.get(it);
                    		
                        	//Crear el objeto dataClass TaskProcess correspondiente
                        	task = new TaskProcess();
                        	task.setSrvID("srv00001");
                        	task.setGrpID(vGroupID);
                        	task.setProcID(process.getProcID());
                        	task.setTypeProc(process.getType());
                        	task.setNumSecExec(vNumSecExec);
                        	task.setStatus("Pending");
                        	task.setInsTime(mylib.getDateNow());
                        	task.setUpdateTime(mylib.getDateNow());
                        	task.setParams(process.getParams());
                        	
                        	String keyMap = process.getProcID()+":"+vNumSecExec;
                        	gDatos.getMapTask().put(keyMap, task);
                        	
                        	//Marca Grupo en Estado Pending
                        	gDatos.updateStatusLstActiveGrupos(i, "Pending");
                        	
                    	} //fin loop de process

            			break;
            		default:
        				break;
            	}
            	
            } //fin de loop de grupos
            
            try {
				logger.info(mylib.serializeObjectToJSon(gDatos.getMapTask(), false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        } //fin run()
    } //fin mainTask() class
}
