package edu.pku.sei.metric.source;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;

import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;

/**
 * Annotation code metric is not very clear yet. Currently, we treat it
 * similarly to TypeMetric and apply lineOfCode related metric to it. Annotation
 * is at the same level of Type, but have no children element.
 * 
 * @author PCT
 * 
 */
public class AnnotationTypeMetric extends AbstractMetricElement {

	private AnnotationTypeDeclaration annotationDeclaration;

	private Logger logger = Logger.getLogger(AnnotationTypeMetric.class
			.getName());

	public AnnotationTypeMetric(String handle,
			AnnotationTypeDeclaration annotationDeclaration, BundleAnalyzer analyzer) {
		super(handle, analyzer);
		this.annotationDeclaration = annotationDeclaration;
		this.logger.setLevel(Level.WARNING);
		initChildren(null);
		
		analyzer.analyze(this);
		this.annotationDeclaration = null;
	}

	@Override
	protected void initChildren(IProgressMonitor monitor) {

	}

	@Override
	public ASTNode getASTNode() {
		return annotationDeclaration;
	}

	@Override
	public int getLevel() {
		return TYPE;
	}

	@Override
	public void accept(MetricElementVisitor visitor) {
		visitor.visit(this);
		visitor.postVisit(this);
	}

}
