package edu.pku.sei.metric.source;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;

/**
 * Enum metric is not very clear yet. Currently, we treat it similarly to
 * TypeMetric and apply lineOfCode related metric to it. Enum is at the same
 * level of Type and also have some children as MethodMetric.
 * 
 * @author PCT
 * 
 */
public class EnumMetric extends AbstractMetricElement {

	private EnumDeclaration enumDeclaration;

	private Logger logger = Logger.getLogger(TypeMetric.class.getName());

	public EnumMetric(String handle, EnumDeclaration enumDeclaration, BundleAnalyzer analyzer) {
		super(handle, analyzer);
		this.enumDeclaration = enumDeclaration;
		this.logger.setLevel(Level.WARNING);
		initChildren(null);
		
		analyzer.analyze(this);
		this.enumDeclaration = null;
	}

	@Override
	protected void initChildren(IProgressMonitor monitor) {
		IType type = (IType) getJavaElement();
		IMethod[] ms = null;
		try {
			ms = type.getMethods();
		} catch (JavaModelException e) {
			logger.log(Level.SEVERE, "Fail to get " + type.getElementName()
					+ "'s methods...", e);
		}
		MethodDeclaration[] methods = getMethods(enumDeclaration);
		for (int i = 0; i < methods.length; i++) {
			MethodDeclaration lastMethod = methods[i];
			ASTNode node = lastMethod.getParent();
			while (node.getNodeType() != ASTNode.COMPILATION_UNIT) {
				node = node.getParent();
			}
			CompilationUnit t = (CompilationUnit) node;
			MethodMetric mm = new MethodMetric(ms[i].getHandleIdentifier(),
					lastMethod, analyzer);
			mm.startLine = t.getLineNumber(lastMethod.getStartPosition());
			mm.endLine = t.getLineNumber(lastMethod.getStartPosition() + lastMethod.getLength());
			addChild(mm);
		}
	}

	/**
	 * Get {@link EnumDeclaration}'s {@link MethodDeclaration}
	 * 
	 * @param enumDeclaration
	 * @return
	 */
	private MethodDeclaration[] getMethods(EnumDeclaration enumDeclaration) {
		List<?> bd = enumDeclaration.bodyDeclarations();
		int methodCount = 0;
		for (Iterator<?> it = bd.listIterator(); it.hasNext();) {
			if (it.next() instanceof MethodDeclaration) {
				methodCount++;
			}
		}
		MethodDeclaration[] methods = new MethodDeclaration[methodCount];
		int next = 0;
		for (Iterator<?> it = bd.listIterator(); it.hasNext();) {
			Object decl = it.next();
			if (decl instanceof MethodDeclaration) {
				methods[next++] = (MethodDeclaration) decl;
			}
		}
		return methods;
	}

	@Override
	public ASTNode getASTNode() {
		return enumDeclaration;
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
