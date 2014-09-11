package openstatestats.model;

import java.util.*;

public class Aggregates<T> {
	private Map<String, Aggregate<T>> aggregates = new LinkedHashMap<String, Aggregate<T>>();
	private Map<String, ArrayList<String>> descriptions = new LinkedHashMap<String, ArrayList<String>>();
	private Map<String, String[]> groups = new LinkedHashMap<String, String[]>();

	public Aggregate<T> createGroup(String key, String[] labels) throws OpenStatsException {
		if ( groups.put(key, labels) != null ) throw new OpenStatsException("Group already exists: " + key);
		Aggregate<T> aggregate = new Aggregate<T>(labels);
		aggregates.put(key, aggregate);
		return aggregate;
	}
	public Aggregate<T> getAggregate(String key) {
		return aggregates.get(key);
	}
	public String[] getGroupLabels(String key) {
		return groups.get(key);
	}
	public void setGroupLabels(String key, String[] labels) {
		groups.put(key, labels);
	}
	public List<String> getDescriptions(String key) {
		return descriptions.get(key);
	}
	public void setDescriptions(String key, ArrayList<String> descriptions) {
		this.descriptions.put(key, descriptions);
	}
}
