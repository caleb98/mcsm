package ccode.mcsm.client;

import ccode.mcsm.net.message.NetLoginMessage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ConnectStage extends Stage {
	
	public ConnectStage(Remote remote) {
		setTitle("MCSM Login");
		setResizable(false);
		
		//Create ip input area
		Label ipLabel = new Label("MCSM IP:");		
		TextField ipInput = new TextField("127.0.0.1");
		
		Label nameLabel = new Label("Player Name:");
		TextField nameInput = new TextField();
		
		Label passLabel = new Label("Password:");
		PasswordField passInput = new PasswordField();
		
		//Create connect button
		Button connectButton = new Button("Connect");
		connectButton.setAlignment(Pos.TOP_CENTER);
		connectButton.setMaxWidth(Double.MAX_VALUE);
		
		connectButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//Only do something if we're not currently trying to connect.
				if(!remote.isConnecting()) {
					
					//If we're not connected, connect first.
					if(!remote.isConnected()) {
						if(!remote.connect(ipInput.getText(), 36363, ConnectStage.this)) return;
					}
					
					//Send the login info
					NetLoginMessage login = new NetLoginMessage(nameInput.getText(), passInput.getText());
					remote.sendUDP(login);
					
				}
			}
		});
		
		//Setup layout
		GridPane layout = new GridPane();
		layout.add(ipLabel, 0, 0);
		layout.add(ipInput, 1, 0);
		
		layout.add(nameLabel, 0, 1);
		layout.add(nameInput, 1, 1);
		
		layout.add(passLabel, 0, 2);
		layout.add(passInput, 1, 2);
		
		layout.add(connectButton, 0, 3, 2, 1);
		
		layout.setHgap(5);
		layout.setVgap(5);
		
		ColumnConstraints cc1 = new ColumnConstraints();
		ColumnConstraints cc2 = new ColumnConstraints();
		cc1.setPercentWidth(25);
		cc2.setPercentWidth(75);
		layout.getColumnConstraints().addAll(cc1, cc2);
		
		GridPane.setFillWidth(connectButton, true);
		GridPane.setFillWidth(ipInput, true);
		layout.setPadding(new Insets(10));
		
		layout.setAlignment(Pos.CENTER);
		
		//Create scene
		Scene scene = new Scene(layout, 400, 150);	
		setScene(scene);
	}

}
