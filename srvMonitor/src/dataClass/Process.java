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
public class Process {
    String procID;
    int nOrder;
    int critical;
    String type;
    String numSecExec;
    
    Object params = new Object();
    
    //Getter and Setter

    public Object getParams() {
		return params;
	}

	public String getNumSecExec() {
		return numSecExec;
	}

	public void setNumSecExec(String numSecExec) {
		this.numSecExec = numSecExec;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setParams(Object params) {
		this.params = params;
	}

	public int getCritical() {
        return critical;
    }

    public void setCritical(int critical) {
        this.critical = critical;
    }

    public int getnOrder() {
        return nOrder;
    }

    public void setnOrder(int nOrder) {
        this.nOrder = nOrder;
    }

    public String getProcID() {
        return procID;
    }

    public void setProcID(String procID) {
        this.procID = procID;
    }
}
