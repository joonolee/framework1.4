/**
 * @(#)Box.java
 */
package framework.action;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/** 
 * 요청객체, 쿠키객체의 값을 담는 해시테이블 객체이다.
 * 요청객체의 파라미터를 추상화 하여 Box 를 생성해 놓고 파라미터이름을 키로 해당 값을 원하는 데이타 타입으로 반환받는다.
 */
public class Box extends Hashtable {
	private static final long serialVersionUID = 7143941735208780214L;
	private String _name = null;

	/***
	 * Box 생성자
	 * @param name Box 객체의 이름
	 */
	public Box(String name) {
		super();
		this._name = name;
	}

	/** 
	 * 요청객체의 파라미터 이름과 값을 저장한 해시테이블을 생성한다.
	 * <br>
	 * ex) request Box 객체를 얻는 경우 => Box box = Box.getBox(request)
	 * 
	 * @param request HTTP 클라이언트 요청객체
	 * 
	 * @return 요청Box 객체
	 */
	public static Box getBox(HttpServletRequest request) {
		Box box = new Box("requestbox");
		Enumeration e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			box.put(key, request.getParameterValues(key));
		}
		return box;
	}

	/** 
	 * 요청객체의 쿠키 이름과 값을 저장한 해시테이블을 생성한다.
	 * <br>
	 * ex) cookie Box 객체를 얻는 경우 => Box box = Box.getBoxFromCookie(request)
	 * 
	 * @param request HTTP 클라이언트 요청객체
	 * 
	 * @return 쿠키Box 객체
	 */
	public static Box getBoxFromCookie(HttpServletRequest request) {
		Box cookiebox = new Box("cookiebox");
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return cookiebox;
		}
		for (int i = 0; cookies != null && i < cookies.length; i++) {
			String key = cookies[i].getName();
			String value = cookies[i].getValue();
			if (value == null) {
				value = "";
			}
			String[] values = new String[1];
			values[0] = value;
			cookiebox.put(key, values);
		}
		return cookiebox;
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 오브젝트를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 오브젝트
	 */
	public Object get(String key) {
		Object value = null;
		try {
			value = super.get(key);
			if (value == null) {
				return value;
			}
			Class c = value.getClass();
			if (c.isArray()) {
				int length = Array.getLength(value);
				if (length == 0) {
					value = null;
				} else {
					value = Array.get(value, 0);
				}
			}
		} catch (Exception e) {
			value = null;
		}
		return value;
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 문자열 배열을 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 문자열 배열
	 */
	public String[] getArray(String key) {
		return (String[]) super.get(key);
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 Boolean 객체를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 Boolean 객체
	 */
	public Boolean getBoolean(String key) {
		String value = getString(key);
		Boolean isTrue = new Boolean(false);
		try {
			isTrue = new Boolean(value);
		} catch (Exception e) {
		}
		return isTrue;
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 Double 객체를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 Double 객체
	 */
	public Double getDouble(String key) {
		String value = getString(key).trim().replaceAll(",", "");
		if (value.equals("")) {
			return new Double(0);
		}
		Double num = null;
		try {
			num = Double.valueOf(value);
		} catch (Exception e) {
			num = new Double(0);
		}
		return num;
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 BigDecimal 객체를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 BigDecimal 객체
	 */
	public BigDecimal getBigDecimal(String key) {
		String value = getString(key).trim().replaceAll(",", "");
		if (value.equals("")) {
			return new BigDecimal(0);
		}
		try {
			return new BigDecimal(value);
		} catch (Exception e) {
			return new BigDecimal(0);
		}
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 Float 객체를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 Float 객체
	 */
	public Float getFloat(String key) {
		return new Float(getDouble(key).doubleValue());
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 Integer 객체를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 Integer 객체
	 */
	public Integer getInteger(String key) {
		Double value = getDouble(key);
		return new Integer(value.intValue());
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 Long 객체를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 Long 객체
	 */
	public Long getLong(String key) {
		Double value = getDouble(key);
		return new Long(value.longValue());
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 long 변수를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 long 변수를
	 */
	public long getlong(String key) {
		Double value = getDouble(key);
		return value.longValue();
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 String 객체를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 String 객체
	 */
	public String getString(String key) {
		String str = (String) get(key);
		if (str == null) {
			return "";
		}
		return str;
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 바이트 배열을 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 바이트 배열
	 */
	public byte[] getByte(String key) {
		Object obj = super.get(key);
		if (obj == null) {
			return null;
		}
		return (byte[]) obj;
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 Timestamp 객체를 리턴한다.
	 * 
	 * @param key 값을 찾기 위한 키 문자열
	 * 
	 * @return key에 매핑되어 있는 Timestamp 객체
	 */
	public Timestamp getTimestamp(String key) {
		String str = getString(key);
		if (str == null || "".equals(str)) {
			return null;
		}
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.KOREA);
		formatter.format(new java.util.Date());
		return Timestamp.valueOf(str + " " + formatter.format(new java.util.Date()));
	}

	/** 
	 * Box 객체가 가지고 있는 값들을 화면 출력을 위해 문자열로 변환한다.
	 * 
	 * @return 화면에 출력하기 위해 변환된 문자열
	 */
	public String toString() {
		int max = size() - 1;
		StringBuffer buf = new StringBuffer();
		Enumeration keys = keys();
		Enumeration objects = elements();
		buf.append("{");
		for (int i = 0; i <= max; i++) {
			String key = keys.nextElement().toString();
			String value = null;
			Object o = objects.nextElement();
			if (o == null) {
				value = "";
			} else {
				if (o.getClass().isArray()) {
					int length = Array.getLength(o);
					if (length == 0) {
						value = "";
					} else if (length == 1) {
						Object item = Array.get(o, 0);
						if (item == null) {
							value = "";
						} else {
							value = item.toString();
						}
					} else {
						StringBuffer valueBuf = new StringBuffer();
						valueBuf.append("[");
						for (int j = 0; j < length; j++) {
							Object item = Array.get(o, j);
							if (item != null) {
								valueBuf.append(item.toString());
							}
							if (j < length - 1) {
								valueBuf.append(",");
							}
						}
						valueBuf.append("]");
						value = valueBuf.toString();
					}
				} else {
					value = o.toString();
				}
			}
			buf.append(key + "=" + value);
			if (i < max) {
				buf.append(",");
			}
		}
		buf.append("}");
		return "Box[" + _name + "]=" + buf.toString();
	}

	/** 
	 * Box 객체가 가지고 있는 값들을 쿼리 스트링으로 변환한다.
	 * 
	 * @return 쿼리 스트링으로 변환된 문자열
	 */
	public String toQueryString() {
		int max = size() - 1;
		StringBuffer buf = new StringBuffer();
		Enumeration keys = keys();
		Enumeration objects = elements();
		for (int i = 0; i <= max; i++) {
			String key = keys.nextElement().toString();
			Object o = objects.nextElement();
			if (o == null) {
				buf.append(key + "=" + "");
			} else {
				if (o.getClass().isArray()) {
					StringBuffer valueBuf = new StringBuffer();
					for (int j = 0, length = Array.getLength(o); j < length; j++) {
						Object item = Array.get(o, j);
						if (item != null) {
							valueBuf.append(key + "=" + item.toString());
						}
						if (j < length - 1) {
							valueBuf.append("&");
						}
					}
					buf.append(valueBuf.toString());
				} else {
					buf.append(key + "=" + o.toString());
				}
			}
			if (i < max) {
				buf.append("&");
			}
		}
		return buf.toString();
	}

	/** 
	 * Box 객체가 가지고 있는 값들을 Xml로 변환한다.
	 * 
	 * @return Xml로 변환된 문자열
	 */
	public String toXml() {
		int max = size() - 1;
		StringBuffer buf = new StringBuffer();
		Enumeration keys = keys();
		Enumeration objects = elements();
		buf.append("<items>");
		buf.append("<item>");
		for (int i = 0; i <= max; i++) {
			String key = keys.nextElement().toString();
			Object o = objects.nextElement();
			if (o == null || "".equals(o)) {
				buf.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
			} else {
				if (o.getClass().isArray()) {
					int length = Array.getLength(o);
					if (length == 0) {
						buf.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
					} else if (length == 1) {
						Object item = Array.get(o, 0);
						if (item == null || "".equals(item)) {
							buf.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
						} else {
							buf.append("<" + key.toLowerCase() + ">" + "<![CDATA[" + item.toString() + "]]>" + "</" + key.toLowerCase() + ">");
						}
					} else {
						for (int j = 0; j < length; j++) {
							Object item = Array.get(o, j);
							if (item == null || "".equals(item)) {
								buf.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
							} else {
								buf.append("<" + key.toLowerCase() + ">" + "<![CDATA[" + item.toString() + "]]>" + "</" + key.toLowerCase() + ">");
							}
						}
					}
				} else {
					buf.append("<" + key.toLowerCase() + ">" + "<![CDATA[" + o.toString() + "]]>" + "</" + key.toLowerCase() + ">");
				}
			}
		}
		buf.append("</item>");
		buf.append("</items>");
		return buf.toString();
	}

	/** 
	 * Box 객체가 가지고 있는 값들을 Json 표기법으로 변환한다.
	 * 
	 * @return Json 표기법으로 변환된 문자열
	 */
	public String toJson() {
		int max = size() - 1;
		StringBuffer buf = new StringBuffer();
		Enumeration keys = keys();
		Enumeration objects = elements();
		buf.append("{");
		for (int i = 0; i <= max; i++) {
			String key = keys.nextElement().toString();
			String value = null;
			Object o = objects.nextElement();
			if (o == null) {
				value = "''";
			} else {
				if (o.getClass().isArray()) {
					int length = Array.getLength(o);
					if (length == 0) {
						value = "''";
					} else if (length == 1) {
						Object item = Array.get(o, 0);
						if (item == null) {
							value = "''";
						} else {
							value = "'" + escapeJS(item.toString()) + "'";
						}
					} else {
						StringBuffer valueBuf = new StringBuffer();
						valueBuf.append("[");
						for (int j = 0; j < length; j++) {
							Object item = Array.get(o, j);
							if (item != null) {
								valueBuf.append("'" + escapeJS(item.toString()) + "'");
							}
							if (j < length - 1) {
								valueBuf.append(",");
							}
						}
						valueBuf.append("]");
						value = valueBuf.toString();
					}
				} else {
					value = "'" + escapeJS(o.toString()) + "'";
				}
			}
			buf.append(key + ":" + value);
			if (i < max) {
				buf.append(",");
			}
		}
		buf.append("}");
		return buf.toString();
	}

	/**
	 * 자바스크립트상에 특수하게 인식되는 문자들을 JSON등에 사용하기 위해 변환하여준다.
	 * 
	 * @param str 변환할 문자열
	 */
	private String escapeJS(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'").replaceAll("\"", "\\\\").replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
	}
}