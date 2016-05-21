package edu.pku.sei.metric.source;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;

import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;

/**
 * @author PCT
 * 
 */
@SuppressWarnings("restriction")
public class PackageFragmentRootMetric extends AbstractMetricElement {

	public PackageFragmentRootMetric(String handle, BundleAnalyzer analyzer,
			IProgressMonitor monitor) {
		super(handle, analyzer);
		if (getJavaElement() instanceof JarPackageFragmentRoot)
			loadClassFilesFromJar();
		if (handle != null)
			initChildren(monitor);

		analyzer.analyze(this);
	}

	@Override
	protected void initChildren(IProgressMonitor monitor) {
		IPackageFragmentRoot pack = (IPackageFragmentRoot) getJavaElement();
		try {
			IJavaElement[] children = pack.getChildren();

			monitor.beginTask("calculate", children.length);
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof IPackageFragment) {
					if (((IPackageFragment) children[i])
							.containsJavaResources()) {
						AbstractMetricElement next = new PackageFragmentMetric(
								children[i].getHandleIdentifier(), analyzer,
								null);
						if (next != null) {
							addChild(next);
						} else
							System.out.println("metrics for package "
									+ children[i].getHandleIdentifier()
									+ " not found.");
					}
				}
				monitor.worked(1);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getLevel() {
		return PACKAGEROOT;
	}

	@Override
	public void accept(MetricElementVisitor visitor) {
		visitor.visit(this);
		visitor.postVisit(this);
	}

	// @Test
	@SuppressWarnings("deprecation")
	private void loadClassFilesFromJar() {
		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) getJavaElement();
		File jarFile = packageFragmentRoot.getResource().getLocation().toFile();
		try {
			URL url = jarFile.toURL();
			URLConnection u = url.openConnection();
			ZipInputStream inputStream = new ZipInputStream(u.getInputStream());
			ZipEntry entry = inputStream.getNextEntry();
			while (null != entry) {
				if (entry.getName().endsWith(".class")) {
					ClassParser parser = new ClassParser(inputStream, entry
							.getName());
					Repository.addClass(parser.parse());
				}
				entry = inputStream.getNextEntry();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
