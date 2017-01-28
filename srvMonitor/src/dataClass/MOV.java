/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author andresbenitez
 */
public class MOV {
    String movID;
    String movDesc;
    int enable;
    String CliDesc;
    int WHEREACTIVE;
    String QUERYBODY;
    String STBNAME;
    String DTBNAME;
    String SIP;
    String SDBDESC;
    String SDBNAME;
    String SDBTYPE;
    String SDBPORT;
    String SDBINSTANCE;
    String SDBCONF;
    String SDBJDBC;
    String SUSERNAME;
    String SUSERPASS;
    String SUSERTYPE;
    String DIP;
    String DDBDESC;
    String DDBNAME;
    String DDBTYPE;
    String DDBPORT;
    String DDBINSTANCE;
    String DDBCONF;
    String DDBJDBC;
    String DUSERNAME;
    String DUSERPASS;
    String DUSERTYPE;
    String NUMSECEXEC;
    int rowsRead;
    int rowsLoad;
    int intentos;
    
    List<MovMatch> lstMovMatch = new ArrayList<>();
    
    //Getter and Setter
    //
    
	public String getMovID() {
		return movID;
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

	public int getIntentos() {
		return intentos;
	}

	public void setIntentos(int intentos) {
		this.intentos = intentos;
	}

	public void setMovID(String movID) {
		this.movID = movID;
	}

	public String getMovDesc() {
		return movDesc;
	}

	public void setMovDesc(String movDesc) {
		this.movDesc = movDesc;
	}

	public int getEnable() {
		return enable;
	}

	public void setEnable(int enable) {
		this.enable = enable;
	}

	public String getCliDesc() {
		return CliDesc;
	}

	public void setCliDesc(String cliDesc) {
		CliDesc = cliDesc;
	}

	public int getWHEREACTIVE() {
		return WHEREACTIVE;
	}

	public void setWHEREACTIVE(int wHEREACTIVE) {
		WHEREACTIVE = wHEREACTIVE;
	}

	public String getQUERYBODY() {
		return QUERYBODY;
	}

	public void setQUERYBODY(String qUERYBODY) {
		QUERYBODY = qUERYBODY;
	}

	public String getSTBNAME() {
		return STBNAME;
	}

	public void setSTBNAME(String sTBNAME) {
		STBNAME = sTBNAME;
	}

	public String getDTBNAME() {
		return DTBNAME;
	}

	public void setDTBNAME(String dTBNAME) {
		DTBNAME = dTBNAME;
	}

	public String getSIP() {
		return SIP;
	}

	public void setSIP(String sIP) {
		SIP = sIP;
	}

	public String getSDBDESC() {
		return SDBDESC;
	}

	public void setSDBDESC(String sDBDESC) {
		SDBDESC = sDBDESC;
	}

	public String getSDBNAME() {
		return SDBNAME;
	}

	public void setSDBNAME(String sDBNAME) {
		SDBNAME = sDBNAME;
	}

	public String getSDBTYPE() {
		return SDBTYPE;
	}

	public void setSDBTYPE(String sDBTYPE) {
		SDBTYPE = sDBTYPE;
	}

	public String getSDBPORT() {
		return SDBPORT;
	}

	public void setSDBPORT(String sDBPORT) {
		SDBPORT = sDBPORT;
	}

	public String getSDBINSTANCE() {
		return SDBINSTANCE;
	}

	public void setSDBINSTANCE(String sDBINSTANCE) {
		SDBINSTANCE = sDBINSTANCE;
	}

	public String getSDBCONF() {
		return SDBCONF;
	}

	public void setSDBCONF(String sDBCONF) {
		SDBCONF = sDBCONF;
	}

	public String getSDBJDBC() {
		return SDBJDBC;
	}

	public void setSDBJDBC(String sDBJDBC) {
		SDBJDBC = sDBJDBC;
	}

	public String getSUSERNAME() {
		return SUSERNAME;
	}

	public void setSUSERNAME(String sUSERNAME) {
		SUSERNAME = sUSERNAME;
	}

	public String getSUSERPASS() {
		return SUSERPASS;
	}

	public void setSUSERPASS(String sUSERPASS) {
		SUSERPASS = sUSERPASS;
	}

	public String getSUSERTYPE() {
		return SUSERTYPE;
	}

	public void setSUSERTYPE(String sUSERTYPE) {
		SUSERTYPE = sUSERTYPE;
	}

	public String getDIP() {
		return DIP;
	}

	public void setDIP(String dIP) {
		DIP = dIP;
	}

	public String getDDBDESC() {
		return DDBDESC;
	}

	public void setDDBDESC(String dDBDESC) {
		DDBDESC = dDBDESC;
	}

	public String getDDBNAME() {
		return DDBNAME;
	}

	public void setDDBNAME(String dDBNAME) {
		DDBNAME = dDBNAME;
	}

	public String getDDBTYPE() {
		return DDBTYPE;
	}

	public void setDDBTYPE(String dDBTYPE) {
		DDBTYPE = dDBTYPE;
	}

	public String getDDBPORT() {
		return DDBPORT;
	}

	public void setDDBPORT(String dDBPORT) {
		DDBPORT = dDBPORT;
	}

	public String getDDBINSTANCE() {
		return DDBINSTANCE;
	}

	public void setDDBINSTANCE(String dDBINSTANCE) {
		DDBINSTANCE = dDBINSTANCE;
	}

	public String getDDBCONF() {
		return DDBCONF;
	}

	public void setDDBCONF(String dDBCONF) {
		DDBCONF = dDBCONF;
	}

	public String getDDBJDBC() {
		return DDBJDBC;
	}

	public void setDDBJDBC(String dDBJDBC) {
		DDBJDBC = dDBJDBC;
	}

	public String getDUSERNAME() {
		return DUSERNAME;
	}

	public void setDUSERNAME(String dUSERNAME) {
		DUSERNAME = dUSERNAME;
	}

	public String getDUSERPASS() {
		return DUSERPASS;
	}

	public void setDUSERPASS(String dUSERPASS) {
		DUSERPASS = dUSERPASS;
	}

	public String getDUSERTYPE() {
		return DUSERTYPE;
	}

	public void setDUSERTYPE(String dUSERTYPE) {
		DUSERTYPE = dUSERTYPE;
	}

	public String getNUMSECEXEC() {
		return NUMSECEXEC;
	}

	public void setNUMSECEXEC(String nUMSECEXEC) {
		NUMSECEXEC = nUMSECEXEC;
	}

	public List<MovMatch> getLstMovMatch() {
		return lstMovMatch;
	}

	public void setLstMovMatch(List<MovMatch> lstMovMatch) {
		this.lstMovMatch = lstMovMatch;
	}
    
    
}
