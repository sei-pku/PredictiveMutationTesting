package edu.pku.sei.metric;

import java.io.Serializable;


/**
 * Data Structure for metric values
 * 
 * @author zhanglm07
 * 
 */
public class MetricValue implements Constants, Serializable {

	private static final long serialVersionUID = 5643016498905684007L;

	protected String name = "";

	private String scope = "";

	protected double value;

	private long modificationStamp;

	public MetricValue(String name, double value) {
		this.name = name;
		this.value = value;
		this.modificationStamp = -1;
	}

	public MetricValue(String name, double value, long modificationStamp) {
		this.name = name;
		this.value = value;
		this.modificationStamp = modificationStamp;
	}

	public MetricValue(String name, String scope, double value,
			long modificationStamp) {
		this(name, value, modificationStamp);
		this.scope = scope;
	}

	public long getModificationStamp() {
		return this.modificationStamp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void setModificationStamp(long modificationStamp) {
		this.modificationStamp = modificationStamp;
	}

	public String toString() {
		return name + "   " + value;
	}

}
