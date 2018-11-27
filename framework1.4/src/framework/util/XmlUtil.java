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
 * XML을 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public class XmlUtil {

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet 이용

	/**
	 * RecordSet을 xml 형식으로 출력한다. (xml 헤더포함)
	 * <br>
	 * ex) response로 rs를 xml 형식으로 출력하는 경우 => XmlUtil.setRecordSet(response, rs, "utf-8")
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs xml 형식으로 변환할 RecordSet 객체
	 * @param encoding 헤더에 포함될 인코딩
	 * 
	 * @return 처리건수
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
	 * RecordSet을 xml 형식으로 변환한다. (xml 헤더 미포함)
	 * <br>
	 * ex) rs를 xml 형식으로 변환하는 경우 => String xml = XmlUtil.format(rs)
	 *
	 * @param rs xml 형식으로 변환할 RecordSet 객체
	 *
	 * @return xml 형식으로 변환된 문자열
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
	 * RecordSet을 xml 형식으로 변환한다. (xml 헤더포함)
	 * <br>
	 * ex) rs를 xml 형식으로 변환하는 경우 => String xml = XmlUtil.format(rs, "utf-8")
	 *
	 * @param rs xml 형식으로 변환할 RecordSet 객체
	 * @param encoding 헤더에 포함될 인코딩
	 *
	 * @return xml 형식으로 변환된 문자열
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

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet 이용

	/**
	 * ResultSet을 xml 형식으로 출력한다 (xml 헤더포함).
	 * <br>
	 * ex) response로 rs를 xml 형식으로 출력하는 경우 => XmlUtil.setResultSet(response, rs, "utf-8")
	 *
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param encoding 헤더에 포함될 인코딩
	 * 
	 * @return 처리건수
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
			pw.print(xmlHeaderStr(encoding));
			pw.print("<items>");
			int rowCount = 0;
			while (rs.next()) {
				rowCount++;
				// 현재 Row 저장 객체
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
	 * ResultSet을 xml 형식으로 변환한다 (xml 헤더 미포함).
	 * <br>
	 * ex) rs를 xml 형식으로 변환하는 경우 => String xml = XmlUtil.format(rs)
	 *
	 * @param rs xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
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
			buffer.append("<items>");
			while (rs.next()) {
				// 현재 Row 저장 객체
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
	 * ResultSet을 xml 형식으로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) rs를 xml 형식으로 변환하는 경우 => String xml = XmlUtil.format(rs, "utf-8")
	 *
	 * @param rs xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param encoding 헤더에 포함될 인코딩
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

	////////////////////////////////////////////////////////////////////////////////////////// 기타 Collection 이용

	/**
	 * Map객체를 xml 형식으로 변환한다 (xml 헤더 미포함).
	 * <br>
	 * ex) map을 xml 형식으로 변환하는 경우 => String xml = XmlUtil.format(map)
	 *
	 * @param map 변환할 Map객체
	 *
	 * @return xml 형식으로 변환된 문자열
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
	 * Map객체를 xml 형식으로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) map을 xml 형식으로 변환하는 경우  => String xml = XmlUtil.format(map, "utf-8")
	 *
	 * @param map 변환할 Map객체
	 * @param encoding 헤더에 포함될 인코딩
	 *
	 * @return xml 형식으로 변환된 문자열
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
	 * List객체를 xml 형태로 변환한다 (xml 헤더 미포함).
	 * <br>
	 * ex) mapList를 xml으로 변환하는 경우 => String xml = XmlUtil.format(mapList)
	 *
	 * @param mapList 변환할 List객체
	 *
	 * @return xml형식으로 변환된 문자열
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
	 * List객체를 xml 형태로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) mapList를 xml으로 변환하는 경우  => String xml = XmlUtil.format(mapList, "utf-8")
	 *
	 * @param mapList 변환할 List객체
	 * @param encoding 헤더에 포함될 인코딩
	 *
	 * @return xml형식으로 변환된 문자열
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

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 *  xml 헤더 문자열 생성
	 */
	private static String xmlHeaderStr(String encoding) {
		return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
	}

	/**
	 * xml item 문자열 생성
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
	 * xml item 문자열 생성
	 * @throws ColumnNotFoundException 
	 */
	private static String xmlItemStr(RecordSet rs, String[] colNms, String[] colInfo) throws ColumnNotFoundException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<item>");
		for (int c = 0; c < colNms.length; c++) {
			if (colInfo[c].equals("LONG") || colInfo[c].equals("LONG RAW") || colInfo[c].equals("INTEGER") || colInfo[c].equals("FLOAT") || colInfo[c].equals("DOUBLE") || colInfo[c].equals("NUMBER")) { // 값이 숫자일때
				if (rs.get(colNms[c]) == null) {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + 0 + "</" + colNms[c].toLowerCase() + ">");
				} else {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + rs.getDouble(colNms[c]) + "</" + colNms[c].toLowerCase() + ">");
				}
			} else { // 값이 문자일때
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