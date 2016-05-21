package edu.pku.sei.metric.source;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;

/**
 * @author PCT
 * 
 */
public class CompilationUnitMetric extends AbstractMetricElement {

	private final Logger logger = Logger.getLogger(CompilationUnitMetric.class
			.getName());

	public CompilationUnitMetric(String handle, BundleAnalyzer analyzer) {
		super(handle, analyzer);
		if (handle != null)
			initChildren(null);
		
		analyzer.analyze(this);
	}

	@Override
	protected void initChildren(IProgressMonitor monitor) {
		CompilationUnit astNode = getASTNode();
	
		List<?> types = astNode.types();
		for (int i = 0; i < types.size(); i++) {
			AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) types
					.get(i);
			if (declaration instanceof TypeDeclaration) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) declaration;
				IType type = ((ICompilationUnit) getJavaElement())
						.getType(typeDeclaration.getName().getIdentifier());
				AbstractMetricElement next = new TypeMetric(type
						.getHandleIdentifier(), typeDeclaration, analyzer);
				if (next != null)
					addChild(next);
				else
					logger.warning("Get null TypeMetric when initializing.");

				addInnerClasses(typeDeclaration, type);
			} else if (declaration instanceof EnumDeclaration) {
				EnumDeclaration enumDeclaration = (EnumDeclaration) declaration;
				IType type = ((ICompilationUnit) getJavaElement())
						.getType(enumDeclaration.getName().getIdentifier());
				AbstractMetricElement next = new EnumMetric(type
						.getHandleIdentifier(), enumDeclaration, analyzer);
				if (next != null)
					addChild(next);
				else
					logger.warning("Get null EnumMetric when initializing.");
			} else if (declaration instanceof AnnotationTypeDeclaration) {
				AnnotationTypeDeclaration annotationDeclaration = (AnnotationTypeDeclaration) declaration;
				IType type = ((ICompilationUnit) getJavaElement())
						.getType(annotationDeclaration.getName()
								.getIdentifier());
				AbstractMetricElement next = new AnnotationTypeMetric(type
						.getHandleIdentifier(), annotationDeclaration, analyzer);
				if (next != null)
					addChild(next);
				else
					logger
							.warning("Get null AnnotationTypeMetric when initializing.");
			} else
				logger.warning("Unknown type : "
						+ declaration.getClass().getName());
		}
	}

	@SuppressWarnings("unchecked")
	private void addInnerClasses(TypeDeclaration typeDeclaration, IType type) {
		try {
			List<ASTNode> bodyDecls = typeDeclaration.bodyDeclarations();
			TypeDeclaration[] typeDecls = typeDeclaration.getTypes();
			IType[] types = type.getTypes();

			for (int i = 0; i < types.length; i++) {
				AbstractMetricElement inner = null;
				if (types[i].isEnum()) {
					EnumDeclaration enumDecl = null;
					for (ASTNode decl : bodyDecls) {
						if (decl instanceof EnumDeclaration) {
							enumDecl = (EnumDeclaration) decl;
							if (enumDecl.getName().toString().compareTo(
									types[i].getElementName()) == 0) {
								break;
							}
						}
					}
					inner = new EnumMetric(types[i].getHandleIdentifier(),
							enumDecl, analyzer);
				} else {
					TypeDeclaration typeDecl = null;
					for (TypeDeclaration decl : typeDecls) {
						if (decl.getName().toString().compareTo(
								types[i].getElementName()) == 0) {
							typeDecl = decl;
							break;
						}
					}
					inner = new TypeMetric(types[i].getHandleIdentifier(),
							typeDecl, analyzer);
				}

				addChild(inner);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// may have problem?!!
	public CompilationUnit getASTNode() {
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource((ICompilationUnit) getJavaElement());
			return (CompilationUnit) parser.createAST(null);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getLevel() {
		return COMPILATIONUNIT;
	}

	@Override
	public void accept(MetricElementVisitor visitor) {
		visitor.visit(this);
		visitor.postVisit(this);
	}
}
