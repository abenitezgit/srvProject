/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.swing.JOptionPane;
import mypkg.hbaseDB;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;

/**
 *
 * @author andresbenitez
 */
public class fileTest {
    
    
    public static void main(String args[]) throws IOException {
        hbaseDB connHB = new hbaseDB("/Users/andresbenitez/Documents/app/ABTViewer3/srvConf.properties","HBConf2");
        
        FileSystem hdfs = org.apache.hadoop.fs.FileSystem.get(connHB.getHcfg());
        
        JOptionPane.showMessageDialog(null, hdfs.getHomeDirectory().toString());
        
        JOptionPane.showMessageDialog(null, hdfs.getWorkingDirectory());
               
        hdfs.setWorkingDirectory(new Path("hdfs://hortonserver.com:8020/user/guest/"));
    
        System.out.println(hdfs.getWorkingDirectory().toString());
    
        String dirName = "TestDirectory";
        Path destPath = new Path("hdfs://hortonserver.e-contact.cl:8020/user/guest/20160413_000118_00011008887674_98458726_TTR42-1460516478.154581.WAV");
        Path sr1 = new Path("hdfs://hortonserver.com:8020/user/guest/Test");
    
        //hdfs.mkdirs(sr1);
    
    
    
        //FileSystem lhdfs = LocalFileSystem.get(hbconf);
    
        //System.out.println(lhdfs.getWorkingDirectory().toString());
        //System.out.println(hdfs.getWorkingDirectory().toString());

        //Path sourcePath = new Path("/Users/andresbenitez/Documents/Apps/test.txt");

        //Path destPath = new Path("/Users/andresbenitez/Documents/Apps/test4.txt");

        //hdfs.copyFromLocalFile(sourcePath, destPath);
    
        //hdfs.copyToLocalFile(false, new Path("hdfs://sandbox.hortonworks.com:8020/user/guest/installupload.log"), new Path("/Users/andresbenitez/Documents/instaldown3.log"), true);

        //hdfs.copyToLocalFile(false, new Path("/Users/andresbenitez/Documents/instaldown.log"), new Path("hdfs://sandbox.hortonworks.com:8020/user/guest/installupload.log"), false);

        
        //File f=new File("http://srv-gui-g.e-contact.cl/e-recorder/audio/20160413/08/01_20160413_084721_90010990790034__1460548041.4646.wav");
        URL url = new URL("http://grabacionesclaro.e-contact.cl/2011/2016041300/20160413_000118_00011008887674_98458726_TTR42-1460516478.154581.WAV");
        
        File filePaso = new File("/Users/andresbenitez/Documents/paso/JOJOJO.WAV");
        
        File f2 = new File("/grabacionesclaro.e-contact.cl/2011/2016041300/20160413_000118_00011008887674_98458726_TTR42-1460516478.154581.WAV");
        
        org.apache.commons.io.FileUtils.copyURLToFile(url, filePaso);
        
        
        
        //org.apache.commons.io.FileUtils.copyFile(f2, filePaso);
        
        
        //&hdfs.copyToLocalFile(false, new Path("/Users/andresbenitez/Documents/paso/JOJOJO.mp3"), destPath);
        
        
        //hdfs.copyFromLocalFile(false, new Path("/Users/andresbenitez/Documents/paso/JOJOJO.WAV"), destPath);
        
    
    
    }
    
}
