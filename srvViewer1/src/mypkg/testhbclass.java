/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mypkg;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author andresbenitez
 */
public class testhbclass {
    
    
    public static void main(String args[]) {
        //Se debe instanciar la clase hbaseDB()
        //
        hbaseDB connHB = new hbaseDB("/Users/andresbenitez/Documents/app/ABTViewer3/srvConf.properties","HBConf2");
        
        
        try {
            //Procedimiento para Conectarse a una TABLA
            //
            
            
            //Procedimiento para Ejecutar Consultas y Retornar ResultSet de Datos
            //
            //Crear Lista para Filtros
            //
            List<Filter> filters = new ArrayList<Filter>();
            
            //Definir los Filtros que se desean
            //
            Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,new BinaryComparator(Bytes.toBytes("srv00001")));
            
            //Se agrupan los filtros
            filters.add(filter1);
            
            //Se crea la matriz de columnas a devolver
            List<String> qualifiers = new ArrayList<>();
            
            qualifiers.add("name");
            qualifiers.add("txpKeep");
            qualifiers.add("txpKeep");
            qualifiers.add("txpProc");
            qualifiers.add("txpControl");
            qualifiers.add("logdir");
            
            //Se ejecuta el Metodo connHB.getRsQuery
            //
            DefaultTableModel dtmData = connHB.getRsQuery("srvConf", qualifiers, filters);
            
            for (int i=0; i<dtmData.getRowCount(); i++){
                System.out.println(dtmData.getColumnName(i)+ " " +dtmData.getValueAt(i, 0));
            }
            
            System.out.println("Total Filas: " + dtmData.getRowCount());
            
        } catch (Exception e) {
            System.out.println("Error Generico");
        }
    
    }
}
