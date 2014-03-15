package paper.nlp.phrase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import paper.nlp.NLPTool;
import paper.nlp.Rules;
import paper.nlp.phrase.object.NounPhrase;
import paper.nlp.phrase.object.Verb;
import paper.nlp.sao.SAOMiner;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TypedDependency;

public class PhraseMiner {

	public static void main(String[] args) {
		String p5446286 = "An efficient and ultrafast sensor for X-ray and UV radiation based on doped nanocrystals. These doped nanocrystals consist preferably of impurity-activator doped wide band gap II-VI semiconductors. They yield high efficiency and short recombination time radiation-sensitive phosphors which in response to radiation emit visible light easily detected by conventional sensors such as Si sensors. The combination of pulsed UV/X-ray sources with efficient and ultrafast sensors will yield sensors with increased signal to noise ratio. In a preferred embodiment, thin films of doped nanocrystals are used for generating visible radiation, which can be imaged with a conventional Si-based camera. Other applications also include the use of doped nanocrystals of piezoelectric materials to sense pressure, of pyroelectric materials to sense heat, and of ferroelectric materials to sense electric fields.";
		String test = "the phosphors are mechanically and chemically stable, in comparison to conventional phosphor powder.";
		
		List<List<HasWord>> sentences = NLPTool.getSentences(p5446286);
		for (List<HasWord> sentence : sentences) {
			Tree parse = NLPTool.parseSentToTree(sentence);	
			new TreePrint("typedDependenciesCollapsed").printTree(parse);
			List<TypedDependency> tdl = NLPTool.getTdFromTree(parse);
			PhraseMiner pMiner = new PhraseMiner(tdl);
			pMiner.mineNounPhrase();
			pMiner.showNP();
			/*
			SAOMiner miner = new SAOMiner(tdl);
			PhraseGroup phrases = miner.extractSAO();
			miner.showSAOMap();
			phrases.showAllPhrases();
			*/
		}	
	}
	
	//property
	private List<TypedDependency> tdl;
	private Map<Integer, NounPhrase> npMap;
	private Map<Integer, Verb> verbMap;
	private Set<Integer> ppIndexs;
	
	//constructor
	private PhraseMiner(){
		this.npMap = new HashMap<Integer, NounPhrase>();
		this.verbMap = new HashMap<Integer, Verb>();
		this.ppIndexs = new HashSet<Integer>();		
	}
	
	public PhraseMiner(List<TypedDependency> tdl){
		this();
		this.tdl = tdl;
	}
	
	//method
	//===Get and Set npMap, verbMap===
	//get Phrase By Tree GraphNode
	public NounPhrase getOrAddNpByTGN(TreeGraphNode tn){
		//declare
		int npIndex = tn.index();
		NounPhrase npObj = null;
		
		//get
		if(this.npMap.containsKey(npIndex)){
			npObj = this.npMap.get(npIndex);
			
		}else if(NLPTool.getTagFromTN(tn).startsWith("NN")){ //new NP
			String mainTerm = tn.nodeString();
			npObj = new NounPhrase(mainTerm);
			this.npMap.put(npIndex, npObj);
		}
		return npObj;
	}
	
	//get Prt Verb By ActionNode
	public Verb getOrAddVerbByTGN(TreeGraphNode tn){
		int verbIndex = tn.index();
		Verb verbObj = null;
		
		if(this.verbMap.containsKey(verbIndex))
			verbObj = this.verbMap.get(verbIndex);
		
		else if(NLPTool.getTagFromTN(tn).startsWith("VB")){
			verbObj = new Verb(tn.nodeString());
			this.verbMap.put(verbIndex, verbObj);
		}
		
		return verbObj;
	}
	
	
	//===update npMap===
	private void findNpAndVp(TypedDependency td){
		TreeGraphNode govTN = td.gov();
		TreeGraphNode depTN = td.dep();
		getOrAddNpByTGN(govTN);
		getOrAddNpByTGN(depTN);
		getOrAddVerbByTGN(govTN);
		getOrAddVerbByTGN(depTN);
	}
	
	 //ex: dobj(eating-5, sausage-6)
	private boolean updateNpMapByNoun(TypedDependency td){
		//declare
		NounPhrase npObj = this.getOrAddNpByTGN(td.dep());
		
		if(npObj == null) 
			return false;
		else {
			//ex: nsubj(stable-7, phosphors-2)
			if(NLPTool.getTagFromTN(td.gov()).startsWith("JJ")){
				npObj.addModifier(td.gov(), "amod");
			}
			return true;
		}
			
	}
	
