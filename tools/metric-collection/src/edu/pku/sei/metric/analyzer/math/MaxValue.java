package edu.pku.sei.metric.analyzer.math;

import edu.pku.sei.metric.MetricValue;

/**
 * The metric value as result of maximum operation
 * 
 * @author PCT
 * 
 */
public class MaxValue extends MetricValue {

	private static final long serialVersionUID = -8555108852746539668L;

	private String handle;

	public MaxValue(String name, double value) {
		super(name, value);
		handle = null;
	}

	public MaxValue(String name, double value, String handle) {
		super(name, value);
		this.handle = handle;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

}
