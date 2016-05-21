package edu.pku.sei.metric.analyzer.math;

import edu.pku.sei.metric.MetricValue;

/**
 * The metric value as result of average operation
 * 
 * @author PCT
 * 
 */
public class AvgValue extends MetricValue {

	private static final long serialVersionUID = 3797603259163008689L;

	private double num;

	public AvgValue(String name, double value) {
		super(name, value);
		num = 1;
	}

	public AvgValue(String name, double value, double num) {
		super(name, value);
		this.num = num;
	}

	public double getNum() {
		return num;
	}

	public void setNum(double num) {
		this.num = num;
	}

	public String toString() {
		return name + "-" + value + "-" + num;
	}
}
