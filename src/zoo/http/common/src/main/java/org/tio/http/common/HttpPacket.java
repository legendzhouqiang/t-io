package org.tio.http.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.tio.core.intf.Packet;

/**
 *
 * @author tanyaowu
 *
 */
public class HttpPacket extends Packet {

	//	private static Logger log = LoggerFactory.getLogger(HttpPacket.class);

	private static final long serialVersionUID = 3903186670675671956L;

	//	public static final int MAX_LENGTH_OF_BODY = (int) (1024 * 1024 * 5.1); //只支持多少M数据

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
	private Map<String, Serializable> props = new ConcurrentHashMap<>();
	
	/**
	 * 获取请求属性
	 * @param key
	 * @return
	 * @author tanyaowu
	 */
	public Object getAttribute(String key) {
		return props.get(key);
	}
	
	public Object getAttribute(String key, Serializable defaultValue) {
		Serializable ret = props.get(key);
		if (ret == null) {
			return defaultValue;
		}
		return ret;
	}
	
	/**
	 *
	 * @param key
	 * @param httpConfig
	 * @author tanyaowu
	 */
	public void removeAttribute(String key) {
		props.remove(key);
	}

	/**
	 * 设置请求属性
	 * @param key
	 * @param value
	 * @param httpConfig
	 * @author tanyaowu
	 */
	public void setAttribute(String key, Serializable value) {
		props.put(key, value);
	}
	
	protected byte[] body;

	private String headerString;

	protected Map<String, String> headers = new HashMap<>();

	public HttpPacket() {

	}

	/**
	 * @return the body
	 */
	public byte[] getBody() {
		return body;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	/**
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getHeaderString() {
		return headerString;
	}

	public void removeHeader(String key, String value) {
		headers.remove(key);
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setHeaderString(String headerString) {
		this.headerString = headerString;
	}
}
