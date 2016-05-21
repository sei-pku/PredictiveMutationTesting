package edu.pku.sei.metric.source;


import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;

import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;

/**
 * @author PCT
 * 
 */
public class ClassFileMetric extends AbstractMetricElement {
	
	private JavaClass javaClass;

	public ClassFileMetric(String handle, BundleAnalyzer analyzer) {
		super(handle, analyzer);
		try {
			IClassFile file = (IClassFile) getJavaElement();
			String className = file.getElementName();
			String packageName = file.getParent().getElementName();
			javaClass = Repository.lookupClass(packageName + "."
					+ className.replace(".class", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (handle != null)
			initChildren(null);
		
		analyzer.analyze(this);
		this.javaClass = null;
	}

	@Override
	public void accept(MetricElementVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int getLevel() {
		return TYPE;
	}


	public JavaClass getJavaClass() {
		return this.javaClass;
	}

	@Override
	protected void initChildren(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}
	

}
