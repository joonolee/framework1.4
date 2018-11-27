/* 
 * @(#)PagingUtil.java
 */
package framework.util;

import java.util.HashMap;
import java.util.Map;

/**
 * �׺���̼� ���� ����¡ ���� ���� ���̺귯��
 */
public class PagingUtil {
	/**
	 * ����¡�� ���� �ʿ��� ������ �����Ѵ�.
	 * 
	 * @param totcnt ��ü ���ڵ� �Ǽ�
	 * @param pagenum ���� ������ ��ȣ 
	 * @param pagesize ���������� ������ ������
	 * @param displaysize �׺���̼� ����¡ ������
	 * @return totalpage(��ü��������), pagenum(����������), startpage(����������), endpage(��������)������ ��� �ִ� �� ��ü
	 */
	public static Map getPagingMap(Integer totcnt, Integer pagenum, Integer pagesize, Integer displaysize) {
		int l_totcnt = totcnt.intValue();
		int l_pagenum = pagenum.intValue();
		int l_pagesize = pagesize.intValue();
		int l_displaysize = displaysize.intValue();
		Map resultMap = new HashMap();

		int l_totalpage = (int) Math.floor(l_totcnt / l_pagesize);
		if (l_totcnt % l_pagesize != 0)
			l_totalpage += 1;
		int l_startpage = (int) (Math.floor((l_pagenum - 1) / l_displaysize) * l_displaysize) + 1;
		int l_endpage = (int) (Math.floor(((l_pagenum - 1) + l_displaysize) / l_displaysize)) * l_displaysize;
		if (l_totalpage <= l_endpage)
			l_endpage = l_totalpage;

		resultMap.put("totalpage", new Integer(l_totalpage));
		resultMap.put("pagenum", new Integer(l_pagenum));
		resultMap.put("startpage", new Integer(l_startpage));
		resultMap.put("endpage", new Integer(l_endpage));
		resultMap.put("displaysize", new Integer(l_displaysize));

		return resultMap;
	}
}
