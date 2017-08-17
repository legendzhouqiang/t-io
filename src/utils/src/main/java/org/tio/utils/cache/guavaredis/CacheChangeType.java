package org.tio.utils.cache.guavaredis;

import java.util.Objects;

/**
 * 
 * @author tanyaowu 
 * 2017年8月12日 下午9:33:02
 */
public enum CacheChangeType {
	REMOVE(1), //key级别清空本地缓存
	UPDATE(2), //key级别清空本地缓存
	PUT(3), //key级别清空本地缓存
	CLEAR(4); //cacheName级别清空本地缓存

	Integer value;

	private CacheChangeType(Integer value) {
		this.value = value;
	}

	public static CacheChangeType from(Integer method) {
		CacheChangeType[] values = CacheChangeType.values();
		for (CacheChangeType v : values) {
			if (Objects.equals(v.value, method)) {
				return v;
			}
		}
		return null;
	}
}