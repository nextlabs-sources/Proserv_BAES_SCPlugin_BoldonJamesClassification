package com.nextlabs.smartclassifier.plugin.action.bj;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PowerClassifierManager {
	private static final String LOG_NULL_POWER_CLASSIFIER_INSTALLATION_PATH_USING_CURRENT_DIRECTORY = "Null Power Classifier Installation Path, using current directory";
	private static final String LOG_EMPTY_POWER_CLASSIFIER_INSTALLATION_PATH_USING_CURRENT_DIRECTORY = "Empty Power Classifier Installation Path, using current directory";

	private static final String ESCAPED_DOUBLE_QUOTE = "\\\\\"";
	private static final String DOUBLE_QUOTE = "\"";

	private static final String STRUCTURE_PATH_TO_DLL_DIRECTORY = "PowerClassifierForFiles";

	private static final String POWERSHELL_FORMAT = "powershell \"%s\"";
	private static final String GETTER_FORMAT = "Import-Module '%s'; $classificationExtractor=Get-Classification -FileList \"%s\"; echo $classificationExtractor.SISL";
	private static final String SETTER_FORMAT = "Import-Module '%s'; $classificationUpdater=Set-Classification -FileList \"%s\" -SISL '%s' -SetUpdateMarkings; echo $classificationUpdater.Status; echo $classificationUpdater.File.Name; echo $classificationUpdater.Classification";

	private static final Logger logger = LogManager.getLogger(PowerClassifierManager.class);

	private ProcessManager processManager;
	private String powerClassifierPath;

	public PowerClassifierManager(String powerClassifierPath) {
		this.powerClassifierPath = generatePowerClassifierDllPath(powerClassifierPath);
		this.processManager = new SimpleProcessManager();
	}

	/**
	 * Gets the classification in the form of XML for the file in the given path
	 * 
	 * @param path
	 *            path to the file
	 * @return Stringified XML classification of the file
	 */
	public String get(String path) {
		String command = createGetterCommand(path);
		return execute(command);
	}

	/**
	 * Sets the classification specified to the file in the given path
	 * 
	 * @param path
	 *            path to the file
	 * @param sisl
	 *            Stringified classification XML
	 * @return Status of the classification setting operation
	 */
	public String set(String path, String sisl) {
		String command = createSetterCommand(path, sisl);
		return execute(command);
	}

	/**
	 * Generates the child directory path from the path of Installation Home of
	 * Power Classifier
	 * 
	 * @param powerClassifierPath
	 *            Path to the Installation Home of Power Classifier
	 * @return Path to the exact DLL directory within the Installation Home of
	 *         Power Classifier
	 */
	private String generatePowerClassifierDllPath(String powerClassifierPath) {
		String dllPath = powerClassifierPath;

		if (dllPath == null) {
			dllPath = ".";
			logger.debug(LOG_NULL_POWER_CLASSIFIER_INSTALLATION_PATH_USING_CURRENT_DIRECTORY);
		}

		if (dllPath.isEmpty()) {
			dllPath = ".";
			logger.debug(LOG_EMPTY_POWER_CLASSIFIER_INSTALLATION_PATH_USING_CURRENT_DIRECTORY);
		}

		if (!powerClassifierPath.endsWith("/") && !powerClassifierPath.endsWith("\\")) {
			// Add Path Seperator
			dllPath = dllPath + "\\";
		}

		// Add Struture Path
		dllPath = dllPath + STRUCTURE_PATH_TO_DLL_DIRECTORY;

		return dllPath;
	}

	/**
	 * Executes a command on the Power Shell
	 * 
	 * @param command
	 *            Command to be executed
	 * @return Console output from the command
	 */
	private String execute(String command) {
		String powershellCommand = createPowershellCommand(command);
		logger.debug(powershellCommand);
		String output = this.processManager.execute(powershellCommand);
		return output;
	}

	/**
	 * Generates Getter Command to retrieve the Classification XML
	 * 
	 * @param path
	 *            Path of the file which the Classification XML is to be
	 *            retrieved
	 * @return Command to retrieve the Classification XML
	 */
	private String createGetterCommand(String path) {
		String command = String.format(GETTER_FORMAT, this.powerClassifierPath, path);
		return command;
	}

	/**
	 * Generates the Setter Command to set the Classification XML
	 * 
	 * @param path
	 *            Path of the file which Classification XML is to be saved
	 * @param sisl
	 *            Classification XML to be saved
	 * @return Command to save the classification XML
	 */
	private String createSetterCommand(String path, String sisl) {
		String command = String.format(SETTER_FORMAT, this.powerClassifierPath, path, sisl);
		return command;
	}

	/**
	 * Generates the wrapper command to run commands in Power Shell
	 * 
	 * @param command
	 *            wrapped command to be run in Power Shell
	 * @return Full command to run Power Shell with parameters
	 */
	private String createPowershellCommand(String command) {
		String escapedCommand = command.replaceAll(DOUBLE_QUOTE, ESCAPED_DOUBLE_QUOTE);
		String powershellCommand = String.format(POWERSHELL_FORMAT, escapedCommand);
		return powershellCommand;
	}

}
