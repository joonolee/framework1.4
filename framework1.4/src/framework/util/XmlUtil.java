/*
 * @(#)XmlUtil.java
 */
package framework.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import framework.db.ColumnNotFoundException;
import framework.db.RecordSet;

/**
 * XML�� �̿��Ͽ� ������ �� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class XmlUtil {

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet �̿�

	/**
	 * RecordSet�� xml �������� ����Ѵ�. (xml �������)
	 * <br>
	 * ex) response�� rs�� xml �������� ����ϴ� ��� => XmlUtil.setRecordSet(response, rs, "utf-8")
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs xml �������� ��ȯ�� RecordSet ��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 * 
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSet(HttpServletResponse response, RecordSet rs, String encoding) throws ColumnNotFoundException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		pw.print(xmlHeaderStr(encoding));
		pw.print("<items>");
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			pw.print(xmlItemStr(rs, colNms, colInfo));
		}
		pw.print("</items>");
		return rowCount;
	}

	/**
	 * RecordSet�� xml �������� ��ȯ�Ѵ�. (xml ��� ������)
	 * <br>
	 * ex) rs�� xml �������� ��ȯ�ϴ� ��� => String xml = XmlUtil.format(rs)
	 *
	 * @param rs xml �������� ��ȯ�� RecordSet ��ü
	 *
	 * @return xml �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String format(RecordSet rs) throws ColumnNotFoundException {
		if (rs == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		buffer.append("<items>");
		while (rs.nextRow()) {
			buffer.append(xmlItemStr(rs, colNms, colInfo));
		}
		buffer.append("</items>");
		return buffer.toString();
	}

	/**
	 * RecordSet�� xml �������� ��ȯ�Ѵ�. (xml �������)
	 * <br>
	 * ex) rs�� xml �������� ��ȯ�ϴ� ��� => String xml = XmlUtil.format(rs, "utf-8")
	 *
	 * @param rs xml �������� ��ȯ�� RecordSet ��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 *
	 * @return xml �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String format(RecordSet rs, String encoding) throws ColumnNotFoundException {
		if (rs == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(xmlHeaderStr(encoding));
		buffer.append(format(rs));
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet �̿�

	/**
	 * ResultSet�� xml �������� ����Ѵ� (xml �������).
	 * <br>
	 * ex) response�� rs�� xml �������� ����ϴ� ��� => XmlUtil.setResultSet(response, rs, "utf-8")
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs xml �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param encoding ����� ���Ե� ���ڵ�
	 * 
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static int setResultSet(HttpServletResponse response, ResultSet rs, String encoding) throws SQLException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			String[] columns_key = new String[count];
			String[] columns_keyInfo = new String[count];
			int[] columns_keySize = new int[count];
			int[] columns_keySizeReal = new int[count];
			int[] columns_keyScale = new int[count];
			// byte[] ������ ó���� ���ؼ� �߰�
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table�� Field �� �ҹ��� �ΰ��� �빮�ڷ� ����ó��
				columns_key[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels �� ���� �� Size �߰�
				columns_keySize[i - 1] = rsmd.getColumnDisplaySize(i);
				columns_keySizeReal[i - 1] = rsmd.getPrecision(i);
				columns_keyScale[i - 1] = rsmd.getScale(i);
				columns_keyInfo[i - 1] = rsmd.getColumnTypeName(i);
			}
			pw.print(xmlHeaderStr(encoding));
			pw.print("<items>");
			int rowCount = 0;
			while (rs.next()) {
				rowCount++;
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				pw.print(xmlItemStr(columns));
			}
			pw.print("</items>");
			return rowCount;
		} finally {
			Statement stmt = rs.getStatement();
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}

	/**
	 * ResultSet�� xml �������� ��ȯ�Ѵ� (xml ��� ������).
	 * <br>
	 * ex) rs�� xml �������� ��ȯ�ϴ� ��� => String xml = XmlUtil.format(rs)
	 *
	 * @param rs xml �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @throws SQLException 
	 */
	public static String format(ResultSet rs) throws SQLException {
		if (rs == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			String[] columns_key = new String[count];
			String[] columns_keyInfo = new String[count];
			int[] columns_keySize = new int[count];
			int[] columns_keySizeReal = new int[count];
			int[] columns_keyScale = new int[count];
			// byte[] ������ ó���� ���ؼ� �߰�
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table�� Field �� �ҹ��� �ΰ��� �빮�ڷ� ����ó��
				columns_key[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels �� ���� �� Size �߰�
				columns_keySize[i - 1] = rsmd.getColumnDisplaySize(i);
				columns_keySizeReal[i - 1] = rsmd.getPrecision(i);
				columns_keyScale[i - 1] = rsmd.getScale(i);
				columns_keyInfo[i - 1] = rsmd.getColumnTypeName(i);
			}
			buffer.append("<items>");
			while (rs.next()) {
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				buffer.append(xmlItemStr(columns));
			}
			buffer.append("</items>");
		} finally {
			Statement stmt = rs.getStatement();
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
		return buffer.toString();
	}

	/**
	 * ResultSet�� xml �������� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) rs�� xml �������� ��ȯ�ϴ� ��� => String xml = XmlUtil.format(rs, "utf-8")
	 *
	 * @param rs xml �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param encoding ����� ���Ե� ���ڵ�
	 * 
	 * @throws SQLException 
	 */
	public static String format(ResultSet rs, String encoding) throws SQLException {
		if (rs == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(xmlHeaderStr(encoding));
		buffer.append(format(rs));
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ��Ÿ Collection �̿�

	/**
	 * Map��ü�� xml �������� ��ȯ�Ѵ� (xml ��� ������).
	 * <br>
	 * ex) map�� xml �������� ��ȯ�ϴ� ��� => String xml = XmlUtil.format(map)
	 *
	 * @param map ��ȯ�� Map��ü
	 *
	 * @return xml �������� ��ȯ�� ���ڿ�
	 */
	public static String format(Map map) {
		if (map == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<items>");
		buffer.append(xmlItemStr(map));
		buffer.append("</items>");
		return buffer.toString();
	}

	/**
	 * Map��ü�� xml �������� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) map�� xml �������� ��ȯ�ϴ� ���  => String xml = XmlUtil.format(map, "utf-8")
	 *
	 * @param map ��ȯ�� Map��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 *
	 * @return xml �������� ��ȯ�� ���ڿ�
	 */
	public static String format(Map map, String encoding) {
		if (map == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(xmlHeaderStr(encoding));
		buffer.append(format(map));
		return buffer.toString();
	}

	/**
	 * List��ü�� xml ���·� ��ȯ�Ѵ� (xml ��� ������).
	 * <br>
	 * ex) mapList�� xml���� ��ȯ�ϴ� ��� => String xml = XmlUtil.format(mapList)
	 *
	 * @param mapList ��ȯ�� List��ü
	 *
	 * @return xml�������� ��ȯ�� ���ڿ�
	 */
	public static String format(List mapList) {
		if (mapList == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<items>");
		Iterator iter = mapList.iterator();
		while (iter.hasNext()) {
			Map map = (Map) iter.next();
			buffer.append(xmlItemStr(map));
		}
		buffer.append("</items>");
		return buffer.toString();
	}

	/**
	 * List��ü�� xml ���·� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) mapList�� xml���� ��ȯ�ϴ� ���  => String xml = XmlUtil.format(mapList, "utf-8")
	 *
	 * @param mapList ��ȯ�� List��ü
	 * @param encoding ����� ���Ե� ���ڵ�
	 *
	 * @return xml�������� ��ȯ�� ���ڿ�
	 */
	public static String format(List mapList, String encoding) {
		if (mapList == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(xmlHeaderStr(encoding));
		buffer.append(format(mapList));
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private �޼ҵ�

	/**
	 *  xml ��� ���ڿ� ����
	 */
	private static String xmlHeaderStr(String encoding) {
		return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
	}

	/**
	 * xml item ���ڿ� ����
	 */
	private static String xmlItemStr(Map map) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<item>");
		Set keys = map.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (map.get(key) == null) {
				buffer.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
			} else {
				if (map.get(key) instanceof Number) {
					buffer.append("<" + key.toLowerCase() + ">" + map.get(key) + "</" + key.toLowerCase() + ">");
				} else if (map.get(key) instanceof Map) {
					buffer.append("<" + key.toLowerCase() + ">" + format((Map) map.get(key)) + "</" + key.toLowerCase() + ">");
				} else if (map.get(key) instanceof List) {
					buffer.append("<" + key.toLowerCase() + ">" + format((List) map.get(key)) + "</" + key.toLowerCase() + ">");
				} else {
					buffer.append("<" + key.toLowerCase() + ">" + "<![CDATA[" + map.get(key) + "]]>" + "</" + key.toLowerCase() + ">");
				}
			}
		}
		buffer.append("</item>");
		return buffer.toString();
	}

	/**
	 * xml item ���ڿ� ����
	 * @throws ColumnNotFoundException 
	 */
	private static String xmlItemStr(RecordSet rs, String[] colNms, String[] colInfo) throws ColumnNotFoundException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<item>");
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) { // ���� �����϶�
				if (rs.get(colNms[c]) == null) {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + 0 + "</" + colNms[c].toLowerCase() + ">");
				} else {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + rs.getDouble(colNms[c]) + "</" + colNms[c].toLowerCase() + ">");
				}
			} else { // ���� �����϶�
				if (rs.get(colNms[c]) == null) {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + "</" + colNms[c].toLowerCase() + ">");
				} else {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + "<![CDATA[" + rs.get(colNms[c]) + "]]>" + "</" + colNms[c].toLowerCase() + ">");
				}
			}
		}
		buffer.append("</item>");
		return buffer.toString();
	}
}