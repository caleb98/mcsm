package ccode.mcsm.net;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import ccode.mcsm.net.message.ActionMessage;
import ccode.mcsm.net.message.ConnectMessage;
import ccode.mcsm.net.message.ErrorMessage;
import ccode.mcsm.net.message.InfoMessage;
import ccode.mcsm.net.message.ServerConnectSuccess;
import ccode.mcsm.net.message.TaskMessage;

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
		
		kryo.register(ConnectMessage.class);
		kryo.register(ErrorMessage.class);
		kryo.register(InfoMessage.class);
		kryo.register(ServerConnectSuccess.class);
		kryo.register(ActionMessage.class);
		kryo.register(TaskMessage.class);
		
	}
	
}
