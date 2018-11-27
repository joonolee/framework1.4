/* 
 * @(#)AccessLogFilter.java
 * Ŭ���̾�Ʈ ��û ���۰� ���Ḧ �α��ϴ� ����
 */
package framework.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import framework.action.Box;

public class AccessLogFilter implements Filter {
	private static Log _logger = LogFactory.getLog(framework.filter.AccessLogFilter.class);

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) req;
		Box box = Box.getBox(httpReq);
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("�ڡڡ� " + httpReq.getRemoteAddr() + " �� ���� \"" + httpReq.getMethod() + " " + httpReq.getRequestURI() + "\" ��û�� ���۵Ǿ����ϴ�");
			getLogger().debug(box.toString());
		}
		chain.doFilter(req, res);
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("�ڡڡ� " + httpReq.getRemoteAddr() + " �� ���� \"" + httpReq.getMethod() + " " + httpReq.getRequestURI() + "\" ��û�� ����Ǿ����ϴ�\n");
		}
	}

	public void init(FilterConfig config) throws ServletException {
	}

	public void destroy() {
	}

	private Log getLogger() {
		return AccessLogFilter._logger;
	}
}