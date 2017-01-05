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
    String intervalID;
    String numSecExec;
    String status;
    String uStatus;
    String fecIns;
    String fecUpdate;
    String fecIni;
    String fecFin;
    int rowsRead;
    int rowsLoad;
    int intentos;
        
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

	public int getIntentos() {
		return intentos;
	}

	public void setIntentos(int intentos) {
		this.intentos = intentos;
	}

	public String getFecIni() {
		return fecIni;
	}

	public void setFecIni(String fecIni) {
		this.fecIni = fecIni;
	}

	public String getFecFin() {
		return fecFin;
	}

	public void setFecFin(String fecFin) {
		this.fecFin = fecFin;
	}

	public void setuStatus(String uStatus) {
		this.uStatus = uStatus;
	}

	public String getFecIns() {
		return fecIns;
	}

	public void setFecIns(String fecIns) {
		this.fecIns = fecIns;
	}

	public String getFecUpdate() {
		return fecUpdate;
	}

	public void setFecUpdate(String fecUpdate) {
		this.fecUpdate = fecUpdate;
	}

	public int getRowsRead() {
		return rowsRead;
	}

	public void setRowsRead(int rowsRead) {
		this.rowsRead = rowsRead;
	}

	public int getRowsLoad() {
		return rowsLoad;
	}

	public void setRowsLoad(int rowsLoad) {
		this.rowsLoad = rowsLoad;
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

}
