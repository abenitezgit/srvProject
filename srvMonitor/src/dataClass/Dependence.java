package dataClass;

public class Dependence {
	String grpID;
	String procHijo;
	String procPadre;
	int critical;
	
	//Getter and Setter
	
	public String getGrpID() {
		return grpID;
	}
	public String getProcHijo() {
		return procHijo;
	}
	public void setProcHijo(String procHijo) {
		this.procHijo = procHijo;
	}
	public String getProcPadre() {
		return procPadre;
	}
	public void setProcPadre(String procPadre) {
		this.procPadre = procPadre;
	}
	public int getCritical() {
		return critical;
	}
	public void setCritical(int critical) {
		this.critical = critical;
	}
	public void setGrpID(String grpID) {
		this.grpID = grpID;
	}
}
