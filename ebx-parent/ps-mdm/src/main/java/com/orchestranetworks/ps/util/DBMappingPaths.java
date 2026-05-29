package com.orchestranetworks.ps.util;

import com.orchestranetworks.schema.*;

public interface DBMappingPaths
{
	public static final Path DB_MAPPING_DM_PATH = Path.parse("/mappingDefinition/xs");
	public static final Path DB_MAPPING_TABLE_PATH = Path.parse("/mappingDefinition/txs");
	public static final Path DBM_DM_PATH = Path.parse("./xsPath");
	public static final Path DBM_TABLE_PATH = Path.parse("./tablePath");
	public static final Path DBM_DATA_MODEL = Path.parse("./xsId");
	public static final Path DB_TABLE_NAME = Path.parse("./tableNameInDB");
	public static final Path DBM_COLUMN_PATH = Path.parse("./columnPath");
	public static final Path DB_COLUMN_NAME = Path.parse("./columnNameInDB");
	public static final Path DBM_REPLICAS = Path.parse("./replicas");
	public static final Path DBM_COLUMNS = Path.parse("./columns");
	public static final Path DBM_LISTS = Path.parse("./lists_master");
	public static final Path DBM_LIST_PATH = Path.parse("./listPath");
	public static final Path DBM_AUX_TABLE_PATH = Path.parse("./auxTxsId");
}
