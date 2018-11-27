/** 
 * @(#)RecordSet.java
 */
package framework.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ����Ÿ���̽� ������ ������ �� �� ����� ���� ���� ����� �����ϴ� Ŭ�����̴�.
 */
public class RecordSet implements Serializable {
	private static final long serialVersionUID = -1248669129395067939L;
	/**
	 * DB�� columns �̸�
	 */
	private String[] _columns_key = null;
	private int[] _columns_keySize = null;
	private int[] _columns_keySizeReal = null;
	private int[] _columns_keyScale = null;
	private String[] _columns_keyInfo = null;
	//Rows�� ��
	private ArrayList _rows = new ArrayList();
	private int _currow = 0;

	RecordSet() {
	};

	/**
	 * RecordSet�� ������
	 */
	public RecordSet(ResultSet rs) throws SQLException {
		this(rs, 0, 0);
	}

	/**
	 * �־��� ������ ���ԵǴ� ���ο� RecordSet ��ü�� �����Ѵ�
	 *
	 * @param rs ���� ������
	 * @param curpage ���� ǥ���� ������
	 * @param pagesize �� �������� ǥ���� ������ ����
	 * 
	 * @throws SQLException
	 */
	public RecordSet(ResultSet rs, int curpage, int pagesize) throws SQLException {
		if (rs == null) {
			return;
		}
		ResultSetMetaData rsmd = rs.getMetaData();
		int count = rsmd.getColumnCount();
		_columns_key = new String[count];
		_columns_keyInfo = new String[count];
		_columns_keySize = new int[count];
		_columns_keySizeReal = new int[count];
		_columns_keyScale = new int[count];
		// byte[] ������ ó���� ���ؼ� �߰�
		int[] columnsType = new int[count];
		for (int i = 1; i <= count; i++) {
			//Table�� Field �� �ҹ��� �ΰ��� �빮�ڷ� ����ó��
			_columns_key[i - 1] = rsmd.getColumnName(i).toUpperCase();
			columnsType[i - 1] = rsmd.getColumnType(i);
			//Fiels �� ���� �� Size �߰� 
			_columns_keySize[i - 1] = rsmd.getColumnDisplaySize(i);
			_columns_keySizeReal[i - 1] = rsmd.getPrecision(i);
			_columns_keyScale[i - 1] = rsmd.getScale(i);
			_columns_keyInfo[i - 1] = rsmd.getColumnTypeName(i);
		}
		rs.setFetchSize(100);
		int num = 0;
		while (rs.next()) {

			// ���� Row ���� ��ü
			Map columns = new HashMap(count);
			num++;
			if (curpage != 0 && (num < (curpage - 1) * pagesize + 1)) {
				continue;
			}
			if (pagesize != 0 && (num > curpage * pagesize)) {
				break;
			}
			for (int i = 1; i <= count; i++) {
				columns.put(_columns_key[i - 1], rs.getObject(_columns_key[i - 1]));
			}
			_rows.add(columns);
		}
		if (rs != null)
			rs.close();
	}

	/**
	 * �־��� ������ ���� �� �÷����� String[] �� ��ȯ
	 *
	 * @return String[]
	 */
	public String[] getColumns() {
		return _columns_key;
	}

	/**
	 * �־��� ������ ���� �� �÷��� Size�� int[] �� ��ȯ 
	 *
	 * @return String[]
	 */
	public int[] getColumnsSize() {
		return _columns_keySize;
	}

	/**
	 * �־��� ������ ���� �� �÷��� ���� Size(���ڼӼ��� ���)�� int[] �� ��ȯ 
	 *
	 * @return String[]
	 */
	public int[] getColumnsSizeReal() {
		return _columns_keySizeReal;
	}

	/**
	 * �־��� ������ ���� �� �÷��� �Ҽ��� �Ʒ� ����� int[] �� ��ȯ 
	 *
	 * @return String[]
	 */
	public int[] getColumnsScale() {
		return _columns_keyScale;
	}

	/**
	 * �־��� ������ ���� �� �÷��� ������  String[] �� ��ȯ
	 *
	 * @return String[]
	 */
	public String[] getColumnsInfo() {
		return _columns_keyInfo;
	}

	/**
	 * �־��� ������ ���� �� �����  ArrayList �� ��ȯ
	 *
	 * @return ArrayList
	 */
	public List getRows() {
		return _rows;
	}

	/**
	 * �־��� ���� ���� �� ��� column�� ������ ���Ѵ�
	 *
	 * @return	int �÷��� ����
	 */
	public int getColumnCount() {
		if (_columns_key == null) {
			return 0;
		}
		return _columns_key.length;
	}

	/**
	 * �־��� ���� ���� �� ��� row�� ������ ���Ѵ�
	 * 
	 * @return	int Row�� ����
	 */
	public int getRowCount() {
		if (_rows == null) {
			return 0;
		}
		return _rows.size();
	}

