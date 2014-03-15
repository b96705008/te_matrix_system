package paper.nlp.phrase.object;

import java.util.ArrayList;
import java.util.List;

public class PrepPhrase extends Phrase{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String prep;
	private NounPhrase npObj;
	
	public PrepPhrase(String prep, NounPhrase npObj){
		this.prep = prep;
		this.npObj = npObj;
	}
	
	@Override
	public String getPhrase(){
		return getPhrase("amod", true);
	}
	
	@Override
	public String getPhrase(String upperReln, boolean considerPP){
		String prepPhrase = this.prep + " " + this.npObj.getPhrase();
		return prepPhrase;
	}
	
	public NounPhrase getNpObj(){
		return this.npObj;
	}
	
	@Override
	public List<String> getTermList() {
		return this.npObj.getTermList("amod", true);
	}
	
	//prep -> stop word
	public List<String> getTermList(String upperReln, boolean considerPP){
		return this.npObj.getTermList(upperReln, considerPP);
	}
	
	@Override
	public void show() {
		System.out.println(this.getPhrase());
	}

}
