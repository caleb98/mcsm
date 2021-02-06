package ccode.mcsm.client;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientStart extends Application {
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Remote remote = new Remote();
		
		ConnectStage connect = new ConnectStage(remote);
		connect.show();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
}
