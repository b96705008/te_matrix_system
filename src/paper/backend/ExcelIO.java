package paper.backend;

import java.io.File; 
import java.io.IOException;
import java.util.Date; 


import paper.Config;
import paper.nlp.NLPTool;
import jxl.*; 
import jxl.read.biff.BiffException;

/*
example:
	Cell a1 = sheet.getCell(21,5); 
	String stringa1 = a1.getContents();
	System.out.println(stringa1);

*/

public class ExcelIO {
	
	public static void main(String[] args) throws Exception{
		Database db = Database.getInstance();
		dwpiDataIO("data/patent_dwpi2.xls", db);
		//tfContentIO("DDS", "data/TF_Matrix/DDS.xls", db);
		//tfContentIO("LEDS", "data/TF_Matrix/LEDS.xls", db);
		//tfContentIO("QDBFD", "data/TF_Matrix/QDBFD.xls", db);
		//updateData(Config.dwpiFilePath, db);
		
		//tfDimeIO("DDS", "data/TF_Matrix/DDS.xls", db);
		//tfDimeIO("LEDS", "data/TF_Matrix/LEDS.xls", db);
		//tfDimeIO("QDGP", "data/TF_Matrix/QDGP.xls", db);
		//tfDimeIO("CN", "data/TF_Matrix/CN.xls", db);
		//tfDimeIO("SRD", "data/TF_Matrix/SRD.xls", db);
	}
	
	static public String getPureString(String rawString){
		//rawString = "Provides terrestrial position \n with       geodetic accuracy.\n";
		rawString = rawString.trim();
		
		if(rawString.contains("ADVANTAGE") || rawString.contains("USE")){
			int index = rawString.indexOf("\n");	
			if(index != -1)
				rawString = rawString.substring(index+1);
		}
		
		rawString = rawString.replaceAll("\\s+", " ");
		
		System.out.println(rawString);
		return rawString.toLowerCase();
	}
	
	public static String getPurePatentID(String rawPatentID){
		return rawPatentID.substring(2, 9);
	}
	
	public static void effectDataIO(String excelPath) throws BiffException, IOException{
		Database db = Database.getInstance();
		Workbook workbook = Workbook.getWorkbook(new File(excelPath));
		Sheet sheet = workbook.getSheet(0);
		int rowNum = sheet.getRows();
		
		for(int i=1; i<rowNum; i++){
			String entry = sheet.getCell(0,i).getContents();
			String positive = sheet.getCell(1,i).getContents();
			String negative = sheet.getCell(2,i).getContents();
			String othrtags = sheet.getCell(3,i).getContents();
			String defined = sheet.getCell(4,i).getContents();
			
			String term = entry;
			String number = "1";
			String phrase = term;
			if(entry.contains("#")){
				String[] entryArr = entry.split("#");
				term = entryArr[0];
				number = entryArr[1];
				
				int firstIndex = defined.indexOf("\"");
				int lastIndex = defined.lastIndexOf("\"");
				if(firstIndex != -1 && lastIndex != -1){
					phrase = defined.substring(firstIndex+1, lastIndex);
				}else{
					phrase = term;
				}
			}
			
			//term = NLPTool.lemmatizeTerm(term.toLowerCase());
			term = term.toLowerCase();
			phrase = phrase.toLowerCase();
			
			String semantic = null;
			if(positive.equals("Positiv")){
				semantic = "positive";
			}else if(negative.equals("Negativ")){
				semantic = "negative";
			}
			
			db.insertEffectTerms(term, number, semantic, othrtags, defined, phrase);
			System.out.println(term+" "+number+" "+semantic+" "+othrtags+" "+defined+" "+phrase);
		}
		workbook.close();
	}
	
	public static void updateData(String excelPath, Database db) throws Exception{
		Workbook workbook = Workbook.getWorkbook(new File(excelPath));
		Sheet sheet = workbook.getSheet(0);
		int rowNum = sheet.getRows();
		for(int i=1; i<rowNum; i++){
			String patentID = getPurePatentID(sheet.getCell(0,i).getContents());
			//String abstractStr = getPureString(sheet.getCell(17,i).getContents());
			String firstClaimStr = getPureString(sheet.getCell(23,i).getContents());
			db.updatePatentProfile(patentID, firstClaimStr);
			System.out.println(patentID+" update!");
		}
	}
	
