/* 
 * @(#)ValueObjectArray.java
 * 테이블의 값을 담는 VO의 배열 클래스
 */
package framework.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ValueObjectArray {
	public final static String INSERT = "IK";
	public final static String UPDATE = "UK";
	public final static String DELETE = "DK";
	public final static String USER_UPDATE = "UU";
	public final static String USER_DELETE = "UD";
	public final static String UPDATE_ONLY = "UO";
	private Map _voMap = new HashMap();
	private String[] _keys, _fields = null;
	private int _seq = 0;

	public void add(String type, ValueObject vo) {
		if (vo == null) {
			return;
		}
		_voMap.put(type + _seq++, vo);
	}

	public int size() {
		if (_voMap == null) {
			return 0;
		}
		return _voMap.size();
	}

	public ValueObject[] get(String type) throws Exception {
		if (size() == 0) {
			return null;
		}
		int cnt = 0;
		List list = new ArrayList();
		Iterator key = _voMap.keySet().iterator();
		String keyStr = "";
		while (key.hasNext()) {
			cnt++;
			keyStr = (String) key.next();
			if ((keyStr).substring(0, 2).equals(type)) {
				list.add(_voMap.get(keyStr));
			}
		}
		ValueObject[] r = new ValueObject[list.size()];
		for (int i = 0; i < r.length; i++) {
			r[i] = (ValueObject) list.get(i);
		}
		return r;
	}

	public void clear() {
		_voMap = null;
		_fields = null;
		_keys = null;
	}

	public void setUserKeys(String[] keys) {
		this._keys = keys;
	}

	public void setUserFields(String[] fields) {
		this._fields = fields;
	}

	public String[] getUserKeys() {
		return _keys;
	}

	public String[] getUserFields() {
		return _fields;
	}
}