package ccode.mcsm.mcserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.action.Action;
import ccode.mcsm.mcserver.event.EventProducer;
import ccode.mcsm.mcserver.event.MinecraftServerEvent;
import ccode.mcsm.mcserver.event.ServerStartedEvent;
import ccode.mcsm.mcserver.event.ServerStoppedEvent;
import ccode.mcsm.permissions.Player;

public class MinecraftServer implements Runnable {
	
	private static final Pattern MSCM_COMMAND_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}\\] \\[Server thread\\/INFO\\]: <(\\w+)> mcsm (.+)$");
	
	private File serverDirectory;
	private MinecraftServerManager manager;
	private String[] arguments;

	private Properties properties = new Properties();
	private boolean arePropsLoaded = false;
	
	private Process serverProcess;
	private BufferedReader stdout;
	private BufferedReader stderr;
	private BufferedWriter stdin;
	
	public MinecraftServer(MinecraftServerManager manager, File serverDir, String... args) {
		this.manager = manager;
		serverDirectory = serverDir;
		arguments = args;
		
		loadProperties();
	}
	
	public void loadProperties() {
		try {
			properties.load(new FileReader(serverDir("server.properties")));
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
		properties.store(new FileWriter(serverDir("server.properties")), null);
	}
	
	public void start() throws IOException {
		
		//Create the process
		ProcessBuilder pb = new ProcessBuilder(arguments);
		pb.directory(serverDirectory);
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
		manager.addEvent(new ServerStartedEvent(DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now())));
		System.out.println("Starting server process thread...");
		
		try {
			
			while(isRunning()) {
				
				String next;
				
				while(stdout.ready() && (next = stdout.readLine()) != null) {
					System.out.println(next);
					checkLineForEvent(next);
					checkLineForCommand(next);
				}
				
				while(stderr.ready() && (next = stderr.readLine()) != null) {
					System.err.println(next);
					checkLineForEvent(next);
				}
				
				Thread.sleep(1);
				
			}
			
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
		
		manager.addEvent(new ServerStoppedEvent(DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now())));
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
		
		String playerName = m.group(1);
		String command = m.group(2).trim();
		
		Player player = manager.getPlayerFromName(playerName);
		
		//Check if the given string matches a command
		m = Action.ACTION_COMMAND_PATTERN.matcher(command);
		if(!m.matches()) {
			try {
				sendCommand(String.format("tell %s \'%s\' is not a valid mcsm action string", playerName, command));
			} catch (IOException e) {}
			return;
		}
		
		String actionID = m.group(1);
		String args = m.group(2);
		if(args == null) args = "";
		
		//Make sure that the provided action exists
		Action action = Action.get(actionID);
		if(action == null) {
			try {
				sendCommand(String.format("tell %s \'%s\' is not a valid mcsm action", playerName, actionID));
			} catch (IOException e) {}
			return;
		}
		
		//Make sure that the user has the permission to execute this action
		if(!player.hasPermissions(action)) {
			try {
				sendCommand(String.format("tell %s you don't have permission for that", playerName));
			} catch (IOException e) {}
			return;
		}
		
		Action.runAsync(actionID, manager, player, args);
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
	
	private String serverDir(String dir) {
		return serverDirectory.getPath() + File.separator + dir;
	}
	
}























