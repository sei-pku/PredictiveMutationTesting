package edu.pku.sei.metric;


/**
 * Some utility service for PKUMetric
 * 
 * @author liushi07
 * 
 */
public class MetricUtility implements Constants {

	/**
	 * From String to int to represent a level
	 * 
	 * @param level
	 * @return the int value that represent the given level
	 */
	public static int transferLevel(String level) {
		if (level.equalsIgnoreCase("project")) {
			return PROJECT;
		} else if (level.equalsIgnoreCase("packageFragmentRoot")) {
			return PACKAGEROOT;
		} else if (level.equalsIgnoreCase("packageFragment")) {
			return PACKAGEFRAGMENT;
		} else if (level.equalsIgnoreCase("compilationUnit")) {
			return COMPILATIONUNIT;
		} else if (level.equalsIgnoreCase("type")) {
			return TYPE;
		} else if (level.equalsIgnoreCase("method")) {
			return METHOD;
		}
		return -1;
	}

	/**
	 * From int to String to represent a level
	 * 
	 * @param level
	 * @return the name of the level
	 */
	public static String transferLevel(int level) {
		switch (level) {
		case 1:
			return "method";
		case 2:
			return "type";
		case 3:
			return "compilationUnit";
		case 4:
			return "packageFragment";
		case 5:
			return "packageFragmentRoot";
		case 6:
			return "project";
		default:
			return null;
		}

	}

	/**
	 * @param propagate
	 * @return
	 */
	public static boolean transferPropagete(String propagate) {
		if (propagate.equalsIgnoreCase("true"))
			return true;
		else if (propagate.equalsIgnoreCase("false"))
			return false;
		return false;
	}
}
