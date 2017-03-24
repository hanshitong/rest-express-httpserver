package org.restexpress.intf;

/**
 * 当spring初始化完毕，实现这个接口的类获得通知并执行
 * @author hanst
 *
 */
public interface SpringInitCompleteAware {
	/**
	 * 执行的方法,这个方法需要是非阻塞的，如果代码里有阻塞代码，请放在线程里运行
	 */
	void execute();
	
	/**
	 * 如果不需要执行的先后顺序，则直接返回0即可,返回值小则优先级高
	 * @return 0 default
	 */
	int getOrder();
}
