package com.nextlabs.smartclassifier.plugin.action.bj;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SimpleProcessManager implements ProcessManager {
	private static final int BUFFER_SIZE = 1024;
	private static final int EMPTY_SIZE = 0;
	private static final int STARTING_INDEX = 0;
	private static final String UTF_8 = "UTF-8";
	private static final String EMPTY_STRING = "NXL: EMPTY OUTPUT";

	private static final Logger logger = LogManager.getLogger(SimpleProcessManager.class);

	@Override
	public String execute(String command) {
		InputStream inputStream = null;
		try {
			Process process = Runtime.getRuntime().exec(command);
			process.getOutputStream().close();
			inputStream = process.getInputStream();

			String output = convertToString(inputStream);
			logger.debug(output);
			return output;
		} catch (IOException e) {
			logger.error(e);
			logger.debug(EMPTY_STRING);
			return EMPTY_STRING;
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
	 * Converts InputStream to String (to be used to convert Console Output)
	 * 
	 * @param inputStream
	 *            InputStream object to be converted to String
	 * @return String representation of the InputStream
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private String convertToString(InputStream inputStream) throws UnsupportedEncodingException, IOException {
		char[] buffer = new char[BUFFER_SIZE];
		StringBuilder output = new StringBuilder();
		Reader inputStreamReader = new InputStreamReader(inputStream, UTF_8);

		for (;;) {
			int readSize = inputStreamReader.read(buffer, STARTING_INDEX, buffer.length);
			if (readSize < EMPTY_SIZE)
				break;
			output.append(buffer, STARTING_INDEX, readSize);
		}

		if (inputStreamReader != null) {
			inputStreamReader.close();
		}

		return output.toString();
	}

}
