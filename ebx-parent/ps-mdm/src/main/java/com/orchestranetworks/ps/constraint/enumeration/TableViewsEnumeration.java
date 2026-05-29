package com.orchestranetworks.ps.constraint.enumeration;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.bean.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Enumerates the table views for a given table. Views are associated with the data model of the table.
 * There are multiple ways you can specify the data model:
 * 
 * <ul>
 * <li>You can provide the names of a dataspace and dataset.</li>
 * <li>You can provide paths for fields on the record that contain the dataspace and dataset names.</li>
 * <li>You can provide the module and schema of the data model when it's packaged in the module.</li>
 * <li>You can provide the publication name of the data model when it's published in embedded mode.</li>
 * </ul>
 * 
 * To specify the table, you can either specify the table path or you can specify a path to a field on
 * the record that contains the table path.
 * 
 * By default, it only shows published views, but can be configured to show non-published as well.
 */
public class TableViewsEnumeration implements ConstraintEnumeration<String>
{
	private String dataspace;
	private Path pathToDataspace;

	private String dataset;
	private Path pathToDataset;

	private String dataModelModule;
	private String dataModelSchema;

	private String dataModelPublication;

	private Path tablePath;
	private Path pathToTablePath;

	private boolean includeNonPublished;

	private SchemaNode dataspaceNode;
	private SchemaNode datasetNode;
	private SchemaNode tablePathNode;

	@Override
	public void checkOccurrence(String value, ValueContextForValidation context)
		throws InvalidSchemaException
	{
		// TODO: Can put in logic to validate that value is a valid view for the table
	}

	@Override
	public String displayOccurrence(String value, ValueContext valueContext, Locale locale)
		throws InvalidSchemaException
	{
		Adaptation viewsDataSet = AdminUtil.getViewsDataSet(valueContext.getHome().getRepository());
		AdaptationTable customViewsTable = AdminUtil.getCustomViewsTable(viewsDataSet);
		Adaptation customViewRecord;
		// If including non-published, then the value is the primary key of the record.
		// Otherwise, the value is the published name
		if (includeNonPublished)
		{
			customViewRecord = customViewsTable
				.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(value));
		}
		else
		{
			RequestResult requestResult = customViewsTable.createRequestResult(
				AdminUtil.getCustomViewsPublicationNamePath().format() + "='" + value + "'");
			try
			{
				customViewRecord = requestResult.nextAdaptation();
			}
			finally
			{
				requestResult.close();
			}
		}
		if (customViewRecord == null)
		{
			return value;
		}

