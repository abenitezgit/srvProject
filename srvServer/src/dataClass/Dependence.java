package dataClass;

public class Dependence {
	String grpID;
	int procHijo;
	int procPadre;
	int critical;
	
	//Getter and Setter
	
	public String getGrpID() {
		return grpID;
	}
	public void setGrpID(String grpID) {
		this.grpID = grpID;
	}
	public int getProcHijo() {
		return procHijo;
	}
	public void setProcHijo(int procHijo) {
		this.procHijo = procHijo;
	}
	public int getProcPadre() {
		return procPadre;
	}
	public void setProcPadre(int procPadre) {
		this.procPadre = procPadre;
	}
	public int getCritical() {
		return critical;
	}
	public void setCritical(int critical) {
		this.critical = critical;
	}
	
}
