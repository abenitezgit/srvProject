package srvserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import utilities.mysqlDB;
import utilities.sqlDB;

public class test {

	public static void main(String[] args) throws SQLException {
		mysqlDB sconn = new mysqlDB("172.17.232.84","navegacion","3306","userNav","u341700");
		sconn.conectar();
		
		System.out.println(sconn.getConnStatus());
		
		sqlDB dconn = new sqlDB("172.17.232.99","VTR_ODS1","1433","sqlAdmin","SQLadmin1");
		dconn.conectar();
		
		System.out.println(dconn.getConnStatus());
		
		StringBuilder vsql = new StringBuilder();
		
		vsql.append("select ");
		vsql.append("cdrd_uniqueid,");
		vsql.append("cdrd_connid,");
		vsql.append("cdrd_secuencia,");
		vsql.append("cdrd_starttime,");
		vsql.append("cdrd_appname,");
		vsql.append("cdrd_opcion_ivr,");
		vsql.append("cdrd_rc,");
		vsql.append("cdrd_duration_ms,");
		vsql.append("cdrd_data,");
		vsql.append("cdrd_msg");
		vsql.append(" from cdr_detail_v2 ");
		vsql.append(" where cdrd_startTime >= UNIX_TIMESTAMP('20161218000000') ");
		vsql.append(" and cdrd_startTime <    UNIX_TIMESTAMP('20161219000000') ");
		
		ResultSet rs = sconn.consultar(vsql.toString());
		
		
		int rowsRead=0;
		int rowsLoad=0;
		int rowShow=2000;
		
		while (rs.next()) {
			rowsRead++;
			//System.out.println(rs.getString(1));
			
			StringBuilder cols = new StringBuilder();
			cols.append("cdrd_uniqueid,");
			cols.append("cdrd_connid,");
			cols.append("cdrd_secuencia,");
			cols.append("cdrd_starttime,");
			cols.append("cdrd_appname,");
			cols.append("cdrd_opcion_ivr,");
			cols.append("cdrd_rc,");
			cols.append("cdrd_duration_ms,");
			cols.append("cdrd_data,");
			cols.append("cdrd_msg");
			
			
			StringBuilder Variables = new StringBuilder();
			Variables.append("?,");
			Variables.append("?,");
			Variables.append("?,");
			Variables.append("?,");
			Variables.append("?,");
			Variables.append("?,");
			Variables.append("?,");
			Variables.append("?,");
			Variables.append("?,");
			Variables.append("?");
			
			PreparedStatement psInsertar = null;

			psInsertar =  dconn.getConexion().prepareStatement(	"insert into cdr_detail_v2 (" + cols + ") VALUES (" + Variables	+ ")");
			
			//psInsertar.setString(1, (char)34 + rs.getString(1)+(char)34);
			psInsertar.setString(1, rs.getString(1));
			psInsertar.setString(2, rs.getString(2));
			psInsertar.setInt(3, rs.getInt(3));
			psInsertar.setInt(4, rs.getInt(4));
			psInsertar.setString(5, rs.getString(5));
			psInsertar.setString(6, rs.getString(6));
			psInsertar.setInt(7, rs.getInt(7));
			psInsertar.setInt(8, rs.getInt(8));
			psInsertar.setString(9, rs.getString(9));
			psInsertar.setString(10, rs.getString(10));
			
			
			if (psInsertar.executeUpdate()==1)  {
				rowsLoad++;
			}
			
			
			if (rowsRead==rowShow) {
				System.out.println("Filas Leidas: "+rowsRead);
				System.out.println("Filas Cargadas: "+rowsLoad);
				rowShow=rowShow+2000;
			}
			
		}
		
		System.out.println("Total Filas Leidas: "+rowsRead);
		System.out.println("Total Filas Cargadas: "+rowsLoad);

	}

}