	/**
	 * ���� �����ϰ� �ִ� row�� ��ġ�� ���Ѵ�.
	 * 
	 * @return	int ���� Row�� ��ġ
	 */
	public int getCurrentRow() {
		return _currow;
	}

	/**
	 * ���� ���࿡ ���� ����� ����� Ư�� column�� �̸��� ��´�
	 * 
	 * @param	index	����� �ϴ� �÷� ��ġ, ù��° �÷��� 1
	 * 
	 * @return	String �ش� column�� �̸�
	 */
	public String getColumnLabel(int index) throws IllegalArgumentException, NullPointerException {
		if (index < 1)
			throw new IllegalArgumentException("index 0 is not vaild ");
		if (_columns_key == null) {
			throw new NullPointerException("is not find");
		}
		return _columns_key[index - 1];
	}

	/**
	 * RecordSet�� ó������ �̵��Ѵ�.
	 * 
	 * @return boolean
	 */
	public boolean firstRow() {
		return moveRow(0);
	}

	/**
	 * RecordSet�� ó��row���� �ƴ��� ���� �Ǵ�.
	 * 
	 * @return boolean
	 */
	public boolean isFirst() {
		return (_currow == 0);
	}

	/**
	 * RecordSet�� ������row���� �ƴ��� ���� �Ǵ�.
	 * 
	 * @return boolean
	 */
	public boolean isLast() {
		return (_currow == _rows.size() && _rows.size() != 0);
	}

	/**
	 * RecordSet�� ���������� �̵��Ѵ�.
	 * 
	 * @return boolean
	 */
	public boolean lastRow() {
		if (_rows == null || _rows.size() == 0) {
			return false;
		}
		_currow = _rows.size();
		return true;
	}

	/**
	 * RecordSet���� ���� row�� ���� row�� �̵��Ѵ�.
	 * 
	 * @return boolean
	 */
	public boolean nextRow() {
		_currow++;
		if (_currow == 0 || _rows == null || _rows.size() == 0 || _currow > _rows.size()) {
			return false;
		}
		return true;
	}

	/**
	 * RecordSet�� ���� row�� ���� row�� �̵��Ѵ�.
	 * 
	 * @return boolean
	 */
	public boolean preRow() {
		_currow--;
		if (_currow == 0 || _rows == null || _rows.size() == 0 || _currow > _rows.size()) {
			return false;
		}
		return true;
	}

	/**
	 * �ش��ϴ� �ϴ� row�� �̵�
	 */
	public boolean moveRow(int row) {
		if (_rows != null && _rows.size() != 0 && row <= _rows.size()) {
			_currow = row;
			return true;
		}
		return false;
	}

	/**
	 * Recordset ����Ÿ�� ���´�.
	 * 
	 * @param row cnt : start 1
	 * @param column name
	 */
	public Object get(int row, String column) {
		HashMap record = (HashMap) _rows.get(row - 1);
		return record.get(column.toUpperCase());
	}

	/**
	 * RecordSet�� column ���� String���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return String  column data
	 */
	public String getString(int row, String column) {
		return get(row, column) == null ? "" : ((String) get(row, column)).trim();
	}

	/**
	 * RecordSet�� column ���� int�� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return int  column data
	 */
	public int getInt(int row, String column) {
		return getBigDecimal(row, column).intValue();
	}

	/** 
	 * RecordSet�� column ���� int�� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return int  column data   
	 */
	public int getInteger(int row, String column) {
		return getBigDecimal(row, column).intValue();
	}

	/**
	 * RecordSet�� column ���� long ������ ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return long  column data
	 */
	public long getLong(int row, String column) {
		return getBigDecimal(row, column).longValue();
	}

	/**
	 * RecordSet�� Column ���� double �� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return double column data
	 */
	public double getDouble(int row, String column) {
		return getBigDecimal(row, column).doubleValue();
	}

	/**
	 * RecordSet�� Column ���� BigDecimal �� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return BigDecimal column data
	 */
	public BigDecimal getBigDecimal(int row, String column) {
		if (get(row, column) == null)
			return new BigDecimal(0);
		return (BigDecimal) get(row, column);
	}

	/**
	 * RecordSet�� Column ���� BigDecimal �� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return BigDecimal column data
	 */
	public BigDecimal getBigDecimal(String column) {
		return getBigDecimal(_currow, column);
	}

	/**
	 * RecordSet�� column ���� float�� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return float  column data
	 */
	public float getFloat(int row, String column) {
		return getBigDecimal(row, column).floatValue();
	}

	/**
	 * RecordSet�� column ���� Date������ ��ȯ�ϴ� �޼ҵ�
	 * YYYY-MM-DD �� ��ȯ
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return float  column data
	 */
	public Date getDate(int row, String column) {
		return Date.valueOf(getString(row, column).substring(0, 10));
	}

