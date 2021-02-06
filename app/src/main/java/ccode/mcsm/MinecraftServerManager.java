package ccode.mcsm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.UUID;

import com.esotericsoftware.jsonbeans.JsonException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ccode.mcsm.action.Action;
import ccode.mcsm.backup.BackupManager;
import ccode.mcsm.backup.BackupPolicy;
import ccode.mcsm.backup.NoLimitPolicy;
import ccode.mcsm.backup.StandardBackupPolicy;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.mcserver.event.EventListener;
import ccode.mcsm.mcserver.event.MinecraftServerEvent;
import ccode.mcsm.mcserver.event.PlayerAuthEvent;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;
import ccode.mcsm.scheduling.Scheduler;

public class MinecraftServerManager  {
	
	private static final String SCHEDULES_FILE = "mcsm_schedules.txt";
	private static final String PLAYERS_FILE = "mcsm_players.json";
	private static final String BACKUP_MANAGER_FILE = "mcsm_backup.json";
	private static final String BACKUP_DEFAULT_DIR = "mcsm_backup";
	private static final int BACKUP_DEFAULT_MAX = -1; //No limit
	
	public static final Player MCSM_EXECUTOR = new Player("MCSM-EXECUTOR", UUID.randomUUID().toString(), Permissions.MCSM_EXECUTOR);

	private File serverDirectory;
	private String serverJar;
	private MinecraftServer server;
	private BackupManager backupManager;
	
	private LinkedList<MinecraftServerEvent> eventQueue = new LinkedList<>();
	private ArrayList<EventListener> eventListeners = new ArrayList<>();
	private Object eventListenersLock = new Object();
	
	// uuid -> player
	private HashMap<String, Player> players = new HashMap<>();
	
	public MinecraftServerManager(String serverJar) {
		this.serverJar = serverJar;
		
		try {
			serverDirectory = new File(serverJar).getCanonicalFile().getParentFile();
		} catch (IOException e) {
			// TODO Handle this more gracefully
			System.err.printf("Error grabbing server directory: %s\n", e.getMessage());
			System.exit(-1);
		}		
	}
	
	public void start() {
		server = new MinecraftServer(this, serverDirectory, "java", "-Xms1024M", "-Xmx4096M", "-jar", serverJar, "-nogui");
		
		Scheduler.loadSchedules(this, new File(SCHEDULES_FILE));
		loadBackupManager();
		loadPlayers();
		
		//Start keyboard listening thread
		Thread inputProcessor = new Thread(()->{
			
			Scanner keyboard = new Scanner(System.in);
			String command;
			String args;
			
			while(true) {
				
				command = keyboard.next();
				args = keyboard.nextLine().trim();
				
				boolean handled = false;
				for(String action : Action.getActions()) {
					if(command.equals(action)) {
						Action.runAsync(action, this, MCSM_EXECUTOR, args);
						handled = true;
						break;
					}
				}
				
				if(command.equals("exit")) {
					if(server.isRunning()) {
						System.out.println("Server not stopped. Please stop the server before closing the remote manager.");
					}
					else {
						break;
					}
				}
				else if(!handled) {
					System.out.println("Unrecognized action: " + command);
				}
				
			}
			
			keyboard.close();
			System.out.println("Exiting...");
			exit(0);
			
		}, "MCSM-Main-Thread");
		
		Thread eventProcessor = new Thread(()->{
			while(true) {
				try {
					
					while(eventQueue.peek() != null) {
						MinecraftServerEvent event = eventQueue.remove();
						synchronized(eventListenersLock) {
							Iterator<EventListener> iter = eventListeners.iterator();
							while(iter.hasNext()) {
								if(iter.next().process(event)) {
									iter.remove();
								}
							}
						}
					}
					
					Thread.sleep(1);
					
				} catch (InterruptedException e) {
					break;
				}
			}
		}, "Event-Processor-Thread");
		
		inputProcessor.start();
		eventProcessor.setDaemon(true);
		eventProcessor.start();
		
		//Listener for adding new players on connect.
		addListener((event)->{
			if(event instanceof PlayerAuthEvent) {
				PlayerAuthEvent auth = (PlayerAuthEvent) event;
				if(!players.containsKey(auth.uuid)) {
					players.put(auth.uuid, new Player(auth.player, auth.uuid, Permissions.NO_PERMISSIONS));
				}
			}
			return false;
		});
	}
	
