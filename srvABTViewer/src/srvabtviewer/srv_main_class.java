/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvabtviewer;

import abtGlobals.globalVARArea;

/**
 *
 * @author ABT
 */
public class srv_main_class {
    
    public static void main(String[] args) {
    
        globalVARArea globalArea = new globalVARArea();
        
        if (globalArea.isIsGetAbtConf()) { 
            //Inicio ModuloCentral
            new ttModuleControl(globalArea);
        } else {
        }
    }
}
