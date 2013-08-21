/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.shell.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import org.springframework.shell.core.CommandResult;
import org.springframework.util.FileCopyUtils;
import org.springframework.xd.shell.AbstractShellIntegrationTest;
import org.springframework.xd.shell.util.Table;
import org.springframework.xd.shell.util.TableRow;

/**
 * Provides an @After JUnit lifecycle method that will destroy the jobs that were created by calling executeJobCreate
 * 
 * @author Glenn Renfro
 * @author Gunnar Hillert
 * 
 */
public abstract class AbstractJobIntegrationTest extends AbstractShellIntegrationTest {

	private static final String MODULE_RESOURCE_DIR = "../spring-xd-shell/src/test/resources/spring-xd/xd/modules/job/";

	private static final String MODULE_TARGET_DIR = "../modules/job/";

	private static final String TEST_TASKLET = "test.xml";

	private static final String JOB_TASKLET = "job.xml";

	private static final String JOB_WITH_PARAMETERS_TASKLET = "jobWithParameters.xml";

	public static final String TMP_FILE = "./src/test/resources/TMPTESTFILE.txt";

	public static final String TEST_FILE = "./src/test/resources/client1.txt";

	public static final String MY_JOB = "myJob";

	public static final String JOB_DESCRIPTOR = "job";

	public static final String MY_TEST = "myTest";

	public static final String TEST_DESCRIPTOR = "test";

	public static final String MY_JOB_WITH_PARAMETERS = "myJobWithParameters";

	public static final String JOB_WITH_PARAMETERS_DESCRIPTOR = "jobWithParameters";

	private List<String> jobs = new ArrayList<String>();

	@Before
	public void before() {
		copyTaskletDescriptorsToServer(MODULE_RESOURCE_DIR + JOB_TASKLET, MODULE_TARGET_DIR + JOB_TASKLET);
		copyTaskletDescriptorsToServer(MODULE_RESOURCE_DIR + TEST_TASKLET, MODULE_TARGET_DIR + TEST_TASKLET);
		copyTaskletDescriptorsToServer(MODULE_RESOURCE_DIR + JOB_WITH_PARAMETERS_TASKLET, MODULE_TARGET_DIR
				+ JOB_WITH_PARAMETERS_TASKLET);
		// clear any test jobs that may still exist
		try {
			executeJobDestroy(MY_JOB);
		}
		catch (Throwable t) {
			// don't worry if it is thrown
		}
		try {
			executeJobDestroy(MY_TEST);
		}
		catch (Throwable t) {
			// don't worry if it is thrown
		}
		try {
			executeJobDestroy(MY_JOB_WITH_PARAMETERS);
		}
		catch (Throwable t) {
			// don't worry if it is thrown
		}
	}

	@After
	public void after() {
		executeJobDestroy(jobs.toArray(new String[jobs.size()]));
		removeTmpFile(TEST_FILE);
		removeTmpFile(TMP_FILE);
	}

	public boolean fileExists(String name) {
		File file = new File(name);
		return file.exists();
	}

	/**
	 * Execute 'job destroy' for the supplied stream names
	 */
	protected void executeJobDestroy(String... jobNames) {
		for (String jobName : jobNames) {
			CommandResult cr = executeCommand("job destroy --name " + jobName);
			assertTrue("Failure to destroy job " + jobName + ".  CommandResult = " + cr.toString(), cr.isSuccess());
		}
	}

	protected void executeJobCreate(String jobName, String jobDefinition) {
		executeJobCreate(jobName, jobDefinition, true);
	}

	/**
	 * Execute job create for the supplied job name/definition, and verify the command result.
	 */
	protected void executeJobCreate(String jobName, String jobDefinition, boolean deploy) {
		CommandResult cr = executeCommand("job create --definition \"" + jobDefinition + "\" --name " + jobName
				+ (deploy ? "" : " --deploy false"));
		String prefix = (deploy) ? "Successfully created and deployed job '" : "Successfully created job '";
		assertEquals(prefix + jobName + "'", cr.getResult());
		jobs.add(jobName);
	}

	protected void checkForJobInList(String jobName, String jobDescriptor) {
		Table t = listJobs();
		assertTrue(t.getRows().contains(new TableRow().addValue(1, jobName).addValue(2, jobDescriptor)));
	}

	protected void checkForFail(CommandResult cr) {
		assertTrue("Failure.  CommandResult = " + cr.toString(), !cr.isSuccess());
	}

	protected void checkForSuccess(CommandResult cr) {
		assertTrue("Failure.  CommandResult = " + cr.toString(), cr.isSuccess());
	}

	protected void checkErrorMessages(CommandResult cr, String expectedMessage) {
		assertTrue("Failure.  CommandResult = " + cr.toString(),
				cr.getException().getMessage().contains(expectedMessage));
	}

	protected void waitForResult(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	protected void waitForResult() {
		waitForResult(1000);
	}

	private Table listJobs() {
		return (Table) getShell().executeCommand("job list").getResult();
	}

	private void copyTaskletDescriptorsToServer(String inFile, String outFile) {
		File out = new File(outFile);
		File in = new File(inFile);
		try {
			FileCopyUtils.copy(in, out);
		}
		catch (IOException ioe) {
			assertTrue("Unable to deploy Job descriptor to server directory", out.isFile());
		}
		out.deleteOnExit();
	}

	private void removeTmpFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
			file.deleteOnExit();
		}
	}
}
