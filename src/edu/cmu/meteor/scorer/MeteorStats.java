/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2010
 * 
 * This software is distributed under the terms of the GNU Lesser General
 * Public License.  See the included COPYING and COPYING.LESSER files.
 * 
 */

package edu.cmu.meteor.scorer;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.InputMismatchException;
import java.util.Scanner;

import edu.cmu.meteor.aligner.Alignment;
import edu.cmu.meteor.util.Constants;

/**
 * Class used to hold several Meteor statistics, including final score
 * 
 */
public class MeteorStats {

	public static final int STATS_LENGTH = 23;

	/* Aggregable statistics */

	public double testLength;
	public double referenceLength;

	public double testFunctionWords;
	public double referenceFunctionWords;

	public double testTotalMatches;
	public double referenceTotalMatches;

	public ArrayList<Double> testStageMatchesContent;
	public ArrayList<Double> referenceStageMatchesContent;

	public ArrayList<Double> testStageMatchesFunction;
	public ArrayList<Double> referenceStageMatchesFunction;

	public double chunks;

	// Different in case of character-based evaluation
	public double testWordMatches;
	public double referenceWordMatches;

	/* Calculated statistics */

	/**
	 * Sums weighted by parameters
	 */
	public double testWeightedMatches;
	public double referenceWeightedMatches;

	public double testWeightedLength;
	public double referenceWeightedLength;

	public double precision;
	public double recall;
	public double f1;
	public double fMean;
	public double fragPenalty;

	/**
	 * Score is required to select the best reference
	 */
	public double score;

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// New statistics!

	/* Aggregable statistics */

	// The following work like testFunctionWords, referenceFunctionWords

	public double testAdjadvs;
	public double testNouns;
	public double testOther;
	public double testVerbs;
	public double referenceAdjadvs;
	public double referenceNouns;
	public double referenceOther;
	public double referenceVerbs;

	// The following work like testStageMatchesContent, testStageMatchesFunction,
	//						referenceStageMatchesContent, referenceStageMatchesFunction

	public ArrayList<ArrayList<Double>> XtestStageMatches;
	public ArrayList<ArrayList<Double>> XreferenceStageMatches;

	public ArrayList<Double> XtestStageMatches_Adjadvs;
	public ArrayList<Double> XtestStageMatches_Nouns;
	public ArrayList<Double> XtestStageMatches_Other;
	public ArrayList<Double> XtestStageMatches_Verbs;
	public ArrayList<Double> XreferenceStageMatches_Adjadvs;
	public ArrayList<Double> XreferenceStageMatches_Nouns;
	public ArrayList<Double> XreferenceStageMatches_Other;
	public ArrayList<Double> XreferenceStageMatches_Verbs;

	public double XtestTotalMatches;
	public double XreferenceTotalMatches;

	/* Calculated statistics */

	// The following work like testWeightedMatches, referenceWeightedMatches,
	//						testWeightedLength, referenceWeightedLength

	public double XprecisionNumerator;
	public double XrecallNumerator;

	public double XprecisionDenominator;
	public double XrecallDenominator;

	// New values

	public double Xprecision;
	public double Xrecall;
	public double Xf1;
	public double XfMean;
	public double Xscore;
	/**
	 * Also keep the underlying alignment if needed
	 */

	public Alignment alignment;

	public MeteorStats() {
		testLength = 0.0;
		referenceLength = 0.0;

		testFunctionWords = 0.0;
		referenceFunctionWords = 0.0;

		testTotalMatches = 0.0;
		referenceTotalMatches = 0.0;

		testStageMatchesContent = new ArrayList<Double>();
		referenceStageMatchesContent = new ArrayList<Double>();

		testStageMatchesFunction = new ArrayList<Double>();
		referenceStageMatchesFunction = new ArrayList<Double>();

		chunks = 0.0;

		testWordMatches = 0.0;
		referenceWordMatches = 0.0;

		// I.e., numerator, denominator

		testWeightedMatches = 0.0;
		referenceWeightedMatches = 0.0;

		testWeightedLength = 0.0;
		referenceWeightedLength = 0.0;

		// ------------------------------------------------------------------------
		// New statistics!

		testAdjadvs = 0.0;
		testNouns = 0.0;
		testOther = 0.0;
		testVerbs = 0.0;
		referenceAdjadvs = 0.0;
		referenceNouns = 0.0;
		referenceOther = 0.0;
		referenceVerbs = 0.0;

		XtestStageMatches = new ArrayList<ArrayList<Double>>();
		XreferenceStageMatches = new ArrayList<ArrayList<Double>>();

		XtestStageMatches_Adjadvs = new ArrayList<Double>();
		XtestStageMatches_Nouns = new ArrayList<Double>();
		XtestStageMatches_Other = new ArrayList<Double>();
		XtestStageMatches_Verbs = new ArrayList<Double>();

		XreferenceStageMatches_Adjadvs = new ArrayList<Double>();
		XreferenceStageMatches_Nouns = new ArrayList<Double>();
		XreferenceStageMatches_Other = new ArrayList<Double>();
		XreferenceStageMatches_Verbs = new ArrayList<Double>();

		XtestStageMatches.add(0, XtestStageMatches_Adjadvs);
		XtestStageMatches.add(1, XtestStageMatches_Nouns);
		XtestStageMatches.add(2, XtestStageMatches_Other);
		XtestStageMatches.add(3, XtestStageMatches_Verbs);
		XreferenceStageMatches.add(0, XreferenceStageMatches_Adjadvs);
		XreferenceStageMatches.add(1, XreferenceStageMatches_Nouns);
		XreferenceStageMatches.add(2, XreferenceStageMatches_Other);
		XreferenceStageMatches.add(3, XreferenceStageMatches_Verbs);

		XprecisionNumerator = 0.0;
		XrecallNumerator = 0.0;

		XprecisionDenominator = 0.0;
		XrecallDenominator = 0.0;

	}

