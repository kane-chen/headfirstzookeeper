package cn.kane.service;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

public interface IFIFOQueue {

	void put(String value, String queueName) throws KeeperException, InterruptedException ;
	
	String take(String queueName,Watcher watcher,Stat stat) throws KeeperException, InterruptedException;
	
}
