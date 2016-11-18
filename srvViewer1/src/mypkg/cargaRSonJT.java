/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mypkg;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ABT
 */
public class cargaRSonJT {
    static int num_columnas;
    static int num_filas;

    public int getNum_columnas() {
        return num_columnas;
    }

    public static void setNum_columnas(int num_columnas) {
        cargaRSonJT.num_columnas = num_columnas;
    }

    public int getNum_filas() {
        return num_filas;
    }

    public static void setNum_filas(int num_filas) {
        cargaRSonJT.num_filas = num_filas;
    }
    
    
    public void update(javax.swing.JTable jTName, java.sql.ResultSet rsDB){
    int numRows;
    try {
        DefaultTableModel modelo = new DefaultTableModel();
        jTName.setModel(modelo);
        ResultSetMetaData rsMd = rsDB.getMetaData();
        int cantidadColumnas = rsMd.getColumnCount();
        setNum_columnas(cantidadColumnas);
        for (int i = 1; i <= cantidadColumnas; i++) {
            modelo.addColumn(rsMd.getColumnLabel(i));
        }
        numRows = 0;
        while (rsDB.next()) {
            numRows ++;
            Object[] fila = new Object[cantidadColumnas];
            for (int i = 0; i < cantidadColumnas; i++) {
                if (rsDB.getObject(i+1)!=null) {
                    fila[i]=rsDB.getObject(i+1);
                    //modelo.addRow(fila);
                } 
                    
                
            }
            modelo.addRow(fila);
        }
        setNum_filas(numRows);
    } catch (SQLException e) {
        }
    }

}
