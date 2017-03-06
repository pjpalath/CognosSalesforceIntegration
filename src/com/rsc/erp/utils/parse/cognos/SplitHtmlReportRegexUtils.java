/**
 * 
 */
package com.rsc.erp.utils.parse.cognos;

import java.util.Scanner;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author PAUL
 *
 */
public class SplitHtmlReportRegexUtils
{
	// Logger
	private static final Logger LOG = Logger.getLogger(SplitHtmlReportRegexUtils.class.getName());	

	// Log String Builder
	private StringBuilder logStringBuilder;	
	
	/**
	 * 
	 */
	public SplitHtmlReportRegexUtils()
	{
	}
	
	/**
	 * 
	 * @param logFileHandler
	 */
	public SplitHtmlReportRegexUtils(Handler logFileHandler, StringBuilder logStringBuilder)
	{
		LOG.addHandler(logFileHandler);
		LOG.setUseParentHandlers(false);
		
		this.logStringBuilder = logStringBuilder;
	}

	/**
	 * Close any open handles to prevent memory leaks
	 */
	public void closeFileScannerHandler(Scanner reportFileScanner)
	{
		logStringBuilder.append("Closed the report file scanner handler.\r\n\r\n");
		reportFileScanner.close();
	}

	/**
	 * Get the footer
	 * @param lastLine
	 * @return
	 */
	public String getFooter(String lastLine, String regexForFooter)
	{
		Scanner footerScanner = new Scanner(lastLine);
		footerScanner.useDelimiter(regexForFooter);
		String footer = "";
		while (footerScanner.hasNext())
		{
			footer = footerScanner.next();
		}
		footerScanner.close();

		logStringBuilder.append("Footer regular expression string used to extract footer is " + footer +"\r\n\r\n");

		return footer;
	}	

	/**
	 * Find the account number from the split file string.
	 * The account number will be used as the split file name.
	 * 
	 * @param splitFileString
	 * @return
	 */
	public String findAccountFromSplitFileString(String splitFileString, String regexForAccountNumber,
			String regexForSplittingAccountString)
	{
		String[] strarray = splitFileString.split(regexForAccountNumber);
		String[] clean = strarray[1].split(regexForSplittingAccountString);
		String account = clean[0];

		Pattern htmlAmperPattern = Pattern.compile("&amp");
		Matcher matcher = htmlAmperPattern.matcher(account);
		while (matcher.find()){
			account = account.replaceAll("&amp;", "&");
		}

		Pattern forwardslashPattern = Pattern.compile("/");
		Matcher matcher1 = forwardslashPattern.matcher(account);
		while(matcher1.find()){
			account = account.replaceAll("/", "");
		}

		// The Java compiler sees the string "\\\\" in the source and actually turns that into 
		// "\\" since it uses \ as an escape character. The the regular expression sees that 
		// "\\" and because it also uses \ as an escape character, will treat it as a single \ character.
		Pattern backlashPattern = Pattern.compile("\\\\");
		Matcher matcher2 = backlashPattern.matcher(account);
		while(matcher2.find()){
			account = account.replaceAll("\\\\", "");
		}

		String[] accountClean = account.split(" - ");
		String accountStringClean = accountClean[0];

		//		LOG.info("Account number from split file: " + accountStringClean);

		return accountStringClean;
	}	

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
	}

}
