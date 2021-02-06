package ccode.mcsm.net;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;

public class ClientStart {
	public static void main(String[] args) {
		
		try {
			Client client = KryoCreator.createClient();
			client.start();
			client.connect(5000, "localhost", 36363);
			
			Thread.sleep(10000);
		} catch (IOException e) {
			System.err.printf("Error creating kryo client: %s\n", e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
