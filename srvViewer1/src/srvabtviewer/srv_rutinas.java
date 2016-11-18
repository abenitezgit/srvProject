/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvabtviewer;

import abtGlobals.*;
import srvabtviewer.*;
import abtGlobals.globalVARArea;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mypkg.hbaseDB;

/**
 *
 * @author andresbenitez
 */
public class srv_rutinas {
    static globalVARArea gDatos;
    static String cCLASS_NAME = "srv_rutinas";

    
    srv_rutinas(globalVARArea m) {
        gDatos = m;
    
    }
    
        
    public int getUsedThreadProc(String vTypeProc) {
        int usedThread = 0;
        String [] vlist;

        int items = gDatos.getgThUsedxProc().size();
        for (int i=0; i<items; i++) {
            vlist = gDatos.getgThUsedxProc().get(i).split(":");
            if (vlist[0].equals(vTypeProc)) {
                usedThread = Integer.parseInt(vlist[1]);
            }
        }

        return(usedThread);
    }
        
    public boolean increaseThreadProc(String vTypeProc) {
        boolean isIncrease = false;

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
                vlist.add(vTypeProc+":"+String.valueOf(usedThread+1));
                isIncrease = true;
            }
        }

        if (!isIncrease) {
            vlist.add(vTypeProc+":1");
            isIncrease = true;
        }

        gDatos.setgThUsedxProc(vlist);

        return(isIncrease);
    }    
    
    public boolean isServerAuth() {
        try {
            boolean serverAuth = false;

            int numHost = gDatos.getCfg_srv_host_auth().length;

            for (int i=0; i<numHost; i++) {
                if (gDatos.getCfg_srv_host_auth()[i].equals(gDatos.getCfg_glb_hostName())) {
                    serverAuth = true;
                    gDatos.writeLog(0, cCLASS_NAME, "Servidor local Autorizado");
                }
            }
            return(serverAuth);
        } catch (Exception e) {
            gDatos.writeLog(2, cCLASS_NAME, "Error Validando Server Auth");
            return(false);
        }

    }    
    
    public void updateSrvStatus(hbaseDB conn, String TypeProc, String status) throws IOException {
        try {

            //Extrae Fecha de Hoy

            Date today;
            String output;
            SimpleDateFormat formatter;

            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            today = new Date();
            output = formatter.format(today);        

            //Setea Data a Actualizar
            //
            List<String> qualifier = new ArrayList<>();

            qualifier.add("stat,srvID,"+gDatos.getCfg_srv_ID());
            qualifier.add("stat,fecStart,"+gDatos.getCfg_srv_FecInicio());
            qualifier.add("stat,typeProc,"+TypeProc);
            qualifier.add("stat,ufecExec,"+output);
            qualifier.add("stat,uStatus,"+status);
            qualifier.add("stat,threadNum,"+gDatos.getCfg_srv_Max_thProcess());
                    String appName;
            switch (TypeProc) {
                case "MCO":
                    appName = "MainProc";
                    break;                
                case "ETL":
                    appName = "thExecETL";
                    break;
                case "KAL":
                    appName = "MainProc";
                    break;
                case "EXE":
                    appName = "MainProc";
                    break;
                case "MOV":
                    appName = "thExecMov";
                    break;
                default:
                    appName = "Not found";
                    break;
            }
            qualifier.add("stat,appName,"+appName);

            String rowKey = gDatos.getCfg_srv_ID() + "|" + TypeProc;

            conn.putDataRows("srvStatus", rowKey, qualifier);
            
        } catch (Exception e) {
            gDatos.writeLog(2, cCLASS_NAME, e.getMessage());
        }
    }
}
