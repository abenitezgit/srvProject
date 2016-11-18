/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataClass;


/**
 *
 * @author andresbenitez
 */
public class Interval {
    String ETLID;
    String intervalID;
    String status;
    String fechaIns;
    String fechaUpdate;
    int numExec;
    
    ETL etlConf;
    
    //Getter and Setter

    public ETL getEtlConf() {
        return etlConf;
    }

    public void setEtlConf(ETL etlConf) {
        this.etlConf = etlConf;
    }

    public String getETLID() {
        return ETLID;
    }

    public void setETLID(String ETLID) {
        this.ETLID = ETLID;
    }

    public String getIntervalID() {
        return intervalID;
    }

    public void setIntervalID(String intervalID) {
        this.intervalID = intervalID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFechaIns() {
        return fechaIns;
    }

    public void setFechaIns(String fechaIns) {
        this.fechaIns = fechaIns;
    }

    public String getFechaUpdate() {
        return fechaUpdate;
    }

    public void setFechaUpdate(String fechaUpdate) {
        this.fechaUpdate = fechaUpdate;
    }

    public int getNumExec() {
        return numExec;
    }

    public void setNumExec(int numExec) {
        this.numExec = numExec;
    }
}
