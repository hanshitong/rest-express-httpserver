package org.restexpress.intf;

/**
 * 分布式锁--提供了redis和zookeeper两种实现，默认使用redis实现
 * 使用场景： 只能有一个线程执行的互斥的资源操作
 * @author hanst
 *
 */
public interface DistributeLock {
	//确保服务可以在2分钟内执行完
	public static final long TIME_OUT = 2 * 60 * 1000;
	
	boolean lock(String key,long expire);
	void unlock(String key);
}
