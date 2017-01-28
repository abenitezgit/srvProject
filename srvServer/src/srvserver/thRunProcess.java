/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;
import dataClass.ActiveTypeProc;
import dataClass.AssignedTypeProc;
import dataClass.TaskProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utilities.globalAreaData;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thRunProcess extends Thread {
    static srvRutinas gSub;
    static globalAreaData gDatos;
    static Logger logger = Logger.getLogger("thRunProcess");
    
    //Carga constructor para inicializar los datos
    public thRunProcess(globalAreaData m) {
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
        
    @Override
    public void run() {
        Timer timerMain = new Timer("thSubRunProcess");
        timerMain.schedule(new mainKeepTask(), 5000, 15000);
    }
    
    
    static class mainKeepTask extends TimerTask {
        /*
        Crea las listas para asignaciones de Pesos especificos
        */
        List<pesoAssigned> lstPesoAssigned = new ArrayList<>();
        List<pesoActive> lstPesoActive = new ArrayList<>();

    
        public mainKeepTask() {
        }

        @Override
        public void run() {
            logger.info("Inicia thSubRunProcess");
            
            int numItemsTask = gDatos.getMapTask().size();
            
            logger.info("Se han encontrado: "+numItemsTask+ " procesos en lista mapTask()");
            
            if (numItemsTask>0) {
            	updateServiceStatistics();
            	updateBalanceStatistics();
                logger.info("Se han actualizado las estadisticas de ejecucion de procesos");
                
                int itemsAssigned = gDatos.getMapAssignedTypeProc().size();
                if (itemsAssigned>0) {
                    
                    /**
                     *Calcula los pesos especificos de las listas asignadas y activas
                     *Las lista a generarse son:
                     *lstPesoAssigned
                     *lstPesoActive
                    **/
                    //setPesoEspecifico();
                    //updatePoolStatistics();
                    

                    /**
                     * Visualiza lista de procesos Ready.
                     */
                    Map<String, TaskProcess> myMapTask = new TreeMap<>(gDatos.getMapTask());
                    
                    for (Map.Entry<String, TaskProcess> entry : myMapTask.entrySet()) {
                    	if (entry.getValue().getStatus().equals("Ready")) {
                    		switch (entry.getValue().getTypeProc()) {
	                            case "MOV":
	                                Thread thMOV = new thExecMOV(gDatos, entry.getValue());
	                                thMOV.setName("thExecMOV-"+entry.getKey());
	                                gDatos.setRunningTaskProcess(entry.getKey());
	                                thMOV.start();
	                                break;
	                            case "ETL":
	                                Thread thETL = new thExecETL(gDatos, entry.getValue());
	                                thETL.setName("thExecETL-"+ entry.getKey());
	                                gDatos.setRunningTaskProcess(entry.getKey());
	                                logger.info("Iniciando thread: thExecETL-"+ entry.getKey());
	                                thETL.start();
	                                break;
	                            default:
	                                logger.info("No hay Rutinas de Ejecucion asociadas a este tipo de proceso: "+entry.getKey());
	                                break;
	                        	} //end switch
                    		} //end if
                    	} //end for
                } else {
                    logger.info("No hay tipos de procesos Asignados");
                }
            } else {
                logger.info("No hay procesos Ready para ser ejecutados en Pool de Ejecucion");
            }
            
            logger.info("Finalizando Thread thRunProcess.");
            
        }
        
        @SuppressWarnings("unused")
		private void setPesoEspecifico() {
            /*
            Calcula peso especifico de lista de typeProc asignados
            */
//            long numTotalAssignedProc = gDatos.getServiceStatus().getLstAssignedTypeProc().size();
//            if (numTotalAssignedProc!=0) {
//                for (int i=0; i<numTotalAssignedProc; i++) {
//                    String typeProc = gDatos.getServiceStatus().getLstAssignedTypeProc().get(i).getTypeProc();
//                    int priority = gDatos.getServiceStatus().getLstAssignedTypeProc().get(i).getPriority();
//                    //float count = gDatos.getLstAssignedTypeProc().stream().filter(p -> p.getPriority()==priority).count();
//                    
//                    Stream<AssignedTypeProc> countd = gDatos.getServiceStatus().getLstAssignedTypeProc().stream().filter(p -> p.getPriority()==priority).distinct();
//                    float count = countd.count();
//                    
//                    float peso = count/numTotalAssignedProc*100/priority;
//                    pesoAssigned pesoAssigned = new pesoAssigned(typeProc, (int) peso);
//                    lstPesoAssigned.add(pesoAssigned);
//                }
//            }
            
            /*
            Calcula perso especifico de lista de ActiveProc 
            */
//            long numTotalActiveProc = gDatos.getServiceStatus().getLstActiveTypeProc().size();
//            if (numTotalActiveProc!=0) {
//                for (int i=0; i<numTotalActiveProc; i++) {
//                    String typeProc = gDatos.getServiceStatus().getLstActiveTypeProc().get(i).getTypeProc();
//                    int usedThread = gDatos.getServiceStatus().getLstActiveTypeProc().get(i).getUsedThread();
//                    float sumThread = gDatos.getServiceStatus().getLstActiveTypeProc().stream().collect(Collectors.summingInt(p -> p.getUsedThread()));
//                    float peso = usedThread/sumThread*100;
//                    pesoActive pesoActive = new pesoActive(typeProc, (int) peso);
//                    lstPesoActive.add(pesoActive);
//                }
//            }
            
            /*
            Ordena lista por peso de Mayor a menos
            */
            int numItems = lstPesoAssigned.size();
            if (numItems>1) {
                for (int i=0;i<numItems; i++ ) {
                    for (int j=0; j<numItems-1; j++) {
                        if (lstPesoAssigned.get(j).getPeso()<lstPesoAssigned.get(j+1).getPeso()) {
                            pesoAssigned pesoAssignedAux = new pesoAssigned(lstPesoAssigned.get(j).getTypeProc(), (int) lstPesoAssigned.get(j).getPeso());
                            lstPesoAssigned.set(j, lstPesoAssigned.get(j+1));
                            lstPesoAssigned.set(j+1, pesoAssignedAux);
                        }
                    }
                }
            }
        }
        
        private void updateBalanceStatistics() {
        	try {
        		Map<String, AssignedTypeProc> myAssigned = new TreeMap<>(gDatos.getMapAssignedTypeProc());
        		Map<String, ActiveTypeProc> myActive = new TreeMap<>(gDatos.getMapActiveTypeProc());
        		
        		int numTypeProc = myAssigned.size();
        		int numPriority = (int) myAssigned.entrySet().stream().map(map -> map.getValue().getPriority()).distinct().count();
        		
        		
        	} catch (Exception e) {
        		logger.error("Error en updateBalanceStatistics...: "+e.getMessage());
        	}
        }
        
        private void updateServiceStatistics() {
            /**
            * Actualiza Estadisticas de Procesos
            * Recorre el mapTask para revisar los estados de los precesos
            **/
        	int procRunning = 0;
        	int procReady = 0;
        	int procFinished = 0;
        	
        	Map<String, ActiveTypeProc> myActive = gDatos.getMapActiveTypeProc();
        	Map<String, TaskProcess> myTask = new TreeMap<>(gDatos.getMapTask());
        	
        	//Se limpia cada vez que se actualiza
        	myActive.clear();
        	
            for (Map.Entry<String, TaskProcess> task : myTask.entrySet()) {
            	
            	String vStatus = task.getValue().getStatus();
            	String vTypeProc = task.getValue().getTypeProc();
            	
            	switch (vStatus) {
            		case "Running":
            			procRunning++;
            			if (myActive.containsKey(vTypeProc))
            			{
            				ActiveTypeProc actTypeProc = new ActiveTypeProc();
            				actTypeProc = (ActiveTypeProc) myActive.get(vTypeProc);
            				
            				int valor = actTypeProc.getUsedThread();
            				actTypeProc.setUsedThread(valor+1);
            				
            				myActive.replace(vTypeProc, actTypeProc);
            			} else {
            				ActiveTypeProc actTypeProc = new ActiveTypeProc();
            				actTypeProc.setTypeProc(vTypeProc);
            				actTypeProc.setUsedThread(1);
            				
            				myActive.put(vTypeProc, actTypeProc);
            			}
            			break;
            		case "Ready":
            			procReady++;
            			break;
            		case "Finished":
            			procFinished++;
            			break;
            	}
            }
            
            //Actualiza los typeProc con valor cero cuando corresponda
            
            Map<String, AssignedTypeProc> myAssigned = new TreeMap<>(gDatos.getMapAssignedTypeProc());
            
            for (Map.Entry<String, AssignedTypeProc> mapAss : myAssigned.entrySet()) {
            	String vTypeProc = mapAss.getKey();
            	if (!myActive.containsKey(vTypeProc)) {
            		ActiveTypeProc activeType = new ActiveTypeProc();
            		activeType.setTypeProc(vTypeProc);
            		activeType.setUsedThread(0);
            		myActive.put(vTypeProc, activeType);
            	}
            }
        	
            //Actualiza Data Class
            
            gDatos.getServiceStatus().setNumProcRunning(procRunning);
            gDatos.getServiceStatus().setNumProcReady(procReady);
            gDatos.getServiceStatus().setNumProcFinished(procFinished);
            gDatos.setMapActiveTypeProc(myActive);
            
        }
        
        public int getIndexOfActiveTypeProc(List<ActiveTypeProc> lstActiveTypeProc, String typeProc) {
            int index=-1;
            try {
                if (!lstActiveTypeProc.isEmpty()) {
                    for (int i=0; i<lstActiveTypeProc.size(); i++) {
                        if (lstActiveTypeProc.get(i).getTypeProc().equals(typeProc)) {
                            index = i;
                            break;
                        }
                    }
                }
                return index;
            } catch (Exception e) {
                return -1;
            }
        }
        
        
        /**
         * Data Class Internas
         */
        
        class pesoAssigned {
            String typeProc;
            int peso;

            public pesoAssigned(String typeProc, int peso) {
                this.typeProc = typeProc;
                this.peso = peso;
            }

            public String getTypeProc() {
                return typeProc;
            }

            public void setTypeProc(String typeProc) {
                this.typeProc = typeProc;
            }

            public float getPeso() {
                return peso;
            }

            public void setPeso(int peso) {
                this.peso = peso;
            }
        }
        
        class pesoActive {
            String typeProc;
            int peso;

            public pesoActive(String typeProc, int peso) {
                this.typeProc = typeProc;
                this.peso = peso;
            }

            public String getTypeProc() {
                return typeProc;
            }

            public void setTypeProc(String typeProc) {
                this.typeProc = typeProc;
            }

            public float getPeso() {
                return peso;
            }

            public void setPeso(int peso) {
                this.peso = peso;
            }
        }
    }
}
