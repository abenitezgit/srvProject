/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forms;

import abtGlobals.globalVARArea;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import mypkg.hbaseDB;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author andresbenitez
 */
public class frmInterval extends javax.swing.JFrame {
    
    //AREA VARIABLES GLOBALES DE LA CLASE
    //
    globalVARArea gDatos = new globalVARArea("/Users/andresbenitez/Documents/app/ABTViewer3/srvConf.properties");
    
    
    
    
    
    //AREA DEL CONSTRUCTOR DE LA CLASE
    //
    public frmInterval() {
        initComponents();
        
    }
    
    
    
    
    //AREA DE METODOS LOCALES
    //
    public void getDataServicios(JTable jtable) {
            // TODO add your handling code here:
        //Se debe instanciar la clase hbaseDB()
        //
        hbaseDB connHB = new hbaseDB(gDatos.getCfg_glb_metaDataConfPath(), "HBConf2");
        
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
            //Para Agregar Filtro por Prefijos
            byte[] prefix = Bytes.toBytes("srv");
            
            //
            //Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,new BinaryComparator(Bytes.toBytes("etl00001|")));
            Filter filter1 = new PrefixFilter(prefix);

            //Se agrupan los filtros
            filters.add(filter1);
            
            //Se crea la matriz de columnas a devolver
            List<String> qualifiers = new ArrayList<>();
            
            //Agrega las Columnas
            qualifiers.add("stat:srvID");
            //qualifiers.add("fecStart");
            //qualifiers.add("fecStop");
            qualifiers.add("stat:fecStart");
            //qualifiers.add("ustatus");
            qualifiers.add("stat:typeProc");
            qualifiers.add("stat:uStatus");
            //qualifiers.add("fecFin");
            qualifiers.add("stat:ufecExec");
            qualifiers.add("stat:threadNum");
            qualifiers.add("stat:appName");
            //qualifiers.add("errorNum");
            //qualifiers.add("errorMsg");
            
            //Se ejecuta el Metodo connHB.getRsQuery
            //
            DefaultTableModel dtmData = connHB.getRsQuery("srvStatus", qualifiers, prefix, filters);   
            
            jtable.setModel(dtmData);
            
            jtable.getColumnModel().getColumn(0).setPreferredWidth(60);
            jtable.getColumnModel().getColumn(1).setPreferredWidth(120);
            jtable.getColumnModel().getColumn(2).setPreferredWidth(60);
            jtable.getColumnModel().getColumn(3).setPreferredWidth(60);
            jtable.getColumnModel().getColumn(4).setPreferredWidth(120);
            
            
        
        } catch (Exception ex) {
            Logger.getLogger(frmInterval.class.getName()).log(Level.SEVERE, null, ex);
        }

    
    }
    
    
    public void getDataIntervals(JTable jtable) {
        //Se debe instanciar la clase hbaseDB()
        //
        hbaseDB connHB = new hbaseDB(gDatos.getCfg_glb_metaDataConfPath(), "HBConf2");
        
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
            //Para Agregar Filtro por Prefijos
            byte[] prefix = Bytes.toBytes("etl00001|");
            
            //
            //Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,new BinaryComparator(Bytes.toBytes("etl00001|")));
            Filter filter1 = new PrefixFilter(prefix);

            //Se agrupan los filtros
            filters.add(filter1);
            
            //Se crea la matriz de columnas a devolver
            List<String> qualifiers = new ArrayList<>();
            
            //Agrega las Columnas
            qualifiers.add("data:intervalID");
            //qualifiers.add("fecStart");
            //qualifiers.add("fecStop");
            qualifiers.add("data:status");
            //qualifiers.add("ustatus");
            qualifiers.add("data:fecIns");
            qualifiers.add("data:fecExec");
            //qualifiers.add("fecFin");
            qualifiers.add("data:rowsRead");
            qualifiers.add("data:rowsWrite");
            qualifiers.add("data:numExec");
            //qualifiers.add("errorNum");
            //qualifiers.add("errorMsg");
            
            //Se ejecuta el Metodo connHB.getRsQuery
            //
            DefaultTableModel dtmData = connHB.getRsQuery("tb_etlInterval", qualifiers, prefix, filters);
            
            jtable.setModel(dtmData);

            jtable.getColumnModel().getColumn(0).setPreferredWidth(110);
            jtable.getColumnModel().getColumn(1).setPreferredWidth(60);
            jtable.getColumnModel().getColumn(2).setPreferredWidth(120);
            jtable.getColumnModel().getColumn(3).setPreferredWidth(120);

        } catch (Exception ex) {
            Logger.getLogger(frmInterval.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable1.setGridColor(java.awt.Color.lightGray);
        jTable1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTable1FocusLost(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jButton2.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        jButton2.setText("Manual Refresh");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel1.setText("RowCount: ");

        jTextField1.setEditable(false);
        jTextField1.setBackground(new java.awt.Color(255, 255, 255));
        jTextField1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jTextField1.setToolTipText("");

        jTable2.setAutoCreateRowSorter(true);
        jTable2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTable2.setDebugGraphicsOptions(javax.swing.DebugGraphics.BUFFERED_OPTION);
        jScrollPane2.setViewportView(jTable2);

        jButton1.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        jButton1.setText("Manual Refresh");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBox1.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        jCheckBox1.setText("Auto Refresh");

        jSpinner1.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(3, null, null, 1));

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        jLabel2.setText("Segundos");

        jCheckBox2.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        jCheckBox2.setText("Auto Refresh");

        jSpinner2.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(3, null, null, 1));

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        jLabel3.setText("Segundos");

        jMenuBar1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 678, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBox1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(jLabel2))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBox2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(jLabel3))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(1, 1, 1)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 375, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jCheckBox1)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jCheckBox2)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    
    //AREA DE LOS COMPONENTES DEL FORMULARIO
    //
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //Ejecuta Metodo para Llenar JTABLE1
        getDataIntervals(this.jTable1);
        
        this.jTextField1.setText( String.valueOf(jTable1.getRowCount()));
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Ejecuta Metodo para Llenar JTABLE2
        getDataServicios(this.jTable2);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTable1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable1FocusLost
        // TODO add your handling code here:
        //showMessageDialog(null, "This language just gets better and better!");
    }//GEN-LAST:event_jTable1FocusLost

    //AREA DEL MAIN METODO
    //
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(frmInterval.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frmInterval.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frmInterval.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frmInterval.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmInterval().setVisible(true);
            }
        });
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
