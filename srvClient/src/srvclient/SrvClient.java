/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.table.DefaultTableModel;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author andresbenitez
 */
public class SrvClient {
    //Variables Globales de la Clase
    //
    static String glb_mdFileProperties;
    static String glb_mdDBType;
    
    
    public static void main(String[] args) throws IOException {
        SrvClient myClient = new SrvClient();
        
        myClient.getFileProperties("/Users/andresbenitez/Documents/apps/srvClient/conf/srvClient.properties");
        
        DefaultTableModel dtm = myClient.getHostServer();
        
        for (int i=0;i<dtm.getRowCount();i++) {
            for (int j=0; j<dtm.getColumnCount();j++) {
                System.out.println(dtm.getValueAt(i, j).toString());
            }
        }
        
    }
    
    private DefaultTableModel getHostServer() {
        try {
            hbaseDB connHB = new hbaseDB(glb_mdFileProperties,"HBConf2");

            List<Filter> filters = new ArrayList<>();

            SingleColumnValueFilter colValFilter = new SingleColumnValueFilter(Bytes.toBytes("srv"), Bytes.toBytes("online")
            ,CompareFilter.CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("1")));

            filters.add(colValFilter);

            //Se crea la matriz de columnas a devolver
            List<String> qualifiers = new ArrayList<>();

            //Agrega las Columnas y Qualifiers
            qualifiers.add("srv:hostIP");
            qualifiers.add("srv:hostPort");

            //Se ejecuta el Metodo connHB.getRsQuery
            //
            DefaultTableModel dtmData = connHB.getRsQuery("srvConf", qualifiers, filters);
        
            return dtmData;
        } catch (Exception e) {
            write(e.getMessage());
            return null;
        }
    }
    
    private void getFileProperties(String metaDataConfPath) throws IOException {
        Properties srvConf = new Properties();
        glb_mdFileProperties = metaDataConfPath;
        
        try {
        
            srvConf.load(new FileInputStream(metaDataConfPath));
            glb_mdDBType = srvConf.getProperty("DBType");
            
            switch (glb_mdDBType) {
                case ("SQL"):
                    break;
                default:
                    break;
            
            }
        
        } catch (IOException e) {
            System.out.println("Error: "+e.getMessage());
        }
    
    }
    
    private void write(String Message) {
        System.out.println(Message);
    }
    
    
}
