package srvabtviewer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import abtGlobals.globalVARArea;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import mypkg.hbaseDB;
import mypkg.sqlDB;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author ABT
 */
public class thExecETL extends Thread{
    String cCLASS_NAME = "thExecETL";
    globalVARArea gDatos;
    srv_rutinas gRutinas;
    int statusCodeProc;
    String statusMessageProc;
    
    //Conectores a Bases de Datos
    //
    
        private hbaseDB connHB;
        
        private sqlDB vSqlConnDB;
        private ResultSet glb_rsSQL;

    
    //Establece variables para recuperar los Parametros del Proceso 
    //desde table tb_etlConf
    //
    static String cETL_ACTIVE;
    static String cETL_CLIID;
    static String cETL_CLINAME;
    static String cETL_DBDESTNAME;
    static String cETL_DBDESTPASS;
    static String cETL_DBDESTPORT;
    static String cETL_DBDESTTABLENAME;
    static String cETL_DBDESTTYPE;
    static String cETL_DBDESTUSER;
    static String cETL_DBDESTSERVERIP;
    static String cETL_DBSOURCENAME;
    static String cETL_DBSOURCEPASS;
    static String cETL_DBSOURCEPORT;
    static String cETL_DBSOURCETABLENAME;
    static String cETL_DBSOURCETYPE;
    static String cETL_DBSOURCEUSER;
    static String cETL_DBSOURCESERVERIP;
    static String cETL_DESC;
    static String cETL_FIELDKEYEXTRACT;
    static String cETL_INTERVALEXTRACT;
    static String cETL_TIMEGAP;
    static String cETL_INTERVALUNIDAD;
    static String cETL_TIMEINIGEN;
    
    //Establece variables para recuperar valores de Match de Campos
    //desde tabla tb_etlMatch
    //
    private String [] vField_etlActive = new String[1000];
    private String [] vField_etlDestDataType= new String[1000];
    private String [] vField_etlDestField= new String[1000];
    private String [] vField_etlDestLength= new String[1000];
    private String [] vField_etlOrder= new String[1000];
    private String [] vField_etlSourceDataType= new String[1000];
    private String [] vField_etlSourceField= new String[1000];
    private String [] vField_etlSourceLength= new String[1000];
    private String [] vField_etlSourceReadType= new String[1000];
    private String [] vIntervalID = new String[1000];
    private String [] vIntervalExecutions = new String[1000];
    private String [] vIntervalFechaIns = new String[1000];
    private boolean glb_isExistRowsSQLExtract;
    
    //Constructor Instancia VAR AREA GLOBAL y ACTIVA LOG
    public thExecETL(globalVARArea m) throws IOException {
        gDatos = m;
        gRutinas = new srv_rutinas(gDatos);
        connHB = new hbaseDB("/Users/andresbenitez/Documents/app/ABTViewer3/srvConf.properties","HBConf2");
    }
    
