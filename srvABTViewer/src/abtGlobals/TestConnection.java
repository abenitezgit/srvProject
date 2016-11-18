/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package abtGlobals;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class TestConnection {
    
    public static void main(String[] args) throws IOException {
    // Instantiating configuration class
        
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "hortonnodo1");
        config.set("hbase.zookeeper.property.clientport", "2181");
        config.set("zookeeper.znode.parent", "/hbase-unsecure"); //this is what most people miss :)
        
    }
    
}