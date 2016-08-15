/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package demo;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import paxos.Acceptor;
import paxos.ProposeData;
import paxos.Proposer;
import tool.LogFormatter;
/**
 *
 * @author Administrator
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        msLogger = Logger.getLogger(Main.class.getName());
        try {
            String fs = String.format("./Main.log");
            FileHandler fileHandler = new FileHandler(fs);
            msLogger.addHandler(fileHandler);
            fileHandler.setFormatter(new LogFormatter());
        } catch (IOException ex) {
            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Proposer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String logText = "11个Proposer, 11个Acceptor准备进行Paxos\r\n"
                + "每个Proposer独立线程，Acceptor不需要线程\r\n"
		+ "Proposer线程中等待随机时间:表示与Acceptor的通信时间\r\n"
		+ "Proposer线程中调用Acceptor.Proposed()表示拿到了Propose请求结果\r\n"
		+ "Proposer线程中调用Acceptor.Accepted()表示拿到了Accept请求结果\r\n"
		+ "Proposer被批准后结束线程,其它线程继续投票最终，全部批准相同的值，达成一致。\r\n\r\n";
        msLogger.info( logText );
        msLogger.info( "Paxos开始" );


        Proposer []p = new Proposer[11];
        Acceptor []a = new Acceptor[11];
        int i = 0;
        for ( i = 0; i < 11; i++ ) 
        {
            a[i] = new Acceptor();
        }
        
        TestPaxos.msLockAcceptors = new ReentrantLock[11];
        for ( i = 0; i < 11; i++ )
        {
        	TestPaxos.msLockAcceptors[i] = new ReentrantLock();
        }
        ProposeData value;
        Thread []t = new Thread[11];
        Date dt= new Date();
        TestPaxos.msStartPaxos = dt.getTime();
        TestPaxos []proposerRun = new TestPaxos[11];
        for ( i = 0; i < 11; i++ )
        {
            p[i] = new Proposer((short)i, (short)i);
            p[i].setPlayerCount((short)11, (short)11);
            value = new ProposeData();
            value.setSerialNum(i);
            value.setValue(i);
            p[i].startPropose(value);
            proposerRun[i] = new TestPaxos(i, p[i], a);
            t[i] = new Thread(proposerRun[i]);
            t[i].start();
        }
        while ( true ) try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static Logger msLogger;

}
