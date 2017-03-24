package org.restexpress.intf;

import org.restexpress.Request;
import org.restexpress.domain.ex.ServerResponse;
import org.restexpress.domain.ex.SessionInfo;

/**
 * 定义获取会话的接口,在spring里实现应用自己的获取会话的方法,强烈建议轻量级实现
 * @author hanst
 * @param <T>
 *
 */

public interface SessionIntf<T extends SessionInfo> {
	/**
	 * 获取会话信息
	 * @param request
	 * @return
	 */
	T getSession(Request request);
	
	/**
	 * 检查会话的状态
	 * 如果不检查，实现类返回null即可
	 * @return
	 */
	ServerResponse checkSession(T sessionObj);
}
