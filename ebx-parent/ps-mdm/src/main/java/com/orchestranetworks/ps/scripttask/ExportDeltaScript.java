package com.orchestranetworks.ps.scripttask;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.comparison.*;
import com.orchestranetworks.workflow.*;

/**
 *
 * This script task bean exports created and updated records as XML into a file.
 * Creations and updates are identified by comparison of 2 homes (snapshot or dataspace)
 *
 * This script task bean must be declared in module.xml as follow:
 * <pre>{@code
	<bean className="com.orchestranetworks.ps.toolbox.workflow.scripttask.ExportDeltaScript">
        <documentation xml:lang="en-US">
            <label>Export Delta as XML</label>
            <description>
                This script export records from delta, created or modified, as XML.
            </description>
        </documentation>
        <properties>
            <property name="fromHome" input="true">
                <documentation xml:lang="en-US">
                     <label>From branch or version</label>
                    <description>
                        Technical identifier prefixed by B or V according to the nature of the home.
                    </description>
                </documentation>
            </property>
            <property name="toHome" input="true">
                <documentation xml:lang="en-US">
                    <label>To branch or version</label>
                    <description>
                        Technical identifier prefixed by B or V according to the nature of the home.
                    </description>
                </documentation>
            </property>
            <property name="header" input="true">
                <documentation xml:lang="en-US">
                    <label>XML Header</label>
                    <description>
                        Specific header to be added at the beginning of the file.
                        XML declaration is added by the script.
                    </description>
                </documentation>
            </property>
            <property name="destinationFile" input="true">
                <documentation xml:lang="en-US">
                    <label>Destination file</label>
                    <description>
                        Absolute path to the file of destination
                    </description>
                </documentation>
            </property>
            <property name="includeTechnicalData" input="true">
                <documentation xml:lang="en-US">
                    <label>Include Technical Data</label>
                    <description>
                        Default value is false
                    </description>
                </documentation>
            </property>
        </properties>
    </bean>
 * }</pre>
 * @author MCH
 */
public class ExportDeltaScript extends ScriptTaskBean
{
	private final static String XML_DECLARATION = "<?xml version='1.0' encoding='UTF-8'?>";

	private String fromHome;
	private String toHome;
	private String header;
	private String destinationFile;
	private Boolean includeTechnicalData = Boolean.FALSE;

	@Override
	public void executeScript(final ScriptTaskBeanContext pContext) throws OperationException
	{
		DifferenceBetweenHomes delta = this.getDelta(pContext);

		File file = new File(this.destinationFile);
		FileOutputStream output = null;
		try
		{
			output = new FileOutputStream(file);
			output.write(XML_DECLARATION.getBytes());
			output.write(System.getProperty("line.separator").getBytes());
			if (!StringUtils.isBlank(this.header))
			{
				output.write(this.header.getBytes());
			}
			output.write(System.getProperty("line.separator").getBytes());
		}
		catch (IOException ex)
		{
			OperationException.createError(ex.getLocalizedMessage());
		}

		final ExportSpec spec = this.getInitialExportSpec(output);

		final List<Adaptation> recordsInDelta = this.getRecordsFromDelta(delta);

		ProgrammaticService srv = ProgrammaticService
			.createForSession(pContext.getSession(), pContext.getRepository().getReferenceBranch());

		srv.execute(new ReadOnlyProcedure()
		{
			@Override
			public void execute(final ProcedureContext procContext) throws Exception
			{
				int nbOfRecordsInDelta = recordsInDelta.size();
				for (int i = 0; i < nbOfRecordsInDelta; i++)
				{
					if (i == nbOfRecordsInDelta - 1)
					{
						spec.setCloseStreamWhenFinished(true);
					}
					spec.setSourceAdaptation(recordsInDelta.get(i));
					procContext.doExport(spec);
				}
			}
		});

		try
		{
			if (output != null)
			{
				output.flush();
				output.close();
			}
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private DifferenceBetweenHomes getDelta(final ScriptTaskBeanContext pContext)
	{
		AdaptationHome leftHome = pContext.getRepository().lookupHome(HomeKey.parse(this.fromHome));
		if (leftHome == null)
		{
			OperationException.createError("Home '" + this.fromHome + "' cannot be found");
		}

		AdaptationHome rightHome = pContext.getRepository().lookupHome(HomeKey.parse(this.toHome));
		if (rightHome == null)
		{
			OperationException.createError("Home '" + this.toHome + "' cannot be found");
		}

		return DifferenceHelper.compareHomes(leftHome, rightHome, true);
	}

	public String getDestinationFile()
	{
		return this.destinationFile;
	}

	public String getFromHome()
	{
		return this.fromHome;
	}

	public String getHeader()
	{
		return this.header;
	}

	public Boolean getIncludeTechnicalData()
	{
		return this.includeTechnicalData;
	}

	private ExportSpec getInitialExportSpec(final OutputStream pOutput)
	{
		ExportSpec spec = new ExportSpec();
		spec.setDestinationStream(pOutput);
		spec.setCloseStreamWhenFinished(false);
		spec.setIncludesTechnicalData(this.includeTechnicalData.booleanValue());
		spec.setOmitXMLComment(true);
		spec.setOmitXMLDeclaration(true);
		return spec;
	}

	private List<Adaptation> getRecordsFromDelta(final DifferenceBetweenHomes delta)
	{
		List<Adaptation> recordsFromDelta = new ArrayList<>();
		for (DifferenceBetweenInstances deltaInstance : delta.getDeltaInstances())
		{
			for (DifferenceBetweenTables deltaTable : deltaInstance.getDeltaTables())
			{
				for (ExtraOccurrenceOnRight extraOnRight : deltaTable.getExtraOccurrencesOnRight())
				{
					recordsFromDelta.add(extraOnRight.getExtraAdaptationOnRight());
				}
				for (DifferenceBetweenOccurrences deltaOccurrences : deltaTable
					.getDeltaOccurrences())
				{
					recordsFromDelta.add(deltaOccurrences.getContentOnRight());
				}
			}
		}
		return recordsFromDelta;
	}

	public String getToHome()
	{
		return this.toHome;
	}

	public void setDestinationFile(final String destinationFile)
	{
		this.destinationFile = destinationFile;
	}

	public void setFromHome(final String fromHome)
	{
		this.fromHome = fromHome;
	}

	public void setHeader(final String header)
	{
		this.header = header;
	}

	public void setIncludeTechnicalData(final Boolean includeTechnicalData)
	{
		this.includeTechnicalData = includeTechnicalData;
	}

	public void setToHome(final String toHome)
	{
		this.toHome = toHome;
	}

}
