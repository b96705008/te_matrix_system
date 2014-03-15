package paper.patent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class InvertedIndex {
	
	//property
	private int docNum;
	private Map<String, Set<String>> invertedMap;
	private Map<String, Integer> termTfMap;
	private Map<String, Document> docMap;
	
	//constructor
	public InvertedIndex(){
		this.docNum = 0; //文件數, 預設為0
		this.invertedMap = new TreeMap<String, Set<String>>(); //term-文件集對照表
		this.termTfMap = new TreeMap<String, Integer>(); //term-集合總字數對照表
		this.docMap = new TreeMap<String, Document>(); //docID-doc物件對照表
	}
	
	//method
	//===Get===
	public Map<String, Set<String>> getInvertedMap(){
		return this.invertedMap ;
	}
	
	public Set<String> getPostingsByTerm(String term){
		return this.invertedMap.get(term);
	}
	
	public Set<String> getDictionary(){
		return this.invertedMap.keySet();
	}
	
	public int getDocNum(){
		return this.docNum;
	}
	
	public int getDicSize(){
		return this.invertedMap.keySet().size();
	}
	
	public Document getDocByID(String docID){
		return this.docMap.get(docID);
	}
	
	public Map<String, Document> getDocMap(){
		return this.docMap;
	}
	
	public int getDF(String term){
		if(this.invertedMap.containsKey(term))
			return this.invertedMap.get(term).size();
		else
			return 0;
	}
	
	public double getIDF(String term){
		double term_df = this.getDF(term);
		return  Math.log10(this.docNum / (term_df + 0.5));
	}
	
	public int getTF(String term){
		return this.termTfMap.get(term);
	}
	
	public double getTfIdf(String term){
		return this.getTF(term) * this.getIDF(term);
	}
	
	//===Add===
	public void addDocument(Document doc){
		this.docMap.put(doc.getID(), doc);
		this.docNum++;
	}
	
	//===Indexing===
	public void indexing(){
		for(String docID : this.docMap.keySet()){
			Map<String, Integer> termMap = 
					this.docMap.get(docID).getTermMap();
			for(String term : termMap.keySet()){
				int freq = termMap.get(term);
				this.updateInvertedMap(docID, term);
				this.updateTermTfMap(term, freq);
			}
		}
	}
		
	private void updateInvertedMap(String docID, String term){
		Set<String> patentIDs = null;
		if(this.invertedMap.containsKey(term)){
			patentIDs = this.invertedMap.get(term);
		}else{
			patentIDs = new TreeSet<String>();
		}
		patentIDs.add(docID);
		this.invertedMap.put(term, patentIDs);
	}
	
	private void updateTermTfMap(String term, int freq){
		if(this.termTfMap.containsKey(term)){
			int lastFreq = this.termTfMap.get(term);
			this.termTfMap.put(term, lastFreq+freq);
		}else{
			this.termTfMap.put(term, freq);
		}
		//System.out.println(term+" "+this.termTfMap.get(term));
	}
	
	//write dictionary
	public void writeDic(String fileName){
		System.out.println("write dictionary.txt...");

		BufferedWriter bw;
		
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			Set<String> dicSet = this.getDictionary();
			int i = 0;
			
			bw.write("t_index"+"\t"+"ter"+"\t"+"df");
			bw.newLine();
			for(String term : dicSet){
				i++;
				int freq = this.getDF(term);
				bw.write(i+"\t"+term+"\t"+freq);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("finish ...");
	}
}
