package org.tio.utils.cache.redis;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.lock.SetWithLock;

/**
 * @author tanyaowu 
 * 2017年8月14日 下午1:34:06
 */
public class RedisTask {
	private static Logger log = LoggerFactory.getLogger(RedisTask.class);

	//	private static RedisTask instance = null;

	private static boolean started = false;

	public static void start() {
		//		instance = new RedisTask();
		if (started) {
			return;
		}
		synchronized (RedisTask.class) {
			if (started) {
				return;
			}
			started = true;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					WriteLock writeLock = setWithLock.getLock().writeLock();
					writeLock.lock();
					try {
						Set<ExpireVo> set = setWithLock.getObj();
						for (ExpireVo expireVo : set) {
							log.info("更新缓存过期时间, cacheName:{}, key:{}, expire:{}", expireVo.getCacheName(), expireVo.getKey(), expireVo.getExpire());

							RedisCache.getCache(expireVo.getCacheName()).getBucket(expireVo.getKey()).expire(expireVo.getExpire(), TimeUnit.SECONDS);
							//							expireVo.getExpirable().expire(expireVo.getExpire(), TimeUnit.SECONDS);
						}
						set.clear();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					} finally {
						writeLock.unlock();
						try {
							Thread.sleep(1000 * 10);
						} catch (InterruptedException e) {
							log.error(e.toString(), e);
						}
					}
				}

			}
		}, RedisTask.class.getName()).start();
	}

	/**
	 * 
	 * @author: tanyaowu
	 */
	private RedisTask() {
		//		this.redisson = redisson;
	}

	//	private RedissonClient redisson;

	private static Set<ExpireVo> set = new HashSet<>();

	private static SetWithLock<ExpireVo> setWithLock = new SetWithLock<>(set);

	public static void add(String cacheName, String key, long expire) {
		ExpireVo expireVo = new ExpireVo(cacheName, key, expire);
		setWithLock.add(expireVo);
	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}
}