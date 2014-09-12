package openstatestats.model;

import java.util.*;

public class Computations<T> {
	private Map<String, Computation<T>> computations = new LinkedHashMap<String, Computation<T>>();
	private Map<String, ArrayList<String>> descriptions = new LinkedHashMap<String, ArrayList<String>>();
	private Map<String, String[]> groups = new LinkedHashMap<String, String[]>();

	public Computation<T> createGroup(String key, String[] labels) throws OpenStatsException {
		if ( groups.put(key, labels) != null ) throw new OpenStatsException("Group already exists: " + key);
		Computation<T> computation = new Computation<T>(labels);
		computations.put(key, computation);
		return computation;
	}
	public Computation<T> getComputation(String key) {
		return computations.get(key);
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
