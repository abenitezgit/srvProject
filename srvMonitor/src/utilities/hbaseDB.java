/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author andresbenitez
 */
public class hbaseDB {
    static String cCLASS_NAME;
    static Configuration hcfg  = HBaseConfiguration.create();
    static String statusMessage;
    static int statusCode;
    
    
    //Constructor Inicia Conexion hacia un ambiente de HBASE
    //especificado en los parametros de entrada
    //
    public hbaseDB(String filePropertiesPath, String HBProperty) {
        Properties fileConf = new Properties();
        String [] rowsFile;
        
        try {
            fileConf.load(new FileInputStream(filePropertiesPath));
            rowsFile = fileConf.getProperty(HBProperty).split(",");
            
            for (int i=0; i< rowsFile.length; i++  ) {
                hcfg.addResource(new Path(rowsFile[i]));
            }
            
            //Return StatusCode y Message
            statusCode = 0;
            statusMessage = null;
        } catch (IOException | IllegalArgumentException e) {
            Logger.getLogger(hbaseDB.class.getName()).log(Level.SEVERE, null, e);
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();
        }            
    }
    
    //Area de Getter an Setter

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Configuration getHcfg() {
        return hcfg;
    }
    
    //Funciones con retorno de valor
    //
    
    private int getPosInArray(List<Object>  lista, String valor) {
        int PosInArray = -1;
        
        for (int i=0; i<lista.size();i++) {
            if (lista.get(i).toString().equals(valor)) {
                PosInArray = i;
                break;
            }
        }
        return(PosInArray);
    }
    
