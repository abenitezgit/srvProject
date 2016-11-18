/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classTest;

import abtGlobals.globalVARArea;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author andresbenitez
 */
public class testClass {
    static globalVARArea gDatos;
    static String cETL_INTERVALEXTRACT="30";
    static String cETL_TIMEGAP="60";
    static String cETL_TIMEINIGEN="1440";
    static String cETL_INTERVALUNIDAD="0";
    
    public testClass(globalVARArea m) throws IOException {
        gDatos = m;
    }
 
    public static void main(String args[]) {
    
        insertETLInterval("etl00001");
    }
    
    public static void insertETLInterval(String vProc) {

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
        
        
        //Setea Fecha Actual
        //
        today = new Date();
        
        
        //Setea Fecha GAP - Desface de tiempo en extraccion
        //
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -(Integer.valueOf(cETL_TIMEGAP)+Integer.valueOf(cETL_INTERVALEXTRACT)));
        fecGap = c.getTime();
        
        //Setea Fecha Inicio Inscripcion/Revision de Intervalos
        
        c.setTime(today);
        c.add(Calendar.MINUTE, -Integer.valueOf(cETL_TIMEINIGEN));
        fecIni = c.getTime();
        
        System.out.println("today   : "+ today);
        System.out.println("fecGap  : "+ fecGap);
        System.out.println("fecIni  : "+ fecIni);
        
        fecItera = fecIni;
        
        while (fecItera.compareTo(fecGap) < 0) {
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
            
            switch (cETL_INTERVALUNIDAD) {
                case "0":
                    fecIntervalIni = null;
                    fecIntervalFin = null;
                    numInterval = 60/Integer.valueOf(cETL_INTERVALEXTRACT);
                    for (int i=1;i<=numInterval;i++) {
                        c.set(AnoItera, MesItera, DiaItera, HoraItera, (i)*Integer.valueOf(cETL_INTERVALEXTRACT),0);
                        fecIntervalFin = c.getTime();
                        if (fecIntervalFin.compareTo(fecItera) >0 ) {
                            c.set(AnoItera, MesItera, DiaItera, HoraItera, (i-1)*Integer.valueOf(cETL_INTERVALEXTRACT),0);
                            fecIntervalIni = c.getTime();
                            break;
                        }
                    }
                    c.setTime(fecItera);
                    c.add(Calendar.MINUTE, Integer.valueOf(cETL_INTERVALEXTRACT));
                    fecItera = c.getTime();
                    
                    System.out.println(fecIntervalIni);
                    System.out.println(fecIntervalFin);
                    
                    break;
                    
                case "1":
                    fecIntervalIni = null;
                    fecIntervalFin = null;
                    numInterval = 24/Integer.valueOf(cETL_INTERVALEXTRACT);
                    for (int i=1;i<=numInterval;i++) {
                        c.set(AnoItera, MesItera, DiaItera, (i)*Integer.valueOf(cETL_INTERVALEXTRACT), 0, 0);
                        fecIntervalFin = c.getTime();
                        if (fecIntervalFin.compareTo(fecItera) >0 ) {
                            c.set(AnoItera, MesItera, DiaItera, (i-1)*Integer.valueOf(cETL_INTERVALEXTRACT), 0, 0);
                            fecIntervalIni = c.getTime();
                            break;
                        }
                    }
                    c.setTime(fecItera);
                    c.add(Calendar.HOUR_OF_DAY, Integer.valueOf(cETL_INTERVALEXTRACT));
                    fecItera = c.getTime();
                    
                    System.out.println(fecIntervalIni);
                    System.out.println(fecIntervalFin);
                    
                    break;
                    
                case "2":
                case "3":
                case "4":
                case "5":
                default:
            }
                
        }
        

    } catch (Exception e) {
        System.out.println("Error Insertando Intervalos");
    }

    }

    
    
}
