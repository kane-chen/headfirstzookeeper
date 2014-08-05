package cn.kane.test;

import java.io.IOException;
import java.util.Random;

import org.apache.zookeeper.KeeperException;

import cn.kane.impl.Lock;
import cn.kane.service.ILock;
import cn.kane.service.vo.AsyncResp;
import junit.framework.TestCase;

public class LockTest extends TestCase {

	private ILock lock ;
	private String connStr = "127.0.0.1:2181";
	private int sessionTimeOut = 3000;
	private String nodeName = "/distlocks";
	final Random random = new Random() ;
	
	@Override
	public void setUp() throws IOException, KeeperException, InterruptedException{
		lock = new Lock(connStr,sessionTimeOut,nodeName);
	}
	
	public void testSyncLock(){
		for(int i=1 ; i<= 10 ; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(random.nextInt(2000));
						System.out.println(String.format("%s try to lock", Thread.currentThread().getName()));
						String lockName = lock.getLockSync();
						System.out.println(String.format("%s got lock", Thread.currentThread().getName()));
						System.out.println(String.format("%s execute with lock", Thread.currentThread().getName()));
						Thread.sleep(random.nextInt(5000));
						lock.releaseLock(lockName);
						System.out.println(String.format("%s release lock", Thread.currentThread().getName()));
					} catch (KeeperException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			},"THD-"+i).start();
		}
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void testAsyncLock(){
		for(int i=1 ; i<= 2 ; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(random.nextInt(2000));
						System.out.println(String.format("%s try to lock", Thread.currentThread().getName()));
						boolean gotLock =false ;
						String lockName = null ;
						while(!gotLock){
							AsyncResp resp = lock.getLockAsync(lockName) ;
							lockName = resp.getLockItemName() ;
							gotLock = resp.isGotLock() ;
							if(resp.isGotLock()){
								System.out.println(String.format("%s got lock", Thread.currentThread().getName()));
								System.out.println(String.format("%s execute with lock", Thread.currentThread().getName()));
								Thread.sleep(random.nextInt(5000));
								lock.releaseLock(lockName);
								System.out.println(String.format("%s release lock", Thread.currentThread().getName()));
							}else{
								Thread.sleep(1000);
								//try again
							}
						}
					} catch (KeeperException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			},"THD-"+i).start();
		}
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
