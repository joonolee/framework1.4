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
 * JSON(JavaScript Object Notation)를 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public class JsonUtil {

	////////////////////////////////////////////////////////////////////////////////////////// RecordSet 이용

	/**
	 * RecordSet을 JSON 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 JSON 형식으로 출력하는 경우 => JsonUtil.setRecordSet(response, rs)
	 * 
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs JSON 형식으로 변환할 RecordSet 객체
	 * @return 처리건수
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
	 * RecordSet을 Json 배열 형태로 변환한다.
	 * <br>
	 * ex) rs를 JSON 형식으로 변환하는 경우 => String json = JsonUtil.format(rs)
	 * 
	 * @param rs JSON 형식으로 변환할 RecordSet 객체
	 * 
	 * @return JSON 형식으로 변환된 문자열
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

	////////////////////////////////////////////////////////////////////////////////////////// ResultSet 이용

	/**
	 * ResultSet을 JSON 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 JSON 형식으로 출력하는 경우 => JsonUtil.setResultSetDirect(response, rs)
	 * 
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs JSON 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @return 처리건수
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
			pw.print("[");
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					pw.print(",");
				}
				// 현재 Row 저장 객체
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
	 * ResultSet을 Json 배열 형태로 변환한다.
	 * <br>
	 * ex) rs를 JSON 형식으로 변환하는 경우 => String json = JsonUtil.format(rs)
	 * 
	 * @param rs JSON 형식으로 변환할 ResultSet 객체
	 * 
	 * @return JSON 형식으로 변환된 문자열
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
			buffer.append("[");
			int rowCount = 0;
			while (rs.next()) {
				if (rowCount++ > 0) {
					buffer.append(",");
				}
				// 현재 Row 저장 객체
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

	////////////////////////////////////////////////////////////////////////////////////////// 기타 Collection 이용

	/**
	 * Map객체를 JSON 형식으로 변환한다.
	 * <br>
	 * ex) map을 JSON 형식으로 변환하는 경우 => String json = JsonUtil.format(map)
	 *
	 * @param map 변환할 Map객체
	 *
	 * @return JSON 형식으로 변환된 문자열
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
	 * List객체를 JSON 형식으로 변환한다.
	 * <br>
	 * ex1) mapList를 JSON 형식으로 변환하는 경우 => String json = JsonUtil.format(mapList)
	 *
	 * @param mapList 변환할 List객체
	 *
	 * @return JSON 형식으로 변환된 문자열
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

	////////////////////////////////////////////////////////////////////////////////////////// 유틸리티

	/**
	 * 자바스크립트상에 특수하게 인식되는 문자들을 JSON등에 사용하기 위해 변환하여준다.
	 * 
	 * @param str 변환할 문자열
	 */
	public static String escapeJS(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'").replaceAll("\"", "\\\\").replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * JSON 용 Row 문자열 생성
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
	 * JSON 용 Row 문자열 생성
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