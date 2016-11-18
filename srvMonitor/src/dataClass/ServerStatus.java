/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataClass;

import java.sql.Connection;

/**
 *
 * @author andresbenitez
 */
public class ServerStatus {
    boolean isSocketServerActive;
    boolean isKeepAliveActive;
    boolean isGetAgendaActive;
    boolean isValMetadataConnect;
    boolean isThreadETLActive;
    boolean isLoadParam;
    boolean isLoadRutinas;
    boolean srvActive;
    Connection metadataConnection;
    int numProcMax;
    String srvStartTime;

    /**
     * 
     * Getter and Setter
     * @return 
     */
    
    public boolean isIsLoadParam() {
        return isLoadParam;
    }

    public void setIsLoadParam(boolean isLoadParam) {
        this.isLoadParam = isLoadParam;
    }

    public boolean isIsLoadRutinas() {
        return isLoadRutinas;
    }

    public void setIsLoadRutinas(boolean isLoadRutinas) {
        this.isLoadRutinas = isLoadRutinas;
    }

    public boolean isIsThreadETLActive() {
        return isThreadETLActive;
    }

    public void setIsThreadETLActive(boolean isThreadETLActive) {
        this.isThreadETLActive = isThreadETLActive;
    }
    
    public boolean isIsGetAgendaActive() {
        return isGetAgendaActive;
    }

    public void setIsGetAgendaActive(boolean isGetAgendaActive) {
        this.isGetAgendaActive = isGetAgendaActive;
    }
    
    public String getSrvStartTime() {
        return srvStartTime;
    }

    public void setSrvStartTime(String srvStartTime) {
        this.srvStartTime = srvStartTime;
    }

    public boolean isSrvActive() {
        return srvActive;
    }

    public void setSrvActive(boolean srvActive) {
        this.srvActive = srvActive;
    }
    
    public int getNumProcMax() {
        return numProcMax;
    }

    public void setNumProcMax(int numProcMax) {
        this.numProcMax = numProcMax;
    }

    public Connection getMetadataConnection() {
        return metadataConnection;
    }

    public void setMetadataConnection(Connection metadataConnection) {
        this.metadataConnection = metadataConnection;
    }

    public boolean isIsValMetadataConnect() {
        return isValMetadataConnect;
    }

    public void setIsValMetadataConnect(boolean isValMetadataConnect) {
        this.isValMetadataConnect = isValMetadataConnect;
    }

    public boolean isIsSocketServerActive() {
        return isSocketServerActive;
    }

    public void setIsSocketServerActive(boolean isSocketServerActive) {
        this.isSocketServerActive = isSocketServerActive;
    }

    public boolean isIsKeepAliveActive() {
        return isKeepAliveActive;
    }

    public void setIsKeepAliveActive(boolean isKeepAliveActive) {
        this.isKeepAliveActive = isKeepAliveActive;
    }
    
}
