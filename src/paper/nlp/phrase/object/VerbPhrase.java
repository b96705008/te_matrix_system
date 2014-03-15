package paper.nlp.phrase.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import paper.nlp.NLPTool;

//AO structure
public class VerbPhrase extends Phrase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		String str = "consist of";	
		System.out.print("me or".split(" ")[0]);
	}
	
	//property 
	private Verb verbObj;
	private NounPhrase npObj;
	
	//constructor
	public VerbPhrase(Verb verbObj, NounPhrase npObj){
		this.verbObj = verbObj;
		this.npObj = npObj;
	}
	
	//method	
	public Verb getVerbObj(){
		return this.verbObj;
	}
	
	public String getVerbWithPrt() {
		return this.getVerbObj().getVerbWithPrt();
	}
	
	public String getVerb() {
		return this.getVerbObj().getVerb();
	}
	
	public NounPhrase getNpObj(){
		return this.npObj;
	}
	
	@Override
	public String getPhrase(){
		return this.getPhrase("amod", true);
	}
	
	@Override
	public String getPhrase(String upperReln, boolean considerPP){
		return this.getVerbWithPrt() + " " + this.npObj.getPhrase(upperReln, considerPP);
	}
	
	@Override
	public List<String> getTermList(){
		return this.getTermList("amod", true, true);
	}
		
	public List<String> getTermListNoVerb(){
		return this.getTermList("amod", true, false);
	}
	
	public List<String> getTermList(String upperReln, boolean considerPP, boolean considerVerb){
		List<String> termList = new ArrayList<String>();
		termList.addAll(this.npObj.getTermList(upperReln, considerPP));
		
		if(considerVerb){
			termList.add(this.getVerb());
		}
		return termList;
	}
	
	@Override
	public void show() {
		System.out.println(this.getPhrase());	
	}
}
