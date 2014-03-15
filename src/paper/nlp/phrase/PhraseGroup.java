package paper.nlp.phrase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import paper.nlp.Rules;
import paper.nlp.phrase.object.NounPhrase;
import paper.nlp.phrase.object.Phrase;
import paper.nlp.phrase.object.SubjPhrase;
import paper.nlp.phrase.object.VerbPhrase;

public class PhraseGroup implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		
	}
	
	private List<NounPhrase> npList;
	private List<SubjPhrase> spList;
	private List<VerbPhrase> vpList;
	
	public PhraseGroup(){
		this.npList = new ArrayList<NounPhrase>();//big cat
		this.spList = new ArrayList<SubjPhrase>();
		this.vpList = new ArrayList<VerbPhrase>();//comprise A
	}
	
	//=====get=====
	public List<? extends Phrase> getPhraseListByType(String type){
		
		if(type.equals("np"))
			return this.npList;
		else if(type.equals("sp"))
			return this.spList;
		else if(type.equals("vp"))
			return this.vpList;
		else
			return null;
	}
	
	public List<String> getAllTermList(){
		List<String> termList = new ArrayList<String>();
		termList.addAll(this.getTermListByType("np"));
		termList.addAll(this.getTermListByType("vp"));
		
		return termList;
	}
	
	public List<String> getTermListByType(String type){
		List<String> termList = new ArrayList<String>();
		List<? extends Phrase> phraseList = this.getPhraseListByType(type);
		
		for(Phrase phrase : phraseList){
			termList.addAll(phrase.getTermList());
		}
		return termList;
	}
	
	//process specific verb
	public List<String> getTermListByFilterVerb(boolean isContain){
		List<String> termList = new ArrayList<String>();
		
		for(VerbPhrase vp : this.vpList){
			String verb = vp.getVerb();
			if(isContain){
				if(!Rules.isTransitionVerb(verb)) 
					continue;
			}else{
				if(Rules.isTransitionVerb(verb)) 
					continue;
			}
			termList.addAll(vp.getTermListNoVerb());
		}
		return termList;
	}
	
	public List<VerbPhrase> getVpListConsiderTransV(boolean isContain){
		List<VerbPhrase> vpList = new ArrayList<VerbPhrase>();
		
		for(VerbPhrase vp : this.vpList){
			String verb = vp.getVerb();
			if(isContain){
				if(!Rules.isTransitionVerb(verb)) 
					continue;
			}else{
				if(Rules.isTransitionVerb(verb)) 
					continue;
			}
			vpList.add(vp);
		}
		return vpList;
	}
	
	public List<SubjPhrase> getSpListConsiderTransV(boolean isContain){
		List<SubjPhrase> spList = new ArrayList<SubjPhrase>();
		
		for(SubjPhrase sp : this.spList){
			String verb = sp.getVerb();
			if(isContain){
				if(!Rules.isTransitionVerb(verb)) 
					continue;
			}else{
				if(Rules.isTransitionVerb(verb)) 
					continue;
			}
			spList.add(sp);
		}
		return spList;
	}
	
	//=====add=====
	public void addPhrase(String type, Phrase phrase){
		if(type.equals("np"))
			this.npList.add((NounPhrase)phrase);
		else if(type.equals("sp"))
			this.spList.add((SubjPhrase)phrase);
		else if(type.equals("vp"))
			this.vpList.add((VerbPhrase)phrase);
	}
	
	public void addPhrases(String type, Collection<? extends Phrase> phrases){
		for(Phrase phrase : phrases){
			this.addPhrase(type, phrase);
		}
	}
	
	public void addPhraseGroup(PhraseGroup phrasesObj){
		this.addPhrases("np", phrasesObj.getPhraseListByType("np"));
		this.addPhrases("sp", phrasesObj.getPhraseListByType("sp"));
		this.addPhrases("vp", phrasesObj.getPhraseListByType("vp"));
	}
	
	//====show====
	//show phrases by type
	public void showPhrases(String type){
		System.out.println(type + " phrase:");
		
		List<? extends Phrase> phraseList = this.getPhraseListByType(type);
		for(Phrase phrase : phraseList){
			phrase.show();
		}
		System.out.println();
	}
	
	public void showPhraseConsiderTransV(){
		System.out.println("Transition Term: ");
		List<SubjPhrase> spList = this.getSpListConsiderTransV(true);
		List<VerbPhrase> vpList = this.getVpListConsiderTransV(true);
		
		for(SubjPhrase sp : spList){
			sp.show();
		}
		
		for(VerbPhrase vp : vpList){
			vp.show();
		}
	}
	
	//show all phrases
	public void showAllPhrases(){
		this.showPhrases("np");
		this.showPhrases("sp");
		this.showPhrases("vp");
		this.showPhraseConsiderTransV();
		System.out.println();
	}
}
