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
public class ETL {
    String ETLID;
    String ETLDesc;
    int ETLEnable;
    String CliDesc;
    String FIELDKEY;
    String FIELDTYPE;
    int TIMEGAP;
    int TIMEGEN;
    int TIMEPERIOD;
    String UNITMEASURE;
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
    String LASTNUMSECEXEC;
    
    List<EtlMatch> lstEtlMatch = new ArrayList<>();
    
    //Getter and Setter
    //

    public String getLASTNUMSECEXEC() {
        return LASTNUMSECEXEC;
    }

    public void setLASTNUMSECEXEC(String LASTNUMSECEXEC) {
        this.LASTNUMSECEXEC = LASTNUMSECEXEC;
    }

    public List<EtlMatch> getLstEtlMatch() {
        return lstEtlMatch;
    }

    public void setLstEtlMatch(List<EtlMatch> lstEtlMatch) {
        this.lstEtlMatch = lstEtlMatch;
    }

    public String getETLID() {
        return ETLID;
    }

    public void setETLID(String ETLID) {
        this.ETLID = ETLID;
    }

    public String getETLDesc() {
        return ETLDesc;
    }

    public void setETLDesc(String ETLDesc) {
        this.ETLDesc = ETLDesc;
    }

    public int getETLEnable() {
        return ETLEnable;
    }

    public void setETLEnable(int ETLEnable) {
        this.ETLEnable = ETLEnable;
    }

    public String getCliDesc() {
        return CliDesc;
    }

    public void setCliDesc(String CliDesc) {
        this.CliDesc = CliDesc;
    }

    public String getFIELDKEY() {
        return FIELDKEY;
    }

    public void setFIELDKEY(String FIELDKEY) {
        this.FIELDKEY = FIELDKEY;
    }

    public String getFIELDTYPE() {
        return FIELDTYPE;
    }

    public void setFIELDTYPE(String FIELDTYPE) {
        this.FIELDTYPE = FIELDTYPE;
    }

    public int getTIMEGAP() {
        return TIMEGAP;
    }

    public void setTIMEGAP(int TIMEGAP) {
        this.TIMEGAP = TIMEGAP;
    }

    public int getTIMEGEN() {
        return TIMEGEN;
    }

    public void setTIMEGEN(int TIMEGEN) {
        this.TIMEGEN = TIMEGEN;
    }

    public int getTIMEPERIOD() {
        return TIMEPERIOD;
    }

    public void setTIMEPERIOD(int TIMEPERIOD) {
        this.TIMEPERIOD = TIMEPERIOD;
    }

    public String getUNITMEASURE() {
        return UNITMEASURE;
    }

    public void setUNITMEASURE(String UNITMEASURE) {
        this.UNITMEASURE = UNITMEASURE;
    }

    public int getWHEREACTIVE() {
        return WHEREACTIVE;
    }

    public void setWHEREACTIVE(int WHEREACTIVE) {
        this.WHEREACTIVE = WHEREACTIVE;
    }

    public String getQUERYBODY() {
        return QUERYBODY;
    }

    public void setQUERYBODY(String QUERYBODY) {
        this.QUERYBODY = QUERYBODY;
    }

    public String getSTBNAME() {
        return STBNAME;
    }

    public void setSTBNAME(String STBNAME) {
        this.STBNAME = STBNAME;
    }

    public String getDTBNAME() {
        return DTBNAME;
    }

    public void setDTBNAME(String DTBNAME) {
        this.DTBNAME = DTBNAME;
    }

    public String getSIP() {
        return SIP;
    }

    public void setSIP(String SIP) {
        this.SIP = SIP;
    }

    public String getSDBDESC() {
        return SDBDESC;
    }

    public void setSDBDESC(String SDBDESC) {
        this.SDBDESC = SDBDESC;
    }

    public String getSDBNAME() {
        return SDBNAME;
    }

    public void setSDBNAME(String SDBNAME) {
        this.SDBNAME = SDBNAME;
    }

    public String getSDBTYPE() {
        return SDBTYPE;
    }

    public void setSDBTYPE(String SDBTYPE) {
        this.SDBTYPE = SDBTYPE;
    }

    public String getSDBPORT() {
        return SDBPORT;
    }

    public void setSDBPORT(String SDBPORT) {
        this.SDBPORT = SDBPORT;
    }

    public String getSDBINSTANCE() {
        return SDBINSTANCE;
    }

    public void setSDBINSTANCE(String SDBINSTANCE) {
        this.SDBINSTANCE = SDBINSTANCE;
    }

    public String getSDBCONF() {
        return SDBCONF;
    }

    public void setSDBCONF(String SDBCONF) {
        this.SDBCONF = SDBCONF;
    }

    public String getSDBJDBC() {
        return SDBJDBC;
    }

    public void setSDBJDBC(String SDBJDBC) {
        this.SDBJDBC = SDBJDBC;
    }

    public String getSUSERNAME() {
        return SUSERNAME;
    }

    public void setSUSERNAME(String SUSERNAME) {
        this.SUSERNAME = SUSERNAME;
    }

    public String getSUSERPASS() {
        return SUSERPASS;
    }

    public void setSUSERPASS(String SUSERPASS) {
        this.SUSERPASS = SUSERPASS;
    }

    public String getSUSERTYPE() {
        return SUSERTYPE;
    }

    public void setSUSERTYPE(String SUSERTYPE) {
        this.SUSERTYPE = SUSERTYPE;
    }

    public String getDIP() {
        return DIP;
    }

    public void setDIP(String DIP) {
        this.DIP = DIP;
    }

    public String getDDBDESC() {
        return DDBDESC;
    }

    public void setDDBDESC(String DDBDESC) {
        this.DDBDESC = DDBDESC;
    }

    public String getDDBNAME() {
        return DDBNAME;
    }

    public void setDDBNAME(String DDBNAME) {
        this.DDBNAME = DDBNAME;
    }

    public String getDDBTYPE() {
        return DDBTYPE;
    }

    public void setDDBTYPE(String DDBTYPE) {
        this.DDBTYPE = DDBTYPE;
    }

    public String getDDBPORT() {
        return DDBPORT;
    }

    public void setDDBPORT(String DDBPORT) {
        this.DDBPORT = DDBPORT;
    }

    public String getDDBINSTANCE() {
        return DDBINSTANCE;
    }

    public void setDDBINSTANCE(String DDBINSTANCE) {
        this.DDBINSTANCE = DDBINSTANCE;
    }

    public String getDDBCONF() {
        return DDBCONF;
    }

    public void setDDBCONF(String DDBCONF) {
        this.DDBCONF = DDBCONF;
    }

    public String getDDBJDBC() {
        return DDBJDBC;
    }

    public void setDDBJDBC(String DDBJDBC) {
        this.DDBJDBC = DDBJDBC;
    }

    public String getDUSERNAME() {
        return DUSERNAME;
    }

    public void setDUSERNAME(String DUSERNAME) {
        this.DUSERNAME = DUSERNAME;
    }

    public String getDUSERPASS() {
        return DUSERPASS;
    }

    public void setDUSERPASS(String DUSERPASS) {
        this.DUSERPASS = DUSERPASS;
    }

    public String getDUSERTYPE() {
        return DUSERTYPE;
    }

    public void setDUSERTYPE(String DUSERTYPE) {
        this.DUSERTYPE = DUSERTYPE;
    }
}
