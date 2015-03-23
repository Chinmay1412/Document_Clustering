package com.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

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
import org.apache.uima.util.Progress;

public class MainFile extends org.apache.uima.collection.CollectionReader_ImplBase{

	static File file;
	static BufferedWriter bw; 
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			file = new File("raw_data/sample.pdf");							//input file name
			bw = new BufferedWriter(new FileWriter("data/op.txt"));		//output file name
			long lStartTime = new Date().getTime();
			printcontent1();
			String tp[]=new String[2];
			tp[0]="desc/Name_Recognizer.xml"; 
			tp[1]="data/op.txt";
			PrintAnnotations.check(tp);
			long lEndTime = new Date().getTime();
			long difference = lEndTime - lStartTime;
		 	System.out.println("Elapsed milliseconds: " + difference);
		} catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printcontent1()
	{
		Tika obj=new Tika();
		Reader r;
		try {
			r=obj.parse(file);
			int data = r.read();
			while(data!=-1 && (data==' ' || data=='\t' || data=='\n'))
			{
				data = r.read();
			}
			while(data != -1){
				char dataChar = (char) data;
				bw.write(dataChar);
				data = r.read();
			}
			bw.close();
			r.close();
			//String st=obj.parseToString(file);
			String filetype=obj.detect(file);
			//System.out.println(st);
			//System.out.println("================================");
			System.out.println(filetype);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void getNext(CAS arg0) throws IOException, CollectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		// TODO Auto-generated method stub
		return false;
	}
}
