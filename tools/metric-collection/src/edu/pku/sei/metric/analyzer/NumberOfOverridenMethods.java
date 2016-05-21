package edu.pku.sei.metric.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.TypeMetric;

/**
 * Count the number of overridden methods
 * 
 * @author PCT
 * 
 */
public class NumberOfOverridenMethods extends Analyzer {

	private static String NORM;

	private Logger logger = Logger.getLogger(NumberOfOverridenMethods.class
			.getName());

	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics.size() == 1 : "There must be 1 child as the extension said";
		NORM = metrics.get(0).getMetricName();
	}

	private class BasicMetric extends Metric {

		private Prefs prefs = new Prefs();

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(NORM, 0) };
		}

		@Override
		protected void calculate() {
			assert source instanceof TypeMetric : "The calculation is set on Type";
			TypeMetric metricElement = (TypeMetric) source;

			IType iType = (IType) metricElement.getJavaElement();
			ITypeHierarchy hierarchy = metricElement.getHierarchy();
			IType[] supers = hierarchy.getAllSuperclasses(iType);
			
			int overridden = 0;
			try {
				IMethod[] myMethods = iType.getMethods();
				List<IMethod> counted = new ArrayList<IMethod>();
				for (int m = 0; m < myMethods.length; m++) {
					IMethod myMethod = myMethods[m];
					// don't consider methods excluded by preferences
					if (getPrefs().countMethod(myMethod.getElementName())) {
						overridden = countMethods(supers, overridden, counted,
								myMethod);
					}
				}
			} catch (JavaModelException e) {
				logger.setLevel(Level.WARNING);
				logger.log(Level.SEVERE,
						"Counting number of overriden methods...", e);
			}

			values[0].setValue(overridden);
		}

		private int countMethods(IType[] supers, int overridden,
				List<IMethod> counted, IMethod myMethod)
				throws JavaModelException {
			for (int s = 0; s < supers.length; s++) {
				IMethod[] inheritedMethods = supers[s].getMethods();
				for (int sm = 0; sm < inheritedMethods.length; sm++) {
					if (counted.contains(myMethod))
						continue;
					IMethod inherited = inheritedMethods[sm];
					int inheritedFlags = inherited.getFlags();
					// don't have to consider static methods
					if ((inheritedFlags & Flags.AccStatic) != 0)
						continue;
					// don't have to consider private methods
					if ((inheritedFlags & Flags.AccPrivate) != 0)
						continue;
					// don't count abstract methods unless preferences dictate
					// it
					if ((!getPrefs().countAbstract())
							&& ((inheritedFlags & Flags.AccAbstract) != 0))
						continue;
					// methods must have same signature and return type
					if (!inherited.isSimilar(myMethod))
						continue;
					// don't count methods invoking super unless preferences
					// override
					if ((getPrefs().countSuper())
							|| (!containsSuperCall(myMethod))) {
						overridden++;
						counted.add(myMethod);
					}
				}
			}
			return overridden;
		}

		private boolean containsSuperCall(IMethod myMethod) {
			try {
				String source = myMethod.getSource();
				int indexOfSuper = source.indexOf("super.");
				if (indexOfSuper == -1)
					return false;
				String rest = source.substring(indexOfSuper + 6);
				return rest.startsWith(myMethod.getElementName());
			} catch (JavaModelException e) {
				return false;
			}
		}

		public Prefs getPrefs() {
			if (prefs == null) {
				prefs = new Prefs();
			}
			return prefs;
		}

		private class Prefs {

			public Prefs() {
			}

			public boolean countAbstract() {
				return true;
			}

			public boolean countSuper() {
				return true;
			}

			public boolean countMethod(String name) {
				return true;
			}
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
