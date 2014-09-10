package openstatestats.model;

import java.util.*;

public class Aggregate {
	private Map<String, Long> aggregates = new LinkedHashMap<String, Long>();
	private Map<String, Double> computations = new LinkedHashMap<String, Double>();

	public Map<String, Long> getAggregates() {
		return aggregates;
	}
	public void setAggregates(Map<String, Long> aggregates) {
		this.aggregates = aggregates;
	}
	public Map<String, Double> getComputations() {
		return computations;
	}
	public void setComputations(Map<String, Double> computations) {
		this.computations = computations;
	}
	
	
}