	private void exit(int code) {
		saveBackupManager();
		savePlayers();
		System.exit(code);
	}
	
	private void loadBackupManager() {
		try (
				BufferedReader backupReader = new BufferedReader(new FileReader(BACKUP_MANAGER_FILE));
		) {
			backupManager = Json.fromJson(backupReader, BackupManager.class);
			backupManager.setServerDir(serverDirectory);
			backupReader.close();
			
			//Link backup manager to policies; add NoLimitPolicy for worlds which had errors parsing
			HashMap<String, BackupPolicy> policies = backupManager.getPolicies();
			for(String world : policies.keySet()) {
				BackupPolicy policy = policies.get(world);
				if(policy == null) {
					policies.put(world, new NoLimitPolicy(backupManager));
				}
				else if(policy instanceof StandardBackupPolicy) {
					((StandardBackupPolicy) policy).setBackupManager(backupManager);
				}
			}
		} catch (IOException e) {
			System.err.printf("Error reading backup manager file: %s\n", e.getMessage());
			backupManager = new BackupManager(serverDirectory, BACKUP_DEFAULT_DIR, BACKUP_DEFAULT_MAX);
		}
	}
	
	private void saveBackupManager() {
		try (
				BufferedWriter backupWriter = new BufferedWriter(new FileWriter(BACKUP_MANAGER_FILE));
		) {
			Gson gson = Json.newBuilder().setPrettyPrinting().create();
			gson.toJson(backupManager, backupManager.getClass(), backupWriter);
			backupWriter.close();
		} catch (IOException e) {
			System.err.printf("Error saving backup manager file: %s\n", e.getMessage());
		}
	}
	
	private void savePlayers() {
		try (
				BufferedWriter playerWriter = new BufferedWriter(new FileWriter(PLAYERS_FILE));
		) {
			Gson gson = Json.newBuilder().setPrettyPrinting().create();
			gson.toJson(players, players.getClass(), playerWriter);
			playerWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadPlayers() {
		try (
				BufferedReader playerReader = new BufferedReader(new FileReader(PLAYERS_FILE));
		) {
			Type hashMapType = new TypeToken<HashMap<String, Player>>(){}.getType();
			players = Json.fromJson(playerReader, hashMapType);
			
			//Move in the uuids
			for(String uuid : players.keySet()) {
				players.get(uuid).setUuid(uuid);
			}
		} catch(FileNotFoundException e) {
			//ignore, we'll make the file later anyway
		} catch (IOException e) {
			System.err.println("Error reading mcsm players file.");
			e.printStackTrace();
		}
		
		//Try to load players from the usercache
		try (
				BufferedReader usercacheReader = new BufferedReader(new FileReader(serverDir("usercache.json")));
		) {
			JsonArray usercache = Json.fromJson(usercacheReader, JsonArray.class);
			for(int i = 0; i < usercache.size(); ++i) {
				JsonObject user = usercache.get(i).getAsJsonObject();
				String uuid = user.get("uuid").getAsString();
				
				//If player isn't already registered, add them now
				if(!players.containsKey(uuid)) {
					Player p = new Player(user.get("name").getAsString(), uuid, Permissions.NO_PERMISSIONS);
					players.put(uuid, p);
				}
			}
		} catch (IOException e) {
			System.err.printf("Warning: unable to load players from usercache: %s\n", e.getMessage());
		} catch (JsonException jse) {
			System.err.printf("Warning: unable to load players from usercache: %s\n", jse.getMessage());
		}
	}
	
	public Player getPlayerFromName(String playerName) {
		for(String uuid : players.keySet()) {
			Player player = players.get(uuid);
			if(player.getName().equals(playerName)) {
				return player;
			}
		}
		return null;
	}
	
	public Player getPlayerFromUUID(String uuid) {
		return players.get(uuid);
	}
	
	public File getServerDirectory() {
		return serverDirectory;
	}
	
	public void addEvent(MinecraftServerEvent event) {
		eventQueue.add(event);
	}
	
	public void addListener(EventListener listener) {
		synchronized(eventListenersLock) {
			eventListeners.add(listener);
		}
	}
	
	public BackupManager getBackupManager() {
		return backupManager;
	}
	
	public MinecraftServer getServer() {
		return server;
	}
	
	private String serverDir(String dir) {
		return serverDirectory.getPath() + File .separator + dir;
	}
	
}
