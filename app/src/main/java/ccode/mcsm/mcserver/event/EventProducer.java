package ccode.mcsm.mcserver.event;

/**
 * Produces an event from a given string.
 */
@FunctionalInterface
public interface EventProducer {
	/**
	 * Uses the given line to determine and produce a
	 * MinecraftServerEvent. This method should first 
	 * check that the line is of the requested event
	 * type, and only then should it attempt to construct
	 * an event object. If the line is not of the 
	 * correct format for this even type, then this
	 * method should return null;
	 * @param line the line to check
	 * @return event; null if line doesn't match event format
	 */
	public MinecraftServerEvent produce(String line);
}
