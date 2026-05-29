package com.orchestranetworks.ps.service;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.collections4.*;
import org.apache.poi.ss.usermodel.*;

import com.onwbp.org.apache.commons.io.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 * Export data set to an excel workbook. Give details of tables and fields and
 * recurse into reachable data sets.
 */
public abstract class AbstractFileDownloadUserService<S extends DataspaceEntitySelection>
	extends
	AbstractUserService<S>
{
	private boolean complete;
	public abstract List<File> getFiles();

	public static File saveWorkbook(Workbook workbook, File file)
	{
		OutputStream os = null;
		try
		{
			os = new FileOutputStream(file);
			workbook.write(os);
			os.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(os);
		}
		return file;
	}

	public static void formatSheet(Sheet sheet, int numCol, int freezeCol)
	{
		try
		{
			for (int i = 0; i < numCol; i++)
			{
				sheet.autoSizeColumn(i);
			}
			if (freezeCol >= 0)
				sheet.createFreezePane(freezeCol, 1);
		}
		catch (Exception e)
		{
			//this is prettiness, don't want an exception to break the entire thing
		}
	}

	@Override
	public void setupDisplay(
		UserServiceSetupDisplayContext<S> aContext,
		UserServiceDisplayConfigurator aConfigurator)
	{
		if (!complete && submitted)
			aConfigurator.setLeftButtons(aConfigurator.newCloseButton());
		super.setupDisplay(aContext, aConfigurator);
	}

	@Override
	public void landService()
	{
		List<File> files = getFiles();
		if (shouldWriteDownloadLinks())
		{
			for (File file : files)
			{
				writeDownloadLink(file);
			}
			complete = true;
		}
		else
		{
			super.landService();
		}
	}

	protected boolean shouldWriteDownloadLinks()
	{
		List<File> files = getFiles();
		return !CollectionUtils.isEmpty(files) && !complete;
	}

	@Override
	protected void writeResultPane(
		UserServicePaneContext aPaneContext,
		UserServicePaneWriter aWriter)
	{
		if (!complete)
			super.writeResultPane(aPaneContext, aWriter);
		else
			landService();
	}

	public static File zipFiles(List<File> files, String zipFilePrefix) throws IOException
	{
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try
		{
			File z = File.createTempFile(zipFilePrefix, ".zip");
			z.deleteOnExit();
			fos = new FileOutputStream(z);
			zos = new ZipOutputStream(fos);
			addZipFiles(zos, null, files.toArray(new File[files.size()]));
			return z;
		}
		finally
		{
			IOUtils.closeQuietly(zos);
			IOUtils.closeQuietly(fos);
		}
	}

	private static void addZipFiles(ZipOutputStream zos, String parentFolderPrefix, File[] files)
		throws IOException
	{
		for (File file : files)
		{
			StringBuilder bldr = new StringBuilder();
			if (parentFolderPrefix != null)
			{
				bldr.append(parentFolderPrefix);
			}
			bldr.append(file.getName());
			if (file.isDirectory())
			{
				bldr.append(File.separator);
			}
			String filename = bldr.toString();
			ZipEntry zipEntry = new ZipEntry(filename);
			zos.putNextEntry(zipEntry);
			if (file.isDirectory())
			{
				zos.closeEntry();
				addZipFiles(zos, filename, file.listFiles());
			}
			else
			{
				FileInputStream fis = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0)
				{
					zos.write(bytes, 0, length);
				}
				zos.closeEntry();
				fis.close();
			}
		}
	}
}
