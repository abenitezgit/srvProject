/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 *
 * @author ABT
 */
public class oracleDB {
    /*
    Posteriormente dentro de la clase declararemos una variable privada 
    de tipo Connection que será la que contendrá la conexión con la base de datos
    */
    private Connection conexion;
    
    static int FilasAfectadas;
    static int FilasConsultadas;
    
    String v_dbName;
    String v_hostIP;
    String v_dbPort;
    String v_dbUser;
    String v_dbPass;
    static boolean connStatus;
    static String connErrMesg;
    static int vLoginTimeout=2;
    static String vSQLError;

    public oracleDB() {
    
    }
    
    public oracleDB(String vHost, String dbName, String dbPort, String dbUser, String dbPass) {
        this.v_hostIP = vHost;
        this.v_dbName = dbName;
        this.v_dbPort = dbPort;
        this.v_dbUser = dbUser;
        this.v_dbPass = dbPass;
    }
    
    
    /*
    Para organizar el código crearemos el get y set de conexion además del método 
    llamado conectar() el cual se encargará de establecer a la conexión con la base 
    de datos para que posteriormente podamos realizar los procedimientos que necesitamos. 
    Dentro del método encerraremos la mayor parte del código dentro de un try-catch 
    con el fin de capturar las excepciones que se puedan originar tras la ejecución del mismo
    */
    public String getvSQLError() {
        return vSQLError;
    }
        
    public void setLoginTimeout (int loginTimeout) {
        vLoginTimeout = loginTimeout;
    }
    
    public void setDbName (String dbName) {
        v_dbName = dbName;
    }
    
    public void setDbUser (String dbUser) {
        v_dbUser = dbUser;
    }
    
    public void setDbPass (String dbPass) {
        v_dbPass = dbPass;
    }
    
    public void setHostIp (String HostIp) {
        v_hostIP = HostIp;
    }
    
    public void setDbPort (String dbPort) {
        v_dbPort = dbPort;
    }
    
    public int getFilasConsultadas() {
        return FilasConsultadas;
    }
    
    public int getFilasAfectadas() {
        return FilasAfectadas;
    }
    
    public boolean getConnStatus() {
        return connStatus;
    }
    
    public String getConnErrMesg() {
        return connErrMesg;
    }
    
    public Connection getConexion() {
    return conexion;
}    
    
    public void closeConexion() throws SQLException {
        conexion.close();
        conexion = null;
        //return conexion;
    }
    
    public void setConexion(Connection conexion) {
    this.conexion = conexion;
}    
    
    /*
    Como puedes ver, el método devuelve una instancia de la clase OracleBD 
    (es decir, la misma que lo contiene), esto con el fin de poder encadenar 
    los siguientes métodos que necesitaremos y ahorrarnos una línea de código
    
    Ahora bien, dentro del try-catch lo primero que realizaremos será cargar 
    el driver en la memoria y posteriormente crear una cadena con la URL de 
    conexión a la base de datos como se muestra en el siguiente código
    */
    
    public oracleDB conectar()  {
        
        //Seteos por defecto
        connErrMesg = null;
        connStatus = false;
        
        try {
                Class.forName("oracle.jdbc.OracleDriver");
        } catch (Exception e) {
            connErrMesg = e.getMessage();
            connStatus = false;
            return this;
        }
        
         String BaseDeDatos = "jdbc:oracle:thin:@"+v_hostIP+":"+v_dbPort+":"+v_dbName;
        
        /*
        En este momento lo único que nos falta para crear la conexión es llamar 
        al método getConnection() del driver de la siguiente manera
        */
        
        try {
            
            DriverManager.setLoginTimeout(vLoginTimeout);
            conexion = DriverManager.getConnection(BaseDeDatos, v_dbUser, v_dbPass);
            /*Puedes validar si la conexión se realizó correctamente verificando si la variable es nula
            */
            if (conexion != null) {
                connErrMesg = null;
                connStatus = true;
            } else {
                connErrMesg = "Error de Conexion";
                connStatus = false;    
            }
        } catch (Exception e) {
            connErrMesg = e.getMessage();
            connStatus = false;    
        }
    
    return this;
} //fin clase conectar()
    
    /*
    Ejecutar sentencias en la base de datos
    */
    
    /*
    Vamos a definir el método para que sea público y nos devuelva un valor booleano, 
    true si se ejecuta la consulta correctamente, false si no. A su vez recibirá una 
    cadena que contendrá la consulta SQL a ejecutar
    */
        
    public boolean execute(String sql) {
       try {
            Statement sentencia;
            sentencia = getConexion().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            FilasAfectadas = sentencia.executeUpdate(sql);
            // En NumFilas se retorna el numero de filas afectadas
            getConexion().commit();
            sentencia.close();
        } catch (SQLException e) {
            connErrMesg = "Error en Ejecucion de Sentencia";
            vSQLError = e.getMessage();
            return false;
        }        
        return true;
    }
    
    public boolean executeSP(String sql) {
       try {
            Statement sentencia;
            sentencia = getConexion().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (sentencia.execute(sql)) {
                return true;
            }
            // En NumFilas se retorna el numero de filas afectadas
            getConexion().commit();
            sentencia.close();
        } catch (SQLException e) {
            connErrMesg = "Error en Ejecucion de Sentencia";
            vSQLError = e.getMessage();
            return false;
        }        
        return true;
    }
    
    /*
    Realizar consultas a la base de datos
    */
    
    /*
    Lo único que nos falta es consultar a la base de datos (SELECT) lo cual haremos 
    igualmente mediante un método público que nos devuelva un objeto de tipo ResultSet 
    e igualmente recibirá una cadena que contendrá una consulta SQL a realizar.
    
    Lo primero que vamos a agregar es una declaración del objeto de tipo ResultSet y 
    posteriormente encerraremos la ejecución dentro de un try-catch para capturar las 
    excepciones que se puedan generar.
    
    De igual manera nos apoyaremos de un objeto de tipo Statement para poder realizar la consulta.
    
    Obtenemos el mapa de resultados a través del método executeQuery() del objeto Statement 
    y posteriormente llamamos al método commit() de la conexión para asegurarnos que la consulta 
    se está realizando.
    
    En el caso de que se presente una excepción daremos por hecho que no se realizó la consulta 
    correctamente, por lo tanto, sólo en ese caso retornaremos null.
    
    */
    
    public ResultSet consultar(String sql) {
        ResultSet resultado;
        try {
            Statement sentencia;
            sentencia = getConexion().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            resultado = sentencia.executeQuery(sql);
            FilasConsultadas = resultado.getRow();
            getConexion().commit();
            //sentencia.close();
        } catch (SQLException e) {
            connErrMesg = e.getMessage();
            vSQLError = e.getMessage();
            return null;
        }
        return resultado;
    }


}
