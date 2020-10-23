package ccode.mcsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import ccode.mcsm.action.Action;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.net.message.ActionMessage;
import ccode.mcsm.net.message.ConnectMessage;
import ccode.mcsm.net.message.ErrorMessage;
import ccode.mcsm.net.message.InfoMessage;
import ccode.mcsm.net.message.ServerConnectSuccess;

public class MinecraftServerManager extends Listener {

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
			String command;
			String args;
			
			while(true)	 {
				
				command = keyboard.next();
				args = keyboard.nextLine().trim();
				
				boolean handled = false;
				for(String action : Action.getActions()) {
					if(command.equals(action)) {
						Action.get(action).execute(this, args);
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
		if(object instanceof ActionMessage) {
			ActionMessage message = (ActionMessage) object;
			Action.get(message.action).execute(this);
			connection.sendTCP(new InfoMessage("Executed " + message.action));
		}
		
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
	
}
