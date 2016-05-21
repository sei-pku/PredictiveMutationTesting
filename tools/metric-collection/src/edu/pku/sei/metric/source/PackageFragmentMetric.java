package edu.pku.sei.metric.source;

import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;

/**
 * @author PCT
 *
 */
public class PackageFragmentMetric extends AbstractMetricElement {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public PackageFragmentMetric(String handle, BundleAnalyzer analyzer, IProgressMonitor monitor) {
		super(handle, analyzer);
		if (handle != null)
			initChildren(monitor);
		
		analyzer.analyze(this);
	}

	@Override
	protected void initChildren(IProgressMonitor monitor) {
		IPackageFragment pack = (IPackageFragment) getJavaElement();
		try {
			IJavaElement[] children = pack.getChildren();
			if (null != monitor) {
				monitor.beginTask("calculate", children.length);
			}
			for (int i = 0; i < children.length; i++) {
				AbstractMetricElement next = null;
				if (children[i] instanceof ICompilationUnit) {
					next = new CompilationUnitMetric(children[i]
							.getHandleIdentifier(), analyzer);
				} else if (children[i] instanceof IClassFile) {
					next = new ClassFileMetric(children[i]
							.getHandleIdentifier(), analyzer);
				}
				if (next != null)
					addChild(next);
				else
					logger.warning("Can't initialize AbstractMetricSource for "
							+ children[i].getElementName());
				
				if(null != monitor)
				{
					monitor.worked(1);
				}

			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

	}

	@Override
	public int getLevel() {
		return PACKAGEFRAGMENT;
	}

	@Override
	public void accept(MetricElementVisitor visitor) {
		visitor.visit(this);
		visitor.postVisit(this);
	}


}
