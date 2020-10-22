package ccode.mcsm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import ccode.mcsm.net.ActiveSignal;
import ccode.mcsm.net.ConnectMessage;
import ccode.mcsm.net.ErrorMessage;
import ccode.mcsm.net.InfoMessage;
import ccode.mcsm.net.SaveServer;
import ccode.mcsm.net.ServerConnectSuccess;
import ccode.mcsm.net.StartServer;
import ccode.mcsm.net.StopServer;

public class MCRemoteListener extends Listener implements Runnable {

	private static long ONE_HOUR = 1000 * 60 * 60;
	private static long THIRTY_MINUTES = 1000 * 60 * 30;
	private static long TEN_MINUTES = 1000 * 60 * 10;
	private static long FIVE_MINUTES = 1000 * 60 * 5;
	private static long ONE_MINUTE = 1000 * 60;
	private static long THIRTY_SECONDS = 1000 * 30;
	
	private String[] cmd;
	private String password;
	
	private Process serverProcess;
	private BufferedReader std;
	private BufferedReader err;
	private PrintWriter out;
	
	private ArrayList<Connection> verified = new ArrayList<>();
	private HashMap<Connection, String> usernames = new HashMap<>();
	
	private long alarmCount;
	private long alarmTime;
	private String alarmMessage;
	private boolean isAlarmActive;
	
