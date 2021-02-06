package ccode.mcsm.client;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import ccode.mcsm.Json;
import ccode.mcsm.net.KryoCreator;
import ccode.mcsm.net.message.NetErrorMessage;
import ccode.mcsm.net.message.NetLoginSuccessMessage;
import ccode.mcsm.net.message.NetMinecraftChatMessage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Remote extends Listener {

	private Client client;
	private boolean isConnecting = false;
	
	private ConnectStage connectStage;
	private RemoteStage remoteStage;
	
	public Remote() {
		client = KryoCreator.createClient();
		client.addListener(this);
		client.start();
	}
	
	public boolean connect(String host, int port, ConnectStage connectStage) {
		this.connectStage = connectStage;
		
		try {
			isConnecting = true;
			client.connect(5000, host, port, port);
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(e.getLocalizedMessage());
			alert.show();
			isConnecting = false;
			return false;
		}
		
		isConnecting = false;
		return true;
	}
	
	public boolean isConnecting() {
		return isConnecting;
	}
	
	public boolean isConnected() {
		return client.isConnected();
	}
	
	public void sendTCP(Object data) {
		client.sendTCP(data);
	}
	
	public void sendUDP(Object data) {
		client.sendUDP(data);
	}
	
	@Override
	public void connected(Connection connection) {
		
	}
	
	@Override
	public void disconnected(Connection connection) {
		
	}
	
	@Override
	public void received(Connection connection, Object object) {
		
		//TODO: remove for release
		String json = Json.toJson(object);
		System.out.println(json);
		
		if(object instanceof NetErrorMessage) {
			NetErrorMessage error = (NetErrorMessage) object;
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText(error.topic);
				alert.setContentText(error.message);
				alert.show();
			});
		}
		else if(object instanceof NetLoginSuccessMessage) {
			Platform.runLater(()->{
				remoteStage = new RemoteStage(this);
				remoteStage.show();
				
				connectStage.close();
				connectStage = null;
			});
		}
		else if(object instanceof NetMinecraftChatMessage) {
			Platform.runLater(()->{
				NetMinecraftChatMessage message = (NetMinecraftChatMessage) object;
				remoteStage.addChatMessage(message);
			});
		}
		
	}
	
}
