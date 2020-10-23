package ccode.mcsm;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.mcserver.event.AchievementGetEvent;
import ccode.mcsm.mcserver.event.EventProducer;
import ccode.mcsm.mcserver.event.MinecraftServerEvent;
import ccode.mcsm.mcserver.event.MinecraftServerEventListener;
import ccode.mcsm.mcserver.event.PlayerJoinedEvent;
import ccode.mcsm.mcserver.event.PlayerLeftEvent;
import ccode.mcsm.mcserver.event.ServerLoadedEvent;
import ccode.mcsm.mcserver.event.ServerUnloadedEvent;

public class MinecraftServerWatcher implements Runnable {

	private static String SERVER_WATCHER_THREAD = "Watcher-MinecraftServer";
	
	public static Thread create(MinecraftServer server) {
		return new Thread(new MinecraftServerWatcher(server), SERVER_WATCHER_THREAD);
	}
	
	private MinecraftServer server;
	private ArrayList<MinecraftServerEventListener> listeners = new ArrayList<>();
	private MinecraftServerEventListener testListener;
	
	private MinecraftServerWatcher(MinecraftServer server) {
		this.server = server;
		
		testListener = new MinecraftServerEventListener((event)->{
			if(event instanceof PlayerJoinedEvent) {
				PlayerJoinedEvent join = (PlayerJoinedEvent) event;
				System.out.printf("EVENT: Player %s joined (%s)\n", join.playerName, join.timestamp);
			}
			else if(event instanceof AchievementGetEvent) {
				AchievementGetEvent achieve = (AchievementGetEvent) event;
				System.out.printf("EVENT: Player %s got achievement %s (%s)\n", achieve.playerName, achieve.achievement, achieve.timestamp);
			}
			else if(event instanceof ServerLoadedEvent) {
				ServerLoadedEvent load = (ServerLoadedEvent) event;
				System.out.printf("EVENT: Server loaded in %ss (%s)\n", load.startupTime, load.timestamp);
			}
			else if(event instanceof ServerUnloadedEvent) {
				ServerUnloadedEvent unload = (ServerUnloadedEvent) event;
				System.out.printf("EVENT: Server unloaded (%s)\n", unload.timestamp);
			}
			else if(event instanceof PlayerLeftEvent) {
				PlayerLeftEvent leave = (PlayerLeftEvent) event;
				System.out.printf("EVENT: Player %s left (%s)\n", leave.playerName, leave.timestamp);
			}
		});
		listeners.add(testListener);
	}
	
	public void addListener(MinecraftServerEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(MinecraftServerEventListener listener) {
		listeners.remove(listener);
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
					for(EventProducer producer : MinecraftServerEvent.getEvents()) {
						MinecraftServerEvent event = producer.produce(next); 
						if(event != null) {
							for(MinecraftServerEventListener listener : listeners) {
								listener.addEvent(event);
							}
						}
					}
				}
				
				while(err.ready() && (next = err.readLine()) != null) {
					System.err.println("[MinecraftServer]: " + next);
				}
				
				testListener.processEvents();
				
				prev = current;
				
				Thread.sleep(1);
				
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Server process closed.");
	}
	
	private void checkLineForEvent() {
		
	}
	
}
