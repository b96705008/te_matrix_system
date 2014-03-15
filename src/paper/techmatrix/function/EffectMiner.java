package paper.techmatrix.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.stanford.nlp.ling.TaggedWord;

import paper.backend.Database;
import paper.nlp.NLPTool;
import paper.nlp.phrase.PhraseGroup;
import paper.nlp.phrase.object.NounPhrase;
import paper.nlp.phrase.object.Phrase;
import paper.nlp.phrase.object.PrepPhrase;
import paper.nlp.phrase.object.SubjPhrase;
import paper.nlp.phrase.object.VerbPhrase;
import paper.patent.Patent;
import paper.patent.PatentGroup;
import paper.techmatrix.tech.TechMiner;
import paper.textprocess.Feature;
import paper.textprocess.TermExtractor;

public class EffectMiner {

	public static void main(String[] args) {
		
		Database db =  Database.getInstance();
		
		Set<String> patentIDs = db.getPatentIDsByField("DDS", "effect");
		for(String patentID : patentIDs){
			Patent patent = db.setPatentProfile(patentID);
			
			//System.out.println("===tech===");
			//TechMiner tMiner = new TechMiner(patent);
			//tMiner.showPhrases();
			//tMiner.showTerms();
			//Set<String> pureTechTerms = tMiner.getPureTechTerms();
			
			System.out.println("===effect===");
			EffectMiner eMiner = new EffectMiner(patent);
			eMiner.countScore();
			eMiner.showPhraseScore();
			eMiner.showTermScore();
			
			/*
			System.out.println("===fiter effect===");
			Set<String> selectedFeats = eMiner.getFeats(pureTechTerms);
			for(String feat : selectedFeats){
				System.out.println(feat);
			}
			*/
		}
		
		/*
		Patent patent = db.setPatentProfile("6236060");
		EffectMiner eMiner = new EffectMiner(patent);
		eMiner.countScore();
		eMiner.showPhraseScore();
		eMiner.showTermScore();
		eMiner.getFeats(10);
		*/
	}
	
	public static double getEffectScore(String rawTerm, String tag){
		Database db = Database.getInstance();
		double score = 0;
		
		if(rawTerm.contains("-")){
			List<TaggedWord> tagWordsList = NLPTool.getTagWordList(rawTerm.replace("-", " "));
			for(TaggedWord tWord : tagWordsList){
				if(tWord.tag().startsWith("NN"))
					score += getEffectScore(tWord.value(), "Noun");
				else if(tWord.tag().startsWith("JJ"))
					score += getEffectScore(tWord.value(), "Modif");
				else if(tWord.tag().startsWith("VB"))
					score += getEffectScore(tWord.value(), "SUPV");
		    }	        
		}else{
			score += db.queryEffectTerms(rawTerm, tag);
		}
		return score;
	}
	
	public static double getEffectScore(NounPhrase npObj){
		double score = 0;
		String mainNoun = npObj.getMainTerm();
		List<String> nns = npObj.getModifierTerms("nn");
		List<String> modifs = npObj.getModifierTerms("amod");
		List<PrepPhrase> ppList = npObj.getPPList();
		
		//mainNoun
		score += getEffectScore(mainNoun, "Noun");
		//nn
		if(nns != null){
			for(String nn : nns) score += getEffectScore(nn, "Noun");
		}
		//amod
		if(modifs != null){
			for(String modif : modifs) score += getEffectScore(modif, "Modif");
		}
		//pp
		if(ppList.size() > 0){
			for(PrepPhrase pp : ppList)
				score += getEffectScore(pp.getNpObj());
		}
		//System.out.println(score);
		return score;
	}
	
	public static double getEffectScore(VerbPhrase vpObj){
		double score = 0;
		String verb = vpObj.getVerb();
		double verbScore = 
				Math.log(getEffectScore(verb, "SUPV") + 1) / Math.log(2);
				
		score += verbScore;
		score += getEffectScore(vpObj.getNpObj());
		//System.out.println(score);
		return score;
	}
	
	
	
	private class EffectPhrase{
		private Phrase phraseObj;
		private double score;
		
		public EffectPhrase(Phrase phraseObj, double score){
			this.phraseObj = phraseObj;
			this.score = score;
		}	
		
		public Phrase getPhraseObj() {return phraseObj;}
		
		public Double getScore() {return score;}
		
		public List<String> getTermList(){
			if(phraseObj instanceof NounPhrase)
				return ((NounPhrase)this.phraseObj).getTermList("amod", true);
			else //vp
				return ((VerbPhrase)this.phraseObj).getTermList("amod", true, false);
		}
		
		public void show(){
			System.out.println(phraseObj.getPhrase()+" "+score);
		}
	}
	
	public static void sortEPhrases(List<EffectPhrase> ePhrases){
		//sort
		Collections.sort(ePhrases, new Comparator<EffectPhrase>(){
			@Override
			public int compare(EffectPhrase f0, EffectPhrase f1) {
				int flag = f0.getScore().compareTo(f1.getScore());
				return (flag * -1);
			}		
		});
	}
	
