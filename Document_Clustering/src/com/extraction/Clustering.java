/*
 * @author akshay
 * 
 * 
 * */

package com.extraction;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;


public class Clustering {

	public static void main(String[] args) throws Exception {
		
		String dataset = "data/train.arff";
		DataSource source  = new DataSource(dataset);
		
 		Instances data = source.getDataSet();
 		SimpleKMeans model = new SimpleKMeans();
 		model.setNumClusters(5);
 		
 		model.buildClusterer(data);
 		System.out.println(model);
 		
 		
 	/*	
 		ClusterEvaluation clsEval = new ClusterEvaluation();
 		String dataset1 = "data/test.arff";
 		DataSource source1 = new DataSource(dataset1);
 		Instances data1 = source1.getDataSet();
 		
 		clsEval.setClusterer(model);
 		clsEval.evaluateClusterer(data1);
 		
 		System.out.println("# of clusters: " + clsEval.getNumClusters());
	*/	
		
		

	}

}

