package com.nextlabs.smartclassifier.plugin.action.bj;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlManager {
	private static final String XMLNS = "xmlns";
	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	private static final String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String XMLNS_XSI = "xmlns:xsi";
	private static final String XMLNS_XSD = "xmlns:xsd";
	private static final String SISL_VERSION_0 = "0";
	private static final String SISL_VERSION = "sislVersion";
	private static final String SIE_INTERNAL_LABEL = "http://www.boldonjames.com/2008/01/sie/internal/label";

	private static final String POLICY = "policy";
	private static final String SISL = "sisl";
	private static final String UID = "uid";
	private static final String ELEMENTS = "elements";
	private static final String ELEMENT = "element";
	private static final String VALUE = "value";

	private static final String EMPTY_VALUE = "";
	private static final String XML_VERSION = "1.0";
	private static final boolean XML_STANDALONE = true;
	private static final String US_ASCII_ENCODING = "us-ascii";

	private static final String REPLACEMENT_REGEX = "\n|\r";

	private static final Logger logger = LogManager.getLogger(XmlManager.class);

	/**
	 * Serializes a Map Object into an XML String
	 * 
	 * @param map
	 *            Map Object to be serialized in to XML String
	 * @return XML String
	 */
	public String serialize(Map<String, Object> map) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		StringWriter writer = null;
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			document.setXmlVersion(XML_VERSION);
			document.setXmlStandalone(XML_STANDALONE);

			Node sislNode = createSislNode(document, map);
			document.appendChild(sislNode);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, US_ASCII_ENCODING);

			writer = new StringWriter();
			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(writer);
			transformer.transform(domSource, streamResult);
			String serializedOutput = writer.getBuffer().toString().replaceAll(REPLACEMENT_REGEX, EMPTY_VALUE);
			return serializedOutput;
		} catch (ParserConfigurationException e) {
			logger.error(e);
			return null;
		} catch (TransformerConfigurationException e) {
			logger.error(e);
			return null;
		} catch (TransformerException e) {
			logger.error(e);
			return null;
		} finally {
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					// Should not reach here
					logger.error(e);
				}
			}
		}
	}

	/**
	 * Parses an XML String into a Map Object
	 * 
	 * @param xml
	 *            XML String to be parsed
	 * @return Map Object parsed from the XML String
	 */
	public Map<String, Object> parse(String xml) {
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);
			document.getDocumentElement().normalize();

			String policy = extractPolicy(document);
			List<String> elementList = extractElements(document);

			Map<String, Object> parsedMap = new HashMap<String, Object>();
			parsedMap.put(POLICY, policy);
			parsedMap.put(ELEMENTS, elementList);

			return parsedMap;
		} catch (ParserConfigurationException e) {
			logger.error(e);
			return new HashMap<String, Object>();
		} catch (SAXException e) {
			logger.error(e);
			return new HashMap<String, Object>();
		} catch (IOException e) {
			logger.error(e);
			return new HashMap<String, Object>();
		} finally {
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					// Should not reach here
					logger.error(e);
				}
			}
		}
	}

	/**
	 * Creates the SISL Node of the XML
	 * 
	 * @param document
	 *            XML Document which the Node is to be created in
	 * @param map
	 *            Map Object containing the value of the SISL node
	 * @return Created SISL Node
	 */
	@SuppressWarnings("unchecked")
	private Node createSislNode(Document document, Map<String, Object> map) {
		Element sisl = document.createElement(SISL);
		sisl.setAttribute(POLICY, (String) map.get(POLICY));
		sisl.setAttribute(XMLNS_XSD, XML_SCHEMA);
		sisl.setAttribute(XMLNS_XSI, XML_SCHEMA_INSTANCE);
		sisl.setAttribute(SISL_VERSION, SISL_VERSION_0);
		sisl.setAttribute(XMLNS, SIE_INTERNAL_LABEL);

		// Create Element Nodes
		List<String> elements = (List<String>) map.get(ELEMENTS);
		for (int i = 0; i < elements.size(); i++) {
			String element = elements.get(i);
			Node elementNode = createElementNode(document, element);
			sisl.appendChild(elementNode);
		}

		return sisl;
	}

	/**
	 * Creates the Element Node for the given XML ID
	 * 
	 * @param document
	 *            Document which the Element is to be created
	 * @param uid
	 *            XML ID of the Element
	 * @return Created Element Node
	 */
	private Node createElementNode(Document document, String uid) {
		Element element = document.createElement(ELEMENT);
		element.setAttribute(UID, uid);
		element.setAttribute(VALUE, EMPTY_VALUE);

		return element;
	}

	/**
	 * Extracts the Policy ID from the XML Document
	 * 
	 * @param document
	 *            XML Document which the Policy ID is to be extracted from
	 * @return String representation of the Policy ID
	 */
	private String extractPolicy(Document document) {
		NodeList nodeList = document.getElementsByTagName(SISL);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String uid = element.getAttribute(POLICY);
				return uid;
			}
		}
		return null;
	}

	/**
	 * Extracts the Element XML ID from the XML Document
	 * 
	 * @param document
	 *            XML Document which the Element IDs are to be extracted from
	 * @return List of String representation of the Element IDs
	 */
	private List<String> extractElements(Document document) {
		List<String> elementList = new ArrayList<String>();
		NodeList nodeList = document.getElementsByTagName(ELEMENT);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String uid = element.getAttribute(UID);
				elementList.add(uid);
			}
		}
		return elementList;
	}
}