	//property
	private Patent patent;
	private List<EffectPhrase> ePhrases;
	private Map<String, Double> termScoreMap;
	private Map<String, Integer> termFreqMap; 
	
	//constructor
	public EffectMiner(Patent patent){
		this.patent = patent;
		this.ePhrases = new ArrayList<EffectPhrase>();
		this.termScoreMap = new HashMap<String, Double>();
		this.termFreqMap = new HashMap<String, Integer>();
		this.getPhrasesFromPatent(patent);
	}
	
	private void getPhrasesFromPatent(Patent patent){
		Map<String, PhraseGroup> phrasesMap = patent.getPhrasesMap();
		PhraseGroup phrasesObj = null;
		
		phrasesObj = phrasesMap.get("adv");
		this.getEffectScoreFromPhrase(phrasesObj.getPhraseListByType("np"), 1);
		this.getEffectScoreFromPhrase(phrasesObj.getPhraseListByType("vp"), 1);
		
		phrasesObj = phrasesMap.get("ab");
		//this.getEffectScoreFromPhrase(phrasesObj.getPhraseListByType("np"), 0);
		this.getEffectScoreFromPhrase(phrasesObj.getVpListConsiderTransV(false), 0);
	}
	
	private void getEffectScoreFromPhrase(List<? extends Phrase> phrases, double initScore){
		for(Phrase pObj : phrases){
			double score = 0;
			
			if(pObj instanceof NounPhrase){
				score = getEffectScore((NounPhrase)pObj) + initScore;
			}else if(pObj instanceof VerbPhrase){
				score = getEffectScore((VerbPhrase)pObj) + initScore;
			}else{
				continue;
			}
			
			EffectPhrase ePhrase = new EffectPhrase(pObj, score);
			this.ePhrases.add(ePhrase);
		}

	}
	
	private void addTermScore(String term, double score){		
		if(this.termScoreMap.containsKey(term)){
			double lastScore = this.termScoreMap.get(term);
			this.termScoreMap.put(term, lastScore + score);
		}else{
			this.termScoreMap.put(term, score);
		}
	}
	
	private void addTermFreq(String term, int freq){		
		if(this.termFreqMap.containsKey(term)){
			int lastFreq = this.termFreqMap.get(term);
			this.termFreqMap.put(term, lastFreq + freq);
		}else{
			this.termFreqMap.put(term, freq);
		}
	}
	
	public Map<String, Double> countScore(){
		for(EffectPhrase ePhrase : this.ePhrases){
			double score = ePhrase.getScore();
			List<String> termList = ePhrase.getTermList();
			
			TermExtractor extractor = new TermExtractor();
			extractor.processTermsSplitDash(termList);
			Map<String, Integer> termMap = extractor.getTermMap();
			for(String term : termMap.keySet()){
				int freq = termMap.get(term);
				this.addTermScore(term, score * freq);
				this.addTermFreq(term, freq);
			}	
		}
		
		for(String term : this.termScoreMap.keySet()){
			double lastScore = this.termScoreMap.get(term);
			int freq = this.termFreqMap.get(term);
			//double freq = this.patent.getTF(term);
			this.termScoreMap.put(term, lastScore/freq);
		}
		
		return this.termScoreMap;
	}
	
	public void showPhraseScore(){
		System.out.println("=======phrase score=======");
		sortEPhrases(this.ePhrases);
		for(EffectPhrase ePhrase : this.ePhrases){
			ePhrase.show();
		}
	}
	
	public void showTermScore(){
		System.out.println("=======term score=======");
		List<Feature> featList = new ArrayList<Feature>();
		for(String term : this.termScoreMap.keySet()){
			Feature feature = new Feature(term, this.termScoreMap.get(term));
			featList.add(feature);
		}
		Feature.sortFeatures(featList, true);
		
		for(Feature feat : featList){
			feat.show();
		}
	}

	public Set<String> getFeats(){
		Set<String> selectedFeats = new TreeSet<String>();
		double threshold = 1.5;
		if(threshold < 0) threshold = 1;
		
		for(String term : this.termScoreMap.keySet()){
			double score = this.termScoreMap.get(term);
			if(score >= threshold){
				selectedFeats.add(term);
			}
		}
		if(selectedFeats.size() != 0)
			return selectedFeats;
		else
			return getFeats(3);
	}
	
	public Set<String> getFeats(int rank){
		List<Feature> featureList = new ArrayList<Feature>();
		
		for(String term : this.termScoreMap.keySet()){
			double score = this.termScoreMap.get(term);
			if(score == 0) continue;
			featureList.add(new Feature(term, score));
		}
		
		Set<String> selectedFeats = Feature.selectByRank(featureList, rank);
		return selectedFeats;
	}
	
	public Set<String> getFeats(Set<String> pureTechTerms){
		Set<String> selectedFeats = this.getFeats();
		selectedFeats.removeAll(pureTechTerms);
		return selectedFeats;
	}
}
