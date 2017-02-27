package org.justin.demo.hydrousageparser;

import java.io.FileInputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {
	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	private Main() {
		// declare private constructor to prevent instantiation of this class
	}

	public static void main(final String[] args) throws Exception {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		final Document document;
		try (final FileInputStream fis = new FileInputStream("target/classes/DownLoadMyData-Hourly-2017.xml")) {
			document = documentBuilder.parse(fis);
		} catch (Exception ex) {
			throw new Exception("Unable to parse XML file.", ex);	// NOSONAR Define and throw a dedicated exception instead of using a generic one.
		}
		LOG.log(Level.INFO, "Parsed OK");
//		recursivePrintNodeList("", document.getChildNodes());
		final NodeList IntervalReadings = document.getElementsByTagName("IntervalReading");
		printHourly(IntervalReadings);
	}

	private static void recursivePrintNodeList(final String indent, final NodeList nodeList) {
		final int numChildNodes = nodeList.getLength();
		for (int i=0; i<numChildNodes; i++) {
			final Node node = nodeList.item(i);
			final String nodeName  = node.getNodeName();
			final String nodeValue = node.getNodeValue();
			System.out.println(indent + "Node[" + nodeName + "]=" + nodeValue);
			recursivePrintNodeList(" " + indent, node.getChildNodes());
		}
	}

	private static void printHourly(final NodeList intervalReadings) {
		final int numIntervalReadings = intervalReadings.getLength();
		for (int i=0; i<numIntervalReadings; i++) {
			final Node intervalReading = intervalReadings.item(i);
			final NodeList intervalReadingNodes = intervalReading.getChildNodes();
			final int numIntervalReadingNodes = intervalReadingNodes.getLength();
			String costValue = null;
			String timePeriodValue = null;
			for (int j=0; j<numIntervalReadingNodes; j++) {
				final Node intervalReadingNode = intervalReadingNodes.item(j);
				if (null == intervalReadingNode) {
					continue;
				}

				final String nodeName  = intervalReadingNode.getNodeName();
				if (null == nodeName) {
					// do nothing
				} else if (nodeName.equals("cost")) {
					costValue = intervalReadingNode.getFirstChild().getNodeValue();
				} else if (nodeName.equals("timePeriod")) {
					if (null == costValue) {
						continue;
					}
					final NodeList timePeriodNodes = intervalReadingNode.getChildNodes();
					final int numTimePeriodNodes = timePeriodNodes.getLength();
					for (int k=0; k<numTimePeriodNodes; k++) {
						final Node timePeriodChildNode = timePeriodNodes.item(k);
						final String timePeriodChildNodeName = timePeriodChildNode.getNodeName();
						if (null == timePeriodChildNodeName) {
							continue;
						} else if (timePeriodChildNodeName.equals("start")) {
							timePeriodValue = timePeriodChildNode.getFirstChild().getNodeValue();
							break;
						}
					}
				}
			}

			if ((null != costValue) && (null != timePeriodValue)) {
				final float costValueDollars = Long.parseLong(costValue) / 10000F;
				final long startValueMillis  = Long.parseLong(timePeriodValue);
				System.out.println(timePeriodValue + "," + costValue + "," +  new Date(startValueMillis*1000L) + ",$" + costValueDollars);
				costValue = null;
				timePeriodValue = null;
			}
		}
	}
}