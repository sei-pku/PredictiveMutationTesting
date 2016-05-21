package edu.pku.sei.metric.xml;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * To print out a XML style file
 *
 * @author liushi07
 *
 */
public class XMLPrintStream extends PrintStream {

	public final static String XML = "<?xml version=\"1.0\" encoding=\"GBK\"?>";
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * @param out
	 */
	public XMLPrintStream(OutputStream out) {
		super(out);
	}

	public void printXMLHeader() {
		println(XML);
	}

	/**
	 * @param level
	 */
	public void indent(int level) {
		if (level > 0) {
			StringBuffer b = new StringBuffer("");
			for (int i = 0; i < level; i++)
				b.append("   ");
			print(b.toString());
		}
	}

	/**
	 * @param d
	 * @return
	 */
	public String formatXSDDate(Date d) {
		synchronized (df) {
			return df.format(d).toString();
		}
	}
}
