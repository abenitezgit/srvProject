package srvmonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataClass.ETL;
import dataClass.Ftp;
import dataClass.Grupo;
import utilities.globalAreaData;
import utilities.srvRutinas;
import dataClass.Process;

public class test {

	public static void main(String[] args) {
		globalAreaData gDatos = new globalAreaData();
		srvRutinas lib = new srvRutinas(gDatos);
		
		
		
		
		
		try {
			List<Process> lstProc = new ArrayList<>();
			List<Grupo> lstGroup = new ArrayList<>();
			
			dataClass.Process proc = new dataClass.Process();
			dataClass.Ftp ftp = new Ftp();
			dataClass.ETL etl = new ETL();
			Grupo grp = new Grupo();
			
			etl.setETLID("ETL00001");
			etl.setETLDesc("Desc");
			
			ftp.setID("FTP00001");
			ftp.setDesc("Desc");
			
			proc.setProcID("FTP0001");
			proc.setnOrder(1);
			proc.setCritical(1);
			proc.setParams(ftp);

			lstProc.add(proc);
			
			proc = new dataClass.Process();
			
			proc.setProcID("ETL0001");
			proc.setnOrder(1);
			proc.setCritical(1);
			proc.setParams(etl);
			
			lstProc.add(proc);
			
			grp.setGrpID("GRP00001");
			grp.setLstProcess(lstProc);
			
			lstGroup.add(grp);
			
			String xx = lib.serializeObjectToJSon(lstGroup, false);
			
			System.out.println(xx);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
