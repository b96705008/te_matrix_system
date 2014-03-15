package paper.patent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import paper.Config;
import paper.backend.Database;
import paper.patent.InvertedIndex;
import paper.patent.Patent;
import paper.techmatrix.function.EffectMiner;

public class PatentGroup implements Iterable<Patent>{

	public static void main(String[] args) {
		Set<String> patentIDs = new TreeSet<String>();
		patentIDs.add("6445000");
		patentIDs.add("6476312");
		patentIDs.add("5952665");
		
		PatentGroup pg = new PatentGroup(patentIDs);
		for(Patent p : pg){
			p.showPhrasesByMap();
		}
	}
	
	//property
	private InvertedIndex inverted; //invertedIndex物件
	private Map<String, Set<String>> featureMap; //(tech,effect)-feature set
	
	//constructor
	public PatentGroup(Set<String> patentIDs){
		this.inverted = new InvertedIndex();
		this.initFeatureMap();
		this.addAllPatents(patentIDs);
	}
	
	//method
	private void initFeatureMap(){
		this.featureMap = new HashMap<String, Set<String>>();
		this.featureMap.put("tech", new TreeSet<String>());
		this.featureMap.put("effect", new TreeSet<String>());
		//this.featureMap.put("use", new TreeSet<String>());
	}
	
	//about feature
	public void addFeatures(String dimeName, Set<String> otherFeats){
		Set<String> features = this.getFeatureSet(dimeName);
		features.addAll(otherFeats);
	}
	
	public Set<String> getFeatureSet(String dimeName){
		return this.featureMap.get(dimeName);
	}
	
	public int getFeatureSize(String dimeName){
		return this.getFeatureSet(dimeName).size();
	}
	
	public void setFeatures(String dimeName, Set<String> newFeats){
		this.featureMap.put(dimeName, newFeats);
	}
	
	public void showFeatures(String dimeName){
		System.out.println(dimeName + " features: ");
		Set<String> features = this.getFeatureSet(dimeName);
		for(String feature : features){
			System.out.println(feature);
		}
	}
	
	//about InvertedIndex
	public InvertedIndex getInvertedIndex(){
		return this.inverted;
	}
	
	public Patent getPatentByID(String patentID){
		return (Patent) this.inverted.getDocByID(patentID);
	}
	
	public Set<String> getPatentIDs(){
		Set<String> patentIDs = this.inverted.getDocMap().keySet();
		return patentIDs;
	}
	
	public List<String> getPatentIDList(){
		List<String> patentIDList = new ArrayList<String>();
		patentIDList.addAll(this.getPatentIDs());
		return patentIDList;
	}
	
	//about Add, Update
	public void addAllPatents(Set<String> patentIDs){
		Database db = Database.getInstance();
		for(String patentID : patentIDs){
			Patent patent = db.setPatentProfile(patentID); //*******
			if(patent == null){
				System.out.println(patentID + " no data..");
				continue;
			}
			this.addPatent(patent);
			//patent.showPhrasesByMap();
			//patent.showTerms();
		}
		this.inverted.indexing(); //indexing
	}
	
	//聯集feature
	private void addPatent(Patent patent){
		this.inverted.addDocument(patent);
		patent.processTech();
		patent.processEffect();
		//patent.processTFfeats();
		this.addFeatures("tech", patent.getFeature("tech"));
		this.addFeatures("effect", patent.getFeature("effect"));
	}
	
	public Set<String> getFilteredEffectTerms(){
		Set<String> filteredEffectTerms = new HashSet<String>();
		filteredEffectTerms.addAll(this.getFeatureSet("effect"));
		
		Set<String> patentIDs = this.getPatentIDs();
		for(String patentID : patentIDs){
			filteredEffectTerms.removeAll(
					this.getPatentByID(patentID).getPureTechTerms());
		}
		
		return filteredEffectTerms;
	}
	
	@Override
	public Iterator<Patent> iterator() {
	
		return new Iterator<Patent> (){	 
			Iterator<String> idItr = getPatentIDs().iterator();
			
			@Override
			public boolean hasNext() {
				return idItr.hasNext();
			}

            @Override
            public Patent next() {
            	String patentID = idItr.next();
            	Patent patent = getPatentByID(patentID);
            	return patent;
            }
            
            @Override
            public void remove() {
            	throw new UnsupportedOperationException();
            }              
		 };
	}	 
	
}
