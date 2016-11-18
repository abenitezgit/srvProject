/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvabtviewer;

import abtGlobals.globalVARArea;
import java.io.IOException;

/**
 *
 * @author ABT
 */
public class srv_main_class {
    static String cCLASS_NAME = "srv_main_class";
    
    public static void main(String[] args) throws IOException {
        String cMETHOD_NAME = "main";
    
        globalVARArea globalArea = new globalVARArea("/Users/andresbenitez/Documents/app/ABTViewer3/srvConf.properties");
        
        if (globalArea.isIsGetSrvConf()) { 
            //Inicio ModuloCentral
            try {
                new ttModuleControl(globalArea);
                globalArea.writeLog(0,cMETHOD_NAME, cMETHOD_NAME, "Inicio Servicio ServiceViewer");
            } catch (Exception e) {
                System.out.println("Main Error: Error reportado por Modulo de Control");
                System.out.println("Error: "+ globalArea.getgErrorNumber() + " " + globalArea.getgErrorMessages() );
                globalArea.writeLog(2,cMETHOD_NAME, cMETHOD_NAME, globalArea.getgErrorNumber() + " " + globalArea.getgErrorMessages());
            }
        } else {
            System.out.println("Error main_class: No se pudo recuperar parametros del servicio");
            System.out.println("Error: "+ globalArea.getgErrorNumber() + " " + globalArea.getgErrorMessages() );
            globalArea.writeLog(2,cMETHOD_NAME, cMETHOD_NAME, globalArea.getgErrorNumber() + " " + globalArea.getgErrorMessages());
        }
    }
}
