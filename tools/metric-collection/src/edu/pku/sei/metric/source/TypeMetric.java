package edu.pku.sei.metric.source;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;

/**
 * @author PCT
 * 
 */
public class TypeMetric extends AbstractMetricElement {

	private TypeDeclaration typeDeclaration;

	private ITypeHierarchy hierarchy;

	private Logger logger = Logger.getLogger(TypeMetric.class.getName());

	public TypeMetric(String handle, TypeDeclaration typeDeclaration, BundleAnalyzer analyzer) {
		super(handle, analyzer);
		this.typeDeclaration = typeDeclaration;
		this.logger.setLevel(Level.WARNING);
		initChildren(null);
		
		analyzer.analyze(this);
		this.typeDeclaration = null;
		this.hierarchy = null;
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
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		
		if (ms.length != methods.length) {
			logger.log(Level.SEVERE, type.getElementName()
					+ "'s methods count?");
		}
		
		for (int i = 0; i < methods.length; i++) {			
			MethodDeclaration lastMethod = methods[i];
			ASTNode node = lastMethod.getParent();
			while (node.getNodeType() != ASTNode.COMPILATION_UNIT) {
				node = node.getParent();
			}
			CompilationUnit t = (CompilationUnit) node;
//			System.out.println("last pos: " + lastMethod.getStartPosition());
			MethodMetric mm = new MethodMetric(ms[i].getHandleIdentifier(),
					lastMethod, analyzer);
			mm.startLine = t.getLineNumber(lastMethod.getStartPosition());
			mm.endLine = t.getLineNumber(lastMethod.getStartPosition() + lastMethod.getLength());
			addChild(mm);
		}
	}

	@Override
	public ASTNode getASTNode() {
		return typeDeclaration;
	}

	@Override
	public int getLevel() {
		return TYPE;
	}

	public ITypeHierarchy getHierarchy() {		
		if (hierarchy == null) {
			IType iType = (IType) getJavaElement();
			try {
				hierarchy = iType.newTypeHierarchy((IJavaProject) iType
						.getAncestor(IJavaElement.JAVA_PROJECT), null);
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Could not get type hierarchy for "
						+ getHandle(), e);

			}
		}
		return hierarchy;
	}

	@Override
	public void accept(MetricElementVisitor visitor) {
		visitor.visit(this);
		visitor.postVisit(this);
	}

}
