package openstatestats.newmodel;

import java.util.Date;

public class Term {
	Date termStart;
	Date termEnd;
	String termName;
	
	public Term(String termName, Date termStart, Date termEnd) {
		this.termName = termName;
		this.termStart = termStart;
		this.termEnd = termEnd;
	}

}