    public HTableInterface getConnectTable(String vHBTable) {
        try {
            HTablePool pool = new HTablePool(hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            statusCode = 0;
            statusMessage = null;
            
            return(hTable);
            
        } catch (IllegalArgumentException e) {
            Logger.getLogger(hbaseDB.class.getName()).log(Level.SEVERE, null, e);
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();
            return(null);
        }
    }
    
    public boolean isKeyValueExist(String vHBTable, String rowKey) throws IOException {
        boolean isExist = false;
    
        try {
            HTablePool pool = new HTablePool(hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            Get get = new Get(rowKey.getBytes());
            Result rs = hTable.get(get);
            
            if (!rs.isEmpty()) {
                isExist = true;
            } 
            
            statusCode = 0;
            statusMessage = null;
            
            return(isExist);
        } catch (IOException e) {
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();            
            return(isExist);
        } catch (Exception e) {
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();            
            return(isExist);
        }
    }
    
    public DefaultTableModel getRsQuery(String vHBTable, List<String> qualifier, byte[] prefix, List<Filter> filters){
        try {
            HTablePool pool = new HTablePool(hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            DefaultTableModel model = new DefaultTableModel();
            Vector<Vector<Object>>  data    = new  Vector<>();
            Vector<String>          cq_cols = new Vector<>();
            List<String>            cf_cols = new ArrayList<>();
            
            //Separa las Columnas Familias de las Columnas Qualifier
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(":");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }
            
            //Genera la consulta a la base de datos
            //
            
            
            FilterList fList = new FilterList( FilterList.Operator.MUST_PASS_ALL, filters);
            
            
            
            Scan scan = new Scan(prefix);
            scan.setFilter(fList);
            scan.setCaching(10000);
            
            //Ingresa los Filtros de Columnas de Familia y Qualifiers
            //
            for (int i=0; i<cf_cols.size(); i++) {
                scan.addColumn(Bytes.toBytes(cf_cols.get(i)), Bytes.toBytes(cq_cols.get(i)));
            }            
            
            //Genera la Consulta a la BD
            //
            ResultScanner HBResult = hTable.getScanner(scan);
            
            //Extrae la DATA del Scanner realizado
            //
            List<Object> list_cq =  new ArrayList<>();
            List<Object> list_key = new ArrayList<>();
            List<Object> list_val = new ArrayList<>();
            
            String cf;
            String cq;
            String key;
            String val;
            
            Result rowIntervals = HBResult.next();
            
            while (rowIntervals!=null) {
                
                Vector<Object> row = new Vector<>();
                list_key.clear();
                list_cq.clear();
                list_val.clear();

                for (KeyValue kv : rowIntervals.raw()) {
                    cf = new String(kv.getFamily());
                    cq = new String(kv.getQualifier());
                    key = Bytes.toStringBinary ( kv.getKey(), 2, kv.getRowLength() );
                    val = Bytes.toStringBinary( kv.getValue());
                    
                    list_key.add(key);
                    list_cq.add(cq);
                    list_val.add(val);
                }
                
                //Compara con Matriz de Columnna para Generar Registro Definitivo
                int pos=0;
                for (int i=0; i<cq_cols.size(); i++) {
                    pos = getPosInArray(list_cq, cq_cols.get(i));
                    if (pos>=0) {
                        row.add(list_val.get(pos).toString());
                    } else {
                        row.add("");
                    }
                }
                data.add(row);
                rowIntervals = HBResult.next();
            }
            
            model.setDataVector(data , cq_cols);
            
            statusCode = 0;
            statusMessage = null;
            
            return(model);
        
        } catch (Exception e) {
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();   
            return(null);
        }
    }
 
    public DefaultTableModel getRsQuery(String vHBTable, List<String> qualifier, String StartRow, String StopRow, List<Filter> filters){
        try {
            HTablePool pool = new HTablePool(hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            DefaultTableModel model = new DefaultTableModel();
            Vector<Vector<Object>>  data    = new  Vector<>();
            Vector<String>          cq_cols = new Vector<>();
            List<String>            cf_cols = new ArrayList<>();
            
            //Separa las Columnas Familias de las Columnas Qualifier
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(":");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }
            
            //Genera la consulta a la base de datos
            //
            FilterList fList = new FilterList( FilterList.Operator.MUST_PASS_ALL, filters);
        
            Scan scan = new Scan();
            scan.setFilter(fList);
            scan.setCaching(10000);
            
            //Ingresa los Filtros de Columnas de Familia y Qualifiers
            //
            for (int i=0; i<cf_cols.size(); i++) {
                scan.addColumn(Bytes.toBytes(cf_cols.get(i)), Bytes.toBytes(cq_cols.get(i)));
            }               
            
            //Agrega los Start y Stop del SCAN
            scan.setStartRow(Bytes.toBytes(StartRow));
            scan.setStopRow(Bytes.toBytes(StopRow));

            ResultScanner HBResult = hTable.getScanner(scan);
            
            //Extrae la DATA del Scanner realizado
            //
            List<Object> list_cq =  new ArrayList<>();
            List<Object> list_key = new ArrayList<>();
            List<Object> list_val = new ArrayList<>();
            
            String cf;
            String cq;
            String key;
            String val;
            
            Result rowIntervals = HBResult.next();
            
            while (rowIntervals!=null) {
                
                Vector<Object> row = new Vector<>();
                list_key.clear();
                list_cq.clear();
                list_val.clear();

                for (KeyValue kv : rowIntervals.raw()) {
                    cf = new String(kv.getFamily());
                    cq = new String(kv.getQualifier());
                    key = Bytes.toStringBinary ( kv.getKey(), 2, kv.getRowLength() );
                    val = Bytes.toStringBinary( kv.getValue());
                    
                    list_key.add(key);
                    list_cq.add(cq);
                    list_val.add(val);
                }
                
                //Compara con Matriz de Columnna para Generar Registro Definitivo
                int pos=0;
                for (int i=0; i<cq_cols.size(); i++) {
                    pos = getPosInArray(list_cq, cq_cols.get(i));
                    if (pos>=0) {
                        row.add(list_val.get(pos).toString());
                    } else {
                        row.add("");
                    }
                }
                data.add(row);
                rowIntervals = HBResult.next();
            }
            
            model.setDataVector(data , cq_cols);
            statusCode = 0;
            statusMessage = null;         
            
            return(model);
        
        } catch (Exception e) {
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();   
            return(null);
        }
    
    }
    
    public DefaultTableModel getRsQuery(String vHBTable, List<String> qualifier, List<Filter> filters){
        
        try {
            HTablePool pool = new HTablePool(hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            DefaultTableModel model = new DefaultTableModel();
            Vector<Vector<Object>>  data    = new  Vector<>();
            Vector<String>          cq_cols = new Vector<>();
            List<String>            cf_cols = new ArrayList<>();
            
            //Separa las Columnas Familias de las Columnas Qualifier
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(":");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }
            
            //Genera la consulta a la base de datos
            //
            FilterList fList = new FilterList( FilterList.Operator.MUST_PASS_ALL, filters);
        
            Scan scan = new Scan();
            scan.setFilter(fList);
            scan.setCaching(10000);

            ResultScanner HBResult = hTable.getScanner(scan);
            
            //Extrae la DATA del Scanner realizado
            //
            List<Object> list_cq =  new ArrayList<>();
            List<Object> list_key = new ArrayList<>();
            List<Object> list_val = new ArrayList<>();
            
            String cf;
            String cq;
            String key;
            String val;
            
            Result rowIntervals = HBResult.next();
            
            while (rowIntervals!=null) {
                
                Vector<Object> row = new Vector<>();
                list_key.clear();
                list_cq.clear();
                list_val.clear();

                for (KeyValue kv : rowIntervals.raw()) {
                    cf = new String(kv.getFamily());
                    cq = new String(kv.getQualifier());
                    key = Bytes.toStringBinary ( kv.getKey(), 2, kv.getRowLength() );
                    val = Bytes.toStringBinary( kv.getValue());
                    
                    list_key.add(key);
                    list_cq.add(cq);
                    list_val.add(val);
                }
                
                //Compara con Matriz de Columnna para Generar Registro Definitivo
                int pos=0;
                for (int i=0; i<cq_cols.size(); i++) {
                    pos = getPosInArray(list_cq, cq_cols.get(i));
                    if (pos>=0) {
                        row.add(list_val.get(pos).toString());
                    } else {
                        row.add("");
                    }
                }
                data.add(row);
                rowIntervals = HBResult.next();
            }
            
            model.setDataVector(data , cq_cols);
            
            statusCode = 0;
            statusMessage = null;
            
            return(model);        
        } catch (Exception e) {
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();
            return(null);
        }
    }

    public DefaultTableModel getRsQuery(String vHBTable, List<String> qualifier, String StartRow, String StopRow){
        
        try {
            HTablePool pool = new HTablePool(hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            DefaultTableModel model = new DefaultTableModel();
            Vector<Vector<Object>>  data    = new  Vector<>();
            Vector<String>          cq_cols = new Vector<>();
            List<String>            cf_cols = new ArrayList<>();
            
            //Separa las Columnas Familias de las Columnas Qualifier
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(":");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }
            
            //Crea el Scan de Consulta
            //
            Scan scan = new Scan();
            scan.setCaching(10000);
            
            //Ingresa los Filtros de Columnas de Familia y Qualifiers
            //
            for (int i=0; i<cf_cols.size(); i++) {
                scan.addColumn(Bytes.toBytes(cf_cols.get(i)), Bytes.toBytes(cq_cols.get(i)));
            }
            
            //Agrega los Start y Stop del SCAN
            //
            scan.setStartRow(Bytes.toBytes(StartRow));
            scan.setStopRow(Bytes.toBytes(StopRow));

            //Genera la Consulta a la BD
            //
            ResultScanner HBResult = hTable.getScanner(scan);
            
            //Extrae la DATA del Scanner realizado
            //
            List<Object> list_cq =  new ArrayList<>();
            List<Object> list_key = new ArrayList<>();
            List<Object> list_val = new ArrayList<>();
            
            String cf;
            String cq;
            String key;
            String val;
            
            Result rowIntervals = HBResult.next();
            
            while (rowIntervals!=null) {
                
                Vector<Object> row = new Vector<>();
                list_key.clear();
                list_cq.clear();
                list_val.clear();

                for (KeyValue kv : rowIntervals.raw()) {
                    cf = new String(kv.getFamily());
                    cq = new String(kv.getQualifier());
                    key = Bytes.toStringBinary ( kv.getKey(), 2, kv.getRowLength() );
                    val = Bytes.toStringBinary( kv.getValue());
                    
                    list_key.add(key);
                    list_cq.add(cq);
                    list_val.add(val);
                }
                
                //Compara con Matriz de Columnna para Generar Registro Definitivo
                int pos=0;
                for (int i=0; i<cq_cols.size(); i++) {
                    pos = getPosInArray(list_cq, cq_cols.get(i));
                    if (pos>=0) {
                        row.add(list_val.get(pos).toString());
                    } else {
                        row.add("");
                    }
                }
                data.add(row);
                rowIntervals = HBResult.next();
            }
            
            model.setDataVector(data , cq_cols);
            
            statusCode = 0;
            statusMessage = null;
            
            return(model);
        
        } catch (Exception e) {
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();
            return(null);
        }
    }

    public DefaultTableModel getRsQuery(String vHBTable, List<String> qualifier, long maxRows){
        
        try {
            HTablePool pool = new HTablePool(hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            DefaultTableModel model = new DefaultTableModel();
            Vector<Vector<Object>>  data    = new  Vector<>();
            Vector<String>          cq_cols = new Vector<>();
            List<String>            cf_cols = new ArrayList<>();
            
            //Separa las Columnas Familias de las Columnas Qualifier
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(":");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }
            
            //Crea el Scan de Consulta
            //
            Scan scan = new Scan();
            scan.setCaching(10000);
            
            //Ingresa los Filtros de Columnas de Familia y Qualifiers
            //
            for (int i=0; i<cf_cols.size(); i++) {
                scan.addColumn(Bytes.toBytes(cf_cols.get(i)), Bytes.toBytes(cq_cols.get(i)));
            }
            
            //Se agrega la maxima cantidad de filas a retornar
            //
            //scan.setMaxResultSize(maxRows);

            //Genera la Consulta a la BD
            //
            ResultScanner HBResult = hTable.getScanner(scan);
            
            //Extrae la DATA del Scanner realizado
            //
            List<Object> list_cq =  new ArrayList<>();
            List<Object> list_key = new ArrayList<>();
            List<Object> list_val = new ArrayList<>();
            
            String cf;
            String cq;
            String key;
            String val;
            
            Result rowIntervals = HBResult.next();
            
            while (rowIntervals!=null) {
                
                Vector<Object> row = new Vector<>();
                list_key.clear();
                list_cq.clear();
                list_val.clear();

                for (KeyValue kv : rowIntervals.raw()) {
                    cf = new String(kv.getFamily());
                    cq = new String(kv.getQualifier());
                    key = Bytes.toStringBinary ( kv.getKey(), 2, kv.getRowLength() );
                    val = Bytes.toStringBinary( kv.getValue());
                    
                    list_key.add(key);
                    list_cq.add(cq);
                    list_val.add(val);
                }
                
                //Compara con Matriz de Columnna para Generar Registro Definitivo
                int pos=0;
                for (int i=0; i<cq_cols.size(); i++) {
                    pos = getPosInArray(list_cq, cq_cols.get(i));
                    if (pos>=0) {
                        row.add(list_val.get(pos).toString());
                    } else {
                        row.add("");
                    }
                }
                data.add(row);
                rowIntervals = HBResult.next();
            }
            
            model.setDataVector(data , cq_cols);
            
            statusCode = 0;
            statusMessage = null;
            
            return(model);
        
        } catch (Exception e) {
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();
            return(null);
        }
    }
    
    
    //Procedimientos con Retorno de statusCode y Message
    //
    public void putDataRows(String vHBTable, String rowKey, List<String> qualifier) {
        try {
            HTablePool pool = new HTablePool(hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            List<String> cf_cols = new ArrayList<>();
            List<String> cq_cols = new ArrayList<>();
            List<String> vu_cols = new ArrayList<>();
        
            //Separa las Columnas Familias de las Columnas Qualifier y sus valores
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(",");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
                vu_cols.add(qa_paso[2]);
            }

            //Parametros de la conexion
            //
            Put p = new Put(Bytes.toBytes(rowKey));
            
            //Set valores a insertar / modificar
            //
            for (int cols=0; cols<qualifier.size(); cols++) {
                p.add(Bytes.toBytes(cf_cols.get(cols)), Bytes.toBytes(cq_cols.get(cols)),Bytes.toBytes(vu_cols.get(cols)));
            }
            
            hTable.put(p);
        
            statusCode = 0;
            statusMessage = null;
            
        } catch (Exception e) {
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();
        }
    }
    
    public void putDataRows(String vHBTable, List<String> rowKey, List<String> qualifier) {
        try {
            HTablePool pool = new HTablePool(hcfg,100000);
            HTableInterface hTable = pool.getTable(vHBTable);
            
            List<String> cf_cols = new ArrayList<>();
            List<String> cq_cols = new ArrayList<>();
            List<String> vu_cols = new ArrayList<>();
        
            //Separa las Columnas Familias de las Columnas Qualifier y sus valores
            //(Esto lo realiza por Default)
            //
            for (int i=0; i<qualifier.size(); i++) {
                String [] qa_paso;
                qa_paso = qualifier.get(i).split(",");
                cf_cols.add(qa_paso[0]);
                cq_cols.add(qa_paso[1]);
            }            
            
            statusCode = 0;
            statusMessage = null;
            
        } catch (Exception e) {
            statusCode = 99;
            statusMessage = cCLASS_NAME+" - "+e.getMessage();            
        }
    }
    
}
