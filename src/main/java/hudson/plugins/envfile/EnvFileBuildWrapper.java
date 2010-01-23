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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
	
	@DataBoundConstructor
	public EnvFileBuildWrapper(String filePath) {
		this.filePath=filePath;
	}

	
	/**
	 * Get the path to the file containing environment variables.
	 * 
	 * @return the filePath
	 */
	public String getFilePath() 
	{
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
		Map<String, String> tmpFileEnvMap = new HashMap<String, String>();
		Map<String, String> newFileEnvMap = new HashMap<String, String>();
		
		tmpFileEnvMap.putAll(currentMap);
		
		//Fetch env variables from fil as properties
		Properties envProps = readPropsFromFile(filePath, currentMap);
		
		if(envProps != null || envProps.size() < 1)
		{
		
			//Add env variables to temporary env map and file map containing new variables.
			for (Entry<Object, Object> prop : envProps.entrySet()) 
			{
				String key = prop.getKey().toString();
				String value = prop.getValue().toString();
				newFileEnvMap.put(key, value);
				tmpFileEnvMap.put(key, value);
			}

			// Resolve all variables against each other.
			EnvVars.resolve(tmpFileEnvMap);
			
			//Print resolved variables and copy resolved value to return map.
			for(String key : newFileEnvMap.keySet())
			{
				newFileEnvMap.put(key, tmpFileEnvMap.get(key));
				console(key + "=" + newFileEnvMap.get(key));
			}
			
		}
		return newFileEnvMap;
	}
	
	private Properties readPropsFromFile(String path, Map<String, String> currentMap)
	{
		console("Reading environment variables from file.");
		
		Properties props = new Properties();
		FileInputStream fis = null;
		String resolvedPath = Util.replaceMacro(path, currentMap);
		console("Path to file: " + resolvedPath);
		
		try 
		{
			if(path != null && path!="")
			{
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
			console("Can not find environment file. Path to file=[" + resolvedPath + "]");
			logger.warning("Environment file not found. Path to file=[" + resolvedPath + "]");
		} 
		catch (IOException e) 
		{
			console("Unable to read from environment file.");
			logger.warning("Unable to read from environment file. " + e.getClass().getName());
		}
		finally
		{
			close(fis);
		}
		
		return props;
	}
	
	/**
	 * Helper to close environment file.
	 * @param fis {@link FileInputStream} for environment file.
	 */
	private void close(FileInputStream fis)
	{
		try 
		{
			if(fis != null)
			{
				fis.close();
			}
		} 
		catch (Exception e) 
		{
			console("Unable to close environment file.");
			logger.warning("Unable to close environment file.");
		}
	}
	
	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		
		logger.fine("Reading environment variables from file. ");
		
		this.buildListner = listener;
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
			env.putAll(getEnvFileMap(env));
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
