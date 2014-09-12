package openstatestats.model;

public class Session extends UserData<Session> implements Comparable<Session> {
	
	private String state;
	private String session;
	private Districts districts = new Districts();
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getSession() {
		return session;
	}
	public void setSession(String session) {
		this.session = session;
	}
	public Districts getDistricts() {
		return districts;
	}
	public void setDistricts(Districts districts) {
		this.districts = districts;
	}
	
	@Override
	public int compareTo(Session session) {
		int s = state.compareTo(session.state);
		if ( s != 0 ) return s;
		return this.session.compareTo(session.session);
	}
}
