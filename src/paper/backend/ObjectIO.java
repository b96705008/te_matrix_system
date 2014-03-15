package paper.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import paper.Config;
import paper.nlp.phrase.PhraseGroup;
import paper.nlp.phrase.object.NounPhrase;

public class ObjectIO {
	
	public static void main(String[] args) {
		 File Dir = new File("data/phrases/111/");
		 Dir.mkdirs();
	}
	
	public static ObjectOutputStream getObjectOutput(String filename){
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return out;
	}
	
	public static ObjectInputStream getObjectInput(String filename){
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
		
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return in;
	}
	
	//寫phraseGroup obj 到二位元檔
	public static void writePatentPhrases(String patentID, String pField, PhraseGroup phrases){
		String patentPhraseDir = Config.phrasesDir + patentID + "/"; 
		File Dir = new File(patentPhraseDir);
		if(!Dir.exists()) Dir.mkdirs();
		
		String patentPhraseFile = patentPhraseDir + pField;
		try {
			ObjectOutputStream out = getObjectOutput(patentPhraseFile);
			out.writeObject(phrases);
			out.close();
			System.out.println(patentID+" "+pField+" Object Persisted");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static PhraseGroup readPatentPhrases(String patentID, String pField){
		String patentPhraseFile = Config.phrasesDir + patentID + "/" + pField;
		PhraseGroup phrases = null;
		
		try {
			ObjectInputStream in  = getObjectInput(patentPhraseFile);
			phrases = (PhraseGroup)in.readObject();
			in.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}catch(InvalidClassException e){
			return null;
		}catch (IOException e) {
			e.printStackTrace();
		}
		return phrases;
	}

}
