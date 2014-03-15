package paper.techmatrix.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import paper.backend.Database;
import paper.nlp.Rules;
import paper.nlp.phrase.PhraseGroup;
import paper.nlp.phrase.object.NounPhrase;
import paper.nlp.phrase.object.Phrase;
import paper.nlp.phrase.object.PrepPhrase;
import paper.nlp.phrase.object.VerbPhrase;
import paper.patent.Patent;

public class UseMiner {

	public static void main(String[] args) {
		/*
		NounPhrase npObj = new NounPhrase("apple");
		VerbPhrase vpObj = new VerbPhrase("use", npObj);
		System.out.println(isUsePhrase(npObj));
		System.out.println(isUsePhrase(vpObj));
		*/
		Database db =  Database.getInstance();
		Set<String> patentIDs = db.getPatentIDsByField("DDS", "use");
		for(String patentID : patentIDs){
			Patent patent = db.setPatentProfile(patentID);
			UseMiner uMiner = new UseMiner(patent);
			uMiner.showResult();
		}
	}
	
	
	public static boolean isUsePhrase(NounPhrase npObj){
		if(Rules.isUseNoun(npObj.getMainTerm()))
			return true;
		
		List<PrepPhrase> ppList = npObj.getPPList();
		if(ppList != null){
			for(PrepPhrase pp : ppList){
				if(Rules.isUseNoun(pp.getNpObj().getMainTerm()))
					return true;
			}
		}
		
		return false;
	}
	
	public static boolean isUsePhrase(VerbPhrase vpObj){
		String verb = vpObj.getVerb();
		if(Rules.isUseVerb(verb)) 
			return true;
		else
			return isUsePhrase(vpObj.getNpObj());
	}
	
	//property
	private List<Phrase> usePhraseList;
	
	//constructor
	public UseMiner(Patent patent){
		this.usePhraseList = new ArrayList<Phrase>();
		this.getPhrasesFromPatent(patent);
	}
	
	//method
	
	private void getPhrasesFromPatent(Patent patent){
		Map<String, PhraseGroup> phrasesMap = patent.getPhrasesMap();
		for(String pField : phrasesMap.keySet()){
			//patent.showPhrasesByType(pField);
			PhraseGroup phrasesObj = phrasesMap.get(pField);
			
			if(pField.equals("use")){
				this.usePhraseList.addAll(phrasesObj.getPhraseListByType("np"));
				this.usePhraseList.addAll(phrasesObj.getVpListConsiderTransV(false));	
			}else if(pField.equals("ab")){
				this.findUseFromNPs((List<NounPhrase>) phrasesObj.getPhraseListByType("np"));
				this.findUseFromVPs(phrasesObj.getVpListConsiderTransV(false));
			}
		}
	}
	
	private void findUseFromNPs(List<NounPhrase> npList){
		for(NounPhrase npObj : npList){
			if(isUsePhrase(npObj))
				this.usePhraseList.add(npObj);
		}
	}
	
	private void findUseFromVPs(List<VerbPhrase> vpList){
		for(VerbPhrase vpObj : vpList){
			if(isUsePhrase(vpObj))
				this.usePhraseList.add(vpObj);
		}
	}
	
	public void showResult(){
		for(Phrase phrase : this.usePhraseList){
			phrase.show();
		}
	}
}
