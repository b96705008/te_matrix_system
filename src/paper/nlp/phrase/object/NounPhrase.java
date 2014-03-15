package paper.nlp.phrase.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import paper.backend.Database;
import paper.nlp.NLPTool;
import paper.nlp.Rules;

import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

public class NounPhrase extends Phrase{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args){
	}
	
	//property
	private String mainTerm; //主要名詞
	private Map<String, List<String>> modifiersMap; //key: nn,amod,advmod, value:修飾字list (有順序性) 
	private List<PrepPhrase> ppList;//相連在後的介係詞片語list
	
	//constructor
	private NounPhrase(){
		this.modifiersMap = new HashMap<String, List<String>>();
		this.ppList = new ArrayList<PrepPhrase>();
	}
	
	public NounPhrase(String mainTerm){
		this();
		this.mainTerm = NLPTool.lemmatizeTermByTag(mainTerm, "NN");
	}
	
	public NounPhrase(String mainTerm, String tag){
		this();
		this.mainTerm = NLPTool.lemmatizeTermByTag(mainTerm, tag);
	}
	
	//method
	
	public String getMainTerm(){
		return this.mainTerm;
	}
	
	@Override
	public String getPhrase(){
		return this.getPhrase("amod", true);
	}
	
	//upperReln: 組出的片語層級到(nn, amod..advmod),實驗是組到形容詞(amod)
	//considerPP: 是否組合介係詞片語
	@Override
	public String getPhrase(String upperReln, boolean considerPP){
		String nounPhrase = this.mainTerm;
		
		List<String> subModifRels = Rules.getSubModifRels(upperReln);
		for(String modifierReln : subModifRels){
			if(!this.modifiersMap.containsKey(modifierReln)) continue;
			List<String> modifierList = this.modifiersMap.get(modifierReln);
			for(String modifier : modifierList){
				nounPhrase = modifier + " " + nounPhrase;
			}
		}
		
		//process PP
		if(considerPP && this.ppList.size() > 0){
			for(PrepPhrase pp : this.ppList){
				nounPhrase = nounPhrase + " " 
						+ pp.getPhrase(upperReln, considerPP);
			}
		}
		
		return nounPhrase;
	}
	
	public List<String> getModifierTerms(String modifierReln){
		if(this.modifiersMap.containsKey(modifierReln)){
			return this.modifiersMap.get(modifierReln);
		}else{
			return null;
		}
	}
	
	public List<PrepPhrase> getPPList(){
		return this.ppList;
	}
	
	public void putModifiersMap(String reln, String modifier){
		List<String> modifierList = null;
		
		if(this.modifiersMap.containsKey(reln))
			modifierList = this.modifiersMap.get(reln);
		else
			modifierList = new ArrayList<String>();
		
		modifierList.add(modifier);
		this.modifiersMap.put(reln, modifierList);
	}
	
	public boolean addModifier(TreeGraphNode tn, String reln){
		String modifier = NLPTool.lemmatizeTermByTag(tn.nodeString(), 
				NLPTool.getTagFromTN(tn));
		this.putModifiersMap(reln, modifier);
		return true;
	}
	
	public boolean addModifier(TypedDependency td){
		String reln = td.reln().toString();
		if(!Rules.isModifierReln(reln)) 
			return false;
		
		String modifier = NLPTool.lemmatizeTermByTag(td.dep().nodeString(), 
				NLPTool.getTagFromTN(td.dep()));
		this.putModifiersMap(reln, modifier);
		return true;
	}
	
	public boolean addPrepPhrase(TypedDependency prepTd, NounPhrase np){
		String reln = prepTd.reln().toString();
		if(!Rules.isPrepPhraseReln(reln)) 
			return false;
		String prep = reln.split("_")[1];
		PrepPhrase pp = new PrepPhrase(prep, np);
		this.ppList.add(pp);
		return true;
	}
	
	@Override
	public List<String> getTermList(){
		return this.getTermList("amod", true);
	}
	
	public List<String> getTermList(String upperReln, boolean considerPP){
		List<String> termList = new ArrayList<String>();
		StringTokenizer strToker = 
				new StringTokenizer(this.getPhrase(upperReln, false), " ");		
		
		while(strToker.hasMoreTokens()){
			termList.add(strToker.nextToken());
		}
		
		if(considerPP){
			for(PrepPhrase pp : this.ppList){
				termList.addAll(pp.getTermList(upperReln, true));
			}
		}
		
		return termList;
	}
	
	@Override
	public void show() {
		System.out.println(this.getPhrase());
	}
	
	@Override
	public boolean equals(Object other) { 
        if (this == other) 
            return true; 
        else
        	return false;
    }

}
