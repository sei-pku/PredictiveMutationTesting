package edu.pku.sei.metric.ui;

import java.text.NumberFormat;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

import edu.pku.sei.metric.Activator;
import edu.pku.sei.metric.Constants;
import edu.pku.sei.metric.Message;
import edu.pku.sei.metric.MetricUtility;
import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.MetricValueDescriptor;
import edu.pku.sei.metric.analyzer.math.AvgValue;
import edu.pku.sei.metric.analyzer.math.MaxValue;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.EnumMetric;
import edu.pku.sei.metric.source.TypeMetric;

/**
 * The metric results is displayed as a tree style table
 * 
 * @author PCT
 * 
 */
public class MetricsResultTable extends Tree implements TreeListener,
		SelectionListener {
	
	/*Text of the Column names to display */
	private String textMetricName = Message.getString("textMetricName");
	private String textTotal = Message.getString("textTotal");
	private String textAverage = Message.getString("textAverage");
	private String textMax= Message.getString("textMax");
	private String textMetricDesc = Message.getString("textMetricDesc");
	private String textMaxPath = Message.getString("textMaxPath");

	private TreeColumn metricName;

	private TreeColumn value;

	private TreeColumn average;

	private TreeColumn max;

	private TreeColumn description;

	private TreeColumn path;

	private MetricValueDescriptor[] descriptors = Activator.metrics
			.toArray(new MetricValueDescriptor[0]);

	public MetricsResultTable(Composite parent, int style) {
		super(parent, SWT.SINGLE);
		setLinesVisible(true);
		setHeaderVisible(true);
		metricName = new TreeColumn(this, SWT.LEFT);
		metricName.setText(textMetricName);
		value = new TreeColumn(this, SWT.RIGHT);
		value.setText(textTotal);
		average = new TreeColumn(this, SWT.RIGHT);
		average.setText(textAverage);
		max = new TreeColumn(this, SWT.RIGHT);
		max.setText(textMax);
		description = new TreeColumn(this, SWT.LEFT);
		description.setText(textMetricDesc);
		path = new TreeColumn(this, SWT.LEFT);
		path.setText(textMaxPath);
		addSelectionListener(this);
		addTreeListener(this);

	}

	/**
	 * Update the table with new metric values.
	 * 
	 * @param ms
	 */
	public void setMetrics(final AbstractMetricElement ms) {
		try {
			removeAll();
			if (ms == null)
				return;

			for (int i = 0; i < descriptors.length; i++) {
				boolean rowNeeded = false;
				String name = descriptors[i].getMetricName();
				int level = descriptors[i].getLevel();
				String[] cols = new String[] { name, "", "", "",
						descriptors[i].getDescription(), "" };

				if (level <= ms.getLevel()) {
					rowNeeded = true;
					MetricValue m = ms.getValue(name);
					if (m != null) {
						cols[1] = format(m.getValue());
					}
				}

				AvgValue avg = ms.getAverageValue(name);
				MaxValue max = ms.getMaxValue(name);
				if ((avg != null) || (max != null)) {
					TreeItem row = createNewRow();
					row.setForeground(getMetricForeground());
					row.setImage(Activator.getDefault().getImageRegistry().get(
							"metric"));
					cols[0] = name
							+ " (avg/max per "
							+ MetricUtility.transferLevel(descriptors[i]
									.getLevel()) + ")";

					if (avg != null) {
						cols[2] = format(avg.getValue());
					}
					if (max != null) {
						cols[3] = format(max.getValue());
						String handle = max.getHandle();
						if (handle != null) {
							IJavaElement element = JavaCore.create(handle);
							cols[5] = element.getPath().toString();
							row.setData("handle", handle);
							row.setData("element", element);
						}
					}
					setText(row, cols);
					addChildren(row, ms, name, level);
					rowNeeded = false;
				}
				if (rowNeeded) {
					TreeItem row = createNewRow();
					row.setForeground(getMetricForeground());
					row.setImage(Activator.getDefault().getImageRegistry().get(
							"metric"));
					setText(row, cols);
					addChildren(row, ms, name, level);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private Color getMetricForeground() {
		RGB color = new RGB(50, 150, 255);
		return new Color(getDisplay(), color);
	}

	private void addChildren(TreeItem row, AbstractMetricElement me,
			String metric, int level) {
		AbstractMetricElement[] children = me.getChildren().toArray(
				new AbstractMetricElement[0]);
		// don't have to do anything if there are no child metrics
		if ((children != null) && (children.length > 0)) {
			for (int i = 0; i < children.length; i++) {
				if (level <= children[i].getLevel()) {
					TreeItem child = createNewRow(row);
					child.setText(getElementName(children[i].getJavaElement()));
					child.setImage(getImage(children[i]));
					MetricValue val = children[i].getValue(metric);
					child.setText(1, (val != null) ? format(val.getValue())
							: "");
					AvgValue avg = children[i].getAverageValue(metric);
					MaxValue max = children[i].getMaxValue(metric);
					if ((avg != null) || (max != null)) {
						if (avg != null) {
							child.setText(2, format(avg.getValue()));
						}
						if (max != null) {
							child.setText(3, format(max.getValue()));
							String handle = max.getHandle();
							if (handle != null) {
								IJavaElement element = JavaCore.create(handle);
								child.setText(5, element.getPath().toString());
							}
						}
					}
					child.setData("handle", children[i].getHandle());
					child.setData("element", children[i].getJavaElement());
					// recurse
					addChildren(child, children[i], metric, level);
				}
			}
		}
	}

	private Image getImage(AbstractMetricElement me) {
		switch (me.getLevel()) {
		case Constants.PROJECT:
			return Activator.getDefault().getImageRegistry().get(
					MetricUtility.transferLevel(me.getLevel()));
		case Constants.PACKAGEROOT:
			return Activator.getDefault().getImageRegistry().get(
					MetricUtility.transferLevel(me.getLevel()));
		case Constants.PACKAGEFRAGMENT:
			return Activator.getDefault().getImageRegistry().get(
					MetricUtility.transferLevel(me.getLevel()));
		case Constants.COMPILATIONUNIT:
			return Activator.getDefault().getImageRegistry().get(
					MetricUtility.transferLevel(me.getLevel()));
		case Constants.TYPE: {
			if (me instanceof TypeMetric)
				return Activator.getDefault().getImageRegistry().get(
						MetricUtility.transferLevel(me.getLevel()));
			else if (me instanceof EnumMetric)
				return Activator.getDefault().getImageRegistry().get(
						Constants.ENUM);
			else
				return Activator.getDefault().getImageRegistry().get(
						Constants.ANNOTATION);
		}

		case Constants.METHOD:
			return Activator.getDefault().getImageRegistry().get(
					MetricUtility.transferLevel(me.getLevel()));
		default:
			return null;
		}
	}

	/**
	 * create a new root row
	 * 
	 * @return
	 */
	private TreeItem createNewRow() {
		TreeItem item = new TreeItem(this, SWT.NULL);
		// item.setForeground(getDefaultForeground());
		return item;
	}

	/**
	 * create a new child row
	 * 
	 * @param parent
	 * @return
	 */
	private TreeItem createNewRow(TreeItem parent) {
		TreeItem item = new TreeItem(parent, SWT.NULL);
		// item.setForeground(getDefaultForeground());
		return item;
	}

	private void setText(TreeItem row, String[] columns) {
		row.setText(columns[0]);
		for (int i = 1; i < columns.length; i++) {
			row.setText(i, columns[i]);
		}
	}

	private String getElementName(IJavaElement element) {
		String candidate = element.getElementName();
		if ("".equals(candidate))
			return "(default package)";
		return candidate;
	}

	/**
	 * Override the method to avoid subclass exception
	 * 
	 * @see org.eclipse.swt.widgets.Widget#checkSubclass()
	 */
	protected void checkSubclass() {
	}

	public void treeCollapsed(TreeEvent e) {
		// TODO Auto-generated method stub

	}

	public void treeExpanded(TreeEvent e) {
		// TODO Auto-generated method stub
	}

	// react to the double-click action
	public void widgetDefaultSelected(SelectionEvent e) {
		TreeItem row = (TreeItem) e.item;
		IJavaElement element = (IJavaElement) row.getData("element");
		String handle = (String) row.getData("handle");
		try {
			if (element != null) {
				IEditorPart javaEditor = JavaUI.openInEditor(element);
				if (element instanceof IMember)
					JavaUI.revealInEditor(javaEditor, element);
			}
		} catch (PartInitException x) {
			System.err.println("Error selecting " + handle);
			x.printStackTrace();
		} catch (JavaModelException x) {
			System.err.println("Error selecting " + handle);
			x.printStackTrace();
		} catch (Throwable t) {
			System.err.println("Error selecting " + handle);
			t.printStackTrace();
		}
	}

	public void widgetSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public void initWidths(IMemento memento) {
		metricName.setWidth(getWidth(memento, "description", 230));
		value.setWidth(getWidth(memento, "value", 50));
		average.setWidth(getWidth(memento, "average", 60));
		max.setWidth(getWidth(memento, "max", 60));
		description.setWidth(getWidth(memento, "variance", 240));
		path.setWidth(getWidth(memento, "path", 300));
	}

	private int getWidth(IMemento m, String name, int defaultVal) {
		try {
			Integer val = m.getInteger(name);
			return (val == null) ? defaultVal : val.intValue();
		} catch (Throwable e) {
			return defaultVal;
		}
	}

	private String format(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		int decimals = 3;//MetricsPlugin.getDefault().getPreferenceStore().getInt
		// ("METRICS.decimals");
		nf.setMaximumFractionDigits(decimals);
		nf.setGroupingUsed(false);
		return nf.format(value);
	}
}
