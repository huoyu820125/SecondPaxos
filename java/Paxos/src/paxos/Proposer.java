/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package paxos;

import java.util.Date;

/**	
 *
 * @author Administrator
 */
public class Proposer {

    public Proposer() {
        setPlayerCount((short) 0, (short) 0);
        mValue = new ProposeData();
    }

    public Proposer(short proposerCount, short acceptorCount) {
        setPlayerCount(proposerCount, acceptorCount);
        mValue = new ProposeData();
    }

    //设置参与者数量
    public void setPlayerCount(short proposerCount, short acceptorCount) {
        mProposerCount = proposerCount;///proposer数量
        mAcceptorCount = acceptorCount;//acceptor数量
    }

    //开始Propose阶段
    public void startPropose(ProposeData value) {
        mValue.setSerialNum(value.serialNum());
        mValue.setValue(value.value());
        mProposeFinished = false;
        mIsAgree = false;
        mMaxAcceptedSerialNum = 0;
        mOkCount = 0;
        mRefuseCount = 0;
		Date curTime = new Date();
        mStart = curTime.getTime();//这就是距离1970年1月1日0点0分0秒的毫秒数
    }

    /*
     * 阶段超时
     * millSecond：超时判定时间
     */
    public boolean isTimeOut( int millSecond )
    {
		Date curTime = new Date();
        int waitTime = (int)(curTime.getTime() - mStart);//这就是距离1970年1月1日0点0分0秒的毫秒数
        if ( waitTime > millSecond ) return true;
        
        return false;
    }
    
    //取得提议
    public ProposeData getProposal() {
        return mValue;
    }

    //提议被投票，Proposed失败则重新开始Propose阶段
    public boolean proposed(boolean ok, ProposeData lastAcceptValue) {
        if (mProposeFinished) {
            return true;//可能是一阶段迟到的回应，直接忽略消息
        }
        if (!ok) {
            mRefuseCount++;
            //已有半数拒绝，不需要等待其它acceptor投票了，重新开始Propose阶段
            if (mRefuseCount > mAcceptorCount / 2) {
                mValue.setSerialNum(mValue.serialNum() + mProposerCount);
                startPropose(mValue);
                return false;
            }
            return true;
        }

        mOkCount++;
        /*
		        没有必要检查分支：serialNum为null
		        因为serialNum>m_maxAcceptedSerialNum，与serialNum非0互为必要条件
         */
        //记录所有收到的提议中，编号最大的提议，当自己获得提议权时，提出
        if (lastAcceptValue.serialNum() > mMaxAcceptedSerialNum) {
            mMaxAcceptedSerialNum = lastAcceptValue.serialNum();
            if (mValue.value() != lastAcceptValue.value()) {
                mValue.setValue(lastAcceptValue.value());
            }
        }
        if (mOkCount > mAcceptorCount / 2) {
            mOkCount = 0;
            mProposeFinished = true;
        }
        return true;
    }

    //开始Accept阶段,满足条件成功开始accept阶段返回ture，不满足开始条件返回false
    public boolean startAccept() {
        return mProposeFinished;
    }

    //提议被接受，Accepted失败则重新开始Propose阶段
    public boolean accepted(boolean ok) {
        if (!mProposeFinished) {
            return true;//可能是上次第二阶段迟到的回应，直接忽略消息
        }
        if (!ok) {
            mRefuseCount++;
            //已有半数拒绝，不需要等待其它acceptor投票了，重新开始Propose阶段
            if (mRefuseCount > mAcceptorCount / 2) {
                mValue.setSerialNum(mValue.serialNum() + mProposerCount);
                startPropose(mValue);
                return false;
            }
        }

        mOkCount++;
        if (mOkCount > mAcceptorCount / 2) {
            mIsAgree = true;
        }

        return true;
    }

    //提议被批准
    public boolean isAgree() {
        return mIsAgree;
    }

    private short mProposerCount;///proposer数量
    private short mAcceptorCount;//acceptor数量
    private ProposeData mValue;//提议内容
    private boolean mProposeFinished;//完成拉票，准备开始二阶段
    private boolean mIsAgree;//m_value被批准
    private int mMaxAcceptedSerialNum;//已被接受的提议中流水号最大的
    private long mStart;//阶段开始时间，阶段一，阶段二共用
    private short mOkCount;//投票数量，阶段一，阶段二共用
    private short mRefuseCount;//拒绝数量，阶段一，阶段二共用
}
