package edu.pku.sei.metric.analyzer;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.MethodMetric;

/**
 * Count the number of parameters of methods
 * 
 * @author PCT
 * 
 */
public class NumberOfParameters extends Analyzer {

	private static String NOP;

	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics.size() == 1 : "There must be 1 child as the extension said";
		NOP = metrics.get(0).getMetricName();
	}

	private static class BasicMetric extends Metric {

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(NOP, 0) };
		}

		@Override
		protected void calculate() {
			assert source instanceof MethodMetric : "The calculation is set on method";
			MethodDeclaration astNode = (MethodDeclaration) source.getASTNode();
			if (null == astNode)
				return;
			else
				values[0].setValue(astNode.parameters().size());
		}
	}

	@Override
	public void analyze(AbstractMetricElement source,
			final List<MathOperator> mathOperators) {

		MetricElementVisitor visitor = new MetricElementVisitor() {

			@Override
			public boolean visit(MethodMetric metricElement) {
				BasicMetric metric = new BasicMetric(metricElement);
				metric.setValuesToMetricElement();
				return false;
			}

			public void postVisit(AbstractMetricElement metricElement) {
				for (int i = 0; i < metrics.size(); i++) {
					MetricValueDescriptor descriptor = metrics.get(i);
					for (int j = 0; j < mathOperators.size(); j++) {
						MathOperator mathOperator = mathOperators.get(j);
						mathOperator.operate(metricElement, descriptor);
					}
				}
			}
		};
		source.accept(visitor);
	}

}
