/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ABT
 */
public class cargaJTonJT {
        
        public void update(javax.swing.JTable jTNameDest, javax.swing.JTable jTNameOri, int maxRows){
            DefaultTableModel modelo = new DefaultTableModel();
            jTNameDest.setModel(modelo);
            
            int cantidadColumnas = jTNameOri.getColumnCount();
            
            for (int i = 0; i < cantidadColumnas; i++) {
                modelo.addColumn(jTNameOri.getColumnName(i));
            }
            
            int cantidadFilas = jTNameOri.getRowCount();
            
            /*if (cantidadFilas>maxRows) { cantidadFilas=maxRows; }*/
            
            for (int j=0; j<cantidadFilas;j++) {
                Object[] fila = new Object[cantidadColumnas];
                for (int i = 0; i < cantidadColumnas; i++) {
                    fila[i]=jTNameOri.getValueAt(j,i);
                }
                modelo.addRow(fila);
            }
}
}
