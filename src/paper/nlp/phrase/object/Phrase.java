package paper.nlp.phrase.object;

import java.io.Serializable;
import java.util.List;

public abstract class Phrase implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public abstract String getPhrase();
	public abstract String getPhrase(String upperReln, boolean considerPP);
	public abstract List<String> getTermList();
	public abstract void show();
}
