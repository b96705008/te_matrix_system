package paper.semantic;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.didion.jwnl.JWNLException;

import paper.Config;


import edu.illinois.cs.cogcomp.wnsim.WNSim;
import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.JWSRandom;
import edu.sussex.nlp.jws.JiangAndConrath;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Resnik;
import edu.sussex.nlp.jws.WuAndPalmer;

public class WordNetSim {


	public static void main(String[] args) {
		/*
		linSimilarity("sensor", "detector", "n");
		linSimilarity("make", "do", "v");
		linSimilarity("optical", "visual", "a");
		wupSimilarity("sensor", "detector", "n");
		wupSimilarity("optical", "visual", "a");
		*/
		Set<String> terms1 = new HashSet<String>();
		terms1.add("detector");
		terms1.add("detection");
		
		Set<String> terms2 = new HashSet<String>();
		terms2.add("sensor");
		terms2.add("layer");
		
		
		//System.out.println(getTermsSimilarity(terms1, terms2));
		System.out.println(wnsimSimilarity("carrier", "detector"));
	}
	
	private static WNSim wnsim;
	private static JWS	jws;
	static{
		try {
			wnsim = WNSim.getInstance(Config.wordNetDir + "3.0", "wordnet.xml");
			jws = new JWS(Config.wordNetDir, "3.0");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JWNLException e) {
			e.printStackTrace();
		} 
	}
	
	
	public static double linSimilarity(String term1, String term2, String tag){
		if(term1.equals(term2)) return 1;	
		Lin lin = jws.getLin();
		double similarity = lin.max(term1, term2, tag);
		return similarity;
	}
	
	public static double wupSimilarity(String term1, String term2, String tag){
		if(term1.equals(term2)) return 1;
		WuAndPalmer wup = jws.getWuAndPalmer();
		double similarity = wup.max(term1, term2, tag);
		return similarity;
	}
	
	public static double wnsimSimilarity(String term1, String term2){
		if(term1.equals(term2)) return 1;	
		double similarity = 0;
		
		try {
			similarity = wnsim.getWupSimilarity(term1, term2);
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		return similarity;
	}
	
	private static double getMaxSimilarity(String term, Collection<String> otherTerms){
		double sim = 0;
		
		for(String otherTerm : otherTerms){
			//sim = Math.max(sim, wupSimilarity(term, otherTerm, "n"));
			sim = Math.max(sim, wnsimSimilarity(term, otherTerm));
			if(sim == 1) break;
		}
		return sim;
	}
	
	public static double getTermsSimilarity(Collection<String> terms1, Collection<String> terms2){
		if(terms1.size() == 0 || terms2.size() == 0) return 0;
		
		double sim1 = 0;
		for(String term1 : terms1){
			sim1 += getMaxSimilarity(term1, terms2);
		}
		sim1 /= terms1.size();
		
		double sim2 = 0;
		for(String term2 : terms2){
			sim2 += getMaxSimilarity(term2, terms1);
		}
		sim2 /= terms2.size();
		
		double finalSim = (sim1 + sim2) / 2;
		return finalSim;
	}
}
