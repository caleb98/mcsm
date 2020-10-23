package ccode.mcsm.net;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import ccode.mcsm.net.message.ConnectMessage;
import ccode.mcsm.net.message.ErrorMessage;
import ccode.mcsm.net.message.InfoMessage;
import ccode.mcsm.net.message.SaveServer;
import ccode.mcsm.net.message.ServerConnectSuccess;
import ccode.mcsm.net.message.StartServer;
import ccode.mcsm.net.message.StopServer;

public class ClientUI extends JFrame {

	private static final long serialVersionUID = 3971399362644365771L;
	
	private static final String LOGIN_PANEL = "LoginPanel";
	private static final String CONTROL_PANEL = "ControlPanel";
	
	//Client
	private Client client;
	
	//UI Objects
	private JPanel loginPanel;
	private JTextField username;
	private JTextField serverIp;
	private JTextField password;
	private JButton connectButton;
	
	private JPanel controlPanel;
	private JButton startServerButton;
	private JButton saveServerButton;
	private JButton stopServerButton;
	private JLabel serverInfo;
	
	private JPanel cards;
	
	public ClientUI() {
		super("MC Remote");
		
		//Create the login panel
		loginPanel = new JPanel(new GridBagLayout());
		GridBagConstraints con = new GridBagConstraints();
		
		JLabel loginLabel = new JLabel("Enter User Info");
		con.gridx = 0;
		con.gridy = 0;
		con.anchor = GridBagConstraints.CENTER;
		con.weightx = 1;
		loginPanel.add(loginLabel, con);
		
		Insets insets = new Insets(2, 10, 0, 10);
		
		username = new JTextField("username");
		con.gridy = 1;
		con.insets = insets;
		con.fill = GridBagConstraints.HORIZONTAL;
		loginPanel.add(username, con);
		
		serverIp = new JTextField("server ip");
		con.gridy = 2;
		loginPanel.add(serverIp, con);
		
		password = new JTextField("password");
		con.gridy = 3;
		loginPanel.add(password, con);
		
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connectButtonPressed();
			}
		});
		con.fill = GridBagConstraints.NONE;
		con.gridy = 4;
		loginPanel.add(connectButton, con);
		
		//Create the control panel
		controlPanel = new JPanel(new GridBagLayout());
		con = new GridBagConstraints();
		con.fill = GridBagConstraints.BOTH;
		
		startServerButton = new JButton("Start Server");
		startServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startServerButtonClicked();
			}
		});
		con.gridheight = 1;
		con.gridwidth = 1;
		con.gridx = 0;
		con.gridy = 0;
		con.weightx = 1;
		con.weighty = 1;
		controlPanel.add(startServerButton, con);
		
		saveServerButton = new JButton("Save Server");
		saveServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveServerButtonClicked();
			}
		});
		con.gridheight = 1;
		con.gridwidth = 1;
		con.gridx = 1;
		con.gridy = 0;
		con.weightx = 1;
		con.weighty = 1;
		controlPanel.add(saveServerButton, con);
		
		stopServerButton = new JButton("Stop Server");
		stopServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopServerButtonClicked();
			}
		});
		con.gridheight = 1;
		con.gridwidth = 2;
		con.gridx = 0;
		con.gridy = 2;
		con.weightx = 1;
		con.weighty = 1;
		controlPanel.add(stopServerButton, con);
		
		serverInfo = new JLabel("Use the buttons above to control the server.");
		con.gridheight = 1;
		con.gridwidth = 2;
		con.gridx = 0;
		con.gridy = 3;
		con.weightx = 1;
		con.weighty = 1;
		controlPanel.add(serverInfo, con);
		
		//Setup the frame
		cards = new JPanel(new CardLayout());
		cards.add(loginPanel, LOGIN_PANEL);
		cards.add(controlPanel, CONTROL_PANEL);

		add(cards, BorderLayout.CENTER);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 200);
		setResizable(false);
		setVisible(true);
	}

	private void startServerButtonClicked() {
		if(client != null && client.isConnected()) {
			client.sendTCP(new StartServer());
		}
	}

	private void saveServerButtonClicked() {
		if(client != null && client.isConnected()) {
			client.sendTCP(new SaveServer());
		}
	}
	
	private void stopServerButtonClicked() {
		if(client != null && client.isConnected()) {
			client.sendTCP(new StopServer());
		}
	}

	private void connectButtonPressed() {
		String user = username.getText();
		String ip = serverIp.getText();
		String pass = password.getText();
		
		try {
			client = KryoCreator.createClient();
			client.addListener(new ClientListener());
			client.start();
			client.connect(5000, ip, 44434, 44434);
			client.sendTCP(new ConnectMessage(user, pass));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void showDialogError(String message) {
		JOptionPane.showMessageDialog(
				this,
				message,
				"Error",
				JOptionPane.ERROR_MESSAGE);
	}
	
	private class ClientListener extends Listener {
		
		@Override
		public void received(Connection connection, Object object) {
			if(object instanceof ServerConnectSuccess) {
				CardLayout cl = (CardLayout) cards.getLayout();
				cl.show(cards, CONTROL_PANEL);
			}
			else if(object instanceof ErrorMessage) {
				ErrorMessage m = (ErrorMessage) object;
				showDialogError(m.error);
			}
			else if(object instanceof InfoMessage) {
				InfoMessage m = (InfoMessage) object;
				serverInfo.setText(m.info);
			}
		}
		
		@Override
		public void disconnected(Connection connection) {
			CardLayout cl = (CardLayout) cards.getLayout();
			cl.show(cards, LOGIN_PANEL);
		}
		
	}
	
}
