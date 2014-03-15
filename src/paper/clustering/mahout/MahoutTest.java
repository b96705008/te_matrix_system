package paper.clustering.mahout;

import java.io.IOException;
import java.util.Map;


import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.clustering.lda.cvb.CVB0Driver;
import org.apache.mahout.driver.MahoutDriver;
import org.apache.mahout.math.NamedVector;

import paper.Config;
import paper.patent.PatentGroup;

public class MahoutTest {
	
	public static void main(String[] args) throws Throwable {
		MahoutDriver.main("cvb0_local".split(" "));
	}
	
	public static void doLSI(String vecType, String dimeName, PatentGroup patentGroup) throws Throwable{
		SequenceIO.generateVectorFile(vecType, dimeName, patentGroup);
		String vectorsFolder = Config.vectorDir;
		
		String eigenFolder = Config.eigenFolder;
		String matrixFolder = Config.matrixFolder;
		int numCols = patentGroup.getFeatureSize(dimeName);
		int numRows = patentGroup.getInvertedIndex().getDocNum();
		
		
		String svdJob = "svd -i "+vectorsFolder+" -o "+eigenFolder+" --rank 100 --numCols "+numCols
				+" --numRows "+numRows+" --cleansvd \"true\" --inMemory \"true\"";
		MahoutDriver.main(svdJob.split(" ")); 
		
		
		String rowidJob = "rowid -Dmapred.input.dir="+vectorsFolder+" -Dmapred.output.dir="+matrixFolder;
		MahoutDriver.main(rowidJob.split(" "));
		
		
		String ssvdJob = "ssvd -i "+matrixFolder+" -o "+matrixFolder+"/output --tempDir "+matrixFolder+"/temp";
		MahoutDriver.main(ssvdJob.split(" ")); 
		
		String transposeMatrixJob =  "transpose -i "+matrixFolder+"matrix --numRows "+numRows
				+" --numCols "+numCols+" --tempDir "+matrixFolder+"transpose";
		MahoutDriver.main(transposeMatrixJob.split(" "));
	}
}
