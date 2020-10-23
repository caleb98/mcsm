package ccode.mcsm.scheduling;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Timestamp {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	
	public static String now() {
		return FORMATTER.format(ZonedDateTime.now());
	}
	
}
