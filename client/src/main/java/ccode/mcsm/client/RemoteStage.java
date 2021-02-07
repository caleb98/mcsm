package ccode.mcsm.client;

import ccode.mcsm.net.message.NetDoActionMessage;
import ccode.mcsm.net.message.NetExecutionMessage;
import ccode.mcsm.net.message.NetMinecraftChatMessage;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RemoteStage extends Stage {

	private Remote remote;
	
	//Menu fields
	private MenuBar menu;
	private Menu fileMenu;
	
	//Content splitpane
	private SplitPane centerPane;
	
	//TabPane fields
	private TabPane tabs;
	
	private Tab generalTab;
	private Button startServerButton;
	private Button stopServerButton;
	private Button saveServerButton;
	
	private Tab chatTab;
	private TextArea chat;
	
	//Infobox
	private VBox infobox;
	private Label infoboxLabel;
	private TextArea infoboxText;
	private TextField serverInput;
	
	public RemoteStage(Remote remote) {
		this.remote = remote;
		
		setTitle("MCSM Remote");
		
		//Create menu
		fileMenu = new Menu("File");
		
		menu = new MenuBar(fileMenu);
		
		//Create tabs
		startServerButton = new Button("Start Server");
		startServerButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		startServerButton.setOnAction((event)->{
			NetDoActionMessage action = new NetDoActionMessage("StartServer", "");
			remote.sendUDP(action);
		});
		stopServerButton = new Button("Stop Server");
		stopServerButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		stopServerButton.setOnAction((event)->{
			NetDoActionMessage action = new NetDoActionMessage("StopServer", "");
			remote.sendUDP(action);
		});
		saveServerButton = new Button("Save World");
		saveServerButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		saveServerButton.setOnAction((event)->{
			NetDoActionMessage action = new NetDoActionMessage("SaveServer", "");
			remote.sendUDP(action);
		});
		
		GridPane generalButtons = new GridPane();
		generalButtons.add(startServerButton, 0, 0);
		generalButtons.add(stopServerButton, 1, 0);
		generalButtons.add(saveServerButton, 0, 1, 2, 1);
		generalButtons.setAlignment(Pos.CENTER);
		generalButtons.setHgap(5);
		generalButtons.setVgap(5);
		generalButtons.setPadding(new Insets(5));
		
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHgrow(Priority.ALWAYS);
		generalButtons.getColumnConstraints().addAll(cc, cc);
		RowConstraints rc = new RowConstraints();
		rc.setVgrow(Priority.ALWAYS);
		generalButtons.getRowConstraints().addAll(rc, rc);
		
		GridPane.setFillHeight(startServerButton, true);
		GridPane.setFillHeight(stopServerButton, true);
		GridPane.setFillHeight(saveServerButton, true);
		
		generalTab = new Tab("General", generalButtons);

		chat = new TextArea();
		chatTab = new Tab("Chat", chat);
		
		tabs = new TabPane(generalTab, chatTab);
		tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		
		//Create infobox
		infoboxLabel = new Label("Server:");
		infoboxText = new TextArea();
		infoboxText.setEditable(false);
		VBox.setVgrow(infoboxText, Priority.ALWAYS);
		
		serverInput = new TextField();
		addEventFilter(KeyEvent.KEY_PRESSED, (event)->{
			if(serverInput.isFocused() && event.getCode() == KeyCode.ENTER) {
				String input = serverInput.getText().trim();
				
				if(input.equals("clear")) {
					infoboxText.clear();
					serverInput.clear();
					return;
				}
				
				serverInput.clear();
				if(input.matches("\\s*")) {
					return;
				}
				String action = input.split("\\s+")[0];
				String args = input.substring(action.length()).trim();
				
				NetDoActionMessage netAction = new NetDoActionMessage(action, args);
				remote.sendUDP(netAction);
			}
		});
		
		infobox = new VBox(infoboxLabel, infoboxText, serverInput);
		
		//Create center pane
		centerPane = new SplitPane(tabs, infobox);
		centerPane.setOrientation(Orientation.VERTICAL);
		
		//Setup BorderPane layout
		BorderPane stageLayout = new BorderPane();
		stageLayout.setTop(menu);
		stageLayout.setCenter(centerPane);
		
		Scene scene = new Scene(stageLayout, 600, 400);
		setScene(scene);
	}
	
	public void addChatMessage(NetMinecraftChatMessage message) {
		String newLine = String.format("[%s] <%s>: %s\n", 
				message.timestamp, message.playerName, message.message);
		chat.appendText(newLine);
	}
	
	public void addExecutionMessage(NetExecutionMessage message) {
		infoboxText.appendText(message.message + "\n");
	}
	
}
