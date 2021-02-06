package ccode.mcsm;

public class Block {

	private boolean block = true;
	
	public synchronized void unblock() {
		block = false;
	}
	
	public synchronized boolean blocking() {
		return block;
	}
	
}
