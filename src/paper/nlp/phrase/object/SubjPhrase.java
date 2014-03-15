package paper.nlp.phrase.object;

import java.util.List;

public class SubjPhrase extends Phrase{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {

	}
	
	//property 
	private Verb verbObj;
	private NounPhrase npObj;
	
	//constructor
	public SubjPhrase(Verb verbObj, NounPhrase npObj){
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
	public String getPhrase() {
		return this.getPhrase("amod", true);
	}

	@Override
	public String getPhrase(String upperReln, boolean considerPP) {
		return this.getNpObj().getPhrase(upperReln, considerPP);
	}
	
	@Override
	public List<String> getTermList() {
		return this.getTermList("amod", true);
	}
	
	public List<String> getTermList(String upperReln, boolean considerPP){
		return this.getNpObj().getTermList(upperReln, considerPP);
	}
	
	@Override
	public void show() {
		System.out.println(this.getPhrase());
	}

}
