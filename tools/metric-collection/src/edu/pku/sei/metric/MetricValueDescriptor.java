package edu.pku.sei.metric;

/**
 * The descriptor is designed to represent a kind of source code metric
 * 
 * @author liushi07
 * 
 */
public class MetricValueDescriptor {

	private String metricName;

	private int level;

	private String description;

	//TODO: what does that mean?
	private boolean propagate;

	public MetricValueDescriptor(String name, int level, String description,
			boolean propagate) {
		this.metricName = name;
		this.level = level;
		this.description = description;
		this.propagate = propagate;
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public boolean isPropagate() {
		return propagate;
	}

	public void setPropagate(boolean propagate) {
		this.propagate = propagate;
	}

	public boolean getPropagate() {
		return propagate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
