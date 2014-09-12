package openstatestats.model;

import java.util.*;

public class Aggregate<T> {

	private Map<T, Long[]> values = new LinkedHashMap<T, Long[]>();
	private String[] labels;
	
	public Aggregate() {}
	public Aggregate(String[] labels) {
		this.labels = labels;
	}
	public Long[] createValues(T key) {
		Long[] values = new Long[labels.length];
		Arrays.fill(values, null);
		this.values.put(key, values);
		return values;
	}
	public Long[] getValues(T key) {
		return values.get(key);
	}
	public Long getValue(T key, String label) throws OpenStatsException {
		Long[] values = this.values.get(key);
		for(int i=0; i<labels.length; ++i) if (labels[i].equals(label)) return values[i];
		throw new OpenStatsException("Cannot find label: " + label);
	}
	public void setValues(T key, Long[] values) throws OpenStatsException {
		this.values.put(key, values);
	}
	public String[] getLabels() {
		return labels;
	}
	public void setLabels(String[] labels) {
		this.labels = labels;
	}
	
}
