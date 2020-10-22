package ccode.mcsm;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import ccode.mcsm.net.ActiveSignal;
import ccode.mcsm.net.ConnectMessage;
import ccode.mcsm.net.ErrorMessage;
import ccode.mcsm.net.InfoMessage;
import ccode.mcsm.net.SaveServer;
import ccode.mcsm.net.ServerConnectSuccess;
import ccode.mcsm.net.StartServer;
import ccode.mcsm.net.StopServer;

public class KryoCreator {

	public static Server createServer() {
		Server s = new Server();
		registerKryo(s.getKryo());
		return s;
	}
	
	public static Client createClient() {
		Client c = new Client();
		registerKryo(c.getKryo());
		return c;
	}
	
	public static void registerKryo(Kryo kryo) {
		
		kryo.register(ActiveSignal.class);
		kryo.register(ConnectMessage.class);
		kryo.register(ErrorMessage.class);
		kryo.register(InfoMessage.class);
		kryo.register(SaveServer.class);
		kryo.register(ServerConnectSuccess.class);
		kryo.register(StartServer.class);
		kryo.register(StopServer.class);
		
	}
	
}
