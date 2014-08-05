package cn.kane.impl;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import cn.kane.service.ILeadElection;

public class LeadElection implements ILeadElection {

	private static final String LEADER_PATH = "/leader" ;
	private static final int ANY_VERSION = -1 ;
	private ZooKeeper zkClient = null ;
	@SuppressWarnings("unused")
	private Watcher watcher ;
	private String nodeName ;
	public LeadElection(String nodeName,int sessionTimeOut,String conn,Watcher watcher) throws IOException, KeeperException, InterruptedException{
		this.nodeName = nodeName ;
		this.watcher = watcher ;
		zkClient = new ZooKeeper(conn, sessionTimeOut, watcher) ;
		Stat stat = zkClient.exists(nodeName, watcher) ;
		if(null == stat){
			zkClient.create(nodeName, new byte[4], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) ;
		}
	}
	
	@Override
	public long getLeadSessionId() throws KeeperException, InterruptedException {
		Stat stat = zkClient.exists(nodeName+LEADER_PATH, false) ;
		if(null == stat){
			try{
				zkClient.create(nodeName+LEADER_PATH, new byte[4], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL) ;
				//be leader
				System.out.println("BE LEADER");
			}catch(KeeperException e){
				//leader already exist
				System.out.println("FOLLOW");
			}
		}else{
			System.out.println("FOLLOW");
		}
		return 0;
	}

	@Override
	public void disLeader() throws KeeperException, InterruptedException{
		zkClient.delete(nodeName+LEADER_PATH, ANY_VERSION);
	}

}
