package cn.kane.service.vo;

import java.io.Serializable;

public class AsyncResp implements Serializable{

	/**
	 * UID
	 */
	private static final long serialVersionUID = -1901379701259102946L;
	/** true-gotLock,false-non */
	private boolean gotLock = false ;
	/** lock-item-name */
	private String lockItemName ;
	
	public boolean isGotLock() {
		return gotLock;
	}
	public void setGotLock(boolean gotLock) {
		this.gotLock = gotLock;
	}
	public String getLockItemName() {
		return lockItemName;
	}
	public void setLockItemName(String lockItemName) {
		this.lockItemName = lockItemName;
	}
	
}
