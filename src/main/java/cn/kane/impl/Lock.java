package cn.kane.impl;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import cn.kane.service.ILock;
import cn.kane.service.vo.AsyncResp;

public class Lock implements ILock {

	private static final String LOCK_NODE_SUFFIX = "/locks" ;
	private static final long WAIT_INTERNAL_MILLS = 3000 ;
	private static final int ANY_VERSION = -1 ;
	/** zkClient */
	private ZooKeeper zkClient ;
	/** zkServer-adds,such-as[ip1:port1,ip2:port2] */
	@SuppressWarnings("unused")
	private String connStr ;
	/** session-timeout,unit-milliseconds */
	@SuppressWarnings("unused")
	private int sessionTimeout ;
	private Watcher watcher ;
	private String nodeName ;
	//TODO mutex multi-instance
	private Object mutex ;
	
	public Lock(String connStr, int sessionTimeout,String nodeName) throws IOException, KeeperException, InterruptedException{
		this.connStr = connStr ;
		this.sessionTimeout = sessionTimeout ;
		this.mutex = new Object() ;
		//watcher
		this.watcher = new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				System.out.println(String.format("Event:::Path[%s],Type[%s],State[%s]", event.getPath(),event.getType().name(),event.getState().name()));
				if(event.getType().compareTo(EventType.NodeDeleted) == 0){
					synchronized (mutex) {
						mutex.notifyAll();
					}
				}
			}
		} ;
		//zkClient
		zkClient = new ZooKeeper(connStr, sessionTimeout, watcher) ;
		this.nodeName = nodeName ;
		Stat stat = zkClient.exists(nodeName, false) ;
		if(null == stat){
			//createNode
			byte[] data = new byte[4];
			String realPath = zkClient.create(nodeName, data , ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) ;
			System.out.println("CreateNode:"+realPath);
		}
	}
	
	@Override
	public String getLockSync() throws KeeperException, InterruptedException {
		String lockNodeName = this.createNode();
		boolean getLock = false ;
		while(!getLock){
			String currentLock = this.getCurrentLock() ;
			if(lockNodeName.endsWith(currentLock)){
				getLock = true ;
			}else{
				try{
					synchronized (mutex) {
						mutex.wait(WAIT_INTERNAL_MILLS);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return lockNodeName ;
	}

	private String getCurrentLock() throws KeeperException, InterruptedException{
		String tlockName = null ;
		List<String> childs = zkClient.getChildren(nodeName, watcher) ;
		if(null != childs && !childs.isEmpty()){
			//min-lock
			int minIndex = Integer.MAX_VALUE ;
			tlockName = null ;
			for(String lockName : childs){
				int index = Integer.parseInt(lockName.substring(LOCK_NODE_SUFFIX.length()-1)) ;
				if(minIndex > index){
					minIndex = index ;
					tlockName = lockName ;
				}
			}
		}
		return tlockName ;
	}
	
	private String createNode() throws KeeperException, InterruptedException{
		String lockNodeName = zkClient.create(nodeName+LOCK_NODE_SUFFIX, new byte[1], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL) ;
		System.out.println(String.format("LOCK-ADD:%s", lockNodeName));
		return lockNodeName ;
	}
	
	@Override
	public AsyncResp getLockAsync(String lockName) throws KeeperException, InterruptedException {
		AsyncResp resp = new AsyncResp() ;
		boolean getLock = false ;
		if(null == lockName){
			lockName = this.createNode() ;
		}
		String currentLock = this.getCurrentLock() ;
		if(lockName.endsWith(currentLock)){
			getLock = true ;
		}else{
			getLock =false ;
		}
		resp.setGotLock(getLock);
		resp.setLockItemName(lockName);
		return resp ;
	}

	@Override
	public void releaseLock(String lockName) throws KeeperException, InterruptedException {
		String currentLock = this.getCurrentLock() ;
		if(lockName.endsWith(currentLock)){
			zkClient.delete(lockName, ANY_VERSION);
		}else{
			//nothing
		}
	}

}
