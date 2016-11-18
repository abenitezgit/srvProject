/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.table.DefaultTableModel;
import mypkg.hbaseDB;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author andresbenitez
 */
public class testFind {
    
    
    public static void main(String args[]) throws IOException {
        hbaseDB conn = new hbaseDB("/Users/andresbenitez/Documents/app/ABTViewer3/srvConf.properties","HBConf2");
        
        //Crear Lista para Filtros
        //
        List<Filter> filters = new ArrayList<Filter>();
        
        //Definir los Filtros que se desean
        //
        //Para Agregar Filtro por Prefijos
        //byte[] prefix = Bytes.toBytes("C000012016");
        //Filter filter1 = new PrefixFilter(prefix);

        //
        Filter filter1 = new RowFilter(CompareFilter.CompareOp.GREATER_OR_EQUAL,new BinaryComparator(Bytes.toBytes("C00002")));
        
        SingleColumnValueFilter colValFilter = new SingleColumnValueFilter(Bytes.toBytes("grab"), Bytes.toBytes("status")
            , CompareFilter.CompareOp.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes("VALOR1")));

        SingleColumnValueFilter colValFilter2 = new SingleColumnValueFilter(Bytes.toBytes("grab"), Bytes.toBytes("url")
            , CompareFilter.CompareOp.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes("http://srv-gui-g.e-contact.cl/e-recorder/audio/20160310/09/01_20160310_095259")));

        colValFilter.setFilterIfMissing(true);
        colValFilter.filterRow();
        colValFilter.getLatestVersionOnly();
        

        //Se agrupan los filtros
        filters.add(filter1);

        //Se crea la matriz de columnas a devolver
        List<String> qualifiers = new ArrayList<>();

        //Agrega las Columnas
        //qualifiers.add("cli:name");
        qualifiers.add("grab:ani");
        qualifiers.add("grab:dnis");
        qualifiers.add("grab:dnis");
        qualifiers.add("grab:name");
        qualifiers.add("grab:rsrv");
        qualifiers.add("grab:scid");
        qualifiers.add("grab:size");
        qualifiers.add("grab:type");
        qualifiers.add("grab:url");
        qualifiers.add("time:ano");
        qualifiers.add("time:dia");
        qualifiers.add("time:fgrab");
        qualifiers.add("time:hora");
        qualifiers.add("time:mes");
        qualifiers.add("time:min");
        qualifiers.add("time:seg");
        qualifiers.add("time:semana");
        //qualifiers.add("user:name");

        //Se ejecuta el Metodo connHB.getRsQuery
        //
        //DefaultTableModel dtmData = conn.getRsQuery("hgrab", qualifiers, prefix, filters);
        DefaultTableModel dtmData = conn.getRsQuery("hgrab", qualifiers, filters);
        
        
        
        //Resultados
        //
        for (int rows=0; rows<dtmData.getRowCount();rows++) {
            for (int cols=0; cols<dtmData.getColumnCount();cols++) {
                System.out.print(dtmData.getValueAt(rows, cols) + " ");
            }
            System.out.println();
        }
        
        
    
    
    }
}
