package cn.kane.test;

import java.io.IOException;
import java.util.Random;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import cn.kane.impl.LeadElection;
import cn.kane.service.ILeadElection;
import junit.framework.TestCase;

public class LeadElectionTest extends TestCase {

	private ILeadElection leadElection ;
	private String nodeName = "/leadelection";
	private int sessionTimeOut = 3000 ;
	private String conn = "127.0.0.1:2181";
	private Watcher watcher;
	private Random random = new Random() ;
	
	@Override
	public void setUp() throws IOException, KeeperException, InterruptedException{
		watcher = new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				System.out.println(String.format("Event:::Path[%s],Type[%s],State[%s]", event.getPath(),event.getType().name(),event.getState().name()));
			}
		};
		leadElection = new LeadElection(nodeName, sessionTimeOut, conn, watcher) ;
	}
	
	public void testLeaderElection() throws InterruptedException{
		for(int i=1 ; i<=10 ; i++){
			final int temp = i ;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(random.nextInt(500));
						int count = 20 ;
						while(count>0){
							leadElection.getLeadSessionId() ;
							if(temp == 1 && count%5==0){
								leadElection.disLeader();
							}
							Thread.sleep(random.nextInt(1000));
							count -- ;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (KeeperException e) {
						e.printStackTrace();
					}
				}
			},"THD-"+i).start();
		}
		
		Thread.sleep(30000);
	}
	
}
