package cn.kane.service;

import org.apache.zookeeper.KeeperException;

public interface ILeadElection {

	long getLeadSessionId()throws KeeperException, InterruptedException ;
	
	void disLeader()throws KeeperException, InterruptedException ;
	
}
