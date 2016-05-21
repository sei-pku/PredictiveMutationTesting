package edu.pku.sei.metric.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.pku.sei.metric.Constants;
import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.analyzer.BundleAnalyzer;
import edu.pku.sei.metric.analyzer.MetricElementVisitor;
import edu.pku.sei.metric.analyzer.math.AvgValue;
import edu.pku.sei.metric.analyzer.math.MaxValue;
import edu.pku.sei.metric.analyzer.math.MinValue;

/**
 * AbstractMetricElement contains the source code for analyze and the metric
 * result. Also, the AbstractMetricElement is organized to a tree structure
 * corresponding to the JavaElement
 * 
 * @author PCT
 * 
 */
public abstract class AbstractMetricElement implements Constants {

	protected BundleAnalyzer analyzer;
	
	protected String handle = null;

	private AbstractMetricElement parent;

	private List<AbstractMetricElement> children;

	private Map<String, MetricValue> values = new HashMap<String, MetricValue>();

	private Map<String, AvgValue> averages = new HashMap<String, AvgValue>();

	private Map<String, MinValue> mins = new HashMap<String, MinValue>();

	private Map<String, MaxValue> maxes = new HashMap<String, MaxValue>();

	protected abstract void initChildren(IProgressMonitor monitor);

	public abstract int getLevel();

	/**
	 * Called by metric analyzer
	 * 
	 * @param visitor
	 */
	public abstract void accept(MetricElementVisitor visitor);
	
	/**
	 * @return the related ASTNode
	 */
	public ASTNode getASTNode(){
		return null;
	}
	
	public AbstractMetricElement() {
		this.children = new ArrayList<AbstractMetricElement>();
	}

	public AbstractMetricElement(String handle, BundleAnalyzer analyzer) {
		this();
		this.handle = handle;
		this.analyzer = analyzer;
	}

	public String getHandle() {
		return handle;
	}

	public AbstractMetricElement getParent() {
		return parent;
	}

	public void addChild(AbstractMetricElement child) {
		if (child != null) {
			if (!getChildren().contains(child)) {
				getChildren().add(child);
				child.parent = this;
			}
		}
	}

	public List<AbstractMetricElement> getChildren() {
		return children;
	}

	public IJavaElement getJavaElement() {
		return JavaCore.create(handle);
	}

	
	public String toString(int tab) {
		String result = getHandle() + "'s "
				+ averages.get("Method Line Of Code").toString();
		if (getLevel() > METHOD) {
			for (Iterator<AbstractMetricElement> iter = children.iterator(); iter
					.hasNext();) {
				AbstractMetricElement element = iter.next();
				result += "\n ";
				for (int i = 0; i < tab; i++) {
					result += " ";
				}
				result += element.toString(tab + 1);
			}
		}
		return result;
	}

	/**
	 * @return the CompilationUint that is an ancestor JavaElement or the
	 *         JavaElement itself
	 */
	public ICompilationUnit getCompilationUnit() {
		ICompilationUnit result = null;
		IJavaElement input = getJavaElement();

		if (input.getElementType() == IJavaElement.COMPILATION_UNIT)
			result = (ICompilationUnit) input;
		else {
			result = (ICompilationUnit) input
					.getAncestor(IJavaElement.COMPILATION_UNIT);
		}
		return result;
	}

	public void setValue(MetricValue value) {
		values.put(value.getName(), value);
	}

	/**
	 * Little trick: in order to make value 'null'
	 * 
	 * @param metricName
	 * @param value
	 */
	public void setValue(String metricName, MetricValue value) {
		values.put(metricName, value);
	}

	public MetricValue getValue(String name) {
		return values.get(name);
	}

	public void setAverageValue(AvgValue value) {
		averages.put(value.getName(), value);
	}

	public void setMinValue(MinValue value) {
		mins.put(value.getName(), value);
	}

	public void setMaxValue(MaxValue value) {
		maxes.put(value.getName(), value);
	}

	public AvgValue getAverageValue(String name) {
		return averages.get(name);
	}

	public MinValue getMinValue(String name) {
		return mins.get(name);
	}

	public MaxValue getMaxValue(String name) {
		return maxes.get(name);
	}

}
