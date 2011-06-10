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

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Builder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

class CreatePropertyFileBuilder extends Builder {

	private static final Logger LOGGER = Logger
			.getLogger(CreatePropertyFileBuilder.class.getName());

	private String basePath;
	private String fileName;
	private String fileContents;

	public CreatePropertyFileBuilder(final String fileName,
			final String fileContents) {
		super();
		this.fileName = fileName;
		this.fileContents = fileContents;
	}

	public CreatePropertyFileBuilder(final String basePath,
			final String fileName,
			final String fileContents) {
		super();
		this.basePath = basePath;
		this.fileName = fileName;
		this.fileContents = fileContents;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build,
			final Launcher launcher, final BuildListener listener)
			throws InterruptedException, IOException {

		FilePath propertyFileBase = null;

		if (this.basePath == null) {
			propertyFileBase = build.getWorkspace();
		}
		else {
			propertyFileBase = new FilePath(new File(this.basePath));
		}

		CreatePropertyFileBuilder.LOGGER.info(String.format(
				"Creating property file [%s] in location [%s]",
				this.fileName, propertyFileBase.absolutize()));

		FilePath created = propertyFileBase.child(this.fileName);
		created.write(this.fileContents, null);

		CreatePropertyFileBuilder.LOGGER.info(String.format(
				"Property file [%s] created.", created
						.absolutize()));

		return created.exists();
	}
}
