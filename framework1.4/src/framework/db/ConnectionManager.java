/* 
 * @(#)ConnectionManager.java
 * 데이타베이스 컨넥션을 관리하는 클래스
 */
package framework.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConnectionManager {
	private static Map _dsMap = new HashMap();
	private static Log _logger = LogFactory.getLog(framework.db.ConnectionManager.class);
	private List _stmtList = null;
	private String _dsName = null;
	private Object _caller = null;
	private Connection _connection = null;

	public ConnectionManager(String dsName, Object caller) throws DBOpenException {
		this._dsName = dsName;
		this._caller = caller;
		if (_stmtList == null) {
			_stmtList = new ArrayList();
		}
		if (dsName != null) {
			try {
				if (_dsMap.get(dsName) == null) {
					InitialContext ctx = new InitialContext();
					DataSource ds = (DataSource) ctx.lookup(dsName);
					_dsMap.put(dsName, ds);
				}
			} catch (Exception e) {
				throw new DBOpenException(e.getMessage());
			}
		}
	}

	public SQLPreparedStatement createPrepareStatement(String str) throws SQLException {
		SQLPreparedStatement pstmt = new SQLPreparedStatement(str, this, _caller);
		_stmtList.add(pstmt);
		return pstmt;
	}
	
	public SQLBatchPreparedStatement createBatchPrepareStatement(String sql) throws SQLException {
		SQLBatchPreparedStatement pstmt = new SQLBatchPreparedStatement(sql, this, _caller);
		_stmtList.add(pstmt);
		return pstmt;
	}

	public SQLStatement createStatement(String str) throws SQLException {
		SQLStatement stmt = new SQLStatement(str, this, _caller);
		_stmtList.add(stmt);
		return createStatement(null);
	}
	
	public SQLBatchStatement createBatchStatement() throws SQLException {
		SQLBatchStatement stmt = new SQLBatchStatement(this, _caller);
		return stmt;
	}

	public void commit() throws SQLException {
		getConnection().commit();
	}

	public void connect() throws Exception {
		setConnection(((DataSource) _dsMap.get(_dsName)).getConnection());
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("DB연결 성공! => " + _dsName);
		}
	}

	public void connect(String jdbcDriver, String url, String userID, String userPW) throws Exception {
		DriverManager.registerDriver((Driver) Class.forName(jdbcDriver).newInstance());
		setConnection(DriverManager.getConnection(url, userID, userPW));
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("DB연결 성공! => " + url);
		}
	}

	public Connection getConnection() {
		return this._connection;
	}

	public void setConnection(Connection conn) {
		this._connection = conn;
	}

	public void release() {
		try {
			if (_stmtList != null) {
				Iterator iter = _stmtList.iterator();
				while (iter.hasNext()) {
					((DBStatement) iter.next()).close();
				}
			}
			if (getConnection() != null) {
				getConnection().rollback();
				getConnection().close();
				if (getLogger().isDebugEnabled()) {
					getLogger().debug("DB연결 종료! => " + _dsName);
				}
			} else {
				if (getLogger().isDebugEnabled()) {
					getLogger().debug("@CONNECTION IS NULL");
				}
			}
		} catch (Exception e) {
			getLogger().error("Release error!", e);
		}
	}

	public void rollback() {
		try {
			getConnection().rollback();
		} catch (SQLException e) {
		}
	}

	public void setAutoCommit(boolean isAuto) {
		try {
			getConnection().setAutoCommit(isAuto);
		} catch (SQLException e) {
		}
	}

	private Log getLogger() {
		return ConnectionManager._logger;
	}
}