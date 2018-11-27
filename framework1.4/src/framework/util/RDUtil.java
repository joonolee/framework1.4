/* 
 * @(#)RDUtil.java
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
 * RD(Report Designer)�� �̿��Ͽ� ������ �� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class RDUtil {

	/**
	 * ����Ʈ �� ������
	 */
	public static final String DEFAULT_COLSEP = "##";

	/**
	 * ����Ʈ �� ������
	 */
	public static final String DEFAULT_LINESEP = "\n";

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet �̿�

	/**
	 * RecordSet�� RD ���� �������� ����Ѵ�.
	 * ��, �� �����ڷ� ����Ʈ �����ڸ� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� RD ���� �������� ����ϴ� ��� => RDUtil.setRecordSet(response, rs)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs RD ���� �������� ��ȯ�� RecordSet ��ü
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSet(HttpServletResponse response, RecordSet rs) throws ColumnNotFoundException, IOException {
		return setRecordSet(response, rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * RecordSet�� RD ���� �������� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� RD ���� �������� ����ϴ� ��� => RDUtil.setRecordSet(response, rs)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs RD ���� �������� ��ȯ�� RecordSet ��ü
	 * @param colSep �� �����ڷ� ���� ���ڿ�
	 * @param lineSep �� �����ڷ� ���� ���ڿ�
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSet(HttpServletResponse response, RecordSet rs, String colSep, String lineSep) throws ColumnNotFoundException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print(lineSep);
			}
			pw.print(rdRowStr(rs, colNms, colSep));
		}
		return rowCount;
	}

	/**
	 * RecordSet�� RD ���� �������� ��ȯ�Ѵ�.
	 * ��, �� �����ڷ� ����Ʈ �����ڸ� ����Ѵ�.
	 * <br>
	 * ex) rs�� RD ���� �������� ��ȯ�ϴ� ��� => String rd = RDUtil.format(rs)
	 * 
	 * @param rs ��ȯ�� RecordSet ��ü
	 * 
	 * @return RD ���� �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String format(RecordSet rs) throws ColumnNotFoundException {
		return format(rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * RecordSet�� RD ���� �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� �������� ##, �౸���� !! �� RD ���� �������� ��ȯ�ϴ� ��� => String rd = RDUtil.format(rs, "##", "!!")
	 * 
	 * @param rs ��ȯ�� RecordSet ��ü
	 * @param colSep �� �����ڷ� ���� ���ڿ�
	 * @param lineSep �� �����ڷ� ���� ���ڿ�
	 * 
	 * @return RD ���� �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String format(RecordSet rs, String colSep, String lineSep) throws ColumnNotFoundException {
		if (rs == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buffer.append(lineSep);
			}
			buffer.append(rdRowStr(rs, colNms, colSep));
		}
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet �̿�

	/**
	 * ResultSet�� RD ���� �������� ����Ѵ�.
	 * ��, �� �����ڷ� ����Ʈ �����ڸ� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� RD ���� �������� ����ϴ� ��� => RDUtil.setResultSet(response, rs)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs RD ���� �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static int setResultSet(HttpServletResponse response, ResultSet rs) throws SQLException, IOException {
		return setResultSet(response, rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * ResultSet�� RD ���� �������� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� RD ���� �������� ����ϴ� ��� => RDUtil.setResultSet(response, rs)
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs RD ���� �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param colSep �� �����ڷ� ���� ���ڿ�
	 * @param lineSep �� �����ڷ� ���� ���ڿ�
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static int setResultSet(HttpServletResponse response, ResultSet rs, String colSep, String lineSep) throws SQLException, IOException {
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
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					pw.print(lineSep);
				}
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				pw.print(rdRowStr(columns, colSep));
			}
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
	 * ResultSet�� RD ���� �������� ��ȯ�Ѵ�.
	 * ��, �� �����ڷ� ����Ʈ �����ڸ� ����Ѵ�.
	 * <br>
	 * ex) rs�� RD ���� �������� ��ȯ�ϴ� ��� => String rd = RDUtil.format(rs)
	 * 
	 * @param rs ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * 
	 * @return RD ���� �������� ��ȯ�� ���ڿ�
	 * @throws SQLException 
	 */
	public static String format(ResultSet rs) throws SQLException {
		return format(rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * ResultSet�� RD ���� �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� �������� ##, �౸���� !! �� RD ���� �������� ��ȯ�ϴ� ��� => String rd = RDUtil.format(rs, "##", "!!")
	 * 
	 * @param rs ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param colSep �� �����ڷ� ���� ���ڿ�
	 * @param lineSep �� �����ڷ� ���� ���ڿ�
	 * 
	 * @return RD ���� �������� ��ȯ�� ���ڿ�
	 * @throws SQLException 
	 */
	public static String format(ResultSet rs, String colSep, String lineSep) throws SQLException {
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
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					buffer.append(lineSep);
				}
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				buffer.append(rdRowStr(columns, colSep));
			}
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
	 * Map��ü�� RD ���� �������� ��ȯ�Ѵ�.
	 * �� �����ڷ� ����Ʈ �����ڸ� ����Ѵ�.
	 * <br>
	 * ex) map�� RD ���� �������� ��ȯ�ϴ� ��� => String rd = RDUtil.format(map)
	 *
	 * @param map ��ȯ�� Map��ü
	 *
	 * @return RD ���� �������� ��ȯ�� ���ڿ�
	 */
	public static String format(Map map) {
		return format(map, DEFAULT_COLSEP);
	}

	/**
	 * Map��ü�� RD ���� �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex) map�� �������� ## �� RD ���� �������� ��ȯ�ϴ� ��� => String rd = RDUtil.format(map, "##")
	 *
	 * @param map ��ȯ�� Map��ü
	 * @param colSep �� �����ڷ� ���� ���ڿ�
	 *
	 * @return RD ���� �������� ��ȯ�� ���ڿ�
	 */
	public static String format(Map map, String colSep) {
		if (map == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(rdRowStr(map, colSep));
		return buffer.toString();
	}

	/**
	 * List��ü�� RD ���� �������� ��ȯ�Ѵ�.
	 * ��, �� �����ڷ� ����Ʈ �����ڸ� ����Ѵ�.
	 * <br>
	 * ex1) mapList�� RD ���� �������� ��ȯ�ϴ� ��� => String rd = RDUtil.format(mapList)
	 *
	 * @param mapList ��ȯ�� List��ü
	 *
	 * @return RD ���� �������� ��ȯ�� ���ڿ�
	 */
	public static String format(List mapList) {
		return format(mapList, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * List��ü�� RD ���� �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex1) mapList�� �������� ##, �౸���� !! �� RD ���� �������� ��ȯ�ϴ� ��� => String rd = RDUtil.format(mapList, "##", "!!")
	 *
	 * @param mapList ��ȯ�� List��ü
	 * @param colSep �� �����ڷ� ���� ���ڿ�
	 * @param lineSep �� �����ڷ� ���� ���ڿ�
	 *
	 * @return RD ���� �������� ��ȯ�� ���ڿ�
	 */
	public static String format(List mapList, String colSep, String lineSep) {
		if (mapList == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		Iterator iter = mapList.iterator();
		while (iter.hasNext()) {
			Map map = (Map) iter.next();
			buffer.append(rdRowStr(map, colSep));
			buffer.append(lineSep);
		}
		buffer.delete(buffer.length() - lineSep.length(), buffer.length());
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private �޼ҵ�
	/**
	 * RD(����Ʈ�����̳�) �� Row ���ڿ� ����
	 */
	private static String rdRowStr(Map map, String colSep) {
		StringBuffer buffer = new StringBuffer();
		Set keys = map.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (map.get(key) != null) {
				buffer.append(map.get(key));
			}
			buffer.append(colSep);
		}
		return buffer.toString();
	}

	/**
	 * RD(����Ʈ�����̳�) �� Row ���ڿ� ����
	 * @throws ColumnNotFoundException 
	 */
	private static String rdRowStr(RecordSet rs, String[] colNms, String colSep) throws ColumnNotFoundException {
		StringBuffer buffer = new StringBuffer();
		for (int c = 0; c < colNms.length; c++) {
			if (rs.get(colNms[c]) != null) {
				buffer.append(rs.get(colNms[c]));
			}
			buffer.append(colSep);
		}
		return buffer.toString();
	}
}
