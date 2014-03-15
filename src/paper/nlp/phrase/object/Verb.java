package paper.nlp.phrase.object;

import java.io.Serializable;

import edu.stanford.nlp.trees.TypedDependency;
import paper.nlp.NLPTool;

public class Verb implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {

	}
	
	//property
	private String verb;
	private String prt;
	
	public Verb(String verb){
		this.verb = NLPTool.lemmatizeTermByTag(verb, "VB");
		this.prt = "";
	}
	
	
	public Verb(String verb, String tag){
		this.verb = NLPTool.lemmatizeTermByTag(verb, tag);
		this.prt = "";
	}

	public Verb(String verb, String tag, String prt){
		this.verb = NLPTool.lemmatizeTermByTag(verb, tag);
		this.prt = prt;
	}
	
	//method
	public void setPrt(String prt){
		this.prt = prt;
	}
	
	public String getVerbWithPrt() {
		if(this.prt.equals(""))
			return this.verb;
		else
			return this.verb + " " + this.prt;
	}
	
	public String getVerb() {
		return this.verb;
	}

}
