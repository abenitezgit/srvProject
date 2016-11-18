/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srvclientmonitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;

/**
 *
 * @author andresbenitez
 */
public class thTime extends Thread{
    Timer t1 = new Timer();
    JLabel lblTime = new JLabel();
    
    public thTime(JLabel lblTime) {
        this.lblTime = lblTime;
    }
    
    @Override
    public void run() {
        t1.schedule(new mainTimerTask(lblTime), 1000,1000);
    }
    
    class mainTimerTask extends TimerTask {
        JLabel lblTime = new JLabel();
    
        public mainTimerTask(JLabel lblTime) {
            this.lblTime = lblTime;
        }
        
        @Override
        public void run() {
            Date today;
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            today = new Date();
            lblTime.setText(formatter.format(today));  
        }
    
    }
}