	/**
	 * Aggregate SS (except score), result stored in this instance
	 * 
	 * @param ss
	 */
	public void addStats(MeteorStats ss) {

		testLength += ss.testLength;
		referenceLength += ss.referenceLength;

		testFunctionWords += ss.testFunctionWords;
		referenceFunctionWords += ss.referenceFunctionWords;

		testAdjadvs += ss.testAdjadvs;
		testNouns += ss.testNouns;
		testOther += ss.testOther;
		testVerbs += ss. testVerbs;
		referenceAdjadvs += ss.referenceAdjadvs;
		referenceNouns += ss.referenceNouns;
		referenceOther += ss.referenceOther;
		referenceVerbs += ss.referenceVerbs;

		testTotalMatches += ss.testTotalMatches;
		referenceTotalMatches += ss.referenceTotalMatches;

		int sizeDiff = ss.referenceStageMatchesContent.size()
				- referenceStageMatchesContent.size();

		int XsizeDiff = ss.XreferenceStageMatches_Adjadvs.size()
				- XreferenceStageMatches_Adjadvs.size();

		for (int i = 0; i < sizeDiff; i++) {
			testStageMatchesContent.add(0.0);
			referenceStageMatchesContent.add(0.0);
			testStageMatchesFunction.add(0.0);
			referenceStageMatchesFunction.add(0.0);
		}

		for (int i = 0; i < XsizeDiff; i++) {
			XtestStageMatches_Adjadvs.add(0.0);
			XreferenceStageMatches_Adjadvs.add(0.0);
			XtestStageMatches_Nouns.add(0.0);
			XreferenceStageMatches_Nouns.add(0.0);
			XtestStageMatches_Other.add(0.0);
			XreferenceStageMatches_Other.add(0.0);
			XtestStageMatches_Verbs.add(0.0);
			XreferenceStageMatches_Verbs.add(0.0);
		}

		for (int i = 0; i < ss.testStageMatchesContent.size(); i++)
			testStageMatchesContent.set(i, testStageMatchesContent.get(i)
					+ ss.testStageMatchesContent.get(i));
		for (int i = 0; i < ss.referenceStageMatchesContent.size(); i++)
			referenceStageMatchesContent.set(i,
					referenceStageMatchesContent.get(i)
							+ ss.referenceStageMatchesContent.get(i));
		for (int i = 0; i < ss.testStageMatchesFunction.size(); i++)
			testStageMatchesFunction.set(i, testStageMatchesFunction.get(i)
					+ ss.testStageMatchesFunction.get(i));
		for (int i = 0; i < ss.referenceStageMatchesFunction.size(); i++)
			referenceStageMatchesFunction.set(i,
					referenceStageMatchesFunction.get(i)
							+ ss.referenceStageMatchesFunction.get(i));

		// ------------------------------------------------------------------------
		// New statistics!

		for (int i = 0; i < ss.XtestStageMatches_Adjadvs.size(); i++)
			XtestStageMatches_Adjadvs.set(i, XtestStageMatches_Adjadvs.get(i)
					+ ss.XtestStageMatches_Adjadvs.get(i));
		for (int i = 0; i < ss.XreferenceStageMatches_Adjadvs.size(); i++)
			XreferenceStageMatches_Adjadvs.set(i, XreferenceStageMatches_Adjadvs.get(i)
					+ ss.XreferenceStageMatches_Adjadvs.get(i));

		for (int i = 0; i < ss.XtestStageMatches_Nouns.size(); i++)
			XtestStageMatches_Nouns.set(i, XtestStageMatches_Nouns.get(i)
					+ ss.XtestStageMatches_Nouns.get(i));
		for (int i = 0; i < ss.XreferenceStageMatches_Nouns.size(); i++)
			XreferenceStageMatches_Nouns.set(i, XreferenceStageMatches_Nouns.get(i)
					+ ss.XreferenceStageMatches_Nouns.get(i));

		for (int i = 0; i < ss.XtestStageMatches_Other.size(); i++)
			XtestStageMatches_Other.set(i, XtestStageMatches_Other.get(i)
					+ ss.XtestStageMatches_Other.get(i));
		for (int i = 0; i < ss.XreferenceStageMatches_Other.size(); i++)
			XreferenceStageMatches_Other.set(i, XreferenceStageMatches_Other.get(i)
					+ ss.XreferenceStageMatches_Other.get(i));

		for (int i = 0; i < ss.XtestStageMatches_Verbs.size(); i++)
			XtestStageMatches_Verbs.set(i, XtestStageMatches_Verbs.get(i)
					+ ss.XtestStageMatches_Verbs.get(i));
		for (int i = 0; i < ss.XreferenceStageMatches_Verbs.size(); i++)
			XreferenceStageMatches_Verbs.set(i, XreferenceStageMatches_Verbs.get(i)
					+ ss.XreferenceStageMatches_Verbs.get(i));


		if (!(ss.testTotalMatches == ss.testLength
				&& ss.referenceTotalMatches == ss.referenceLength && ss.chunks == 1.0))
			chunks += ss.chunks;

		testWordMatches += ss.testWordMatches;
		referenceWordMatches += ss.referenceWordMatches;

		// Score does not aggregate
	}

