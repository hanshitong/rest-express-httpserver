package org.restexpress.intf;

/**
 * 业务实现这个接口并注入到spring，让运维或可以在外面通过telnet 输入命令获取系统的各种状态
 * @author hanst
 *
 */
public interface SystemStatIntf {
	/**
	 * 返回的系统状态说明，比如当前有多少登录用户，有多少内存，可以返回plain text html等
	 * @return
	 */
	public String getStat(String command);
	/**
	 * telnet 里输入的命令,不能为null,"",不能重复,不能是help,nettyconfig，作为保留字
	 * @return
	 */
	public String[] getCommand();
	
	/**
	 * 命令功能说明
	 * @return
	 */
	public String[] commandDesc();
}
