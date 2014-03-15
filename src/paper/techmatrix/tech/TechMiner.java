package paper.techmatrix.tech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import paper.backend.Database;
import paper.nlp.phrase.PhraseGroup;
import paper.nlp.phrase.object.NounPhrase;
import paper.nlp.phrase.object.Phrase;
import paper.nlp.phrase.object.SubjPhrase;
import paper.nlp.phrase.object.VerbPhrase;
import paper.patent.Patent;
import paper.techmatrix.function.EffectMiner;
import paper.textprocess.Feature;
import paper.textprocess.TermExtractor;

public class TechMiner {

	public static void main(String[] args) {
		
		Database db =  Database.getInstance();
		
		Set<String> patentIDs = db.getPatentIDsByField("DDS", "tech");
		for(String patentID : patentIDs){
			Patent patent = db.setPatentProfile(patentID);
			//patent.showPhrasesByType("title");
			//patent.showPhrasesByType("ab");
			//patent.showPhrasesByType("claim");
			
			TechMiner tMiner = new TechMiner(patent);
			tMiner.showPhrases();
			tMiner.showTerms();
			//tMiner.getPureTechTerms();
		}
		
		/*
		Patent patent = db.setPatentProfile("5613140");
		TechMiner tMiner = new TechMiner(patent);
		tMiner.showPhrases();
		tMiner.showTerms();
		*/
	}
	
	//property
	private List<Phrase> techPhraseList;
	private Set<String> techTerms;
	private Map<String, Integer> termFreqMap; 

	public TechMiner(Patent patent){
		this.techPhraseList = new ArrayList<Phrase>();
		this.techTerms = new TreeSet<String>();
		this.termFreqMap = new HashMap<String, Integer>();
		this.getPhrasesFromPatent(patent);
	}
	
	//選欄位
	private void getPhrasesFromPatent(Patent patent){
		Map<String, PhraseGroup> phrasesMap = patent.getPhrasesMap();
		PhraseGroup phrasesObj = null;
		
		phrasesObj = phrasesMap.get("title");
		this.extractTermsFromPhrases(phrasesObj.getPhraseListByType("np"));
		this.extractTermsFromPhrases(phrasesObj.getVpListConsiderTransV(true));
		
		phrasesObj = phrasesMap.get("ab");
		this.extractTermsFromPhrases(phrasesObj.getVpListConsiderTransV(true));
		
		phrasesObj = phrasesMap.get("claim");
		this.extractTermsFromPhrases(phrasesObj.getPhraseListByType("np"));
		//this.extractTermsFromPhrases(phrasesObj.getPhraseListByType("sp"));
		this.extractTermsFromPhrases(phrasesObj.getVpListConsiderTransV(true));
	}
	
	//從片語抓terms
	private void extractTermsFromPhrases(List<? extends Phrase> phrases){
		List<String> termList = null;
		
		for(Phrase pObj : phrases){
			TermExtractor extractor = new TermExtractor();
			
			if(pObj instanceof NounPhrase){
				termList = ((NounPhrase)pObj).getTermList("amod", true);
			}else if(pObj instanceof VerbPhrase){
				termList = ((VerbPhrase)pObj).getTermList("amod", true, false);
			}else if(pObj instanceof SubjPhrase){
				termList = ((SubjPhrase)pObj).getTermList("amod", true);
			}else{
				continue;
			}
			
			extractor.processTermsSplitDash(termList);
			Map<String, Integer> termMap = extractor.getTermMap();
			this.addTermMap(termMap);
			this.techTerms.addAll(termMap.keySet());
			this.techPhraseList.add(pObj);
		}
	}
	
	public void addTermMap(Map<String, Integer> termMap){
		for(String term : termMap.keySet()){
			int freq = termMap.get(term);
			if(this.termFreqMap.keySet().contains(term)){
				int lastFreq = this.termFreqMap.get(term);
				this.termFreqMap.put(term, lastFreq + freq);
			}else{
				this.termFreqMap.put(term, freq);
			}
		}
	}
	
	public Set<String> getPureTechTerms(){
		//System.out.println("Pure Tech Terms:");
		Set<String> pureTechTerms = new HashSet<String>();;
		
		for(String term : this.techTerms){
			double score = EffectMiner.getEffectScore(term, "");
			if(score == 0){
				pureTechTerms.add(term);
				//System.out.println(term);
			}
		}
		return pureTechTerms;
	}
	
	public void showTerms(){
		System.out.println("Tech term:");
		List<Feature> featList = new ArrayList<Feature>();
		for(String term : this.termFreqMap.keySet()){
			Feature feature = new Feature(term, this.termFreqMap.get(term));
			featList.add(feature);
		}
		Feature.sortFeatures(featList, true);
		
		for(Feature feat : featList){
			feat.show();
		}
	}
	
	public void showPhrases(){
		System.out.println("Tech Phrase:");
		for(Phrase phrase : this.techPhraseList){
			System.out.println(phrase.getPhrase("amod", true));
		}
		System.out.println();
	}
	
	public Set<String> getFeats(){
		return this.techTerms;
	}
}

