package edu.pku.sei.metric.analyzer.math;

import java.util.List;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.source.AbstractMetricElement;


/**
 * Get the minimum value among the children's metric results and set to the
 * metric element.
 * 
 * @author PCT
 * 
 */
public class Min implements MathOperator {

	public void operate(AbstractMetricElement metricElement,
			MetricValueDescriptor descriptor) {
		int level = descriptor.getLevel();
		String metricName = descriptor.getMetricName();
		if (metricElement.getLevel() > level) {
			List<AbstractMetricElement> children = metricElement.getChildren();
			double min = 10000;
			String handle = null;
			for (int i = 0; i < children.size(); i++) {
				AbstractMetricElement child = children.get(i);
				if (null != child.getMinValue(metricName)
						&& child.getMinValue(metricName).getValue() < min) {
					min = child.getMinValue(metricName).getValue();
					handle = child.getMinValue(metricName).getHandle();
				}
			}
			if (null != handle)
				metricElement
						.setMinValue(new MinValue(metricName, min, handle));

		} else if (metricElement.getLevel() == level) {
			MetricValue mv = metricElement.getValue(metricName);
			if (mv != null)
				metricElement.setMinValue(new MinValue(mv.getName(), mv
						.getValue(), metricElement.getHandle()));
		}
	}

}