    //EJECUTADO AUTOMATICAMENTE POR SER UN THREAD
    public void run() {
        String cMETHOD_NAME = "run";
        
        try {
            //
            //
            gRutinas.updateSrvStatus(connHB, "ETL","Running");

            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Iniciando Modulo");

            if (loadETLParam("etl00001")) {
                gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Parametros ETL cargados");
                insertETLInterval("etl00001");
                if (loadETLIntervalParam("etl00001")) {
                    gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Parametros de Intervalos Recuperados");

                    //Inicia Proceso de Extraccion de Datos

                    //Registra Inicio del Proceso en tb_etlInterval
                    if (setETLStartup("etl00001", vIntervalID[0])) {

                        if (genETLExtraccion("etl00001", vIntervalID[0])) {
                            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Proceso Extraccion Exitoso");
                        } else {
                            gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Proceso Extraccion con Errores");
                        }

                        if (setETLFinished("etl00001", vIntervalID[0])) {
                            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Proceso Extraccion Terminado");
                        }
                    } else {
                        gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "No fue posible Iniciar Proceso de Extracción");
                    }

                } else {
                    gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "No hay intervalos a Procesar");
                }
            } else {
                gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "No se pudo recuperar los parametros del ETL");
            }

            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Finalizando Modulo");

            if (decreaseThreadProc("ETL")) {
                gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Finalizando Modulo ETL Exitosamente");
            } else {
                gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Finalizando Modulo Con errores");
            }

            gRutinas.updateSrvStatus(connHB, "ETL","Sleeping");
            
        } catch (Throwable ex) {
            gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Error general: "+ex.getMessage());
            try {
                if (!decreaseThreadProc("ETL")) {
                    gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Finalizando Modulo Con errores");
                } 
                gRutinas.updateSrvStatus(connHB, "ETL","Sleeping");
            } catch (IOException ex1) {
                Logger.getLogger(thExecETL.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    } //FIN run()
    
    public boolean decreaseThreadProc(String vTypeProc) {
    boolean isDecrease = false;

    int usedThread;
    List<String> vlist;
    String [] row;


    vlist = gDatos.getgThUsedxProc();

    int items = vlist.size();
    for (int i=0; i<items; i++) {
        row = vlist.get(i).split(":");
        if (row[0].equals(vTypeProc)) {
            usedThread = Integer.parseInt(row[1]);
            vlist.remove(i);
            vlist.add(vTypeProc+":"+String.valueOf(usedThread-1));
            isDecrease = true;
        }
    }

    if (!isDecrease) {
        vlist.add(vTypeProc+":1");
        isDecrease = true;
    }

    gDatos.setgThUsedxProc(vlist);

    return(isDecrease);
}
    
    public boolean loadETLParam(String vProc) throws ZooKeeperConnectionException, IOException {
        String cMETHOD_NAME = "loadETLParam";
        boolean isLoadETLParam = false;
        
        //Procedimiento para Ejecutar Consultas y Retornar ResultSet de Datos
        //
        //Crear Lista para Filtros
        //
        List<Filter> filters = new ArrayList<>();

        //Definir los Filtros que se desean
        //
        //Para Agregar Filtro por Prefijos
        //byte[] prefix = Bytes.toBytes("srv");

        //
        //Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,new BinaryComparator(Bytes.toBytes("C00002|")));
        //Filter filter2 = new ColumnPrefixFilter((Bytes.toBytes("grab")));
        //Filter filter1 = new PrefixFilter(prefix);
        Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,new BinaryComparator(Bytes.toBytes(vProc)));

        //Se agrupan los filtros
        filters.add(filter1);
        //filters.add(filter2);

        //Se crea la matriz de columnas a devolver
        List<String> qualifiers = new ArrayList<>();

        //Agrega las Columnas y Qualifiers
        qualifiers.add("conf:etlActive");
        qualifiers.add("conf:etlCliID");
        qualifiers.add("conf:etlCliName");
        qualifiers.add("conf:etlDBDestName");
        qualifiers.add("conf:etlDBDestPass");
        qualifiers.add("conf:etlDBDestPort");
        qualifiers.add("conf:etlDBDestTableName");
        qualifiers.add("conf:etlDBDestType");
        qualifiers.add("conf:etlDBDestUser");
        qualifiers.add("conf:etlDBSourceName");
        qualifiers.add("conf:etlDBSourcePass");
        qualifiers.add("conf:etlDBSourcePort");
        qualifiers.add("conf:etlDBSourceTableName");
        qualifiers.add("conf:etlDBSourceType");
        qualifiers.add("conf:etlDBSourceUser");
        qualifiers.add("conf:etlDesc");
        qualifiers.add("conf:etlFieldKeyExtract");
        qualifiers.add("conf:etlIntervalExtract");
        qualifiers.add("conf:etlIntervalUnidad");
        qualifiers.add("conf:etlTimeGap");
        qualifiers.add("conf:etlTimeIniGen");
        qualifiers.add("conf:etlDBSourceServerIP");
        qualifiers.add("conf:etlDBDestServerIP");

        //Se ejecuta el Metodo connHB.getRsQuery
        //
        DefaultTableModel dtmData = connHB.getRsQuery("tb_etlConf", qualifiers, filters);
        
        if (dtmData.getRowCount()!=0) {
            for (int i=0; i<dtmData.getColumnCount();i++) {
                switch (dtmData.getColumnName(i)) {
                    case "etlActive":
                        cETL_ACTIVE = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlCliID":
                        cETL_CLIID = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlCliName":
                        cETL_CLINAME = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBDestName":
                        cETL_DBDESTNAME = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBDestPass":
                        cETL_DBDESTPASS = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBDestPort":
                        cETL_DBDESTPORT = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBDestTableName":
                        cETL_DBDESTTABLENAME = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBDestType":
                        cETL_DBDESTTYPE = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBDestUser":
                        cETL_DBDESTUSER = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBSourceName":
                        cETL_DBSOURCENAME = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBSourcePass":
                        cETL_DBSOURCEPASS = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBSourcePort":
                        cETL_DBSOURCEPORT = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBSourceTableName":
                        cETL_DBSOURCETABLENAME = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBSourceType":
                        cETL_DBSOURCETYPE = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBSourceUser":
                        cETL_DBSOURCEUSER = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDesc":
                        cETL_DESC = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlFieldKeyExtract":
                        cETL_FIELDKEYEXTRACT = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlIntervalExtract":
                        cETL_INTERVALEXTRACT = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlTimeGap":
                        cETL_TIMEGAP = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlIntervalUnidad":
                        cETL_INTERVALUNIDAD = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlTimeIniGen":
                        cETL_TIMEINIGEN = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBSourceServerIP":
                        cETL_DBSOURCESERVERIP = dtmData.getValueAt(0, i).toString();
                        break;
                    case "etlDBDestServerIP":
                        cETL_DBDESTSERVERIP = dtmData.getValueAt(0, i).toString();
                        break;
                    default:
                        break;
                }
            }
        }
        
        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Cargando Match de Campos");
        
        //Procedimiento para Ejecutar Consultas y Retornar ResultSet de Datos
        //
        //Crear Lista para Filtros
        //
        List<Filter> filters2 = new ArrayList<>();

        //Definir los Filtros que se desean
        //
        //Para Agregar Filtro por Prefijos
        //byte[] prefix = Bytes.toBytes("srv");

        //
        //Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,new BinaryComparator(Bytes.toBytes("C00002|")));
        //Filter filter2 = new ColumnPrefixFilter((Bytes.toBytes("grab")));
        //Filter filter1 = new PrefixFilter(prefix);
        //Filter filter2 = new RowFilter(CompareFilter.CompareOp.EQUAL,new BinaryComparator(Bytes.toBytes(vProc)));
        SingleColumnValueFilter colValFilter = new SingleColumnValueFilter(Bytes.toBytes("match"), Bytes.toBytes("etlActive")
                , CompareFilter.CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("1")));

        //Se agrupan los filtros
        filters2.add(colValFilter);
        //filters.add(filter2);
        
        //Se setean el StartRow y StopRow
        String StartRow = vProc + "|" + "00000";
        String StopRow = vProc + "|" + "99999";

        //Se crea la matriz de columnas a devolver
        List<String> qualifiers2 = new ArrayList<>();

        //Agrega las Columnas y Qualifiers
        qualifiers2.add("match:etlActive");
        qualifiers2.add("match:etlDestDataType");
        qualifiers2.add("match:etlDestField");
        qualifiers2.add("match:etlDestLength");
        qualifiers2.add("match:etlOrder");
        qualifiers2.add("match:etlSourceDateType");
        qualifiers2.add("match:etlSourceField");
        qualifiers2.add("match:etlSourceLength");
        qualifiers2.add("match:etlSourceReadType");

        //Se ejecuta el Metodo connHB.getRsQuery
        //
        DefaultTableModel dtmData2 = connHB.getRsQuery("tb_etlMatch", qualifiers2, StartRow, StopRow, filters2);

        if (dtmData2.getRowCount()!=0) {
            for (int rows=0; rows<dtmData2.getRowCount(); rows++) {
                for (int cols=0; cols<dtmData2.getColumnCount(); cols++) {
                    switch (dtmData2.getColumnName(cols)) {
                        case "etlActive":
                            vField_etlActive[rows] = dtmData2.getValueAt(rows, cols).toString();
                            break;
                        case "etlDestDataType":
                            vField_etlDestDataType[rows] = dtmData2.getValueAt(rows, cols).toString();
                            break;
                        case "etlDestField":
                            vField_etlDestField[rows] = dtmData2.getValueAt(rows, cols).toString();
                            break;
                        case "etlDestLength":
                            vField_etlDestLength[rows] = dtmData2.getValueAt(rows, cols).toString();
                            break;
                        case "etlOrder":
                            vField_etlOrder[rows] = dtmData2.getValueAt(rows, cols).toString();
                            break;
                        case "etlSourceDataType":
                            vField_etlSourceDataType[rows] = dtmData2.getValueAt(rows, cols).toString();
                            break;
                        case "etlSourceField":
                            vField_etlSourceField[rows] = dtmData2.getValueAt(rows, cols).toString();
                            break;
                        case "etlSourceLength":
                            vField_etlSourceLength[rows] = dtmData2.getValueAt(rows, cols).toString();
                            break;
                        case "etlSourceReadType":
                            vField_etlSourceReadType[rows] = dtmData2.getValueAt(rows, cols).toString();
                            break;
                        default:
                            break;
                        
                    }
                }
            }
        
        }

        isLoadETLParam = true;
    
        return(isLoadETLParam);
    }
    
    public void insertETLInterval(String vProc) {
        String cMETHOD_NAME = "insertETLInterval";
        
        
        try {
            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Inicio Proceso Inscripcion");

            //Extre Fecha Actual
            Date today;
            Date fecGap;
            Date fecIni;
            Date fecItera;
            Date fecIntervalIni;
            Date fecIntervalFin;

            int MinItera;
            int HoraItera;
            int DiaItera;
            int MesItera;
            int AnoItera;

            long numInterval;
            String localIntervalID;
            String todayChar;


            //Setea Fecha Actual
            //
            today = new Date();


            //Setea Fecha GAP - Desface de tiempo en extraccion
            //
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MINUTE, -(Integer.valueOf(cETL_TIMEGAP)+Integer.valueOf(cETL_INTERVALEXTRACT)));
            fecGap = c.getTime();

            //Setea Fecha Inicio Inscripcion/Revision de Intervalos

            c.setTime(today);
            c.add(Calendar.MINUTE, -Integer.valueOf(cETL_TIMEINIGEN));
            fecIni = c.getTime();

            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Fecha Actual: "+ today);
            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Fecha GAP   : "+ fecGap);
            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Fecha IniIns: "+ fecIni);

            //System.out.println("today   : "+ today);
            //System.out.println("fecGap  : "+ fecGap);
            //System.out.println("fecIni  : "+ fecIni);

            fecItera = fecIni;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdfToday = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String IntervalIni;
            String IntervalFin;
            List<String> qualifier = new ArrayList<>();

            while (fecItera.compareTo(fecGap) < 0) {
                //Extrae Intervalo para Fecha fecItera
                //
                c.setTime(fecItera);
                AnoItera = c.get(Calendar.YEAR);
                MesItera = c.get(Calendar.MONTH);
                DiaItera = c.get(Calendar.DAY_OF_MONTH);
                HoraItera = c.get(Calendar.HOUR_OF_DAY);
                MinItera = c.get(Calendar.MINUTE);

                //Valida si el intervalo de extraccion (cETL_INTERVALUNIDAD) es por:
                //  Minutos     : 0
                //  Horas       : 1
                //  Dias        : 2
                //  Semanas     : 3
                //  Mensuales   : 4
                //  Anuales     : 5

                switch (cETL_INTERVALUNIDAD) {
                    case "0":
                        fecIntervalIni = null;
                        fecIntervalFin = null;
                        numInterval = 60/Integer.valueOf(cETL_INTERVALEXTRACT);
                        for (int i=1;i<=numInterval;i++) {
                            c.set(AnoItera, MesItera, DiaItera, HoraItera, (i)*Integer.valueOf(cETL_INTERVALEXTRACT),0);
                            fecIntervalFin = c.getTime();
                            if (fecIntervalFin.compareTo(fecItera) >0 ) {
                                c.set(AnoItera, MesItera, DiaItera, HoraItera, (i-1)*Integer.valueOf(cETL_INTERVALEXTRACT),0);
                                fecIntervalIni = c.getTime();
                                break;
                            }
                        }
                        c.setTime(fecItera);
                        c.add(Calendar.MINUTE, Integer.valueOf(cETL_INTERVALEXTRACT));
                        fecItera = c.getTime();

                        
                        IntervalIni = sdf.format(fecIntervalIni);
                        IntervalFin = sdf.format(fecIntervalFin);
                        localIntervalID = IntervalIni+'-'+IntervalFin;
                        
                        //System.out.println(fecIntervalIni);
                        //System.out.println(fecIntervalFin);                        


                        if (connHB.isKeyValueExist("tb_etlInterval", vProc+"|"+localIntervalID)) {
                            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "rowKey: "+ vProc+"|"+localIntervalID +" Existe!!");
                            
                        } else {
                            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "rowKey: "+ vProc+"|"+localIntervalID +" NO Existe!!");
                            
                            //Preperando Datos para Inscribir
                            //
                            qualifier.add("data,intervalID,"+localIntervalID);
                            qualifier.add("data,fecIns,"+sdfToday.format(today));
                            qualifier.add("data,status,Ready");
                            qualifier.add("data,numExec,0");
                            
                            connHB.putDataRows("tb_etlInterval", vProc+"|"+localIntervalID, qualifier);
                            if (connHB.getStatusCode()==0) {
                                gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Intervalo: "+localIntervalID +" Inscrito Correctamente");
                            } else {
                                gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Error Inscribiendo Intervalo: "+localIntervalID);
                            }
                        }

                        break;

                    case "1":
                        fecIntervalIni = null;
                        fecIntervalFin = null;
                        numInterval = 24/Integer.valueOf(cETL_INTERVALEXTRACT);
                        for (int i=1;i<=numInterval;i++) {
                            c.set(AnoItera, MesItera, DiaItera, (i)*Integer.valueOf(cETL_INTERVALEXTRACT), 0, 0);
                            fecIntervalFin = c.getTime();
                            if (fecIntervalFin.compareTo(fecItera) >0 ) {
                                c.set(AnoItera, MesItera, DiaItera, (i-1)*Integer.valueOf(cETL_INTERVALEXTRACT), 0, 0);
                                fecIntervalIni = c.getTime();
                                break;
                            }
                        }
                        c.setTime(fecItera);
                        c.add(Calendar.HOUR_OF_DAY, Integer.valueOf(cETL_INTERVALEXTRACT));
                        fecItera = c.getTime();

                        System.out.println(fecIntervalIni);
                        System.out.println(fecIntervalFin);

                        break;

                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    default:
                }

            }
            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Termino Proceso");

        } catch (Exception e) {
            gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Error: "+ e.getMessage());
        }
    }
    
    public boolean loadETLIntervalParam(String vProc) throws IOException {
        String cMETHOD_NAME = "loadETLIntervalParam";
        boolean isLoadETLIntervalParam = false;
        
        try {
        
            //Setea los Filtros
            //
            List<Filter> filters = new ArrayList<>();

            SingleColumnValueFilter colValFilter = new SingleColumnValueFilter(Bytes.toBytes("data"), Bytes.toBytes("status")
                    , CompareFilter.CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("Ready")));

            colValFilter.setFilterIfMissing(false);
            colValFilter.filterRow();
            colValFilter.getLatestVersionOnly();

            //Asocia Prefijo
            //
            byte[] prefix = Bytes.toBytes(vProc + "|");
            Scan scan = new Scan(prefix);
            Filter prefixFilter = new PrefixFilter(prefix);

            //Agrupa los Filtros
            //
            filters.add(colValFilter);        
            filters.add(prefixFilter);

            //Se crea la matriz de columnas a devolver
            List<String> qualifiers = new ArrayList<>();

            //Agrega las Columnas y Qualifiers
            qualifiers.add("data:intervalID");
            qualifiers.add("data:numExec");
            qualifiers.add("data:fecIns");
            qualifiers.add("data:status");

            //Genera consluta Hbase
            //
            DefaultTableModel dtmData = connHB.getRsQuery("tb_etlInterval", qualifiers, prefix, filters);

            if (dtmData.getRowCount()!=0) {
                //for (int rows=0; rows<dtmData.getRowCount(); rows++) {
                for (int rows=0; rows<1; rows++) {
                    for (int cols=0; cols<dtmData.getColumnCount(); cols++) {
                        switch (dtmData.getColumnName(cols)) {
                            case "intervalID":
                                vIntervalID[rows] = dtmData.getValueAt(rows, cols).toString();
                                break;
                            case "executions":
                                vIntervalExecutions[rows] = dtmData.getValueAt(rows, cols).toString();
                                break;
                            case "FechaIns":
                                vIntervalFechaIns[rows] = dtmData.getValueAt(rows, cols).toString();
                                break;
                            default:
                                break;
                        }
                    }
                }
                isLoadETLIntervalParam = true;
                gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Intervalos Ready: " + dtmData.getRowCount());
            } else {
                isLoadETLIntervalParam = false;
                gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Sin intervalos disponibles");
            }

            return(isLoadETLIntervalParam);
        } catch (Exception e) {
            gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Error general: "+e.getMessage());
            return(false);
        }
    }
    
    public boolean setETLFinished(String vProc, String vIntervalID) throws IOException {
        boolean isETLFinished = false;
        
        try {
        
            //Extrae Fecha de Hoy

            Date today;
            String output;
            SimpleDateFormat formatter;

            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            today = new Date();
            output = formatter.format(today);

            String RowKey = vProc + "|" + vIntervalID;

            // Define el Set de datos a insertar o actualizar
            //
            //Se crea la matriz de columnas a devolver
            List<String> qualifiers = new ArrayList<>();

            //Agrega las Columnas y Qualifiers
            qualifiers.add("data,status,Running");
            qualifiers.add("data,fechaExec,"+output);
            qualifiers.add("data,rowReads,1500");
            qualifiers.add("data,rowWrites,1200");
            qualifiers.add("data,executions,1");

            connHB.putDataRows("tb_etlInterval", RowKey, qualifiers);
            if (connHB.getStatusCode()==0) {
                isETLFinished = true;
            } else {
                isETLFinished = false;
            }

            return(isETLFinished);
        } catch (Exception e) {
            return(false);
        }        
    }
    
    public boolean setETLStartup(String vProc, String vIntervalID) throws IOException {
        String cMETHOD_NAME = "setETLStartup";
        boolean isETLStartup = false;
        
        try {
        
            //Extrae Fecha de Hoy
            gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Marcando intervalo: "+vIntervalID+" en estado Running");

            Date today;
            String output;
            SimpleDateFormat formatter;

            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            today = new Date();
            output = formatter.format(today);

            String RowKey = vProc + "|" + vIntervalID;

            // Define el Set de datos a insertar o actualizar
            //
            //Se crea la matriz de columnas a devolver
            List<String> qualifiers = new ArrayList<>();

            //Agrega las Columnas y Qualifiers
            qualifiers.add("data,status,Running");
            qualifiers.add("data,fechaExec,"+output);

            connHB.putDataRows("tb_etlInterval", RowKey, qualifiers);
            if (connHB.getStatusCode()==0) {
                isETLStartup = true;
            } else {
                isETLStartup = false;
            }

            return(isETLStartup);
        } catch (Exception e) {
            gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Error general: "+e.getMessage());
            return(false);
        }
    }
    
    public boolean sqlConnectDBSource() {
        String cMETHOD_NAME = "sqlConnectDBSource";
        boolean isSqlConnectDBSource = false;
        sqlDB gConnSQL = new sqlDB();
        
        gConnSQL.setHostIp(cETL_DBSOURCESERVERIP);
        gConnSQL.setDbName(cETL_DBSOURCENAME);
        gConnSQL.setDbPort(cETL_DBSOURCEPORT);
        gConnSQL.setDbUser(cETL_DBSOURCEUSER);
        gConnSQL.setDbPass(cETL_DBSOURCEPASS);

        try {
            gConnSQL.conectar();
            vSqlConnDB = gConnSQL;

            isSqlConnectDBSource = true;
        } catch (Exception e) {
            isSqlConnectDBSource = false;
            gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "Error de Conexión a BD. Error: " + e.getMessage());
        }
        
        return(isSqlConnectDBSource);
    }
    
    public boolean sqlQueryDBSource(String vIntervalID) throws SQLException {
        String cMETHOD_NAME = "sqlQueryDBSource";
        boolean isSqlQueryDBSource = false;
        String vFecInicio = null;
        String vFecTermino = null;
        
        //Genera String de Conexion
        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Generando Query Origen");
        
        int swHead = 1;
        String vSQL = "select ";
        
        for (int i=0; i<1; i++) {
            switch (vField_etlSourceReadType[i]) {
                case "F":
                    vSQL = vSQL + vField_etlSourceField[i];
                    if (i<(1-1)) { // No es el último Match de Campos
                        vSQL = vSQL + ",";
                    } else {
                
                    }
            }
        
        }
        
        //Adiciona FROM TableName
        
        vSQL = vSQL + " From " + cETL_DBSOURCETABLENAME;
        
        //Adiciona WHERE Clause (Condiciones del Periodo
        
        vSQL = vSQL + " Where " + cETL_FIELDKEYEXTRACT;
        
        //Adiciona StartROW del Periodo
        
        vFecInicio = "'" + vIntervalID.substring(0, 4)+"-"+ vIntervalID.substring(4, 6) + "-" + vIntervalID.substring(6, 8)+ " "+ vIntervalID.substring(8, 10)+":"+vIntervalID.substring(10, 12) + "'";
        
        vSQL = vSQL + " >= " + vFecInicio;
        
        //Adiciona StopROW del Periodo
        
        vFecTermino = "'" + vIntervalID.substring(15, 19)+"-"+ vIntervalID.substring(19, 21) + "-" + vIntervalID.substring(21, 23)+ " "+ vIntervalID.substring(23, 25)+":"+vIntervalID.substring(25, 27) + "'";
        
        vSQL = vSQL + " And " + cETL_FIELDKEYEXTRACT;
        vSQL = vSQL + " < " + vFecTermino;
        
        gDatos.writeLog(0, cCLASS_NAME, cMETHOD_NAME, "Query Origen: "+vSQL);
        
        glb_rsSQL = vSqlConnDB.consultar(vSQL);
        
        int rowCount = 0;
        glb_isExistRowsSQLExtract = false;
        
        if (glb_rsSQL!=null) {
            if (glb_rsSQL.next()) {
                rowCount ++;
                glb_isExistRowsSQLExtract = true;
            }
        }
        isSqlQueryDBSource = true;
        return(isSqlQueryDBSource);
    }
    
    public boolean genETLExtraccion(String vProc, String vIntervalID) throws SQLException {
        String cMETHOD_NAME = "genETLExtraccion";
        boolean isGenETLExtraccion = false;
        
        //Genera Query Extraccion Base Source dependiendo del tipo Motor DB
        
        switch (cETL_DBSOURCETYPE) {
            case "SQL":
                //Conecta BD Origen
                //VAR GLOBAL OUTPUT:
                //      vSqlConnDB: sqlDB Connection hacia BD Origen
                if (sqlConnectDBSource()) {
                    //Genera Query de Extracción
                    //VAR GLOBAL OUPUT:
                    //      glb_rsSQL                 : Almacena RecordSet de Data Extraida
                    //      glb_isExistRowsSQLExtract : True si existen registros extraidos
                    if (sqlQueryDBSource(vIntervalID)) {
                        isGenETLExtraccion = true;
                        if (glb_isExistRowsSQLExtract) {
                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Hay datos origen");
                        } else {
                            gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "No hay datos origen");
                        }
                    
                    } else {
                        gDatos.writeLog(2, cCLASS_NAME, cMETHOD_NAME, "No pudo ejecutar Query de Extraccion");
                    }
                } else {
                    gDatos.writeLog(1, cCLASS_NAME, cMETHOD_NAME, "Imposible conectarse a DB SQL: " + cETL_DBSOURCENAME);
                }
                
                break;
            default:
                break;
        }
        
        return(isGenETLExtraccion);
    }
    
}
