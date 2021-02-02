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
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ccode.mcsm.action.Action;
import ccode.mcsm.backup.BackupManager;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.mcserver.event.EventListener;
import ccode.mcsm.mcserver.event.MinecraftServerEvent;
import ccode.mcsm.mcserver.event.PlayerAuthEvent;
import ccode.mcsm.net.message.ActionMessage;
import ccode.mcsm.net.message.ConnectMessage;
import ccode.mcsm.net.message.ErrorMessage;
import ccode.mcsm.net.message.ServerConnectSuccess;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;
import ccode.mcsm.scheduling.Scheduler;

public class MinecraftServerManager extends Listener {
	
	private static final String SCHEDULES_FILE = "mcsm_schedules.txt";
	private static final String PLAYERS_FILE = "mcsm_players.json";
	private static final String BACKUP_MANAGER_FILE = "mcsm_backup.json";
	private static final String BACKUP_DEFAULT_DIR = "mcsm_backup";
	private static final int BACKUP_DEFAULT_MAX = 10;
	
	public static final Player MCSM_EXECUTOR = new Player("MCSM-EXECUTOR", UUID.randomUUID().toString(), Permissions.MCSM_EXECUTOR);

	private File serverDirectory;
	private MinecraftServer server;
	private LinkedList<MinecraftServerEvent> eventQueue = new LinkedList<>();
	private ArrayList<EventListener> eventListeners = new ArrayList<>();
	private Object eventListenersLock = new Object();
	
	// uuid -> player
	private HashMap<String, Player> players = new HashMap<>();
	
	private BackupManager backupManager;
	
	//Data for remote connection
	private String password;
	private ArrayList<Connection> verified = new ArrayList<>();
	private HashMap<Connection, String> usernames = new HashMap<>();
	
	public MinecraftServerManager(String serverJar) {
		this.password = "password"; //TODO: set this up properly
		
		try {
			serverDirectory = new File(serverJar).getCanonicalFile().getParentFile();
		} catch (IOException e) {
			// TODO Handle this more gracefully
			System.err.printf("Error grabbing server directory: %s\n", e.getMessage());
			System.exit(-1);
		}
		
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
	
	@Override
	public void disconnected(Connection connection) {
		if(verified.contains(connection)) {
			System.out.printf("%s disconnected.\n", usernames.get(connection));
			verified.remove(connection);
			usernames.remove(connection);
		}
	}
	
	@Override
	public void received(Connection connection, Object object) {
		
		//Check for a new connection first
		if(object instanceof ConnectMessage) {
			
			//Check the password and add this connection to verified
			ConnectMessage m = (ConnectMessage) object;
			if(m.password.equals(password)) {
				verified.add(connection);
				usernames.put(connection, m.username);
				System.out.printf("%s (%s) connected!\n", m.username, connection.getRemoteAddressTCP().getAddress());
				connection.sendTCP(new ServerConnectSuccess());
			}
			//Otherwise, disconnect
			else {
				connection.sendTCP(new ErrorMessage("Incorrect Password"));
				connection.close();
			}
			
			return;
		}
		
		//Not a connect message, so check to make sure that the connection is verified
		if(!verified.contains(connection)) {
			System.out.printf("Warning! Received non-connection message from unverified connection %s.\n", connection.getRemoteAddressTCP().getAddress());
			connection.close();
			return;
		}
		
		//Connection is verified, so continue processing the message
		if(object instanceof ActionMessage) {
			ActionMessage message = (ActionMessage) object;
			//TODO: Executing messages from remote
			//Action.get(message.action).execute(this);
			
			connection.sendTCP(new ErrorMessage("REMOTE CURRENTLY DISABLED!"));
		}
		
	}
	
	public BackupManager getBackupManager() {
		return backupManager;
	}
	
	public MinecraftServer getServer() {
		return server;
	}
	
	public ArrayList<Connection> getVerifiedConnections() {
		return verified;
	}
	
	public HashMap<Connection, String> getUsernames() {
		return usernames;
	}
	
	private String serverDir(String dir) {
		return serverDirectory.getPath() + File .separator + dir;
	}
	
}
