/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvmonitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utilities.globalAreaData;

/**
 *
 * @author andresbenitez
 */
public class thGetAgendas extends Thread{
    static globalAreaData gDatos;
    
//Carga Clase log4
    static Logger logger = Logger.getLogger("thGetAgendas");   
    
    public thGetAgendas(globalAreaData m) {
        gDatos = m;
    }
    
    @Override
    public void run() {
        /*
            Recupera Parametros Fecha Actual
        */
        logger.info("Buscando Agendas Activas");

        String[] ids = TimeZone.getAvailableIDs(-4 * 60 * 60 * 1000);
        String clt = ids[0];
        SimpleTimeZone tz = new SimpleTimeZone(-4 * 60 * 60 * 1000, clt);
        tz.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        tz.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        Calendar calendar = new GregorianCalendar(tz);

        int year       = calendar.get(Calendar.YEAR);
        int month      = calendar.get(Calendar.MONTH); // Jan = 0, dec = 11
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); 
        int dayOfWeek  = calendar.get(Calendar.DAY_OF_WEEK);
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int weekOfMonth= calendar.get(Calendar.WEEK_OF_MONTH);

        int hour       = calendar.get(Calendar.HOUR);        // 12 hour clock
        int hourOfDay  = calendar.get(Calendar.HOUR_OF_DAY); // 24 hour clock
        int minute     = calendar.get(Calendar.MINUTE);
        int second     = calendar.get(Calendar.SECOND);
        int millisecond= calendar.get(Calendar.MILLISECOND);
        
        int findHour=12;
        int findMinutes=5;

        /*
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        int hourBefore = calendar.get(Calendar.HOUR_OF_DAY);

        calendar.add(Calendar.HOUR_OF_DAY, 2);
        int hourAfter = calendar.get(Calendar.HOUR_OF_DAY);
        */
        
        String posmonth = String.valueOf(month+1);
        String posdayOfMonth = String.valueOf(dayOfMonth);
        String posdayOfWeek = String.valueOf(dayOfWeek);
        String posweekOfYear = String.valueOf(weekOfYear);
        String posweekOfMonth = String.valueOf(weekOfMonth);
        String poshourOfDay = String.valueOf(hourOfDay);
        String posminute = String.valueOf(minute);
        String possecond = String.valueOf(second);
        String posmillisecond = String.valueOf(millisecond);
        
        Calendar iteratorCalendar;
        String vSQL;
        String iteratorHour;
        String iteratorMinute;
        Statement stm;
        JSONObject jData;
        JSONObject jDataMinute;
        JSONArray jArray = new JSONArray();
        JSONArray jArrayMinute = new JSONArray();
        String posIteratorHour;
        String posIteratorMinute;
        
        /*
        Inicializa Lista de Agendas
        */
        gDatos.getLstShowAgendas().clear();
        gDatos.getLstActiveAgendas().clear();
        
        for (int i=-findHour; i<=findHour; i++) {
            iteratorCalendar = new GregorianCalendar(tz);
            iteratorCalendar.add(Calendar.HOUR_OF_DAY, i);
            iteratorHour = String.valueOf(iteratorCalendar.get(Calendar.HOUR_OF_DAY));
            posIteratorHour = String.valueOf(Integer.valueOf(iteratorHour)+1);
            
            vSQL = "select "+iteratorHour+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from process.tb_agenda where "
                    + "     ageEnable=1 "
                    + "     and substr(month,"+posmonth+",1) = '1'"
                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'";
            logger.debug("i: "+i+" vSQL: "+vSQL);
            try {
                stm = gDatos.getServerStatus().getMetadataConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                jData = new JSONObject();

                ResultSet rs = stm.executeQuery(vSQL);
                if (rs!=null) {
                    while (rs.next()) {
                        jData = new JSONObject();
                        jData.put("horaAgenda", rs.getString("horaAgenda"));
                        jData.put("ageID", rs.getString("ageID"));
                        jData.put("month", rs.getString("month"));
                        jData.put("dayOfMonth", rs.getString("dayOfMonth"));
                        jData.put("weekOfYear", rs.getString("weekOfYear"));
                        jData.put("weekOfMonth", rs.getString("weekOfMonth"));
                        jData.put("hourOfDay", rs.getString("hourOfDay"));
                        jArray.put(jData);
                        gDatos.getLstShowAgendas().add(jData);
                    }
                } else {
                        jData.put("horaAgenda", iteratorHour);
                        jData.put("ageID", "");
                        jData.put("month", "");
                        jData.put("dayOfMonth", "");
                        jData.put("weekOfYear", "");
                        jData.put("weekOfMonth","");
                        jData.put("hourOfDay", "");
                        jArray.put(jData);
                        gDatos.getLstShowAgendas().add(jData);
                    System.out.println("No hay registros");
                }
                stm.close();
            } catch (SQLException | JSONException e) {
                logger.error(e.getMessage());
            }
        }
        
