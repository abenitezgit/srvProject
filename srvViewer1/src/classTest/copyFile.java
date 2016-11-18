/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author andresbenitez
 */
public class copyFile {
    
    public static void main(String args[]) throws IOException {
    
        File sourceFile = new File("\\grabacionesclaro.e-contact.cl/2011/2016041300/20160413_000118_00011008887674_98458726_TTR42-1460516478.154581.WAV");
        
        File destinationFile = new File("/Users/andresbenitez/Documents/paso/JOJOJO.WAV");
        InputStream in = new FileInputStream(sourceFile);
        OutputStream out = new FileOutputStream(destinationFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.close();
        
    
    }
}
