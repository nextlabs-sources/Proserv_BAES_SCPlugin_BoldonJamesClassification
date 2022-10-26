package com.nextlabs.smartclassifier.plugin.action.addboldonjamesclassification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;

import com.nextlabs.smartclassifier.constant.ActionResult;
import com.nextlabs.smartclassifier.plugin.action.ActionOutcome;
import com.nextlabs.smartclassifier.plugin.action.ExecuteOncePerFile;
import com.nextlabs.smartclassifier.plugin.action.SharedFolderAction;
import com.nextlabs.smartclassifier.plugin.action.bj.PowerClassifierManager;
import com.nextlabs.smartclassifier.plugin.action.bj.SpifParser;
import com.nextlabs.smartclassifier.plugin.action.bj.XmlManager;

public class AddBoldonJamesClassification extends SharedFolderAction implements ExecuteOncePerFile {
	private static final String TOTAL_TIME_TAKEN = "Total Time Taken: ";
	private static final String POWER_CLASSIFIER_TIME_TAKEN = "Power Classifier Time Taken: ";
	private static final String SPIF_LOADING_TIME_TAKEN = "SPIF Loading Time Taken: ";
	private static final String TAGS_TO_BE_ADDED = "Tags to be added: ";
	private static final String SPIF_LOCATION = "SPIF Location: ";
	private static final String LOG_EXCLUDE_DIRECTORY_IS_EMPTY = "Exclude Directory is Empty";
	private static final String LOG_EXCLUDE_DIRECTORY_IS_NULL = "Exclude Directory is Null";
	private static final String LOG_INVALID_FILE_PATH = "Invalid File Path";
	private static final String LOG_PATH_IS_NULL = "File path is Null";
	private static final String LOG_PATH_IS_EMPTY = "File path is Empty";
	private static final String LOG_POWER_CLASSIFIER_HOME_PATH_IS_NULL = "Power Classifier Home Path is Null";
	private static final String LOG_POWER_CLASSIFIER_HOME_PATH_IS_EMPTY = "Power Classifier Home Path is Empty";
	private static final String LOG_SPIF_PATH_IS_NULL = "Boldon James Configuration File path is null";
	private static final String LOG_SPIF_PATH_IS_EMPTY = "Boldon James Configuration File path is Empty";
	private static final String LOG_SPIF_SECURITY_CATEGORY_TAG_SET_IS_NULL = "Labels to Remove is null";
	private static final String LOG_PROCESSING = "Processing: ";
	private static final String LOG_PROCESS_OUTPUT = "Process Output: ";
	private static final String LOG_GET_CLASSIFICATION_XML_OUTPUT = "Get-Classification XML output: ";

	private static final String LOG_OK = "Process Completed Successfully";
	private static final String LOG_FAIL = "Process Failed";
	private static final String LOG_IGNORE = "Process Ignored";

	private static final String MESSAGE_OK = "Everything OK";
	private static final String MESSAGE_FAIL = "Failed to add Boldon James Classification";
	private static final String MESSAGE_IGNORE = "File Ignored";

	private static final String KEY_ID = "id";
	private static final String KEY_PATH = KEY_ID;
	private static final String KEY_SPIF_PATH = "spif_path";
	private static final String KEY_SPIF_SECURITY_CATEGORY_TAG_SET = "spif_security_category_tag_set";
	private static final String KEY_POWER_CLASSIFIER_PATH = "power_classifier_path";
	private static final String KEY_EXCLUDE_FOLDER = "exclude_folder";

	private static final String KEY_ELEMENTS = "elements";
	private static final String NEW_LINE = "\n|\r";
	private static final int FIRST_ELEMENT = 0;
	private static final String PATH_SEPERATOR_REGEX = "/|\\\\";

	private static final String STATUS_XML_NOT_AVAILABLE = "Xml Not Available";
	private static final String STATUS_LABEL_WRITTEN_SUCCESSFULLY = "label written successfully";
	private static final String STATUS_IS_NULL = "Status is Null";
	private static final String STATUS_IS_EMPTY = "Status is Empty";

	private static final Logger logger = LogManager.getLogger(AddBoldonJamesClassification.class);
	private static final String ACTION_NAME = "ADD_BOLDON_JAMES_CLASSIFICATION";

	public AddBoldonJamesClassification() {
		super(ACTION_NAME);
	}