	/**
	 * Stats are output in lines:
	 * 
	 * tstLen refLen
	 * tstFuncWords refFuncWords
	 * stage1tstMatchesContent stage1refMatchesContent
	 * stage1tstMatchesFunction stage1refMatchesFunction
	 *
	 * s2tc s2rc s2tf s2rf s3tc s3rc s3tf s3rf s4tc s4rc s4tf s4rf chunks
	 * tstWordMatches refWordMatches
	 *
	 * PLUS!!…
	 *
	 * tstAdjadvs refAdjadvs tstNouns refNouns
	 * tstOther refOther tstVerbs refVerbs
	 * stage1tstMatchesNouns stage1refMatchesNouns,
	 * stage1tstMatchesAdjadvs stage1refMatchesAdjadvs
	 * stage1tstMatchesOther stage1refMatchesOther
	 * stage1tstMatchesVerbs stage1refMatchesVerbs
	 *
	 *
	 * ex: 15 14 4 3 6 6 2 2 1 1 0 0 1 1 0 0 2 2 1 1 3 15 14 .............
	 *
	 *
	 * 
	 * @param delim
	 */
	public String toString(String delim) {
		StringBuilder sb = new StringBuilder();
		sb.append(testLength + delim);
		sb.append(referenceLength + delim);

		sb.append(testFunctionWords + delim);
		sb.append(referenceFunctionWords + delim);

		for (int i = 0; i < Constants.MAX_MODULES; i++) {
			if (i < testStageMatchesContent.size()) {
				sb.append(testStageMatchesContent.get(i) + delim);
				sb.append(referenceStageMatchesContent.get(i) + delim);
				sb.append(testStageMatchesFunction.get(i) + delim);
				sb.append(referenceStageMatchesFunction.get(i) + delim);
			} else {
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
			}
		}
		sb.append(chunks + delim);
		sb.append(testWordMatches + delim);
		sb.append(referenceWordMatches + delim);

		// ------------------------------------------------------------------------
		// New statistics!

		sb.append(testAdjadvs + delim);
		sb.append(referenceAdjadvs + delim);
		sb.append(testNouns + delim);
		sb.append(referenceNouns + delim);
		sb.append(testOther + delim);
		sb.append(referenceOther + delim);
		sb.append(testVerbs + delim);
		sb.append(referenceVerbs + delim);

		for (int i = 0; i < Constants.MAX_MODULES; i++) {
			if (i < XtestStageMatches_Adjadvs.size()) {
				sb.append(XtestStageMatches_Adjadvs.get(i) + delim);
				sb.append(XreferenceStageMatches_Adjadvs.get(i) + delim);
				sb.append(XtestStageMatches_Nouns.get(i) + delim);
				sb.append(XreferenceStageMatches_Nouns.get(i) + delim);
				sb.append(XtestStageMatches_Other.get(i) + delim);
				sb.append(XreferenceStageMatches_Other.get(i) + delim);
				sb.append(XtestStageMatches_Verbs.get(i) + delim);
				sb.append(XreferenceStageMatches_Verbs.get(i) + delim);
			} else {
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
				sb.append(0.0 + delim);
			}
		}

		return sb.toString().trim();
	}

