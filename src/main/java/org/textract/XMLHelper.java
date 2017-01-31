
package org.textract;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;


public class XMLHelper {

	XPathAPI xpathAPI;
	transient TransformerFactory transformerFactory;
	DocumentBuilder docBuilder;
	Document document;
	static boolean debugFlag = false;

	public XMLHelper() {
		debugFlag = false;
		initialize();
	}

	public XMLHelper(boolean yesNo) {
		debugFlag = yesNo;
		initialize();
	}
	
	private void initialize() {
		try {
			// init XPath
			xpathAPI = new XPathAPI();
			// init Parser
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(true);
			docBuilder = docFactory.newDocumentBuilder();
			initTransformer();
		} catch (Exception e) {
			logInfo("Error: initialize(): "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	void initTransformer() {
		try {
			String key = "javax.xml.transform.TransfomerFactory";
			String value = "org.apache.xalan.processor.TransformerFactoryImpl";
			System.setProperty(key, value);
			transformerFactory = TransformerFactory.newInstance();
		} catch (Exception e) {
			logInfo("Error: initTransformer(): "+e.getMessage());
			e.printStackTrace();
		}
	}

	public void load(String xmlSource) {
		logInfo("load(String)");
		try {
			InputStream inStream = new ByteArrayInputStream(xmlSource.getBytes());
			document = docBuilder.parse(inStream);
		} catch (Exception e) {
			document=null;
			logInfo("Error: load(String): "+e.getMessage());
			e.printStackTrace();
		}
	}

	public void load(InputStream xmlStream) {
		logInfo("load(InputStream)");
		try {
			document = docBuilder.parse(xmlStream);
		} catch (Exception e) {
			document=null;
			logInfo("Error: load(String): "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public Element getRoot() {
		return document.getDocumentElement();
	}
	
	public Document getDocument() {
		return document;
	}
	
	public void createDocument(String docElemName) {
		document = docBuilder.newDocument();
		Node node = document.createElement(docElemName);
		document.appendChild(node);
	}
	
	public void setAttribute(String name, String value) {
		if (name != null && value != null)
			document.getDocumentElement().setAttribute(name, value);
	}
	
	public void setElementAttribute(String elemName, String name, String value) {
		Element elem = (Element) getElem(elemName);
		elem.setAttribute(name, value);
	}

	public Node createElement(String name) {
		return (Node) document.createElement(name); 
	}
	
	public Node createElement(String namespaceURI, String elemName) {
		return (Node) document.createElementNS(namespaceURI, elemName);
	}
	
	public Node addChild(String child) {
		Node node = createElement(child);
		document.getDocumentElement().appendChild(node);
		return node;
	}
	
	public Node addChild(String parent, Node child) {
		Node node = getElem(parent);
		return node.appendChild(child);
	}
	
	public Text createText(String value) {
		Text tNode = document.createTextNode(value);
		return tNode;
	}
	
	public void addTextChild(Node parent, Text child) {	
//		Node node = getElem(parent);
		parent.appendChild(child);
	}
	
	public void addTextNode(String parent, String elemName, String value) {
		Node newNode = createElement(elemName);
		Node childNode = addChild(parent, newNode);
		Text tNode = createText(value);
		addTextChild(childNode, tNode);
	}
	public void addTextNode(Node parent, String elemName, String value) {
		Node newNode = createElement(elemName);
		Node childNode = parent.appendChild(newNode);
		Text tNode = createText(value);
		addTextChild(childNode, tNode);
	}
	
	public void replaceTextNode(Node parent, String value) {
		NodeList childList = parent.getChildNodes();
		if (childList != null) {
			for (int i=0;i<childList.getLength();i++) {
				Node currNode = childList.item(i);
				if (currNode.getNodeType() == Node.TEXT_NODE) {
					currNode.setNodeValue(value);
				}
			}
		}
	}
	
	public NodeIterator fetchNodeIterator(Node start, String xpath) throws TransformerException {
		NodeIterator nodeIter = XPathAPI.selectNodeIterator(start, xpath);
		return nodeIter;
	}
	
	/*
	 * getElem(String xpath)
	 * 
	 * Always specify xpath from root !
	 */
	public Node getElem(String xpath) {
		logInfo("getElem(String)");
		Node target = null;
		try {
			logInfo("DocumentElement: "+document.getDocumentElement().getNodeName());
			target = XPathAPI.selectSingleNode(document.getDocumentElement(), xpath);
		} catch (Exception e) {
			logInfo("Error: getElem(String): "+e.getMessage());
			e.printStackTrace();
		}
		return target;
	}
	
	public String[] getAllElemValue(Node start, String xpath) {
		logInfo("getAllElemValue(Node, String)");
		String vals[]=null;
		try {
			NodeList nodes = XPathAPI.selectNodeList(start, xpath);
			if (nodes != null) {
				logInfo("NodeList is not NULL");
				if(nodes.getLength()>0)
				{
					vals = new String[nodes.getLength()];
					for(int i=0;i<nodes.getLength();i++)
					{
						vals[i]=((Text)nodes.item(i).getFirstChild()).getData();
					}
				}
			
			}
		} catch (Exception e) {
			logInfo("Error: getAllElemValue(Node, String): "+e.getMessage());
			e.printStackTrace();
		}
		return vals;
	}
	
	public Properties getPIAttrValues() {
		logInfo("getPIAttrValues()");
		Properties props = new Properties();
		try {
			Node node = document.getFirstChild();
			logInfo("Node Name: "+node.getNodeName());
			if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
				String value = node.getNodeValue(); 
				StringTokenizer tokenizer1 = new StringTokenizer(value, " ");
				while (tokenizer1.hasMoreTokens()) {
					String attr = tokenizer1.nextToken();
					StringTokenizer tokenizer2 = new StringTokenizer(attr, "=");
					while (tokenizer2.hasMoreTokens()) {
						String attrName = tokenizer2.nextToken();
						logInfo("Key = "+attrName);
						String attrValue = tokenizer2.nextToken();
						logInfo("Value = "+attrValue);
						props.put(attrName, attrValue);
					}
				}
			}
		} catch (Exception e) {
			logInfo("Error: getPIAttrValues(): "+e.getMessage());
			e.printStackTrace();
		}
		return props;
	}	
	
	public Node[] getElements() {
		logInfo("getElements()");
		Node[] nodes = null;
		try {
			NodeList childList = document.getChildNodes();
			int length = childList.getLength();
			nodes = new Node[length];
			for (int i=0;i<length;i++) {
				Node currNode = childList.item(i);
				if (currNode.getNodeType() == Node.ELEMENT_NODE)
					nodes[i] = currNode;
			}
		} catch (Exception e) {
			logInfo("Error: getElem(String): "+e.getMessage());
			e.printStackTrace();
		}
		return nodes;
	}
	
	public String getElemValue(Node start, String xpath) {
		logInfo("getElemValue(Node, String)");
		try {
			Node target = XPathAPI.selectSingleNode(start, xpath);
			if (target != null) {
				Text tNode = (Text) target.getFirstChild();
				if (tNode == null) {
					return "";
				} else {
					logInfo("Got 'Text' using "+xpath+" "+tNode.getData());
					return tNode.getData();
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			logInfo("Error: getElemValue(Node, String): "+e.getMessage());
			e.printStackTrace();
		}
		return "";
	}
	
	public String seekElemValue(String xmlSource, String xpath) {
		logInfo("seekElemValue(String, String)");
		try {
			load(xmlSource);
			return getElemValue((Node) document.getDocumentElement(), xpath);
		} catch (Exception e) {
			logInfo("Error: seekElemValue(String, String): "+e.getMessage());
			e.printStackTrace();
		}
		return "";
	}
	
	public void mergeDocument(Document newDoc) {
		logInfo("mergeDocument(Document)");
		try {
			Node impNode = document.importNode(newDoc.getDocumentElement(), true);
			getRoot().appendChild(impNode);
			//System.out.println("impNode.NodeName: "+impNode.getNodeName());
		} catch (Exception e) {
			logInfo("Error: mergeDocument(Document): "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String serialize() {
		logInfo("serialize()");
		try {
			StringWriter sw = new StringWriter();
			OutputFormat oFormat = new OutputFormat(document);
			oFormat.setIndenting(true);
			XMLSerializer xmlSer = new XMLSerializer(sw, oFormat);
			xmlSer.serialize(document);
			return sw.getBuffer().toString();
		} catch (Exception e) {
			logInfo("Error: serialize(): "+e.getMessage());
			e.printStackTrace();
		}
		return "";
	}
	
	public static String serialize(Document doc) {
		logInfo("serialize(Document)");
		try {
			StringWriter sw = new StringWriter();
			OutputFormat oFormat = new OutputFormat(doc);
			oFormat.setIndenting(true);
			XMLSerializer xmlSer = new XMLSerializer(sw, oFormat);
			xmlSer.serialize(doc);
			return sw.getBuffer().toString();
		} catch (Exception e) {
			logInfo("Error: serialize(Document): "+e.getMessage());
			e.printStackTrace();
		}
		return "";
	}
	
	public Document transform(InputStream xslStream) {
		DOMSource xmlIn = new DOMSource(document);
		DOMResult xmlOut = new DOMResult();
		try {
			Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslStream));
			transformer.transform(xmlIn, xmlOut);
		} catch (Exception e) {
			logInfo("Error: transform(): "+e.getMessage());
		}
		return (Document) xmlOut.getNode();
	}

	public Document transform(Transformer t) {
		DOMSource xmlIn = new DOMSource(document);
		DOMResult xmlOut = new DOMResult();
		try {
			//Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslStream));
			t.transform(xmlIn, xmlOut);
		} catch (Exception e) {
			logInfo("Error: transform(): "+e.getMessage());
		}
		return (Document) xmlOut.getNode();
	}
	
	public void print() {
		logInfo("print()");
		System.out.println("XML = \n"+serialize());
	}

	// test
	public static void main(String[] args) {
		StringBuffer sb = new StringBuffer();

		sb.append("<?xml version=\"1.0\"?>")
			.append("<BillDispatch2>")
			.append("<ControlInfo2>")
			.append("<ExternalID2>")
			.append("9840209763")
			.append("</ExternalID2>")
			.append("<ExternalIDType2>")
			.append("11")
			.append("</ExternalIDType2>")
			.append("</ControlInfo2>")
			.append("<BillDispMeth2>")
			.append("yes")
			.append("</BillDispMeth2>")
			.append("</BillDispatch2>");
			//System.out.println(sb.toString());

		XMLHelper dataHelper = new XMLHelper(true);
		dataHelper.createDocument("BillDispatch1");
		Node node = dataHelper.createElement("ControlInfo");
		dataHelper.addChild("/BillDispatch1", node);

		dataHelper.addTextNode("/BillDispatch1/ControlInfo", "ExternalID", "9840209763");
		dataHelper.addTextNode("/BillDispatch1", "BillDispMeth", "YES");

		dataHelper.addTextNode("/BillDispatch1/ControlInfo", "ExternalID", "9880101320");
		dataHelper.addTextNode("/BillDispatch1", "BillDispMeth", "NO");
		
		dataHelper.print();
		logInfo("ExternalID: "+dataHelper.getElemValue(dataHelper.getRoot(), "/BillDispatch1/ControlInfo/ExternalID"));
		logInfo("BillDispMeth: "+dataHelper.getElemValue(dataHelper.getRoot(), "/BillDispatch1/BillDispMeth"));
		XMLHelper dataHelper2 = new XMLHelper(true);
		dataHelper2.load(sb.toString());
		dataHelper.mergeDocument(dataHelper2.getDocument());
		String xml = dataHelper.serialize();
		System.out.println("After import:\n"+xml);
	}
	
	static void logInfo(String s) {
		if (debugFlag)  {
			System.out.println(s);
		}
	}
	/**
	 * @param document
	 */
	public void setDocument(Document document) {
		this.document = document;
	}

}
