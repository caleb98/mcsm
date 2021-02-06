package ccode.mcsm.mcserver.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public abstract class MinecraftServerEvent {
	
	private static final List<EventProducer> events;
	
	static {
		ArrayList<EventProducer> eventProducers = new ArrayList<>();
		
		//Add all event producers here.
		eventProducers.add((line)->{
			Matcher m = PlayerJoinedEvent.MATCHER.matcher(line);
			if(!m.matches()) {
				return null;
			}
			return new PlayerJoinedEvent(m.group(1), m.group(2));
		});
		
		eventProducers.add((line)->{
			Matcher m = PlayerLeftEvent.MATCHER.matcher(line);
			if(!m.matches()) {
				return null;
			}
			return new PlayerLeftEvent(m.group(1), m.group(2));
		});
		
		eventProducers.add((line)->{
			Matcher m = AchievementGetEvent.MATCHER.matcher(line);
			if(!m.matches()) {
				return null;
			}
			return new AchievementGetEvent(m.group(1), m.group(2), m.group(3));
		});
		
		eventProducers.add((line)->{
			Matcher m = ServerLoadedEvent.MATCHER.matcher(line);
			if(!m.matches()) {
				return null;
			}
			return new ServerLoadedEvent(m.group(1), m.group(2));
		});
		
		eventProducers.add((line)->{
			Matcher m = PlayerAuthEvent.MATHCHER.matcher(line);
			if(!m.matches()) {
				return null;
			}
			return new PlayerAuthEvent(m.group(1), m.group(2), m.group(3));
		});
		
		eventProducers.add((line)->{
			Matcher m = WorldSavedEvent.MATCHER.matcher(line);
			if(!m.matches()) {
				return null;
			}
			return new WorldSavedEvent(m.group(1));
		});
		
		events = Collections.unmodifiableList(eventProducers);
	}
	
	public static List<EventProducer> getEvents() {
		return events;
	}
	
	public final String id;
	public final String timestamp;
	
	public MinecraftServerEvent(String id, String timestamp) {
		this.id = id;
		this.timestamp = timestamp;
	}
	
}