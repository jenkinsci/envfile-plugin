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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.tasks.Builder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Builder to set environment variables from a file.
 * 
 * @author Leo Hart
 */
public class EnvFileBuilder extends Builder {

	private static final Logger LOGGER = Logger
			.getLogger(EnvFileBuildWrapper.class.getName());

	private String filePath;
	private Boolean relativeToWorkspace;

	@Extension
	public static final EnvFileBuilderDescriptor DESCRIPTOR = new EnvFileBuilderDescriptor();

	public EnvFileBuilder(final String filePath,
			final boolean relativeToWorkspace) {
		super();
		this.filePath = Util.fixEmpty(filePath);
		this.relativeToWorkspace = relativeToWorkspace;
	}

	@Override
	public Descriptor<Builder> getDescriptor() {
		return EnvFileBuilder.DESCRIPTOR;
	}

	public String getFilePath() {
		return this.filePath;
	}

	public Boolean getRelativeToWorkspace() {
		return this.relativeToWorkspace;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build,
			final Launcher launcher,
			final BuildListener listener) throws InterruptedException,
			IOException {

		FilePath completePath = null;

		if (this.relativeToWorkspace) {
			completePath = new FilePath(build.getWorkspace(),
					this.filePath);
		}
		else {
			completePath = new FilePath(
					new File(this.filePath));
		}

		listener.getLogger()
				.println(
						Messages.EnvFileBuilder_LoadingPropertiesFromPath(completePath));

		Properties properties = new Properties();
		properties.load(completePath.read());

		for (Map.Entry<Object, Object> property : properties.entrySet()) {
			EnvFileBuilder.LOGGER.fine(String.format("Adding property [%s].",
					property.getKey()));

			listener.getLogger().println(
					Messages.EnvFileBuilder_AddingProperty(property.getKey()));

			build.addAction(new ParametersAction(
									new StringParameterValue(property.getKey()
											.toString(), property.getValue()
											.toString())
									));
		}

		EnvFileBuilder.LOGGER.info(String.format(
				"Properties are now [%s].",
				build.getEnvironment(listener).keySet().toString()));

		return true;
	}
}
