package paper.textprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import edu.stanford.nlp.ling.HasWord;

import paper.Config;
import paper.nlp.NLPTool;


public class TermExtractor {

	public static void main(String[] args) throws Exception {
	}
	
	static Set<String> stopWords = new HashSet<String>();
	static{
		try {
			TermExtractor.loadStopWords(Config.stopListFile);
			TermExtractor.loadStopWords(Config.patentStopListFile);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	static public void loadStopWords(String path) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String stopWord = null;
		while((stopWord = br.readLine()) != null){
			stopWord = stopWord.trim();
			if(stopWord != ""){
				TermExtractor.stopWords.add(stopWord);
			}				
		}
		br.close();	
	}
	
	public static List<String> splitDash(String orginalTerm){
		List<String> termList = new ArrayList<String>();
		orginalTerm = orginalTerm.trim();	
		termList.add(orginalTerm.replace("-", ""));
		
		if(orginalTerm.contains("-")){	
			String[] terms = orginalTerm.split("-");
			for(String term : terms){
				termList.add(term);
			}
		}
		return termList;
	}
	
	//property
	private Map<String, Integer> termMap;
	
	public TermExtractor(){
		termMap = new HashMap<String, Integer>();
	}	
	
	public Map<String, Integer> getTermMap(){		
		return this.termMap;
	}
	
	private void addTerm(String term){
		if(this.termMap.containsKey(term)){
			int freq = this.termMap.get(term);
			this.termMap.put(term, freq+1);
		}else{
			this.termMap.put(term, 1);
		}
	}
	
	private static String getStemWord(String str){
		char[] term = str.toCharArray(); 
		Stemmer stemmer = new Stemmer();
		stemmer.add(term, term.length);
		stemmer.stem();
		return stemmer.toString();		
	}
	
	//=====process term=====
	
	public static String getRefinedTerm(String rawTerm){
		//normalize
		String normalizedTerm = rawTerm.replaceAll("[\\W&&[^\\'-]]", ""); //get rid of some Punctuation except "-", "'"		
		normalizedTerm = normalizedTerm.toLowerCase(); //Lowercasing
		if(stopWords.contains(normalizedTerm)) return null; //Stopword removal
		
		//filter after lookup stopList
		normalizedTerm = normalizedTerm.replace("'", "");
		if(normalizedTerm.equals("") || normalizedTerm.matches("[\\W]+")) return null;
		
		//String refinedTerm = getStemWord(normalizedTerm); //stemming
		//String refinedTerm = NLPTool.lemmatizeTerm(normalizedTerm); //lemmatize
		String refinedTerm = normalizedTerm;
		return refinedTerm;
	}
	
	public void processTerm(String rawTerm){
		String refinedTerm = TermExtractor.getRefinedTerm(rawTerm);
		if(refinedTerm != null)
			this.addTerm(refinedTerm);
	}
	
	public void processTermsByStr(String line){
		String[] rawTermArr = line.split(" "); //Tokenization
		for(String rawTerm:rawTermArr){
			this.processTerm(rawTerm); //extract term
		}
	}
	
	public void processTermsByList(List<String> termList){	
		for(String rawTerm : termList){
			this.processTerm(rawTerm); //extract term
		}
	}
	
	public void processTermsBySents(List<List<HasWord>> sentences){
		for(List<HasWord> sentence : sentences){
			for(HasWord word : sentence){
				List<String> termList = splitDash(word.toString());
				this.processTermsByList(termList);
			}
		}
	}
	
	public void processTermsSplitDash(List<String> rawTermList){	
		for(String rawTerm : rawTermList){
			List<String> termList = splitDash(rawTerm);
			this.processTermsByList(termList);
		}
	}
	
	public void showTerms(){
		Iterator<String> itr = this.termMap.keySet().iterator();
		while(itr.hasNext()){
			String term = itr.next();
			int freq = this.termMap.get(term);
			System.out.println(term+" "+freq);
		}
		System.out.println("total: "+this.termMap.size());
	}
	
	public void clear(){
		this.termMap.clear();
	}
}
