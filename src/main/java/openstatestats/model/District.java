package openstatestats.model;

import java.util.*;

public class District extends Aggregate implements Comparable<District> {
	private String chamber;
	private String district;
	private List<Legislator> legislators = new ArrayList<Legislator>();

	public String getChamber() {
		return chamber;
	}
	public void setChamber(String chamber) {
		this.chamber = chamber;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public List<Legislator> getLegislators() {
		return legislators;
	}
	public void setLegislators(List<Legislator> legislators) {
		this.legislators = legislators;
	}
	@Override
	public int compareTo(District o) {
		if ( !chamber.equals(o.chamber)) return chamber.compareTo(o.chamber);
		return district.compareTo(o.district);
	}
}
