package ccode.mcsm.mcserver.event;

@FunctionalInterface
public interface EventListener {
	/**
	 * Processes the given event
	 * @param event
	 * @return whether or not this listener should be removed
	 */
	public boolean process(MinecraftServerEvent event);
}
