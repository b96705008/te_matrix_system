package paper.techmatrix.function;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import paper.Config;
import paper.backend.Database;
import paper.nlp.phrase.PhraseGroup;
import paper.nlp.phrase.object.NounPhrase;
import paper.nlp.phrase.object.VerbPhrase;
import paper.patent.Patent;
import paper.textprocess.TermExtractor;

public class FunctionMiner {

	public static void main(String[] args) {
		Database db =  Database.getInstance();
		Patent patent = db.setPatentProfile("5703896");
		patent.showPhrasesByMap();
		FunctionMiner fMiner = new FunctionMiner(patent);
		//fMiner.mineFuncFromfW(5);
		//fMiner.mineFuncFromTerms(5);
	}
	
	public static Set<String> funcSeeds = new HashSet<String>();
	static{loadFuncSeeds();}
	
	public static void loadFuncSeeds(){
		BufferedReader br;
		try {
			String funcSeed = null;
			br = new BufferedReader(new FileReader(Config.funcWordsFile));
			while((funcSeed = br.readLine()) != null){
				if(funcSeed != "") funcSeeds.add(funcSeed);				
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean compareVerbs(VerbPhrase vp, Set<String> otherVerbs){
		if(otherVerbs.contains(vp.getVerbWithPrt()))
			return true;
		else
			return false;
	}
	
	public static String getEntityString(VerbPhrase vp){
		return vp.getNpObj().getMainTerm();
	}
	
	//property
	private List<VerbPhrase> vpList;
	private List<NounPhrase> npList;
	
	//constructor
	public FunctionMiner(Patent patent){
		this.vpList = new ArrayList<VerbPhrase>();
		this.npList = new ArrayList<NounPhrase>();
		this.extractPhraseFromPatent(patent);
	}
	
	//method
	private void extractPhraseFromPatent(Patent patent){
		Map<String, PhraseGroup> phrasesMap = patent.getPhrasesMap();
		for(String pField : phrasesMap.keySet()){
			PhraseGroup phrasesObj = phrasesMap.get(pField);
			this.vpList.addAll((List<VerbPhrase>) phrasesObj.getPhraseListByType("vp"));
			this.npList.addAll((List<NounPhrase>) phrasesObj.getPhraseListByType("np"));
		}
	}
	
	
	
	private Set<String> mineEntitiesFromVerb(Set<String> funcWords){
		Set<String> entities = new HashSet<String>();
		
		for(VerbPhrase vp : this.vpList){
			if(compareVerbs(vp, funcWords)){
				entities.add(getEntityString(vp));
			}
		}
		return entities;
	}
	
	private Set<String> mineVerbFromEntities(Set<String> entities){
		Set<String> funcWords = new HashSet<String>();
		
		for(VerbPhrase vp : this.vpList){
			if(entities.contains(getEntityString(vp))){
				funcWords.add(vp.getVerbWithPrt());	
			}
		}		
		return funcWords;
	}
	
	//===use function words as seed===
	public void mineFuncFromfW(int round){
		Set<String> funcWords = FunctionMiner.funcSeeds;
		Set<String> entities = null;
		int lastSize = 0;
		
		for(int i=0; i<round; i++){
			entities = this.mineEntitiesFromVerb(funcWords);
			funcWords = this.mineVerbFromEntities(entities);
			System.out.println("round " + (i+1));
			System.out.println(entities.toString());
			System.out.println(funcWords.toString());
			this.showByFW(funcWords);
			
			int curSize = funcWords.size();
			if(curSize == lastSize) break;
			lastSize = curSize;
		}
	}
	
	public void showByFW(Set<String> funcWords){
		System.out.println("selected VP: ");
		for(VerbPhrase vp : this.vpList){
			if(compareVerbs(vp, funcWords)){
				vp.show();
			}
		}
	}
	
	//===use main term as seed===
	public Set<String> getEntitySeeds(){
		Set<String> entities = new HashSet<String>();
		entities.add("sensitivity");
		return entities;
	}
	
	public void mineFuncFromTerms(int round){
		Set<String> entities = this.getEntitySeeds();
		Set<String> funcWords = null;
		int lastSize = 0;
		for(int i=0; i<round; i++){
			funcWords = this.mineVerbFromEntities(entities);
			entities = this.mineEntitiesFromVerb(funcWords);
			
			System.out.println("round " + (i+1));
			System.out.println(funcWords.toString());
			System.out.println(entities.toString());
	
			this.showByEntity(entities);
			
			int curSize = entities.size();
			if(curSize == lastSize) break;
			lastSize = curSize;
		}
	}
	
	public void showByEntity(Set<String> entities){
		System.out.println("selected VP: ");
		for(VerbPhrase vp : this.vpList){
			if(entities.contains(getEntityString(vp))){
				vp.show();
			}
		}
	}
	
	public void show(){
		System.out.println("All VP: ");
		for(VerbPhrase vp : this.vpList){
			vp.show();
		}
	}
}
