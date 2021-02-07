package ccode.mcsm.permissions;

import java.util.Set;

import com.esotericsoftware.kryonet.Connection;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.action.Action;
import ccode.mcsm.net.message.NetExecutionMessage;
import ccode.mcsm.task.Task;

public class RemoteExecutor extends Executor {
	
	private Connection connection;
	private Player player;
	
	public RemoteExecutor(Connection connection, Player player) {
		this.connection = connection;
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Connection getConnection() {
		return connection;
	}

	@Override
	public void sendMessage(MinecraftServerManager manager, String message) {
		NetExecutionMessage netmsg = new NetExecutionMessage(message);
		connection.sendUDP(netmsg);
	}

	@Override
	public Set<String> getOverrideCommands() {
		return player.getOverrideCommands();
	}
	
	@Override
	public Permissions getPermissions() {
		return player.getPermissions();
	}

	@Override
	public int getPermissionsLevel() {
		return player.getPermissionsLevel();
	}

	@Override
	public boolean hasPermissions(String actionId) {
		return player.hasPermissions(actionId);
	}

	@Override
	public boolean hasPermissions(Action action) {
		return player.hasPermissions(action);
	}

	@Override
	public boolean hasPermissions(Task task) {
		return player.hasPermissions(task);
	}

}
