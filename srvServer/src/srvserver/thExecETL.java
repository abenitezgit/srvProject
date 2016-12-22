/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;

import dataClass.ETL;
import dataClass.Interval;
import dataClass.TaskProcess;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import utilities.dataAccess;
import utilities.globalAreaData;
import utilities.srvRutinas;

/**
 *
 * @author andresbenitez
 */
public class thExecETL extends Thread{
	boolean isConnectDBSource;
	boolean isConnectDBDest;
    TaskProcess taskProcess = new TaskProcess();
    static srvRutinas gSub;
    static globalAreaData gDatos;
    Map<String, Interval> mapInterval;
    ETL etl = new ETL();
    String vSQLSource;
    String vSQLDest;
    int totalNewInterval;
    Logger logger = Logger.getLogger("thExecETL");
    
    public thExecETL(globalAreaData m, TaskProcess taskProcess) {
        this.taskProcess = taskProcess;
        gDatos = m;
        gSub = new srvRutinas(gDatos);
    }
    
    @Override
    public void run() {
        logger.info("Iniciando ejecución ETL:"+taskProcess.getProcID()+" "+taskProcess.getNumSecExec());
        
        if (isValidDataParam()) {
        	
            String etlString="";
			try {
				etlString = gSub.serializeObjectToJSon(taskProcess.getParams(), false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
            
            try {
				etl = (ETL) gSub.serializeJSonStringToObject(etlString, ETL.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        	/**
        	 * Establece conexion hacia BD Origen
        	 */
        	logger.info("Establece conexión a Base Origen: "+etl.getSDBNAME());
        	logger.info("dbType: "+etl.getSDBTYPE());
        	logger.info("IP: "+etl.getSIP());
        	logger.info("Port: "+etl.getSDBPORT());
        	logger.info("UserName: "+etl.getSUSERNAME());
        	logger.debug("Pass: "+etl.getSUSERPASS());
        	
            dataAccess sConn = new dataAccess(gDatos);
            
            sConn.setDbType(etl.getSDBTYPE());
            sConn.setDbHost(etl.getSIP());
            sConn.setDbPort(etl.getSDBPORT());
            sConn.setDbName(etl.getSDBNAME());
            sConn.setDbUser(etl.getSUSERNAME());
            sConn.setDbPass(etl.getSUSERPASS());
            
            sConn.conectar();
            
            if (sConn.isConnected()) {
                logger.info("Base origen connected!");
                isConnectDBSource= true;
            } else {
            	logger.info("Base origen NO connected!");
                isConnectDBSource = false;
            }
            
            /**
             * Establece conexion hacia BD Destino
             */
            logger.info("Establece conexión a Base Destino: "+etl.getDDBNAME());
        	logger.info("dbType: "+etl.getDDBTYPE());
        	logger.info("IP: "+etl.getDIP());
        	logger.info("Port: "+etl.getDDBPORT());
        	logger.info("UserName: "+etl.getDUSERNAME());
        	logger.debug("Pass: "+etl.getDUSERPASS());
            
            dataAccess dConn = new dataAccess(gDatos);
            dConn.setDbType(etl.getDDBTYPE());
            dConn.setDbHost(etl.getDIP());
            dConn.setDbPort(etl.getDDBPORT());
            dConn.setDbName(etl.getDDBNAME());
            dConn.setDbUser(etl.getDUSERNAME());
            dConn.setDbPass(etl.getDUSERPASS());
           
            
            dConn.conectar();
            
            if (dConn.isConnected()) {
                logger.info("Base Destino connected!");
                isConnectDBDest= true;
            } else {
            	logger.info("Base Destino NO connected!");
            	isConnectDBDest = false;
            }
            
            /**
             * Recupera Intervalos desde Metatada
             * los cuales debe venir como lista de intervalos en objeto ETL
             */
            
            totalNewInterval=0;
            
            //mapInterval = new TreeMap<>(etl.getMapInterval());
            
            logger.info("Inscribiendo intervalos desde Metadata");
            for (Map.Entry<String, Interval> entry : mapInterval.entrySet()) {
                if (!gDatos.getMapInteval().containsKey(entry.getKey())) {
                	gDatos.getMapInteval().put(entry.getKey(), entry.getValue());
                	logger.info("Nuevo intervalo inscrito: "+entry.getKey());
                	totalNewInterval++;
                }
            }
            
            
            if (isConnectDBSource&&isConnectDBDest) {
            
            /**
             * Genera nuevos intervalos faltantes
             */
            
            logger.info("Inscribiendo intervalos nuevos");
        	if (genNewIntervals()) {
        		
        		if (totalNewInterval>0) {
        		
	        		logger.info("Generando query global de extracción...");
	        		if (genQueryExtract()) {
	    	        	/**
	    	        	 * Procesa Extraccion de Intervalos
	    	        	 */
	    	        
	    	        	logger.info("Inicio Proceso de Extracción de Intervalos Generados");
	
	    	        	
	    	        
	    	        	//Genera las conversiones de fecha en base al type de datos del keyField
	    	        	int itera=1;
	    	        	
	    	        	Map<String, Interval> myMapInterval = new TreeMap<>(gDatos.getMapInteval());
	    	        
	    	            for (Map.Entry<String, Interval> entry : myMapInterval.entrySet()) {
	    	            	if   (itera>10000000) {
	    	            		break;
	    	            	} else {
	    	            		itera++;
	    	            	}
	    	            	
	    	                if (entry.getValue().getStatus().equals("Ready")&&entry.getValue().getNumSecExec().equals(taskProcess.getNumSecExec())) {
	    						int rowsRead=0;
	    						int rowsLoad=0;
	
	    	                	logger.info("Ejecutando Intervalo: "+entry.getKey());
	    	                	/**
	    	                	 * Genera Query para Cada Intervalo
	    	                	 */
	    	                	
	    	                	/**
	    	                	 * Actualiza Status del Interval y TaskProcess
	    	                	 */
	    	                	logger.info("Actualiza status del Intervalo");
	    	                	gDatos.getMapInteval().get(entry.getKey()).setFecIns(gSub.getDateNow());
	    	                	gDatos.getMapInteval().get(entry.getKey()).setFecUpdate(gSub.getDateNow());
	    	                	gDatos.getMapInteval().get(entry.getKey()).setStatus("Running");
	    	                	
	    	                	logger.info("Actualiza status del ETL");
	    	                	//etl.setMapInterval(gDatos.getMapInteval());
	    	                	
	    	                	logger.info("Actualiza status del Task");
	    	                	taskProcess.setUpdateTime(gSub.getDateNow());
	    	                	taskProcess.setStartTime(gSub.getDateNow());
	    	                	taskProcess.setParams(etl);
	    	                	
	    	                	gDatos.getMapTask().replace(taskProcess.getProcID()+":"+taskProcess.getNumSecExec(), taskProcess);
	    	                	
	//    	                	gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setUpdateTime(gSub.getDateNow());
	//    	                	gDatos.getMapTask().get(taskProcess.getProcID()+":"+taskProcess.getNumSecExec()).setParams(etl);
	    	                	
	    	                	
	    	                	String vQuery = new String(vSQLSource);
	    	                	String fecQueryIni;
	    	                	String fecQueryFin;
	    	    	        	switch (etl.getFIELDTYPE()) {
	    				        	case "date":
	    				        		fecQueryIni = fnConvertFecInterval(entry.getValue().getFecIni(),"date",etl.getSDBTYPE());
	    				        		fecQueryFin = fnConvertFecInterval(entry.getValue().getFecFin(),"date",etl.getSDBTYPE());
	    				        		break;
	    				        	case "int":
	    				        		fecQueryIni = fnConvertFecInterval(entry.getValue().getFecIni(),"int",etl.getSDBTYPE());
	    				        		fecQueryFin = fnConvertFecInterval(entry.getValue().getFecFin(),"int",etl.getSDBTYPE());
	    				        		break;
	    				        	case "varchar":
	    				        		fecQueryIni = fnConvertFecInterval(entry.getValue().getFecIni(),"varchar",etl.getSDBTYPE());
	    				        		fecQueryFin = fnConvertFecInterval(entry.getValue().getFecFin(),"varchar",etl.getSDBTYPE());
	    				        		break;
	    				        	default:
	    				        		fecQueryIni="";
	    				        		fecQueryFin="";
	    				        		break;
	    	    	        	}
	    	    	        	
	    	    	        	vQuery = vQuery + etl.getFIELDKEY() + " >= " + fecQueryIni;
	    	    	        	vQuery = vQuery + " and " + etl.getFIELDKEY() + " < " + fecQueryFin;
	    	    	        	
	    	    	        	/**
	    	    	        	 * Ejecuta Query en DBSource
	    	    	        	 */
	    	    	        	logger.info("Ejecutando Query a DBSource: "+vQuery);
	    	    	        	ResultSet rs = (ResultSet) sConn.getQuery(vQuery);
	    	    	        	if (rs!=null) {
	    	    	        		
	    	    	        		logger.debug("Resultado de rs no es nulo.");
	    	    	        		
	    	    	        		StringBuilder columnNames = new StringBuilder();
	    	    	        		StringBuilder bindVariables = new StringBuilder();
	    	    	        		
	    	    	        		int totalCols=0;
	    	    	        		for (int nf=0; nf<etl.getLstEtlMatch().size(); nf++) {
	    		        				if (nf>0) {
	    		        					columnNames.append(", ");
	    		        					bindVariables.append(", ");
	    		        				}
	    						       columnNames.append(etl.getLstEtlMatch().get(nf).getEtlDestField());
	    						       bindVariables.append('?');
	    						       totalCols = nf+1;
	    	    	        		}
	    	    	        		
	    	    	        		logger.info("Total de Columnas obtenidas: "+totalCols);
	    									  
	    							try {
	    								
	    								logger.info("Cargando datos de Etl-Interval: "+entry.getKey());
	    								int showRows=1000;
	    								while (rs.next()) {
	    									rowsRead++;
	    											
	    									PreparedStatement psInsertar = null;
	    									String pSql = "insert into "
	    											+ etl.getDTBNAME()
	    											+ " ( " 
	    											+ columnNames 
	    											+ " ) VALUES ( " 
	    											+ bindVariables	+ " ) ";
	    									
	    									//logger.debug("se insertará sql: "+pSql);
	    									try {
	    										psInsertar =  dConn.getConnection().prepareStatement(pSql);
	    									} catch (Exception e) {
	    										logger.error("Error deconocido en psInsertar.."+e.getMessage());
	    									}
	    									logger.debug("Recuperando valores de datos...");
	    									try {
	    										for (int nf=0; nf<etl.getLstEtlMatch().size(); nf++) {
	    											switch (etl.getLstEtlMatch().get(nf).getEtlSourceType()) {
	    												case "varchar":
	    													psInsertar.setString(nf+1, rs.getString(nf+1));
	    													break;
	    												case "int":
	    													psInsertar.setInt(nf+1, rs.getInt(nf+1));
	    													break;
	    												case "date":
	    													psInsertar.setDate(nf+1, rs.getDate(nf+1));
	    													break;
	    												default:
	    													psInsertar.setString(nf+1, rs.getString(nf+1));
	    											}
	    										}
	    									} catch (Exception e) {
	    										logger.error("Error incorporando parametros al insert "+e.getMessage());
	    									}
	
	    									try {
	    										
	    										if (psInsertar.executeUpdate()==1) {
	    											rowsLoad++;
	    										}
	    										
												if (rowsRead==showRows) {
													logger.info("Filas leídas   Etl-Interval "+entry.getKey()+ ": "+rowsRead);
													logger.info("Filas cargadas Etl-Interval "+entry.getKey()+ ": "+rowsLoad);
						    						gDatos.getMapInteval().get(entry.getKey()).setRowsLoad(rowsLoad);
						    						gDatos.getMapInteval().get(entry.getKey()).setRowsRead(rowsRead);
						    						
						    						//etl.setMapInterval(gDatos.getMapInteval());
						    						
						    						taskProcess.setParams(etl);
						    						
						    						gDatos.getMapTask().replace(taskProcess.getProcID()+":"+taskProcess.getNumSecExec(), taskProcess);
	
													showRows=showRows+1000;
												}
	
	    									} catch (Exception e) {
	    										logger.error("insercion insertando: "+pSql+ " error: "+e.getMessage());
	    									}
	    								} //end while rescorset dbSource
	    								rs.close();
	    								logger.info("Finalizando caga de Etl-Interval: "+entry.getKey());
	    								
	    								
	    							} catch (SQLException e) {
	    								// TODO Auto-generated catch block
	    								//e.printStackTrace();
	    								logger.error("Error inesperado en ciclo recorre DBSource "+e.getMessage());
	    							} //end while
	    	    	        	} else {
	    	    	        		logger.info("No hay datos para el intervalo: "+entry.getKey());
	    	    	        	}//end if (rs!=null)
	    	    	        	
	    						logger.info("Se leyeron: "+rowsRead+" filas para el intervalo: "+entry.getKey());
	    						logger.info("Se cargaron: "+rowsLoad+" filas para el intervalo: "+entry.getKey());
	    						
	    						/**
	    						 * Actualizando Status del Interval
	    						 */
	    						gDatos.getMapInteval().get(entry.getKey()).setRowsLoad(rowsLoad);
	    						gDatos.getMapInteval().get(entry.getKey()).setRowsRead(rowsRead);
	    						gDatos.getMapInteval().get(entry.getKey()).setuStatus("Finished");
	    						gDatos.getMapInteval().get(entry.getKey()).setStatus("Finished");
	    						gDatos.getMapInteval().get(entry.getKey()).setFecUpdate(gSub.getDateNow());
	    						
	    						//etl.setMapInterval(gDatos.getMapInteval());
	    						
	    						taskProcess.setParams(etl);
	    						
	    						gDatos.getMapTask().replace(taskProcess.getProcID()+":"+taskProcess.getNumSecExec(), taskProcess);
	    						
	    	    	        	
	    	                } //end if interval is Ready
	    	                logger.info("Finalizando Intervalo: "+entry.getKey());
	                    } //end for recorre intervals
	        		}
	        	}
	        	dConn.closeConnection();
	        	sConn.closeConnection();
	        	
	        	taskProcess.setStatus("Finished");
	        	taskProcess.setuStatus("Finished");
        	} else {
        		logger.error("No hay intervalos nuevos para generar");
            	taskProcess.setStatus("Finished");
            	taskProcess.setuStatus("Finished");
        	}

        } else {
        	logger.error("No es posible conectarse a bases destino o source.");
        	taskProcess.setStatus("Error");
        	taskProcess.setuStatus("Error");
        }
        } else {
            logger.error("Error en lectura de parámetros de entrada.");
        	taskProcess.setStatus("Error");
        	taskProcess.setuStatus("Error");
        }
        
        /**
         * Finalizando proceso de ETL
         */
        taskProcess.setEndTime(gSub.getDateNow());
        taskProcess.setUpdateTime(gSub.getDateNow());
        
        logger.info("Finalizando ejecución ETL:"+taskProcess.getProcID()+" "+taskProcess.getNumSecExec());
    }
    
    private boolean genNewIntervals() {
    	try {
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
	
//	                    logger.debug("Datos de Inicio de Extraccion de Intervalos...");
//	                    logger.debug("Fecha inicio intervalo: "+ fecIntervalIni);
//	                    logger.debug("Fecha fin Intervalo: "+fecIntervalFin);
//	                    logger.debug("IntervalID generado: "+localIntervalID);
	
	                    interval.setETLID(vETLID);
	                    interval.setIntervalID(localIntervalID);
	                    interval.setStatus("Ready");
	                    interval.setNumSecExec(taskProcess.getNumSecExec());
	                    interval.setFecIns(sdfToday.format(today));
	                    interval.setFecUpdate(sdfToday.format(today));
	                    interval.setFecIni(IntervalIni);
	                    interval.setFecFin(IntervalFin);
	
	                    if (!gDatos.getMapInteval().containsKey(vETLID+":"+localIntervalID)) {
	                    	gDatos.getMapInteval().put(vETLID+":"+localIntervalID, interval);
	                    	logger.info("Nuevo intervalo inscrito: "+vETLID+":"+localIntervalID);
	                    	totalNewInterval++;
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
	
	
//	                        logger.debug("Datos de Inicio de Extraccion de Intervalos...");
//	                        logger.debug("Fecha inicio intervalo: "+ fecIntervalIni);
//	                        logger.debug("Fecha fin Intervalo: "+fecIntervalFin);
//	                        logger.debug("IntervalID generado: "+localIntervalID);
	
	                        interval.setETLID(vETLID);
	                        interval.setIntervalID(localIntervalID);
	                        interval.setStatus("Ready");
	                        interval.setNumSecExec(taskProcess.getNumSecExec());
	                        interval.setFecIns(sdfToday.format(today));
	                        interval.setFecUpdate(sdfToday.format(today));
	                        interval.setFecIni(IntervalIni);
	                        interval.setFecFin(IntervalFin);
	
	                        if (!gDatos.getMapInteval().containsKey(vETLID+":"+localIntervalID)) {
	                        	gDatos.getMapInteval().put(vETLID+":"+localIntervalID, interval);
	                        	logger.info("Nuevo intervalo inscrito: "+vETLID+":"+localIntervalID);
	                        	totalNewInterval++;
	                        }
	
	                        break;
	
	                    case "2":
	                    case "3":
	                    case "4":
	                    case "5":
	                    default:
	                } //end switch
	            } //end while
	        return true;
    	} catch (Exception e) {
    		logger.error("Error generando intervalos faltantes..."+e.getMessage());
    		return false;
    	}
    }
    
    private boolean genQueryExtract() {
    	try {
        	//Genera Query de Select DB Source y Dest
        	vSQLSource = "select ";
        	vSQLDest = "select ";
        	boolean first = true;
        	for (int i=0; i<etl.getLstEtlMatch().size(); i++) {
        		if (first) {
        			first = false;
        		} else {
        			vSQLSource = vSQLSource + ",";
        			vSQLDest = vSQLDest + ",";
        		}
        		switch (etl.getLstEtlMatch().get(i).getEtlDestType()) {
        			case "F":
        				vSQLSource = vSQLSource + etl.getLstEtlMatch().get(i).getEtlSourceField();
        				break;
        			case "T":
        				vSQLSource = vSQLSource + (char)34 + etl.getLstEtlMatch().get(i).getEtlSourceField() + (char)34;
        				break;
    				default:
    					vSQLSource = vSQLSource + etl.getLstEtlMatch().get(i).getEtlSourceField();
    					break;
        		}
        		vSQLDest = vSQLDest + etl.getLstEtlMatch().get(i).getEtlDestField();
        	}
        	//Agrega el From
        	
        	vSQLSource = vSQLSource + " From " + etl.getSTBNAME();
        	vSQLDest = vSQLDest + " From " + etl.getDTBNAME();
        	
        	//Agrega Where key de extracción
        	
        	vSQLSource = vSQLSource + " Where ";
        	vSQLDest = vSQLDest + " Where 1=2";
        	
        	logger.info("Query global de extracción generada: "+vSQLSource);
        	
    		return true;
    	} catch (Exception e) {
    		logger.error("Error generando Query Extraccion..."+e.getMessage());
    		return false;
    	}
    }
    
    private String fnConvertFecInterval(String fecha, String dataType, String dbType) {
    	String dateConvert = "";
    	
    	switch (dbType) {
    		case "MYSQL":
    			switch (dataType) {
    				case "date":
    						dateConvert = "STR_TO_DATE("+fecha+",'%Y%m%d%H%i')";
    					break;
    				case "int":
    						dateConvert = "UNIX_TIMESTAMP('"+fecha+"')";
    					break;
    				case "varchar":
						break;
    			}
    			break;
    		case "SQL":
				break;
    		case "ORACLE":
    			break;
    	}
    	
    	return dateConvert;
    }
    
    public boolean isValidDataParam() {
        boolean isValid = true;
        
        try {
            if (taskProcess.getGrpID()==null || taskProcess.getProcID()==null || taskProcess.getIntervalID()==null || taskProcess.getNumSecExec()==null ||
            		taskProcess.getParams()==null) {
                //isValid = false;
            }
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}