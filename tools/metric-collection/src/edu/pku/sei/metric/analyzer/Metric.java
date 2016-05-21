package edu.pku.sei.metric.analyzer;

import edu.pku.sei.metric.MetricValue;
import edu.pku.sei.metric.source.AbstractMetricElement;


/**
 * Do basic metric and cache function
 * 
 * @author PCT
 * 
 */
public abstract class Metric {

	protected String name;

	protected AbstractMetricElement source;

	protected MetricValue[] values;

	//private Logger logger = Logger.getLogger(Metric.class.getName());

	public Metric(AbstractMetricElement source) {
		this.source = source;
	}

	/**
	 * Do the calculation and Set metric results to the AbstractMetricElement
	 */
	public final void setValuesToMetricElement() {
		
		// calculate the metric
		calculate();
		
		
		
//		if (isCacheAvailable() == false) {
//			// calculate the metric
//			calculate();
//
//			// set new time stamps
//			for (int i = 0; i < values.length; i++) {
//				values[i].setModificationStamp(source.getJavaElement()
//						.getResource().getModificationStamp());
//			}
//
//			// save the new results to the cache
//			for (int i = 0; i < values.length; i++) {
//				Cache.singleton.put(source.getHandle(), values[i]);
//			}
//		}

		// set the results to the AbstractMetricElement
		for (int i = 0; i < values.length; i++) {
			source.setValue(values[i]);
		}
	}

	/**
	 * basic function, calculate and put results in each values[i] derived class
	 * must implement this method
	 */
	abstract protected void calculate();

	/**
	 * Return true if their is cache value available and set the cached value
	 * 
	 * @return false if the code has been modified and need recalculation
	 */
//	private boolean isCacheAvailable() {
//		MetricValue cacheValue;
//		for (int i = 0; i < values.length; i++) {
//			cacheValue = Cache.singleton.get(source.getHandle(), values[i]
//					.getName());
//			if (null == cacheValue
//					|| cacheValue.getModificationStamp() != source
//							.getJavaElement().getResource()
//							.getModificationStamp()) {
//				logger.info(source.getHandle() + " cache unavailable..");
//				return false;
//			} else {
//				values[i].setValue(cacheValue.getValue());
//			}
//		}		
//		return true;
//	}

}
