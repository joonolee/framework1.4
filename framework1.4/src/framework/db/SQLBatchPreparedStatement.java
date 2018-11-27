/* 
 * @(#)SQLBatchStatement.java
 * Statement의 Batch 처리를 이용하기 위한 객체
 */
package framework.db;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class SQLBatchPreparedStatement extends DBStatement {
	private String _sql;
	private ConnectionManager _connMgr = null;
	private PreparedStatement _pstmt = null;
	private List _paramList = new ArrayList();
	private Object _caller = null;

	public SQLBatchPreparedStatement(String sql, ConnectionManager connMgr, Object caller) {
		this._sql = sql;
		this._connMgr = connMgr;
		this._caller = caller;
	}

	public void addBatch(Object[] where) {
		List param = new ArrayList();
		for (int i = 0, len = where.length; i < len; i++) {
			param.add(where[i]);
		}
		_paramList.add(param);
	}

	protected PreparedStatement getPrepareStatment() throws SQLException {
		if (getSQL() == null) {
			getLogger().error("Query is Null");
			return null;
		}
		try {
			if (_pstmt == null) {
				_pstmt = _connMgr.getConnection().prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				_pstmt.setFetchSize(100);
			}
		} catch (SQLException e) {
			getLogger().error("getPrepareStatment Error!");
			throw e;
		}
		return _pstmt;
	}

	public void close() throws SQLException {
		try {
			if (_pstmt != null) {
				_pstmt.close();
				_pstmt = null;
			}
			clearParamList();
		} catch (SQLException e) {
			getLogger().error("close Error!");
			throw e;
		}
	}

	public void clearParamList() {
		this._paramList = new ArrayList();
	}

	public int[] executeBatch() throws SQLException {
		if (getSQL() == null) {
			getLogger().error("Query is Null");
			return new int[] { 0 };
		}
		int[] _upCnts = null;
		try {
			PreparedStatement pstmt = getPrepareStatment();
			if (getLogger().isDebugEnabled()) {
				StringBuffer log = new StringBuffer();
				log.append("@Sql Start (BATCH P_STATEMENT) FetchSize : " + pstmt.getFetchSize() + " Caller : " + _caller.getClass().getName() + "\n");
				log.append("@Sql Command => \n" + getQueryString());
				getLogger().debug(log.toString());
			}
			Iterator iter = _paramList.iterator();
			while (iter.hasNext()) {
				List param = (List) iter.next();
				for (int i = 1, length = param.size(); i <= length; i++) {
					if (param.get(i - 1) == null || "".equals(param.get(i - 1))) {
						pstmt.setNull(i, java.sql.Types.VARCHAR);
					} else if (param.get(i - 1) instanceof byte[]) {
						int size = ((byte[]) param.get(i - 1)).length;
						if (size > 0) {
							InputStream is = new ByteArrayInputStream((byte[]) param.get(i - 1));
							pstmt.setBinaryStream(i, is, size);
						} else {
							pstmt.setBinaryStream(i, null, 0);
						}
					} else {
						pstmt.setObject(i, param.get(i - 1));
					}
				}
				pstmt.addBatch();
			}
			_upCnts = pstmt.executeBatch();
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("@Sql End (BATCH P_STATEMENT)");
			}
		} catch (SQLException e) {
			getLogger().error("executeQuery Error!");
			throw new SQLException(e.getMessage() + "\nSQL : " + getQueryString());
		}
		return _upCnts;
	}

	public void setSQL(String newSql) throws SQLException {
		close();
		_sql = newSql;
	}

	public String getSQL() {
		return this._sql;
	}

	public String toString() {
		return "SQL : " + getSQL();
	}

	public String getQueryString() {
		StringBuffer buf = new StringBuffer();
		Iterator iter = _paramList.iterator();
		while (iter.hasNext()) {
			List param = (List) iter.next();
			Object value = null;
			int qMarkCount = 0;
			StringTokenizer token = new StringTokenizer(getSQL(), "?");
			while (token.hasMoreTokens()) {
				String oneChunk = token.nextToken();
				buf.append(oneChunk);
				try {
					if (param.size() > qMarkCount) {
						value = param.get(qMarkCount++);
						if (value == null || "".equals(value)) {
							value = "NULL";
						} else if (value instanceof String || value instanceof Date) {
							value = "'" + value + "'";
						}
					} else {
						if (token.hasMoreTokens()) {
							value = null;
						} else {
							value = "";
						}
					}
					buf.append("" + value);
				} catch (Throwable e) {
					buf.append("ERROR WHEN PRODUCING QUERY STRING FOR LOG." + e.toString());
				}
			}
			buf.append("\n");
		}
		return buf.toString().trim();
	}
}