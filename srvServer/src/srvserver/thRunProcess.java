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
                updatePoolStatistics();
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
                    logger.info("Se han encontrado: "+gDatos.getServiceStatus().getNumProcSleeping()+ " procesos en estado Ready para ser ejecutados");
                    
                    Map<String, TaskProcess> myMapTask = new TreeMap<>(gDatos.getMapTask());
                    
                    for (Map.Entry<String, TaskProcess> entry : myMapTask.entrySet()) {
                    	if (entry.getValue().getStatus().equals("Ready")) {
                    		switch (entry.getValue().getTypeProc()) {
	                            case "OSP":
	                                Thread thOSP = new thExecOSP(gDatos, entry.getValue());
	                                thOSP.setName("thExecOSP-"+entry.getKey());
	                                thOSP.setName("OSPThread");
	                                gDatos.setRunningTaskProcess(entry.getKey());
	                                thOSP.start();
	                                break;
	                            case "OTX":
	//                                Thread thOTX = new thExecOTX(gDatos, lstReadyProc.get(i).getParams());
	//                                thOTX.setName("thExecOTX-"+lstReadyProc.get(i).getProcID());
	//                                gDatos.setRunningTaskProcess(entry.getKey());
	//                                thOTX.start();
	                                break;
	                            case "LOR":
	//                                Thread thLOR = new thExecLOR(gDatos, lstReadyProc.get(i).getParams());
	//                                thLOR.setName("thExecLOR-"+lstReadyProc.get(i).getProcID());
	//                                gDatos.setRunningTaskProcess(entry.getKey());
	//                                thLOR.start();
	                                break;
	                            case "FTP":
	                                Thread thFTP = new thExecFTP(gDatos, entry.getValue());
	                                thFTP.setName("thExecFTP-"+entry.getKey());
	                                gDatos.setRunningTaskProcess(entry.getKey());;
	                                thFTP.start();
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
            long numTotalAssignedProc = gDatos.getServiceStatus().getLstAssignedTypeProc().size();
            if (numTotalAssignedProc!=0) {
                for (int i=0; i<numTotalAssignedProc; i++) {
                    String typeProc = gDatos.getServiceStatus().getLstAssignedTypeProc().get(i).getTypeProc();
                    int priority = gDatos.getServiceStatus().getLstAssignedTypeProc().get(i).getPriority();
                    //float count = gDatos.getLstAssignedTypeProc().stream().filter(p -> p.getPriority()==priority).count();
                    
                    Stream<AssignedTypeProc> countd = gDatos.getServiceStatus().getLstAssignedTypeProc().stream().filter(p -> p.getPriority()==priority).distinct();
                    float count = countd.count();
                    
                    float peso = count/numTotalAssignedProc*100/priority;
                    pesoAssigned pesoAssigned = new pesoAssigned(typeProc, (int) peso);
                    lstPesoAssigned.add(pesoAssigned);
                }
            }
            
            /*
            Calcula perso especifico de lista de ActiveProc 
            */
            long numTotalActiveProc = gDatos.getServiceStatus().getLstActiveTypeProc().size();
            if (numTotalActiveProc!=0) {
                for (int i=0; i<numTotalActiveProc; i++) {
                    String typeProc = gDatos.getServiceStatus().getLstActiveTypeProc().get(i).getTypeProc();
                    int usedThread = gDatos.getServiceStatus().getLstActiveTypeProc().get(i).getUsedThread();
                    float sumThread = gDatos.getServiceStatus().getLstActiveTypeProc().stream().collect(Collectors.summingInt(p -> p.getUsedThread()));
                    float peso = usedThread/sumThread*100;
                    pesoActive pesoActive = new pesoActive(typeProc, (int) peso);
                    lstPesoActive.add(pesoActive);
                }
            }
            
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
        
        private void updatePoolStatistics() {
            /**
            * Actualiza Estadisticas de Procesos
            * Recorre el mapTask para revisar los estados de los precesos
            **/
        	int procRunning = 0;
        	int procReady = 0;
        	int procFinished = 0;
        	
            for (Map.Entry<String, TaskProcess> entry : gDatos.getMapTask().entrySet()) {
            	switch (entry.getValue().getStatus()) {
            		case "Running":
            			procRunning++;
            			if (gDatos.getMapActiveTypeProc().containsKey(entry.getValue().getTypeProc())) {
            				int valor = (int) gDatos.getMapActiveTypeProc().get(entry.getValue().getTypeProc());
            				valor++;
            				gDatos.getMapActiveTypeProc().replace(entry.getValue().getTypeProc(), valor);
            			} else {
            				int valor = 1;
            				gDatos.getMapActiveTypeProc().put(entry.getValue().getTypeProc(), valor);
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
        	
            gDatos.getServiceStatus().setNumProcRunning(procRunning);
            gDatos.getServiceStatus().setNumProcSleeping(procReady);
            gDatos.getServiceStatus().setNumProcFinished(procFinished);
            
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
