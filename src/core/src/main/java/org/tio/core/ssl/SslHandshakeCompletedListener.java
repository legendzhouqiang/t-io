/**
 * 
 */
package org.tio.core.ssl;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.core.ssl.facade.IHandshakeCompletedListener;
import org.tio.core.task.SendRunnable;

/**
 * @author tanyw
 *
 */
public class SslHandshakeCompletedListener implements IHandshakeCompletedListener {
	private static Logger log = LoggerFactory.getLogger(SslHandshakeCompletedListener.class);

	private ChannelContext channelContext;

	/**
	 * 
	 */
	public SslHandshakeCompletedListener(ChannelContext channelContext) {
		this.channelContext = channelContext;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	@Override
	public void onComplete() {
		log.info("{}, 完成SSL握手", channelContext);
		channelContext.getSslFacadeContext().setHandshakeCompleted(true);
		
		try {
			channelContext.getGroupContext().getAioListener().onAfterConnected(channelContext, true, channelContext.isReconnect());
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		
		
		SendRunnable sendRunnable = channelContext.getSendRunnable();
		ConcurrentLinkedQueue<Packet>  forSendAfterSslHandshakeCompleted = sendRunnable.getForSendAfterSslHandshakeCompleted(false);
		if (forSendAfterSslHandshakeCompleted == null || forSendAfterSslHandshakeCompleted.size() == 0) {
			return;
		}
		
		log.info("{} 业务层在SSL握手前就有{}条数据待发送", channelContext, forSendAfterSslHandshakeCompleted.size());
		while(true) {
			Packet packet = forSendAfterSslHandshakeCompleted.poll();
			if (packet != null) {
				sendRunnable.addMsg(packet);
			} else {
				break;
			}
		}
		
		channelContext.getGroupContext().getTioExecutor().execute(sendRunnable);
	}

}
