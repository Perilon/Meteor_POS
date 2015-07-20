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

import edu.cmu.meteor.aligner.Aligner;
import edu.cmu.meteor.aligner.Alignment;
import edu.cmu.meteor.aligner.Match;
import edu.cmu.meteor.util.Constants;
import edu.cmu.meteor.util.Normalizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Entry point class which oversees Meteor scoring. Instantiate with either the
 * default configuration (no args) or an existing MeteorConfiguration. Call the
 * getMeteorStats() methods to obtain MeteorStats objects which include a score
 * field
 * 
 */
public class MeteorScorer {

	private MaxentTagger tagger;

	private Aligner aligner;
	private String language;
	private int langID;

	private boolean normalize;
	private boolean keepPunctuation;
	private boolean lowerCase;

	// Parameters
	private double alpha;
	private double beta;
	private double gamma;
	private double delta;

	// Weights
	private ArrayList<Double> moduleWeights;

	private ArrayList<Double> newDeltaWeights;

	private boolean charBased;

	/**
	 * Use default configuration
	 * 
	 */
	public MeteorScorer() {
		MeteorConfiguration config = new MeteorConfiguration();
		loadConfiguration(config);
	}

	/**
	 * Use a custom configuration
	 * 
	 * @param config
	 */
	public MeteorScorer(MeteorConfiguration config) {
		loadConfiguration(config);
	}

	/**
	 * Create a new scorer that shares thread-safe resources with an existing
	 * scorer
	 * 
	 * @param scorer
	 */
	public MeteorScorer(MeteorScorer scorer) {
		language = scorer.language;
		langID = scorer.langID;
		normalize = scorer.normalize;
		keepPunctuation = scorer.keepPunctuation;
		lowerCase = scorer.lowerCase;
		alpha = scorer.alpha;
		beta = scorer.beta;
		gamma = scorer.gamma;
		delta = scorer.delta;

		newDeltaWeights = scorer.newDeltaWeights;

		moduleWeights = new ArrayList<Double>(scorer.moduleWeights);

		newDeltaWeights = new ArrayList<Double>(scorer.newDeltaWeights);

		aligner = new Aligner(scorer.aligner);
		charBased = scorer.charBased;
	}

	/**
	 * Load configuration (only used by constructor)
	 * 
	 * @param config
	 */
	private void loadConfiguration(MeteorConfiguration config) {
		language = config.getLanguage();
		langID = config.getLangID();
		setNormalize(config.getNormalization());
		ArrayList<Double> parameters = config.getParameters();
		alpha = parameters.get(0);
		beta = parameters.get(1);
		gamma = parameters.get(2);
		delta = parameters.get(3);

		moduleWeights = config.getModuleWeights();

		// New!  New delta weights

		newDeltaWeights = config.getNewDeltaWeights();

		aligner = new Aligner(tagger, language, config.getModules(),
				config.getModuleWeights(),
				config.getNewDeltaWeights(),
				config.getBeamSize(),
				config.getWordFileURL(), config.getSynDirURL(),
				config.getParaDirURL(), config.getPoscatsAdjadvURL(), config.getPoscatsNounURL(),
				config.getPoscatsOtherURL(), config.getPoscatsVerbURL(),
				// Best alignments for evaluation
				Constants.PARTIAL_COMPARE_TOTAL);
		// Best weights for evaluation
		ArrayList<Integer> modules = config.getModules();
		ArrayList<Double> setWeights = new ArrayList<Double>();
		for (int module : modules) {
			if (module == Constants.MODULE_EXACT)
				setWeights.add(1.0);
			else if (module == Constants.MODULE_STEM)
				setWeights.add(0.5);
			else if (module == Constants.MODULE_SYNONYM)
				setWeights.add(0.5);
			else
				setWeights.add(0.5);
		}
		aligner.updateModuleWeights(setWeights);
		charBased = config.getCharBased();
	}

	/**
	 * Set normalization type
	 * 
	 * @param normtype
	 */
	private void setNormalize(int normtype) {
		if (normtype == Constants.NORMALIZE_LC_ONLY) {
			this.normalize = false;
			this.keepPunctuation = true;
			this.lowerCase = true;
		} else if (normtype == Constants.NORMALIZE_KEEP_PUNCT) {
			this.normalize = true;
			this.keepPunctuation = true;
			this.lowerCase = true;

		} else if (normtype == Constants.NORMALIZE_NO_PUNCT) {
			this.normalize = true;
			this.keepPunctuation = false;
			this.lowerCase = true;
		} else {
			// Assume NO_NORMALIZE
			this.normalize = false;
			this.keepPunctuation = true;
			this.lowerCase = false;
		}
	}

