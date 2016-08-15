/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package paxos;

/**
 *
 * @author Administrator
 */
public class ProposeData {
    public void setSerialNum(int serialNum)
    {
        mSerialNum = serialNum;
    }
    public int serialNum()
    {
        return mSerialNum;
    }
    public void setValue(int value)
    {
        mValue = value;
    }
    public int value()
    {
        return mValue;
    }
    private int	mSerialNum;//流水号,1开始递增，保证全局唯一
    private int	mValue;//提议内容
}
