package edu.pku.sei.metric.analyzer.math;

import edu.pku.sei.metric.MetricValue;

/**
 * The metric value as result of minimum operation
 * 
 * @author PCT
 * 
 */
public class MinValue extends MetricValue {

	private static final long serialVersionUID = -5811512038422729006L;

	private String handle;

	public MinValue(String name, double value) {
		super(name, value);
		this.handle = null;
	}

	public MinValue(String name, double value, String handle) {
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
