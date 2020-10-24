package ccode.mcsm.mcserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.action.Action;
import ccode.mcsm.mcserver.event.EventProducer;
import ccode.mcsm.mcserver.event.MinecraftServerEvent;
import ccode.mcsm.mcserver.event.ServerStartedEvent;
import ccode.mcsm.mcserver.event.ServerStoppedEvent;
import ccode.mcsm.scheduling.TimeFormatters;

public class MinecraftServer implements Runnable {
	
	private static final Pattern MSCM_COMMAND_PATTERN = Pattern.compile("\\[\\d{2}:\\d{2}:\\d{2}\\] \\[Server thread\\/INFO]: <(\\w+)> mcsm ([\\w ]+)");
	
	private MinecraftServerManager manager;
	private String[] arguments;

	private Properties properties = new Properties();
	private boolean arePropsLoaded = false;
	private JsonArray ops;
	private boolean areOpsLoaded = false;
	
	private Process serverProcess;
	private BufferedReader stdout;
	private BufferedReader stderr;
	private BufferedWriter stdin;
	
	public MinecraftServer(MinecraftServerManager manager, String... args) {
		this.manager = manager;
		arguments = args;
		
		loadProperties();
		loadOps();
	}
	
	public JsonArray getOps() {
		return ops;
	}
	
	public void loadOps() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("ops.json"));
			Gson gson = new Gson();
			ops = gson.fromJson(reader, JsonArray.class);
			areOpsLoaded = true;
		} catch (JsonIOException e) {
			System.err.println("Error loading ops file.");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("Ops file not found.");
		}
	}
	
	/**
	 * Returns the op level of a player as specified in the server's 
	 * <code>ops.json</code> file. If the ops file is not loaded, then
	 * this method will return -1 for all players.
	 * @param playerName
	 * @return player op level
	 */
	public int getOpLevel(String playerName) {
		if(!areOpsLoaded) 
			return -1;
		
		JsonObject op;
		String opName;
		
		for(int i = 0; i < ops.size(); i++) {
			
			op = ops.get(i).getAsJsonObject();
			if(op.has("name") && op.has("level")) {
				
				opName = op.get("name").getAsString();
				if(playerName.equals(opName))
					return op.get("level").getAsInt();
				
			}
			
		}
		
		return 0;
	}
	
	public boolean areOpsLoaded() {
		return areOpsLoaded;
	}
	
	public void loadProperties() {
		try {
			properties.load(new FileReader("server.properties"));
			arePropsLoaded = true;
		} catch (IOException e) {
			System.out.println("Error loading properties file.");
			e.printStackTrace();
		}
	}
	
	public boolean arePropsLoaded() {
		return arePropsLoaded;
	}
	
	public String getProperty(String property) {
		return properties.getProperty(property);
	}
	
	public void setProperty(String property, String value) throws IOException {
		properties.setProperty(property, value);
		properties.store(new FileWriter("server.properties"), null);
	}
	
	public void start() throws IOException {
		
		//Create the process
		ProcessBuilder pb = new ProcessBuilder(arguments);
		serverProcess = pb.start();
		
		//Redirect process streams
		InputStream stdoutStream = serverProcess.getInputStream();
		InputStream stderrStream = serverProcess.getErrorStream();
		OutputStream stdinStream = serverProcess.getOutputStream();
		
		stdout = new BufferedReader(new InputStreamReader(stdoutStream));
		stderr = new BufferedReader(new InputStreamReader(stderrStream));
		stdin = new BufferedWriter(new OutputStreamWriter(stdinStream));
		
		Thread serverMonitor = new Thread(this, "Minecraft-Server-Monitor");
		serverMonitor.start();
		
	}
	
	public BufferedReader stdout() {
		return stdout;
	}
	
	public BufferedReader stderr() {
		return stderr;
	}
	
	public BufferedWriter stdin() {
		return stdin;
	}
	
	public boolean isRunning() {
		return serverProcess != null && serverProcess.isAlive();
	}
	
	@Override
	public void run() {
		manager.addEvent(new ServerStartedEvent(TimeFormatters.now()));
		System.out.println("Starting server process thread...");
		
		try {
			
			while(isRunning()) {
				
				String next;
				
				while(stdout.ready() && (next = stdout.readLine()) != null) {
					System.out.println("[MinecraftServer]: " + next);
					checkLineForEvent(next);
					if(areOpsLoaded)
						checkLineForCommand(next);
				}
				
				while(stderr.ready() && (next = stderr.readLine()) != null) {
					System.err.println("[MinecraftServer]: " + next);
					checkLineForEvent(next);
				}
				
				Thread.sleep(1);
				
			}
			
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
		
		manager.addEvent(new ServerStoppedEvent(TimeFormatters.now()));
		System.out.println("Server process closed.");
	}
	
	private void checkLineForEvent(String line) {
		for(EventProducer producer : MinecraftServerEvent.getEvents()) {
			MinecraftServerEvent event = producer.produce(line); 
			if(event != null) {
				manager.addEvent(event);
			}
		}
	}
	
	private void checkLineForCommand(String line) {
		Matcher m = MSCM_COMMAND_PATTERN.matcher(line);
		if(!m.matches())
			return;
		
		String player = m.group(1);
		String command = m.group(2).trim();
		
		//For now, only run if the player has FULL server op permission
		if(getOpLevel(player) == 4) {
			m = Action.ACTION_COMMAND_PATTERN.matcher(command);
			if(!m.matches()) {
				try {
					sendCommand(String.format("tell %s \'%s\' is not a valid mcsm action string", player, command));
				} catch (IOException e) {}
				return;
			}
			
			String action = m.group(1);
			String args = m.group(2);
			if(args == null) args = "";
			
			Action a = Action.get(action);
			if(a == null) {
				try {
					sendCommand(String.format("tell %s \'%s\' is not a valid mcsm action", player, action));
				} catch (IOException e) {}
				return;
			}
			
			Action.get(action).execute(manager, args);
		}
		else {
			try {
				sendCommand(String.format("tell %s you do not have the permissions to run mcsm commands", player));
			} catch (IOException e) {}
		}
	}
	
	/**
	 * Sends a given command to the server process.
	 * @param command
	 */
	public void sendCommand(String command) throws IOException {
		stdin.write(command + "\n");
		stdin.flush();
	}
	
	public void sendCommands(String... commands) throws IOException {
		for(String command : commands) {
			stdin.write(command + "\n");
		}
		stdin.flush();
	}
	
	// ==========================================
	// UTILITY METHODS FOR COMMON SERVER COMMANDS
	// ==========================================
	public void stop() throws IOException {
		sendCommand("stop");
	}
	
	public void save() throws IOException {
		sendCommand("save-all");
	}
	
	public void op(String player) throws IOException {
		sendCommand("op " + player);
	}
	
	public void deop(String player) throws IOException {
		sendCommand("deop" + player);
	}
	
}