	/**
	 * Update module weights without reloading resources. This is used for
	 * tuning and is likely not needed for most applications
	 * 
	 * @param moduleWeights
	 */
	public void updateModuleWeights(ArrayList<Double> moduleWeights) {
		aligner.updateModuleWeights(moduleWeights);
	}

	/**
	 * Get stats when test and reference are already tokenized and normalized
	 * (Make sure you know what you're doing)
	 * 
	 * @param test
	 * @param reference
	 * @return
	 */
	public MeteorStats getMeteorStats(ArrayList<String> test,
			ArrayList<String> reference) {
		Alignment alignment = aligner.align(test, reference);
		return getMeteorStats(alignment);
	}

	/**
	 * Get the Meteor sufficient statistics for a test / reference pair
	 * 
	 * @param test
	 * @param reference
	 * @return
	 */
	public MeteorStats getMeteorStats(String test, String reference, MaxentTagger tagger) {
		// Normalize both
		if (normalize) {
			test = Normalizer.normalizeLine(test, langID, keepPunctuation);
			reference = Normalizer.normalizeLine(reference, langID,
					keepPunctuation);
		}
		// Lowercase both
		if (lowerCase) {
			test = test.toLowerCase();
			reference = reference.toLowerCase();
		}
		// Score
		Alignment alignment = aligner.align(test, reference, tagger);
		return getMeteorStats(alignment);
	}

	/**
	 * Get the Meteor sufficient statistics for a test give a list of references
	 * 
	 * @param test
	 * @param references
	 * @return
	 */
	public MeteorStats getMeteorStats(String test, ArrayList<String> references, MaxentTagger tagger) {
		// Normalize test
		if (normalize)
			test = Normalizer.normalizeLine(test, langID, keepPunctuation);
		if (lowerCase)
			test = test.toLowerCase();
		MeteorStats stats = new MeteorStats();
		stats.score = -1;
		// Score each reference
		for (String reference : references) {
			// Normalize reference
			if (normalize)
				reference = Normalizer.normalizeLine(reference, langID,
						keepPunctuation);
			if (lowerCase)
				reference = reference.toLowerCase();
			Alignment alignment = aligner.align(test, reference, tagger);
			MeteorStats curStats = getMeteorStats(alignment);
			if (curStats.score > stats.score)
				stats = curStats;
		}
		return stats;
	}

	/**
	 * Get the Meteor sufficient statistics for an alignment
	 * 
	 * @param alignment
	 * @return
	 */
	public MeteorStats getMeteorStats(Alignment alignment) {
		MeteorStats stats = new MeteorStats();

		// Copy alignment stats

		// Sum word lengths if evaluating by character
		if (charBased) {
			stats.testLength = 0;
			for (String word : alignment.words1)
				stats.testLength += word.length();
			stats.referenceLength = 0;
			for (String word : alignment.words2)
				stats.referenceLength += word.length();
			stats.testFunctionWords = 0;
			for (int i : alignment.line1FunctionWords)
				stats.testFunctionWords += alignment.words1.get(i).length();
			stats.referenceFunctionWords = 0;
			for (int i : alignment.line2FunctionWords)
				stats.referenceFunctionWords += alignment.words2.get(i)
						.length();

			// Module and total matches with summed word lengths
			int[] testStageMatchesContent = new int[alignment.moduleContentMatches1
					.size()];
			int[] referenceStageMatchesContent = new int[alignment.moduleContentMatches1
					.size()];
			int[] testStageMatchesFunction = new int[alignment.moduleContentMatches1
					.size()];
			int[] referenceStageMatchesFunction = new int[alignment.moduleContentMatches1
					.size()];
			// Sum these here to avoid pushing character-level operations to the
			// aligner
			for (Match m : alignment.matches) {
				if (m != null) {
					for (int i = 0; i < m.matchLength; i++)
						if (alignment.line1FunctionWords.contains(m.matchStart
								+ i))
							testStageMatchesFunction[m.module] += alignment.words1
									.get(m.matchStart + i).length();
						else
							testStageMatchesContent[m.module] += alignment.words1
									.get(m.matchStart + i).length();
					for (int i = 0; i < m.length; i++)
						if (alignment.line2FunctionWords.contains(m.start + i))
							referenceStageMatchesFunction[m.module] += alignment.words2
									.get(m.start + i).length();
						else
							referenceStageMatchesContent[m.module] += alignment.words2
									.get(m.start + i).length();
				}
			}
			for (int i = 0; i < alignment.moduleContentMatches1.size(); i++) {
				stats.testStageMatchesContent
						.add((double) testStageMatchesContent[i]);
				stats.referenceStageMatchesContent
						.add((double) referenceStageMatchesContent[i]);
				stats.testStageMatchesFunction
						.add((double) testStageMatchesFunction[i]);
				stats.referenceStageMatchesFunction
						.add((double) referenceStageMatchesFunction[i]);
			}
		}
		// Otherwise use word counts
		else {
			stats.testLength = alignment.words1.size();
			stats.referenceLength = alignment.words2.size();
			stats.testFunctionWords = alignment.line1FunctionWords.size();
			stats.referenceFunctionWords = alignment.line2FunctionWords.size();

			// New!
			// ----------------------------------
			// ----------------------------------

			stats.testAdjadvs = alignment.line1Adjadvs.size();
			stats.referenceAdjadvs = alignment.line2Adjadvs.size();
			stats.testNouns = alignment.line1Nouns.size();
			stats.referenceNouns = alignment.line2Nouns.size();
			stats.testOther = alignment.line1Other.size();
			stats.referenceOther = alignment.line2Other.size();
			stats.testVerbs = alignment.line1Verbs.size();
			stats.referenceVerbs = alignment.line2Verbs.size();

			stats.testStageMatchesContent = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleContentMatches1.size();
				 stats.testStageMatchesContent.add((double) alignment.moduleContentMatches1.get(i++)));

			stats.referenceStageMatchesContent = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleContentMatches2.size();
				 stats.referenceStageMatchesContent.add((double) alignment.moduleContentMatches2.get(i++)));

