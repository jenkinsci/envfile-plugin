/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Alan Harder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.envfile;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.Cause.UserCause;
import hudson.model.FreeStyleProject;
import junit.framework.Assert;

import org.jvnet.hudson.test.HudsonTestCase;

public class EnvFileBuilder_performTests extends HudsonTestCase {

	private static final String SAMPLE_ABSOLUTE_PROPFILE_BASEPATH = "target";
	private static final String SAMPLE_PROPFILE_NAME = "sample.properties";
	private static final String SAMPLE_PROPERTY_NAME = "exampleProperty";
	private static final String SAMPLE_PROPERTY_VALUE = "exampleValue";
	private static final String SAMPLE_ABSOLUTE_PROPFILE = String.format(
			"%s/%s",
			EnvFileBuilder_performTests.SAMPLE_ABSOLUTE_PROPFILE_BASEPATH,
			EnvFileBuilder_performTests.SAMPLE_PROPFILE_NAME);
	private static final String SAMPLE_PROPERTY = String.format("%s=%s",
			EnvFileBuilder_performTests.SAMPLE_PROPERTY_NAME,
			EnvFileBuilder_performTests.SAMPLE_PROPERTY_VALUE);

	public void testThatItCreatesEnvironmentVariablesForAllPropertiesInSpecifiedFile()
			throws Exception {
		FreeStyleProject project = super.createFreeStyleProject();

		//Add builder to create sample property
		project.getBuildersList().add(
				new CreatePropertyFileBuilder(
						EnvFileBuilder_performTests.SAMPLE_PROPFILE_NAME,
						EnvFileBuilder_performTests.SAMPLE_PROPERTY));

		//Add builder under test
		project.getBuildersList().add(
				new EnvFileBuilder(
						EnvFileBuilder_performTests.SAMPLE_PROPFILE_NAME,
						true));

		//Add builder to get environment while build is executed
		EnvironmentReadingBuilder environmentReadingBuilder = new EnvironmentReadingBuilder();
		project.getBuildersList().add(environmentReadingBuilder);

		//Run build
		FreeStyleBuild build = project.scheduleBuild2(0, new UserCause()).get();

		//Verify
		this.assertBuildStatusSuccess(build);

		this.assertThatPropertyIsInEnvironment(environmentReadingBuilder
				.getEnvVarsDuringExecution(),
				EnvFileBuilder_performTests.SAMPLE_PROPERTY_NAME,
				EnvFileBuilder_performTests.SAMPLE_PROPERTY_VALUE);
	}

	public void testThatItSupportsAbsolutePaths()
			throws Exception {
		FreeStyleProject project = super.createFreeStyleProject();

		//Add builder to create sample property
		project.getBuildersList()
				.add(
						new CreatePropertyFileBuilder(
								EnvFileBuilder_performTests.SAMPLE_ABSOLUTE_PROPFILE_BASEPATH,
								EnvFileBuilder_performTests.SAMPLE_PROPFILE_NAME,
								EnvFileBuilder_performTests.SAMPLE_PROPERTY));

		//Add builder under test
		project.getBuildersList().add(
				new EnvFileBuilder(
						EnvFileBuilder_performTests.SAMPLE_ABSOLUTE_PROPFILE,
						false));

		//Add builder to get environment while build is executed
		EnvironmentReadingBuilder environmentReadingBuilder = new EnvironmentReadingBuilder();
		project.getBuildersList().add(environmentReadingBuilder);

		//Run build
		FreeStyleBuild build = project.scheduleBuild2(0, new UserCause()).get();

		//Verify
		this.assertBuildStatusSuccess(build);

		this.assertThatPropertyIsInEnvironment(environmentReadingBuilder
						.getEnvVarsDuringExecution(),
				EnvFileBuilder_performTests.SAMPLE_PROPERTY_NAME,
				EnvFileBuilder_performTests.SAMPLE_PROPERTY_VALUE);
	}

	private void assertThatPropertyIsInEnvironment(final EnvVars environment,
			final String expectedPropertyName,
			final String expectedPropertyValue) {
		Assert.assertTrue("Property key was not found: ",
				environment.containsKey(expectedPropertyName));
		Assert.assertEquals("Property value was not correct: ",
				expectedPropertyValue, environment.get(expectedPropertyName));
	}
}
