/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataClass;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author andresbenitez
 */
public class Threads {
    Thread thread;
    String thName;
    long thID;
    String startTime;
    String status;
    int thPriority;
    
    public Threads(Thread inputThread) {
        thread =  inputThread;
        thName = thread.getName();
        thID = thread.getId();
        thPriority = thread.getPriority();
        status = thread.getState().toString();
        
        //Extrae Fecha de Hoy
        //
        Date today;
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        today = new Date();
        startTime = formatter.format(today);
    }

    public int getThPriority() {
        return thPriority;
    }

    public void setThPriority(int thPriority) {
        this.thPriority = thPriority;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public String getThName() {
        return thName;
    }

    public void setThName(String thName) {
        this.thName = thName;
    }

    public long getThID() {
        return thID;
    }

    public void setThID(long thID) {
        this.thID = thID;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
