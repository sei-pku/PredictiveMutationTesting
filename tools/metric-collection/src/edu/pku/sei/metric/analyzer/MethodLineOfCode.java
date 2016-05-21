package edu.pku.sei.metric.analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.MethodMetric;

/**
 * Calculate the source code line length
 * 
 * @author PCT
 * 
 */
public class MethodLineOfCode extends Analyzer {

	private static String MLOC;

	private Logger logger = Logger.getLogger(MethodLineOfCode.class.getName());

	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics.size() == 1 : "There must be 1 child as the extension said";
		MLOC = metrics.get(0).getMetricName();
	}

	private class BasicMetric extends Metric {

		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(MLOC, 0) };
		}

		@Override
		protected void calculate() {
			if (source.getASTNode() != null) {
				try {
					String sourceText = getSource(source);
					values[0].setValue(calculateNumberOfLines(sourceText));
				} catch (JavaModelException e) {
					logger.setLevel(Level.WARNING);
					logger.log(Level.SEVERE, "Do methodlineofcode calculation",
							e);
				}
			} else {
				logger
						.warning("Error in AbstractLinesOfCode: no AstNode associated with Level("
								+ source.getLevel() + ")");
			}
		}

		private String getSource(AbstractMetricElement source)
				throws JavaModelException {
			ASTNode astNode = source.getASTNode();
			int start = astNode.getStartPosition();
			int length = astNode.getLength();
			ICompilationUnit unit = source.getCompilationUnit();
			String s = unit.getSource();
			return s.substring(start, start + length);
		}

		private int calculateNumberOfLines(String source) {
			String sourceToAnalyze = source.trim();
			Set<Integer> lineSet = new HashSet<Integer>();
			IScanner scanner = ToolFactory.createScanner(false, false, true,
					true);
			scanner.setSource(sourceToAnalyze.toCharArray());
			try {
				while (true) {
					int token = scanner.getNextToken();
					if (token == ITerminalSymbols.TokenNameEOF)
						break;
					int startPosition = scanner.getCurrentTokenStartPosition();
					int lineNumber = scanner.getLineNumber(startPosition);
					lineSet.add(Integer.valueOf(lineNumber));
				}
			} catch (InvalidInputException e) {
				e.printStackTrace();
			}
			return lineSet.size();
		}
	}

	@Override
	public void analyze(AbstractMetricElement source,
			final List<MathOperator> mathOperators) {
		
		MetricElementVisitor visitor = new MetricElementVisitor() {
		
			public boolean visit(MethodMetric metricElement) {
				metricCodeLength(metricElement);
				return false;
			}

			private void metricCodeLength(AbstractMetricElement metricElement) {
				BasicMetric metric = new BasicMetric(metricElement);
				metric.setValuesToMetricElement();
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
