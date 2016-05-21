package edu.pku.sei.metric.analyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.PackageFragmentMetric;

/**
 * Do Robert Martin Metric
 * 
 * @author PCT
 * 
 */
public class RMartinCouplings extends Analyzer {

	private static String RMD;

	private static String CA;

	private static String CE;

	private static String RMI;

	private static String RMA;

	private Logger logger = Logger.getLogger(RMartinCouplings.class.getName());

	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics != null && metrics.size() == 5;
		RMD = metrics.get(0).getMetricName();
		CA = metrics.get(1).getMetricName();
		CE = metrics.get(2).getMetricName();
		RMI = metrics.get(3).getMetricName();
		RMA = metrics.get(4).getMetricName();
	}

	private class BasicMetric extends Metric {

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(RMD, 0),
					new MetricValue(CA, 0), new MetricValue(CE, 0),
					new MetricValue(RMI, 0), new MetricValue(RMA, 0) };
		}

		@Override
		protected void calculate() {
			assert source instanceof PackageFragmentMetric : "The calculation is set on Package";

			PackageFragmentMetric metricElement = (PackageFragmentMetric) source;

			double afferentCoupling = calculateAfferentCoupling(metricElement);
			double efferentCoupling = calculateEfferentCoupling(metricElement);
			double abstractness = calculateAbstractness(metricElement);
			double instability = calculateInstability(afferentCoupling,
					efferentCoupling);
			double distance = calculateDistance(abstractness, instability);

			values[0].setValue(distance);
			values[1].setValue(afferentCoupling);
			values[2].setValue(efferentCoupling);
			values[3].setValue(instability);
			values[4].setValue(abstractness);
		}

		/**
		 * Calculates the normalized distance (Dn) of this package from the main
		 * sequence, defined as |(A+I-1)|
		 * 
		 * @param source
		 */
		private double calculateDistance(double a, double i) {
			return Math.abs(a + i - 1);
		}

		/**
		 * Calculates the number of abstract types (including interfaces)
		 * divided by the total number of types in this package
		 * 
		 * @param source
		 */
		private double calculateAbstractness(PackageFragmentMetric source) {
			try {
				IPackageFragment p = (IPackageFragment) source.getJavaElement();
				ICompilationUnit[] units = p.getCompilationUnits();
				double allTypes = 0;
				double abstractTypes = 0;
				for (int u = 0; u < units.length; u++) {
					IType types[] = units[u].getAllTypes();
					allTypes += types.length;
					for (int t = 0; t < types.length; t++) {
						if (types[t].isInterface()) {
							abstractTypes++;
						} else {
							int flags = types[t].getFlags();
							if (Flags.isAbstract(flags)
									&& Flags.isPublic(flags))
								abstractTypes++;
						}
					}
				}
				return abstractTypes / allTypes;
			} catch (JavaModelException e) {
				logger.setLevel(Level.WARNING);
				logger.log(Level.SEVERE, "Doing Abstractness metric.....", e);
				return 0;
			}
		}

		
		private double calculateInstability(double ca, double ce) {
			if (ca == 0.0) {
				return 1.0;
			} else {
				return ce / (ca + ce);
			}
		}

		private double calculateAfferentCoupling(PackageFragmentMetric source) {
			IPackageFragment pf = (IPackageFragment) source.getJavaElement();
			if (!pf.isDefaultPackage()) {
				try {
					SearchPattern pattern = SearchPattern.createPattern(source
							.getJavaElement(), IJavaSearchConstants.REFERENCES);

					IJavaSearchScope scope = createProjectSearchScope(source
							.getJavaElement());
					SearchEngine searchEngine = new SearchEngine();
					AfferentCollector c = new AfferentCollector(source);
					searchEngine.search(pattern,
							new SearchParticipant[] { SearchEngine
									.getDefaultSearchParticipant() }, scope, c,
							null);
					return c.getResult();
				} catch (CoreException e) {
					logger.setLevel(Level.WARNING);
					logger.log(Level.SEVERE,
							"Calculating AfferentCoupling metric....", e);
					return 0;
				}
			} else {
				return 0;
			}
		}
	
		private IJavaSearchScope createProjectSearchScope(IJavaElement element)
				throws JavaModelException {
			IJavaProject p = (IJavaProject) element
					.getAncestor(IJavaElement.JAVA_PROJECT);
			List<IJavaElement> scopeElements = new ArrayList<IJavaElement>();
			addPackagesInScope(p, scopeElements);
			// find referencing projects and add their packages if any
			IProject[] refProjects = p.getProject().getReferencingProjects();
			if ((refProjects != null) && (refProjects.length > 0)) {
				for (int i = 0; i < refProjects.length; i++) {
					IJavaProject next = JavaCore.create(refProjects[i]);
					if (next != null) {
						addPackagesInScope(next, scopeElements);
					}
				}
			}
			// don't include the package under investigation!
			scopeElements.remove(element);
			return SearchEngine.createJavaSearchScope(scopeElements
					.toArray(new IJavaElement[] {}));
		}

		private void addPackagesInScope(IJavaProject project,
				List<IJavaElement> scope) throws JavaModelException {
			IPackageFragment[] packages = project.getPackageFragments();
			for (int i = 0; i < packages.length; i++) {
				if (packages[i].getKind() != IPackageFragmentRoot.K_BINARY)
					scope.add(packages[i]);
			}
		}

		private double calculateEfferentCoupling(PackageFragmentMetric source) {
			SearchPattern pattern = SearchPattern.createPattern("*",
					IJavaSearchConstants.PACKAGE,
					IJavaSearchConstants.REFERENCES,
					SearchPattern.R_PATTERN_MATCH);
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(new IJavaElement[] { source
							.getJavaElement() });
			SearchEngine searchEngine = new SearchEngine();
			try {
				EfferentCollector c = new EfferentCollector(source);
				searchEngine.search(pattern,
						new SearchParticipant[] { SearchEngine
								.getDefaultSearchParticipant() }, scope, c,
						null);
				return c.getResult();
			} catch (CoreException e) {
				logger.setLevel(Level.WARNING);
				logger.log(Level.SEVERE,
						"Calculating Efferent Coupling Metric....", e);
				return 0;
			}
		}
		
		private class AfferentCollector extends SearchRequestor {

			private IJavaSearchScope packageScope;

			private Set<String> results = null;

			private double result = 0;

			private Set<String> packages = null;

			public AfferentCollector(PackageFragmentMetric source) {
				packageScope = SearchEngine
						.createJavaSearchScope(new IJavaElement[] { source
								.getJavaElement() });
			}

			public double getResult() {
				return result;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.search.SearchRequestor#beginReporting()
			 */
			public void beginReporting() {
				results = new HashSet<String>();
				packages = new HashSet<String>();
			}

			public void acceptSearchMatch(SearchMatch match)
					throws CoreException {
				IJavaElement enclosingElement = (IJavaElement) match
						.getElement();
				if ((enclosingElement != null)
						&& (!packageScope.encloses(enclosingElement))) {
					IJavaElement pkg = enclosingElement
							.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
					results.add(match.getResource().getFullPath().toString());
					packages.add(pkg.getElementName());
				}
			}

			public void endReporting() {
				result = results.size();
			}

		}

		private class EfferentCollector extends SearchRequestor {

			double result = 0;

			Set<String> results = null;

			Set<String> packages = null;

			//PackageFragmentMetric source = null;

			public EfferentCollector(PackageFragmentMetric source) {
				//this.source = source;
			}

			/**
			 * @return
			 */
			public double getResult() {
				return result;
			}

			public void beginReporting() {
				results = new HashSet<String>();
				packages = new HashSet<String>();
			}

			private String getPackageName(IJavaElement enclosingElement,
					int start, int end) {
				if (enclosingElement.getElementType() == IJavaElement.IMPORT_DECLARATION) {
					String name = enclosingElement.getElementName();
					int lastDot = name.lastIndexOf('.');
					return name.substring(0, lastDot);
				} else {
					ICompilationUnit unit = (ICompilationUnit) enclosingElement
							.getAncestor(IJavaElement.COMPILATION_UNIT);
					try {
						String source = unit.getSource();
						return source.substring(start, end);
					} catch (JavaModelException e) {
						return null;
					}
				}
			}

			public void acceptSearchMatch(SearchMatch match)
					throws CoreException {
				int start = match.getOffset();
				int end = start + match.getLength();
				if (match.getElement() != null) {
					// don't count references to standard java(x) API
					try {
						String name = getPackageName((IJavaElement) match
								.getElement(), start, end);
						if (!name.startsWith("java")) {
							results.add(match.getResource().getFullPath()
									.toString());
							packages.add(name);
						}
					} catch (StringIndexOutOfBoundsException x) {
					}
				}
			}

			public void endReporting() {
				result = results.size();
				// source.setEfferentDependencies(packages);
			}
		}
	}

	@Override
	public void analyze(AbstractMetricElement source,
			final List<MathOperator> mathOperators) {

		MetricElementVisitor visitor = new MetricElementVisitor() {

			@Override
			public boolean visit(PackageFragmentMetric metricElement) {
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
