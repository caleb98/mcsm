package ccode.mcsm;

import java.io.IOException;

import com.esotericsoftware.kryonet.Server;

public class ServerStart {
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.out.println("Usage: mcremote.jar <server jarfile path>");
			System.exit(0);
		}
		
		Server server = KryoCreator.createServer();
		MinecraftServerManager listener = new MinecraftServerManager("java", "-Xms1024M", "-Xmx4096M", "-jar", args[0], "-nogui");
		
		try {
			server.addListener(listener);
			server.start();
			server.bind(44434, 44434);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
