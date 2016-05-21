package edu.pku.sei.metric.source;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.pku.sei.metric.analyzer.MetricElementVisitor;


/**
 * In order to make multiple java elements a parent
 * 
 * @author PCT
 * 
 */
public class FakeMetric extends AbstractMetricElement {

	private int level = 0;

	@Override
	public void accept(MetricElementVisitor visitor) {
		List<AbstractMetricElement> children = this.getChildren();
		for (int i = 0; i < children.size(); i++) {
			children.get(i).accept(visitor);
		}
		visitor.postVisit(this);
	}

	@Override
	public int getLevel() {
		return level;
	}


	@Override
	public void addChild(AbstractMetricElement child) {
		super.addChild(child);
		if (child.getLevel() > level) {
			level = child.getLevel();
		}
	}

	@Override
	protected void initChildren(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

}
