package ccode.mcsm.mcserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MinecraftServer {

	private String[] arguments;
	
	private Process serverProcess;
	private BufferedReader stdout;
	private BufferedReader stderr;
	private BufferedWriter stdin;
	
	public MinecraftServer(String... args) {
		arguments = args;
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
	public void sendCommand(String command) {
		try {
			stdin.write(command + "\n");
			stdin.flush();
		} catch (IOException e) {
			System.err.printf("Error writing command \'%s\' to server: %s\n", command, e.getMessage());
		}
	}
	
	public void sendCommands(String... commands) {
		for(String command : commands) {
			try {
				stdin.write(command + "\n");
			} catch (IOException e) {
				System.err.printf("Error writing command \'%s\' to server: %s\n", command, e.getMessage());
			}
		}
		try {
			stdin.flush();
		} catch (IOException e) {
			System.err.printf("Error flushing commands: %s\n", e.getMessage());
		}
	}
	
	// ==========================================
	// UTILITY METHODS FOR COMMON SERVER COMMANDS
	// ==========================================
	public void stop() {
		sendCommand("stop");
	}
	
	public void save() {
		sendCommand("save-all");
	}
	
	public void op(String player) {
		sendCommand("op " + player);
	}
	
	public void deop(String player) {
		sendCommand("deop" + player);
	}
	
}























