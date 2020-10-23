package ccode.mcsm;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.mcserver.event.EventProducer;
import ccode.mcsm.mcserver.event.MinecraftServerEvent;
import ccode.mcsm.mcserver.event.MinecraftServerEventListener;
import ccode.mcsm.mcserver.event.ServerStartedEvent;
import ccode.mcsm.mcserver.event.ServerStoppedEvent;
import ccode.mcsm.scheduling.Timestamp;

public class MinecraftServerWatcher implements Runnable {

	private static String SERVER_WATCHER_THREAD = "Watcher-MinecraftServer";
	
	public static Thread create(MinecraftServer server) {
		return new Thread(new MinecraftServerWatcher(server), SERVER_WATCHER_THREAD);
	}
	
	private MinecraftServer server;
	private ArrayList<MinecraftServerEventListener> listeners = new ArrayList<>();
	
	private MinecraftServerWatcher(MinecraftServer server) {
		this.server = server;
	}
	
	public void addListener(MinecraftServerEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(MinecraftServerEventListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void run() {
		fireEvent(new ServerStartedEvent(Timestamp.now()));
		System.out.println("Starting server process thread...");
		
		BufferedReader std = server.stdout();
		BufferedReader err = server.stderr();
		
		try {
			
			while(server.isRunning()) {
				
				String next;
				
				while(std.ready() && (next = std.readLine()) != null) {
					System.out.println("[MinecraftServer]: " + next);
					checkLineForEvent(next);
				}
				
				while(err.ready() && (next = err.readLine()) != null) {
					System.err.println("[MinecraftServer]: " + next);
					checkLineForEvent(next);
				}
				
				Thread.sleep(1);
				
			}
			
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
		
		fireEvent(new ServerStoppedEvent(Timestamp.now()));
		System.out.println("Server process closed.");
	}
	
	private void checkLineForEvent(String line) {
		for(EventProducer producer : MinecraftServerEvent.getEvents()) {
			MinecraftServerEvent event = producer.produce(line); 
			if(event != null) {
				fireEvent(event);
			}
		}
	}
	
	private void fireEvent(MinecraftServerEvent event) {
		for(MinecraftServerEventListener listener : listeners) {
			listener.addEvent(event);
		}
	}
	
}
