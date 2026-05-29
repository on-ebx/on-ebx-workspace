package com.orchestranetworks.ps.constraint;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.adaptation.AdaptationName;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * When a field in a table represents a foreign key, but for varying reasons, a fk constraint is not desired,
 * this constraint enumeration can be used for selecting a record in a specific foreign table.
 */
public class RecordInSpecificTableConstraintEnumeration extends AbstractRecordInTableConstraintEnumeration
{
	private String branchKey;
	private String datasetName;
	private Path tablePath;

	public Path getTablePath()
	{
		return tablePath;
	}

	public void setTablePath(Path tablePath)
	{
		this.tablePath = tablePath;
	}

	@Override
	public void setup(ConstraintContext context)
	{
	}

	@Override
	public Adaptation getDataSet(ValueContext context) {
		Adaptation currDataSet = context.getAdaptationInstance();
		if (datasetName != null) {
			AdaptationHome branch = currDataSet.getHome();
			if (branchKey != null) {
				branch = currDataSet.getHome().getRepository().lookupHome(HomeKey.forBranchName(branchKey));
			}
			return branch.findAdaptationOrNull(AdaptationName.forName(datasetName));
		}
		return currDataSet;
	}

	public String getBranchKey() {
		return branchKey;
	}

	public void setBranchKey(String branchKey) {
		this.branchKey = branchKey;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	@Override
	protected Path getTablePath(ValueContext context) {
		return tablePath;
	}

}
