/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvserver;

import dataClass.PoolProcess;
import utilities.globalAreaData;

/**
 *
 * @author andresbenitez
 */
public class testThread {
    static globalAreaData gDatos = new globalAreaData();
    static PoolProcess pool = new PoolProcess();
    
    public static void main(String args[]) {
        Thread th = new thExecFTP(gDatos, pool);
        th.start();
    }
    
    
    
}
