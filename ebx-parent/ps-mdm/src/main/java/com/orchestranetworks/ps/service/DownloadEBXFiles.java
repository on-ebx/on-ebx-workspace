package com.orchestranetworks.ps.service;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Map.*;

import com.onwbp.org.apache.commons.io.comparator.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

public class DownloadEBXFiles extends AbstractFileDownloadUserService<DataspaceEntitySelection>
{
	private final String ebxPropertyName;
	private final String extraPath;
	private final String resultFilePrefix;
	private Map<Integer, String> filesNames = new HashMap<>();
	private WizardStep step = new WizardStepMain(this);
	private File zipFile;

	public DownloadEBXFiles(String ebxPropertyName, String extraPath, String resultFilePrefix) {
		super();
		this.ebxPropertyName = ebxPropertyName;
		this.extraPath = extraPath;
		this.resultFilePrefix = resultFilePrefix;
	}

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<DataspaceEntitySelection> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		if (aContext.isInitialDisplay())
		{
			File[] fileArray = getEbxDir().listFiles();

			Arrays.sort(fileArray, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

			List<File> files = Arrays.asList(fileArray);

			Integer index = 0;
			for (File file : files)
			{
				filesNames.put(index, file.getName());
				index++;
			}
		}
	}

	@Override
	public void setupDisplay(
		UserServiceSetupDisplayContext<DataspaceEntitySelection> aContext,
		UserServiceDisplayConfigurator aConfigurator)
	{
		this.step.setupDisplay(aContext, aConfigurator);
	}

	@Override
	protected void writeInputPane(
		UserServicePaneContext aPaneContext,
		UserServicePaneWriter aWriter)
	{
		step.writeInputPane(aPaneContext, aWriter);
	}

	@Override
	public UserServiceEventOutcome processEventOutcome(
		UserServiceProcessEventOutcomeContext<DataspaceEntitySelection> aContext,
		UserServiceEventOutcome anEventOutcome)
	{
		if (anEventOutcome instanceof CustomOutcome)
		{
			CustomOutcome action = (CustomOutcome) anEventOutcome;
			switch (action)
			{
			case displayMainStep:
				this.step = new WizardStepMain(this);
				break;
			case displayEndStep:
				this.step = new WizardStepEnd(this);
				break;

			}
			return null;
		}

		return anEventOutcome;
	}

	@Override
	public List<File> getFiles()
	{
		if (zipFile == null)
			return Collections.emptyList();
		return Collections.singletonList(zipFile);
	}

	private File getEbxDir()
	{
		String ebxLogDirString = null;
		try
		{
			ebxLogDirString = CommonConstants.getEBXProperties().getProperty(ebxPropertyName);
			if (ebxLogDirString.contains("${ebx.home}"))
			{
				String ebxHome = CommonConstants.getEBXHome();
				ebxLogDirString = ebxLogDirString.replace("${ebx.home}", ebxHome);
			}
			File result = new File(ebxLogDirString);
			if (extraPath != null)
				return new File(result, extraPath);
			return result;
		}
		catch (IOException e)
		{
			LoggingCategory.getKernel().error("Failed to find ebx directory for ebx.property "+ebxPropertyName);
		}
		return null;
	}

	public enum CustomOutcome implements UserServiceEventOutcome {
		displayMainStep, displayEndStep
	}

	public interface WizardStep
	{
		public void setupDisplay(
			UserServiceSetupDisplayContext<DataspaceEntitySelection> aContext,
			UserServiceDisplayConfigurator aConfigurator);

		void writeInputPane(UserServicePaneContext aPaneContext, UserServicePaneWriter aWriter);
	}

	public class WizardStepMain implements WizardStep
	{

		private DownloadEBXFiles parent;

		private WizardStepMain(DownloadEBXFiles parent)
		{
			this.parent = parent;
		}

		@Override
		public void setupDisplay(
			UserServiceSetupDisplayContext<DataspaceEntitySelection> aContext,
			UserServiceDisplayConfigurator aConfigurator)
		{
			aConfigurator.setContent(this::writeInputPane);
		}

