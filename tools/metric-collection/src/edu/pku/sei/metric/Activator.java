package edu.pku.sei.metric;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.pku.sei.metric.analyzer.Analyzer;
import edu.pku.sei.metric.analyzer.math.MathOperator;
import edu.pku.sei.metric.source.Cache;
import edu.pku.sei.metric.ui.MetricsView;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author liushi07, zhanglm07
 *
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.pku.sei.metric";

	public static final String Anlyzer_Extension_ID = "edu.pku.sei.metric.metricsAnalyzer";

	public static final String MathOperator_Extension_ID = "edu.pku.sei.metric.mathOperation";

	public static final List<Analyzer> analyzers = new ArrayList<Analyzer>();

	public static final List<MetricValueDescriptor> metrics = new ArrayList<MetricValueDescriptor>();

	public static final List<MathOperator> operators = new ArrayList<MathOperator>();

	// The shared instance
	private static Activator plugin;

	// The metrics result view
	private MetricsView view;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		System.out.println("Start....");
		super.start(context);
		this.installMetricsAnalyzers();
		this.installMathOperators();
		this.installImages();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		Cache.singleton.close();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Set the metrics view
	 *
	 * @param view
	 */
	public void setMetricsView(MetricsView view) {
		this.view = view;
	}

	/**
	 * @return the metrics view
	 * @throws PartInitException
	 */
	public MetricsView getMetricsView() {
		return this.view;
	}

	private void installMetricsAnalyzers() throws CoreException {
		IExtensionPoint p = Platform.getExtensionRegistry().getExtensionPoint(
				Anlyzer_Extension_ID);
		assert p!= null : "ExtensionPoint should not be null";
		if (p != null) {
			IExtension[] x = p.getExtensions();

			System.out.println("Total " + x.length + " extensions..");

			for (int i = 0; i < x.length; i++) {
				IConfigurationElement[] analyzerExtentions = x[i]
						.getConfigurationElements();
				for (int j = 0; j < analyzerExtentions.length; j++) {
					IConfigurationElement analyzerExtention = analyzerExtentions[j];
					String kind = analyzerExtention.getName();
					if (kind.equals("analyzer")) {
						IConfigurationElement[] metricExtentions = analyzerExtention
								.getChildren();
						List<MetricValueDescriptor> descriptors = new ArrayList<MetricValueDescriptor>();
						for (int k = 0; k < metricExtentions.length; k++) {
							IConfigurationElement metricExtention = metricExtentions[k];
							MetricValueDescriptor descriptor = new MetricValueDescriptor(
									metricExtention.getAttribute("metricName"),
									MetricUtility.transferLevel(metricExtention
											.getAttribute("level")),
									metricExtention.getAttribute("description"),
									MetricUtility
											.transferPropagete(metricExtention
													.getAttribute("propagate")));
							descriptors.add(descriptor);
						}
						metrics.addAll(descriptors);

						System.out.println("Total " + descriptors.size() + " metric descriptors..");

						Analyzer analyzer = (Analyzer) analyzerExtention
								.createExecutableExtension("analyzerClass");
						analyzer.setMetrics(descriptors);
						analyzers.add(analyzer);
					}
				}
			}
		}
	}

	private void installMathOperators() throws CoreException {
		IExtensionPoint p = Platform.getExtensionRegistry().getExtensionPoint(
				MathOperator_Extension_ID);
		if (p != null) {
			IExtension[] x = p.getExtensions();
			for (int i = 0; i < x.length; i++) {
				IConfigurationElement[] operatorExtentions = x[i]
						.getConfigurationElements();
				for (int j = 0; j < operatorExtentions.length; j++) {
					IConfigurationElement operatorExtention = operatorExtentions[j];
					String kind = operatorExtention.getName();
					if (kind.equals("mathOperator")) {
						MathOperator operator = (MathOperator) operatorExtention
								.createExecutableExtension("operatorClass");
						operators.add(operator);
					}
				}
			}
		}
	}

	private void installImages() {
		// Load images
		ImageRegistry imgReg = getImageRegistry();
		imgReg.put("metric", getImageDescriptor("chart_bar.png"));
		imgReg.put("clean", getImageDescriptor("clean.png"));
		imgReg.put("export", getImageDescriptor("export.gif"));
		imgReg.put(MetricUtility.transferLevel(Constants.PROJECT),
				getImageDescriptor("project.gif"));
		imgReg.put(MetricUtility.transferLevel(Constants.PACKAGEROOT),
				getImageDescriptor("packageRoot.gif"));
		imgReg.put(MetricUtility.transferLevel(Constants.PACKAGEFRAGMENT),
				getImageDescriptor("package.gif"));
		imgReg.put(MetricUtility.transferLevel(Constants.COMPILATIONUNIT),
				getImageDescriptor("java.gif"));
		imgReg.put(MetricUtility.transferLevel(Constants.TYPE),
				getImageDescriptor("class.gif"));
		imgReg.put(MetricUtility.transferLevel(Constants.METHOD),
				getImageDescriptor("method.gif"));

		imgReg.put(Constants.ENUM, getImageDescriptor("enum.gif"));
		imgReg.put(Constants.ANNOTATION, getImageDescriptor("annotation.gif"));
	}

	/**
	 * @throws PartInitException
	 */
	public void showMetricView() throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getWorkbenchWindows()[0]
				.getActivePage();
		page.showView("PKUMetric Plugin.MetricsView", null,
				IWorkbenchPage.VIEW_ACTIVATE);
	}

	/**
	 * Returns the ImageDescriptor of the file at the given location relative to
	 * the plugin's icon directory.
	 */
	public static ImageDescriptor getImageDescriptor(String name) {
		try {

			URL installURL = getDefault().getBundle().getEntry("/");
			URL url = new URL(installURL, "icons/" + name);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

}