	@Override
	public ActionOutcome execute(final SolrDocument document) {
		long start = System.nanoTime();
		
		// Get File Path
		String path = (String) document.get(KEY_PATH);
		if (!isPathValid(path)) {
			logger.debug(LOG_INVALID_FILE_PATH);
			return generateFailOutcome();
		}

		logger.info(LOG_PROCESSING + path);

		// Check for file exclusion
		if (shouldExclude(path)) {
			return generateIgnoreOutcome();
		}

		// Process
		boolean success = process(path);

		long end = System.nanoTime();
		logger.debug(TOTAL_TIME_TAKEN + Long.toString((end - start) / 1000000));
		// Return Action Outcome
		if (success) {
			return generateSuccessOutcome();
		} else {
			return generateFailOutcome();
		}
	}

	/**
	 * Check for path validity (null and empty check)
	 * 
	 * @param path
	 *            Path to be checked
	 * @return True if not Null and not Empty, False otherwise
	 */
	private boolean isPathValid(String path) {
		if (path == null) {
			logger.debug(LOG_PATH_IS_NULL);
			return false;
		} else if (path.isEmpty()) {
			logger.debug(LOG_PATH_IS_EMPTY);
			return false;
		}

		return true;
	}

	/**
	 * Generates a Failed Action Outcome
	 * 
	 * @return Failed Action Outcome
	 */
	private ActionOutcome generateFailOutcome() {
		ActionOutcome outcome = new ActionOutcome();
		outcome.setResult(ActionResult.FAIL);
		outcome.setMessage(MESSAGE_FAIL);
		logger.info(LOG_FAIL);
		return outcome;
	}

	/**
	 * Generates a Success Action Outcome
	 * 
	 * @return Success Action Outcome
	 */
	private ActionOutcome generateSuccessOutcome() {
		ActionOutcome outcome = new ActionOutcome();
		outcome.setResult(ActionResult.SUCCESS);
		outcome.setMessage(MESSAGE_OK);
		logger.info(LOG_OK);
		return outcome;
	}

	/**
	 * Generates an Ignored Action Outcome
	 * 
	 * @return Ignore Action Outcome
	 */
	private ActionOutcome generateIgnoreOutcome() {
		ActionOutcome outcome = new ActionOutcome();
		outcome.setResult(ActionResult.SUCCESS);
		outcome.setMessage(MESSAGE_IGNORE);
		logger.info(LOG_IGNORE);
		return outcome;
	}

	/**
	 * Processes the file of the given path
	 * 
	 * @param path
	 *            Path to the file
	 * @return status of whether the process is successful
	 */
	private boolean process(String path) {
		// SPIF
		String spifPath = getParameterByKey(KEY_SPIF_PATH).trim();
		Set<String> toBeAddedSecurityCategoryTagSet = getParametersByKey(KEY_SPIF_SECURITY_CATEGORY_TAG_SET);
		logger.debug(SPIF_LOCATION + spifPath);
		logger.debug(TAGS_TO_BE_ADDED + toBeAddedSecurityCategoryTagSet.toString());

		if (!isSpifParametersValid(spifPath, toBeAddedSecurityCategoryTagSet)) {
			// Assume Fail
			return false;
		}
		long start = System.nanoTime();
		List<String> spifList = loadSpifFromFile(spifPath, toBeAddedSecurityCategoryTagSet);
		String policyId = loadPolicyIdFromFile(spifPath);

		long end = System.nanoTime();
		logger.debug(SPIF_LOADING_TIME_TAKEN + Long.toString((end - start) / 1000000));
		// Power Classifier Manager
		String powerClassifierPath = getParameterByKey(KEY_POWER_CLASSIFIER_PATH).trim();

		if (!isPowerClassifierManagerParametersValid(powerClassifierPath)) {
			// Assume Fail
			return false;
		}
		PowerClassifierManager powerClassifierManager = new PowerClassifierManager(powerClassifierPath);

		// Add Classifications
		long start2 = System.nanoTime();
		String status = addRecordClassification(powerClassifierManager, path, spifList, policyId);
		long end2 = System.nanoTime();
		logger.debug(POWER_CLASSIFIER_TIME_TAKEN + Long.toString((end2 - start2) / 1000000));

		// Log if fail to remove
		if (!status.toLowerCase().startsWith(STATUS_LABEL_WRITTEN_SUCCESSFULLY)) {
			logger.warn(LOG_PROCESS_OUTPUT + status);
			return false;
		} else {
			logger.info(LOG_PROCESS_OUTPUT + status);
			return true;
		}
	}

	/**
	 * Check for Power Classifier Path Validity (null and empty check)
	 * 
	 * @param powerClassifierPath
	 *            Path to the Power Classifier
	 * @return True if the path is not Null and not Empty, False otherwise
	 */
	private boolean isPowerClassifierManagerParametersValid(String powerClassifierPath) {
		if (powerClassifierPath == null) {
			logger.debug(LOG_POWER_CLASSIFIER_HOME_PATH_IS_NULL);
			return false;
		} else if (powerClassifierPath.isEmpty()) {
			logger.debug(LOG_POWER_CLASSIFIER_HOME_PATH_IS_EMPTY);
			return false;
		}

		return true;
	}

