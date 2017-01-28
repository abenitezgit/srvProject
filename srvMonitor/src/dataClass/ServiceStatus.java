/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataClass;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    int numProcMax;
    int srvEnable;
    int numProcActive;
    int srvPort;
    int maxTimeOffLine;
    int numProcRunning;
    int numProcReady;
    int numProcFinished;
    
    //Información de Procesos
    Map<String,TaskProcess> mapTask = new TreeMap<>();
    Map<String, AssignedTypeProc> mapAssignedTypeProc = new TreeMap<>();
    Map<String, ActiveTypeProc> mapActiveTypeProc = new HashMap<>();
    
    //Getter and Setter
    //
    
	public String getSrvID() {
		return srvID;
	}
	public int getNumProcRunning() {
		return numProcRunning;
	}
	public void setNumProcRunning(int numProcRunning) {
		this.numProcRunning = numProcRunning;
	}
	public int getNumProcReady() {
		return numProcReady;
	}
	public void setNumProcReady(int numProcReady) {
		this.numProcReady = numProcReady;
	}
	public int getNumProcFinished() {
		return numProcFinished;
	}
	public void setNumProcFinished(int numProcFinished) {
		this.numProcFinished = numProcFinished;
	}
	public void setSrvID(String srvID) {
		this.srvID = srvID;
	}
	public String getSrvHost() {
		return srvHost;
	}
	public void setSrvHost(String srvHost) {
		this.srvHost = srvHost;
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
	public boolean isSrvActive() {
		return srvActive;
	}
	public void setSrvActive(boolean srvActive) {
		this.srvActive = srvActive;
	}
	public boolean isActivePrimaryMonHost() {
		return isActivePrimaryMonHost;
	}
	public void setActivePrimaryMonHost(boolean isActivePrimaryMonHost) {
		this.isActivePrimaryMonHost = isActivePrimaryMonHost;
	}
	public boolean isSocketServerActive() {
		return isSocketServerActive;
	}
	public void setSocketServerActive(boolean isSocketServerActive) {
		this.isSocketServerActive = isSocketServerActive;
	}
	public boolean isConnectMonHost() {
		return isConnectMonHost;
	}
	public void setConnectMonHost(boolean isConnectMonHost) {
		this.isConnectMonHost = isConnectMonHost;
	}
	public boolean isAssignedTypeProc() {
		return isAssignedTypeProc;
	}
	public void setAssignedTypeProc(boolean isAssignedTypeProc) {
		this.isAssignedTypeProc = isAssignedTypeProc;
	}
	public boolean isKeepAliveActive() {
		return isKeepAliveActive;
	}
	public void setKeepAliveActive(boolean isKeepAliveActive) {
		this.isKeepAliveActive = isKeepAliveActive;
	}
	public boolean isSubRunProcActive() {
		return isSubRunProcActive;
	}
	public void setSubRunProcActive(boolean isSubRunProcActive) {
		this.isSubRunProcActive = isSubRunProcActive;
	}
	public boolean isLoadParam() {
		return isLoadParam;
	}
	public void setLoadParam(boolean isLoadParam) {
		this.isLoadParam = isLoadParam;
	}
	public boolean isLoadRutinas() {
		return isLoadRutinas;
	}
	public void setLoadRutinas(boolean isLoadRutinas) {
		this.isLoadRutinas = isLoadRutinas;
	}
	public int getNumProcMax() {
		return numProcMax;
	}
	public void setNumProcMax(int numProcMax) {
		this.numProcMax = numProcMax;
	}
	public int getSrvEnable() {
		return srvEnable;
	}
	public void setSrvEnable(int srvEnable) {
		this.srvEnable = srvEnable;
	}
	public int getNumProcActive() {
		return numProcActive;
	}
	public void setNumProcActive(int numProcActive) {
		this.numProcActive = numProcActive;
	}
	public int getSrvPort() {
		return srvPort;
	}
	public void setSrvPort(int srvPort) {
		this.srvPort = srvPort;
	}
	public int getMaxTimeOffLine() {
		return maxTimeOffLine;
	}
	public void setMaxTimeOffLine(int maxTimeOffLine) {
		this.maxTimeOffLine = maxTimeOffLine;
	}
	public Map<String, TaskProcess> getMapTask() {
		return mapTask;
	}
	public void setMapTask(Map<String, TaskProcess> mapTask) {
		this.mapTask = mapTask;
	}
	public Map<String, AssignedTypeProc> getMapAssignedTypeProc() {
		return mapAssignedTypeProc;
	}
	public void setMapAssignedTypeProc(Map<String, AssignedTypeProc> mapAssignedTypeProc) {
		this.mapAssignedTypeProc = mapAssignedTypeProc;
	}
	public Map<String, ActiveTypeProc> getMapActiveTypeProc() {
		return mapActiveTypeProc;
	}
	public void setMapActiveTypeProc(Map<String, ActiveTypeProc> mapActiveTypeProc) {
		this.mapActiveTypeProc = mapActiveTypeProc;
	} 
    
}
