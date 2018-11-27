/* 
 * @(#)JsonUtil.java
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
 * JSON(JavaScript Object Notation)�� �̿��Ͽ� ������ �� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class JsonUtil {

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet �̿�

	/**
	 * RecordSet�� JSON �������� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� JSON �������� ����ϴ� ��� => JsonUtil.setRecordSet(response, rs)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs JSON �������� ��ȯ�� RecordSet ��ü
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSet(HttpServletResponse response, RecordSet rs) throws ColumnNotFoundException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		pw.print("[");
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print(",");
			}
			pw.print(jsonRowStr(rs, colNms, colInfo));
		}
		pw.print("]");
		return rowCount;
	}

	/**
	 * RecordSet�� Json �迭 ���·� ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� JSON �������� ��ȯ�ϴ� ��� => String json = JsonUtil.format(rs)
	 * 
	 * @param rs JSON �������� ��ȯ�� RecordSet ��ü
	 * 
	 * @return JSON �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String format(RecordSet rs) throws ColumnNotFoundException {
		StringBuffer buffer = new StringBuffer();
		if (rs == null) {
			return null;
		}
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		buffer.append("[");
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buffer.append(",");
			}
			buffer.append(jsonRowStr(rs, colNms, colInfo));
		}
		buffer.append("]");
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet �̿�

	/**
	 * ResultSet�� JSON �������� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� JSON �������� ����ϴ� ��� => JsonUtil.setResultSetDirect(response, rs)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs JSON �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 * @throws IOException 
	 * 
	 */
	public static int setResultSet(HttpServletResponse response, ResultSet rs) throws SQLException, IOException {
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
			pw.print("[");
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					pw.print(",");
				}
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				pw.print(jsonRowStr(columns));
			}
			pw.print("]");
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
	 * ResultSet�� Json �迭 ���·� ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� JSON �������� ��ȯ�ϴ� ��� => String json = JsonUtil.format(rs)
	 * 
	 * @param rs JSON �������� ��ȯ�� ResultSet ��ü
	 * 
	 * @return JSON �������� ��ȯ�� ���ڿ�
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
			buffer.append("[");
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					buffer.append(",");
				}
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				buffer.append(jsonRowStr(columns));
			}
			buffer.append("]");
		} finally {
			Statement stmt = rs.getStatement();
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ��Ÿ Collection �̿�

	/**
	 * Map��ü�� JSON �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex) map�� JSON �������� ��ȯ�ϴ� ��� => String json = JsonUtil.format(map)
	 *
	 * @param map ��ȯ�� Map��ü
	 *
	 * @return JSON �������� ��ȯ�� ���ڿ�
	 */
	public static String format(Map map) {
		if (map == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(jsonRowStr(map));
		return buffer.toString();
	}

	/**
	 * List��ü�� JSON �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex1) mapList�� JSON �������� ��ȯ�ϴ� ��� => String json = JsonUtil.format(mapList)
	 *
	 * @param mapList ��ȯ�� List��ü
	 *
	 * @return JSON �������� ��ȯ�� ���ڿ�
	 */
	public static String format(List mapList) {
		if (mapList == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		Iterator iter = mapList.iterator();
		while (iter.hasNext()) {
			buffer.append(jsonRowStr((Map) iter.next()));
			buffer.append(",");
		}
		buffer.delete(buffer.length() - 1, buffer.length());
		buffer.append("]");
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ��ƿ��Ƽ

	/**
	 * �ڹٽ�ũ��Ʈ�� Ư���ϰ� �νĵǴ� ���ڵ��� JSON� ����ϱ� ���� ��ȯ�Ͽ��ش�.
	 * 
	 * @param str ��ȯ�� ���ڿ�
	 */
	public static String escapeJS(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'").replaceAll("\"", "\\\\").replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private �޼ҵ�

	/**
	 * JSON �� Row ���ڿ� ����
	 */
	private static String jsonRowStr(Map map) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		Set keys = map.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (map.get(key) == null) {
				buffer.append(key.toLowerCase() + ":" + "''");
			} else {
				if (map.get(key) instanceof Number) {
					buffer.append(key.toLowerCase() + ":" + map.get(key));
				} else if (map.get(key) instanceof Map) {
					buffer.append(key.toLowerCase() + ":" + format((Map) map.get(key)));
				} else if (map.get(key) instanceof List) {
					buffer.append(key.toLowerCase() + ":" + format((List) map.get(key)));
				} else {
					buffer.append(key.toLowerCase() + ":" + "'" + escapeJS((String) map.get(key)) + "'");
				}
			}
			buffer.append(",");
		}
		buffer.delete(buffer.length() - 1, buffer.length());
		buffer.append("}");
		return buffer.toString();
	}

	/**
	 * JSON �� Row ���ڿ� ����
	 * @throws ColumnNotFoundException 
	 */
	private static String jsonRowStr(RecordSet rs, String[] colNms, String[] colInfo) throws ColumnNotFoundException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) {
				if (rs.get(colNms[c]) == null) {
					buffer.append(colNms[c].toLowerCase() + ":" + 0);
				} else {
					buffer.append(colNms[c].toLowerCase() + ":" + rs.getDouble(colNms[c]));
				}
			} else {
				if (rs.get(colNms[c]) == null) {
					buffer.append(colNms[c].toLowerCase() + ":" + "''");
				} else {
					buffer.append(colNms[c].toLowerCase() + ":" + "'" + escapeJS(rs.get(colNms[c]).toString()) + "'");
				}
			}
			buffer.append(",");
		}
		buffer.delete(buffer.length() - 1, buffer.length());
		buffer.append("}");
		return buffer.toString();
	}
}