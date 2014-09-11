package openstatestats.model;

public class UserData<T> {
	private Aggregates<T> aggregates = new Aggregates<T>();
	private Computations<T> computations = new Computations<T>();

	public Aggregate<T> createAggregate(String group, String[] labels) throws OpenStatsException {
		return aggregates.createGroup(group, labels);
	}
	public Aggregates<T> getAggregates() {
		return aggregates;
	}
	public void setAggregates(Aggregates<T> aggregates) {
		this.aggregates = aggregates;
	}
	public Aggregate<T> getAggregate(String key) {
		return aggregates.getAggregate(key);
	}
	public void setAggregate(Aggregates<T> aggregates) {
		this.aggregates = aggregates;
	}
	public Computation<T> createComputation(String group, String[] labels) throws OpenStatsException {
		return computations.createGroup(group, labels);
	}
	public Computations<T> getComputations() {
		return computations;
	}
	public Computation<T> getComputation(String key) {
		return computations.getComputation(key);
	}
	public void setComputations(Computations<T> computations) {
		this.computations = computations;
	}
}