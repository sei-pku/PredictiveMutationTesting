package edu.pku.sei.metric;

import java.util.ResourceBundle;
/**
 * Constants used for the project
 * 
 * @author zhanglm07
 * 
 */
public class Message {

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle("edu.pku.sei.metric.Resources");

	public static String getString(String key) {
		return RESOURCE_BUNDLE.getString(key);
	}

}
