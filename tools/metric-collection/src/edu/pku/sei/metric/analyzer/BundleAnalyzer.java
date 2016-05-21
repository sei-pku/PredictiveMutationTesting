package edu.pku.sei.metric.analyzer;

import java.util.List;

import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;

/**
 * BundleAnalyzer is used to wrap the list of metric analyzers and math
 * operators
 * 
 * @author zhanglm07
 */
public class BundleAnalyzer {

	private List<Analyzer> analyzers;

	private List<MathOperator> mathOperators;

	public BundleAnalyzer(List<Analyzer> analyzers, List<MathOperator> operators) {
		this.analyzers = analyzers;
		this.mathOperators = operators;
	}

	/**
	 * Analyzer the input element and set the metric result to it.
	 * 
	 * @param source
	 */
	public void analyze(AbstractMetricElement source) {
		for (int i = 0; i < analyzers.size(); i++) {
			analyzers.get(i).analyze(source, mathOperators);
		}

	}
}
