package edu.pku.sei.metric.analyzer.math;

import java.util.List;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.source.AbstractMetricElement;


/**
 * Get the maximum value among the children's metric results and set to the
 * metric element.
 * 
 * @author PCT
 * 
 */
public class Max implements MathOperator {

	public void operate(AbstractMetricElement metricElement,
			MetricValueDescriptor descriptor) {
		int level = descriptor.getLevel();
		String metricName = descriptor.getMetricName();
		if (metricElement.getLevel() > level) {
			List<AbstractMetricElement> children = metricElement.getChildren();
			double max = 0;
			String handle = null;
			for (int i = 0; i < children.size(); i++) {
				AbstractMetricElement child = children.get(i);
				if (null != child.getMaxValue(metricName)
						&& child.getMaxValue(metricName).getValue() > max) {
					max = child.getMaxValue(metricName).getValue();
					handle = child.getMaxValue(metricName).getHandle();
				}
			}
			if (null != handle)
				metricElement
						.setMaxValue(new MaxValue(metricName, max, handle));

		} else if (metricElement.getLevel() == level) {
			MetricValue mv = metricElement.getValue(metricName);
			if (mv != null)
				metricElement.setMaxValue(new MaxValue(mv.getName(), mv
						.getValue(), metricElement.getHandle()));
		}

	}

}
