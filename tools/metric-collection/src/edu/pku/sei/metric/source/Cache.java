package edu.pku.sei.metric.source;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.pku.sei.metric.Activator;
import edu.pku.sei.metric.MetricValue;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.htree.HTree;

/**
 * public API to the private database. Currently the database is a jdbm
 * persistent hashtable with MRU cache.
 * 
 * @author PCT
 */
public class Cache {

	private static final String DBNAME = "/metricsdata";

	private RecordManager recman;

	private String pluginDir;

	public final static Cache singleton = new Cache();

	private HashMap<String, HTree> metrics = new HashMap<String, HTree>();

	private Logger logger = Logger.getLogger(Cache.class.getName());

	private Cache() {
		super();
		logger.setLevel(Level.WARNING);
		pluginDir = Activator.getDefault().getStateLocation().toString();
		initRecordManager();
	}

	private void initRecordManager() {
		try {
			Properties props = new Properties();
			props.put(RecordManagerOptions.CACHE_SIZE, "500");
			props.put(RecordManagerOptions.AUTO_COMMIT, "false");
			props.put(RecordManagerOptions.THREAD_SAFE, "true");
			recman = RecordManagerFactory.createRecordManager(pluginDir
					+ DBNAME, props);
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Could not open/create jdbm database", e);
		}
	}

	/**
	 * get the HTree related to a java element given the handle
	 * 
	 * @param handle
	 * @return
	 */
	private HTree getHashtableForHandle(String handle) {
		HTree hashtable = metrics.get(handle);
		if (null == hashtable) {
			try {
				long recid = recman.getNamedObject(handle);
				if (recid != 0) {
					hashtable = HTree.load(recman, recid);
				} else {
					hashtable = HTree.createInstance(recman);
					recman.setNamedObject(handle, hashtable.getRecid());
				}
				metrics.put(handle, hashtable);
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Counld not get cached value for "
						+ handle, e);
			}
		}
		return hashtable;
	}

	/**
	 * Put a given handle 's metric value to the database
	 * 
	 * @param handle
	 * @param value
	 */
	public void put(String handle, MetricValue value) {
		if (null == value)
			return;
		try {
			getHashtableForHandle(handle).put(value.getName(), value);
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Could not store " + handle, e);
		}
	}

	/**
	 * Get the metric result given handle and the metric name
	 * 
	 * @param handle
	 * @param metricName
	 * @return
	 */
	public MetricValue get(String handle, String metricName) {
		try {
			return (MetricValue) getHashtableForHandle(handle).get(metricName);
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Could not get " + handle, e);
			return null;
		}
	}

	/**
	 * Remove the handle's recored
	 * 
	 * @param handle
	 */
	public void remove(String handle) {
		try {
			metrics.remove(handle);
			long id = recman.getNamedObject(handle);
			if (id != 0) {
				recman.delete(id);
				recman.commit();
			}
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Could not remove " + handle, e);
		}
	}

	/**
	 * Close the database
	 */
	public void close() {
		try {
			recman.commit();
			recman.close();
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Could not get close the jdbm database ",
					e);
		}
	}

	/**
	 * Commit to the jdbm database
	 */
	public void commit() {
		try {
			recman.commit();
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Could not commit to the jdbm database ",
					e);
		}

	}

	/**
	 * clean out entire database and set up a new one
	 */
	public void clear() {
		try {
			recman.close();
			File db = new File(pluginDir + DBNAME + ".db");
			db.delete();
			db = new File(pluginDir + DBNAME + ".lg");
			db.delete();
			logger.info(pluginDir + DBNAME + ".db/.lg" + " deleted....");
			initRecordManager();
			metrics.clear();
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Error deleting jdbm database ", e);
		}
	}
}
