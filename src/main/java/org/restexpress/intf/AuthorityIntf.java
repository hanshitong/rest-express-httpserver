package org.restexpress.intf;

import org.restexpress.domain.ex.SessionInfo;

/**
 * 用户在spring实现这个接口从会话中获得登录用户的基本信息，从而获得权限信息,注意实现这个类的spring必须是多例
 * prototype而不是singleton
 * @author hanst
 * @param <T>
 */
public interface AuthorityIntf {
	/**
	 * 从会话中获得登录用户的基本信息，从而获得权限信息 
	 */	
	void setSessionInfo(SessionInfo sessionInfo); 
	
	void setAuthRightIntf(AuthRightIntf authRightIntf);
	
	/**
	 * 从会话中获取查看资料的权限sql
	 * @return
	 */
	String getAuthoritySql();
	
	/**
	 * 从会话中获取查看资料的权限目标单一值
	 * @return
	 */
	String getAuthorityStr();
	
	/**
	 * 从会话中获取查看资料的权限目标对象 ,后面再添加吧
	 * @return
	 */
//	List<T> getAuthority();
}
