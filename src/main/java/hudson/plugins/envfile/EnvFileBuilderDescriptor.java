/* ************************************************************
 * Copyright 2011 Fidelity Investments. All rights reserved.
 * Information Security: Highly Confidential
 * Author: a122695 
 *************************************************************/
package hudson.plugins.envfile;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

public class EnvFileBuilderDescriptor extends BuildStepDescriptor<Builder> {

	EnvFileBuilderDescriptor() {
		super(EnvFileBuilder.class);
		this.load();
	}

	@Override
	public String getDisplayName() {
		return "Load Properties From File";
	}

	@Override
	public String getHelpFile() {
		return "/plugin/envfile/project-config.html";
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean isApplicable(
			final Class<? extends AbstractProject> jobType) {
		return true;
	}

	@Override
	public Builder newInstance(final StaplerRequest req, final JSONObject data)
			throws FormException {
		String filePath = data.getString("filePath");
		Boolean relativeToWorkspace = data.getBoolean("relativeToWorkspace");

		return new EnvFileBuilder(filePath, relativeToWorkspace);
	}

}
