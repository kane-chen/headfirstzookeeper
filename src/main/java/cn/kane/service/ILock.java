package cn.kane.service;

import org.apache.zookeeper.KeeperException;

import cn.kane.service.vo.AsyncResp;

public interface ILock {

	String getLockSync() throws KeeperException, InterruptedException  ;
	
	AsyncResp getLockAsync(String lockName) throws KeeperException, InterruptedException;
	
	void releaseLock(String lockName)  throws KeeperException, InterruptedException ;
	
}
