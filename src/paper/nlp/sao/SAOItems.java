package paper.nlp.sao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import paper.nlp.phrase.object.NounPhrase;
import paper.nlp.phrase.object.SubjPhrase;
import paper.nlp.phrase.object.Verb;
import paper.nlp.phrase.object.VerbPhrase;

public class SAOItems {

	public static void main(String[] args) {
	}
	
	//property 
	private Verb action;
	private Set<NounPhrase> subjectSet;
	private Set<NounPhrase> objectSet;
	private String subjectReln;
	private boolean isValid;
	
	//constructor
	public SAOItems(Verb action){
		this.action = action;
		this.subjectSet = new HashSet<NounPhrase>();
		this.objectSet = new HashSet<NounPhrase>();
		this.subjectReln = "";
		this.isValid = false;
	}
	
	//set subject reln
	public void setSubjectReln(String subjectReln){
		this.subjectReln = subjectReln;
	}
	
	public String getSubjectReln(){
		return this.subjectReln;
	}
	
	//get Subjects and Objects 
	public Set<NounPhrase> getEntities(String type){
		if(type.equals("S") || type.equals("s"))
			return this.subjectSet;
		else if(type.equals("O") || type.equals("o"))
			return this.objectSet;
		else
			return null;
	}
	
	//add NounPhrase List or Set 
	public void addEntities(String type, Collection<NounPhrase> otherEntities){
		Set<NounPhrase> entitySet = this.getEntities(type);
		if(entitySet == null || otherEntities == null) return;	
		entitySet.addAll(otherEntities);
		this.checkValid();
	}
	
	//add NounPhrase
	public void addEntity(String type, NounPhrase entity){
		Set<NounPhrase> entitySet = this.getEntities(type);
		if(entitySet == null || entity == null) return;	
		entitySet.add(entity);
		this.checkValid();
	}
	
	//copy All Entities from other SAOItems
	public void copyFromSAOItems(SAOItems otherSAO){
		this.addEntities("S", otherSAO.getEntities("S"));
		this.addEntities("O", otherSAO.getEntities("O"));
		this.setSubjectReln(otherSAO.getSubjectReln());
		this.checkValid();
	}
	
	//check valid
	private void checkValid(){
		if(this.subjectSet.size() > 0 && this.objectSet.size() > 0)
			this.isValid = true;
	}
	
	public boolean isValid(){
		return this.isValid;
	}
	
	//--------process phrases---------------------------------------	
	//action
	
	public String getActionStr(){
		return this.action.getVerbWithPrt();
	}
	
	//consider nsubjpass
	public Set<NounPhrase> getEntConsiderPass(String type){
		if(this.subjectReln.equals("nsubjpass")){
			if(type.equals("S") || type.equals("s")) type = "O";
			else if(type.equals("O") || type.equals("o")) type = "S";
		}
			
		return this.getEntities(type);
	}
	
	//get Subject phrase List
	public List<SubjPhrase> getSpList(){
		Set<NounPhrase> entitySet = this.getEntConsiderPass("S");
		List<SubjPhrase> spList = new ArrayList<SubjPhrase>();
		
		for(NounPhrase subject : entitySet){
			spList.add(new SubjPhrase(this.action, subject));
		}
		return spList;
	}
	
	//get Subject phrase List
	public Set<NounPhrase> getOpSet(){
		Set<NounPhrase> opSet = this.getEntConsiderPass("O");
		return opSet;
	}
	
	//get Verb + Object phrase List
	public List<VerbPhrase> getVpList(){
		Set<NounPhrase> entitySet = this.getEntConsiderPass("O");
		List<VerbPhrase> vpList = new ArrayList<VerbPhrase>();
		
		for(NounPhrase object : entitySet){		
			//vpList.add(new VerbPhrase(this.getActionStr(), object));
			vpList.add(new VerbPhrase(this.action, object));
		}		
		return vpList;
	}
	
	//----------show---------------------------------------------
	//show SAO
	public void showSAO(){
		//show S
		for(NounPhrase subject : this.subjectSet)
			System.out.println("S: " + subject.getPhrase());
		//show A
		System.out.println("A: " + this.getActionStr());
		
		//show O
		for(NounPhrase object : this.objectSet)			
			System.out.println("O: " + object.getPhrase());
		
		System.out.println();
	}
}