	/**
	 * RecordSet�� column ���� Timestamp������ ��ȯ�ϴ� �޼ҵ�
	 * YYYY-MM-DD �� ��ȯ
	 * 
	 * @param row  row number, ù��° row�� 1
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return float  column data
	 */
	public Timestamp getTimestamp(int row, String column) {
		if ((String) get(row, column) == null) {
			return null;
		} else {
			return Timestamp.valueOf(get(row, column).toString());
		}
	}

	/**
	 * ���� pointing �� row�� column �����͸� �д´�
	 * 
	 * @param	column	column number, ù��° column �� 1
	 * 
	 * @return String column data
	 */
	public Object get(int column) {
		return get(_currow, _columns_key[column]);
	}

	/**
	 * �������� RecordSet�� int ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return int
	 */
	public int getInt(int column) {
		return getInt(_currow, _columns_key[column]);
	}

	/**
	 * �������� RecordSet�� int ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return Integer
	 */
	public int getInteger(int column) {
		return getInteger(_currow, _columns_key[column]);
	}

	/**
	 * ���� ���� RecordSet�� long ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return long
	 */
	public long getLong(int column) {
		return getLong(_currow, _columns_key[column]);
	}

	/**
	 * ���� ���� RecordSet�� float ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return float
	 */
	public float getFloat(int column) {
		return getFloat(_currow, _columns_key[column]);
	}

	/**
	 * ���� ���� RecordSet�� double ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return double
	 */
	public double getDouble(int column) {
		return getDouble(_currow, _columns_key[column]);
	}

	/**
	 * ���� ���� RecordSet�� Date ���� ��ȯ�ϴ� �޼ҵ�
	 * YYYY-MM-DD �� ��ȯ
	 * 
	 * @param column  column number, ù��° column�� 1
	 * 
	 * @return Date
	 */
	public Date getDate(int column) {
		return getDate(_currow, _columns_key[column]);
	}

	/**
	 * ���� ���� RecordSet�� Timestamp ���� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param column
	 * 
	 * @return Timestamp
	 */
	public Timestamp getTimestamp(int column) {
		return getTimestamp(_currow, _columns_key[column]);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� column �����͸� ���Ѵ�
	 *
	 * @param	name	�а��� �ϴ� column �̸�
	 * 
	 * @return	column data
	 */
	public Object get(String name) throws ColumnNotFoundException {
		return get(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� int�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return int
	 */
	public int getInt(String name) throws ColumnNotFoundException {
		return getInt(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� int�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return Integer
	 */
	public Integer getInteger(String name) throws ColumnNotFoundException {
		Integer returnValue = null;
		returnValue = new Integer(getInt(_currow, name));
		return returnValue;
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� long�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return long
	 */
	public long getLong(String name) throws ColumnNotFoundException {
		return getLong(_currow, name);
	}

	/** 
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� String�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return String
	 */
	public String getString(String name) throws ColumnNotFoundException {
		return getString(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� float�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return float
	 */
	public float getFloat(String name) throws ColumnNotFoundException {
		return getFloat(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� double�� column �����͸� ���Ѵ�
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return double
	 */
	public double getDouble(String name) throws ColumnNotFoundException {
		return getDouble(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� Date�� column �����͸� ���Ѵ�
	 * YYYY-MM-DD�� ��ȯ
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return Date
	 */
	public Date getDate(String name) throws ColumnNotFoundException {
		return getDate(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ ���� pointing�� row�� Date�� column �����͸� ���Ѵ�
	 * YYYY-MM-DD�� ��ȯ
	 * 
	 * @param name �а��� �ϴ� column �̸�
	 * 
	 * @return Date
	 */
	public Timestamp getTimestamp(String name) throws ColumnNotFoundException {
		return getTimestamp(_currow, name);
	}

	/**
	 * ���ڷ� ������ �̸��� ������ column�� ��ġ�� ���Ѵ�.
	 *
	 * @param	name 	column �̸�
	 * 
	 * @return column index, ã�� ���ϸ� -1
	 */
	public int findColumn(String name) throws ColumnNotFoundException {
		if (name == null || _columns_key == null) {
			throw new ColumnNotFoundException("name or column_keys is null ");
		}
		int count = _columns_key.length;
		for (int i = 0; i < count; i++) {
			if (name.equals(_columns_key[i])) {
				return i + 1;
			}
		}
		throw new ColumnNotFoundException("name : " + name + " is not found ");
	}

	/**
	 * ���ڵ� ���� 0 ���� check
	 * 
	 * @return boolean True if there are no records in this object, false otherwise
	 */
	public boolean isEmpty() {
		if (_rows.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ���ͷ����͸� ��ȯ�Ѵ�.
	 */
	public Iterator iterator() {
		return getRows().iterator();
	}
}