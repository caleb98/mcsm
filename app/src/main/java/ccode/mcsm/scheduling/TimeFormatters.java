package ccode.mcsm.scheduling;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatters {

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	
	public static String now() {
		return DATE_FORMATTER.format(LocalDateTime.now());
	}
	
}
