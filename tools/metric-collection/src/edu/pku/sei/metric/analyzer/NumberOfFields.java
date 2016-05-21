package edu.pku.sei.metric.analyzer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.TypeMetric;

/**
 * Count the number of fields
 * 
 * @author PCT
 * 
 */
public class NumberOfFields extends Analyzer {

	private static String NOF;

	private static String NUM_INST_FIELDS;

	private static String NUM_STAT_FIELDS;

	private Logger logger = Logger.getLogger(NumberOfFields.class.getName());

	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics.size() == 3 : "There must be 3 children as the extension said";
		NOF = metrics.get(0).getMetricName();
		NUM_INST_FIELDS = metrics.get(1).getMetricName();
		NUM_STAT_FIELDS = metrics.get(2).getMetricName();
	}

	private class BasicMetric extends Metric {

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(NOF, 0),
					new MetricValue(NUM_INST_FIELDS, 0),
					new MetricValue(NUM_STAT_FIELDS, 0) };
		}

		@Override
		protected void calculate() {
			assert source instanceof TypeMetric : "The calculation is set on Type";
			TypeMetric metricElement = (TypeMetric) source;

			int sFieldNumber = 0;
			int iFieldNumber = 0;

			try {
				IField[] fields = ((IType) metricElement.getJavaElement())
						.getFields();
				for (int i = 0; i < fields.length; i++) {
					if ((fields[i].getFlags() & Flags.AccStatic) != 0) {
						sFieldNumber++;
					} else {
						iFieldNumber++;
					}
				}

			} catch (JavaModelException e) {
				logger.setLevel(Level.WARNING);
				logger
						.log(Level.SEVERE, "Counting the number of fields....",
								e);
			}

			values[0].setValue(sFieldNumber + iFieldNumber);
			values[1].setValue(iFieldNumber);
			values[2].setValue(sFieldNumber);
		}
	}

	@Override
	public void analyze(AbstractMetricElement source,
			final List<MathOperator> mathOperators) {
		
		MetricElementVisitor visitor = new MetricElementVisitor() {

			@Override
			public boolean visit(TypeMetric metricElement) {
				BasicMetric metric = new BasicMetric(metricElement);
				metric.setValuesToMetricElement();
				return false;
			}

			@Override
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
