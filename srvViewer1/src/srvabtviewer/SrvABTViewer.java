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
public class SrvABTViewer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        globalVARArea globalArea = new globalVARArea("/Users/andresbenitez/Documents/app/ABTViewer3/srvConf.properties");
        
        Thread th1 = new thExecETL(globalArea);
        th1.start();
        System.out.println("Se ha lanzado el Thread TH1-ETL");
        
    }
    
}
