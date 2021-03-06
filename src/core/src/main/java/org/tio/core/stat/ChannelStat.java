package org.tio.core.stat;

import java.util.concurrent.atomic.AtomicLong;

import org.tio.utils.SystemTimer;

/**
 * @author tanyaowu
 * 2017年4月1日 下午2:17:35
 */
public class ChannelStat implements java.io.Serializable {
	private static final long serialVersionUID = -6942731710053482089L;

	/**
	 * 本次解码失败的次数
	 */
	private int decodeFailCount = 0;

	/**
	 * 最近一次收到业务消息包的时间(一个完整的业务消息包，一部分消息不算)
	 */
	private long latestTimeOfReceivedPacket = SystemTimer.currentTimeMillis();

	/**
	 * 最近一次发送业务消息包的时间(一个完整的业务消息包，一部分消息不算)
	 */
	private long latestTimeOfSentPacket = SystemTimer.currentTimeMillis();
	
	/**
	 * 最近一次收到业务消息包的时间:收到字节就算
	 */
	private long latestTimeOfReceivedByte = SystemTimer.currentTimeMillis();

	/**
	 * 最近一次发送业务消息包的时间：发送字节就算
	 */
	private long latestTimeOfSentByte = SystemTimer.currentTimeMillis();

	/**
	 * ChannelContext对象创建的时间
	 */
	private long timeCreated = SystemTimer.currentTimeMillis();

	/**
	 * 第一次连接成功的时间
	 */
	private Long timeFirstConnected = null;

	/**
	 * 连接关闭的时间
	 */
	private long timeClosed = SystemTimer.currentTimeMillis();

	/**
	 * 进入重连队列时间
	 */
	private long timeInReconnQueue = SystemTimer.currentTimeMillis();

	/**
	 * 本连接已发送的字节数
	 */
	private AtomicLong sentBytes = new AtomicLong();

	/**
	 * 本连接已发送的packet数
	 */
	private AtomicLong sentPackets = new AtomicLong();

	/**
	 * 本连接已处理的字节数
	 */
	private AtomicLong handledBytes = new AtomicLong();

	/**
	 * 本连接已处理的packet数
	 */
	private AtomicLong handledPackets = new AtomicLong();
	
	/**
	 * 处理消息包耗时，单位：毫秒
	 * 拿这个值除以handledPackets，就是处理每个消息包的平均耗时
	 */
	private AtomicLong handledPacketCosts = new AtomicLong();

	/**
	 * 本连接已接收的字节数
	 */
	private AtomicLong receivedBytes = new AtomicLong();
	
	/**
	 * 本连接已接收了多少次TCP数据包
	 */
	private AtomicLong receivedTcps = new AtomicLong();

	/**
	 * 本连接已接收的packet数
	 */
	private AtomicLong receivedPackets = new AtomicLong();
	
	/**
	 * 平均每次TCP接收到的字节数，这个可以用来监控慢攻击，配置PacketsPerTcpReceive定位慢攻击
	 */
	public double getBytesPerTcpReceive() {
		if (receivedTcps.get() == 0) {
			return 0;
		}
		double ret = (double)receivedBytes.get() / (double)receivedTcps.get();
		return ret;
	}
	
	/**
	 * 平均每次TCP接收到的业务包数，这个可以用来监控慢攻击，此值越小越有攻击嫌疑
	 */
	public double getPacketsPerTcpReceive() {
		if (receivedTcps.get() == 0) {
			return 0;
		}
		double ret = (double)receivedPackets.get() / (double)receivedTcps.get();
		return ret;
	}

	/**
	 * @return the decodeFailCount
	 */
	public int getDecodeFailCount() {
		return decodeFailCount;
	}

	/**
	 * @return the countHandledByte
	 */
	public AtomicLong getHandledBytes() {
		return handledBytes;
	}

	/**
	 * @return the countHandledPacket
	 */
	public AtomicLong getHandledPackets() {
		return handledPackets;
	}

	/**
	 * @return the timeLatestReceivedMsg
	 */
	public long getLatestTimeOfReceivedPacket() {
		return latestTimeOfReceivedPacket;
	}

	/**
	 * @return the timeLatestSentMsg
	 */
	public long getLatestTimeOfSentPacket() {
		return latestTimeOfSentPacket;
	}

	/**
	 * @return the countReceivedByte
	 */
	public AtomicLong getReceivedBytes() {
		return receivedBytes;
	}

	/**
	 * @return the countReceivedPacket
	 */
	public AtomicLong getReceivedPackets() {
		return receivedPackets;
	}

