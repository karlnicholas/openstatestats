package openstatestats.newmodel;

import java.util.*;

public class StateCapital {
	public static enum STATES { CA, NY };
	private static final Map<STATES, TimeZone> timeZones;
	static {
		timeZones = new TreeMap<STATES, TimeZone>();
		timeZones.put(STATES.CA, TimeZone.getTimeZone("America/Los_Angeles"));
		timeZones.put(STATES.NY, TimeZone.getTimeZone("GMT-05:00"));
	};
	
	public STATES state;
	
	public StateCapital(STATES state) {
		this.state = state;
	}
	
	public Date createDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance(timeZones.get(state));
		cal.set(year,  month, day);
		return cal.getTime();
	}

}
