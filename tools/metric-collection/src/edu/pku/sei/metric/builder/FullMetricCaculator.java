package edu.pku.sei.metric.builder;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;

import edu.pku.sei.metric.Activator;
import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.source.AbstractMetricElement;

/**
 * Calculate all the implemented source code metric analyzer
 * 
 * @author PCT
 * 
 */
public class FullMetricCaculator {
	
	private static final BundleAnalyzer analyzer = new BundleAnalyzer(Activator.analyzers, Activator.operators);

	
	/**
	 * When single Java element was selected.
	 * @param javaElement
	 * @param monitor
	 * @return
	 */
	public static AbstractMetricElement calculate(IJavaElement javaElement,
			IProgressMonitor monitor) {
		MetricElementBuilder builder = new MetricElementBuilder();
		AbstractMetricElement metricElement = builder.build(javaElement, analyzer, monitor);
		return metricElement;
	}

	/**
	 * When Multiple Java element are selected. Need a FakeElement as the root node 
	 * 
	 * @param elementList
	 * @param parent
	 * @param monitor
	 * @return
	 */
	public static AbstractMetricElement calculate(
			List<?> elementList, AbstractMetricElement parent,
			IProgressMonitor monitor) {
		MetricElementBuilder builder = new MetricElementBuilder();
		monitor.beginTask("calculate", elementList.size());
		for (Iterator<?> iter = elementList.iterator(); iter
				.hasNext();) {
			IJavaElement element = (IJavaElement) iter.next();
			AbstractMetricElement metricElement = builder.build(element, analyzer, monitor);
			parent.addChild(metricElement);
			
			monitor.worked(1);
		}		
		return parent;
	}
}
