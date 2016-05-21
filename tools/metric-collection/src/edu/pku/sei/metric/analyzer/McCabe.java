package edu.pku.sei.metric.analyzer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.MethodMetric;

/**
 * Do McCabe complexity metric
 * 
 * @author zhanglm07
 * 
 */
public class McCabe extends Analyzer {

	private static String MCCABE;

	private Logger logger = Logger.getLogger(McCabe.class.getName());

	@Override
	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics != null && metrics.size() == 1;
		MCCABE = metrics.get(0).getMetricName();
	}

	private class BasicMetric extends Metric {

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(MCCABE, 0) };
		}

		@Override
		protected void calculate() {
			assert source instanceof MethodMetric : "The calculation is set on method";
			MethodDeclaration astNode = (MethodDeclaration) source.getASTNode();
			if (astNode == null) {
				return;
			}
			Block body = astNode.getBody();
			if (body == null) {
				return;
			}
			String sourceCode = null;
			try {
				sourceCode = source.getCompilationUnit().getSource();
			} catch (JavaModelException e) {
				logger.setLevel(Level.WARNING);
				logger.log(Level.SEVERE, "Do McCabe calculation..", e);
			}
			McCabeVisitor mcb = new McCabeVisitor(sourceCode);
			astNode.accept(mcb);
			values[0].setValue(mcb.getCyclomatic());
		}
	}

	@Override
	public void analyze(AbstractMetricElement source,
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
		source.accept(visitor);
	}

	private static class McCabeVisitor extends ASTVisitor {

		private int cyclomatic = 1;

		private String source;

		McCabeVisitor(String source) {
			this.source = source;
		}

		public boolean visit(CatchClause node) {
			cyclomatic++;
			return true;
		}

		public boolean visit(ConditionalExpression node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}

		public boolean visit(DoStatement node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}

		public boolean visit(ForStatement node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}

		public boolean visit(IfStatement node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}

		public boolean visit(SwitchCase node) {
			if (!node.isDefault())
				cyclomatic++;
			return true;
		}

		public boolean visit(WhileStatement node) {
			cyclomatic++;
			inspectExpression(node.getExpression());
			return true;
		}

		public boolean visit(ExpressionStatement exs) {
			inspectExpression(exs.getExpression());
			return false;
		}

		public int getCyclomatic() {
			return cyclomatic;
		}

		private void inspectExpression(Expression ex) {
			if ((ex != null) && (source != null)) {
				int start = ex.getStartPosition();
				int end = start + ex.getLength();
				String expression = source.substring(start, end);
				char[] chars = expression.toCharArray();
				for (int i = 0; i < chars.length - 1; i++) {
					char next = chars[i];
					if ((next == '&' || next == '|') && (next == chars[i + 1])) {
						cyclomatic++;
					}
				}
			}
		}
	}
}
