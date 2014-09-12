package openstatestats.model;

import java.util.*;

public class Computation<T> {
	private Map<T, Double[]> values = new LinkedHashMap<T, Double[]>();
	private String[] labels;

	public Computation() {}
	public Computation(String[] labels) {
		this.labels = labels;		
	}
	public Double[] createValues(T key) {
		Double[] values = new Double[labels.length];
		Arrays.fill(values, null);
		this.values.put(key, values);
		return values;
	}
	public Double[] getValues(T key) {
		return values.get(key);
	}
	public Double getValue(T key, String label) throws OpenStatsException {
		Double[] values = this.values.get(key);
		for(int i=0; i<labels.length; ++i) if (labels[i].equals(label)) return values[i];
		throw new OpenStatsException("Cannot find label: " + label);
	}
	public void setValues(T key, Double[] values) {
		this.values.put(key, values);
	}
	public String[] getLabels() {
		return labels;
	}
	public void setLabels(String[] labels) {
		this.labels = labels;
	}
}