        iteratorCalendar = new GregorianCalendar(tz);
        iteratorHour = String.valueOf(iteratorCalendar.get(Calendar.HOUR_OF_DAY));
        posIteratorHour = String.valueOf(Integer.valueOf(iteratorHour)+1);
        

        for (int i=-findMinutes; i<=0; i++) {
            iteratorCalendar = new GregorianCalendar(tz);
            iteratorCalendar.add(Calendar.MINUTE, i);
            iteratorMinute = String.valueOf(iteratorCalendar.get(Calendar.MINUTE));
            posIteratorMinute = String.valueOf(Integer.valueOf(iteratorMinute)+1);
            
            vSQL = "select "+iteratorMinute+" horaAgenda,ageID, month, dayOfMonth, dayOfWeek, weekOfYear, weekOfMonth, hourOfDay from process.tb_agenda where "
                    + "     ageEnable=1 "
                    + "     and substr(month,"+posmonth+",1) = '1'"
                    + "     and substr(dayOfMonth,"+posdayOfMonth+",1) = '1'"
                    + "     and substr(dayOfWeek,"+posdayOfWeek+",1) = '1'"
                    + "     and substr(weekOfYear,"+posweekOfYear+",1) = '1'"
                    + "     and substr(weekOfMonth,"+posweekOfMonth+",1) = '1'"
                    + "     and substr(hourOfDay,"+posIteratorHour +",1) = '1'"
                    + "     and substr(minute,"+posIteratorMinute +",1) = '1'";
            logger.debug("i: "+i+" vSQL: "+vSQL);
            try {
                stm = gDatos.getServerStatus().getMetadataConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

                ResultSet rs = stm.executeQuery(vSQL);
                if (rs!=null) {
                    while (rs.next()) {
                        jDataMinute = new JSONObject();
                        jDataMinute.put("horaAgenda", rs.getString("horaAgenda"));
                        jDataMinute.put("ageID", rs.getString("ageID"));
                        jDataMinute.put("month", rs.getString("month"));
                        jDataMinute.put("dayOfMonth", rs.getString("dayOfMonth"));
                        jDataMinute.put("weekOfYear", rs.getString("weekOfYear"));
                        jDataMinute.put("weekOfMonth", rs.getString("weekOfMonth"));
                        jDataMinute.put("hourOfDay", rs.getString("hourOfDay"));
                        jArrayMinute.put(jDataMinute);
                        gDatos.getLstActiveAgendas().add(jDataMinute);
                    }
                }
                stm.close();
            } catch (SQLException | JSONException e) {
                logger.error(e.getMessage());
            }
        }

        for (int i=0; i<gDatos.getLstShowAgendas().size(); i++) {
            logger.debug(gDatos.getLstShowAgendas().get(i).toString());
        }
        
        
        for (int i=0; i<gDatos.getLstActiveAgendas().size(); i++) {
            logger.debug(gDatos.getLstActiveAgendas().get(i).toString());
        }


        logger.info("Finaliza busquenda agendas activas...");
    }
}
