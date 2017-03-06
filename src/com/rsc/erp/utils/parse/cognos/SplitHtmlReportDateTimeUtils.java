/**
 * 
 */
package com.rsc.erp.utils.parse.cognos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * @author PAUL
 *
 */
public class SplitHtmlReportDateTimeUtils
{
	// Logger
	private static final Logger LOG = Logger.getLogger(SplitHtmlReportDateTimeUtils.class.getName());

	// Required classes
	private SplitHtmlReportExceptionHandlingUtils splitHtmlReportExceptionHandlingUtils;

	// Log String Builder
	private StringBuilder logStringBuilder;

	/**
	 * 
	 */
	public SplitHtmlReportDateTimeUtils()
	{
	}

	/**
	 * 
	 * @param logFileHandler
	 */
	public SplitHtmlReportDateTimeUtils(Handler logFileHandler, StringBuilder logStringBuilder)
	{
		splitHtmlReportExceptionHandlingUtils = new SplitHtmlReportExceptionHandlingUtils(logFileHandler);		

		LOG.addHandler(logFileHandler);
		LOG.setUseParentHandlers(false);

		this.logStringBuilder = logStringBuilder;
	}	

	/**
	 * Check if this run is the first run of the day
	 * @return
	 */
	public boolean isFirstRunForTheDay(String lastRunDate)
	{
		boolean isitFirstRun = false;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		logStringBuilder.append("Last run date for this script is " + lastRunDate + "\r\n\r\n");
		if (lastRunDate == null)
		{
			return true;
		}
		else
		{
			try 
			{
				Date lastRun = sdf.parse(lastRunDate);					

				Calendar lastRunCalendar = Calendar.getInstance();
				Calendar todayCalendar = Calendar.getInstance();

				lastRunCalendar.setTime(lastRun);
				todayCalendar.setTime(new Date());

				// Get the number of days between this folder date and today
				long daysBetween = 0;  
				while (lastRunCalendar.before(todayCalendar)) 
				{  
					lastRunCalendar.add(Calendar.DAY_OF_MONTH, 1);  
					daysBetween++;  
				}  

				// If the number of days in between is greater than 0 (Next day or later)
				logStringBuilder.append("Days in between today and last run date for this script is " + daysBetween + "\r\n\r\n");
				if (daysBetween > 1)
				{
					return true;
				}
				else
				{
					return false;
				}
			} 
			catch (ParseException e) 
			{					
			}
		}

		return isitFirstRun;
	}

	/**
	 * Check to see if the report file is newer
	 * 
	 * @param reportFile
	 * @param reportName
	 * @param dateReportLastUpdatedConfigFile
	 * @param reportLatestUpdatedProps
	 * @param lastRunDateForScript
	 * @return
	 */
	public boolean isReportFileNewer(String reportFile, String reportName, String dateReportLastUpdatedConfigFile,
			Properties reportLatestUpdatedProps)
	{					
		// Get current timestame from file
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		File report = new File(reportFile);
		String fileTimeStamp = sdf.format(report.lastModified());
		Date lastUpdatedTimeStamp;
		try 
		{
			lastUpdatedTimeStamp = sdf.parse(fileTimeStamp);
		} 
		catch (ParseException e) 
		{
			logStringBuilder.append("Could not parse the last modified timestamp " + fileTimeStamp + " for report "
					+ reportName + "\r\n\r\n");
			return false;
		}

		try 
		{
			reportLatestUpdatedProps.load(new FileInputStream(dateReportLastUpdatedConfigFile));
		} 
		catch (FileNotFoundException e) 
		{		
			logStringBuilder.append("The properties file " + dateReportLastUpdatedConfigFile
					+ " that has the date the report was last used is not present\r\n\r\n");			
			try 
			{
				splitHtmlReportExceptionHandlingUtils.exceptionHandlingForUpdatedProps(reportLatestUpdatedProps, reportName, 
						fileTimeStamp, dateReportLastUpdatedConfigFile);
			} 
			catch (Exception e1) 
			{
				return false;
			}

			// Since the properties file was not there and it has been created now we
			// can treat the report as a newer file and process it
			return true;
		} 
		catch (IOException e) 
		{
			logStringBuilder.append("Error reading the " + reportLatestUpdatedProps + " properties file\r\n\r\n");
			return false;
		}

		// Last update from properties file
		String lastUpdated = reportLatestUpdatedProps.getProperty(reportName);		
		logStringBuilder.append(reportName + " was last parsed on (Null for first time) : " + lastUpdated + " and the report"
				+ " was last updated in the Cognos Reports directory at : "
				+ fileTimeStamp + "\r\n\r\n");

		// If this is the first time there will be no value in the properties file
		// and hence the report file should be newer. Check to make sure the report
		// file exists too.		
		if (lastUpdated == null)
		{
			if (report.exists())
			{
				try 
				{
					splitHtmlReportExceptionHandlingUtils.exceptionHandlingForUpdatedProps(reportLatestUpdatedProps, reportName,
							fileTimeStamp, dateReportLastUpdatedConfigFile);
				} 
				catch (Exception e) 
				{
					return false;
				}
				return true;
			}
			else
			{
				return false;
			}
		}
		// If there is value for the last updated property then check
		// its value and if it is older than the current timestamp
		// of the file then process the report file.
		else
		{
			Date lastUpdatedDate;
			try 
			{
				lastUpdatedDate = sdf.parse(lastUpdated);
			} 
			catch (ParseException e) 
			{
				logStringBuilder.append("Could not parse the last updated property value " + lastUpdated
						+ " for report " + reportName + "\r\n\r\n");
				return false;
			}			

			if (lastUpdatedTimeStamp.after(lastUpdatedDate))
			{
				try 
				{
					splitHtmlReportExceptionHandlingUtils.exceptionHandlingForUpdatedProps(reportLatestUpdatedProps, 
							reportName, fileTimeStamp, dateReportLastUpdatedConfigFile);
				} 
				catch (Exception e) 
				{
					return false;
				}				
				return true;
			}
			else
				return false;
		}
	}	

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
	}

}
