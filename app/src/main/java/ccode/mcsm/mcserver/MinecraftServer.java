package ccode.mcsm.mcserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

public class MinecraftServer {

	private String[] arguments;

	private Properties properties = new Properties();
	private boolean arePropsLoaded = false;
	
	private Process serverProcess;
	private BufferedReader stdout;
	private BufferedReader stderr;
	private BufferedWriter stdin;
	
	public MinecraftServer(String... args) {
		arguments = args;
		
		try {
			properties.load(new FileReader("server.properties"));
			arePropsLoaded = true;
		} catch (IOException e) {
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