			stats.testStageMatchesFunction = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleFunctionMatches1.size();
				 stats.testStageMatchesFunction.add((double) alignment.moduleFunctionMatches1.get(i++)));

			stats.referenceStageMatchesFunction = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleFunctionMatches2.size();
				 stats.referenceStageMatchesFunction.add((double) alignment.moduleFunctionMatches2.get(i++)));

			// New!
			// ----------------------------------
			// ----------------------------------

			stats.XtestStageMatches_Adjadvs = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleAdjAdvsMatches1.size();
					stats.XtestStageMatches_Adjadvs.add((double) alignment.moduleAdjAdvsMatches1.get(i++)));
			stats.XreferenceStageMatches_Adjadvs = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleAdjAdvsMatches2.size();
				 stats.XreferenceStageMatches_Adjadvs.add((double) alignment.moduleAdjAdvsMatches2.get(i++)));

			stats.XtestStageMatches_Nouns = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleNounsMatches1.size();
				 stats.XtestStageMatches_Nouns.add((double) alignment.moduleNounsMatches1.get(i++)));
			stats.XreferenceStageMatches_Nouns = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleNounsMatches2.size();
				 stats.XreferenceStageMatches_Nouns.add((double) alignment.moduleNounsMatches2.get(i++)));

			stats.XtestStageMatches_Other = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleOtherMatches1.size();
				 stats.XtestStageMatches_Other.add((double) alignment.moduleOtherMatches1.get(i++)));
			stats.XreferenceStageMatches_Other = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleOtherMatches2.size();
				 stats.XreferenceStageMatches_Other.add((double) alignment.moduleOtherMatches2.get(i++)));

			stats.XtestStageMatches_Verbs = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleVerbsMatches1.size();
				 stats.XtestStageMatches_Verbs.add((double) alignment.moduleVerbsMatches1.get(i++)));
			stats.XreferenceStageMatches_Verbs = new ArrayList<Double>();
			for (int i = 0; i < alignment.moduleVerbsMatches2.size();
				 stats.XreferenceStageMatches_Verbs.add((double) alignment.moduleVerbsMatches2.get(i++)));

			stats.XtestStageMatches = new ArrayList<ArrayList<Double>>();
			stats.XreferenceStageMatches = new ArrayList<ArrayList<Double>>();

			stats.XtestStageMatches.add(0, stats.XtestStageMatches_Adjadvs);
			stats.XtestStageMatches.add(1, stats.XtestStageMatches_Nouns);
			stats.XtestStageMatches.add(2, stats.XtestStageMatches_Other);
			stats.XtestStageMatches.add(3, stats.XtestStageMatches_Verbs);

			stats.XreferenceStageMatches.add(0, stats.XreferenceStageMatches_Adjadvs);
			stats.XreferenceStageMatches.add(1, stats.XreferenceStageMatches_Nouns);
			stats.XreferenceStageMatches.add(2, stats.XreferenceStageMatches_Other);
			stats.XreferenceStageMatches.add(3, stats.XreferenceStageMatches_Verbs);

		}

		// Same for word and character level
		stats.chunks = alignment.numChunks;

		// Total matches
		// Important: sum from stage matches instead of taking total from
		// alignment as alignment totals are WEIGHTED and totals here are
		// UNWEIGHTED
		for (int i = 0; i < stats.testStageMatchesContent.size(); i++) {
			stats.testTotalMatches += stats.testStageMatchesContent.get(i);
			stats.testTotalMatches += stats.testStageMatchesFunction.get(i);
			stats.referenceTotalMatches += stats.referenceStageMatchesContent.get(i);
			stats.referenceTotalMatches += stats.referenceStageMatchesFunction.get(i);

			// Total for fragmentation/reporting
			stats.testWordMatches += alignment.moduleContentMatches1.get(i);
			stats.testWordMatches += alignment.moduleFunctionMatches1.get(i);
			stats.referenceWordMatches += alignment.moduleContentMatches2.get(i);
			stats.referenceWordMatches += alignment.moduleFunctionMatches2.get(i);
		}

		// New!

		for (int i = 0; i < stats.XtestStageMatches.size(); i++) {
			for (int j = 0; j < stats.XtestStageMatches.get(i).size(); j++) {
				stats.XtestTotalMatches += stats.XtestStageMatches.get(i).get(j);
			}
		}
		for (int i = 0; i < stats.XreferenceStageMatches.size(); i++) {
			for (int j = 0; j < stats.XreferenceStageMatches.get(i).size(); j++) {
				stats.XreferenceTotalMatches += stats.XreferenceStageMatches.get(i).get(j);
			}
		}

