package cn.kane.test;

import java.io.IOException;
import java.util.Random;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import cn.kane.impl.FIFOQueue;
import cn.kane.service.IFIFOQueue;
import junit.framework.TestCase;

public class FIFOQueueTest extends TestCase {

	private IFIFOQueue queue ;
	private String connStr = "localhost:2181";
	private int sessionTimeout = 3000;
    private	Watcher watcher;
	private String queueName = "/kane";
	
	@Override
	public void setUp() throws IOException{
		this.queue = FIFOQueue.getInstance(connStr, sessionTimeout, watcher) ;
		watcher = new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				System.out.println(String.format("Event:::Path[%s],Type[%s],State[%s]", event.getPath(),event.getType().name(),event.getState().name()));
			}
		};
	}
	
	public void testPut() throws KeeperException, InterruptedException{
		String value = "1";
		queue.put(value, queueName);
	}
	
	public void testTake() throws KeeperException, InterruptedException{
		String value = queue.take(queueName, watcher, null) ;
		System.out.println(value);
	}
	
	public void testMulti(){
		//producer
		new Thread(new Runnable() {
			@Override
			public void run() {
				Random random = new Random() ;
				for(int i = 1 ; i <100;i++){
					try {
						queue.put(i+"", queueName);
						Thread.sleep(random.nextInt(1000));
					} catch (KeeperException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		//consumer
		new Thread(new Runnable() {
			@Override
			public void run() {
				Random random = new Random() ;
				for(int i = 1 ; i <100;i++){
					try {
						String value = queue.take(queueName,watcher,null);
						System.out.println(value);
						Thread.sleep(random.nextInt(1000));
					} catch (KeeperException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
}