		@Override
		public void writeInputPane(
			UserServicePaneContext aPaneContext,
			UserServicePaneWriter aWriter)
		{
			aWriter.addJS_cr();
			aWriter.addJS("function toggle(source) {");
			aWriter.addJS_cr();
			aWriter.addJS("  checkboxes = document.getElementsByName('selectedFile');");
			aWriter.addJS_cr();
			aWriter.addJS("  for(var i=0; i< checkboxes.length; i++) {");
			aWriter.addJS_cr();
			aWriter.addJS("    checkboxes[i].checked = source.checked;");
			aWriter.addJS_cr();
			aWriter.addJS("	 }");
			aWriter.addJS_cr();

			aWriter.addJS("}");
			aWriter.addJS_cr();

			aWriter.add("<form id=\"fileNamesForm\">");
			aWriter.add("<tr><td bgcolor=\"#DCDCDC\">");
			aWriter.addButton(
				aWriter.newSubmitButton("Download selected files", this::processSelectedFiles));
			aWriter.add("</td><td bgcolor=\"#DCDCDC\">");
			aWriter.addButton(aWriter.newSubmitButton("Download all files", this::processAllFiles));
			aWriter.add("</td></tr>");

			aWriter.add("<table id=\"fileNames\" border=\"0\">");

			aWriter.add("<thead>");

			aWriter.add("<tr>");
			aWriter.add("<th>");

			aWriter.add(" Toggle All <input type=\"checkbox\" onClick=\"toggle(this)\" />");

			aWriter.add("</th>");

			aWriter.add("<th>");

			aWriter.add("File Name");

			aWriter.add("</th>");

			aWriter.add("</tr>");
			aWriter.add("</thead>");

			aWriter.add("<tbody>");

			for (Entry<Integer, String> fileEntry : filesNames.entrySet())
			{
				aWriter.add("<tr>");

				aWriter.add(
					MessageFormat.format(
						"<td><input name=\"selectedFile\" type=\"checkbox\" value=\"{0}\"</input></td>",
						fileEntry.getKey()));
				aWriter.add(MessageFormat.format("<td>{0}</td>", fileEntry.getValue()));

				aWriter.add("</tr>");
			}

			aWriter.add("</tbody>");
			aWriter.add("</table>");
			aWriter.add("<table id=\"buttons\" border=\"0\">");
			aWriter.add("<tbody>");
			aWriter.add("</tbody>");
			aWriter.add("</table>");
			aWriter.add("</Form>");

		}

		protected UserServiceEventOutcome processSelectedFiles(
			UserServiceEventContext anEventContext)
		{
			return processFiles(anEventContext, true);
		}

		protected UserServiceEventOutcome processAllFiles(UserServiceEventContext anEventContext)
		{
			return processFiles(anEventContext, false);
		}
		
		protected UserServiceEventOutcome processFiles(UserServiceEventContext anEventContext, boolean selected)
		{
			File folder = getEbxDir();
			if (folder != null)
			{
				try
				{
					List<File> files = getFiles(anEventContext, folder, selected);
					parent.zipFile = zipFiles(files, resultFilePrefix);
				}
				catch (IOException e)
				{
					LoggingCategory.getKernel().error("Error creating result zip file");
				}
			}
			return CustomOutcome.displayEndStep;
		}
		
		private List<File> getFiles(UserServiceEventContext anEventContext, File folder, boolean selected) {
			List<File> files = new ArrayList<>();
			if (selected) {
				String[] values = anEventContext.getParameterValues("selectedFile");
				for (String fileIndex : values)
				{
					File selectedFile = new File(
						folder,
						filesNames.get(Integer.valueOf(fileIndex)));
					files.add(selectedFile);
				}
			} else {
				for (String fileName : filesNames.values())
				{
					File selectedFile = new File(folder, fileName);
					files.add(selectedFile);
				}
			}
			return files;
		}
	}
	

	public class WizardStepEnd implements WizardStep
	{
		private DownloadEBXFiles parent;

		private WizardStepEnd(DownloadEBXFiles parent)
		{
			this.parent = parent;
		}

		@Override
		public void setupDisplay(
			UserServiceSetupDisplayContext<DataspaceEntitySelection> aContext,
			UserServiceDisplayConfigurator aConfigurator)
		{
			aConfigurator.setContent(this::writeInputPane);
		}

		@Override
		public void writeInputPane(
			UserServicePaneContext aPaneContext,
			UserServicePaneWriter aWriter)
		{
			aWriter.add("<div ");
			aWriter.addSafeAttribute("class", UICSSClasses.CONTAINER_WITH_TEXT_PADDING);
			aWriter.add(">");

			String downloadURL = getDownloadURL(aWriter, parent.zipFile);
			aWriter.add("<a ");
			aWriter.addSafeAttribute("href", downloadURL);
			aWriter.add(
				">Click here to download Selected files included in this: "
					+ parent.zipFile.getName() + "</a>");

			aWriter.add("</div>");

		}

	}

	@Override
	public void execute(Session session) throws OperationException
	{
		// all handled in step implementations

	}

}
