/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataClass;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andresbenitez
 */
public class Grupo {
    String grpID;
    String grpDESC;
    String grpCLIID;
    String grpHORID;
    String grpUFechaExec;
    String uStatus;
    String status;
    String numSecExec;
    String lastNumSecExec;
    
    List<Process> lstProcess = new ArrayList<>();

    //Getter and Setter

    
    
    public String getuStatus() {
        return uStatus;
    }

    public String getNumSecExec() {
		return numSecExec;
	}

	public void setNumSecExec(String numSecExec) {
		this.numSecExec = numSecExec;
	}

	public String getLastNumSecExec() {
		return lastNumSecExec;
	}

	public void setLastNumSecExec(String lastNumSecExec) {
		this.lastNumSecExec = lastNumSecExec;
	}

	public void setuStatus(String uStatus) {
        this.uStatus = uStatus;
    }
    
    public List<Process> getLstProcess() {
        return lstProcess;
    }

    public void setLstProcess(List<Process> lstProcess) {
        this.lstProcess = lstProcess;
    }

    public String getGrpUFechaExec() {
        return grpUFechaExec;
    }

    public void setGrpUFechaExec(String grpUFechaExec) {
        this.grpUFechaExec = grpUFechaExec;
    }
    
    public String getGrpDESC() {
        return grpDESC;
    }

    public void setGrpDESC(String grpDESC) {
        this.grpDESC = grpDESC;
    }

    public String getGrpCLIID() {
        return grpCLIID;
    }

    public void setGrpCLIID(String grpCLIID) {
        this.grpCLIID = grpCLIID;
    }

    public String getGrpHORID() {
        return grpHORID;
    }

    public void setGrpHORID(String grpHORID) {
        this.grpHORID = grpHORID;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGrpID() {
        return grpID;
    }

    public void setGrpID(String grpID) {
        this.grpID = grpID;
    }
    
}
