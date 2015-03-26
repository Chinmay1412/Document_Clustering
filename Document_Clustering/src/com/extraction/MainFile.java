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
import java.util.Map;
import java.util.TreeMap;

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

import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

class DocInfo
{
	HashMap<String, Integer> wordCount;
	Integer totalWords;
	DocInfo()
	{
		this.wordCount=new HashMap<String, Integer>();
		this.totalWords=0;
	}
}

public class MainFile {

	File inputDir,file,outputDir;		
	File[] files;
	BufferedWriter bw; 
	String st;
	static TreeMap<String, Integer> uniqWords;
	static HashMap<String, DocInfo> docWords;
	static Integer totalUniqWords=0,totalDocs=0;
	ProcessAnnotations objAnnotation;
	static MainFile obj;
	DocInfo doc;
	int i;
	StringBuilder sb,nullsb;
	static PorterStemmer myStem;
	static String destFolder="resources/clustered",dataset="resources/data.arff";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long lStartTime = new Date().getTime();
		obj=new MainFile();
		obj.init();
		obj.process();
		long lEndTime = new Date().getTime();
		long difference = lEndTime - lStartTime;
		System.out.println("Elapsed milliseconds: " + difference);
	}

	public void process()
	{
		try{
			inputDir=new File("resources/raw_data");
			outputDir=new File("resources/data");

			// get all files in the input directory
			files = inputDir.listFiles();
			totalDocs=files.length;

			if (files == null) {
				System.out.println("No files to process");
			} else {
				//delete older files in op directory
				FileUtils.deleteAllFiles(outputDir); 

				String para[]=new String[2];
				para[0]="descriptor/Unigram.xml"; 	//Name of descriptor

				// process documents
				for (i = 0; i < files.length; i++) {
					if (!files[i].isDirectory()) {
						st=outputDir.getPath()+"/"+files[i].getName().replaceFirst("[.][^.]+$", "")+".txt";
						file = new File(files[i].getPath());				//input file name
						bw = new BufferedWriter(new FileWriter(st));		//output file name
						writeContent();	
						bw.close();
						para[1]=st;					//Data folder
						objAnnotation.analyze(para);

						/*for(Map.Entry<String,Integer> entry : MainFile.uniqWords.entrySet()) {
							  String key = entry.getKey();
							  Integer value = entry.getValue();
							  System.out.println(key + " => " + value);
							}

						for(Map.Entry<String, DocInfo> entry : MainFile.docWords.entrySet()) {
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
				}
				if(totalUniqWords!=0)
				{
					datasetCreation();
					clusterData();
				}
				else
					System.out.println("Not a proper dataset");
			}

		} catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clusterData()
	{
		DataSource source;
		int no_cluster=4;
		try {
			source = new DataSource(dataset);

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
				file=new File(destFolder+"/"+i);
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
				Files.copy(files[i].toPath(),(new File(destFolder+"/"+no+"/"+files[i].getName())).toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println(files[i]+"-->"+no);
				i++;
			}
			System.out.println(i);
			System.out.println(model);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void recursiveDelete(File file) {
		//to end the recursive loop
		if (!file.exists())
			return;

		//if directory, go inside and call recursively
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				//call recursively
				recursiveDelete(f);
			}
		}
		//call delete to delete files and empty directory
		file.delete();
	}

	public void datasetCreation()
	{
		try {
			sb=new StringBuilder(1000);
			nullsb=new StringBuilder(1000);
			//file for clustering in required format
			bw = new BufferedWriter(new FileWriter(dataset));
			bw.write("@relation document\n\n");

			for(i=0;i<totalUniqWords;i++)
			{
				bw.write("@attribute uw"+i+" numeric\n");
				nullsb.append("0,");
			}
			nullsb.setCharAt(nullsb.length()-1,'\n');
			bw.write("\n@data\n");

			String key;
			Integer value,tf;
			Double tf_idf,idf;
			for (i = 0; i < files.length; i++) {
				sb.setLength(0);
				st=files[i].getName().replaceFirst("[.][^.]+$", "")+".txt";
				//System.out.println(docWords.size());
				doc=docWords.get(st);
				if(doc==null || doc.totalWords==0)
				{
					bw.write(nullsb.toString());
					continue;
				}

				for(Map.Entry<String,Integer> entry : MainFile.uniqWords.entrySet()) {
					key = entry.getKey();
					value = entry.getValue();
					tf=doc.wordCount.get(key);
					if(tf!=null)
					{
						idf=Math.log(totalDocs*1.0/value);
						tf_idf=tf*idf;
						sb.append(tf_idf+",");
					}
					else
					{
						sb.append("0,");
					}
				}
				sb.setCharAt(sb.length()-1,'\n');
				bw.write(sb.toString());
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeContent()
	{
		Tika obj=new Tika();
		Reader r;
		Boolean fg=true;
		try {
			r=obj.parse(file);
			int data = r.read();
			while(data!=-1 && (data==' ' || data=='\t' || data=='\n'))
			{
				data = r.read();
			}
			while(data != -1){
				char dataChar = (char) data;

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
		uniqWords=new TreeMap<String, Integer>();
		docWords=new HashMap<String, DocInfo>();
		objAnnotation=new ProcessAnnotations();
		myStem=new PorterStemmer();
	}
}
