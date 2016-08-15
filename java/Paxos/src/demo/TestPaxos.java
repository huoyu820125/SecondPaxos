package demo;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import paxos.Acceptor;
import paxos.ProposeData;
import paxos.Proposer;
import tool.IOFile;

public class TestPaxos implements Runnable{
	public TestPaxos(int id, Proposer proposer, Acceptor[] acceptors) {
        mId = id;
        mAcceptors = acceptors;
        mProposer = proposer;
	}
	
    //测试线程，模拟与Acceptor通信
    public void run() {
        ProposeData lastValue = new ProposeData();
        int[] acceptorId = new int[11];
        int count = 0;
        String fs = String.format("./Proposer%d.log", mId);
        IOFile logger = new IOFile(fs);
        Date dt = new Date();
        long startTime = dt.getTime();//开始Paxos计时
        ProposeData value = null;
        while (true) {
            value = mProposer.getProposal();//拿到提议
            String logText = String.format("Proposer%d号开始(Propose阶段):提议=[编号:%d，提议:%d]\r\n", 
            		mId, value.serialNum(), value.value());
            logger.write(logText);
            count = 0;
            int i = 0;
            for (i = 0; i < 11; i++) {
            	/*
            	 * 发送消息到第i个acceptor
            	 * 经过一定时间达到acceptor，sleep(随机时间)模拟
            	 * acceptor处理消息，mAcceptors[i].Propose()
            	 * 回应proposer
            	 * 经过一定时间proposer收到回应，sleep(随机时间)模拟
            	 * proposer处理回应mProposer.proposed(ok, lastValue)
            	 * fix是demo辅助参数（正式使用不需要）表示这次回应有没有导致proposer修改提议
            	 */
                sleep(rand(2000));//经过随机时间，消息到达了mAcceptors[i]
                //处理消息
                TestPaxos.msLockAcceptors[mId].lock();
                boolean ok = mAcceptors[i].Propose(value.serialNum(), lastValue);
                TestPaxos.msLockAcceptors[mId].unlock();
                sleep(rand(2000));//经过随机时间,消息到达Proposer
                //处理Propose回应
                if (!mProposer.proposed(ok, lastValue)) //重新开始Propose阶段
                {
                    sleep(1000);//为了降低活锁，多等一会让别的proposer有机会完成自己的2阶段批准
                    break;
                }
                ProposeData newValue = mProposer.getProposal();//拿到提议
                if (newValue.value() != value.value()) {//acceptor本次回应可能推荐了一个提议
                    logText = String.format("Proposer%d号修改了提议:提议=[编号:%d，提议:%d]\r\n", 
                    		mId, newValue.serialNum(), newValue.value());
                    logger.write(logText);
                }
                acceptorId[count++] = i;//记录愿意投票的acceptor
                if (mProposer.startAccept()) {
                    if (0 == rand(100) % 2) 
                    {
                    	break;
                    }
                }
            }
        	//检查有没有达到Accept开始条件，如果没有表示要重新开始Propose阶段
            if (!mProposer.startAccept()) continue;

            //开始Accept阶段
        	//发送Accept消息到所有愿意投票的acceptor
            value = mProposer.getProposal();//拿到提议
            logText = String.format("Proposer%d号开始(Accept阶段):提议=[编号:%d，提议:%d]\r\n", mId, value.serialNum(), value.value());
            logger.write(logText);
            for (i = 0; i < count; i++) {
            	//发送accept消息到acceptor
                sleep(rand(2000));//经过随机时间,accept消息到达acceptor
                //处理accept消息
                TestPaxos.msLockAcceptors[mId].lock();
                boolean ok = mAcceptors[acceptorId[i]].Accept(value);
                TestPaxos.msLockAcceptors[mId].unlock();
                sleep(rand(2000));//经过随机时间,accept回应到达proposer
                //处理accept回应
                if (!mProposer.accepted(ok)) //重新开始Propose阶段
                {
                    sleep(1000);//为了降低活锁，多等一会让别的proposer有机会完成自己的2阶段批准
                    break;
                }
                if (mProposer.isAgree()) {//成功批准了提议
                    dt = new Date();
                    long end = dt.getTime();
                    startTime = end - startTime;
                    logText = String.format("Proposer%d号批准了提议,用时%dMS:最终提议 = [编号:%d，提议:%d]\r\n", mId, 
                    		(int)startTime, value.serialNum(), value.value());
                    logger.write(logText);
                    Main.msLogger.info(logText);

                    msLockFinished.lock();
                    msFinishedCount += 1;
                    if (11 == msFinishedCount) {
                        msStartPaxos = dt.getTime() - msStartPaxos;
                        logText = String.format("Paxos完成，用时%dMS\r\n", (int) msStartPaxos);
                        Main.msLogger.info(logText);
                    }
                    msLockFinished.unlock();
                    return;
                }
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestPaxos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int rand(int max){
        int r = (int) (Math.random() * max);
        return r;
    }

    private int mId;//自身结点id
    private Proposer mProposer;//线程操作的proposer结点
    private Acceptor[] mAcceptors;//所有acceptor结点，用户模拟与acceptor交互，直接调用acceptor方法表示通信完成
    public static Lock[] msLockAcceptors = null;
    public static long msStartPaxos;//开始时间
    public static int msFinishedCount;//完成的proposer结点数量
    public static Lock msLockFinished = new ReentrantLock();//msFinishedCount锁

}
