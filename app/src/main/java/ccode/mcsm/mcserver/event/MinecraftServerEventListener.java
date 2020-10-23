package ccode.mcsm.mcserver.event;

import java.util.LinkedList;
import java.util.function.Consumer;

public class MinecraftServerEventListener {

	private LinkedList<MinecraftServerEvent> queue = new LinkedList<>();
	private Consumer<MinecraftServerEvent> eventConsumer;
	
	public MinecraftServerEventListener(Consumer<MinecraftServerEvent> processEvent) {
		eventConsumer = processEvent;
	}
	
	public void processEvents() {
		while(queue.peek() != null) {
			eventConsumer.accept(queue.remove());
		}
	}
	
	public void addEvent(MinecraftServerEvent event) {
		queue.add(event);
	}
	
}
