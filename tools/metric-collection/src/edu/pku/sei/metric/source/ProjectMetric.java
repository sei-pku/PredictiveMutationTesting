package edu.pku.sei.metric.source;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;

/**
 * @author PCT
 * 
 */
public class ProjectMetric extends AbstractMetricElement {

	public ProjectMetric(String handle, BundleAnalyzer analyzer, IProgressMonitor monitor) {
		super(handle, analyzer);
		if (handle != null)
			initChildren(monitor);
		
		analyzer.analyze(this);
	}

	@Override
	protected void initChildren(IProgressMonitor monitor) {
		IJavaProject project = (IJavaProject) getJavaElement();
		try {
			IJavaElement[] children = project.getChildren();
			
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof IPackageFragmentRoot) {
					if (!((IPackageFragmentRoot) children[i]).isArchive()) {
						AbstractMetricElement next = new PackageFragmentRootMetric(
								children[i].getHandleIdentifier(), analyzer, monitor);

						if (next != null) {
							addChild(next);
						} else
							System.out.println("");
					}
				}
				
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getLevel() {
		return PROJECT;
	}

	@Override
	public void accept(MetricElementVisitor visitor) {
		visitor.visit(this);
		visitor.postVisit(this);
	}

}