	public String toString() {
		return this.toString(" ");
	}

	/**
	 * Use a string from the toString() method to create a MeteorStats object.
	 * 
	 * @param ssString
	 */
	public MeteorStats(String ssString) {
		Scanner s = new Scanner(ssString);

		testLength = s.nextDouble();
		referenceLength = s.nextDouble();

		testFunctionWords = s.nextDouble();
		referenceFunctionWords = s.nextDouble();

		testTotalMatches = 0.0;
		referenceTotalMatches = 0.0;

		testStageMatchesContent = new ArrayList<Double>();
		referenceStageMatchesContent = new ArrayList<Double>();
		testStageMatchesFunction = new ArrayList<Double>();
		referenceStageMatchesFunction = new ArrayList<Double>();

		for (int i = 0; i < Constants.MAX_MODULES; i++) {

			double tstC = s.nextDouble();
			double refC = s.nextDouble();

			testTotalMatches += tstC;
			referenceTotalMatches += refC;

			testStageMatchesContent.add(tstC);
			referenceStageMatchesContent.add(refC);

			double tstF = s.nextDouble();
			double refF = s.nextDouble();

			testTotalMatches += tstF;
			referenceTotalMatches += refF;

			testStageMatchesFunction.add(tstF);
			referenceStageMatchesFunction.add(refF);
		}

		chunks = s.nextDouble();

		testWordMatches = s.nextDouble();
		referenceWordMatches = s.nextDouble();

		// ------------------------------------------------------------------------
		// New statistics!

		testAdjadvs = s.nextDouble();
		referenceAdjadvs = s.nextDouble();
		testNouns = s.nextDouble();
		referenceNouns = s.nextDouble();
		testOther = s.nextDouble();
		referenceOther = s.nextDouble();
		testVerbs = s.nextDouble();
		referenceVerbs = s.nextDouble();

		XtestTotalMatches = 0.0;
		XreferenceTotalMatches = 0.0;

		XtestStageMatches_Adjadvs = new ArrayList<Double>();
		XreferenceStageMatches_Adjadvs = new ArrayList<Double>();
		XtestStageMatches_Nouns = new ArrayList<Double>();
		XreferenceStageMatches_Nouns = new ArrayList<Double>();
		XtestStageMatches_Other = new ArrayList<Double>();
		XreferenceStageMatches_Other = new ArrayList<Double>();
		XtestStageMatches_Verbs = new ArrayList<Double>();
		XreferenceStageMatches_Verbs = new ArrayList<Double>();

		for (int i = 0; i < Constants.MAX_MODULES; i++) {

			double tstA = s.nextDouble();
			double refA = s.nextDouble();

			XtestTotalMatches += tstA;
			XreferenceTotalMatches += refA;

			XtestStageMatches_Adjadvs.add(tstA);
			XreferenceStageMatches_Adjadvs.add(refA);

			double tstN = s.nextDouble();
			double refN = s.nextDouble();

			XtestTotalMatches += tstN;
			XreferenceTotalMatches += refN;

			XtestStageMatches_Nouns.add(tstN);
			XreferenceStageMatches_Nouns.add(refN);

			double tstO = s.nextDouble();
			double refO = s.nextDouble();

			XtestTotalMatches += tstO;
			XreferenceTotalMatches += refO;

			XtestStageMatches_Other.add(tstO);
			XreferenceStageMatches_Other.add(refO);

			double tstV = s.nextDouble();
			double refV = s.nextDouble();

			XtestTotalMatches += tstV;
			XreferenceTotalMatches += refV;

			XtestStageMatches_Verbs.add(tstV);
			XreferenceStageMatches_Verbs.add(refV);
		}

		XtestStageMatches = new ArrayList<ArrayList<Double>>();
		XreferenceStageMatches = new ArrayList<ArrayList<Double>>();

		XtestStageMatches.add(0, XtestStageMatches_Adjadvs);
		XtestStageMatches.add(1, XtestStageMatches_Nouns);
		XtestStageMatches.add(2, XtestStageMatches_Other);
		XtestStageMatches.add(3, XtestStageMatches_Verbs);

		XreferenceStageMatches.add(0, XreferenceStageMatches_Adjadvs);
		XreferenceStageMatches.add(1, XreferenceStageMatches_Nouns);
		XreferenceStageMatches.add(2, XreferenceStageMatches_Other);
		XreferenceStageMatches.add(3, XreferenceStageMatches_Verbs);

	}

}