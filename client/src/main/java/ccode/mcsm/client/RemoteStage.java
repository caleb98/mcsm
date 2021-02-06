package ccode.mcsm.client;

import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class RemoteStage extends Stage {

	private Remote remote;
	
	//Menu fields
	private MenuBar menu;
	private Menu login;
	private Menu connection;
	
	public RemoteStage(Remote remote) {
		this.remote = remote;
		
		setTitle("MCSM Remote");
		
		//Create menu
		login = new Menu("Login");
		connection = new Menu("Connection");
		
		menu = new MenuBar(login, connection);
		
		//Setup BorderPane layout
		BorderPane stageLayout = new BorderPane();
		stageLayout.setTop(menu);
		
		Scene scene = new Scene(stageLayout, 1280, 720);
		setScene(scene);
	}
	
}
