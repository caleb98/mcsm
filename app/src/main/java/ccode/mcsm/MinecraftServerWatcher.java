package ccode.mcsm;

import java.io.BufferedReader;
import java.io.IOException;

import ccode.mcsm.mcserver.MinecraftServer;

public class MinecraftServerWatcher implements Runnable {

	private static String SERVER_WATCHER_THREAD = "Watcher-MinecraftServer";
	
	public static Thread create(MinecraftServer server) {
		return new Thread(new MinecraftServerWatcher(server), SERVER_WATCHER_THREAD);
	}
	
	private MinecraftServer server;
	
	private MinecraftServerWatcher(MinecraftServer server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		System.out.println("Starting server process thread...");
		
		BufferedReader std = server.stdout();
		BufferedReader err = server.stderr();
		
		try {
			
			long prev = System.currentTimeMillis();
			long current;
			long delta;
			
			while(server.isRunning()) {
				
				current = System.currentTimeMillis();
				delta = current - prev;
				
				String next;
				
				while(std.ready() && (next = std.readLine()) != null) {
					System.out.println("[MinecraftServer]: " + next);
				}
				
				while(err.ready() && (next = err.readLine()) != null) {
					System.err.println("[MinecraftServer]: " + next);
				}
				
				prev = current;
				
				Thread.sleep(1);
				
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Server process closed.");
	}
	
}
