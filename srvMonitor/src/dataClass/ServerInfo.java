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
public class ServerInfo {
    String srvID;
    String srvDesc;
    int srvEnable;
    int txpMain;
    int txpKeep;
    int txpSocket;
    int txpAgendas;
    int txpETL;
    int srvPort;
    int ageShowHour;
    int ageGapMinute;
    String authKey;
    String dbType;
    String dbHost;
    String dbPort;
    String dbUser;
    String dbPass;
    String dbName;
    String dbInstance;
    String dbJDBCDriver;
        
    public ServerInfo() {
    }
    
    public ServerInfo(String srvID, String srvDesc, int srvEnable) {
        this.srvID = srvID;
        this.srvDesc = srvDesc;
        this.srvEnable = srvEnable;
    }
    
    /**
     * Getter And Setter
     * @return
     */
    
    public int getAgeShowHour() {
        return ageShowHour;
    }

    public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public String getDbPort() {
		return dbPort;
	}

	public void setDbPort(String dbPort) {
		this.dbPort = dbPort;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPass() {
		return dbPass;
	}

	public void setDbPass(String dbPass) {
		this.dbPass = dbPass;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbInstance() {
		return dbInstance;
	}

	public void setDbInstance(String dbInstance) {
		this.dbInstance = dbInstance;
	}

	public String getDbJDBCDriver() {
		return dbJDBCDriver;
	}

	public void setDbJDBCDriver(String dbJDBCDriver) {
		this.dbJDBCDriver = dbJDBCDriver;
	}

	public void setAgeShowHour(int ageShowHour) {
        this.ageShowHour = ageShowHour;
    }

    public int getAgeGapMinute() {
        return ageGapMinute;
    }

    public void setAgeGapMinute(int ageGapMinute) {
        this.ageGapMinute = ageGapMinute;
    }

    public int getTxpKeep() {
        return txpKeep;
    }

    public void setTxpKeep(int txpKeep) {
        this.txpKeep = txpKeep;
    }

    public int getTxpSocket() {
        return txpSocket;
    }

    public void setTxpSocket(int txpSocket) {
        this.txpSocket = txpSocket;
    }

    public int getTxpAgendas() {
        return txpAgendas;
    }

    public void setTxpAgendas(int txpAgendas) {
        this.txpAgendas = txpAgendas;
    }

    public int getTxpETL() {
        return txpETL;
    }

    public void setTxpETL(int txpETL) {
        this.txpETL = txpETL;
    }

    public int getTxpMain() {
        return txpMain;
    }

    public void setTxpMain(int txpMain) {
        this.txpMain = txpMain;
    }

    public int getSrvPort() {
        return srvPort;
    }

    public void setSrvPort(int srvPort) {
        this.srvPort = srvPort;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getSrvID() {
        return srvID;
    }

    public void setSrvID(String srvID) {
        this.srvID = srvID;
    }

    public String getSrvDesc() {
        return srvDesc;
    }

    public void setSrvDesc(String srvDesc) {
        this.srvDesc = srvDesc;
    }

    public int getSrvEnable() {
        return srvEnable;
    }

    public void setSrvEnable(int srvEnable) {
        this.srvEnable = srvEnable;
    }
}
