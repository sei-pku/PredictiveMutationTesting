package edu.pku.sei.metric.builder;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;

import edu.pku.sei.metric.Constants;
import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.ClassFileMetric;
import edu.pku.sei.metric.source.CompilationUnitMetric;
import edu.pku.sei.metric.source.PackageFragmentMetric;
import edu.pku.sei.metric.source.PackageFragmentRootMetric;
import edu.pku.sei.metric.source.ProjectMetric;

/**
 * Wrap a JavaElement to an AbstractMetricElement tree
 *
 * @author PCT
 *
 */
public class MetricElementBuilder implements Constants {

	/**
	 * Build an AbstractMetricElement tree
	 *
	 * @param javaElement
	 * @return the AbstractMetricElement
	 */
	public AbstractMetricElement build(IJavaElement javaElement,
			BundleAnalyzer analyzer, IProgressMonitor monitor) {
		AbstractMetricElement result = null;
		if (javaElement.getElementType() == IJavaElement.JAVA_PROJECT)
			result = new ProjectMetric(javaElement.getHandleIdentifier(),
					analyzer, monitor);
		else if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT)
			result = new PackageFragmentRootMetric(javaElement
					.getHandleIdentifier(), analyzer, monitor);
		else if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
			result = new PackageFragmentMetric(javaElement
					.getHandleIdentifier(), analyzer, monitor);
		else if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT)
			result = new CompilationUnitMetric(javaElement
					.getHandleIdentifier(), analyzer);
		else if (javaElement.getElementType() == IJavaElement.CLASS_FILE)
			result = new ClassFileMetric(javaElement.getHandleIdentifier(),
					analyzer);
		return result;
	}
}