		String viewLabel = null;
		// There's no way in the public API to get the details in the label,
		// so have to use a non-public API
		LabelDescription documentation = (LabelDescription) customViewRecord
			.get(AdminUtil.getCustomViewsDocumentationPath());
		if (documentation != null)
		{
			LabelDescriptionForLocale localizedDocumentation = documentation
				.getLocalizedDocumentation(locale);
			if (localizedDocumentation != null)
			{
				viewLabel = localizedDocumentation.getLabel();
			}
		}
		String publicationName = customViewRecord
			.getString(AdminUtil.getCustomViewsPublicationNamePath());
		StringBuilder bldr = new StringBuilder();
		if (viewLabel == null)
		{
			bldr.append(customViewRecord.getString(AdminUtil.getCustomViewsTablePathPath()));
		}
		else
		{
			bldr.append(viewLabel);
			if (publicationName != null)
			{
				bldr.append(" ");
			}
		}
		if (publicationName != null)
		{
			bldr.append("[");
			bldr.append(publicationName);
			bldr.append("]");
		}
		return bldr.toString();
	}

	@Override
	public List<String> getValues(ValueContext valueContext) throws InvalidSchemaException
	{
		Adaptation viewsDataSet = AdminUtil.getViewsDataSet(valueContext.getHome().getRepository());
		AdaptationTable customViewsTable = AdminUtil.getCustomViewsTable(viewsDataSet);
		String predicate;
		try
		{
			predicate = createViewsPredicate(valueContext);
		}
		catch (OperationException ex)
		{
			throw new InvalidSchemaException(ex);
		}

		List<String> values = new ArrayList<>();
		RequestResult requestResult = customViewsTable.createRequestResult(predicate);
		try
		{
			for (Adaptation customViewRecord; (customViewRecord = requestResult
				.nextAdaptation()) != null;)
			{
				// If including non-published, then the value is the primary key of the record.
				// Otherwise, the value is the published name
				if (includeNonPublished)
				{
					values.add(customViewRecord.getOccurrencePrimaryKey().format());
				}
				else
				{
					values.add(
						customViewRecord.getString(AdminUtil.getCustomViewsPublicationNamePath()));
				}
			}
		}
		finally
		{
			requestResult.close();
		}
		return values;
	}

	private String createViewsPredicate(ValueContext valueContext) throws OperationException
	{
		StringBuilder bldr = new StringBuilder();

		bldr.append(AdminUtil.getCustomViewsSchemaKeyPath().format());
		bldr.append("=");
		String schemaKey = createDataModelSchemaKey(valueContext);
		bldr.append(XPathExpressionHelper.encodeLiteralStringWithDelimiters(schemaKey));
		bldr.append(" and ");

		bldr.append(AdminUtil.getCustomViewsTablePathPath().format());
		bldr.append("='");
		String tablePathStr = getTablePathString(valueContext);
		bldr.append(tablePathStr);
		bldr.append("'");

		if (!includeNonPublished)
		{
			bldr.append(" and osd:is-not-null(");
			bldr.append(AdminUtil.getCustomViewsPublicationNamePath().format());
			bldr.append(")");
		}

		return bldr.toString();
	}

	private String createDataModelSchemaKey(ValueContext valueContext) throws OperationException
	{
		String schemaKey;
		Adaptation datasetRef = lookupDataset(valueContext);
		if (datasetRef == null)
		{
			if (dataModelPublication == null)
			{
				schemaKey = createPackagedDataModelSchemaKey(dataModelModule, dataModelSchema);
			}
			else
			{
				schemaKey = createEmbeddedDataModelSchemaKey(dataModelPublication);
			}
		}
		else
		{
			SchemaLocation schemaLocation = datasetRef.getSchemaLocation();
			String schemaLocationStr = schemaLocation.format();
			String lastSchemaLocationSegment = schemaLocationStr
				.substring(schemaLocationStr.lastIndexOf(":") + 1);
			if (schemaLocation.isPackagedInModule())
			{
				schemaKey = createPackagedDataModelSchemaKey(
					schemaLocation.getModuleName(),
					lastSchemaLocationSegment);
			}
			else
			{
				schemaKey = createEmbeddedDataModelSchemaKey(lastSchemaLocationSegment);
			}
		}
		return schemaKey;
	}

	private static String createPackagedDataModelSchemaKey(String moduleName, String schemaPath)
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append("module: ");
		bldr.append(moduleName);
		bldr.append(", path: ");
		bldr.append(schemaPath);
		return bldr.toString();
	}

	private static String createEmbeddedDataModelSchemaKey(String publicationName)
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append("PublicationName: ");
		bldr.append(publicationName);
		return bldr.toString();
	}

	private Adaptation lookupDataset(ValueContext valueContext) throws OperationException
	{
		String dataspaceName = dataspace;
		if (dataspaceName == null)
		{
			if (dataspaceNode != null)
			{
				dataspaceName = (String) valueContext.getValue(dataspaceNode);
			}
		}
		if (dataspaceName == null)
		{
			return null;
		}

		String datasetName = dataset;
		if (datasetName == null)
		{
			if (datasetNode != null)
			{
				datasetName = (String) valueContext.getValue(datasetNode);
			}
		}
		if (datasetName == null)
		{
			return null;
		}

		return AdaptationUtil
			.getDataSetOrThrowOperationException(valueContext, dataspaceName, datasetName);
	}

	private String getTablePathString(ValueContext valueContext)
	{
		return tablePath == null ? (String) valueContext.getValue(tablePathNode)
			: tablePath.format();
	}

	@Override
	public void setup(ConstraintContext context)
	{
		boolean tablePathSpecified = false;
		if (tablePath == null)
		{
			if (pathToTablePath != null)
			{
				tablePathSpecified = true;
			}
		}
		else
		{
			tablePathSpecified = true;
			if (pathToTablePath != null)
			{
				context.addError("Can't specify both tablePath and pathToTablePath parameters.");
			}
		}

		if (!tablePathSpecified)
		{
			context.addError("Must specify either tablePath or pathToTablePath parameter.");
		}

		boolean dataspaceSpecified = false;
		if (dataspace == null)
		{
			if (pathToDataspace != null)
			{
				dataspaceSpecified = true;
			}
		}
		else
		{
			dataspaceSpecified = true;
			if (pathToDataspace != null)
			{
				context.addError("Can't specify both dataspace and pathToDataspace parameters.");
			}
		}

		boolean datasetSpecified = false;
		if (dataset == null)
		{
			if (pathToDataset != null)
			{
				datasetSpecified = true;
			}
		}
		else
		{
			datasetSpecified = true;
			if (pathToDataset != null)
			{
				context.addError("Can't specify both dataset and pathToDataset parameters.");
			}
		}

		if (dataspaceSpecified && !datasetSpecified)
		{
			context.addError(
				"Must specify either dataset or pathToDataset when dataspace or pathToDataspace is specified.");
		}
		else if (!dataspaceSpecified && datasetSpecified)
		{
			context.addError(
				"Must specify either dataspace or pathToDataspace when dataset or pathToDataSet is specified.");
		}

		if (dataModelModule != null && dataModelSchema == null)
		{
			context.addError("Must specify dataModelSchema when dataModelModule is specified.");
		}
		else if (dataModelModule == null && dataModelSchema != null)
		{
			context.addError("Must specify dataModelModule when dataModelSchema is specified.");
		}

		if (dataModelPublication != null && (dataModelModule != null || dataModelSchema != null))
		{
			context.addError(
				"Can't specify dataModelPublication parameter when dataModelModule and dataModelSchema parameters are specified.");
		}

		if (dataModelPublication == null && dataModelModule == null && dataModelSchema == null)
		{
			if (!dataspaceSpecified && !datasetSpecified)
			{
				context.addError(
					"Must specify either the data model or the dataspace/dataset combination.");
			}
		}
		else
		{
			if (dataspaceSpecified || datasetSpecified)
			{
				context.addError(
					"Can't specify both the data model and the dataspace/dataset combination.");
			}
		}

		dataspaceNode = PathUtils.setupFieldNode(
			context,
			context.getSchemaNode(),
			pathToDataspace,
			"pathToDataspace",
			false,
			true);
		datasetNode = PathUtils.setupFieldNode(
			context,
			context.getSchemaNode(),
			pathToDataset,
			"pathToDataset",
			false,
			true);
		tablePathNode = PathUtils.setupFieldNode(
			context,
			context.getSchemaNode(),
			pathToTablePath,
			"pathToTablePath",
			false,
			true);
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		// TODO: If we put in logic to validate that value is a valid view for the table, then should add a message
		return null;
	}

	/**
	 * @see #setDataspace(String)
	 */
	public String getDataspace()
	{
		return dataspace;
	}

	/**
	 * Set the data space.
	 * Should be used in conjunction with setting the dataset and not used when specifying the model schema.
	 * 
	 * @param dataspace the dataspace key
	 */
	public void setDataspace(String dataspace)
	{
		this.dataspace = dataspace;
	}

	/**
	 * @see #setPathToDataspace(Path)
	 */
	public Path getPathToDataspace()
	{
		return pathToDataspace;
	}

	/**
	 * Set the path to the data space, when the value is captured in a field.
	 * Should be used in conjunction with setting the dataset and not used when specifying the model schema.
	 * 
	 * @param pathToDataspace the field path
	 */
	public void setPathToDataspace(Path pathToDataspace)
	{
		this.pathToDataspace = pathToDataspace;
	}

	/**
	 * @see #setDataset(String)
	 */
	public String getDataset()
	{
		return dataset;
	}

	/**
	 * Set the data set.
	 * Should be used in conjunction with setting the dataspace and not used when specifying the model schema.
	 * 
	 * @param dataset the dataset name
	 */
	public void setDataset(String dataset)
	{
		this.dataset = dataset;
	}

	/**
	 * @see #setPathToDataset(Path)
	 */
	public Path getPathToDataset()
	{
		return pathToDataset;
	}

	/**
	 * Set the path to the data set, when the value is captured in a field.
	 * Should be used in conjunction with setting the dataspace and not used when specifying the model schema.
	 * 
	 * @param pathToDataset the field path
	 */
	public void setPathToDataset(Path pathToDataset)
	{
		this.pathToDataset = pathToDataset;
	}

	/**
	 * @see #setDataModelModule(String)
	 */
	public String getDataModelModule()
	{
		return dataModelModule;
	}

	/**
	 * Set the data model module, when it's packaged.
	 * Should be used in conjunction with setting the data model schema and not used when specifying the dataspace and dataset
	 * or when specifying the data model publication.
	 * 
	 * @param dataModelModule the module
	 */
	public void setDataModelModule(String dataModelModule)
	{
		this.dataModelModule = dataModelModule;
	}

	/**
	 * @see #setDataModelSchema(String)
	 */
	public String getDataModelSchema()
	{
		return dataModelSchema;
	}

	/**
	 * Set the data model schema, when it's packaged (i.e. /WEB-INF/ebx/schemas/MySchema.xsd).
	 * Should be used in conjunction with setting the data model module and not used when specifying the dataspace and dataset
	 * or when specifying the data model publication.
	 * 
	 * @param dataModelSchema the schema
	 */
	public void setDataModelSchema(String dataModelSchema)
	{
		this.dataModelSchema = dataModelSchema;
	}

	/**
	 * @see #setDataModelPublication(String)
	 */
	public String getDataModelPublication()
	{
		return dataModelPublication;
	}

	/**
	 * Set the data model publication, when it's embedded.
	 * Should not be used when specifying the dataspace and dataset
	 * or when specifying the data model module and schema.
	 * 
	 * @param dataModelPublication the publication
	 */
	public void setDataModelPublication(String dataModelPublication)
	{
		this.dataModelPublication = dataModelPublication;
	}

	/**
	 * @see #setTablePath(Path)
	 */
	public Path getTablePath()
	{
		return tablePath;
	}

	/**
	 * Set the table path
	 * 
	 * @param tablePath the table path
	 */
	public void setTablePath(Path tablePath)
	{
		this.tablePath = tablePath;
	}

	/**
	 * @see #setPathToTablePath(Path)
	 */
	public Path getPathToTablePath()
	{
		return pathToTablePath;
	}

	/**
	 * Set the path to the table path, when the value is captured in a field
	* 
	 * @param pathToTablePath the field path
	 */
	public void setPathToTablePath(Path pathToTablePath)
	{
		this.pathToTablePath = pathToTablePath;
	}

	/**
	 * @see #setIncludeNonPublished(boolean)
	 */
	public boolean isIncludeNonPublished()
	{
		return includeNonPublished;
	}

	/**
	 * Set whether to include non-published views. By default, they are not included.
	 * 
	 * @param includeNonPublished whether to include non-published views
	 */
	public void setIncludeNonPublished(boolean includeNonPublished)
	{
		this.includeNonPublished = includeNonPublished;
	}
}
