package paper.clustering.mahout;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.clustering.dirichlet.DirichletDriver;
import org.apache.mahout.clustering.dirichlet.models.DistributionDescription;
import org.apache.mahout.clustering.dirichlet.models.GaussianClusterDistribution;
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansDriver;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.hadoop.decomposer.DistributedLanczosSolver;

import paper.Config;
import paper.clustering.cluster.ClusterGroup;
import paper.clustering.cluster.ClusterValidity;
import paper.patent.PatentGroup;

public class MahoutCluster {
	
	 public static void main(String args[]) throws Exception {
     }
	 
	 private static Path VECTOR_PATH = new Path(Config.vectorFile);
	
	 public static ClusterGroup clusterData(int k, int rankThold, 
			 double weightThold) throws Exception{
		 
		 DistanceMeasure measure = new CosineDistanceMeasure(); 
		 //DistanceMeasure measure = new EuclideanDistanceMeasure();
		 HadoopUtil.delete(SequenceIO.CONF, new Path(Config.clusterCentroids));
		 HadoopUtil.delete(SequenceIO.CONF, new Path(Config.clusterOutput));
		 
		 useFuzzyKMeans(measure, k);
		 //useKMeans(measure, k);
		 //useDirichlet(k);
		 ClusterGroup clusters = 
				 SequenceIO.readClusters(Config.clusterPointsFile, rankThold, weightThold);
		 
		 return clusters;
	 }
	 
	 private static Path getPreClustersPath(DistanceMeasure measure, int k) throws IOException{
		 Path clusters = RandomSeedGenerator.buildRandom(SequenceIO.CONF, VECTOR_PATH, 
					 new Path(Config.clusterCentroids), k, measure);
		 return clusters;
	 }
	 
	 public static void useFuzzyKMeans(DistanceMeasure measure, int k) throws Exception{			
         //½×¤å-> m=1.5f, max Itr=5000, k=¸s¼Æ
		 Path clusters = getPreClustersPath(measure, k);
		 FuzzyKMeansDriver.run(SequenceIO.CONF, VECTOR_PATH, clusters, new Path(Config.clusterOutput),
        		 measure, 0.0, 5000, 1.5f, true, false, 0.0, true);
	 }
	 
	 public static void useKMeans(DistanceMeasure measure, int k) throws Exception{
			 
		 Path clusters = getPreClustersPath(measure, k);	
         KMeansDriver.run(SequenceIO.CONF, VECTOR_PATH, clusters, new Path(Config.clusterOutput), 
        		 measure, 0.0, 30, true, 0.0, true);
	 }
	 
	 public static void useDirichlet(int k) throws ClassNotFoundException, IOException, InterruptedException{
		 DistributionDescription description = new DistributionDescription(GaussianClusterDistribution.class.getName(),
				 RandomAccessSparseVector.class.getName(), null, 198);
		 DirichletDriver.run(SequenceIO.CONF, VECTOR_PATH, new Path(Config.clusterOutput), 
				 description, k, 500, 1.0, true, false, 1, true);
	 }
	
}
