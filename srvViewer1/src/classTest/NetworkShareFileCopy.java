/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classTest;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

/**
 *
 * @author andresbenitez
 */
public class NetworkShareFileCopy {
    static final String USER_NAME = "e-contact\\abenitez";
    static final String PASSWORD = "pp";
    //e.g. Assuming your network folder is: \my.myserver.netsharedpublicphotos
    //static final String NETWORK_FOLDER = "smb://grabacionesclaro.e-contact.cl/2011/2016041300";
    static final String NETWORK_FOLDER = "smb://grabacionesclaro.e-contact.cl/2011/2016041300/";
 
    public static void main(String args[]) {
        String fileContent = "20160413_000118_00011008887674_98458726_TTR42-1460516478.154581.WAV";
 
        new NetworkShareFileCopy().copyFiles(fileContent, "20160413_000118_00011008887674_98458726_TTR42-1460516478.154581.WAV");
    }
 
    public boolean copyFiles(String fileContent, String fileName) {
        boolean successful = false;
         try{
                String user = USER_NAME + ":" + PASSWORD;
                System.out.println("User: " + user);
 
                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);
                String path = NETWORK_FOLDER + fileName;
                System.out.println("Path: " +path);
 
                SmbFile sFile = new SmbFile(path, auth);
                SmbFileOutputStream sfos = new SmbFileOutputStream(sFile);
                sfos.write(fileContent.getBytes());
 
                successful = true;
                System.out.println("Successful" + successful);
            } catch (Exception e) {
                successful = false;
                e.printStackTrace();
            }
        return successful;
    }
}
