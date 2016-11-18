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
public class Ftp {
    String ID;
    String Desc;
    String SourceServerID;
    String SourceUserID;
    String SourcePassID;
    String SourcePath;
    String SourceFile;
    boolean SourceIsPattern;
    boolean SourceIsFtp;
    String DestServerID;
    String DestUserID;
    String DestPassID;
    String DestPath;
    String DestFile;
    boolean DestIsPattern;
    boolean DestIsFtp;
    
    //Getter and Setter

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String Desc) {
        this.Desc = Desc;
    }

    public String getSourceServerID() {
        return SourceServerID;
    }

    public void setSourceServerID(String SourceServerID) {
        this.SourceServerID = SourceServerID;
    }

    public String getSourceUserID() {
        return SourceUserID;
    }

    public void setSourceUserID(String SourceUserID) {
        this.SourceUserID = SourceUserID;
    }

    public String getSourcePassID() {
        return SourcePassID;
    }

    public void setSourcePassID(String SourcePassID) {
        this.SourcePassID = SourcePassID;
    }

    public String getSourcePath() {
        return SourcePath;
    }

    public void setSourcePath(String SourcePath) {
        this.SourcePath = SourcePath;
    }

    public String getSourceFile() {
        return SourceFile;
    }

    public void setSourceFile(String SourceFile) {
        this.SourceFile = SourceFile;
    }

    public boolean isSourceIsPattern() {
        return SourceIsPattern;
    }

    public void setSourceIsPattern(boolean SourceIsPattern) {
        this.SourceIsPattern = SourceIsPattern;
    }

    public boolean isSourceIsFtp() {
        return SourceIsFtp;
    }

    public void setSourceIsFtp(boolean SourceIsFtp) {
        this.SourceIsFtp = SourceIsFtp;
    }

    public String getDestServerID() {
        return DestServerID;
    }

    public void setDestServerID(String DestServerID) {
        this.DestServerID = DestServerID;
    }

    public String getDestUserID() {
        return DestUserID;
    }

    public void setDestUserID(String DestUserID) {
        this.DestUserID = DestUserID;
    }

    public String getDestPassID() {
        return DestPassID;
    }

    public void setDestPassID(String DestPassID) {
        this.DestPassID = DestPassID;
    }

    public String getDestPath() {
        return DestPath;
    }

    public void setDestPath(String DestPath) {
        this.DestPath = DestPath;
    }

    public String getDestFile() {
        return DestFile;
    }

    public void setDestFile(String DestFile) {
        this.DestFile = DestFile;
    }

    public boolean isDestIsPattern() {
        return DestIsPattern;
    }

    public void setDestIsPattern(boolean DestIsPattern) {
        this.DestIsPattern = DestIsPattern;
    }

    public boolean isDestIsFtp() {
        return DestIsFtp;
    }

    public void setDestIsFtp(boolean DestIsFtp) {
        this.DestIsFtp = DestIsFtp;
    }
}
