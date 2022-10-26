package com.nextlabs.smartclassifier.plugin.action.bj;

public interface ProcessManager {
	/**
	 * Executes console command
	 * 
	 * @param command
	 *            console command to be executed
	 * @return Console output from the command operation
	 */
	public String execute(String command);
}
