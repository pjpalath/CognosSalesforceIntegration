/**
 * 
 */
package com.rsc.erp.utils.parse.cognos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author PAUL
 *
 */
public class SplitHtmlReportFileHandlingUtils
{
	// Logger
	private static final Logger LOG = Logger.getLogger(SplitHtmlReportFileHandlingUtils.class.getName());
	
	// Log String Builder
	private StringBuilder logStringBuilder;	

	/**
	 * 
	 */
	public SplitHtmlReportFileHandlingUtils()
	{		
	}

	/**
	 * 
	 * @param logFileHandler
	 */
	public SplitHtmlReportFileHandlingUtils(Handler logFileHandler, StringBuilder logStringBuilder)
	{
		LOG.addHandler(logFileHandler);
		LOG.setUseParentHandlers(false);
		
		this.logStringBuilder = logStringBuilder;
	}	

	/**
	 * Create a new sub folder under the date folder. This is
	 * done every time there is a new report run.
	 * @param folderPath
	 */
	public String createSubFolderForSplitFiles(String splitFilesRootFolder)
	{
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String newSplitReportsFolder = splitFilesRootFolder + "/" + today;

		// Check to see if the folder for this date and for this
		// report has already been created. If so then just move ahead.
		File splitReportsFileFolder = new File(newSplitReportsFolder);
		if (!splitReportsFileFolder.exists())
		{
			splitReportsFileFolder.mkdirs();
		}

		// Use folder 0 to replicate what is in current folder and hold all the files
		// across multiple runs of the script (On report files arriving at diferent times)
		// and use SplitScriptTodaysRun<folderCounter> to hold files from each individual run
		int folderCounter = ((splitReportsFileFolder.listFiles() == null) || (splitReportsFileFolder.listFiles().length == 0))
				? 0 : (splitReportsFileFolder.listFiles().length - 1);				

		// Create the new sub folder
		String newSplitReportsSubFolder = newSplitReportsFolder + "/SplitScriptTodaysRun" + (folderCounter + 1);		
		
		File splitReportsSubFolder = new File(newSplitReportsSubFolder);
		splitReportsSubFolder.mkdirs();

		logStringBuilder.append("Created the new subfolder " + newSplitReportsSubFolder + " to store the split files\r\n\r\n");

		return newSplitReportsSubFolder;
	}

	/**
	 * 
	 * @param counter
	 * @param splitFileString
	 * @throws IOException 
	 */
	public void createFileAndWriteSplitString(String accountString, String newSplitReportsFolder, 
			String splitFileString) throws IOException
			{
		String splitFileName = newSplitReportsFolder + "/" + accountString + ".html";
		File splitFile = new File(splitFileName);
		FileOutputStream fileOutputStream = new FileOutputStream(splitFile);

		// if file doesnt exists, then create it
		if (!splitFile.exists()) 
		{
			splitFile.createNewFile();
		}

		// get the content in bytes
		byte[] contentInBytes = splitFileString.getBytes();

		fileOutputStream.write(contentInBytes);
		fileOutputStream.flush();
		fileOutputStream.close();

			}	

	/**
	 * Delete split file folders older than a
	 * certain date
	 * @throws IOException 
	 */
	public void deletePastHistory(String splitFilesRootFolder, int numOfDaysReportHistory) throws IOException
	{
		File rootFolder = new File(splitFilesRootFolder);
		// Get the list of directories that need to be deleted
		List<File> listOfFoldersToDelete = new ArrayList<File>();

		if (rootFolder.exists())
		{			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");        
			for (File splitFilesDateFolder : rootFolder.listFiles())
			{
				if (splitFilesDateFolder.isDirectory())
				{								
					try 
					{
						Date folderDate = sdf.parse(splitFilesDateFolder.getName());					

						Calendar folderCalendar = Calendar.getInstance();
						Calendar todayCalendar = Calendar.getInstance();

						folderCalendar.setTime(folderDate);
						todayCalendar.setTime(new Date());

						// Get the number of days between this folder date and today
						long daysBetween = 0;  
						while (folderCalendar.before(todayCalendar)) 
						{  
							folderCalendar.add(Calendar.DAY_OF_MONTH, 1);  
							daysBetween++;  
						}  

						// If the number of days in between is greater than a 
						// certain number of days delete it
						if (daysBetween > numOfDaysReportHistory)
						{
							listOfFoldersToDelete.add(splitFilesDateFolder);
						}
					} 
					catch (ParseException e) 
					{					
					}
				}
			}
		}

		for (File folderToDelete : listOfFoldersToDelete)
		{			
			invokeDelete(folderToDelete.getCanonicalPath());
			logStringBuilder.append("Deleted the folder " + folderToDelete.getCanonicalPath() + " because it is "
					+ "older than " + numOfDaysReportHistory + " days.\r\n\r\n");
		}
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean invokeDelete(String fileName) 
	{
		File file = new File(fileName);
		if (file.exists()) 
		{
			//check if the file is a directory
			if (file.isDirectory()) 
			{
				if ((file.list()).length > 0) 
				{
					for(String s:file.list())
					{
						//call deletion of file individually
						invokeDelete(fileName+"\\"+s);
					}
				}
			}

			boolean result = file.delete();
			// test if delete of file is success or not
			if (!result)			
			{
				logStringBuilder.append("File was not deleted, unknown reason " + fileName + "\r\n\r\n");
			}
			return result;
		} 
		else 
		{
			logStringBuilder.append("File delete failed, file does not exists " + fileName + "\r\n\r\n");
			return false;
		}
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean isReportFileAvailabe(String reportFilePath, String reportName) throws Exception
	{
		// Set the report file (A report file could come in many parts with different
		// ending names)
		File multipleReportFilesFolder = new File(reportFilePath);
		if (!multipleReportFilesFolder.isDirectory())
		{
			LOG.log(Level.SEVERE, "The reports folder " + reportFilePath + " defined in the properties file "
					+ "is not a directory. Please correct in the properties file and run script again\r\n\r\n");
			throw new Exception();
		}

		for (File multipleReportFile : multipleReportFilesFolder.listFiles())
		{
			if (multipleReportFile.getName().startsWith(reportName))
			{					
				return true;
			}
		}

		return false;
	}	

	/**
	 * 
	 * @return
	 */
	public String getCurrentSplitFilesFolder(String splitFilesRootFolder)
	{
		return splitFilesRootFolder + "/" + "Current";
	}

	/**
	 * 
	 * @param splitFilersRootFolder
	 * @return
	 */
	public String getTodayZeroSplitFilesFolder(String splitFilesRootFolder)
	{
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return splitFilesRootFolder + "/" + today + "/0";
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
	}

}
