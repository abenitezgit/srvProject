/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;

import dataClass.Agenda;
import dataClass.Dependence;
import dataClass.ETL;
import dataClass.EtlMatch;
import dataClass.Ftp;
import dataClass.Grupo;
import dataClass.Interval;
import dataClass.MOV;
import dataClass.MovMatch;
import dataClass.Process;
import dataClass.TaskProcess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thInscribeTask extends Thread{
    static srvRutinas gSub;
    static globalAreaData gDatos;
    static metaData metadata;
    
//Carga Clase log4
    static Logger logger = Logger.getLogger("srv.inscribeTask");   
    
    public thInscribeTask(globalAreaData m) {
        try {
            gDatos = m;
            gSub = new srvRutinas(gDatos);
            
        } catch (Exception e) {
            logger.error("Error en Constructor: "+e.getMessage());
        }
    }
    
    @Override
    public void run() {
        Timer timerMain = new Timer("thSubInscribeTask");
        timerMain.schedule(new mainTask(), 10000, gDatos.getServerInfo().getTxpInscribe());
        logger.info("Se ha agendado thSubInscribeTask cada "+ gDatos.getServerInfo().getTxpInscribe() + " segundos");
    }
    
    
    static class mainTask extends TimerTask {
    	boolean isOpenMetaData=false;
    	int year;
    	int month;
    	int dayOfMonth;
    	int dayOfWeek;
    	int weekOfYear;
    	int weekOfMonth;
        String posmonth;
        String posdayOfMonth;
        String posdayOfWeek;
        String posweekOfYear;
        String posweekOfMonth;
        int findHour;
        int findMinutes;
        Calendar iteratorCalendar;
        SimpleTimeZone tz;
        String vSQL;
        String iteratorHour;
        String iteratorMinute;
        String posIteratorHour;
        String posIteratorMinute;
        String numSecExec;
        Agenda agenda;
        Grupo grupo;
        Process process;
        
        
        public mainTask() {
        	
        }
        
    
	    @Override
	    public void run() {
	    	try {
	    		logger.info("Iniciando Thread thInscribeTask");
	    		
	    		metadata = new metaData(gDatos);
	    		metadata.openConnection();
	    		
	    		logger.info("Setea Datos del Calendar y Fechas ");
	    		setDataCalendar();
	    		
		        /*
		        Inicializa Lista de Agendas
		        */
		        gDatos.getLstShowAgendas().clear();     //Lista para el muestreo de agendas
		        gDatos.getLstActiveAgendas().clear();   //Lista para las agendas que deben activar grupos de procesos
		        
		        //Busca Agendas para Muestreo
		        //Actualiza la lista global gDatos.getLstShowAgendas
		        getShowAgendas();
		        
		        //Busca Agendas para Activar Grupos
		        //Actualiza la lista global gDatos.getLstActiveAgendas
		        getActiveAgendas();
		        
		        /**
		         * Busca para todas las agendas activas los grupos y procesos asignados
		         */
		        
		        logger.info("Buscando Grupos de Procesos asociados a las agendas activas.");
		        
		        int numAgeActives = gDatos.getLstActiveAgendas().size();
		
		        if (numAgeActives>0) {
		        	Agenda agenda;

		        	//Para cada Agenda se buscaran los grupos y procesos correspondientes
		        	for (int i=0; i<numAgeActives; i++) {
		        		
		        		//Datos de la Agenda buscada
		        		agenda = new Agenda();
			        	agenda = gDatos.getLstActiveAgendas().get(i);
			        	
			        	Map<String, Grupo> vMapGrupo = new TreeMap<>();
			        	Grupo grupo;
			        	
			        	//Genera Query Consulta de Grupos
			        	String vSQLGroup = metadata.getSqlFindGroup(agenda.getAgeID());
			        	ResultSet rs = (ResultSet) metadata.getQuery(vSQLGroup);
			        	if (rs!=null) {
			        		//Para cada grupo enconrtado de la agenda activa
			        		while (rs.next()) {
			        			if (!isGrupoInscrito(rs.getString("GRPID"), agenda.getNumSecExec())) {
				        			logger.info("Se Inscribirá el Grupo:"+rs.getString("GRPID")+ " asociado a la agenda: "+agenda.getAgeID()+ " numSec: "+agenda.getNumSecExec());
				        			grupo = new Grupo();
				                    grupo.setGrpID(rs.getString("GRPID"));
				                    grupo.setGrpDESC(rs.getString("GRPDESC"));
				                    grupo.setCliID(rs.getString("CLIID"));
				                    grupo.setCliDesc(rs.getString("CLIDESC"));
				                    grupo.setHorDesc(rs.getString("HORDESC"));
				                    grupo.setNumSecExec(agenda.getNumSecExec());
				                    grupo.setMaxTimeExec(rs.getInt("MAXTIMEEXEC"));
				                    
				                    //Para cada grupo genera la lista de procesos
				                    grupo.setLstProcess(genListaProcess(grupo));
				                    
				                    //Para cada grupo genera la lista de dependencias
				                    grupo.setLstDepend(genListaDependences(grupo.getGrpID()));
				                    
				                    //Por cada grupo encontrado validar su incripcion de ejecución
				                    //
				                    inscribeGrupoExec(grupo, agenda.getNumSecExec());
				                    
				                    String keyMap = grupo.getGrpID();
				                    
				                    //Para cada grupo encontrado siempre actualiza la lista de grupos
				                    vMapGrupo.put(keyMap, grupo);
			        			}
			        		}
			        		rs.close();
			        		
			        		//Actualiza la Lista de Grupos encontrados
			        		gDatos.updateMapGroup(vMapGrupo);
			        		
			        	} //No hay grupos para esa agenda
		        	}
		        } else { //end if
		        	logger.warn("No hay agendas para activar grupos");
		        }
		        		    		
		        //Cierra Conexiones
		        if (metadata.isConnected()) {
		        	metadata.closeConnection();
		        }
	    		
	    		logger.info("Finalizando Thread thActiveGroups");
	    	} catch (Exception e) {
	    		logger.error("Error Thread thSubActiveGroups: "+e.getMessage());
	    		
	    		if (metadata.isConnected()) {
	    			metadata.closeConnection();
	    		}
	    	}
	    }
	    
        private void setDataCalendar() throws Exception {
	        //Setea Calendario en Base al TimeZone
	        //
	        String[] ids = TimeZone.getAvailableIDs(-4 * 60 * 60 * 1000);
	        String clt = ids[0];
	        tz = new SimpleTimeZone(-3 * 60 * 60 * 1000, clt);
	        tz.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
	        tz.setEndRule(Calendar.AUGUST, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
	        Calendar calendar = new GregorianCalendar(tz);
	
	        year       = calendar.get(Calendar.YEAR);
	        month      = calendar.get(Calendar.MONTH); // Jan = 0, dec = 11
	        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); 
	        dayOfWeek  = calendar.get(Calendar.DAY_OF_WEEK);
	        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
	        weekOfMonth= calendar.get(Calendar.WEEK_OF_MONTH);
	
	        findHour    = gDatos.getServerInfo().getAgeShowHour();  //Cantidad de Horas definidas para la muestra de las agendas
	        findMinutes = gDatos.getServerInfo().getAgeGapMinute(); //GAP en minutos para encontrar agendas que deberían haberse activado
	
	        //Genera las variables de Posicion a comparar con las guardadas en la base de datos
	        
	        //Ajusta Semana del Mes para que no retorne una semana 5
	        if (weekOfMonth==5) {
	            weekOfMonth = 4;
	        }
	        
	        posmonth = String.valueOf(month+1);
	        posdayOfMonth = String.valueOf(dayOfMonth);
	        posdayOfWeek = String.valueOf(dayOfWeek);
	        posweekOfYear = String.valueOf(weekOfYear);
	        posweekOfMonth = String.valueOf(weekOfMonth);
	        
	        numSecExec="0";
        }
        
        private void getShowAgendas() throws Exception {
	        /**
	         * Busca Todas las agenda en un rango de findHour (12) horas antes y después de la hora actual
	         * 
	         * Llena la lista global gDatos.lstShowAgendas()
	         * 
	         */
	        logger.info("Buscando Agendas para Monitoreo...");
	        
	        if (gDatos.getServerStatus().isIsValMetadataConnect()) {
		        for (int i=-findHour; i<=findHour; i++) {
		            iteratorCalendar = new GregorianCalendar(tz);
		            
		            //Posiciona el iteratorCalendar tantas horas atrás como definido en findHour
		            //Y extrae la hora correspondiente
		            //
		            iteratorCalendar.add(Calendar.HOUR_OF_DAY, i);
		            iteratorHour = String.valueOf(iteratorCalendar.get(Calendar.HOUR_OF_DAY));
		            posIteratorHour = String.valueOf(Integer.valueOf(iteratorHour)+1);
		            
		            vSQL = metadata.getSqlFindAgeShow(iteratorHour, posmonth, posdayOfMonth, posdayOfWeek, posweekOfYear, posweekOfMonth, posIteratorHour);		            
		            try {
		                try (ResultSet rs = (ResultSet) metadata.getQuery(vSQL)) {
		                    if (rs!=null) {
		                        while (rs.next()) {
		                            agenda = new Agenda();
		                            agenda.setHoraAgenda(rs.getString("horaAgenda"));
		                            agenda.setAgeID(rs.getString("ageID"));
		                            agenda.setMonth(rs.getString("month"));
		                            agenda.setDayOfMonth(rs.getString("dayOfMonth"));
		                            agenda.setWeekOfYear(rs.getString("weekOfYear"));
		                            agenda.setWeekOfMonth(rs.getString("weekOfMonth"));
		                            agenda.setHourOfDay(rs.getString("hourOfDay"));
		                            gDatos.getLstShowAgendas().add(agenda);
		                        }
		                        rs.close();
		                    } else {
		                        //Si para la hora buscada no encuentra agenda genera un objeto vacio
		                        //solo con el valor de la hora correspondiente
		                        //esto es para poder mostrar que en esa hora no hay agenda
		                        agenda = new Agenda();
		                        agenda.setHoraAgenda(iteratorHour);
		                        agenda.setAgeID("");
		                        agenda.setMonth("");
		                        agenda.setDayOfMonth("");
		                        agenda.setWeekOfYear("");
		                        agenda.setWeekOfMonth("");
		                        agenda.setHourOfDay("");
		                        gDatos.getLstShowAgendas().add(agenda);
		                    }
		                }
		            } catch (SQLException e) {
		                logger.error("Error buscando agendas en Metadata. err: "+e.getMessage());
		            }
		        } //end for
		        logger.info("Se encontraron: "+gDatos.getLstShowAgendas().size()+ " Agendas para Monitoreo..");
	        } else { //end if
	        	logger.error("No se ha podido validar conexion a Metdadata para recuperar agendas");
	        }
        }
        
        private void getActiveAgendas() throws Exception {
	        /**
	         * Busca Todas las agendas que deberían ser activadas en el periodo de findMinutes (5) minutos de holgura
	         * Actualiza la lista global de agendas activas gDatos.lstActiveAgendas
	         * 
	         * Llena la lista global gDatos.lstActiveAgendas()
	         * 
	         */
	        logger.info("Buscando Agendas para Activar Grupos...");
	
	        //Vuelve a inicializar las variables para realizar la busqueda de agendas que activaran grupos
	        //
	        iteratorCalendar = new GregorianCalendar(tz);
	        iteratorHour = String.valueOf(iteratorCalendar.get(Calendar.HOUR_OF_DAY));
	        posIteratorHour = String.valueOf(Integer.valueOf(iteratorHour)+1);
	        
	        if (gDatos.getServerStatus().isIsValMetadataConnect()) {
		        for (int i=-findMinutes; i<=0; i++) {
		            iteratorCalendar = new GregorianCalendar(tz);
		            iteratorCalendar.add(Calendar.MINUTE, i);
		            iteratorMinute = String.valueOf(iteratorCalendar.get(Calendar.MINUTE));
		            posIteratorMinute = String.valueOf(Integer.valueOf(iteratorMinute)+1);
		            
		            //Para cada agenda que se encuentra en el periodo buscado se le asignara un numero unico de secuencia de ejecucion
		            //Esto para no confundir las mismas agendas en periodos de ejecución buscados en el gap
		            //Este numero de ejecucuion identificara a cada procesos y grupo respectivo
		            //
		            numSecExec = String.format("%04d", year)+String.format("%02d", month+1)+String.format("%02d", dayOfMonth)+String.format("%02d", Integer.valueOf(iteratorHour))+String.format("%02d", Integer.valueOf(iteratorMinute));
		            
		            /**
		             * Busca Agendas correspondientes al minuto exacto del iterador del ciclo for
		             */
		            vSQL = metadata.getSqlFindAgeActive(iteratorMinute, posmonth, posdayOfMonth, posdayOfWeek, posweekOfYear, posweekOfMonth, posIteratorHour, posIteratorMinute);
		            try {
		            	logger.debug("Query Agenda: "+vSQL);
		                try (ResultSet rs = (ResultSet) metadata.getQuery(vSQL)) {
		                    if (rs!=null) {
		                        while (rs.next()) {
		                            agenda = new Agenda();
		                            agenda.setHoraAgenda(rs.getString("horaAgenda"));
		                            agenda.setAgeID(rs.getString("ageID"));
		                            agenda.setMonth(rs.getString("month"));
		                            agenda.setDayOfMonth(rs.getString("dayOfMonth"));
		                            agenda.setWeekOfYear(rs.getString("weekOfYear"));
		                            agenda.setWeekOfMonth(rs.getString("weekOfMonth"));
		                            agenda.setHourOfDay(rs.getString("hourOfDay"));
		                            agenda.setNumSecExec(numSecExec);
		                            gDatos.updateLstActiveAgendas(agenda);
		                        }
		                        rs.close();
		                    }
		                }
		            } catch (SQLException e) {
		                logger.error(e.getMessage());
		            }
		        } //end for
		        logger.info("Se han encontrado: "+gDatos.getLstActiveAgendas().size()+" Potenciales Agendas para activar Grupos.");
	        } else { //end if
	        	logger.error("No es posible validar conexion a Metadata para recuperar agendas y activar grupos");
	        }
        }
        
        private boolean isGrupoInscrito(String vGrpID, String vNumSecExec) throws Exception {
        	String vSQL = metadata.getSqlFindGrupoExec(vGrpID, vNumSecExec);
        	return metadata.ifExistRowKey(vSQL);
        }
        
        private void inscribeGrupoExec(Grupo grupo, String numSecExec) {
        	try {
        		/*
        		 * Valida si grupo-numSecExec ya está regsitrado en MD
        		 */
        		String vSQL = metadata.getSqlFindGrupoExec(grupo.getGrpID(), numSecExec);
        		if (!metadata.ifExistRowKey(vSQL)) {
        			
        			//Inscribe el grupoExec
        			String vSqlIns = metadata.getSqlInsGrupoExec(grupo.getGrpID(), numSecExec);
        			logger.debug("query insGrpExec: "+vSqlIns);
        			int result = metadata.executeQuery(vSqlIns);
        			
        			if (result==1) {
        				logger.info("Se inscribio grupo de Ejecucion: "+grupo.getGrpID()+":"+numSecExec);
        				
        				//Inscribiendo los TaskProcess del Grupo
        				//sin asignacion de servidor y en estado Pending
        				logger.info("Incribiendo los TaskProcess del Grupo: "+grupo.getGrpID()+":"+numSecExec);
        				
        				TaskProcess task;
                    	int numProcess = grupo.getLstProcess().size();
                    	for (int it=0; it<numProcess; it++) {
                    		process = new Process();
                    		process = grupo.getLstProcess().get(it);
                    		
                        	task = new TaskProcess();
                        	task.setGrpID(grupo.getGrpID());
                        	task.setProcID(process.getProcID());
                        	task.setTypeProc(process.getType());
                        	task.setNumSecExec(numSecExec);
                        	task.setSrvID("srv00000");
                        	task.setStatus("Pending");
                        	task.setInsTime(gSub.getDateNow());
                        	task.setUpdateTime(gSub.getDateNow());
                        	task.setParams(process.getParams());
                        	
                        	//Genera al Key del Map de este Task
                        	String keyMap = process.getProcID()+":"+numSecExec;
                        	gDatos.getMapTask().put(keyMap, task);
                        	logger.info("Se inscribe Task: "+gDatos.getMapTask().get(keyMap).getProcID()+":"+gDatos.getMapTask().get(keyMap).getNumSecExec()+ " Status: "+gDatos.getMapTask().get(keyMap).getStatus());
                        	
                    	} //fin loop de process
        				
        				
        			} else {
        				logger.error("Error al inscribir grupo de Ejecucion: "+grupo.getGrpID()+":"+numSecExec);
        			} 
        			
        		} else {
        			//Ya está inscrito en MD
        			//Por cada proceso del Grupo encontrado en MD valida que exista
        			//una TaskProcess para cada proceso del grupo.
        			//Esto es en caso que el srvMonitor se haya caido o reiniciado
    				//Inscribiendo los TaskProcess del Grupo
        			
        			//Primero recupera el status del MapExec para ver si no ha finalizado
        			//Se volverán a inscribir los taskProcess solo si el grupoExec esta en 
        			// status='Pending'
        			
        			//Construccion pendiente
        			
        		}
        		
        	} catch (Exception e) {
        		logger.error("Error en inscribeGrupoExec...: "+e.getMessage());
        	}
        }
        
        private List<Process> genListaProcess(Grupo grupo) {
        	try {
        		boolean isFindProcess = false;
        		
	        	Process process;
	        	List<Process> vLstProcess = new ArrayList<>();
	        	String vSQLProcess = metadata.getSqlFindProcess(grupo.getGrpID());
	        	
	        	ResultSet rs = (ResultSet) metadata.getQuery(vSQLProcess);
	        	
	        	if (rs!=null) {
	        		while (rs.next()) {
	        			isFindProcess=true;
	        			process = new Process();
	        			
	        			process.setProcID(rs.getString("procID"));
	        			process.setType(rs.getString("type"));
	        			process.setCritical(rs.getInt("critical"));
	        			process.setnOrder(rs.getInt("nOrder"));
	        			process.setNumSecExec(grupo.getNumSecExec());
	        			process.setParams(genDatalleProcess(process));
	        			
	        			vLstProcess.add(process);
	        		}
	        	}
	        	
	        	if (isFindProcess) {
	        		return vLstProcess;
	        	} else {
	        		String vMesg = "No se encontraron process asociados al grupo: "+grupo.getGrpID();
	        		logger.error(vMesg);
	        		return null;
	        	}
	        	
        	} catch (Exception e) {
        		String vMesg = "Error en genListaProcess";
        		vMesg = vMesg + "...: "+e.getMessage();
        		logger.error(vMesg);
        		return null;
        	}
        	
        	
        }
	    
	    private List<Dependence> genListaDependences(String vGrpID) {
            try {
            	boolean isFindDepend = false;
            	List<Dependence> vLstDepend = new ArrayList<>();
            	Dependence depend;
            	
            	logger.info("Buscando dependencias del grupo: "+vGrpID);
            	String query = metadata.getSqlFindDependences(vGrpID);
            	
            	ResultSet rsDependences = (ResultSet) metadata.getQuery(query);
            	if (rsDependences!=null) {
            			while (rsDependences.next()) {
            				isFindDepend = true;
            				
            				depend = new Dependence();
            				depend.setGrpID(rsDependences.getString("GRPID"));
            				depend.setProcHijo(rsDependences.getString("PROCHIJO"));
            				depend.setProcPadre(rsDependences.getString("PROCPADRE"));
            				depend.setCritical(rsDependences.getInt("CRITICAL"));
            				
            				vLstDepend.add(depend);
            			}
            	}
            	
            	if (isFindDepend) {
            		return vLstDepend;
            	} else {
            		logger.warn("No se encuentran Dependencias de procesos para el grupo: "+vGrpID);
            		return null;
            	}
            	
            } catch (Exception e) {
            	logger.error("Error recuperando Dependencias de procesos para el grupo: "+vGrpID+ " error: "+e.getMessage());
            	return null;
            }
	    } 
	    
	    private Object genDatalleProcess(Process process) {
            try {
            	Object param;
                switch (process.getType()) {
                    case "ETL":
                        logger.info("Buscando programacion de ETL "+ process.getProcID() + " en MetaData.");
                        param = findETLDetail(process);
                        break;
                    case "FTP":
                    	logger.info("Buscando programacion de FTP "+ process.getProcID() + " en MetaData.");
                    	param = findFtpDetail(process);
                    	break;
                    case "MOV":
                    	logger.info("Buscando programacion de MOV "+ process.getProcID() + " en MetaData.");
                    	param = findMovDetail(process);
                    	break;
                    default:
                    	logger.error("No existe definicion para tipo de proceso");
                    	param = null;
                }
                return param;
            } catch (Exception e) {
            	logger.error("Error recuperando detalle del proceso: "+process.getProcID());
            	return null;
            }
	    }
	    
	    private MOV findMovDetail(Process process) {
	    	try {
            	MOV mov = new MOV();
                
                //Recupera parametros globales del MOV
            	logger.info("Recuperando parmatros globales del MOV "+process.getProcID());
                vSQL = metadata.getSqlFindMOV(process.getProcID());
                
                ResultSet rsProc = (ResultSet) metadata.getQuery(vSQL);
                if (rsProc!=null) {
                	mov = getParseMovParam(rsProc);
                } else {
                	logger.info("No se encontró detalle de proceso para MOV: "+process.getProcID());
                }

                //Asocia numSecExec
                mov.setNUMSECEXEC(process.getNumSecExec());
                
                //Recupera Match de Campos del MOV
                //Recupera detalle de Match de Campos para este MOV
                logger.info("Recuperando Match de campos del MOV "+process.getProcID());
                vSQL = metadata.getSqlFindMOVMatch(process.getProcID());
                
                ResultSet rsMatch = (ResultSet) metadata.getQuery(vSQL);
                if (rsMatch!=null) {
                	mov.setLstMovMatch(getParseMOVMatch(rsMatch));
                }

                logger.info("Se recuperaron "+mov.getLstMovMatch().size()+ " campos del Match MOV "+process.getProcID());
                
	    	return mov;
	    		
	    	} catch (Exception e) {
	    		logger.error("Error en findMovDetail: "+e.getMessage());
	    		return null;
	    	}
	    } //end MOV findMovDetail()

	    
	    private ETL findETLDetail(Process process) {
	    	try {
            	ETL etl = new ETL();
                
                //Recupera parametros globales del ETL
            	logger.info("Recuperando parmatros globales del ETL "+process.getProcID());
                vSQL = metadata.getSqlFindETL(process.getProcID());
                
                ResultSet rsProc = (ResultSet) metadata.getQuery(vSQL);
                if (rsProc!=null) {
                	etl = getParseEtlParam(rsProc);
                }
                
                
                //Asocia numSecExec
                etl.setNUMSECEXEC(process.getNumSecExec());
                
                //Recupera Match de Campos del ETL
                //Recupera detalle de Match de Campos para este ETL
                logger.info("Recuperando Match de campos del ETL "+process.getProcID());
                vSQL = metadata.getSqlFindETLMatch(process.getProcID());
                
                ResultSet rsMatch = (ResultSet) metadata.getQuery(vSQL);
                if (rsMatch!=null) {
                	etl.setLstEtlMatch(getParseEtlMatch(rsMatch));
                }

                logger.info("Se recuperaron "+etl.getLstEtlMatch().size()+ " campos del Match ETL "+process.getProcID());
                
                /**
                 * Procesando Intervalos del ETL
                 */
                
                //Recupera intervalos pendientes de ejecución para el proceso de ETL correspondiente
                Map<String, Interval> vMapInterval = new TreeMap<>();
                Interval interval;
                
                //Recupera Intervalos desde MD
                vSQL = metadata.getSqlFindIntervalReady(etl.getETLID());
                ResultSet rs = (ResultSet) metadata.getQuery(vSQL);
                if (rs!=null) {
                	while (rs.next()) {
                		if (rs.getString("NUMSECEXEC")!=null) {
	                		if (!isExistTaskForProcess(etl.getETLID(), rs.getString("NUMSECEXEC"))) {
	                			interval = new Interval();
	                			interval = getParseInterval(rs);
	                			//interval.setNumSecExec(etl.getNUMSECEXEC());
	                			interval.setStatus("Finished");
	                			interval.setuStatus("Abort");
	                			interval.setFecUpdate(gSub.getDateNow());
	                			String keyMap = interval.getIntervalID();
	                			vMapInterval.put(keyMap, interval);
	                		}
                		} else {
                			interval = new Interval();
                			interval = getParseInterval(rs);
                			interval.setNumSecExec(etl.getNUMSECEXEC());
                			String keyMap = interval.getIntervalID();
                			vMapInterval.put(keyMap, interval);
                		}
                	}
                	rs.close();
                }
                
                
                //Genera nuevos intervalos de extraccion
               Map<String, Interval> newMapInterval = new TreeMap<>();
               newMapInterval = genNewIntervals(etl);
               
               //Adiciona Intervalos Nuevos a vMapInterval
               for (Map.Entry<String, Interval> entry : newMapInterval.entrySet()) {
            	   if (!vMapInterval.containsKey(entry.getKey())&&!metadata.isExistIntervalMD(etl.getETLID(), entry.getKey())) {
            		   if (!isExistTaskForInterval(etl.getETLID(), entry.getKey())) {
            			   vMapInterval.put(entry.getKey(), entry.getValue());
            		   }
            	   }
               }
               
               etl.setMapInterval(vMapInterval);
                
	    	return etl;
	    		
	    	} catch (Exception e) {
	    		logger.error("Error en findETLDetail: "+e.getMessage());
	    		return null;
	    	}
	    } //end ETL findEtlDetail()
	    
	    private boolean isExistTaskForInterval(String etlID, String intervalID) {
	    	try {
		    	boolean isExist = false;
		    		Map<String, TaskProcess> vMapTask = new TreeMap<>(gDatos.getMapTask());
			    	for (Map.Entry<String, TaskProcess> entry : vMapTask.entrySet()) {
			    		String etlString = gSub.serializeObjectToJSon(entry.getValue().getParams(), false);
			    		ETL etl = new ETL();
			    		etl = (ETL) gSub.serializeJSonStringToObject(etlString, ETL.class);
			    		Map<String, Interval> vMapInterval = new TreeMap<>();
			    		vMapInterval = etl.getMapInterval();
			    		for (Map.Entry<String, Interval> entryInt : vMapInterval.entrySet()) {
			    			if (entryInt.getKey().equals(intervalID)) {
			    				isExist = true;
			    				break;
			    			}
			    		}
			    	}
		    	return isExist;
	    	} catch (Exception e) {
	    		logger.error("Error en isExistTaskForInterval...: "+e.getMessage());
	    		return false;
	    	}
	    }
	    
	    private boolean isExistTaskForProcess(String etlID, String numSecExec) throws Exception{
	    	boolean isFind=false;
	    	Map<String, TaskProcess> vMapTask = new TreeMap<>(gDatos.getMapTask());
	    	for (Map.Entry<String, TaskProcess> entry : vMapTask.entrySet()) {
	    		if (entry.getValue().getProcID().equals(etlID)&&entry.getValue().getNumSecExec().equals(numSecExec)) {
	    			isFind=true;
	    			break;
	    		}
	    	}
	    	return isFind;
	    }
	    
	    private Map<String, Interval> genNewIntervals(ETL etl) {
	    	try {
	    		//Objeto local de MapInterval
	    		Map<String, Interval> vMapInterval = new TreeMap<>();
	    			    		
		        //Extre Fecha Actual
		        Date today;
		        Date fecGap;
		        Date fecIni;
		        Date fecItera;
		        Date fecIntervalIni;
		        Date fecIntervalFin;
		
		        @SuppressWarnings("unused")
				int MinItera;
		        int HoraItera;
		        int DiaItera;
		        int MesItera;
		        int AnoItera;
		
		        long numInterval;
		        String localIntervalID;
		        @SuppressWarnings("unused")
				String todayChar;


		        //Setea Fecha Actual
		        //
		        today = new Date();
		        
		        //Variables del Objeto ETL
		        //
		        int vTimeGap = etl.getTIMEGAP();
		        int vTimePeriod = etl.getTIMEPERIOD();
		        int vTimeGen = etl.getTIMEGEN();
		        String vUnitMeasure = etl.getUNITMEASURE();
		        String vETLID = etl.getETLID();
	            
		        //Setea Fecha GAP - Desface de tiempo en extraccion
		        //
		        Calendar c = Calendar.getInstance();
		        c.add(Calendar.MINUTE, -(vTimeGap+vTimePeriod));
		        fecGap = c.getTime();

		        //Setea Fecha Inicio Inscripcion/Revision de Intervalos
		        //
		
		        c.setTime(today);
		        c.add(Calendar.MINUTE, -vTimeGen);
		        fecIni = c.getTime();

		        logger.info("Datos del ETLID: "+vETLID);
		        logger.info("Fecha Actual: "+ today);
		        logger.info("Fecha GAP   : "+ fecGap);
		        logger.info("Fecha IniIns: "+ fecIni);
	        
		        fecItera = fecIni;
		        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		        SimpleDateFormat sdfToday = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        String IntervalIni;
		        String IntervalFin;
		        Interval interval;
		        String keyMap;
	        
		        while (fecItera.compareTo(fecGap) < 0) {
		            //Crea Objecto Interval
		            //
		            interval = new Interval();
		
		            //Extrae Intervalo para Fecha fecItera
		            //
		            c.setTime(fecItera);
		            AnoItera = c.get(Calendar.YEAR);
		            MesItera = c.get(Calendar.MONTH);
		            DiaItera = c.get(Calendar.DAY_OF_MONTH);
		            HoraItera = c.get(Calendar.HOUR_OF_DAY);
		            MinItera = c.get(Calendar.MINUTE);
		
		            //Valida si el intervalo de extraccion (cETL_INTERVALUNIDAD) es por:
		            //  Minutos     : 0
		            //  Horas       : 1
		            //  Dias        : 2
		            //  Semanas     : 3
		            //  Mensuales   : 4
		            //  Anuales     : 5
		
		            switch (vUnitMeasure) {
		                case "MINUTE":
		                    fecIntervalIni = null;
		                    fecIntervalFin = null;
		                    numInterval = 60/vTimePeriod;
		                    for (int i=1;i<=numInterval;i++) {
		                        c.set(AnoItera, MesItera, DiaItera, HoraItera, (i)*vTimePeriod,0);
		                        fecIntervalFin = c.getTime();
		                        if (fecIntervalFin.compareTo(fecItera) >0 ) {
		                            c.set(AnoItera, MesItera, DiaItera, HoraItera, (i-1)*vTimePeriod,0);
		                            fecIntervalIni = c.getTime();
		                            break;
		                        }
		                    }
		                    c.setTime(fecItera);
		                    c.add(Calendar.MINUTE, vTimePeriod);
		                    fecItera = c.getTime();
		
		
		                    IntervalIni = sdf.format(fecIntervalIni);
		                    IntervalFin = sdf.format(fecIntervalFin);
		                    localIntervalID = IntervalIni+'-'+IntervalFin;
		
		                    interval.setIntervalID(localIntervalID);
		                    interval.setStatus("Ready");
		                    interval.setFecIns(sdfToday.format(today));
		                    interval.setFecUpdate(sdfToday.format(today));
		                    interval.setFecIni(IntervalIni);
		                    interval.setFecFin(IntervalFin);
		                    interval.setNumSecExec(etl.getNUMSECEXEC());
		
		                    keyMap = interval.getIntervalID();
		                    if (!vMapInterval.containsKey(keyMap)) {
		                    	vMapInterval.put(keyMap, interval);
		                    	logger.info("Se inscribió intrevalID: "+keyMap+ " para el ETL: "+ etl.getETLID());
		                    }
		
		                    break;
		
		                    case "HOUR":
		                        fecIntervalIni = null;
		                        fecIntervalFin = null;
		                        numInterval = 24/vTimePeriod;
		                        for (int i=1;i<=numInterval;i++) {
		                            c.set(AnoItera, MesItera, DiaItera, (i)*vTimePeriod, 0, 0);
		                            fecIntervalFin = c.getTime();
		                            if (fecIntervalFin.compareTo(fecItera) >0 ) {
		                                c.set(AnoItera, MesItera, DiaItera, (i-1)*vTimePeriod, 0, 0);
		                                fecIntervalIni = c.getTime();
		                                break;
		                            }
		                        }
		                        c.setTime(fecItera);
		                        c.add(Calendar.HOUR_OF_DAY, vTimePeriod);
		                        fecItera = c.getTime();
		
		                        IntervalIni = sdf.format(fecIntervalIni);
		                        IntervalFin = sdf.format(fecIntervalFin);
		                        localIntervalID = IntervalIni+'-'+IntervalFin;                    
		
		                        interval.setIntervalID(localIntervalID);
		                        interval.setStatus("Ready");
		                        interval.setFecIns(sdfToday.format(today));
		                        interval.setFecUpdate(sdfToday.format(today));
		                        interval.setFecIni(IntervalIni);
		                        interval.setFecFin(IntervalFin);
		                        interval.setNumSecExec(etl.getNUMSECEXEC());
		
			                    keyMap = interval.getIntervalID();
			                    if (!vMapInterval.containsKey(keyMap)) {
			                    	vMapInterval.put(keyMap, interval);
			                    	logger.info("Se inscribió intrevalID: "+keyMap+ " para el ETL: "+ etl.getETLID());
			                    }
		
		                        break;
		
		                    case "2":
		                    case "3":
		                    case "4":
		                    case "5":
		                    default:
		                } //end switch
		            } //end while
		        return vMapInterval;
	    	} catch (Exception e) {
	    		logger.error("Error generando intervalos faltantes..."+e.getMessage());
	    		return null;
	    	}
	    }

	    
	    private Ftp findFtpDetail(Process process) {
	    	try {
            	Ftp ftp = new Ftp();
                
                //Recupera parametros globales del FTP
            	logger.info("Recuperando parmatros globales del FTP "+process.getProcID());
                vSQL = metadata.getSqlFindFTP(process.getProcID());
                
                ResultSet rsFtp = (ResultSet) metadata.getQuery(vSQL);
                if (rsFtp!=null) {
                	ftp = getParseFtpParam(rsFtp);
                }
                
	    		return ftp;
	    		
	    	} catch (Exception e) {
	    		logger.error("Error en findFtpDetail: "+e.getMessage());
	    		return null;
	    	}
	    } //end FTP findFtpDetail()

	    	    
	    private List<EtlMatch> getParseEtlMatch(ResultSet rs) throws Exception{
	    	EtlMatch etlMatch;
	    	List<EtlMatch> lstEtlMatch = new ArrayList<>();
	    	
            while (rs.next()) {
                etlMatch = new EtlMatch();
                
                if (rs.getString("ETLORDER")!=null) {
                    etlMatch.setEtlOrder(rs.getInt("ETLORDER"));
                }
                if (rs.getString("ETLSOURCEFIELD")!=null) {
                    etlMatch.setEtlSourceField(rs.getString("ETLSOURCEFIELD"));
                }
                if (rs.getString("ETLSOURCELENGTH")!=null) {
                    etlMatch.setEtlSourceLength(rs.getInt("ETLSOURCELENGTH"));
                }
                if (rs.getString("ETLSOURCETYPE")!=null) {
                    etlMatch.setEtlSourceType(rs.getString("ETLSOURCETYPE"));
                }
                if (rs.getString("ETLDESTFIELD")!=null) {
                    etlMatch.setEtlDestField(rs.getString("ETLDESTFIELD"));
                }
                if (rs.getString("ETLDESTLENGTH")!=null) {
                    etlMatch.setEtlDestLength(rs.getInt("ETLDESTLENGTH"));
                }
                if (rs.getString("ETLDESTTYPE")!=null) {
                    etlMatch.setEtlDestType(rs.getString("ETLDESTTYPE"));
                }
                lstEtlMatch.add(etlMatch);
            }
	    	
	    	return lstEtlMatch;
	    }

	    private List<MovMatch> getParseMOVMatch(ResultSet rs) throws Exception{
	    	MovMatch movMatch;
	    	List<MovMatch> lstMovMatch = new ArrayList<>();
	    	
            while (rs.next()) {
            	movMatch = new MovMatch();
                
                if (rs.getString("MOVORDER")!=null) {
                	movMatch.setMovOrder(rs.getInt("MOVORDER"));
                }
                if (rs.getString("SOURCEFIELD")!=null) {
                	movMatch.setSourceField(rs.getString("SOURCEFIELD"));
                }
                if (rs.getString("SOURCELENGTH")!=null) {
                    movMatch.setSourceLength(rs.getInt("SOURCELENGTH"));
                }
                if (rs.getString("SOURCETYPE")!=null) {
                    movMatch.setSourceType(rs.getString("SOURCETYPE"));
                }
                if (rs.getString("DESTFIELD")!=null) {
                    movMatch.setDestField(rs.getString("DESTFIELD"));
                }
                if (rs.getString("DESTLENGTH")!=null) {
                    movMatch.setDestLength(rs.getInt("DESTLENGTH"));
                }
                if (rs.getString("DESTTYPE")!=null) {
                    movMatch.setDestType(rs.getString("DESTTYPE"));
                }
                lstMovMatch.add(movMatch);
            }
	    	
	    	return lstMovMatch;
	    }

	    private Ftp getParseFtpParam(ResultSet rs) throws Exception {
	    	Ftp ftp = new Ftp();
	    	if (rs.next()) {
	    		if (rs.getString("FTPID")!=null) {
	    			ftp.setFtpID(rs.getString("FTPID"));
	    		}
	    		if (rs.getString("FTPDESC")!=null) {
	    			ftp.setFtpDesc(rs.getString("FTPDESC"));
	    		}
	    		if (rs.getString("srvSourceID")!=null) {
	    			ftp.setSrvSourceID(rs.getString("srvSourceID"));
	    		}
	    		if (rs.getString("srvDestID")!=null) {
	    			ftp.setSrvDestID(rs.getString("srvDestID"));
	    		}
	    		if (rs.getString("patternFind")!=null) {
	    			ftp.setPatternFind(rs.getString("patternFind"));
	    		}
	    		if (rs.getString("usePatternFind")!=null) {
	    			ftp.setUsePatternFind(rs.getInt("usePatternFind"));
	    		}
	    		if (rs.getString("fileSourceName")!=null) {
	    			ftp.setFileSourceName(rs.getString("fileSourceName"));
	    		}
	    		if (rs.getString("fileDestName")!=null) {
	    			ftp.setFileDestName(rs.getString("fileDestName"));
	    		}
	    		if (rs.getString("userSourceID")!=null) {
	    			ftp.setUserSourceID(rs.getString("userSourceID"));
	    		}
	    		if (rs.getString("userDestID")!=null) {
	    			ftp.setUserDestID(rs.getString("userDestID"));
	    		}
	    		if (rs.getString("pathSource")!=null) {
	    			ftp.setPathSource(rs.getString("pathSource"));
	    		}
	    		if (rs.getString("pathDest")!=null) {
	    			ftp.setPathDest(rs.getString("pathDest"));
	    		}
	    		if (rs.getString("ftpType")!=null) {
	    			ftp.setFtpType(rs.getString("ftpType"));
	    		}
	    		if (rs.getString("ftpEnable")!=null) {
	    			ftp.setFtpEnable(rs.getInt("ftpEnable"));
	    		}
	    	}
	    	return ftp;
	    }
	    
	    private Interval getParseInterval(ResultSet rs) throws Exception {
	    	Interval interval = new Interval();
	    	interval.setFecFin(rs.getString("FECFIN"));
	    	interval.setFecIni(rs.getString("FECINI"));
	    	interval.setFecIns(rs.getString("FECINS"));
	    	interval.setFecUpdate(rs.getString("FECUPDATE"));
	    	interval.setIntentos(rs.getInt("INTENTOS"));
	    	interval.setIntervalID(rs.getString("INTERVALID"));
	    	interval.setRowsLoad(rs.getInt("ROWSLOAD"));
	    	interval.setRowsRead(rs.getInt("ROWSREAD"));
	    	interval.setStatus(rs.getString("STATUS"));
	    	interval.setuStatus(rs.getString("USTATUS"));
	    	interval.setNumSecExec(rs.getString("NUMSECEXEC"));
	    	return interval;
	    }
    
    	private ETL getParseEtlParam(ResultSet rs) throws Exception {
    		ETL etl = new ETL();
    		if (rs.next()) {
                if (rs.getString("ETLID")!=null) {
                    etl.setETLID(rs.getString("ETLID")); 
                }
                if (rs.getString("ETLDESC")!=null) {
                    etl.setETLDesc(rs.getString("ETLDESC")); 
                }
                if (rs.getString("ETLENABLE")!=null) {
                    etl.setETLEnable(rs.getInt("ETLENABLE")); 
                }
                if (rs.getString("CLIDESC")!=null) {
                    etl.setCliDesc(rs.getString("CLIDESC")); 
                }
                if (rs.getString("FIELDKEY")!=null) {
                    etl.setFIELDKEY(rs.getString("FIELDKEY")); 
                }
                if (rs.getString("FIELDTYPE")!=null) {
                    etl.setFIELDTYPE(rs.getString("FIELDTYPE")); 
                }
                if (rs.getString("TIMEGAP")!=null) {
                    etl.setTIMEGAP(rs.getInt("TIMEGAP")); 
                }
                if (rs.getString("TIMEGEN")!=null) {
                    etl.setTIMEGEN(rs.getInt("TIMEGEN")); 
                }
                if (rs.getString("TIMEPERIOD")!=null) {
                    etl.setTIMEPERIOD(rs.getInt("TIMEPERIOD")); 
                }
                if (rs.getString("UNITMEASURE")!=null) {
                    etl.setUNITMEASURE(rs.getString("UNITMEASURE")); 
                }
                if (rs.getString("WHEREACTIVE")!=null) {
                    etl.setWHEREACTIVE(rs.getInt("WHEREACTIVE")); 
                }
                if (rs.getString("QUERYBODY")!=null) {
                    etl.setQUERYBODY(rs.getString("QUERYBODY")); 
                }
                if (rs.getString("STBNAME")!=null) {
                    etl.setSTBNAME(rs.getString("STBNAME")); 
                }
                if (rs.getString("DTBNAME")!=null) {
                    etl.setDTBNAME(rs.getString("DTBNAME")); 
                }
                if (rs.getString("SIP")!=null) {
                    etl.setSIP(rs.getString("SIP")); 
                }
                if (rs.getString("SDBNAME")!=null) {
                    etl.setSDBNAME(rs.getString("SDBNAME")); 
                }
                if (rs.getString("SDBDESC")!=null) {
                    etl.setSDBDESC(rs.getString("SDBDESC")); 
                }
                if (rs.getString("SDBTYPE")!=null) {
                    etl.setSDBTYPE(rs.getString("SDBTYPE")); 
                }
                if (rs.getString("SDBPORT")!=null) {
                    etl.setSDBPORT(rs.getString("SDBPORT")); 
                }
                if (rs.getString("SDBINSTANCE")!=null) {
                    etl.setSDBINSTANCE(rs.getString("SDBINSTANCE")); 
                }
                if (rs.getString("SDBCONF")!=null) {
                    etl.setSDBCONF(rs.getString("SDBCONF")); 
                }
                if (rs.getString("SDBJDBC")!=null) {
                    etl.setSDBJDBC(rs.getString("SDBJDBC")); 
                }
                if (rs.getString("SUSERNAME")!=null) {
                    etl.setSUSERNAME(rs.getString("SUSERNAME")); 
                }
                if (rs.getString("SUSERPASS")!=null) {
                    etl.setSUSERPASS(rs.getString("SUSERPASS")); 
                }
                if (rs.getString("SUSERTYPE")!=null) {
                    etl.setSUSERTYPE(rs.getString("SUSERTYPE")); 
                }
                if (rs.getString("DIP")!=null) {
                    etl.setDIP(rs.getString("DIP")); 
                }
                if (rs.getString("DDBDESC")!=null) {
                    etl.setDDBDESC(rs.getString("DDBDESC")); 
                }
                if (rs.getString("DDBNAME")!=null) {
                    etl.setDDBNAME(rs.getString("DDBNAME")); 
                }
                if (rs.getString("DDBTYPE")!=null) {
                    etl.setDDBTYPE(rs.getString("DDBTYPE")); 
                }
                if (rs.getString("DDBPORT")!=null) {
                    etl.setDDBPORT(rs.getString("DDBPORT")); 
                }
                if (rs.getString("DDBINSTANCE")!=null) {
                    etl.setDDBINSTANCE(rs.getString("DDBINSTANCE")); 
                }
                if (rs.getString("DDBCONF")!=null) {
                    etl.setDDBCONF(rs.getString("DDBCONF")); 
                }
                if (rs.getString("DDBJDBC")!=null) {
                    etl.setDDBJDBC(rs.getString("DDBJDBC")); 
                }
                if (rs.getString("DUSERNAME")!=null) {
                    etl.setDUSERNAME(rs.getString("DUSERNAME")); 
                }
                if (rs.getString("DUSERPASS")!=null) {
                    etl.setDUSERPASS(rs.getString("DUSERPASS")); 
                }
                if (rs.getString("DUSERTYPE")!=null) {
                    etl.setDUSERTYPE(rs.getString("DUSERTYPE")); 
                }
    		}
    		return etl;
    	}
    	
    	private MOV getParseMovParam(ResultSet rs) throws Exception {
    		MOV mov = new MOV();
    		if (rs.next()) {
                if (rs.getString("MOVID")!=null) {
                    mov.setMovID(rs.getString("MOVID")); 
                }
                if (rs.getString("MOVDESC")!=null) {
                    mov.setMovDesc(rs.getString("MOVDESC")); 
                }
                if (rs.getString("MOVENABLE")!=null) {
                    mov.setEnable(rs.getInt("MOVENABLE")); 
                }
                if (rs.getString("CLIDESC")!=null) {
                    mov.setCliDesc(rs.getString("CLIDESC")); 
                }
                if (rs.getString("WHEREACTIVE")!=null) {
                    mov.setWHEREACTIVE(rs.getInt("WHEREACTIVE")); 
                }
                if (rs.getString("QUERYBODY")!=null) {
                    mov.setQUERYBODY(rs.getString("QUERYBODY")); 
                }
                if (rs.getString("STBNAME")!=null) {
                    mov.setSTBNAME(rs.getString("STBNAME")); 
                }
                if (rs.getString("DTBNAME")!=null) {
                	mov.setDTBNAME(rs.getString("DTBNAME")); 
                }
                if (rs.getString("SIP")!=null) {
                	mov.setSIP(rs.getString("SIP")); 
                }
                if (rs.getString("SDBNAME")!=null) {
                	mov.setSDBNAME(rs.getString("SDBNAME")); 
                }
                if (rs.getString("SDBDESC")!=null) {
                	mov.setSDBDESC(rs.getString("SDBDESC")); 
                }
                if (rs.getString("SDBTYPE")!=null) {
                	mov.setSDBTYPE(rs.getString("SDBTYPE")); 
                }
                if (rs.getString("SDBPORT")!=null) {
                	mov.setSDBPORT(rs.getString("SDBPORT")); 
                }
                if (rs.getString("SDBINSTANCE")!=null) {
                	mov.setSDBINSTANCE(rs.getString("SDBINSTANCE")); 
                }
                if (rs.getString("SDBCONF")!=null) {
                	mov.setSDBCONF(rs.getString("SDBCONF")); 
                }
                if (rs.getString("SDBJDBC")!=null) {
                	mov.setSDBJDBC(rs.getString("SDBJDBC")); 
                }
                if (rs.getString("SUSERNAME")!=null) {
                	mov.setSUSERNAME(rs.getString("SUSERNAME")); 
                }
                if (rs.getString("SUSERPASS")!=null) {
                	mov.setSUSERPASS(rs.getString("SUSERPASS")); 
                }
                if (rs.getString("SUSERTYPE")!=null) {
                	mov.setSUSERTYPE(rs.getString("SUSERTYPE")); 
                }
                if (rs.getString("DIP")!=null) {
                	mov.setDIP(rs.getString("DIP")); 
                }
                if (rs.getString("DDBDESC")!=null) {
                	mov.setDDBDESC(rs.getString("DDBDESC")); 
                }
                if (rs.getString("DDBNAME")!=null) {
                	mov.setDDBNAME(rs.getString("DDBNAME")); 
                }
                if (rs.getString("DDBTYPE")!=null) {
                	mov.setDDBTYPE(rs.getString("DDBTYPE")); 
                }
                if (rs.getString("DDBPORT")!=null) {
                	mov.setDDBPORT(rs.getString("DDBPORT")); 
                }
                if (rs.getString("DDBINSTANCE")!=null) {
                	mov.setDDBINSTANCE(rs.getString("DDBINSTANCE")); 
                }
                if (rs.getString("DDBCONF")!=null) {
                	mov.setDDBCONF(rs.getString("DDBCONF")); 
                }
                if (rs.getString("DDBJDBC")!=null) {
                	mov.setDDBJDBC(rs.getString("DDBJDBC")); 
                }
                if (rs.getString("DUSERNAME")!=null) {
                	mov.setDUSERNAME(rs.getString("DUSERNAME")); 
                }
                if (rs.getString("DUSERPASS")!=null) {
                	mov.setDUSERPASS(rs.getString("DUSERPASS")); 
                }
                if (rs.getString("DUSERTYPE")!=null) {
                	mov.setDUSERTYPE(rs.getString("DUSERTYPE")); 
                }
    		}
    		return mov;
    	}

    	
    	
    } //class
}
