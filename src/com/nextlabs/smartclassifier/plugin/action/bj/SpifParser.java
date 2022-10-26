package com.nextlabs.smartclassifier.plugin.action.bj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SpifParser {
	private static final String LOG_SPIF_FILE_IS_NOT_FILE = "SPIF File is not File";
	private static final String LOG_SPIF_FILE_DOES_NOT_EXIST = "SPIF File does not exist";

	private static final String SECURITY_CATEGORY_TAG_SET = "spif:securityCategoryTagSet";
	private static final String TAG_CATEGORY = "spif:tagCategory";
	private static final String NAME = "name";
	private static final String XML_ID = "xml:id";

	private static final Logger logger = LogManager.getLogger(SpifParser.class);

	/**
	 * Extracts the List of XML IDs from the Child Elements within the Category
	 * Tag Set Element
	 * 
	 * @param parentElement
	 *            Element which the child is to be extracted from
	 * @return List of XML IDs belonged to the Element
	 */
	private List<String> extractCategoryTagSet(Element parentElement) {
		List<String> wantedList = new ArrayList<String>();

		NodeList nodeList = parentElement.getElementsByTagName(TAG_CATEGORY);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String xmlId = element.getAttribute(XML_ID);

				wantedList.add(xmlId);
			}
		}

		return wantedList;
	}
	
	/**
	 * Extracts Policy ID from the SPIF file
	 * @param document	XML Document which is to be parsed
	 * @return Policy ID
	 */
	private String extractPolicyId(Document document) {
		NodeList nodeList = document.getElementsByTagName("spif:securityPolicyId");
		if(nodeList.getLength() > 0){
			Node node = nodeList.item(0);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String policyId = element.getAttribute(XML_ID);
				return policyId;
			}
			else{
				return "";
			}
		}
		else{
			return "";
		}
	}
	
	/**
	 * Extracts List of XML IDs from the list of Category Tag Set
	 * 
	 * @param document
	 *            XML Document which is to be parsed
	 * @param wantedSecurityCategoryTagSetList
	 *            List of Wanted Category Tag Set which the XML IDs is to be
	 *            extracted
	 * @return List of XML IDs belonged to the Category Tag Set
	 */
	private List<String> extractWantedList(Document document, List<String> wantedSecurityCategoryTagSetList) {
		
		System.out.println("Extract wanted list");
		NodeList nodeList = document.getElementsByTagName(SECURITY_CATEGORY_TAG_SET);
		List<String> wantedList = new ArrayList<String>();

		for (int i = 0; i < nodeList.getLength(); i++) {
			System.out.println("Loop test list");
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String name = element.getAttribute(NAME);
				String lowerCaseName = name.toLowerCase();

				if (wantedSecurityCategoryTagSetList.contains(lowerCaseName)) {
					List<String> categoryXmlIdList = extractCategoryTagSet(element);
					wantedList.addAll(categoryXmlIdList);
				}
			}
		}

		return wantedList;
	}

	/**
	 * Extracts List of XML IDs from the list of Category Tag Set
	 * 
	 * @param document
	 *            XML Document which is to be parsed
	 * @param wantedSecurityCategoryTagSetList
	 *            List of Wanted Category Tag Set which the XML IDs is to be
	 *            extracted
	 * @return List of XML IDs belonged to the Category Tag Set
	 */
	private List<String> extractWantedListForAdd(Document document, List<String> wantedSecurityCategoryTagSetList) {
		NodeList nodeList = document.getElementsByTagName(TAG_CATEGORY);
		List<String> wantedList = new ArrayList<String>();
		
		System.out.println("Node list length " + nodeList.getLength());

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String name = element.getAttribute(NAME);
				String lowerCaseName = name.toLowerCase();
				
				System.out.println("Node name " + name);

				if (wantedSecurityCategoryTagSetList.contains(lowerCaseName)) {
					wantedList.add(element.getAttribute(XML_ID));
				}
			}
		}

		return wantedList;
	}

	/**
	 * Checks for SPIF File validity (existence and is a File)
	 * 
	 * @param spifFile
	 *            SPIF File object to be checked
	 * @return True if File exists and is a File, False otherwise
	 */
	private boolean isSpifFileValid(File spifFile) {
		if (!spifFile.exists()) {
			logger.debug(LOG_SPIF_FILE_DOES_NOT_EXIST);
			return false;
		}
		if (!spifFile.isFile()) {
			logger.debug(LOG_SPIF_FILE_IS_NOT_FILE);
			return false;
		}

		return true;
	}

	/**
	 * Parses the SPIF file to retrieve the list of XML ID belonged to the list
	 * of Category Tag Set
	 * 
	 * @param spifPath
	 *            Path to the SPIF XML file (spif.xml)
	 * @param wantedSecurityCategoryTagSetList
	 *            List of Category Tag Set which the XML IDs are to be returned
	 * @return List of XML IDs belonged to the Category Tag Set
	 */
	public List<String> parse(String spifPath, List<String> wantedSecurityCategoryTagSetList) {
		File spifFile = new File(spifPath);
		if (!isSpifFileValid(spifFile)) {
			return new ArrayList<String>();
		}

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(spifFile);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);
			document.getDocumentElement().normalize();
			List<String> wantedList = extractWantedList(document, wantedSecurityCategoryTagSetList);
			return wantedList;
		} catch (FileNotFoundException e) {
			logger.error(e);
			return new ArrayList<String>();
		} catch (ParserConfigurationException e) {
			logger.error(e);
			return new ArrayList<String>();
		} catch (SAXException e) {
			logger.error(e);
			return new ArrayList<String>();
		} catch (IOException e) {
			logger.error(e);
			return new ArrayList<String>();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}
	
	/**
	 * Parses the SPIF file to retrieve the list of XML ID belonged to the list
	 * of Category Tag Set
	 * 
	 * @param spifPath
	 *            Path to the SPIF XML file (spif.xml)
	 * @param wantedSecurityCategoryTagSetList
	 *            List of Category Tag Set which the XML IDs are to be returned
	 * @return List of XML IDs belonged to the Category Tag Set
	 */
	public List<String> parseForAdd(String spifPath, List<String> wantedSecurityCategoryTagSetList) {
		File spifFile = new File(spifPath);
		if (!isSpifFileValid(spifFile)) {
			return new ArrayList<String>();
		}

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(spifFile);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);
			document.getDocumentElement().normalize();
			List<String> wantedList = extractWantedListForAdd(document, wantedSecurityCategoryTagSetList);
			return wantedList;
		} catch (FileNotFoundException e) {
			logger.error(e);
			return new ArrayList<String>();
		} catch (ParserConfigurationException e) {
			logger.error(e);
			return new ArrayList<String>();
		} catch (SAXException e) {
			logger.error(e);
			return new ArrayList<String>();
		} catch (IOException e) {
			logger.error(e);
			return new ArrayList<String>();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}
	
	public String parsePolicyId(String spifPath) {
		File spifFile = new File(spifPath);
		if (!isSpifFileValid(spifFile)) {
			return "";
		}

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(spifFile);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);
			document.getDocumentElement().normalize();
			String policyId = extractPolicyId(document);
			return policyId;
		} catch (FileNotFoundException e) {
			logger.error(e);
			return "";
		} catch (ParserConfigurationException e) {
			logger.error(e);
			return "";
		} catch (SAXException e) {
			logger.error(e);
			return "";
		} catch (IOException e) {
			logger.error(e);
			return "";
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}
}
