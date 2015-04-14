package com.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import org.apache.tika.*;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;

import com.features.*;
import com.visualization.*;

import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class MainFile {

	public static TreeMap<String, Integer> uniqUnigrams;
	public static HashMap<String, DocInfo> docUnigrams;
	public static TreeMap<String, Integer> uniqBigrams;
	public static HashMap<String, DocInfo> docBigrams;
	public static TreeMap<String, Integer> uniqTrigrams;
	public static HashMap<String, DocInfo> docTrigrams;
	public static TreeMap<String, Integer> uniqCapitals;
	public static HashMap<String, DocInfo> docCapitals;
	public static TreeSet<String> uniqPunct;
	public static HashMap<String, DocInfo> docPunct;
	public static TreeSet<String> uniqPOS;
	public static HashMap<String, DocInfo> docPOS;
	public static HashMap<String, Integer> docSentences;
	public static String currentDoc;
	public static ArrayList<Integer> featureChoice;
	public static Integer totalFeatures=0;
	public static PorterStemmer myStem;
	public static String currFilename;
	public static ArrayList<ArrayList<Integer>> cluster;
	int diffrentFeatures=7;
	File inputDir,file,outputDir,tempDir;		
	File[] files;
	BufferedWriter bw; 
	String st;
	DocInfo doc;	
	static Integer totalDocs=0;
	Unigram objUnigram;
	Bigram objBigram;
	Trigram objTrigram;
	Capital objCapital;
	Sentence objSentence;
	Punctuation objPunctuation;
	POS objPOS;
	int i;
	StringBuilder sb;	
	String destFolder,dataset,raw_data,processed,feature_data;
	int no_cluster=2;


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long lStartTime = new Date().getTime();
		MainFile obj;
		obj=new MainFile();
		obj.check();
		obj.init();
		obj.process();
		long lEndTime = new Date().getTime();
		long difference = lEndTime - lStartTime;
		System.out.println("Elapsed milliseconds: " + difference);
	}

	public void process()
	{
		try{
			String[] para = new String[2];
			totalDocs=files.length;

			// process each document
			for (i = 0; i < files.length; i++) {
				st=outputDir.getPath()+File.separator+files[i].getName().replaceFirst("[.][^.]+$", "")+".txt";
				file = new File(files[i].getPath());				//input file name
				bw = new BufferedWriter(new FileWriter(st));		//output file name
				currFilename=file.getName();
				processRawData();	
				bw.close();
				// read contents of file
				currentDoc = FileUtils.file2String(new File(st));
				para[1]=st;
				for(int c=0;c<featureChoice.size();c++)
				{
					switch(featureChoice.get(c)){
						case 0: docUnigrams.put(currFilename,new DocInfo());
								objUnigram=new Unigram();
								para[0]="descriptor"+File.separator+"Unigram.xml"; 	//Name of descriptor
								objUnigram.analyze(para);
								break;
						case 1: docBigrams.put(currFilename,new DocInfo());
								objBigram=new Bigram();
								para[0]="descriptor"+File.separator+"Bigram.xml";
								objBigram.analyze(para);
								break;
						case 2: docTrigrams.put(currFilename,new DocInfo());
								objTrigram=new Trigram();
								para[0]="descriptor"+File.separator+"Trigram.xml";
								objTrigram.analyze(para);
								break;
						case 3: docSentences.put(currFilename, 0);
								objSentence=new Sentence();
								para[0]="descriptor"+File.separator+"Sentence.xml";
								objSentence.analyze(para);
								break;
						case 4: docPOS.put(currFilename, new DocInfo());
								objPOS=new POS();
								para[0]="tagger"+File.separator+"english-left3words-distsim.tagger";
								objPOS.analyze(para);
								break;
						case 5: docPunct.put(currFilename,new DocInfo());
								objPunctuation=new Punctuation();
								para[0]="descriptor"+File.separator+"Punctuation.xml"; 
								objPunctuation.analyze(para);
								break;
						case 6: docCapitals.put(currFilename,new DocInfo());
								objCapital=new Capital();
								para[0]="descriptor"+File.separator+"Unigram.xml"; 
								objCapital.analyze(para);
								break;
					}
				}

				/*doc=docUnigrams.get(currFilename);
				if(doc.totalWords==0)
					docUnigrams.remove(currFilename);
				doc=docWords.get(currFilename);
				if(doc.totalWords==0)
					docWords.remove(currFilename);
				doc=docPOS.get(currFilename);
				if(doc.totalWords==0)
					docPOS.remove(currFilename);*/

				//objAnnotation.analyze(para);

				/*for(Map.Entry<String,Integer> entry : MainFile.uniqUnigrams.entrySet()) {
					  String key = entry.getKey();
					  Integer value = entry.getValue();
					  System.out.println(key + " => " + value);
					}*/

				/*for(Map.Entry<String, DocInfo> entry : MainFile.docPOS.entrySet()) {
					  String key = entry.getKey();
					  DocInfo value = entry.getValue();
					  System.out.println(key + " ===========================> "+value.totalWords);
					  for(Map.Entry<String, Integer> entry2 : value.wordCount.entrySet()) {
						  String key1 = entry2.getKey();
						  Integer value1 = entry2.getValue();
						  System.out.println(key1 + " ## " + value1);
					  }
					}*/
			}
			if(featureChoice.contains(3))
				totalFeatures=totalFeatures+1;
			if(totalFeatures!=0)
			{
				datasetCreation();
				clusterData();
				String[] args=new String[3];
				args[0]="6";	//Feature selection
				args[1]="0";	//Cluster selection
				args[2]=feature_data;	//location of features files
				BarGraph app = new BarGraph();
				app.caller(args);
			}
			else
			{
				System.out.println("Not a proper dataset");
				System.out.println("Program terminated");
				System.exit(0);
			}

		} catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clusterData()
	{
		DataSource source;
	
		try {
			source = new DataSource(dataset);
			cluster=new ArrayList<ArrayList<Integer>>(no_cluster);
			for(i=0;i<no_cluster;i++)
				cluster.add(new ArrayList<Integer>());
			Instances data = source.getDataSet();
			SimpleKMeans model = new SimpleKMeans();
			model.setNumClusters(no_cluster);
			
			model.buildClusterer(data);
			file=new File(destFolder);
			if (file.exists()) 
				//delete older files recursively
				recursiveDelete(file);

			if (!(file.mkdir())) 
			{
				System.out.println("Failed to create cluster directory???!");
				System.exit(0);
			}

			int i=0;
			for(i=0;i<no_cluster;i++)
			{
				file=new File(destFolder+File.separator+i);
				if(!(file.mkdir()))
				{
					System.out.println("Failed to create cluster directory!");
					System.exit(0);
				}
			}

			i=0;
			int no;
			for (Instance instance : data) {
				no=model.clusterInstance(instance);
								
				Files.copy(new File(processed+File.separator+files[i].getName().replaceFirst("[.][^.]+$", "")+".txt").toPath(),(new File(destFolder+"/"+no+"/"+files[i].getName().replaceFirst("[.][^.]+$", "")+".txt")).toPath(), StandardCopyOption.REPLACE_EXISTING);
				//System.out.println(processed+"/"+files[i].getName().replaceFirst("[.][^.]+$", "")+".txt"+"-->"+no);
				cluster.get(no).add(i);
				i++;
			}
			//	System.out.println(i);
			//	System.out.println(model);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void datasetCreation()
	{		
		try {
			int c;
			BufferedWriter tpbw;
			ArrayList<BufferedWriter> bw_tf=new ArrayList<BufferedWriter>(diffrentFeatures);
			ArrayList<BufferedWriter> bw_idf=new ArrayList<BufferedWriter>(diffrentFeatures);
			String key;
			Integer value,tf;
			Double tf_idf,idf;
			Iterator<String> iterator;
			//file for clustering in required format
			bw = new BufferedWriter(new FileWriter(dataset));
			bw.write("@relation document\n\n");
			
			for(i=0;i<diffrentFeatures;i++)
			{
				bw_tf.add(null);
				bw_idf.add(null);
			}
			
			for(c=0;c<featureChoice.size();c++)
			{
				switch(featureChoice.get(c)){
					case 0: tpbw=new BufferedWriter(new FileWriter(feature_data+File.separator+"unigrams.txt"));
							i=0;
							for(Map.Entry<String,Integer> entry : uniqUnigrams.entrySet()) {
								key = entry.getKey();
								bw.write("@attribute uni"+i+" numeric\n");
								tpbw.write(key+"\n");
								i++;								
							}
							tpbw.close();
							bw_tf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"unigram_tf.txt",true)));
							bw_idf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"unigram_tf_idf.txt",true)));
							break;
					case 1: tpbw=new BufferedWriter(new FileWriter(feature_data+File.separator+"bigrams.txt"));
							i=0;
							for(Map.Entry<String,Integer> entry : uniqBigrams.entrySet()) {
								key = entry.getKey();
								bw.write("@attribute bi"+i+" numeric\n");
								tpbw.write(key+"\n");
								i++;								
							}
							tpbw.close();
							bw_tf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"bigram_tf.txt",true)));
							bw_idf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"bigram_tf_idf.txt",true)));
							break;
					case 2: tpbw=new BufferedWriter(new FileWriter(feature_data+File.separator+"trigrams.txt"));
							i=0;
							for(Map.Entry<String,Integer> entry : uniqTrigrams.entrySet()) {
								key = entry.getKey();
								bw.write("@attribute tri"+i+" numeric\n");
								tpbw.write(key+"\n");
								i++;								
							}
							tpbw.close();
							bw_tf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"trigram_tf.txt",true)));
							bw_idf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"trigram_tf_idf.txt",true)));
							break;
					case 3: tpbw=new BufferedWriter(new FileWriter(feature_data+File.separator+"sentences.txt"));
							bw.write("@attribute #Sentences numeric\n");
							tpbw.write("Total Sentences\n");
							tpbw.close();
							bw_tf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"sentence_tf.txt",true)));
							break;
					case 4: tpbw=new BufferedWriter(new FileWriter(feature_data+File.separator+"POS.txt"));
							i=0;
							iterator = uniqPOS.iterator();
							while (iterator.hasNext()){
								key = iterator.next();
								bw.write("@attribute POS"+i+" numeric\n");
								tpbw.write(key+"\n");
								i++;								
							}
							tpbw.close();
							bw_tf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"POS_tf.txt",true)));
							break;
					case 5: tpbw=new BufferedWriter(new FileWriter(feature_data+File.separator+"punctuations.txt"));
							i=0;
							iterator = uniqPunct.iterator();
							while (iterator.hasNext()){
								key = iterator.next();
								bw.write("@attribute punct"+i+" numeric\n");
								tpbw.write(key+"\n");
								i++;								
							}
							tpbw.close();
							bw_tf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"punctuation_tf.txt",true)));
							break;
					case 6: tpbw=new BufferedWriter(new FileWriter(feature_data+File.separator+"capitals.txt"));
							i=0;
							for(Map.Entry<String,Integer> entry : uniqCapitals.entrySet()) {
								key = entry.getKey();
								bw.write("@attribute capital"+i+" numeric\n");
								tpbw.write(key+"\n");
								i++;								
							}
							tpbw.close();
							bw_tf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"capital_tf.txt",true)));
							bw_idf.set(c,new BufferedWriter(new FileWriter(feature_data+File.separator+"capital_tf_idf.txt",true)));
							break;
				}					
			}
			bw.write("\n@data\n");

			sb=new StringBuilder(1000);
			for (i = 0; i < files.length; i++) {
				sb.setLength(0);
				st=files[i].getName();
							
				for(c=0;c<featureChoice.size();c++){
					switch(featureChoice.get(c)){
						case 0: doc=docUnigrams.get(st);
								for(Map.Entry<String,Integer> entry : MainFile.uniqUnigrams.entrySet()) {
									key = entry.getKey();
									value = entry.getValue();
									tf=doc.wordCount.get(key);
									if(tf!=null)
									{
										idf=Math.log10(totalDocs*1.0/value);
										tf_idf=tf*idf;
										sb.append(tf_idf+",");
										bw_idf.get(c).write(tf_idf+",");
										bw_tf.get(c).write(tf+",");
									}
									else
									{
										sb.append("0,");	
										bw_idf.get(c).write("0,");
										bw_tf.get(c).write("0,");
									}									
								}	
								bw_idf.get(c).write("\n");
								bw_tf.get(c).write("\n");
								break;
						case 1: doc=docBigrams.get(st);
								for(Map.Entry<String,Integer> entry : MainFile.uniqBigrams.entrySet()) {
									key = entry.getKey();
									value = entry.getValue();
									tf=doc.wordCount.get(key);
									if(tf!=null)
									{
										idf=Math.log10(totalDocs*1.0/value);
										tf_idf=tf*idf;
										sb.append(tf_idf+",");
										bw_idf.get(c).write(tf_idf+",");
										bw_tf.get(c).write(tf+",");
									}
									else
									{
										sb.append("0,");
										bw_idf.get(c).write("0,");
										bw_tf.get(c).write("0,");
									}
								}
								bw_idf.get(c).write("\n");
								bw_tf.get(c).write("\n");
								break;
						case 2: doc=docTrigrams.get(st);
								for(Map.Entry<String,Integer> entry : MainFile.uniqTrigrams.entrySet()) {
									key = entry.getKey();
									value = entry.getValue();
									tf=doc.wordCount.get(key);
									if(tf!=null)
									{
										idf=Math.log10(totalDocs*1.0/value);
										tf_idf=tf*idf;
										sb.append(tf_idf+",");
										bw_idf.get(c).write(tf_idf+",");
										bw_tf.get(c).write(tf+",");
									}
									else
									{
										sb.append("0,");
										bw_idf.get(c).write("0,");
										bw_tf.get(c).write("0,");
									}
								}
								bw_idf.get(c).write("\n");
								bw_tf.get(c).write("\n");
								break;
						case 3: sb.append(docSentences.get(st)+",");
								bw_tf.get(c).write(docSentences.get(st)+"\n");
								break;
						case 4: doc=docPOS.get(st);
								iterator = uniqPOS.iterator();
								while (iterator.hasNext()){
									tf=doc.wordCount.get(iterator.next());
									if(tf!=null)
									{
										tf_idf= (double)tf/doc.totalWords;
										sb.append(tf_idf+",");
										bw_tf.get(c).write(tf_idf+" ");
									}
									else
									{
										sb.append("0,");
										bw_tf.get(c).write("0 ");
									}
								}
								bw_tf.get(c).write("\n");
								break;
						case 5: doc=docPunct.get(st);
								iterator = uniqPunct.iterator();
								while (iterator.hasNext()){
									tf=doc.wordCount.get(iterator.next());
									if(tf!=null)
									{
										tf_idf= (double)tf/doc.totalWords;
										sb.append(tf_idf+",");
										bw_tf.get(c).write(tf_idf+" ");
									}
									else
									{
										sb.append("0,");
										bw_tf.get(c).write("0 ");
									}
								}
								bw_tf.get(c).write("\n");
								break;
						case 6: doc=docCapitals.get(st);
								for(Map.Entry<String,Integer> entry : MainFile.uniqCapitals.entrySet()) {
									key = entry.getKey();
									value = entry.getValue();
									tf=doc.wordCount.get(key);
									if(tf!=null)
									{
										idf=Math.log10(totalDocs*1.0/value);
										tf_idf=tf*idf;
										sb.append(tf_idf+",");
										bw_idf.get(c).write(tf_idf+",");
										bw_tf.get(c).write(tf+",");
									}
									else
									{
										sb.append("0,");
										bw_idf.get(c).write("0,");
										bw_tf.get(c).write("0,");
									}
								}
								bw_idf.get(c).write("\n");
								bw_tf.get(c).write("\n");
								break;
					}
				}
				sb.setCharAt(sb.length()-1,'\n');
				bw.write(sb.toString());
			}
			uniqUnigrams.clear();
			docUnigrams.clear();
			uniqBigrams.clear();
			docBigrams.clear();
			uniqTrigrams.clear();
			docTrigrams.clear();
			docSentences.clear();
			uniqPOS.clear();
			docPOS.clear();
			bw.close();
			for(i=0;i<diffrentFeatures;i++)
			{
				if(bw_idf.get(i)!=null)
					bw_idf.get(i).close();
				if(bw_tf.get(i)!=null)
					bw_tf.get(i).close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processRawData()
	{
		Tika obj=new Tika();
		Reader r;
		Boolean fg=true;	//specify previous character is not space
		char dataChar;
		int data;
		try {
			r=obj.parse(file);
			data = r.read();
			while(data!=-1 && (data==' ' || data=='\t' || data=='\n' || data=='-'))
			{
				data = r.read();
			}
			while(data != -1){
				dataChar = (char) data;

				if(fg && dataChar=='-')
				{
					while(data != -1 && (dataChar=='-' || dataChar=='\n'))
					{
						data = r.read();
						dataChar = (char) data;
					}
				}
				if(dataChar==' ' || dataChar=='\t' || dataChar=='\n')
				{
					if(fg)
						bw.write(dataChar);
					fg=false;
				}
				else
				{
					bw.write(dataChar);
					fg=true;
				}
				data = r.read();				
			}
			bw.write(" ");
			r.close();
			//String st=obj.parseToString(file);
			//String filetype=obj.detect(file);
			//System.out.println(st);
			//System.out.println("================================");
			//System.out.println(filetype);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init()
	{
		uniqUnigrams=new TreeMap<String, Integer>();
		docUnigrams=new HashMap<String, DocInfo>();
		uniqBigrams=new TreeMap<String, Integer>();
		docBigrams=new HashMap<String, DocInfo>();
		uniqTrigrams=new TreeMap<String, Integer>();
		docTrigrams=new HashMap<String, DocInfo>();
		uniqCapitals=new TreeMap<String, Integer>();
		docCapitals=new HashMap<String, DocInfo>();
		uniqPOS=new TreeSet<String>();
		docPOS=new HashMap<String, DocInfo>();
		uniqPunct=new TreeSet<String>();
		docPunct=new HashMap<String, DocInfo>();
		docSentences=new HashMap<String, Integer>();
		myStem=new PorterStemmer();
		featureChoice=new ArrayList<Integer>(diffrentFeatures);

		//featureChoice.add(5);
		//featureChoice.add(4);
		//featureChoice.add(2);
		featureChoice.add(6);
	}

	void check()
	{
		destFolder="resources"+File.separator+"clustered";
		dataset="resources"+File.separator+"data.arff";	
		raw_data="resources"+File.separator+"raw_data";
		processed="resources"+File.separator+"data";
		feature_data="resources"+File.separator+"temp";
		
		inputDir=new File(raw_data);
		if(!inputDir.exists() || !inputDir.isDirectory())
		{
			System.out.println("It seems input files are not present or input data structure is not in appropriate format");
			System.out.println("Create directory structure in current project folder \"resources/raw_data\" and put all input documents int that folder.");
			System.out.println("Program terminated");
			System.exit(0);
		}
		// get all files in the input directory
		files = inputDir.listFiles();
		if(files==null)
		{
			System.out.println("Input documents are not present");
			System.out.println("Program terminated");
			System.exit(0);
		}
		for (i=0;i<files.length;i++) {
			if(files[i].length()==0)
			{
				System.out.println("Some input documents are empty. Delete those documents before running program");
				System.out.println("Program terminated");
				System.exit(0);
			}
			if(files[i].isDirectory())
			{
				System.out.println("Input folder should contain documents only");
				System.out.println("Delete folders in input folder before running program");
				System.out.println("Program terminated");
				System.exit(0);
			}
		}
		
		outputDir=new File(processed);
		if(outputDir.exists())
		{
			if(!outputDir.isDirectory())
			{
				if(!(outputDir.delete())){
					System.out.println("Failed to delete files");
					System.out.println("Program terminated");
					System.exit(0);
				}
			}
			else
			{
				//delete older files in processed directory
				recursiveDelete(outputDir);
			}
		}
		boolean status = outputDir.mkdirs();
		if(!status)
		{
			System.out.println("Failed to create directory");
			System.out.println("Program terminated");
			System.exit(0);
		}	
		
		tempDir=new File(feature_data);
		if(tempDir.exists())
		{
			if(!tempDir.isDirectory())
			{
				if(!(tempDir.delete())){
					System.out.println("Failed to delete files");
					System.out.println("Program terminated");
					System.exit(0);
				}
			}
			else
			{
				//delete older files in processed directory
				recursiveDelete(tempDir);
			}
		}
		status = tempDir.mkdirs();
		if(!status)
		{
			System.out.println("Failed to create directory");
			System.out.println("Program terminated");
			System.exit(0);
		}			
	}

	public boolean recursiveDelete(File file) {
		//to end the recursive loop
		if (!file.exists())
			return true;

		//if directory, go inside and call recursively
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				//call recursively
				recursiveDelete(f);
			}
		}
		//call delete to delete files and empty directory
		if(!file.delete())
			return false;
		return true;
	}
}
