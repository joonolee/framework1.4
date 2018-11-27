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
 * Excel 출력을 위해 이용할 수 있는 유틸리티 클래스이다.
 */
public class ExcelUtil {

	/**
	 * 콤마로 구분된 CSV 파일 형식
	 */
	public static final int CSV = 1;

	/**
	 * 탭(Tab)문자로 구분된 TSV 파일 형식
	 */
	public static final int TSV = 2;

	/**
	 * 엑셀 XML 파일 형식 (파일의 용량이 크다.)
	 */
	public static final int XML = 3;

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet 이용

	/**
	 * RecordSet을 엑셀 파일 형식으로 출력한다.
	 * <br>
	 * ex1) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => ExcelUtil.setRecordSet(response, rs, ExcelUtil.CSV)
	 * <br>
	 * ex2) response로 rs를 열구분자 탭문자(\t) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => ExcelUtil.setRecordSet(response, rs, ExcelUtil.TSV)
	 * <br>
	 * ex3) response로 rs를 excel xml 형식으로 출력하는 경우 => ExcelUtil.setRecordSet(response, rs, ExcelUtil.XML)
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 엑셀 파일 형식으로 변환할 RecordSet 객체
	 * @param ft 파일타입 (CSV, TSV, XML) 
	 * @return 처리건수
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
	 * RecordSet을 구분자(CSV, TSV 등)파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => ExcelUtil.setRecordSetSep(response, rs, ",")
	 * 
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 구분자(CSV, TSV 등)파일 형식으로 변환할 RecordSet 객체
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 처리건수
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
	 * RecordSet을 구분자(CSV, TSV 등)파일 형식으로 변환한다.
	 * <br>
	 * ex) rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 => String csv = ExcelUtil.formatSep(rs, ",")
	 * 
	 * @param rs 변환할 RecordSet 객체
	 * @param sep 열 구분자로 쓰일 문자열
	 * 
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
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
	 * RecordSet을 excel xml 형식으로 출력한다 (xml 헤더포함).
	 * <br>
	 * ex) response로 rs를 excel xml 형식으로 출력하는 경우 => ExcelUtil.setRecordSetXml(response, rs)
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs excel xml 형식으로 변환할 RecordSet 객체
	 * @return 처리건수
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
			if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
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
	 * RecordSet을 excel xml 형식으로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) rs를 excel xml 형식으로 변환하는 경우 => String excel = ExcelUtil.formatXml(rs)
	 *
	 * @param rs excel xml 형식으로 변환할 RecordSet 객체
	 *
	 * @return excel xml형식으로 변환된 문자열
	 * @throws ColumnNotFoundException 
	 */
	public static String formatXml(RecordSet rs) throws ColumnNotFoundException {
		return formatXml(rs, true);
	}

