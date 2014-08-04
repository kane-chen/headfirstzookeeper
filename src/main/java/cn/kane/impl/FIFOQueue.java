package cn.kane.impl;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import cn.kane.service.IFIFOQueue;

public class FIFOQueue implements IFIFOQueue {

	private static final int ANY_VERSION = -1 ;
	private static final String QUEUE_SUFFIX = "/items" ;
	
	private static Object mutex = new Object();
	
	private static FIFOQueue queue ;

	private ZooKeeper zkClient ;
	/** zkServer-adds,such-as[ip1:port1,ip2:port2] */
	@SuppressWarnings("unused")
	private String connStr ;
	/** session-timeout,unit-milliseconds */
	@SuppressWarnings("unused")
	private int sessionTimeout ;
	private Watcher watcher ;
	
	
	private FIFOQueue(String connStr, int sessionTimeout,Watcher watcher) throws IOException{
		this.connStr = connStr ;
		this.sessionTimeout = sessionTimeout ;
		this.watcher = watcher ;
		zkClient = new ZooKeeper(connStr, sessionTimeout, watcher) ;
	}
	
	public static FIFOQueue getInstance(String connStr,int sessionTimeout,Watcher watcher) throws IOException{
		if(null!=queue){
			return queue ;
		}else{
			//double-check
			synchronized (mutex) {
				if(null!=queue){
					return queue ;
				}else{
					queue = new FIFOQueue(connStr, sessionTimeout, watcher) ;
					return queue ;
				}
			}
		}
	}
	
	@Override
	public void put(String value, String queueName) throws KeeperException, InterruptedException {
		//queue exist
		boolean isExisted = this.isNodeExisted(queueName, watcher) ;
		//create node
		if(!isExisted){
			this.createNode(queueName, watcher);
		}
		String realPath = zkClient.create(queueName+QUEUE_SUFFIX,value.getBytes() , ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL) ;
		System.out.println(String.format("Put [%s] in Queue[%s]-Path[%s]", value,queueName,realPath));
	}

	private boolean isNodeExisted(String path,Watcher watcher) throws KeeperException, InterruptedException{
		Stat stat = zkClient.exists(path, watcher);
		if(null == stat){
			return false ;
		}else{
			return true ;
		}
	}
	
	private void createNode(String queueName,Watcher watcher) throws KeeperException, InterruptedException{
		synchronized (mutex) {
			if(!this.isNodeExisted(queueName, watcher)){
				byte[] data = new byte[4];
				String realPath = zkClient.create(queueName, data , ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) ;
				System.out.println("CreateNode:"+realPath);
			}
		}
	}
	
	@Override
	public String take(String queueName,Watcher watcher,Stat stat) throws KeeperException, InterruptedException {
		List<String> nodes = zkClient.getChildren(queueName, watcher) ;
		if(null!=nodes && !nodes.isEmpty()){
			//get min-index(tail in queue)
			int minIndex = Integer.MAX_VALUE ;
			String tnodeName = null ;
			//TODO bad efficiency
			for(String nodeName : nodes){
				if(null!=nodeName){
					int index = Integer.parseInt(nodeName.substring(QUEUE_SUFFIX.length()-1)) ;
					if(index < minIndex){
						minIndex = index ;
						tnodeName = nodeName ;
					}
				}
			}
			//target-node
			String targetNode = queueName+"/"+tnodeName ;
			//get-data
			byte[] datas = zkClient.getData(targetNode, watcher, stat);
			zkClient.delete(targetNode, ANY_VERSION );
			return new String(datas) ;
		}
		return null;
	}

}
