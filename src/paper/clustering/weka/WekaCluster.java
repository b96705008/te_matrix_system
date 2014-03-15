package paper.clustering.weka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import paper.clustering.cluster.ClusterGroup;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaCluster {

	public static void main(String[] args) {
		double[][] input={        // input dissimilarity matrix
			    {0.00,2.04,1.92,2.35,2.06,2.12,2.27,2.34,2.57,2.43,1.90,2.41},
			    {2.04,0.00,2.10,2.00,2.23,2.04,2.38,2.36,2.23,2.36,2.57,2.34},
			    {1.92,2.10,0.00,1.95,2.21,2.23,2.32,2.46,1.87,1.88,2.41,1.97},
			    {2.35,2.00,1.95,0.00,2.05,1.78,2.08,2.27,2.14,2.14,2.38,2.17},
			    {2.06,2.23,2.21,2.05,0.00,2.35,2.23,2.18,2.30,1.98,1.74,2.06},
			    {2.12,2.04,2.23,1.78,2.35,0.00,2.21,2.12,2.21,2.12,2.17,2.23},
			    {2.27,2.38,2.32,2.08,2.23,2.21,0.00,2.04,2.44,2.19,1.74,2.13},
			    {2.34,2.36,2.46,2.27,2.18,2.12,2.04,0.00,2.19,2.09,1.71,2.17},
			    {2.57,2.23,1.87,2.14,2.30,2.21,2.44,2.19,0.00,1.81,2.53,1.98},
			    {2.43,2.36,1.88,2.14,1.98,2.12,2.19,2.09,1.81,0.00,2.00,1.52},
			    {1.90,2.57,2.41,2.38,1.74,2.17,1.74,1.71,2.53,2.00,0.00,2.33},
			    {2.41,2.34,1.97,2.17,2.06,2.23,2.13,2.17,1.98,1.52,2.33,0.00}
		        };
		List<String> idList = new ArrayList<String>();
		for(int i=0; i<input.length; i++){
			idList.add(i+"");
		}
		
		WekaCluster cluster = new WekaCluster(input, idList);
		cluster.clusteringData(3);
	}
	
	//property
	private Instances dataSet;
	private List<String> idList;
	
	//constructor
	public WekaCluster(double[][] matrix, List<String> idList){
		this.idList = idList;
		this.setDataSet(matrix);
	}
	
	private void setDataSet(double[][] matrix){
		//Declare numeric attributes and the feature vector
		int dim = matrix[0].length;
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for(int i=0; i<dim; i++){
			Attribute attr = new Attribute(i+"");
			attributes.add(attr);
		}

		//Declare the Instances
		this.dataSet = new Instances("ClusterSet", attributes, 0);
		
		//add data
		for(int i=0; i<matrix.length; i++){
			Instance inst = new DenseInstance(1.0, matrix[i]);
			this.dataSet.add(inst);
		}
	}
	
	public ClusterGroup clusteringData(int k){
		ClusterGroup clusters = new ClusterGroup();
		try {
			SimpleKMeans clusterer = new SimpleKMeans();
			clusterer.setSeed(k);
			clusterer.setNumClusters(k);
			clusterer.setPreserveInstancesOrder(true);
			clusterer.buildClusterer(this.dataSet);    // build the clusterer
			
			int[] assignments = clusterer.getAssignments();

			int i=0;
			for(int clusterID : assignments) {
				String id = this.idList.get(i);
				clusters.addElement(clusterID+"", id);
				System.out.println(id+"->"+clusterID);
			    i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return clusters;
	}
}
