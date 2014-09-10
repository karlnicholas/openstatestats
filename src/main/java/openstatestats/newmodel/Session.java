package openstatestats.newmodel;

import java.util.Date;

public class Session {
	
	Date sessionStart;
	Date sessionEnd;
	String sessionName;

	public Session(String sessionName, Date sessionStart, Date sessionEnd) {
		this.sessionName = sessionName;
		this.sessionStart = sessionStart;
		this.sessionEnd = sessionEnd;
	}

}