	public MCRemoteListener(Server server, String password, String... cmd) {
		this.cmd = cmd;
		this.password = password;
		
		//Start keyboard listening thread
		Thread keyboardListener = new Thread(()->{
			
			Scanner keyboard = new Scanner(System.in);
			String input;
			
			while(true)	 {
				
				input = keyboard.nextLine();
				
				if(input.equals("start")) {
					if(isServerActive()) {
						System.out.println("[remote]: Server already started.");
					}
					else {
						try {
							startServer();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else if(input.equals("togglealarm")) {
					if(isAlarmActive) {
						System.out.println("[remote]: Server timout turned off. Server will remain on until manually stopped, or until timeout is reenabled.");
						issueServerCommand("say Server timout turned off. Server will remain on until manually stopped, or until timeout is reenabled.");
						isAlarmActive = false;
					}
					else {
						System.out.println("Server timeout turned on. Server will close in one hour if no active signals are received.");
						issueServerCommand("say Server timeout turned on. Server will close in one hour if no active signals are received.");
						resetAlarm();
						isAlarmActive = true;
					}
				}
				else if(input.equals("exit")) {
					if(isServerActive()) {
						System.out.println("[remote]: Server not stopped. Please stop the server before closing the remote manager.");
					}
					else {
						break;
					}
				}
				else {
					if(isServerActive()) {
						issueServerCommand(input);
					}
					else {
						System.out.println("[remote]: Unable to issue server commands. Please start server first.");
					}
				}
				
			}
			
			keyboard.close();
			System.out.println("[remote]: Exiting...");
			System.exit(0);
			
		}, "Keyboard-Listener-Thread");
		
		keyboardListener.start();
		
	}
	
	public void startServer() throws IOException {
		
		//Start process
		ProcessBuilder pb = new ProcessBuilder(cmd);
		
		serverProcess = pb.start();
		std = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
		err = new BufferedReader(new InputStreamReader(serverProcess.getErrorStream()));
		out = new PrintWriter(serverProcess.getOutputStream());
		
		//Setup alarm
		resetAlarm();
		
		Thread t = new Thread(this);
		t.start();
		
	}
	
	public boolean isServerActive() {
		return serverProcess != null && serverProcess.isAlive();
	}
	
	public void issueServerCommand(String command) {
		out.println(command);
		out.flush();
	}
	
	private void resetAlarm() {
		isAlarmActive = true;
		alarmTime = THIRTY_MINUTES;
		alarmCount = THIRTY_MINUTES;
		alarmMessage = "The server will close in 30 minutes. To keep the server active, send an active signal with your remote.";
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
		try {
			
			if(object instanceof SaveServer) {
				if(isServerActive()) {
					issueServerCommand("save-all");
					issueServerCommand("say Server saved.");
					connection.sendTCP(new InfoMessage("Server saved."));
				}
				else {
					connection.sendTCP(new ErrorMessage("Can't save, server not started."));
				}
			}
			else if(object instanceof StartServer) {
				if(isServerActive()) {
					connection.sendTCP(new ErrorMessage("Server already started."));
				}
				else {
					startServer();
					connection.sendTCP(new InfoMessage("Server started."));
				}
			}
			else if(object instanceof StopServer) {
				if(isServerActive()) {
					issueServerCommand("stop");
					connection.sendTCP(new InfoMessage("Server stopped."));
				}
				else {
					connection.sendTCP(new ErrorMessage("Server already stopped."));
				}
			}
			else if(object instanceof ActiveSignal) {
				if(isServerActive() && isAlarmActive) {
					resetAlarm();
					connection.sendTCP(new InfoMessage("Active signal received."));
					issueServerCommand("say Active signal received. Server will close in one hour if no other active signals are received.");
				}
				else if(isServerActive() && !isAlarmActive) {
					connection.sendTCP(new ErrorMessage("Server timeout not enabled. Active signals are not necessary until timeout is reenabled."));
				}
				else {
					connection.sendTCP(new ErrorMessage("Server must be online to send active signal."));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void run() {
		System.out.println("Starting server process thread...");
		
		try {
			
			long prev = System.currentTimeMillis();
			long current;
			long delta;
			
			while(serverProcess.isAlive()) {
				
				current = System.currentTimeMillis();
				delta = current - prev;
				
				String next;
				
				while(std.ready() && (next = std.readLine()) != null) {
					System.out.println("[server]: " + next);
				}
				
				while(err.ready() && (next = err.readLine()) != null) {
					System.err.println("[server]: " + next);
				}
				
				if(isAlarmActive) {
					
					alarmCount -= delta;
					
					if(alarmCount < 0) {
						issueServerCommand("say " + alarmMessage);
						for(Connection c : verified) {
							c.sendTCP(new InfoMessage(alarmMessage));
						}
						
						if(alarmTime == THIRTY_MINUTES) {
							alarmTime = TEN_MINUTES;
							alarmCount = 1000 * 60 * 20; //Next alarm should sound in 20 mins
							alarmMessage = "The server will close in 10 minutes. To keep the server active, send an active signal with your remote.";
						}
						else if(alarmTime == TEN_MINUTES) {
							alarmTime = FIVE_MINUTES;
							alarmCount = 1000 * 60 * 5; //Next alarm should sound in 5 mins
							alarmMessage= "The server will close in 5 minutes. To keep the server active, send an active signal with your remote.";
						}
						else if(alarmTime == FIVE_MINUTES) {
							alarmTime = ONE_MINUTE;
							alarmCount = 1000 * 60 * 4; //Next alarm should sound in 4 mins
							alarmMessage = "The server will close in 1 minute. To keeep the server active, send an active signal with your remote.";
						}
						else if(alarmTime == ONE_MINUTE) {
							alarmTime = THIRTY_SECONDS;
							alarmCount = 1000 * 30; //Next alarm should sound in 30 sec
							alarmMessage = "The server will close in 30 seconds. To keep the server active, send an active signal with your remote.";
						}
						else if(alarmTime == THIRTY_SECONDS) {
							//If prev alarm total was 30 seconds, this is for every second under 15
							alarmTime = 1000 * 15; 
							alarmCount = 1000;
							alarmMessage = "Server closing in 15 seconds...";
						}
						else if(alarmTime < THIRTY_SECONDS && alarmTime > 0) {
							alarmTime -= 1000;
							alarmCount = 1000;
							if(alarmTime == 1000) {
								alarmMessage = "Server closing in 1 second...";
							}
							else {
								alarmMessage = String.format("Server closing in %s seconds...", alarmTime / 1000);
							}
						}
						else if(alarmTime <= 0) {
							issueServerCommand("stop");
							isAlarmActive = false;
						}
						
					}
					
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
