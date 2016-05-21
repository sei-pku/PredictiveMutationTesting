package edu.pku.sei.metric.analyzer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.TypeMetric;

/**
 * Count the number of different kind of method declarations
 * 
 * @author PCT
 * 
 */
public class NumberOfMethods extends Analyzer {

	private static String NOM;

	private static String NOSM;

	private static String NOIM;

	private Logger logger = Logger.getLogger(NumberOfMethods.class.getName());

	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics.size() == 3 : "There must be 3 children as the extension said";
		NOM = metrics.get(0).getMetricName();
		NOSM = metrics.get(1).getMetricName();
		NOIM = metrics.get(2).getMetricName();
	}

	private class BasicMetric extends Metric {

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(NOM, 0),
					new MetricValue(NOSM, 0), new MetricValue(NOIM, 0) };
		}

		@Override
		protected void calculate() {
			assert source instanceof TypeMetric : "The calculation is set on Type";
			TypeMetric metricElement = (TypeMetric) source;

			int sMethodCount = 0;
			int iMethodCount = 0;
			try {
				IMethod[] methods = ((IType) metricElement.getJavaElement())
						.getMethods();
				for (int i = 0; i < methods.length; i++) {
					if ((methods[i].getFlags() & Flags.AccStatic) != 0) {
						sMethodCount++;
					} else {
						iMethodCount++;
					}
				}
			} catch (JavaModelException e) {
				logger.setLevel(Level.WARNING);
				logger.log(Level.SEVERE, "Calculating NumberOfMethods....", e);
			}

			values[0].setValue(sMethodCount + iMethodCount);
			values[1].setValue(sMethodCount);
			values[2].setValue(iMethodCount);
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
