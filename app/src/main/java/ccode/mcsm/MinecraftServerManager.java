package ccode.mcsm;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.net.message.ConnectMessage;
import ccode.mcsm.net.message.ErrorMessage;
import ccode.mcsm.net.message.InfoMessage;
import ccode.mcsm.net.message.SaveServer;
import ccode.mcsm.net.message.ServerConnectSuccess;
import ccode.mcsm.net.message.StartServer;
import ccode.mcsm.net.message.StopServer;

public class MinecraftServerManager extends Listener implements Runnable {

	private MinecraftServer server;
	
	private String password;
	
	private ArrayList<Connection> verified = new ArrayList<>();
	private HashMap<Connection, String> usernames = new HashMap<>();
	
	public MinecraftServerManager(String... arguments) {
		this.password = "password"; //TODO: set this up properly
		
		server = new MinecraftServer(arguments);
		
		//Start keyboard listening thread
		Thread keyboardListener = new Thread(()->{
			
			Scanner keyboard = new Scanner(System.in);
			String input;
			
			while(true)	 {
				
				input = keyboard.nextLine();
				
				if(input.equals("start")) {
					if(server.isRunning()) {
						System.out.println("[remote]: Server already started.");
					}
					else {
						try {
							server.start();
							new Thread(this, "MinecraftServer-Process-Watcher").start();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else if(input.equals("exit")) {
					if(server.isRunning()) {
						System.out.println("[remote]: Server not stopped. Please stop the server before closing the remote manager.");
					}
					else {
						break;
					}
				}
				else {
					if(server.isRunning()) {
						server.sendCommand(input);
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
				if(server.isRunning()) {
					server.save();
					server.sendCommand("say Server saved.");
					connection.sendTCP(new InfoMessage("Server saved."));
				}
				else {
					connection.sendTCP(new ErrorMessage("Can't save, server not started."));
				}
			}
			else if(object instanceof StartServer) {
				if(server.isRunning()) {
					connection.sendTCP(new ErrorMessage("Server already started."));
				}
				else {
					server.start();
					connection.sendTCP(new InfoMessage("Server started."));
				}
			}
			else if(object instanceof StopServer) {
				if(server.isRunning()) {
					server.stop();
					connection.sendTCP(new InfoMessage("Server stopped."));
				}
				else {
					connection.sendTCP(new ErrorMessage("Server already stopped."));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