	private boolean isSpifParametersValid(String spifPath, Set<String> toBeRemovedSecurityCategoryTagSet) {
		if (spifPath == null) {
			logger.debug(LOG_SPIF_PATH_IS_NULL);
			return false;
		}
		if (spifPath.trim().isEmpty()) {
			logger.debug(LOG_SPIF_PATH_IS_EMPTY);
			return false;
		}
		if (toBeRemovedSecurityCategoryTagSet == null) {
			logger.debug(LOG_SPIF_SECURITY_CATEGORY_TAG_SET_IS_NULL);
			return false;
		}

		return true;
	}

	private boolean shouldExclude(String path) {
		String excludeDirectory = getParameterByKey(KEY_EXCLUDE_FOLDER);

		// Check Exclude Directory
		if (excludeDirectory == null) {
			logger.debug(LOG_EXCLUDE_DIRECTORY_IS_NULL);
			return false;
		}
		if (excludeDirectory.isEmpty()) {
			logger.debug(LOG_EXCLUDE_DIRECTORY_IS_EMPTY);
			return false;
		}

		String lowerCaseExcludeDirectory = excludeDirectory.toLowerCase();
		boolean foundMatch = false;

		String lowerCasePath = path.toLowerCase();
		String[] splittedPath = lowerCasePath.split(PATH_SEPERATOR_REGEX);

		// Loop until second last, last is file name, ignore
		for (int i = 0; i < splittedPath.length - 1; i++) {
			String lowerCaseSplittedPath = splittedPath[i];

			// Check if is Directory to be excluded
			if (lowerCaseSplittedPath.equals(lowerCaseExcludeDirectory)) {
				foundMatch = true;
				break;
			}
		}

		return foundMatch;
	}

	@SuppressWarnings("unchecked")
	private String addRecordClassification(PowerClassifierManager powerClassifierManager, String path,
			List<String> spifList, String policyId) {

		// Create a new XML
		XmlManager xmlManager = new XmlManager();
		Map<String, Object> xmlMap = createNewXmlMap(policyId);

		// Update XML (Remove SPIF)
		List<String> elements = ((List<String>) xmlMap.get(KEY_ELEMENTS));

		elements.addAll(spifList);

		// Serialize Updated XML
		String updatedXml = xmlManager.serialize(xmlMap);

		// Set Updated XML
		String setterOutput = powerClassifierManager.set(path, updatedXml);

		// Get Status
		String[] splittedSetterOutput = setterOutput.split(NEW_LINE);
		String status = splittedSetterOutput[FIRST_ELEMENT];

		return getStatusString(status);
	}

	private Map<String, Object> createNewXmlMap(String policyId) {
		Map<String, Object> xmlMap = new HashMap<String, Object>();
		xmlMap.put("policy", policyId);
		xmlMap.put(KEY_ELEMENTS, new ArrayList<String>());
		return xmlMap;
	}

	private String getStatusString(String status) {
		if (status == null) {
			return STATUS_IS_NULL;
		} else if (status.isEmpty()) {
			return STATUS_IS_EMPTY;
		} else {
			return status;
		}
	}

	private List<String> loadSpifFromFile(String spifPath, Set<String> toBeAddedSecurityCategoryTagSet) {
		SpifParser spifParser = new SpifParser();

		// Add lower case tags to list
		List<String> toBeAdddedSecurityCategoryTagSetList = new ArrayList<String>();
		for (String toBeAddedSecurityCategoryTag : toBeAddedSecurityCategoryTagSet) {
			toBeAdddedSecurityCategoryTagSetList.add(toBeAddedSecurityCategoryTag.toLowerCase().trim());
		}

		// Load SPIF list from xml
		List<String> spifList = spifParser.parseForAdd(spifPath, toBeAdddedSecurityCategoryTagSetList);
		return spifList;
	}

	private String loadPolicyIdFromFile(String spifPath) {
		SpifParser spifParser = new SpifParser();
		String policyId = spifParser.parsePolicyId(spifPath);
		return policyId;
	}
	
	public static void main(String args[]){
				
		List<String> listTag = new ArrayList<String>();
		
		listTag.add("record category");
		
		SpifParser spifParser = new SpifParser();
		
		List<String> spifList = spifParser.parseForAdd("./conf/spif.xml", listTag);
		
		System.out.println(spifList);
	}
}
