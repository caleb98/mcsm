package ccode.mcsm.net;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import ccode.mcsm.net.message.NetDoActionMessage;
import ccode.mcsm.net.message.NetErrorMessage;
import ccode.mcsm.net.message.NetExecutionMessage;
import ccode.mcsm.net.message.NetLoginMessage;
import ccode.mcsm.net.message.NetLoginSuccessMessage;
import ccode.mcsm.net.message.NetMinecraftChatMessage;

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
		
		kryo.register(byte[].class);
		
		kryo.register(NetDoActionMessage.class);
		kryo.register(NetErrorMessage.class);
		kryo.register(NetExecutionMessage.class);
		kryo.register(NetLoginMessage.class);
		kryo.register(NetLoginSuccessMessage.class);
		kryo.register(NetMinecraftChatMessage.class);
		
	}
	
}
