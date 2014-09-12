package openstatestats.model;

public class Legislator extends UserData<Legislator> {
	
	private String name;
	private String party;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParty() {
		return party;
	}
	public void setParty(String party) {
		this.party = party;
	}

}
