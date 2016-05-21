package edu.pku.sei.metric.analyzer;

import java.util.List;

import edu.pku.sei.metric.Constants;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;


/**
 * Source code analyzer to get different metric value
 * 
 * @author zhanglm07
 * 
 */
public abstract class Analyzer implements Constants {

	// metrics for calculation
	protected List<MetricValueDescriptor> metrics;

	/**
	 * Set the source code metrics for calculation
	 * 
	 * @param metrics
	 */
	public void setMetrics(List<MetricValueDescriptor> metrics) {
		this.metrics = metrics;
	}

	/**
	 * Analyze a given Java source and get the metric results
	 * 
	 * @param source
	 * @param mathOperators
	 */
	public abstract void analyze(AbstractMetricElement source,
			List<MathOperator> mathOperators);
}
