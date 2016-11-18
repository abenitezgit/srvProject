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
public class ServiceInfo {
    String srvID;
    String srvDesc;
    String srvHost;
    int srvPort;
    String srvMonHost;
    String srvMonHostBack;
    int monPort;
    int monPortBack;
    int txpMain;
    String authKey;

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

    public String getSrvHost() {
        return srvHost;
    }

    public void setSrvHost(String srvHost) {
        this.srvHost = srvHost;
    }

    public int getSrvPort() {
        return srvPort;
    }

    public void setSrvPort(int srvPort) {
        this.srvPort = srvPort;
    }

    public String getSrvMonHost() {
        return srvMonHost;
    }

    public void setSrvMonHost(String srvMonHost) {
        this.srvMonHost = srvMonHost;
    }

    public String getSrvMonHostBack() {
        return srvMonHostBack;
    }

    public void setSrvMonHostBack(String srvMonHostBack) {
        this.srvMonHostBack = srvMonHostBack;
    }

    public int getMonPort() {
        return monPort;
    }

    public void setMonPort(int monPort) {
        this.monPort = monPort;
    }

    public int getMonPortBack() {
        return monPortBack;
    }

    public void setMonPortBack(int monPortBack) {
        this.monPortBack = monPortBack;
    }

    public int getTxpMain() {
        return txpMain;
    }

    public void setTxpMain(int txpMain) {
        this.txpMain = txpMain;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }
    
}
