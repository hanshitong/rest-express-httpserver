package org.restexpress.intf;

import org.restexpress.domain.ex.SessionInfo;

/**
 * 用户在spring实现这个接口从会话中获得登录用户的基本信息，从而获得权限信息,注意实现这个类的spring必须是多例
 * prototype而不是singleton
 * @author hanst
 * @param <T>
 */
public interface AuthRightIntf<T> {
	/**
	 * 从会话中获得登录用户的基本信息，从而获得权限信息
	 * @param <T>
	 * 
	 */
	T getAuthRight();
	
	T getAuthRightOnly();
	
	void setSessionInfo(SessionInfo sessionInfo); 
}