//		for (int i = 0; i < stats.XtestStageMatches.size(); i++) {
//			stats.testAdjadvs += alignment.moduleAdjAdvsMatches1.get(i);
//			stats.referenceAdjadvs += alignment.moduleAdjAdvsMatches2.get(i);
//			stats.testNouns += alignment.moduleNounsMatches1.get(i);
//			stats.referenceNouns += alignment.moduleNounsMatches2.get(i);
//			stats.testOther += alignment.moduleOtherMatches1.get(i);
//			stats.referenceOther += alignment.moduleOtherMatches2.get(i);
//			stats.testVerbs += alignment.moduleVerbsMatches1.get(i);
//			stats.testVerbs += alignment.moduleVerbsMatches2.get(i);
//		}

//		for (int j = 0; j < stats.XtestStageMatches_Adjadvs.size(); j++) {
//			stats.XtestTotalMatches += stats.XtestStageMatches_Adjadvs.get(j);
//		}
//		for (int j = 0; j < stats.XtestStageMatches_Nouns.size(); j++) {
//			stats.XtestTotalMatches += stats.XtestStageMatches_Nouns.get(j);
//		}
//		for (int j = 0; j < stats.XtestStageMatches_Other.size(); j++) {
//			stats.XtestTotalMatches += stats.XtestStageMatches_Other.get(j);
//		}
//		for (int j = 0; j < stats.XtestStageMatches_Verbs.size(); j++) {
//			stats.XtestTotalMatches += stats.XtestStageMatches_Verbs.get(j);
//		}

		// Meteor score is required to pick best reference
		computeMetrics(stats);

		// Keep underlying alignment
		stats.alignment = alignment;

		return stats;
	}

	/**
	 * Get the Meteor score given sufficient statistics
	 * 
	 * @param stats
	 */
	public void computeMetrics(MeteorStats stats) {

		stats.testWeightedMatches = 0;
		stats.referenceWeightedMatches = 0;

		stats.XprecisionNumerator = 0;
		stats.XrecallNumerator = 0;
		stats.XprecisionDenominator = 0;
		stats.XrecallDenominator = 0;

// (Original denominator calculations)
	// (Precision)
		stats.testWeightedLength = (delta * (stats.testLength - stats.testFunctionWords))
				+ ((1.0 - delta) * (stats.testFunctionWords));
	// (Recall)
		stats.referenceWeightedLength = (delta * (stats.referenceLength - stats.referenceFunctionWords))
				+ ((1.0 - delta) * (stats.referenceFunctionWords));

// New denominator calculations

	// Precision
		stats.XprecisionDenominator += (newDeltaWeights.get(0) * stats.testAdjadvs) +
				(newDeltaWeights.get(1) * stats.testNouns) + (newDeltaWeights.get(2) * stats.testOther) +
				(newDeltaWeights.get(3) * stats.testVerbs);
	// Recall
		stats.XrecallDenominator += (newDeltaWeights.get(0) * stats.referenceAdjadvs) +
				(newDeltaWeights.get(1) * stats.referenceNouns) + (newDeltaWeights.get(2) * stats.referenceOther) +
				(newDeltaWeights.get(3) * stats.referenceVerbs);

// Apply module weights and delta to test and reference matches
// (Content)
// (Original numerator calculations)

		for (int i = 0; i < moduleWeights.size(); i++)
			stats.testWeightedMatches += stats.testStageMatchesContent.get(i)
					* moduleWeights.get(i) * delta;

 		for (int i = 0; i < moduleWeights.size(); i++)
			stats.referenceWeightedMatches += stats.referenceStageMatchesContent
					.get(i) * moduleWeights.get(i) * delta;

		// Apply module weights and delta to test and reference matches
		// (Function)
		for (int i = 0; i < moduleWeights.size(); i++)
			stats.testWeightedMatches += stats.testStageMatchesFunction.get(i)
					* moduleWeights.get(i) * (1.0 - delta);

 		for (int i = 0; i < moduleWeights.size(); i++)
			stats.referenceWeightedMatches += stats.referenceStageMatchesFunction
					.get(i) * moduleWeights.get(i) * (1.0 - delta);

// New numerator calculations
	//Precision
		for (int i = 0; i < moduleWeights.size(); i++) {
			for (int j = 0; j < newDeltaWeights.size(); j++) {
				//System.out.println("moduleWeights size = " + moduleWeights.size());
				//System.out.println("newDeltaWeights size = " + newDeltaWeights.size());
				stats.XprecisionNumerator += newDeltaWeights.get(i) * stats.XtestStageMatches.get(i).get(j)
						* moduleWeights.get(j);
 			}
		}
	//Recall
		for (int i = 0; i < moduleWeights.size(); i++) {
			for (int j = 0; j < newDeltaWeights.size(); j++) {
				stats.XrecallNumerator += newDeltaWeights.get(i) * stats.XreferenceStageMatches.get(i).get(j)
						* moduleWeights.get(j);
			}
		}

		// Precision = test matches / test length
		stats.precision = stats.testWeightedMatches / stats.testWeightedLength;

		// New precision
		stats.Xprecision = stats.XprecisionNumerator / stats.XprecisionDenominator;

		// Recall = ref matches / ref length
		stats.recall = stats.referenceWeightedMatches / stats.referenceWeightedLength;

		// New recall
		stats.Xrecall = stats.XrecallNumerator / stats.XrecallDenominator;

		// F1 = 2pr / (p + r) [not part of final score]
		stats.f1 = (2 * stats.precision * stats.recall)
				/ (stats.precision + stats.recall);

		// Xf1
		stats.Xf1 = (2 * stats.Xprecision * stats.Xrecall)
				/ (stats.Xprecision + stats.Xrecall);

		// Fmean = 1 / alpha-weighted average of p and r
		stats.fMean = 1.0 / (((1.0 - alpha) / stats.precision) + (alpha / stats.recall));

		// XfMean
		stats.XfMean = 1.0 / (((1.0 - alpha) / stats.Xprecision + alpha / stats.Xrecall));

		// Fragmentation
		double frag;

		// Case if test = ref
		if (stats.testTotalMatches == stats.testLength
				&& stats.referenceTotalMatches == stats.referenceLength
				&& stats.chunks == 1)
			frag = 0;
		else
			frag = ((double) stats.chunks)
					/ (((double) (stats.testWordMatches + stats.referenceWordMatches)) / 2);

		// Fragmentation penalty
		stats.fragPenalty = gamma * Math.pow(frag, beta);

		// Score
		double score = stats.fMean * (1.0 - stats.fragPenalty);

		double Xscore = stats.XfMean * (1.0 - stats.fragPenalty);

		// Catch division by zero
		if (Double.isNaN(score))
			stats.score = 0;
		else
			// score >= 0.0
			stats.score = Math.max(score, 0.0);

		if (Double.isNaN(Xscore))
			stats.Xscore = 0;
		else
			// score >= 0.0
			stats.Xscore = Math.max(Xscore, 0.0);
	}
}
