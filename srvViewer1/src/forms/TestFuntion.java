/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forms;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author andresbenitez
 */
public class TestFuntion {
    
    
    @SuppressWarnings("empty-statement")
    public static void main(String args[]) {
    
        //Object [][] matriz = new Object[10][10];
        
        //List<List<Object>> matriz = new ArrayList<>();
        

        List<Object> rows = new ArrayList<>();
        
        Vector vrows = new Vector();
       
        
        DefaultTableModel model = new DefaultTableModel();
        
        
        model.addColumn("col1");
        model.addColumn("col2");
        model.addColumn("col3");
        
        vrows.add("datacol1");
        vrows.add("datacol2");
        vrows.add("datacol3");
        
        
        model.addRow(vrows);
        
        
        System.out.println(model.getRowCount());
        
        
        

        
        
        
        
        
        
        
    
    }
    
}
