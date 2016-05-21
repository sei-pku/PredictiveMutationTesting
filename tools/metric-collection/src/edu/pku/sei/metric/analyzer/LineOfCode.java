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
import edu.pku.sei.metric.source.CompilationUnitMetric;

/**
 * Calculate the source code line length
 * 
 * @author PCT
 * 
 */
public class LineOfCode extends Analyzer {
	// line of code
	private String LOC;

	// line of code and comment
	private String LOCC;

	// line of gross code
	private String LOGC;

	private Logger logger = Logger.getLogger(LineOfCode.class.getName());

	public void setMetrics(List<MetricValueDescriptor> metrics) {
		super.setMetrics(metrics);
		assert metrics.size() == 3 : "There must be 3 children as the extension said";
		LOC = metrics.get(0).getMetricName();
		LOCC = metrics.get(1).getMetricName();
		LOGC = metrics.get(2).getMetricName();
	}

	@Override
	public void analyze(AbstractMetricElement source,
			final List<MathOperator> mathOperators) {
		
		MetricElementVisitor visitor = new MetricElementVisitor() {
		
			public boolean visit(CompilationUnitMetric metricElement) {
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

	private class BasicMetric extends Metric {
		public BasicMetric(AbstractMetricElement source) {
			super(source);
			values = new MetricValue[] { new MetricValue(LOC, 0),
					new MetricValue(LOCC, 0), new MetricValue(LOGC, 0) };
		}

		@Override
		protected void calculate() {
			try {
				String sourceText = getSource(source).trim();
				Set<Integer> lineSet = new HashSet<Integer>();
				int commentLines = 0;
				int totalLines = 0;
				IScanner scanner = ToolFactory.createScanner(true, false, true,
						true);
				scanner.setSource(sourceText.toCharArray());
				try {
					while (true) {
						int token = scanner.getNextToken();
						if (token == ITerminalSymbols.TokenNameEOF) {
							int startPosition = scanner
									.getCurrentTokenStartPosition();
							totalLines = scanner.getLineNumber(startPosition);
							break;
						}

						if ((token == ITerminalSymbols.TokenNameCOMMENT_LINE)
								|| (token == ITerminalSymbols.TokenNameCOMMENT_BLOCK)
								|| (token == ITerminalSymbols.TokenNameCOMMENT_JAVADOC)) {
							commentLines++;
						} else {
							int startPosition = scanner
									.getCurrentTokenStartPosition();
							int lineNumber = scanner
									.getLineNumber(startPosition);
							lineSet.add(Integer.valueOf(lineNumber));
						}
					}
				} catch (InvalidInputException e) {
					e.printStackTrace();
				}
				values[0].setValue(lineSet.size());
				values[1].setValue(commentLines + lineSet.size());
				values[2].setValue(totalLines);

			} catch (JavaModelException e) {
				logger.setLevel(Level.WARNING);
				logger.log(Level.SEVERE, "Couting lines....", e);
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
	}
}