	/**
	 * RecordSet을 excel xml 형식으로 변환한다.
	 * <br>
	 * ex1) rs를 excel xml 형식으로 변환하는 경우 (xml 헤더포함) => String excel = ExcelUtil.formatXml(rs, true)
	 * <br>
	 * ex2) rs를 excel xml 형식으로 변환하는 경우 (xml 헤더미포함) => String excel = ExcelUtil.formatXml(rs, false)
	 *
	 * @param rs excel xml 형식으로 변환할 RecordSet 객체
	 * @param isHeader 헤더포함 여부
	 *
	 * @return excel xml 형식으로 변환된 문자열
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
			if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
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

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet 이용

	/**
	 * ResultSet을 엑셀 파일 형식으로 출력한다.
	 * <br>
	 * ex1) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => ExcelUtil.setResultSet(response, rs, ExcelUtil.CSV)
	 * <br>
	 * ex2) response로 rs를 열구분자 탭문자(\t) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => ExcelUtil.setResultSet(response, rs, ExcelUtil.TSV)
	 * <br>
	 * ex3) response로 rs를 excel xml 형식으로 출력하는 경우 => ExcelUtil.setResultSet(response, rs, ExcelUtil.XML)
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 엑셀 파일 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param ft 파일타입 (CSV, TSV, XML)
	 * @return 처리건수
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
	 * ResultSet을 구분자(CSV, TSV 등)파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => ExcelUtil.setResultSetSep(response, rs, ",")
	 * 
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 구분자(CSV, TSV 등)파일 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 처리건수
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
			// byte[] 데이터 처리를 위해서 추가
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table의 Field 가 소문자 인것은 대문자로 변경처리
				columns_key[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels 의 정보 및 Size 추가
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
				// 현재 Row 저장 객체
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
	 * ResultSet을 구분자(CSV, TSV 등)파일 형식으로 변환한다.
	 * <br>
	 * ex) rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 => String csv = ExcelUtil.formatSep(rs, ",")
	 * 
	 * @param rs 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param sep 열 구분자로 쓰일 문자열
	 * 
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
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
			// byte[] 데이터 처리를 위해서 추가
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table의 Field 가 소문자 인것은 대문자로 변경처리
				columns_key[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels 의 정보 및 Size 추가
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
				// 현재 Row 저장 객체
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
	 * ResultSet을 excel xml 형식으로 출력한다 (xml 헤더포함).
	 * <br>
	 * ex) response로 rs를 excel xml 형식으로 출력하는 경우 => ExcelUtil.setResultSetXml(response, rs)
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs excel xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @return 처리건수
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
			// byte[] 데이터 처리를 위해서 추가
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table의 Field 가 소문자 인것은 대문자로 변경처리
				columns_key[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels 의 정보 및 Size 추가
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
				if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
					pw.print("</Table>");
					pw.print("</Worksheet>");
					pw.print("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
					pw.print("<Table>");
				}
				// 현재 Row 저장 객체
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
	 * ResultSet을 excel xml 형식으로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) rs를 excel xml 형식으로 변환하는 경우 => String excel = ExcelUtil.formatXml(rs)
	 *
	 * @param rs excel xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 *
	 * @return excel xml형식으로 변환된 문자열
	 * @throws SQLException 
	 */
	public static String formatXml(ResultSet rs) throws SQLException {
		return formatXml(rs, true);
	}

	/**
	 * ResultSet을 excel xml 형식으로 변환한다.
	 * <br>
	 * ex1) rs를 excel xml 형식으로 변환하는 경우 (xml 헤더포함) => String excel = ExcelUtil.formatXml(rs, true)
	 * <br>
	 * ex2) rs를 excel xml 형식으로 변환하는 경우 (xml 헤더미포함) => String excel = ExcelUtil.formatXml(rs, false)
	 *
	 * @param rs excel xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param isHeader 헤더포함 여부
	 *
	 * @return excel xml 형식으로 변환된 문자열
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
			// byte[] 데이터 처리를 위해서 추가
			int[] columnsType = new int[count];
			for (int i = 1; i <= count; i++) {
				//Table의 Field 가 소문자 인것은 대문자로 변경처리
				columns_key[i - 1] = rsmd.getColumnName(i).toUpperCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels 의 정보 및 Size 추가
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
				if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
					buffer.append("</Table>");
					buffer.append("</Worksheet>");
					buffer.append("<Worksheet ss:Name=\"Result" + ((rowCount / 65537) + 1) + "\">");
					buffer.append("<Table>");
				}
				// 현재 Row 저장 객체
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

	////////////////////////////////////////////////////////////////////////////////////////// 기타 Collection 이용

	/**
	 * Map객체를 구분자(CSV, TSV 등)파일 형식으로 변환한다.
	 * <br>
	 * ex) map을 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 => String csv = ExcelUtil.formatSep(map, ",")
	 *
	 * @param map 변환할 Map객체
	 * @param sep 열 구분자로 쓰일 문자열
	 *
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
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
	 * List객체를 구분자(CSV, TSV 등)파일 형식으로 변환한다.
	 * <br>
	 * ex1) mapList를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 => String csv = ExcelUtil.formatSep(mapList, ",")
	 *
	 * @param mapList 변환할 List객체
	 * @param sep 열 구분자로 쓰일 문자열
	 *
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
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
	 * Map객체를 excel xml 형식으로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) map을 excel xml 형식으로 변환하는 경우 => String xml = ExcelUtil.formatXml(map)
	 *
	 * @param map 변환할 Map객체
	 *
	 * @return excel xml 형식으로 변환된 문자열
	 */
	public static String formatXml(Map map) {
		return formatXml(map, true);
	}

