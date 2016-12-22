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
import dataClass.Grupo;
import dataClass.Interval;
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
    static MetaData metadata;
    
//Carga Clase log4
    static Logger logger = Logger.getLogger("thInscribeTask");   
    
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
        timerMain.schedule(new mainTask(), 10000, 20000);
        logger.info("Se ha agendado thSubInscribeTask cada 20 segundos");
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
	    		
	    		
	    		logger.info("Validando Conexion a MetaData...");
	    		try {
	    			/*
	    			 * El objeto MetaData queda abierto para ser cerrado al final de las operaciones
	    			 */
	    			metadata = new MetaData(gDatos);
	    			isOpenMetaData=true;
	    			logger.info("Conectado a Metadata OK");
	    			
	    		} catch (Exception e) {
	    			logger.error("Error de conexión a MetaData: "+e.getMessage());
	    			isOpenMetaData=false;
	    		}
	    		
	    		
	    		logger.info("Setea Datos del Calendar y Fechas ");
	    		setDataCalendar();
	    		
		        /*
		        Inicializa Lista de Agendas
		        */
		        gDatos.getLstShowAgendas().clear();     //Lista para el muestreo de agendas
		        gDatos.getLstActiveAgendas().clear();   //Lista para las agendas que deben activar grupos de procesos
		        
		        //Busca Agendas para Muestreo
		        getShowAgendas();
		        
		        //Busca Agendas para Activar Grupos
		        getActiveAgendas();
		        
		        /**
		         * Busca para todas las agendas activas los grupos y procesos asignados
		         */
		        
		        logger.info("Buscando Grupos de Procesos asociados a las agendas activas.");
		        
		        int numAgeActives = gDatos.getLstActiveAgendas().size();
		
		        if (numAgeActives>0) {
		        	Agenda agenda;
		        	
		        	//keyMap:  grpID (Id del Grupo)
		        	Map<String, Grupo> vMapGrupo;
		        	
			        //Para cada Agenda se buscaran los grupos y procesos correspondientes
		        	for (int i=0; i<numAgeActives; i++) {

		        		agenda = new Agenda();
			        	agenda = gDatos.getLstActiveAgendas().get(i);
			        	
			        	logger.info("Buscando Grupos asociados a Agenda: "+agenda.getAgeID()+ " numSec: "+agenda.getNumSecExec());
			        	
			        	try {
			        		//Recupera un Map local de Grupos para la agenda en curso
			        		//esta busqueda esta en un ciclo de agendas activas
			        		
			        		vMapGrupo = new TreeMap<>(genActiveGroup(agenda));
			        		
			        		//Para cada Grupo encontrado se inscribiran sus task
			        		for (Map.Entry<String, Grupo> entry : vMapGrupo.entrySet()) {
			        			inscribeGrupoExec(entry.getValue(), agenda.getNumSecExec());
			        		}
			        		
			        		gDatos.updateMapGroup(vMapGrupo);
			        		
			        	} catch (Exception e) {
			        		logger.error("Error generando Grupos Activos para agenda: "+agenda.getAgeID());
			        	}
			        }
		        	
		        } else { //end if
		        	logger.warn("No hay agendas para activar grupos");
		        }
	    		
		        //Cierra Conexiones
		        if (isOpenMetaData) {
		        	metadata.closeConnection();
		        }
	    		
	    		logger.info("Finalizando Thread thActiveGroups");
	    	} catch (Exception e) {
	    		logger.error("Error Thread thSubActiveGroups: "+e.getMessage());
	    		
	    		if (isOpenMetaData) {
	    			metadata.closeConnection();
	    			isOpenMetaData=false;
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
        
        private Map<String, Grupo> genActiveGroup(Agenda agenda) throws Exception{
        	try {
	        	Map<String, Grupo> vMapGrupo = new TreeMap<>();
	        	Grupo grupo;
	        	String vSQLGroup = metadata.getSqlFindGroup(agenda.getAgeID());
	        	
	        	logger.debug("Ejecutando Query: "+vSQLGroup);
	        	
	        	ResultSet rs = (ResultSet) metadata.getQuery(vSQLGroup);
	        	
	        	if (rs!=null) {
	        		while (rs.next()) {
	        			
	        			logger.info("Se encontro el Grupo:"+rs.getString("GRPID")+ " asociado a la agenda: "+agenda.getAgeID()+ " numSec: "+agenda.getNumSecExec());
	        			grupo = new Grupo();
	                    grupo.setGrpID(rs.getString("GRPID"));
	                    grupo.setGrpDESC(rs.getString("GRPDESC"));
	                    grupo.setCliID(rs.getString("CLIID"));
	                    grupo.setCliDesc(rs.getString("CLIDESC"));
	                    grupo.setHorDesc(rs.getString("HORDESC"));
	                    
	                    //Para cada grupo genera la lista de procesos
	                    grupo.setLstProcess(genListaProcess(grupo));
	                    
	                    //Para cada grupo genera la lista de dependencias
	                    grupo.setLstDepend(genListaDependences(grupo.getGrpID()));
	                    
	                    //Por cada grupo encontrado validar su incripcion de ejecución
	                    //
	                    //inscribeGrupoExec(grupo, agenda.getNumSecExec());
	                    
	                    String keyMap = grupo.getGrpID();
	                    
	                    //Para cada grupo encontrado siempre actualiza la lista de grupos
	                    vMapGrupo.put(keyMap, grupo);
	        		}
	        	} else {
	        		logger.info("No se encontraton grupos asociados a la agenda: "+agenda.getAgeID());
	        	}
	        	
	        	return vMapGrupo;
	        	
        	} catch (Exception e) {
        		logger.error("Error en genActiveGroupNew...: "+e.getMessage());
        		return null;
        	}
        	
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
        			logger.debug("Squery insGrpExec: "+vSqlIns);
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
            				depend.setProcHijo(rsDependences.getInt("PROCHIJO"));
            				depend.setProcPadre(rsDependences.getInt("PROCPADRE"));
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
                
                //Recupera Match de Campos del ETL
                //Recupera detalle de Match de Campos para este ETL
                logger.info("Recuperando Match de campos del ETL "+process.getProcID());
                vSQL = metadata.getSqlFindETLMatch(process.getProcID());
                
                ResultSet rsMatch = (ResultSet) metadata.getQuery(vSQL);
                if (rsMatch!=null) {
                	etl.setLstEtlMatch(getParseEtlMatch(rsMatch));
                }

                logger.info("Se recuperaron "+etl.getLstEtlMatch().size()+ " campos del Match ETL "+process.getProcID());
                
	    		return etl;
	    		
	    	} catch (Exception e) {
	    		logger.error("Error en findETLDetail: "+e.getMessage());
	    		return null;
	    	}
	    } //end ETL findEtlDetail()
	    
	    private Map<String, Interval> genNewIntervals(Process process) {
	    	try {
	    		//Objeto local de MapInterval
	    		Map<String, Interval> vMapInterval = new TreeMap<>();
	    		
	    		//Extrae objeto ETL
	    		ETL etl = new ETL();
	    		etl = (ETL) process.getParams();
	    		
		        //Extre Fecha Actual
		        Date today;
		        Date fecGap;
		        Date fecIni;
		        Date fecItera;
		        Date fecIntervalIni;
		        Date fecIntervalFin;
		
		        int MinItera;
		        int HoraItera;
		        int DiaItera;
		        int MesItera;
		        int AnoItera;
		
		        long numInterval;
		        String localIntervalID;
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
		
		                    keyMap = interval.getIntervalID();
		                    if (!vMapInterval.containsKey(keyMap)) {
		                    	vMapInterval.put(keyMap, interval);
		                    	logger.info("Se inscribió intrevalID: "+keyMap);
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
		
			                    keyMap = interval.getIntervalID();
			                    if (!vMapInterval.containsKey(keyMap)) {
			                    	vMapInterval.put(keyMap, interval);
			                    	logger.info("Se inscribió intrevalID: "+keyMap);
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

	    
	    private Map<String, Interval> getParseEtlInterval(Process process, ResultSet rs) throws Exception {

	    	Interval interval;
	    	Map<String, Interval> mapInterval = new TreeMap<>();
	    	
            while (rs.next()) {
            	interval = new Interval();
            	
                if (rs.getString("INTERVALID")!=null) {
                    interval.setIntervalID(rs.getString("INTERVALID"));
                }
                if (rs.getString("FECINS")!=null) {
                    interval.setFecIns(rs.getString("FECINS"));
                }
                if (rs.getString("FECUPDATE")!=null) {
                    interval.setFecUpdate(rs.getString("FECUPDATE"));
                }
                if (rs.getString("STATUS")!=null) {
                    interval.setStatus(rs.getString("STATUS"));
                }
                if (rs.getString("USTATUS")!=null) {
                    interval.setStatus(rs.getString("USTATUS"));
                }
                interval.setRowsLoad(rs.getInt("ROWSLOAD"));
                interval.setRowsRead(rs.getInt("ROWSREAD"));
                interval.setIntentos(rs.getInt("INTENTOS"));
                if (rs.getString("FECINI")!=null) {
                    interval.setFecIni(rs.getString("FECINI"));
                }
                if (rs.getString("FECFIN")!=null) {
                    interval.setFecFin(rs.getString("FECFIN"));
                }
                
                //Genera MapInterval con key: ETLID + INTERVALID
                String mapKey = process.getProcID()+":"+interval.getIntervalID();
                mapInterval.put(mapKey, interval);
            }
            logger.info("Se recuperaron "+mapInterval.size()+ " intervalos pendientes desde Metadata");
            
	    	return mapInterval;
	    }
	    
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
                if (rs.getString("LASTNUMSECEXEC")!=null) {
                    etl.setLASTNUMSECEXEC(rs.getString("LASTNUMSECEXEC")); 
                }
    		}
    		return etl;
    	}
    	
    	
    	
    } //class
}
