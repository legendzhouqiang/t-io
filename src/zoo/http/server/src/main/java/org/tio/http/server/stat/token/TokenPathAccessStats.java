package org.tio.http.server.stat.token;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.GroupContext;
import org.tio.utils.cache.caffeine.CaffeineCache;

/**
 * 
 * @author tanyaowu
 * 2017年4月15日 下午12:13:19
 */
public class TokenPathAccessStats {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(TokenPathAccessStats.class);

	private final static String CACHE_NAME = "TIO_TOKEN_ACCESSPATH";
	//	private final static Long timeToLiveSeconds = null;
	//	private final static Long timeToIdleSeconds = Time.DAY_1;

	private GroupContext groupContext;
	
	private String groupContextId;

	//	private CaffeineCache[] caches = null;
	/**
	 * key:   时长段，单位：秒
	 * value: CaffeineCache: key: token, value: TokenAccessStat
	 */
	public final Map<Long, CaffeineCache> cacheMap = new HashMap<>();

	/**
	 * 时长段列表
	 */
	public final List<Long> durationList = new ArrayList<>();
	
	private final Map<Long, TokenPathAccessStatListener> listenerMap = new HashMap<>();
	
	private TokenGetter tokenGetter;

	/**
	 * 
	 * @param tokenGetter
	 * @param groupContext
	 * @param tokenPathAccessStatListener
	 * @param durations
	 */
	public TokenPathAccessStats(TokenGetter tokenGetter, GroupContext groupContext, TokenPathAccessStatListener tokenPathAccessStatListener, Long[] durations) {
		if (tokenGetter == null) {
			throw new RuntimeException("tokenGetter can not be null");
		}
		
		this.tokenGetter = tokenGetter;
		this.groupContext = groupContext;
		this.groupContextId = groupContext.getId();
		if (durations != null) {
			for (Long duration : durations) {
				addDuration(duration, tokenPathAccessStatListener);
			}
		}
	}
	
	public TokenPathAccessStats(GroupContext groupContext, TokenPathAccessStatListener tokenPathAccessStatListener, Long[] durations) {
		this.tokenGetter = DefaultTokenGetter.me;
		this.groupContext = groupContext;
		this.groupContextId = groupContext.getId();
		if (durations != null) {
			for (Long duration : durations) {
				addDuration(duration, tokenPathAccessStatListener);
			}
		}
	}

	/**
	 * 添加监控时段
	 * @param duration 单位：秒
	 * @param tokenPathAccessStatListener 可以为null
	 * @author: tanyaowu
	 */
	public void addDuration(Long duration, TokenPathAccessStatListener tokenPathAccessStatListener) {
		@SuppressWarnings("unchecked")
		CaffeineCache caffeineCache = CaffeineCache.register(getCacheName(duration), duration, null, new TokenPathAccessStatRemovalListener(groupContext, tokenPathAccessStatListener));
		cacheMap.put(duration, caffeineCache);
		durationList.add(duration);
		
		if (tokenPathAccessStatListener != null) {
			listenerMap.put(duration, tokenPathAccessStatListener);
		}
	}
	
	/**
	 * 
	 * @param duration
	 * @return
	 * @author tanyaowu
	 */
	public TokenPathAccessStatListener getListener(Long duration) {
		return listenerMap.get(duration);
	}

	/**
	 * 添加监控时段
	 * @param durations 单位：秒
	 * @param tokenPathAccessStatListener 可以为null
	 * @author: tanyaowu
	 */
	public void addDurations(Long[] durations, TokenPathAccessStatListener tokenPathAccessStatListener) {
		if (durations != null) {
			for (Long duration : durations) {
				addDuration(duration, tokenPathAccessStatListener);
			}
		}
	}

	/**
	 * 删除监控时间段
	 * @param duration
	 * @author: tanyaowu
	 */
	public void removeMonitor(Long duration) {
		clear(duration);
		cacheMap.remove(duration);
		durationList.remove(duration);
	}

	/**
	 * 
	 * @param duration
	 * @return
	 * @author: tanyaowu
	 */
	public String getCacheName(Long duration) {
		String cacheName = CACHE_NAME + "_" + this.groupContextId + "_";
		return cacheName + duration;
	}

	/**
	 * 清空监控数据
	 * @author: tanyaowu
	 */
	public void clear(Long duration) {
		CaffeineCache caffeineCache = cacheMap.get(duration);
		if (caffeineCache == null) {
			return;
		}
		caffeineCache.clear();
	}

	
	
	/**
	 * 获取TokenAccessStat
	 * @param duration
	 * @param token
	 * @param forceCreate
	 * @return
	 * @author tanyaowu
	 */
	public TokenAccessStat get(Long duration, String token, boolean forceCreate) {
		if (StringUtils.isBlank(token)) {
			return null;
		}
		
		CaffeineCache caffeineCache = cacheMap.get(duration);
		if (caffeineCache == null) {
			return null;
		}

		TokenAccessStat tokenAccessStat = (TokenAccessStat) caffeineCache.get(token);
		if (tokenAccessStat == null && forceCreate) {
			synchronized (caffeineCache) {
				tokenAccessStat = (TokenAccessStat) caffeineCache.get(token);
				if (tokenAccessStat == null) {
					tokenAccessStat = new TokenAccessStat(duration, token);//new MapWithLock<String, TokenPathAccessStat>(new HashMap<>());//new TokenPathAccessStat(duration, token, path);
					caffeineCache.put(token, tokenAccessStat);
				}
			}
		}
		
		return tokenAccessStat;
	}
	
	/**
	 * 获取TokenAccessStat
	 * @param duration
	 * @param token
	 * @return
	 * @author tanyaowu
	 */
	public TokenAccessStat get(Long duration, String token) {
		return get(duration, token, true);
	}

	/**
	 * key:   token
	 * value: TokenPathAccessStat
	 * @param duration
	 * @return
	 * @author tanyaowu
	 */
	public ConcurrentMap<String, Serializable> map(Long duration) {
		CaffeineCache caffeineCache = cacheMap.get(duration);
		if (caffeineCache == null) {
			return null;
		}
		ConcurrentMap<String, Serializable> map = caffeineCache.asMap();
		return map;
	}

	/**
	 *
	 * @return
	 * @author: tanyaowu
	 */
	public Long size(Long duration) {
		CaffeineCache caffeineCache = cacheMap.get(duration);
		if (caffeineCache == null) {
			return null;
		}
		return caffeineCache.size();
	}

	/**
	 *
	 * @return
	 * @author: tanyaowu
	 */
	public Collection<Serializable> values(Long duration) {
		CaffeineCache caffeineCache = cacheMap.get(duration);
		if (caffeineCache == null) {
			return null;
		}
		Collection<Serializable> set = caffeineCache.asMap().values();
		return set;
	}

	public TokenGetter getTokenGetter() {
		return tokenGetter;
	}

//	public void setTokenGetter(TokenGetter tokenGetter) {
//		this.tokenGetter = tokenGetter;
//	}

}
