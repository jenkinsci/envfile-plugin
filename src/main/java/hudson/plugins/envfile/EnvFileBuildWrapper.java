package hudson.plugins.envfile;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.xalan.xsltc.compiler.sym;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * BuildWrapper to set environment variables from a file.
 * 
 * @author Anders Johansson
 *
 */
public class EnvFileBuildWrapper extends BuildWrapper{

	private static final Logger logger = Logger.getLogger(EnvFileBuildWrapper.class.getName());
	private static final String NAME = "[envfile] ";
	private String filePath;
	private BuildListener buildListner;
	private AbstractBuild build;
	
	@DataBoundConstructor
	public EnvFileBuildWrapper(String filePath) {
		this.filePath=filePath;
	}

	/**
	 * Get the path to the file containing environment variables.
	 * 
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Get the path to the file containing environment variables.
	 * 
	 * @return the filePath
	 */
	public void setFilePath(String filePath) 
	{
		this.filePath=filePath;
	}
	
	private Map<String, String> getEnvFileMap(Map<String, String> currentMap)
	{
		Map<String, String> fileEnvMap = new HashMap<String, String>();
		Properties envProps = readPropsFromFile(filePath, currentMap);
		for (Entry<Object, Object> prop : envProps.entrySet()) 
		{
			fileEnvMap.put(prop.getKey().toString(), prop.getValue().toString());
		}
		return fileEnvMap;
	}
	
	private Properties readPropsFromFile(String path, Map<String, String> currentMap)
	{
		console("Reading environment variables from file.");
		Properties props = new Properties();
		FileInputStream fis = null;
		try 
		{
			if(path != null && path!="")
			{
				
				String resolvedPath = Util.replaceMacro(path, currentMap);
				console("Path to file: " + resolvedPath);
				fis = new FileInputStream(resolvedPath);
				props.load(fis);
			}
			else
			{
				console("No path to environment file has been entered.");
			}
		}
		catch (FileNotFoundException e) 
		{
			console("Environment file not found. Path to file=[" + path + "]");
			logger.warning("Environment file not found. Path to file=[" + path + "]");
		} 
		catch (IOException e) 
		{
			console("Unable to read from environment file.");
			logger.warning("Unable to read from environment file. " + e.getClass().getName());
		}
		finally
		{
			if(fis!=null)
			{
				try 
				{
					fis.close();
				} catch (Exception e2) 
				{
					console("Unable to close environment file.");
					logger.warning("Unable to close environment file.");
				}
				
			}
		}
		return props;
	}
	
	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		
		logger.info("Reading environment variables from file. ");
		
		this.buildListner = listener;
		this.build = build;
		return new EnvironmentImpl();
	}
	
	private void console(String str)
	{
		buildListner.getLogger().println(NAME + str);
	}
	
	class EnvironmentImpl extends Environment
	{
		@Override
		public void buildEnvVars(Map<String, String> env) 
		{
			
			Map<String, String> envFileMap = getEnvFileMap(env); 
			
			//Temporary map for system and file environment variables 
			Map<String, String> tmpFileEnvMap = new HashMap<String, String>();
			
			//Add system environment variables.
			for (Entry<String, String> systemVar : System.getenv().entrySet()) 
			{
				tmpFileEnvMap.put(systemVar.getKey(), systemVar.getValue());
			}
			
			//Add file environment variables.
			tmpFileEnvMap.putAll(envFileMap);
			
			//Resolve variables.
			EnvVars.resolve(tmpFileEnvMap);
			
			if(tmpFileEnvMap != null)
			{
				console("Environment variables from file:");
				for (Entry<String, String> varEntry : envFileMap.entrySet()) 
				{
					String var = varEntry.getKey();
					String varValue = tmpFileEnvMap.get(var);
					env.put(var, varValue);
					console(var+"="+varValue);
				}
			}
		}
	}
	
	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor
	{
		@Override
		public String getDisplayName() {
			return Messages.EnvFileBuildWrapper_DisplayName();
		}
		
		@Override
		public boolean isApplicable(AbstractProject item) {
			return true;
		}
	}
	
}
