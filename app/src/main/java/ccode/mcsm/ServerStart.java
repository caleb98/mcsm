package ccode.mcsm;

import java.io.IOException;

import com.esotericsoftware.kryonet.Server;

public class ServerStart {
	public static void main(String[] args) {
		
		if(args.length != 2) {
			System.out.println("Usage: mcremote.jar <password> <server jarfile path>");
			System.exit(0);
		}
		
		Server server = KryoCreator.createServer();
		MCRemoteListener listener = new MCRemoteListener(server, args[0], "java", "-Xms1024M", "-Xmx4096M", "-jar", args[1], "-nogui");
		
		try {
			server.addListener(listener);
			server.start();
			server.bind(44434, 44434);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Keep the main thread alive while the server is still active.
		while(server.getUpdateThread().isAlive()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
		}
		
	}
}
