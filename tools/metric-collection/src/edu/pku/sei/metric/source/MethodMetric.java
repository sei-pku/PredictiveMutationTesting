package edu.pku.sei.metric.source;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;

/**
 * @author PCT
 *
 */
public class MethodMetric extends AbstractMetricElement {

	private MethodDeclaration methodDeclaration;
	public int startLine;
	public int endLine;

	public MethodMetric(String handle, MethodDeclaration methodDeclaration, BundleAnalyzer analyzer) {
		super(handle, analyzer);
		this.methodDeclaration = methodDeclaration;
		
		analyzer.analyze(this);
		this.methodDeclaration = null;
	}


	@Override
	public ASTNode getASTNode() {
		return methodDeclaration;
	}

	@Override
	public int getLevel() {
		return METHOD;
	}

	@Override
	public void accept(MetricElementVisitor visitor) {
		visitor.visit(this);
		visitor.postVisit(this);
	}

	@Override
	protected void initChildren(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

}