	/**
	 * Map객체를 excel xml 형식으로 변환한다.
	 * <br>
	 * ex1) map을 excel xml 형식으로 변환하는 경우 (xml 헤더포함) => String xml = ExcelUtil.formatXml(map, true)
	 * <br>
	 * ex2) map을 excel xml 형식으로 변환하는 경우 (xml 헤더미포함) => String xml = ExcelUtil.formatXml(map, false)
	 *
	 * @param map 변환할 Map객체
	 * @param isHeader 헤더포함 여부
	 *
	 * @return excel xml 형식으로 변환된 문자열
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
	 * List객체를 excel xml 형태로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) mapList를 excel xml으로 변환하는 경우 => String xml = ExcelUtil.formatXml(mapList)
	 *
	 * @param mapList 변환할 List객체
	 *
	 * @return excel xml형식으로 변환된 문자열
	 */
	public static String formatXml(List mapList) {
		return formatXml(mapList, true);
	}

	/**
	 * List객체를 excel xml 형태로 변환한다.
	 * <br>
	 * ex1) mapList를 excel xml으로 변환하는 경우 (xml 헤더포함) => String xml = ExcelUtil.formatXml(mapList, true)
	 * <br>
	 * ex2) mapList를 excel xml으로 변환하는 경우 (xml 헤더미포함) => String xml = ExcelUtil.formatXml(mapList, false)
	 *
	 * @param mapList 변환할 List객체
	 * @param isHeader 헤더포함 여부
	 *
	 * @return excel xml형식으로 변환된 문자열
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
			if (rowCount % 65537 == 0) { // 현재로우가 65536이면 새로운 시트를 생성한다.
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

	////////////////////////////////////////////////////////////////////////////////////////// 유틸리티

	/**
	 * 구분자로 쓰이는 문자열 또는 개행문자가 값에 포함되어 있을 경우 값을 쌍따옴표로 둘러싸도록 변환한다.
	 * 
	 * @param str 변환할 문자열
	 * @param sep 열 구분자로 쓰일 문자열
	 */
	public static String escapeSep(String str, String sep) {
		if (str == null) {
			return "";
		}
		return (str.indexOf(sep) > 0 || str.indexOf("\n") > 0) ? "\"" + str + "\"" : str;
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * 구분자(CSV, TSV 등)파일 생성용 Row 문자열 생성
	 * 데이타가 숫자가 아닐때에는 구분자로 쓰인 문자열 또는 개행문자를 escape 하기 위해 값을 쌍따옴표로 둘러싼다.
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
	 * 구분자(CSV, TSV 등)파일 생성용 Row 문자열 생성
	 * 데이타가 숫자가 아닐때에는 구분자로 쓰인 문자열 또는 개행문자를 escape 하기 위해 값을 쌍따옴표로 둘러싼다.
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
	 *  xml 헤더 문자열 생성
	 */
	private static String xmlHeaderStr(String encoding) {
		return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
	}

	/**
	 * xml excel 용 Row 문자열 생성
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
	 * xml excel 용 Row 문자열 생성
	 * @throws ColumnNotFoundException 
	 */
	private static String xmlRowStr(RecordSet rs, String[] colNms, String[] colInfo) throws ColumnNotFoundException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<Row>");
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) { // 값이 숫자일때
				if (rs.get(colNms[c]) == null) {
					buffer.append("<Cell><Data ss:Type=\"Number\"></Data></Cell>");
				} else {
					buffer.append("<Cell><Data ss:Type=\"Number\">" + rs.getDouble(colNms[c]) + "</Data></Cell>");
				}
			} else { // 값이 문자일때
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