	//DWPI ÀÉ®×IO
	public static void dwpiDataIO(String excelPath, Database db) throws BiffException, IOException{
		Workbook workbook = Workbook.getWorkbook(new File(excelPath));
		Sheet sheet = workbook.getSheet(0);
		int rowNum = sheet.getRows();
		for(int i=1; i<rowNum; i++){
			String patentID = getPurePatentID(sheet.getCell(0,i).getContents());
			String year = sheet.getCell(1,i).getContents().toLowerCase();
			String title = sheet.getCell(2,i).getContents().toLowerCase();
			String title_dwpi = sheet.getCell(3,i).getContents().toLowerCase();
			String ipc = sheet.getCell(6,i).getContents().toLowerCase();
			String citing_ref = sheet.getCell(13,i).getContents().toLowerCase();
			String forwardCiteNum = sheet.getCell(14,i).getContents().toLowerCase();
			String ab_dwpi = sheet.getCell(18,i).getContents().toLowerCase();
			String novelty_dwpi = getPureString(sheet.getCell(19,i).getContents());
			String use_dwpi = getPureString(sheet.getCell(20,i).getContents());
			String adv_dwpi = getPureString(sheet.getCell(21,i).getContents());
			String tech_dwpi = sheet.getCell(22,i).getContents().toLowerCase();
			String first_claim =  getPureString(sheet.getCell(23,i).getContents());
			String terms_dwpi = sheet.getCell(25,i).getContents().toLowerCase();
			String ab =  getPureString(sheet.getCell(17,i).getContents());
			db.insertPatentProfile(patentID, year, title, title_dwpi, ipc, citing_ref, forwardCiteNum, 
					ab_dwpi, novelty_dwpi, use_dwpi, adv_dwpi, tech_dwpi, first_claim, terms_dwpi, ab);
			System.out.println(patentID+" inserted!");
		}
		
		System.out.println("total patent num: "+(rowNum-1));
	}

	public static void tfContentIO(String field, String excelPath, Database db) throws BiffException, IOException{
		Workbook workbook = Workbook.getWorkbook(new File(excelPath));
		Sheet sheet = workbook.getSheet(2);
		int rowNum = sheet.getRows();
		
		for(int i=1; i<rowNum; i++){
			String patentID = sheet.getCell(0,i).getContents().toLowerCase();
			String year = sheet.getCell(1,i).getContents().toLowerCase();
			String tech = sheet.getCell(2,i).getContents().toLowerCase();
			String fun = sheet.getCell(3,i).getContents().toLowerCase();
			db.insertTFMatrixData(field, patentID, year, tech, fun);
			System.out.println(patentID+" inserted!");
		}
		
		System.out.println("total record num: "+(rowNum-1));
	}
	
	//TF answer IO
	public static void tfDimeIO(String field, String excelPath, Database db) throws BiffException, IOException{
		Workbook workbook = Workbook.getWorkbook(new File(excelPath));
		//String[] dimeArr = {"TECH", "EFFECT", "USE"};
		String[] dimeArr = {"TECH", "EFFECT"};
		
		for(int j=0; j<dimeArr.length; j++){
			Sheet sheet = workbook.getSheet(j+3);
			int rowNum = sheet.getRows();
			String dime = dimeArr[j];
			
			for(int i=1; i<rowNum; i++){
				String patentID = sheet.getCell(0,i).getContents().toLowerCase();
				String dime_level1 = sheet.getCell(1,i).getContents().toLowerCase();
				String dime_level2 = sheet.getCell(2,i).getContents().toLowerCase();
			
				db.insertTFDimeData(dime, patentID, field, dime_level1, dime_level2);
				System.out.println(dime+" "+patentID+" "+field+" "+dime_level1+" "+dime_level2);
			}
			
			System.out.println("total record num: "+(rowNum-1));
			
		}

	}
}
