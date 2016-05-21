package edu.pku.sei.metric.analyzer;

import java.util.List;


import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;

import edu.pku.sei.metric.Constants;
import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.TypeMetric;

/**
 * Analyze the inheritance depth of a Type and its sub classes number
 * 
 * @author PCT
 * 
 */
public class InheritanceDepth extends Analyzer implements Constants {

	private static String INHERITANCE_DEPTH;

	private static String SUB_CLASSES;

	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics.size() == 2 : "There must be two children as the extension said";
		INHERITANCE_DEPTH = metrics.get(0).getMetricName();
		SUB_CLASSES = metrics.get(1).getMetricName();
	}

	private static class BasicMetric extends Metric {

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(INHERITANCE_DEPTH, 0),
					new MetricValue(SUB_CLASSES, 0) };
		}

		@Override
		protected void calculate() {
			assert source instanceof TypeMetric : "The calculation must be set on Type";
			TypeMetric metricElement = (TypeMetric) source;
			IType iType = (IType) metricElement.getJavaElement();

			ITypeHierarchy hierarchy = metricElement.getHierarchy();
			
			IType[] supers = hierarchy.getAllSuperclasses(iType);
			IType[] subs = hierarchy.getSubtypes(iType);
			values[0].setValue(supers.length);
			values[1].setValue(subs.length);
		}

	}

	@Override
	public void analyze(AbstractMetricElement source,
			final List<MathOperator> mathOperators) {
		
		MetricElementVisitor visitor = new MetricElementVisitor() {
			
			public boolean visit(TypeMetric metricElement) {
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