	//ex: amod(sensitivity-2, high-1)-O, advmod(likes-4, also-3)-X
	private boolean updateNpMapByModif(TypedDependency td){
		//declare
		NounPhrase npObj = this.getOrAddNpByTGN(td.gov());
		if(npObj == null) return false;	
		
		//update
		boolean isAddSuccess = npObj.addModifier(td); 	
		if(isAddSuccess && td.reln().toString().equals("nn")) 
			this.ppIndexs.add(td.dep().index());
		return isAddSuccess;
	}
	
	//ex: prep_of(top-1, desk-3)-O, prep_on(counting-3, you-5)-X
	private boolean updateNpMapByPrep(TypedDependency prepTd){
		//declare
		NounPhrase govNpObj = this.getOrAddNpByTGN(prepTd.gov());
		NounPhrase depNpObj = this.getOrAddNpByTGN(prepTd.dep());
		//if(govNpObj == null || depNpObj == null) return false;
		
		//consider ex: one of [np] -> one is not a np
		if(depNpObj == null) return false;
		else if(govNpObj == null){
			govNpObj = new NounPhrase(prepTd.gov().nodeString());
			this.npMap.put(prepTd.gov().index(), govNpObj);
		}
		
		//update
		boolean isAddSuccess = govNpObj.addPrepPhrase(prepTd, depNpObj);
		if(isAddSuccess) this.ppIndexs.add(prepTd.dep().index());
		return isAddSuccess;
	}
	
	//===update verbMap===
	//ex: prt(tell-1, apart-2)
	private void updateVerbMapByPrt(TypedDependency td){
		Verb verbObj = this.getOrAddVerbByTGN(td.gov());
		String prt = td.dep().nodeString();
		verbObj.setPrt(prt);
	}
	
	//ex: prep_on(counting-3, you-5)
	private void updateVerbMapByPrep(TypedDependency td){
		Verb verbObj = this.getOrAddVerbByTGN(td.gov());
		String prep = td.reln().toString().split("_")[1];
		verbObj.setPrt(prep);
	}
	
	//mine NP, verb prt
	private void mineNounPhrase(){
		List<TypedDependency> removeTdl = new ArrayList<TypedDependency>();
		
		for(TypedDependency td : this.tdl){
			String reln = td.reln().toString();
			
			if(Rules.isModifierReln(reln)){
				this.updateNpMapByModif(td);
				removeTdl.add(td);
			}else if(Rules.isNounRelatedReln(reln)){
				this.updateNpMapByNoun(td);
			}else if(Rules.isVerbParticleReln(reln)){
				this.updateVerbMapByPrt(td);
				removeTdl.add(td);
			}else{
				//this.findNpAndVp(td);
			}
		}//end for
		
		this.tdl.removeAll(removeTdl);
	}
	
	//mine PP and combine, verb prep
	private void minePrepPhrase(){
		List<TypedDependency> removeTdl = new ArrayList<TypedDependency>();
		
		for(int i=this.tdl.size()-1; i>=0; i--){
			TypedDependency td = this.tdl.get(i);
			String reln = td.reln().toString();
			
			if(Rules.isPrepPhraseReln(reln)){
				String tag = NLPTool.getTagFromTN(td.gov());
				if(tag.startsWith("VB")){
					this.updateVerbMapByPrep(td);
				//}else if(tag.startsWith("NN")){
				}else{	
					if(this.updateNpMapByPrep(td)) 
						removeTdl.add(td);
				}
			}
		}
		this.tdl.removeAll(removeTdl);
	}
	
	public List<TypedDependency> minePhrase(){
		this.mineNounPhrase();
		this.minePrepPhrase();
		return this.tdl;
	}
	
	//get nounPhrase list
	public List<NounPhrase> getNpList(){
		List<NounPhrase> npList = new ArrayList<NounPhrase>();
		
		for(int index : this.npMap.keySet()){
			if(this.ppIndexs.contains(index)) continue;
			NounPhrase npObj = this.npMap.get(index);
			npList.add(npObj);
		}
		return npList;
	}
	
	//get nounPhrase list
	public List<NounPhrase> getNpList(Set<NounPhrase> opSet){
		List<NounPhrase> npList = new ArrayList<NounPhrase>();
		
		for(int index : this.npMap.keySet()){
			if(this.ppIndexs.contains(index)) continue;
			NounPhrase npObj = this.npMap.get(index);
			if(opSet.contains(npObj)) continue;
			npList.add(npObj);
		}
		return npList;
	}
	
	public void showNP(){
		System.out.println("NP:");
		for(int index : this.npMap.keySet()){
			if(this.ppIndexs.contains(index)) continue;
			NounPhrase npObj = this.npMap.get(index);
			System.out.print(index+": ");
			npObj.show();
		}	
		System.out.println();
	}
}
