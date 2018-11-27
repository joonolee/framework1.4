/** 
 * @(#)ActionServlet.java
 */
package framework.action;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * 컨트롤러 역할을 하는 서블릿으로 모든 클라이언트의 요청을 받아 해당 액션을 실행한다.
 * 확장자가 (.do)로 실행되는 모든 요청을 이 서블릿이 처리하기 위하여 web.xml 파일에서 서블릿을 매핑하여야 하며
 * 서버 부팅시 한개의 객체를 생성해 놓는다.  
 * 요청에서 추출한 액션키로 action.properties에서 Action클래스를 찾아 객체를 생성하여 비지니스 프로세스를 실행한다. 
 */
public class ActionServlet extends HttpServlet {
	private static final long serialVersionUID = -6478697606075642071L;
	private static Log _logger = LogFactory.getLog(framework.action.ActionServlet.class);

	/**
	 * 서블릿 객체를 초기화 한다.
	 * web.xml에 초기화 파라미터로 등록되어 있는 action-mapping 값을 찾아 리소스 번들을 생성하는 역할을 한다.
	 * 
	 * @param config ServletConfig 객체
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ResourceBundle bundle = null;
		try {
			bundle = ResourceBundle.getBundle(config.getInitParameter("action-mapping"));
		} catch (MissingResourceException e) {
			throw new ServletException(e);
		}
		getServletContext().setAttribute("action-mapping", bundle);
	}

	/**
	 * 클라이언트가 Get 방식으로 요청할 경우 processRequest로 처리를 이관한다.
	 * 
	 * @param request HTTP 클라이언트 요청객체
	 * @param response HTTP 클라이언트 응답객체
	 * 
	 * @exception java.io.IOException ActionServlet에서 IO와 관련된 오류가 발생할 경우 
	 * @exception javax.servlet.ServletException 서블릿과 관련된 오류가 발생할 경우
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		processRequest(request, response);
	}

	/**
	 * 클라이언트가 Post 방식으로 요청할 경우 processRequest로 처리를 이관한다.
	 * 
	 * @param request HTTP 클라이언트 요청객체
	 * @param response HTTP 클라이언트 응답객체
	 * 
	 * @exception java.io.IOException ActionServlet에서 IO와 관련된 오류가 발생할 경우 
	 * @exception javax.servlet.ServletException 서블릿과 관련된 오류가 발생할 경우
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			String actionKey = getActionKey(request);
			if (actionKey == null) {
				getLogger().error("Pgm and ActionKey are null!");
				return;
			}
			String actionClassName = getActionClass(actionKey).trim();
			Action action = null;
			try {
				Class actionClass = Class.forName(actionClassName);
				action = (Action) actionClass.newInstance();
			} catch (Exception e) {
				getLogger().error("Pgm Name : [" + actionKey + "] Bean Create Failed!", e);
				return;
			}
			long currTime = 0;
			if (getLogger().isDebugEnabled()) {
				currTime = System.currentTimeMillis();
				getLogger().debug("Start [ Pgm : " + actionKey + " | Action : " + actionClassName + " ]");
			}
			action.execute(this, request, response);
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("End [ Pgm : " + actionKey + " | Action : " + actionClassName + " ] TIME : " + (System.currentTimeMillis() - currTime) + "ms");
			}
		} catch (Exception e) {
			getLogger().error("processRequest Error", e);
		}
	}

	private String getActionClass(String pgm) {
		ResourceBundle bundle = null;
		try {
			bundle = (ResourceBundle) getServletContext().getAttribute("action-mapping");
		} catch (MissingResourceException e) {
			getLogger().error("설정파일에서 액션 클래스를 찾을 수 없습니다.");
		}
		return (String) bundle.getObject(pgm);
	}

	private String getActionKey(HttpServletRequest request) {
		Box box = Box.getBox(request);
		String pgm = box.getString("pgm");
		if (pgm == null || pgm.equals("")) {
			String path = request.getServletPath();
			int slash = path.lastIndexOf("/");
			int period = path.lastIndexOf(".");
			if (period > 0 && period > slash) {
				path = path.substring(slash + 1, period);
				return path;
			}
			return null;
		}
		return pgm;
	}

	private Log getLogger() {
		return ActionServlet._logger;
	}
}