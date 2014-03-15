package paper.nlp;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


import paper.Config;
import paper.nlp.phrase.PhraseGroup;
import paper.nlp.sao.SAOMiner;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class NLPTool {
	
	public static void main(String[] args) throws Exception {
		String p5446286 = "An efficient and ultrafast sensor for X-ray and UV radiation based on doped nanocrystals. These doped nanocrystals consist preferably of impurity-activator doped wide band gap II-VI semiconductors. They yield high efficiency and short recombination time radiation-sensitive phosphors which in response to radiation emit visible light easily detected by conventional sensors such as Si sensors. The combination of pulsed UV/X-ray sources with efficient and ultrafast sensors will yield sensors with increased signal to noise ratio. In a preferred embodiment, thin films of doped nanocrystals are used for generating visible radiation, which can be imaged with a conventional Si-based camera. Other applications also include the use of doped nanocrystals of piezoelectric materials to sense pressure, of pyroelectric materials to sense heat, and of ferroelectric materials to sense electric fields.";
		String p6239449 = "A photodetector capable of normal incidence detection over a broad range of long wavelength light signals to efficiently convert infrared light into electrical signals. It is capable of converting long wavelength light signals into electrical signals with direct normal incidence sensitivity without the assistance of light coupling devices or schemes. In the apparatus, stored charged carriers are ejected by photons from quantum dots, then flow over the other barrier and quantum dot layers with the help of an electric field produced with a voltage applied to the device, producing a detectable photovoltage and photocurrent. The photodetector has multiple layers of materials including at least one quantum dot layer between an emitter layer and a collector layer, with a barrier layer between the quantum dot layer and the emitter layer, and another barrier layer between the quantum dot layer and the collector.";
		
		//getPhrasesFromSents(p5446286);
		//System.out.println(lemmatizeTerm("Conflicted"));
		//System.out.println(NLPTool.lemmatizeTerm("talked about"));
		//System.out.println(NLPTool.lemmatizeTermByTag("sensing", "VB"));
	}
	
	static MaxentTagger TAGGER;
	static LexicalizedParser PARSER;
	static{
		try {
			//String[] options = { "-maxLength", "100", "-retainTmpSubcategories" };
			PARSER = LexicalizedParser.loadModel(Config.parserModelFile);
			TAGGER = new MaxentTagger(Config.taggerModelFile);	
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static String removeNoise(String sent) {
		// String regex = "((\\d+\\w?\\s?,\\s?)*(\\d+\\w?\\s?(and|or)\\s?\\d+\\w?))|(\\d+\\w?\\s?(and|or)\\s?\\d+\\w?)|(\\d+\\w?\\s)";
		//return sent.replaceAll(regex, " ");
		//sent = sent.replaceAll("[*:]", "");
		sent = sent.replaceAll("[*:]|\\(\\w\\)", "");
		sent = sent.replace(";", ".");
		return sent;
	} 
	
	//===Tagger===
	//get sentences from raw str
	public static List<List<HasWord>> getSentences(String sentsStr){
		sentsStr = sentsStr.replace("e.g.,", "");
		sentsStr = sentsStr.replace("e.g.", "");
		StringReader sReader = new StringReader(sentsStr);	
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(sReader);
		
		return sentences;
	}
	
	public static List<TaggedWord> getTagWordList(String sentsStr){
		List<TaggedWord> tagWordsList = new ArrayList<TaggedWord>();
		
		List<List<HasWord>> sentences = getSentences(sentsStr);
		for(List<HasWord> sentence : sentences){
			tagWordsList.addAll(TAGGER.tagSentence(sentence));
		}
		return tagWordsList;
	}
	
	//pos tag: get NN, VB
	public static List<String> getPOSWordsFromSent(List<HasWord> sentence, String pos){
		List<String> wordList = new ArrayList<String>();	
		ArrayList<TaggedWord> tSentence = TAGGER.tagSentence(sentence);
	      
	    for(TaggedWord tWord:tSentence){
	    	if(tWord.tag().startsWith(pos)){
	    		//System.out.println(tWord.value()+"_"+tWord.tag());
	    		wordList.add(tWord.value());
	    	}
	    }	        
	    //System.out.println(Sentence.listToString(tSentence, false));	    
		return wordList;
	}
	
	//pos tag: get NN, VB
	public static List<String> getPOSWordsFromSents(String str, String pos){
		List<String> wordList = new ArrayList<String>();
		
		List<List<HasWord>> sentences = getSentences(str);
		for (List<HasWord> sentence : sentences) {
			List<String> subWordList = getPOSWordsFromSent(sentence, pos);
			wordList.addAll(subWordList);
		}	    
		
		return wordList;
	}
	
	//===Parser===
	//parse Tree
	public static Tree parseSentToTree(List<? extends HasWord> sentence){
		Tree parse = PARSER.apply(sentence);
		return parse;
	}
	
	//parse one sentence and get List<TypedDependency>
	public static List<TypedDependency> getTdFromTree(Tree parse){		 
	    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
	    
	    return tdl;
	}
	
	//get tag from treeGraphNodes
	public static String getTagFromTN(TreeGraphNode tn){
		String tag = tn.label().tag();
		if(tag != null)
			return tag;
		else
			return "";
	}
	
	//From raw sents -> SAO -> Phrases
	public static PhraseGroup getPhrasesFromsSents(List<List<HasWord>> sentences){
		PhraseGroup mainPhrasesObj	= new PhraseGroup();
		
		for (List<HasWord> sentence : sentences) {
			Tree parse = parseSentToTree(sentence);
			List<TypedDependency> tdl = getTdFromTree(parse);
			SAOMiner miner = new SAOMiner(tdl);
			PhraseGroup subPhrasesObj = miner.extractSAO();
			mainPhrasesObj.addPhraseGroup(subPhrasesObj);
			
			//new TreePrint("typedDependenciesCollapsed").printTree(parse);
		    //System.out.println(sentence.toString());
			//miner.showSAOMap();
		}
		//mainPhrasesObj.showAllPhrases();
		return mainPhrasesObj;
	}
	
	public static PhraseGroup getPhrasesFromsStr(String sents){
		List<List<HasWord>> sentences = NLPTool.getSentences(sents);	
		return getPhrasesFromsSents(sentences);
	}
	
	//===lemmatization===
	private static List<String> lemmatize(String str){
		List<List<HasWord>> sentences = getSentences(str);
		return lemmatizeSents(sentences);
    }
	
	public static List<String> lemmatizeSents(List<List<HasWord>> sentences){
		List<String> lemmaTerms = new ArrayList<String>();
		Morphology mph = new Morphology();
		
		for(List<HasWord> sentence : sentences){
			ArrayList<TaggedWord> tSentence = TAGGER.tagSentence(sentence);
			for(TaggedWord tWord:tSentence){
				String lammaTerm = mph.lemma(tWord.value(), tWord.tag()).toString();
			    lemmaTerms.add(lammaTerm);
			}
		}
	    return lemmaTerms;
	}
	
	//lemmatization one word given tag
	public static String lemmatizeTermByTag(String term, String tag){
		term = term.toLowerCase();
		Morphology mph = new Morphology();
		String lammaTerm = mph.lemma(term, tag).toString();
		return lammaTerm;
	}
	
	//lemmatization one word
	public static String lemmatizeTerm(String str){
		List<String> lemmaTerms = lemmatize(str);
		if(lemmaTerms.size() == 0)
			return "";
		else
			return lemmaTerms.get(0);
	}
	
	//lemmatization phrase
	public static String lemmatizePhrase(String str){
		List<String> lemmaTerms = lemmatize(str);
		String lemmaPhrase = "";
		
		for(String lemmaTerm : lemmaTerms){
			lemmaPhrase += lemmaTerm +" ";
		}
		lemmaPhrase = lemmaPhrase.trim();
	  
	    return lemmaPhrase;
    }
}
