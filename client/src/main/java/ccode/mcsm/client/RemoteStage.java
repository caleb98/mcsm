package ccode.mcsm.client;

import ccode.mcsm.net.message.NetMinecraftChatMessage;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class RemoteStage extends Stage {

	private Remote remote;
	
	//Menu fields
	private MenuBar menu;
	private Menu fileMenu;
	
	//TabPane fields
	private TabPane tabs;
	private Tab controlTab;
	private Tab tasksTab;
	private Tab backupsTab;
	
	private Tab chatTab;
	private TextArea chat;
	
	public RemoteStage(Remote remote) {
		this.remote = remote;
		
		setTitle("MCSM Remote");
		
		//Create menu
		fileMenu = new Menu("File");
		
		menu = new MenuBar(fileMenu);
		
		//Create tabs
		controlTab = new Tab("Control");
		tasksTab = new Tab("Tasks");
		backupsTab = new Tab("Backups");

		chat = new TextArea();
		chatTab = new Tab("Chat", chat);
		
		tabs = new TabPane(controlTab, tasksTab, backupsTab, chatTab);
		tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		
		//Setup BorderPane layout
		BorderPane stageLayout = new BorderPane();
		stageLayout.setTop(menu);
		stageLayout.setCenter(tabs);
		
		Scene scene = new Scene(stageLayout, 600, 400);
		setScene(scene);
	}
	
	public void addChatMessage(NetMinecraftChatMessage message) {
		String newLine = String.format("[%s] <%s>: %s\n", 
				message.timestamp, message.playerName, message.message);
		chat.appendText(newLine);
	}
	
}
