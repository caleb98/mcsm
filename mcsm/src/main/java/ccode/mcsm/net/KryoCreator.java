package ccode.mcsm.net;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

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
		
		
		
	}
	
}
