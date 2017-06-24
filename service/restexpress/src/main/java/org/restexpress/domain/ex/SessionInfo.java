package org.restexpress.domain.ex;

 

/**
 * 会话信息,应用里的会话信息必须继承此类
 * @author hanst
 *
 */
public abstract class SessionInfo{
	public Class getSessionImpl(){
		return null;
	}
}
