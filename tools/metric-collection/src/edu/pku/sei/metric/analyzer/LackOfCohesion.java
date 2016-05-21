package edu.pku.sei.metric.analyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.TypeMetric;

/**
 * Calculating the Lack Of Cohesion OO metric
 * 
 * @author zhanglm07
 * 
 */
public class LackOfCohesion extends Analyzer {

	private static String LCOM;

	private static Logger logger = Logger.getLogger(LackOfCohesion.class
			.getName());

	@Override
	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		logger.setLevel(Level.WARNING);
		assert metrics.size() == 1 : "There must be one child as the extension said";
		LCOM = metrics.get(0).getMetricName();
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

	/**
	 * @author hyy
	 * 
	 */
	private static class BasicMetric extends Metric {

		private Prefs prefs = new Prefs();

		private HashMap<String, Set<String>> buckets = new HashMap<String, Set<String>>();

		/**
		 * for configuration
		 */
		private static class Prefs {
			public Prefs() {
			}

			public boolean countStaticMethods() {
				return true;
			}

			public boolean countStaticAttributes() {
				return true;
			}
		}

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			this.values = new MetricValue[] { new MetricValue(LCOM, 0) };
		}

		@Override
		protected void calculate() {
			double value = 0;
			try {
				IType type = (IType) source.getJavaElement();
				IMethod[] methods = type.getMethods();
				IField[] fields = type.getFields();
				if ((fields.length > 1) && (methods.length > 1)) {
					initBuckets(fields);
					if (buckets.size() > 0) {
						visitMethods(methods);
						value = calculateResult();
					}
				}
			} catch (JavaModelException e) {
				logger.log(Level.SEVERE,
						"Caculating LackOfCohesion metric....", e);
			}
			values[0].setValue(value);
		}

		private void add(String field, String method) {
			if (buckets.containsKey(field)) {
				Set<String> methods = buckets.get(field);
				methods.add(method);
			}
		}

		/**
		 * @return double (avg(m(a)) - m)/(1 - m) where m(a) is the number of
		 *         methods that access a
		 */
		private double calculateResult() {
			int sum = 0;
			int a = 0;
			Set<String> allMethods = new HashSet<String>();
			for (Iterator<Set<String>> i = buckets.values().iterator(); i
					.hasNext(); a++) {
				Set<String> methods = i.next();
				allMethods.addAll(methods);
				sum += methods.size();
			}
			int m = allMethods.size();
			if (m == 1)
				return 0;
			double avg = (double) sum / (double) a;
			return Math.abs((avg - m) / (1 - m));
		}

		private void visitMethods(IMethod[] methods) {
			boolean countStatics = getPrefs().countStaticMethods();
			for (int i = 0; i < methods.length; i++) {
				String methodName = methods[i].getElementName();
				try {
					if ((countStatics)
							|| ((methods[i].getFlags() & Flags.AccStatic) == 0)) {
						IScanner s = ToolFactory.createScanner(false, false,
								false, false);
						s.setSource(methods[i].getSource().toCharArray());
						while (true) {
							int token = s.getNextToken();
							if (token == ITerminalSymbols.TokenNameEOF)
								break;
							if (token == ITerminalSymbols.TokenNameIdentifier) {
								add(new String(s.getCurrentTokenSource()),
										methodName);
							}
						}
					}
				} catch (JavaModelException e) {
					logger.log(Level.SEVERE,
							"Caculating LackOfCohesion metric....", e);
				} catch (InvalidInputException e) {
					logger.log(Level.SEVERE,
							"Caculating LackOfCohesion metric....", e);
				}
			}
		}

		/**
		 * create a map of HashSets to store methods for each attribute. Ask
		 * preferences whether static attributes have to be considered
		 * 
		 * @param fields
		 */
		private void initBuckets(IField[] fields) {
			buckets.clear(); 
			try {
				boolean countStatics = getPrefs().countStaticAttributes();
				for (int i = 0; i < fields.length; i++) {
					if (countStatics
							|| ((fields[i].getFlags() & Flags.AccStatic) == 0)) {
						buckets.put(fields[i].getElementName(),
								new HashSet<String>());
					}
				}
			} catch (JavaModelException e) {
				logger.log(Level.SEVERE,
						"Caculating LackOfCohesion metric....", e);
			}
		}

		public Prefs getPrefs() {
			if (prefs == null) {
				prefs = new Prefs();
			}
			return prefs;
		}

	}

}
