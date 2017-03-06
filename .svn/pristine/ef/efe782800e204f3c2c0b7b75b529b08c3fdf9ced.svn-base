/**
 * 
 */
package com.rsc.erp.utils.parse.cognos;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author PAUL
 *
 */
public class SplitHtmlReportExceptionHandlingUtils
{
	// Logger
	private static final Logger LOG = Logger.getLogger(SplitHtmlReportRegexUtils.class.getName());	

	/**
	 * 
	 */
	public SplitHtmlReportExceptionHandlingUtils()
	{
	}

	/**
	 * 
	 * @param logFileHandler
	 */
	public SplitHtmlReportExceptionHandlingUtils(Handler logFileHandler)
	{
		LOG.addHandler(logFileHandler);
		LOG.setUseParentHandlers(false);		
	}	
	
	/**
	 * 
	 * @param reportLatestUpdatedProps
	 * @param fileTimeStamp
	 * @param out
	 * @throws Exception
	 */
	public void exceptionHandlingForUpdatedProps(Properties reportLatestUpdatedProps, String propertyName, String propertyValue,
			String dateReportLastUpdatedConfigFile)
			throws Exception
			{
		try 
		{
			reportLatestUpdatedProps.setProperty(propertyName, propertyValue);
			OutputStream out = new FileOutputStream(dateReportLastUpdatedConfigFile);
			reportLatestUpdatedProps.store(out, "");
		} 
		catch (FileNotFoundException e1) 
		{	
			LOG.log(Level.SEVERE, 
					"The properties file " + dateReportLastUpdatedConfigFile + " could not be created");
			throw e1;
		}
		catch (IOException e1) 
		{
			LOG.log(Level.SEVERE, 
					"The properties file " + dateReportLastUpdatedConfigFile + " could not be written to");
			throw e1;
		}
			}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
	}

}
