package edu.pku.sei.metric.analyzer.math;

import edu.pku.sei.metric.Constants;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.source.AbstractMetricElement;

/**
 * The interface of Math Operator. All the operators should implements it.
 * 
 * @author zhanglm07
 * 
 */
public interface MathOperator extends Constants {

	/**
	 * Do the math operation on the metric values and set to the metric element
	 * 
	 * @param metricElement
	 * @param descriptor
	 */
	public void operate(AbstractMetricElement metricElement,
			MetricValueDescriptor descriptor);

}
