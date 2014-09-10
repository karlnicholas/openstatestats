package openstatestats.model;

import java.util.*;

public class Session {
	
	private String state;
	private String session;
	private List<District> districts = new ArrayList<District>();
	
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
	public List<District> getDistricts() {
		return districts;
	}
	public void setDistricts(List<District> districts) {
		this.districts = districts;
	}	
}
