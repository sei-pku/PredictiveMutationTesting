package edu.pku.sei.metric.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Date;



import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import edu.pku.sei.metric.Activator;
import edu.pku.sei.metric.MetricUtility;
import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.AvgValue;
import edu.pku.sei.metric.analyzer.math.MaxValue;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.MethodMetric;

/**
 * Create an XML report of the metrics
 *
 * @author liushi07
 *
 */
public class XMLExporter {

	private MetricValueDescriptor[] metricDescriptors;

	private FileOutputStream out;

	private XMLPrintStream pOut;

	private int indent = 0;

	public XMLExporter(File outputFile) throws FileNotFoundException {
		metricDescriptors = Activator.metrics
				.toArray(new MetricValueDescriptor[0]);
		out = new FileOutputStream(outputFile);
		pOut = new XMLPrintStream(out);
	}

	/**
	 * Export the metric result to a given file
	 *
	 * @param monitor
	 */
	public void export(AbstractMetricElement element, IProgressMonitor monitor)
			throws InvocationTargetException {
		try {
			pOut.printXMLHeader();
			// TODO: add task number calculation code
			monitor.beginTask("Exporting metrics to XML file...",
					metricDescriptors.length);
			printRoot(element, monitor);
			pOut.close();
			out.close();
		} catch (Throwable e) {
			throw new InvocationTargetException(e);
		}
	}

	private void printRoot(AbstractMetricElement root, IProgressMonitor monitor) {
		pOut.print("<Metrics scope=\"");
		pOut.print(root.getJavaElement().getElementName());
		pOut.print("\" level=\"");
		pOut.print(MetricUtility.transferLevel(root.getLevel()));
		pOut.print("\" date=\"");
		pOut.print(pOut.formatXSDDate(new Date()));
		pOut.println("\" >");

		for (int i = 0; i < metricDescriptors.length; i++) {
			monitor.subTask("Exporting: "
					+ metricDescriptors[i].getMetricName());

			String name = metricDescriptors[i].getMetricName();
			int level = metricDescriptors[i].getLevel();

			pOut.indent(++indent);
			pOut.print("<Metric ");
			pOut.print("name = \"");
			pOut.print(name);
			pOut.print("\" ");
			pOut.print("level = \"");
			pOut.print(MetricUtility.transferLevel(level));
			pOut.print("\" ");
			pOut.print("description =\"");
			pOut.print(metricDescriptors[i].getDescription());
			pOut.print("\"");

			pOut.println(">");

			boolean rowNeeded = false;

			pOut.indent(++indent);
			pOut.print("<" + MetricUtility.transferLevel(root.getLevel())
					+ " per = \"");
			pOut.print(MetricUtility.transferLevel(level));
			pOut.print("\"");

			if (level <= root.getLevel()) {
				rowNeeded = true;
				MetricValue m = root.getValue(name);
				if (m != null) {
					pOut.print(" total = \"");
					pOut.print(format(m.getValue()));
					pOut.print("\"");
				}
			}

			AvgValue avg = root.getAverageValue(name);
			MaxValue max = root.getMaxValue(name);
			if ((avg != null) || (max != null)) {
				if (avg != null) {
					pOut.print(" avg = \"");
					pOut.print(format(avg.getValue()));
					pOut.print("\"");
				}
				if (max != null) {
					pOut.print(" max = \"");
					pOut.print(format(max.getValue()));
					pOut.print("\"");

					String handle = max.getHandle();
					if (handle != null) {
						IJavaElement element = JavaCore.create(handle);
						pOut.print(" path = \"");
						pOut.print(element.getPath().toString());
						pOut.print("\"");
					}
				}
				pOut.println(">");
				addChildren(root, name, level);
				rowNeeded = false;
			} else {
				pOut.println(">");
			}

			if (rowNeeded) {
				addChildren(root, name, level);
			}

			pOut.indent(indent--);
			pOut.println("</" + MetricUtility.transferLevel(root.getLevel())
					+ ">");
			pOut.indent(indent--);
			pOut.println("</Metric>");
			monitor.worked(1);
		}
		pOut.println("</Metrics>");
		monitor.done();
	}

	private void addChildren(AbstractMetricElement me, String metric, int level) {
		AbstractMetricElement[] children = me.getChildren().toArray(
				new AbstractMetricElement[0]);

		indent++;
		// don't have to do anything if there are no child metrics
		if ((children != null) && (children.length > 0)) {
			for (int i = 0; i < children.length; i++) {
				if (level <= children[i].getLevel()) {
					pOut.indent(indent);
					pOut.print("<"
							+ MetricUtility.transferLevel(children[i]
									.getLevel()) + " name = ");

					pOut.print("\""
							+ getElementName(children[i].getJavaElement())
							+ "\"");
					//添加行号
					if (children[i].getLevel() == 1) {
						MethodMetric mm = (MethodMetric) children[i];
						pOut.print(" line = ");
						pOut.print("\""
								+ mm.startLine + "-" + mm.endLine
								+ "\"");
					}
					
					MetricValue val = children[i].getValue(metric);
					if (val != null) {
						pOut.print(" total = ");
						pOut.print("\"" + format(val.getValue()) + "\"");
					}
					AvgValue avg = children[i].getAverageValue(metric);
					MaxValue max = children[i].getMaxValue(metric);
					if ((avg != null) || (max != null)) {
						if (avg != null) {
							pOut.print(" avg = ");
							pOut.print("\"" + format(avg.getValue()) + "\"");
						}
						if (max != null) {
							pOut.print(" max = ");
							pOut.print("\"" + format(max.getValue()) + "\"");

							String handle = max.getHandle();
							if (handle != null) {
								IJavaElement element = JavaCore.create(handle);
								pOut.print(" path = ");
								pOut.print("\"" + element.getPath().toString()
										+ "\"");
							}
						}
					}
					pOut.println(">");

					// recurse
					addChildren(children[i], metric, level);
					pOut.indent(indent);
					pOut.println("</"
							+ MetricUtility.transferLevel(children[i]
									.getLevel()) + ">");
				}
			}
		}
		indent--;
	}

	private String format(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		int decimals = 3;
		nf.setMaximumFractionDigits(decimals);
		nf.setGroupingUsed(false);
		return nf.format(value);
	}

	private String getElementName(IJavaElement element) {
		String candidate = element.getElementName();
		if ("".equals(candidate))
			return "(default package)";
		return candidate;
	}
}
