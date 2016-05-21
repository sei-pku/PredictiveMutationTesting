package edu.pku.sei.metric.analyzer.math;

import java.util.List;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.source.AbstractMetricElement;


/**
 * Get the average metric value and set to the metric element. The average value
 * is calculated from the metric results of children's .
 * 
 * @author zhanglm07
 * 
 */
public class Average implements MathOperator {

	public void operate(AbstractMetricElement metricElement,
			MetricValueDescriptor descriptor) {
		int level = descriptor.getLevel();
		String metricName = descriptor.getMetricName();
		boolean propagate = descriptor.getPropagate();
		if (metricElement.getLevel() > level) {
			List<AbstractMetricElement> children = metricElement.getChildren();
			double sum = 0;
			double num = 0;
			double mean = 0;

			for (int i = 0; i < children.size(); i++) {
				AbstractMetricElement child = children.get(i);
				if (null != child.getAverageValue(metricName)) {
					sum += child.getAverageValue(metricName).getValue()
							* child.getAverageValue(metricName).getNum();
					num += child.getAverageValue(metricName).getNum();
				}
			}
			if (num != 0)
				mean = sum / num;
			metricElement.setAverageValue(new AvgValue(metricName, mean, num));
			if (propagate)
				metricElement.setValue(new MetricValue(metricName, sum));

		} else if (metricElement.getLevel() == level) {
			MetricValue mv = metricElement.getValue(metricName);
			if (mv != null)
				metricElement.setAverageValue(new AvgValue(mv.getName(), mv
						.getValue()));
		}

	}

}
