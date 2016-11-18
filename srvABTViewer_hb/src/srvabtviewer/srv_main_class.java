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
    
    public static void main(String[] args) throws IOException {
    
        globalVARArea globalArea = new globalVARArea();
        
        globalArea.activaLog();        
        
        if (globalArea.isIsGetSrvConf()) { 
            //Inicio ModuloCentral
            try {
                new ttModuleControl(globalArea);
                globalArea.writeLog(0,"Inicio Servicio ServiceViewer");
            } catch (Exception e) {
                System.out.println("Main Error: Error reportado por Modulo de Control");
                System.out.println("Error: "+ globalArea.getgErrorNumber() + " " + globalArea.getgErrorMessages() );
                globalArea.writeLog(2,globalArea.getgErrorNumber() + " " + globalArea.getgErrorMessages());
            }
        } else {
            System.out.println("Error main_class: No se pudo recuperar parametros del servicio");
            System.out.println("Error: "+ globalArea.getgErrorNumber() + " " + globalArea.getgErrorMessages() );
            globalArea.writeLog(2,globalArea.getgErrorNumber() + " " + globalArea.getgErrorMessages());
        }
    }
}
