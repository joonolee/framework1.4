/** 
 * @(#)SelectConditionObject.java
 */
package framework.db;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL ���忡�� ������ȸ�� �ʿ��� �˻������� ��� ��ü Ŭ�����̴�.
 * �˻� ������ PreparedStatement ����� ���ε� �Ǿ����� �α� ��½� ���ڿ��� ���ε� �Ǿ� ��µȴ�.
 */
public class SelectConditionObject {
	private List _param = new ArrayList();

	/**
	 * �˻� �������� ���ε��� ��ü(Object)�� �����Ѵ�.
	 * 
	 * @param obj ���ε��� ��ü
	 */
	public void setObject(Object obj) {
		_param.add(obj);
	}

	/**
	 * �˻� �������� ���ε��� int�� ������ �����Ѵ�.
	 * 
	 * @param i ���ε��� int�� ����
	 */
	public void setInt(int i) {
		setObject(new Integer(i));
	}

	/**
	 * �˻� �������� ���ε��� long�� ������ �����Ѵ�.
	 * 
	 * @param l ���ε��� long�� ����
	 */
	public void setLong(long l) {
		setObject(new Long(l));
	}

	/**
	 * �˻� �������� ���ε��� double�� ������ �����Ѵ�.
	 * 
	 * @param d ���ε��� double�� ����
	 */
	public void setDouble(double d) {
		setObject(new Double(d));
	}

	/**
	 * �˻��������� ���ε� �� ��� �Ķ���͸� ������Ʈ �迭�� �����Ѵ�.
	 * 
	 * @return ���ε��� ������Ʈ �Ķ����
	 */
	public Object[] getParameter() {
		if (_param == null) {
			return null;
		}
		return _param.toArray();
	}
}