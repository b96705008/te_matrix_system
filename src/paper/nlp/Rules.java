package paper.nlp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rules {

	public static void main(String[] args) {
		System.out.println(getSubModifRels("amod").get(0));
	}
	
	private static Set<String> subjectRelnSet = new HashSet<String>();
	private static Set<String> objectRelnSet = new HashSet<String>();
	private static String[] modifierRelns = {"nn", "amod", "advmod"};
	static{
			//Subject Reln
			subjectRelnSet.add("nsubj"); 
			subjectRelnSet.add("nsubjpass"); //*passive
			subjectRelnSet.add("xsubj");
			
			//Object Reln
			objectRelnSet.add("dobj");
			objectRelnSet.add("pobj");
			objectRelnSet.add("iobj");
	}
	
	public static boolean isNounRelatedReln(String reln){
		if(Rules.subjectRelnSet.contains(reln) 
			|| Rules.objectRelnSet.contains(reln) 
			|| reln.startsWith("prep_"))
			return true;
		else
			return false;
	}
	
	public static boolean isObjectRelatedReln(String reln){
		if(Rules.objectRelnSet.contains(reln) 
			|| reln.startsWith("prep_"))
			return true;
		else
			return false;
	}
	
	public static boolean isSubjectRelatedReln(String reln){
		if(Rules.subjectRelnSet.contains(reln)) 
			return true;
		else
			return false;
	}
	
	public static boolean isModifierReln(String reln){
		boolean isModifier = false;
		//compare reln to the modifierRelns
		for(String modifierReln : modifierRelns){
			if(reln.equals(modifierReln)){
				isModifier = true;
				break;
			}
		}
		return isModifier;
	}
	
	public static List<String> getSubModifRels(String reln){
		List<String> subModifRels = new ArrayList<String>();
		
		for(int i=modifierRelns.length-1; i>=0; i--){
			if(reln.equals(modifierRelns[i])){
				for(int j=0; j<=i; j++){
					subModifRels.add(modifierRelns[j]);
				}
				break;
			}
		}
		return subModifRels;
	}
	
	public static boolean isPrepPhraseReln(String reln){
		if(reln.startsWith("prep_") 
			//&& !reln.equals("prep_with")
			) 
			return true;
		else
			return false;
	}
	
	//prt
	public static boolean isVerbParticleReln(String reln){
		if(reln.equals("prt")) 
			return true;
		else 
			return false;
	}
	
	//filter by specific verb
	public static boolean isTransitionVerb(String verb){
		if(verb.equals("comprise") 
			|| verb.equals("contain") 
			|| verb.equals("include") 
			|| verb.equals("compose")
			|| verb.equals("consist"))
			return true;
		else
			return false;
	}
	
	//relate to use verb -> From WordNet search
	public static boolean isUseVerb(String verb){
		if(verb.equals("use") 
			|| verb.equals("utilize") 
			|| verb.equals("utilise") 
			|| verb.equals("apply")
			|| verb.equals("employ"))
			return true;
		else
			return false;
	}
	
	//relate to use noun -> From WordNet search
	public static boolean isUseNoun(String noun){
		if(noun.equals("use")
			|| noun.equals("function")
			|| noun.equals("purpose")
			|| noun.equals("role"))
			return true;
		else
			return false;
	}
}
