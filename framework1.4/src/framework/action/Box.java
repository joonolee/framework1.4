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
 * ��û��ü, ��Ű��ü�� ���� ��� �ؽ����̺� ��ü�̴�.
 * ��û��ü�� �Ķ���͸� �߻�ȭ �Ͽ� Box �� ������ ���� �Ķ�����̸��� Ű�� �ش� ���� ���ϴ� ����Ÿ Ÿ������ ��ȯ�޴´�.
 */
public class Box extends Hashtable {
	private static final long serialVersionUID = 7143941735208780214L;
	private String _name = null;

	/***
	 * Box ������
	 * @param name Box ��ü�� �̸�
	 */
	public Box(String name) {
		super();
		this._name = name;
	}

	/** 
	 * ��û��ü�� �Ķ���� �̸��� ���� ������ �ؽ����̺��� �����Ѵ�.
	 * <br>
	 * ex) request Box ��ü�� ��� ��� => Box box = Box.getBox(request)
	 * 
	 * @param request HTTP Ŭ���̾�Ʈ ��û��ü
	 * 
	 * @return ��ûBox ��ü
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
	 * ��û��ü�� ��Ű �̸��� ���� ������ �ؽ����̺��� �����Ѵ�.
	 * <br>
	 * ex) cookie Box ��ü�� ��� ��� => Box box = Box.getBoxFromCookie(request)
	 * 
	 * @param request HTTP Ŭ���̾�Ʈ ��û��ü
	 * 
	 * @return ��ŰBox ��ü
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
	 * Ű(key)���ڿ��� ���εǾ� �ִ� ������Ʈ�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� ������Ʈ
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
	 * Ű(key)���ڿ��� ���εǾ� �ִ� ���ڿ� �迭�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� ���ڿ� �迭
	 */
	public String[] getArray(String key) {
		return (String[]) super.get(key);
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� Boolean ��ü�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� Boolean ��ü
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
	 * Ű(key)���ڿ��� ���εǾ� �ִ� Double ��ü�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� Double ��ü
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
	 * Ű(key)���ڿ��� ���εǾ� �ִ� BigDecimal ��ü�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� BigDecimal ��ü
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
	 * Ű(key)���ڿ��� ���εǾ� �ִ� Float ��ü�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� Float ��ü
	 */
	public Float getFloat(String key) {
		return new Float(getDouble(key).doubleValue());
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� Integer ��ü�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� Integer ��ü
	 */
	public Integer getInteger(String key) {
		Double value = getDouble(key);
		return new Integer(value.intValue());
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� Long ��ü�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� Long ��ü
	 */
	public Long getLong(String key) {
		Double value = getDouble(key);
		return new Long(value.longValue());
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� long ������ �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� long ������
	 */
	public long getlong(String key) {
		Double value = getDouble(key);
		return value.longValue();
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� String ��ü�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� String ��ü
	 */
	public String getString(String key) {
		String str = (String) get(key);
		if (str == null) {
			return "";
		}
		return str;
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� ����Ʈ �迭�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� ����Ʈ �迭
	 */
	public byte[] getByte(String key) {
		Object obj = super.get(key);
		if (obj == null) {
			return null;
		}
		return (byte[]) obj;
	}

	/** 
	 * Ű(key)���ڿ��� ���εǾ� �ִ� Timestamp ��ü�� �����Ѵ�.
	 * 
	 * @param key ���� ã�� ���� Ű ���ڿ�
	 * 
	 * @return key�� ���εǾ� �ִ� Timestamp ��ü
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
	 * Box ��ü�� ������ �ִ� ������ ȭ�� ����� ���� ���ڿ��� ��ȯ�Ѵ�.
	 * 
	 * @return ȭ�鿡 ����ϱ� ���� ��ȯ�� ���ڿ�
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
	 * Box ��ü�� ������ �ִ� ������ ���� ��Ʈ������ ��ȯ�Ѵ�.
	 * 
	 * @return ���� ��Ʈ������ ��ȯ�� ���ڿ�
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
	 * Box ��ü�� ������ �ִ� ������ Xml�� ��ȯ�Ѵ�.
	 * 
	 * @return Xml�� ��ȯ�� ���ڿ�
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
	 * Box ��ü�� ������ �ִ� ������ Json ǥ������� ��ȯ�Ѵ�.
	 * 
	 * @return Json ǥ������� ��ȯ�� ���ڿ�
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
	 * �ڹٽ�ũ��Ʈ�� Ư���ϰ� �νĵǴ� ���ڵ��� JSON� ����ϱ� ���� ��ȯ�Ͽ��ش�.
	 * 
	 * @param str ��ȯ�� ���ڿ�
	 */
	private String escapeJS(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'").replaceAll("\"", "\\\\").replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
	}
}