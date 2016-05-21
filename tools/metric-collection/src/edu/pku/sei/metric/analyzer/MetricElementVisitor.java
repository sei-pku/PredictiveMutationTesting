package edu.pku.sei.metric.analyzer;

import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.AnnotationTypeMetric;
import edu.pku.sei.metric.source.ClassFileMetric;
import edu.pku.sei.metric.source.CompilationUnitMetric;
import edu.pku.sei.metric.source.EnumMetric;
import edu.pku.sei.metric.source.MethodMetric;
import edu.pku.sei.metric.source.PackageFragmentMetric;
import edu.pku.sei.metric.source.PackageFragmentRootMetric;
import edu.pku.sei.metric.source.ProjectMetric;
import edu.pku.sei.metric.source.TypeMetric;

/**
 * AbstractMetricElement tree visitor. The visitor was designed to acquire the
 * fact for source code metric
 * 
 * @author zhanglm07
 * 
 */
public abstract class MetricElementVisitor {

	public boolean visit(ProjectMetric metricElement) {
		return true;
	}

	public boolean visit(PackageFragmentRootMetric metricElement) {
		return true;
	}

	public boolean visit(PackageFragmentMetric metricElement) {
		return true;
	}

	public boolean visit(CompilationUnitMetric metricElement) {
		return true;
	}

	public boolean visit(TypeMetric metricElement) {
		return true;
	}

	public boolean visit(EnumMetric metricElement) {
		return true;
	}

	public boolean visit(AnnotationTypeMetric metricElement) {
		return false;
	}

	public boolean visit(MethodMetric metricElement) {
		return false;
	}

	public boolean visit(ClassFileMetric metricElement) {
		return false;
	}

	public void postVisit(AbstractMetricElement metricElement) {

	}

}
