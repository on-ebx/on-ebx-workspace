package com.orchestranetworks.ps.util.matching;

import java.math.*;

import com.onwbp.adaptation.*;

/**
 * <p><b>SimMatchCandidate:</b> Holds the individual match score between a pivot record and it's match candidate.</p>
 * 
 * @author Craig Cox - Orchestra Networks - March 2015
 *
 */
public class SimMatchCandidate {

	private AdaptationName pivotrecord = null;
	private PrimaryKey primaryKey = null;
	private BigDecimal score = null;

	/**
	 * <p><b>SimMatchCandidate</b> (constructor) used to create a match candidate and also set the values. This is the only
	 * point where the values can be set.</p> 
	 * @param primaryKey the primary key of the match candidate 
	 * @param score the match percentage 0% not a match 100% perfect match
	 * @param pivot_record the record used as the pivot record for the match.
	 */
	public SimMatchCandidate(final PrimaryKey primaryKey, 
			final BigDecimal score,
			final AdaptationName pivot_record) {
		this.primaryKey = primaryKey;
		this.score = score;
		this.pivotrecord = pivot_record;
	}

	public AdaptationName getPivotRecord(){
		return pivotrecord;
	}
	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public BigDecimal getScore() {
		return score;
	}

}