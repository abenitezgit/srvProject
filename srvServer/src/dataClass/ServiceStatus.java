/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataClass;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andresbenitez
 */
public class ServiceStatus {
    String srvID;
    String srvHost;
    String srvStartTime;
    String srvUpdateTime;
    boolean srvActive;
    boolean isActivePrimaryMonHost;
    boolean isSocketServerActive;
    boolean isConnectMonHost;
    boolean isAssignedTypeProc;
    boolean isKeepAliveActive;
    boolean isSubRunProcActive;
    boolean isLoadParam;
    boolean isLoadRutinas;
    int numProcRunning;
    int numProcSleeping;
    int numProcFinished;
    int numTotalExec;
    int numProcMax;
    int srvEnable;
    int numThreadActives;
    int srvPort;
    Socket skCliente;
    
    //Información Propia
    List<AssignedTypeProc> lstAssignedTypeProc = new ArrayList<>();
    List<ActiveTypeProc> lstActiveTypeProc = new ArrayList<>();
    List<PoolProcess> lstPoolProcess = new ArrayList<>();
    
    //Getter and Setter
    //

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
    
    public String getSrvStartTime() {
        return srvStartTime;
    }

    public void setSrvStartTime(String srvStartTime) {
        this.srvStartTime = srvStartTime;
    }

    public String getSrvUpdateTime() {
        return srvUpdateTime;
    }

    public void setSrvUpdateTime(String srvUpdateTime) {
        this.srvUpdateTime = srvUpdateTime;
    }

    public List<PoolProcess> getLstPoolProcess() {
        return lstPoolProcess;
    }

    public void setLstPoolProcess(List<PoolProcess> lstPoolProcess) {
        this.lstPoolProcess = lstPoolProcess;
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

    public boolean isIsAssignedTypeProc() {
        return isAssignedTypeProc;
    }

    public void setIsAssignedTypeProc(boolean isAssignedTypeProc) {
        this.isAssignedTypeProc = isAssignedTypeProc;
    }

    public int getNumThreadActives() {
        return numThreadActives;
    }

    public void setNumThreadActives(int numThreadActives) {
        this.numThreadActives = numThreadActives;
    }
    
    public boolean isIsSubRunProcActive() {
        return isSubRunProcActive;
    }

    public void setIsSubRunProcActive(boolean isSubRunProcActive) {
        this.isSubRunProcActive = isSubRunProcActive;
    }
    
    public int getNumProcRunning() {
        return numProcRunning;
    }

    public void setNumProcRunning(int numProcRunning) {
        this.numProcRunning = numProcRunning;
    }

    public int getNumProcSleeping() {
        return numProcSleeping;
    }

    public void setNumProcSleeping(int numProcSleeping) {
        this.numProcSleeping = numProcSleeping;
    }

    public int getNumProcFinished() {
        return numProcFinished;
    }

    public void setNumProcFinished(int numProcFinished) {
        this.numProcFinished = numProcFinished;
    }
    
    public List<AssignedTypeProc> getLstAssignedTypeProc() {
        return lstAssignedTypeProc;
    }

    public void setLstAssignedTypeProc(List<AssignedTypeProc> lstAssignedTypeProc) {
        this.lstAssignedTypeProc = lstAssignedTypeProc;
    }

    public List<ActiveTypeProc> getLstActiveTypeProc() {
        return lstActiveTypeProc;
    }

    public void setLstActiveTypeProc(List<ActiveTypeProc> lstActiveTypeProc) {
        this.lstActiveTypeProc = lstActiveTypeProc;
    }
    
    public Socket getSkCliente() {
        return skCliente;
    }

    public void setSkCliente(Socket skCliente) {
        this.skCliente = skCliente;
    }

    public boolean isIsKeepAliveActive() {
        return isKeepAliveActive;
    }

    public void setIsKeepAliveActive(boolean isKeepAliveActive) {
        this.isKeepAliveActive = isKeepAliveActive;
    }
    
    public boolean isIsActivePrimaryMonHost() {
        return isActivePrimaryMonHost;
    }

    public void setIsActivePrimaryMonHost(boolean isActivePrimaryMonHost) {
        this.isActivePrimaryMonHost = isActivePrimaryMonHost;
    }

    public boolean isIsSocketServerActive() {
        return isSocketServerActive;
    }

    public void setIsSocketServerActive(boolean isSocketServerActive) {
        this.isSocketServerActive = isSocketServerActive;
    }

    public boolean isIsConnectMonHost() {
        return isConnectMonHost;
    }

    public void setIsConnectMonHost(boolean isConnectMonHost) {
        this.isConnectMonHost = isConnectMonHost;
    }

    public int getNumTotalExec() {
        return numTotalExec;
    }

    public void setNumTotalExec(int numTotalExec) {
        this.numTotalExec = numTotalExec;
    }

    public boolean isSrvActive() {
        return srvActive;
    }

    public void setSrvActive(boolean srvActive) {
        this.srvActive = srvActive;
    }

    public int getSrvEnable() {
        return srvEnable;
    }

    public void setSrvEnable(int srvEnable) {
        this.srvEnable = srvEnable;
    }

    public String getSrvID() {
        return srvID;
    }

    public void setSrvID(String srvID) {
        this.srvID = srvID;
    }

    public int getNumProcMax() {
        return numProcMax;
    }

    public void setNumProcMax(int numProcMax) {
        this.numProcMax = numProcMax;
    }
}
