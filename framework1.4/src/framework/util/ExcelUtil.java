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
 * Excel ����� ���� �̿��� �� �ִ� ��ƿ��Ƽ Ŭ�����̴�.
 */
public class ExcelUtil {

	/**
	 * �޸��� ���е� CSV ���� ����
	 */
	public static final int CSV = 1;

	/**
	 * ��(Tab)���ڷ� ���е� TSV ���� ����
	 */
	public static final int TSV = 2;

	/**
	 * ���� XML ���� ���� (������ �뷮�� ũ��.)
	 */
	public static final int XML = 3;

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet �̿�

	/**
	 * RecordSet�� ���� ���� �������� ����Ѵ�.
	 * <br>
	 * ex1) response�� rs�� �������� �޸�(,) �� ������(CSV, TSV ��)���� �������� ����ϴ� ��� => ExcelUtil.setRecordSet(response, rs, ExcelUtil.CSV)
	 * <br>
	 * ex2) response�� rs�� �������� �ǹ���(\t) �� ������(CSV, TSV ��)���� �������� ����ϴ� ��� => ExcelUtil.setRecordSet(response, rs, ExcelUtil.TSV)
	 * <br>
	 * ex3) response�� rs�� excel xml �������� ����ϴ� ��� => ExcelUtil.setRecordSet(response, rs, ExcelUtil.XML)
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs ���� ���� �������� ��ȯ�� RecordSet ��ü
	 * @param ft ����Ÿ�� (CSV, TSV, XML) 
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSet(HttpServletResponse response, RecordSet rs, int ft) throws ColumnNotFoundException, IOException {
		switch (ft) {
		case TSV:
			return setRecordSetSep(response, rs, "\t");
		case XML:
			return setRecordSetXml(response, rs);
		case CSV:
		default:
			return setRecordSetSep(response, rs, ",");
		}
	}

	/**
	 * RecordSet�� ������(CSV, TSV ��)���� �������� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� �������� �޸�(,) �� ������(CSV, TSV ��)���� �������� ����ϴ� ��� => ExcelUtil.setRecordSetSep(response, rs, ",")
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs ������(CSV, TSV ��)���� �������� ��ȯ�� RecordSet ��ü
	 * @param sep �� �����ڷ� ���� ���ڿ�
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSetSep(HttpServletResponse response, RecordSet rs, String sep) throws ColumnNotFoundException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print("\n");
			}
			pw.print(sepRowStr(rs, colNms, sep));
		}
		return rowCount;
	}

	/**
	 * RecordSet�� ������(CSV, TSV ��)���� �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� �������� �޸�(,) �� ������(CSV, TSV ��)���� �������� ��ȯ�ϴ� ��� => String csv = ExcelUtil.formatSep(rs, ",")
	 * 
	 * @param rs ��ȯ�� RecordSet ��ü
	 * @param sep �� �����ڷ� ���� ���ڿ�
	 * 
	 * @return ������(CSV, TSV ��)���� �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String formatSep(RecordSet rs, String sep) throws ColumnNotFoundException {
		if (rs == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buffer.append("\n");
			}
			buffer.append(sepRowStr(rs, colNms, sep));
		}
		return buffer.toString();
	}

	/**
	 * RecordSet�� excel xml �������� ����Ѵ� (xml �������).
	 * <br>
	 * ex) response�� rs�� excel xml �������� ����ϴ� ��� => ExcelUtil.setRecordSetXml(response, rs)
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs excel xml �������� ��ȯ�� RecordSet ��ü
	 * @return ó���Ǽ�
	 * @throws ColumnNotFoundException 
	 * @throws IOException 
	 */
	public static int setRecordSetXml(HttpServletResponse response, RecordSet rs) throws ColumnNotFoundException, IOException {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw = response.getWriter();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		pw.print(xmlHeaderStr("utf-8"));
		pw.print("<?mso-application progid=\"Excel.Sheet\"?>");
		pw.print("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
		pw.print("<Worksheet ss:Name=\"Result1\">");
		pw.print("<Table>");
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			if (rowCount % 65537 == 0) { // ����ο찡 65536�̸� ���ο� ��Ʈ�� �����Ѵ�.
				pw.print("</Table>");
				pw.print("</Worksheet>");
				pw.print("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
				pw.print("<Table>");
			}
			pw.print(xmlRowStr(rs, colNms, colInfo));
		}
		pw.print("</Table>");
		pw.print("</Worksheet>");
		pw.print("</Workbook>");
		return rowCount;
	}

	/**
	 * RecordSet�� excel xml �������� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) rs�� excel xml �������� ��ȯ�ϴ� ��� => String excel = ExcelUtil.formatXml(rs)
	 *
	 * @param rs excel xml �������� ��ȯ�� RecordSet ��ü
	 *
	 * @return excel xml�������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String formatXml(RecordSet rs) throws ColumnNotFoundException {
		return formatXml(rs, true);
	}

	/**
	 * RecordSet�� excel xml �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex1) rs�� excel xml �������� ��ȯ�ϴ� ��� (xml �������) => String excel = ExcelUtil.formatXml(rs, true)
	 * <br>
	 * ex2) rs�� excel xml �������� ��ȯ�ϴ� ��� (xml ���������) => String excel = ExcelUtil.formatXml(rs, false)
	 *
	 * @param rs excel xml �������� ��ȯ�� RecordSet ��ü
	 * @param isHeader ������� ����
	 *
	 * @return excel xml �������� ��ȯ�� ���ڿ�
	 * @throws ColumnNotFoundException 
	 */
	public static String formatXml(RecordSet rs, boolean isHeader) throws ColumnNotFoundException {
		if (rs == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		rs.moveRow(0);
		if (isHeader) {
			buffer.append(xmlHeaderStr("utf-8"));
		}
		buffer.append("<?mso-application progid=\"Excel.Sheet\"?>");
		buffer.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
		buffer.append("<Worksheet ss:Name=\"Result1\">");
		buffer.append("<Table>");
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			if (rowCount % 65537 == 0) { // ����ο찡 65536�̸� ���ο� ��Ʈ�� �����Ѵ�.
				buffer.append("</Table>");
				buffer.append("</Worksheet>");
				buffer.append("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
				buffer.append("<Table>");
			}
			buffer.append(xmlRowStr(rs, colNms, colInfo));
		}
		buffer.append("</Table>");
		buffer.append("</Worksheet>");
		buffer.append("</Workbook>");
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet �̿�

	/**
	 * ResultSet�� ���� ���� �������� ����Ѵ�.
	 * <br>
	 * ex1) response�� rs�� �������� �޸�(,) �� ������(CSV, TSV ��)���� �������� ����ϴ� ��� => ExcelUtil.setResultSet(response, rs, ExcelUtil.CSV)
	 * <br>
	 * ex2) response�� rs�� �������� �ǹ���(\t) �� ������(CSV, TSV ��)���� �������� ����ϴ� ��� => ExcelUtil.setResultSet(response, rs, ExcelUtil.TSV)
	 * <br>
	 * ex3) response�� rs�� excel xml �������� ����ϴ� ��� => ExcelUtil.setResultSet(response, rs, ExcelUtil.XML)
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs ���� ���� �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param ft ����Ÿ�� (CSV, TSV, XML)
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static int setResultSet(HttpServletResponse response, ResultSet rs, int ft) throws SQLException, IOException {
		switch (ft) {
		case TSV:
			return setResultSetSep(response, rs, "\t");
		case XML:
			return setResultSetXml(response, rs);
		case CSV:
		default:
			return setResultSetSep(response, rs, ",");
		}
	}

	/**
	 * ResultSet�� ������(CSV, TSV ��)���� �������� ����Ѵ�.
	 * <br>
	 * ex) response�� rs�� �������� �޸�(,) �� ������(CSV, TSV ��)���� �������� ����ϴ� ��� => ExcelUtil.setResultSetSep(response, rs, ",")
	 * 
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs ������(CSV, TSV ��)���� �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param sep �� �����ڷ� ���� ���ڿ�
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static int setResultSetSep(HttpServletResponse response, ResultSet rs, String sep) throws SQLException, IOException {
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
					pw.print("\n");
				}
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				pw.print(sepRowStr(columns, sep));
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
	 * ResultSet�� ������(CSV, TSV ��)���� �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex) rs�� �������� �޸�(,) �� ������(CSV, TSV ��)���� �������� ��ȯ�ϴ� ��� => String csv = ExcelUtil.formatSep(rs, ",")
	 * 
	 * @param rs ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param sep �� �����ڷ� ���� ���ڿ�
	 * 
	 * @return ������(CSV, TSV ��)���� �������� ��ȯ�� ���ڿ�
	 * @throws SQLException 
	 */
	public static String formatSep(ResultSet rs, String sep) throws SQLException {
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
					buffer.append("\n");
				}
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				buffer.append(sepRowStr(columns, sep));
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

	/**
	 * ResultSet�� excel xml �������� ����Ѵ� (xml �������).
	 * <br>
	 * ex) response�� rs�� excel xml �������� ����ϴ� ��� => ExcelUtil.setResultSetXml(response, rs)
	 *
	 * @param response Ŭ���̾�Ʈ�� ������ Response ��ü
	 * @param rs excel xml �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @return ó���Ǽ�
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static int setResultSetXml(HttpServletResponse response, ResultSet rs) throws SQLException, IOException {
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
			pw.print(xmlHeaderStr("utf-8"));
			pw.print("<?mso-application progid=\"Excel.Sheet\"?>");
			pw.print("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
			pw.print("<Worksheet ss:Name=\"Result1\">");
			pw.print("<Table>");
			int rowCount = 0;
			while (rs.next()) {
				rowCount++;
				if (rowCount % 65537 == 0) { // ����ο찡 65536�̸� ���ο� ��Ʈ�� �����Ѵ�.
					pw.print("</Table>");
					pw.print("</Worksheet>");
					pw.print("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
					pw.print("<Table>");
				}
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				pw.print(xmlRowStr(columns));
			}
			pw.print("</Table>");
			pw.print("</Worksheet>");
			pw.print("</Workbook>");
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
	 * ResultSet�� excel xml �������� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) rs�� excel xml �������� ��ȯ�ϴ� ��� => String excel = ExcelUtil.formatXml(rs)
	 *
	 * @param rs excel xml �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 *
	 * @return excel xml�������� ��ȯ�� ���ڿ�
	 * @throws SQLException 
	 */
	public static String formatXml(ResultSet rs) throws SQLException {
		return formatXml(rs, true);
	}

	/**
	 * ResultSet�� excel xml �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex1) rs�� excel xml �������� ��ȯ�ϴ� ��� (xml �������) => String excel = ExcelUtil.formatXml(rs, true)
	 * <br>
	 * ex2) rs�� excel xml �������� ��ȯ�ϴ� ��� (xml ���������) => String excel = ExcelUtil.formatXml(rs, false)
	 *
	 * @param rs excel xml �������� ��ȯ�� ResultSet ��ü, ResultSet ��ü�� �ڵ����� close �ȴ�.
	 * @param isHeader ������� ����
	 *
	 * @return excel xml �������� ��ȯ�� ���ڿ�
	 * @throws SQLException 
	 */
	public static String formatXml(ResultSet rs, boolean isHeader) throws SQLException {
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
			if (isHeader) {
				buffer.append(xmlHeaderStr("utf-8"));
			}
			buffer.append("<?mso-application progid=\"Excel.Sheet\"?>");
			buffer.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
			buffer.append("<Worksheet ss:Name=\"Result1\">");
			buffer.append("<Table>");
			int rowCount = 0;
			while (rs.next()) {
				rowCount++;
				if (rowCount % 65537 == 0) { // ����ο찡 65536�̸� ���ο� ��Ʈ�� �����Ѵ�.
					buffer.append("</Table>");
					buffer.append("</Worksheet>");
					buffer.append("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
					buffer.append("<Table>");
				}
				// ���� Row ���� ��ü
				Map columns = new LinkedHashMap(count);
				for (int i = 1; i <= count; i++) {
					columns.put(columns_key[i - 1], rs.getObject(columns_key[i - 1]));
				}
				buffer.append(xmlRowStr(columns));
			}
			buffer.append("</Table>");
			buffer.append("</Worksheet>");
			buffer.append("</Workbook>");
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
	 * Map��ü�� ������(CSV, TSV ��)���� �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex) map�� �������� �޸�(,) �� ������(CSV, TSV ��)���� �������� ��ȯ�ϴ� ��� => String csv = ExcelUtil.formatSep(map, ",")
	 *
	 * @param map ��ȯ�� Map��ü
	 * @param sep �� �����ڷ� ���� ���ڿ�
	 *
	 * @return ������(CSV, TSV ��)���� �������� ��ȯ�� ���ڿ�
	 */
	public static String formatSep(Map map, String sep) {
		if (map == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(sepRowStr(map, sep));
		return buffer.toString();
	}

	/**
	 * List��ü�� ������(CSV, TSV ��)���� �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex1) mapList�� �������� �޸�(,) �� ������(CSV, TSV ��)���� �������� ��ȯ�ϴ� ��� => String csv = ExcelUtil.formatSep(mapList, ",")
	 *
	 * @param mapList ��ȯ�� List��ü
	 * @param sep �� �����ڷ� ���� ���ڿ�
	 *
	 * @return ������(CSV, TSV ��)���� �������� ��ȯ�� ���ڿ�
	 */
	public static String formatSep(List mapList, String sep) {
		if (mapList == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		Iterator iter = mapList.iterator();
		int rowCount = 0;
		while (iter.hasNext()) {
			Map map = (Map) iter.next();
			if (rowCount++ > 0) {
				buffer.append("\n");
			}
			buffer.append(sepRowStr(map, sep));
		}
		return buffer.toString();
	}

	/**
	 * Map��ü�� excel xml �������� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) map�� excel xml �������� ��ȯ�ϴ� ��� => String xml = ExcelUtil.formatXml(map)
	 *
	 * @param map ��ȯ�� Map��ü
	 *
	 * @return excel xml �������� ��ȯ�� ���ڿ�
	 */
	public static String formatXml(Map map) {
		return formatXml(map, true);
	}

	/**
	 * Map��ü�� excel xml �������� ��ȯ�Ѵ�.
	 * <br>
	 * ex1) map�� excel xml �������� ��ȯ�ϴ� ��� (xml �������) => String xml = ExcelUtil.formatXml(map, true)
	 * <br>
	 * ex2) map�� excel xml �������� ��ȯ�ϴ� ��� (xml ���������) => String xml = ExcelUtil.formatXml(map, false)
	 *
	 * @param map ��ȯ�� Map��ü
	 * @param isHeader ������� ����
	 *
	 * @return excel xml �������� ��ȯ�� ���ڿ�
	 */
	public static String formatXml(Map map, boolean isHeader) {
		if (map == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		if (isHeader) {
			buffer.append(xmlHeaderStr("utf-8"));
		}
		buffer.append("<?mso-application progid=\"Excel.Sheet\"?>");
		buffer.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
		buffer.append("<Worksheet ss:Name=\"Result1\">");
		buffer.append("<Table>");
		buffer.append(xmlRowStr(map));
		buffer.append("</Table>");
		buffer.append("</Worksheet>");
		buffer.append("</Workbook>");
		return buffer.toString();
	}

	/**
	 * List��ü�� excel xml ���·� ��ȯ�Ѵ� (xml �������).
	 * <br>
	 * ex) mapList�� excel xml���� ��ȯ�ϴ� ��� => String xml = ExcelUtil.formatXml(mapList)
	 *
	 * @param mapList ��ȯ�� List��ü
	 *
	 * @return excel xml�������� ��ȯ�� ���ڿ�
	 */
	public static String formatXml(List mapList) {
		return formatXml(mapList, true);
	}

	/**
	 * List��ü�� excel xml ���·� ��ȯ�Ѵ�.
	 * <br>
	 * ex1) mapList�� excel xml���� ��ȯ�ϴ� ��� (xml �������) => String xml = ExcelUtil.formatXml(mapList, true)
	 * <br>
	 * ex2) mapList�� excel xml���� ��ȯ�ϴ� ��� (xml ���������) => String xml = ExcelUtil.formatXml(mapList, false)
	 *
	 * @param mapList ��ȯ�� List��ü
	 * @param isHeader ������� ����
	 *
	 * @return excel xml�������� ��ȯ�� ���ڿ�
	 */
	public static String formatXml(List mapList, boolean isHeader) {
		if (mapList == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		if (isHeader) {
			buffer.append(xmlHeaderStr("utf-8"));
		}
		buffer.append("<?mso-application progid=\"Excel.Sheet\"?>");
		buffer.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
		buffer.append("<Worksheet ss:Name=\"Result1\">");
		buffer.append("<Table>");
		Iterator iter = mapList.iterator();
		int rowCount = 0;
		while (iter.hasNext()) {
			Map map = (Map) iter.next();
			rowCount++;
			if (rowCount % 65537 == 0) { // ����ο찡 65536�̸� ���ο� ��Ʈ�� �����Ѵ�.
				buffer.append("</Table>");
				buffer.append("</Worksheet>");
				buffer.append("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
				buffer.append("<Table>");
			}
			buffer.append(xmlRowStr(map));
		}
		buffer.append("</Table>");
		buffer.append("</Worksheet>");
		buffer.append("</Workbook>");
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// ��ƿ��Ƽ

	/**
	 * �����ڷ� ���̴� ���ڿ� �Ǵ� ���๮�ڰ� ���� ���ԵǾ� ���� ��� ���� �ֵ���ǥ�� �ѷ��ε��� ��ȯ�Ѵ�.
	 * 
	 * @param str ��ȯ�� ���ڿ�
	 * @param sep �� �����ڷ� ���� ���ڿ�
	 */
	public static String escapeSep(String str, String sep) {
		if (str == null) {
			return "";
		}
		return (str.indexOf(sep) > 0 || str.indexOf("\n") > 0) ? "\"" + str + "\"" : str;
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private �޼ҵ�

	/**
	 * ������(CSV, TSV ��)���� ������ Row ���ڿ� ����
	 * ����Ÿ�� ���ڰ� �ƴҶ����� �����ڷ� ���� ���ڿ� �Ǵ� ���๮�ڸ� escape �ϱ� ���� ���� �ֵ���ǥ�� �ѷ��Ѵ�.
	 */
	private static String sepRowStr(Map map, String sep) {
		StringBuffer buffer = new StringBuffer();
		Set keys = map.keySet();
		Iterator iter = keys.iterator();
		int rowCount = 0;
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (rowCount++ > 0) {
				buffer.append(sep);
			}
			if (map.get(key) != null) {
				if (map.get(key) instanceof Number) {
					buffer.append(map.get(key));
				} else {
					buffer.append(escapeSep(map.get(key).toString(), sep));
				}
			}
		}
		return buffer.toString();
	}

	/**
	 * ������(CSV, TSV ��)���� ������ Row ���ڿ� ����
	 * ����Ÿ�� ���ڰ� �ƴҶ����� �����ڷ� ���� ���ڿ� �Ǵ� ���๮�ڸ� escape �ϱ� ���� ���� �ֵ���ǥ�� �ѷ��Ѵ�.
	 * @throws ColumnNotFoundException 
	 */
	private static String sepRowStr(RecordSet rs, String[] colNms, String sep) throws ColumnNotFoundException {
		StringBuffer buffer = new StringBuffer();
		int rowCount = 0;
		for (int c = 0; c < colNms.length; c++) {
			if (rowCount++ > 0) {
				buffer.append(sep);
			}
			if (rs.get(colNms[c]) != null) {
				if (rs.get(colNms[c]) instanceof Number) {
					buffer.append(rs.get(colNms[c]));
				} else {
					buffer.append(escapeSep(rs.get(colNms[c]).toString(), sep));
				}
			}
		}
		return buffer.toString();
	}

	/**
	 *  xml ��� ���ڿ� ����
	 */
	private static String xmlHeaderStr(String encoding) {
		return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
	}

	/**
	 * xml excel �� Row ���ڿ� ����
	 */
	private static String xmlRowStr(Map map) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<Row>");
		Set keys = map.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (map.get(key) == null) {
				buffer.append("<Cell><Data ss:Type=\"String\"></Data></Cell>");
			} else {
				if (map.get(key) instanceof Number) {
					buffer.append("<Cell><Data ss:Type=\"Number\">" + map.get(key) + "</Data></Cell>");
				} else {
					buffer.append("<Cell><Data ss:Type=\"String\">" + "<![CDATA[" + map.get(key) + "]]>" + "</Data></Cell>");
				}
			}
		}
		buffer.append("</Row>");
		return buffer.toString();
	}

	/**
	 * xml excel �� Row ���ڿ� ����
	 * @throws ColumnNotFoundException 
	 */
	private static String xmlRowStr(RecordSet rs, String[] colNms, String[] colInfo) throws ColumnNotFoundException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<Row>");
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) { // ���� �����϶�
				if (rs.get(colNms[c]) == null) {
					buffer.append("<Cell><Data ss:Type=\"Number\"></Data></Cell>");
				} else {
					buffer.append("<Cell><Data ss:Type=\"Number\">" + rs.getDouble(colNms[c]) + "</Data></Cell>");
				}
			} else { // ���� �����϶�
				if (rs.get(colNms[c]) == null) {
					buffer.append("<Cell><Data ss:Type=\"String\"></Data></Cell>");
				} else {
					buffer.append("<Cell><Data ss:Type=\"String\">" + "<![CDATA[" + rs.get(colNms[c]) + "]]>" + "</Data></Cell>");
				}
			}
		}
		buffer.append("</Row>");
		return buffer.toString();
	}
}