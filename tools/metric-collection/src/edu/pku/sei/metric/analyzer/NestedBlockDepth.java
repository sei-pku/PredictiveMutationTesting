package edu.pku.sei.metric.analyzer;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.MethodMetric;

/**
 * Count the deepest block depth
 * 
 * @author PCT
 * 
 */
public class NestedBlockDepth extends Analyzer {

	private static String NBD;

	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics.size() == 1 : "There must be 1 child as the extension said";
		NBD = metrics.get(0).getMetricName();
	}

	private static class BasicMetric extends Metric {

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(NBD, 0) };
		}

		@Override
		protected void calculate() {
			assert source instanceof MethodMetric : "The calculation is set on method";
			MethodDeclaration astNode = (MethodDeclaration) source.getASTNode();

			if (astNode == null) {
				return;
			} else {
				Block body = astNode.getBody();
				if (body == null) {
					return;
				} else {
					LevelCounter lc = new LevelCounter();
					astNode.accept(lc);
					values[0].setValue(lc.maxDepth);
				}
			}
		}

		private static class LevelCounter extends ASTVisitor {

			int maxDepth = 0;

			int depth = 0;

			public boolean visit(Block node) {
				depth++;
				return true;
			}

			public void endVisit(Block node) {
				if (depth > maxDepth) {
					maxDepth = depth;
				}
				depth--;
			}
		}
	}

	@Override
	public void analyze(AbstractMetricElement ame,
			final List<MathOperator> mathOperators) {
		MetricElementVisitor visitor = new MetricElementVisitor() {

			@Override
			public boolean visit(MethodMetric metricElement) {
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
		ame.accept(visitor);
	}

}
