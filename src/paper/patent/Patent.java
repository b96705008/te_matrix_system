package paper.patent;

import java.io.File;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.ling.HasWord;

import paper.Config;
import paper.backend.Database;
import paper.backend.ObjectIO;
import paper.nlp.NLPTool;
import paper.nlp.phrase.PhraseGroup;
import paper.patent.IPC;
import paper.techmatrix.function.EffectMiner;
import paper.techmatrix.tech.TechMiner;
import paper.textprocess.TermExtractor;

public class Patent extends Document{
	
	public static void main(String[] args) {
		InvertedIndex invertIndex = new InvertedIndex();
		Patent p = new Patent("test");
		invertIndex.addDocument(p);
	}
	
	//property
	private Map<String, PhraseGroup> phrasesMap; 
	private IPC ipc; 
	private Map<String, Set<String>> featureMap; //(tech, effect)-extract terms 對照表
	private Map<String, Double> effectTermWeight;
	private Set<String> pureTechTerms;
	
	//constructor
	public Patent(String patentID) {
		super(patentID);
		this.phrasesMap = new LinkedHashMap<String, PhraseGroup>();
		this.featureMap = new HashMap<String, Set<String>>();
		System.out.println("==="+patentID+"===");
	}
	
	//method
	
	//============Add Content==============	
	private Set<String> addSentsToDoc(List<List<HasWord>> fullText){
		//extract terms from sents
		List<String> lemmaTerms = NLPTool.lemmatizeSents(fullText);
		TermExtractor extractor = new TermExtractor();
		extractor.processTermsSplitDash(lemmaTerms);
		Map<String, Integer> termMap = extractor.getTermMap();
		this.addTermFromMap(termMap);
		
		return termMap.keySet();
	}
	
	private void addPhrasesToDoc(PhraseGroup phrases, Set<String> fullTextTerms){
		List<String> phraseTermList = phrases.getAllTermList();
		TermExtractor extractor = new TermExtractor();
		extractor.processTermsSplitDash(phraseTermList);
		Map<String, Integer> termMap = extractor.getTermMap();
		
		for(String term : termMap.keySet()){
			if(fullTextTerms.contains(term)) continue;
			int freq = termMap.get(term);
			this.addTerm(term, freq);
		}
	}
	
	//如果data/phrases/[文件ID]/ 裡沒有該欄位的PhrasesObj, 則預存
	public PhraseGroup generatePhrasesObj(List<List<HasWord>> fullText, String pField){
		PhraseGroup phrases = null;
		String patentPhraseFile = Config.phrasesDir + this.docID + "/" + pField;
		
		if(new File(patentPhraseFile).exists()){
			phrases = ObjectIO.readPatentPhrases(this.docID, pField);
			if(phrases == null){
				phrases = NLPTool.getPhrasesFromsSents(fullText);
				ObjectIO.writePatentPhrases(this.docID, pField, phrases);
			}
		}else{
			phrases = NLPTool.getPhrasesFromsSents(fullText);
			ObjectIO.writePatentPhrases(this.docID, pField, phrases);
		}
		return phrases;
	}
	
	public void addPatentContent(String pField, String contentStr){
		List<List<HasWord>> fullText = NLPTool.getSentences(contentStr);//rawString -> sentences (parser)
		PhraseGroup phrases = this.generatePhrasesObj(fullText, pField);
		//PhraseGroup phrases = NLPTool.getPhrasesFromsSents(fullText);
		this.phrasesMap.put(pField, phrases); //set phrase
		
		Set<String> fullTextTerms = this.addSentsToDoc(fullText);
		this.addPhrasesToDoc(phrases, fullTextTerms);
	}
	
	//============Phrases==============	
	public Map<String, PhraseGroup> getPhrasesMap(){
		return this.phrasesMap;
	}
	
	//type: title, novelty, use, adv
	public PhraseGroup getPhrasesByType(String pField){
		return this.phrasesMap.get(pField);
	}
	
	public void showPhrasesByType(String pField){
		PhraseGroup phrases = this.getPhrasesByType(pField);
		if(phrases == null) return;
		System.out.println("======="+pField+"=======");
		phrases.showAllPhrases();
	}
	
	public void showPhrasesByMap(){
		for(String pField : this.phrasesMap.keySet()){
			this.showPhrasesByType(pField);
		}
	}
	
	//=====tech, effect, use====
	public void processTFfeats(){
		//tech
		TechMiner tMiner = new TechMiner(this);
		Set<String> pureTechTerms = tMiner.getPureTechTerms();
		this.featureMap.put("tech", tMiner.getFeats());
		
		//effect
		EffectMiner eMiner = new EffectMiner(this);
		this.effectTermWeight = eMiner.countScore();
		this.featureMap.put("effect", eMiner.getFeats(pureTechTerms));
	}
	
	public void processTech(){
		TechMiner tMiner = new TechMiner(this);
		Set<String> techFeats = tMiner.getFeats();
		this.pureTechTerms = tMiner.getPureTechTerms();
		this.featureMap.put("tech", techFeats);
	}
	
	public void processEffect(){
		EffectMiner eMiner = new EffectMiner(this);
		this.effectTermWeight = eMiner.countScore();
		Set<String> effectFeats = eMiner.getFeats(); //**
		this.featureMap.put("effect", effectFeats);
	}
	
	public Set<String> getPureTechTerms(){
		return this.pureTechTerms;
	}
	
	//============process Feature relate=====================
	public Set<String> getFeature(String dimeName){
		return this.featureMap.get(dimeName);
	}
	
	
	private void extractFeatFromIPC(TermExtractor extractor){
		Database db = Database.getInstance();
		try{
			this.ipc = new IPC(db.getIpcSetByPatentID(this.getID())); 
			Set<String> parseIpcSet = this.ipc.getParseIpcSet("12345");
			String titleStr = db.getTitleByIpcSet(parseIpcSet);
			List<String> nounList = NLPTool.getPOSWordsFromSents(titleStr, "NN");
			extractor.processTermsSplitDash(nounList);
			
			//add content
			this.addTermFromMap(extractor.getTermMap());
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	//get special vector
	public double[] getEffectVector(String vecType, InvertedIndex inverted, Set<String> featureSet){
		double[] vector = new double[featureSet.size()];
		int index = 0;
		
		for(String term : featureSet){
			double value = this.getTermWeight(term, vecType, inverted);;
			if(this.effectTermWeight.containsKey(term))
				value *= (this.effectTermWeight.get(term)+1);
			vector[index] = value;
			index++;
		}
		return vector;
	}
}