	/**
	 * @return the countSentByte
	 */
	public AtomicLong getSentBytes() {
		return sentBytes;
	}

	/**
	 * @return the countSentPacket
	 */
	public AtomicLong getSentPackets() {
		return sentPackets;
	}

	/**
	 * @return the timeClosed
	 */
	public long getTimeClosed() {
		return timeClosed;
	}

	/**
	 * @return the timeCreated
	 */
	public long getTimeCreated() {
		return timeCreated;
	}

	/**
	 * @return the timeFirstConnected
	 */
	public Long getTimeFirstConnected() {
		return timeFirstConnected;
	}

	/**
	 * @return the timeInReconnQueue
	 */
	public long getTimeInReconnQueue() {
		return timeInReconnQueue;
	}

	/**
	 * @param decodeFailCount the decodeFailCount to set
	 */
	public void setDecodeFailCount(int decodeFailCount) {
		this.decodeFailCount = decodeFailCount;
	}

	/**
	 * @param countHandledByte the countHandledByte to set
	 */
	public void setHandledBytes(AtomicLong countHandledByte) {
		this.handledBytes = countHandledByte;
	}

	/**
	 * @param timeLatestReceivedMsg the timeLatestReceivedMsg to set
	 */
	public void setLatestTimeOfReceivedPacket(long latestTimeOfReceivedPacket) {
		this.latestTimeOfReceivedPacket = latestTimeOfReceivedPacket;
	}

	/**
	 * @param timeLatestSentMsg the timeLatestSentMsg to set
	 */
	public void setLatestTimeOfSentPacket(long latestTimeOfSentPacket) {
		this.latestTimeOfSentPacket = latestTimeOfSentPacket;
	}

	/**
	 * @param countReceivedByte the countReceivedByte to set
	 */
	public void setReceivedBytes(AtomicLong receivedBytes) {
		this.receivedBytes = receivedBytes;
	}

	/**
	 * @param countReceivedPacket the countReceivedPacket to set
	 */
	public void setReceivedPackets(AtomicLong receivedPackets) {
		this.receivedPackets = receivedPackets;
	}

	/**
	 * @param countSentByte the countSentByte to set
	 */
	public void setSentBytes(AtomicLong sentBytes) {
		this.sentBytes = sentBytes;
	}

	/**
	 * @param countSentPacket the countSentPacket to set
	 */
	public void setSentPackets(AtomicLong sentPackets) {
		this.sentPackets = sentPackets;
	}

	/**
	 * @param timeClosed the timeClosed to set
	 */
	public void setTimeClosed(long timeClosed) {
		this.timeClosed = timeClosed;
	}

	/**
	 * @param timeFirstConnected the timeFirstConnected to set
	 */
	public void setTimeFirstConnected(Long timeFirstConnected) {
		this.timeFirstConnected = timeFirstConnected;
	}

	/**
	 * @param timeInReconnQueue the timeInReconnQueue to set
	 */
	public void setTimeInReconnQueue(long timeInReconnQueue) {
		this.timeInReconnQueue = timeInReconnQueue;
	}

	/**
	 * @return the latestTimeOfReceivedByte
	 */
	public long getLatestTimeOfReceivedByte() {
		return latestTimeOfReceivedByte;
	}

	/**
	 * @param latestTimeOfReceivedByte the latestTimeOfReceivedByte to set
	 */
	public void setLatestTimeOfReceivedByte(long latestTimeOfReceivedByte) {
		this.latestTimeOfReceivedByte = latestTimeOfReceivedByte;
	}

	/**
	 * @return the latestTimeOfSentByte
	 */
	public long getLatestTimeOfSentByte() {
		return latestTimeOfSentByte;
	}

	/**
	 * @param latestTimeOfSentByte the latestTimeOfSentByte to set
	 */
	public void setLatestTimeOfSentByte(long latestTimeOfSentByte) {
		this.latestTimeOfSentByte = latestTimeOfSentByte;
	}

	/**
	 * @return the receivedTcps
	 */
	public AtomicLong getReceivedTcps() {
		return receivedTcps;
	}

	/**
	 * @param receivedTcps the receivedTcps to set
	 */
	public void setReceivedTcps(AtomicLong receivedTcps) {
		this.receivedTcps = receivedTcps;
	}

	public AtomicLong getHandledPacketCosts() {
		return handledPacketCosts;
	}
	
	/**
	 * 处理packet平均耗时，单位：毫秒
	 * @return
	 */
	public double getHandledCostsPerPacket() {
		if (handledPackets.get() > 0) {
			return handledPacketCosts.get() / handledPackets.get();
		}
		return 0;
	}
}
