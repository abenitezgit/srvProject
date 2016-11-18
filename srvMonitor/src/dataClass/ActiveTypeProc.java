/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataClass;

/**
 *
 * @author andresbenitez
 */
public class ActiveTypeProc {
    String typeProc;
    int usedThread;
    
    public ActiveTypeProc() {}
    
    public ActiveTypeProc(String typeProc, int usedThread) {
        this.typeProc = typeProc;
        this.usedThread = usedThread;
    }

    public String getTypeProc() {
        return typeProc;
    }

    public void setTypeProc(String typeProc) {
        this.typeProc = typeProc;
    }

    public int getUsedThread() {
        return usedThread;
    }

    public void setUsedThread(int usedThread) {
        this.usedThread = usedThread;
    }
}
