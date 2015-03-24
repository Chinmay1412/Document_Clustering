package com.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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

public class MainFile {

	static File file;
	static BufferedWriter bw; 

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			file = new File("sample.pdf");		//input file name
			bw = new BufferedWriter(new FileWriter("op.txt"));		//output file name
			long lStartTime = new Date().getTime();
			printcontent1();
			//getmetadata();
			//languageDetect();
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

	public static void printcontent2()
	{
		try{		 
			//parse method parameters
			Tika tika=new Tika();
			Parser parser = new AutoDetectParser();
			ContentHandler handler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			FileInputStream inputstream = new FileInputStream(file);
			ParseContext context = new ParseContext();

			String mimeType = tika.detect(inputstream);
			metadata.set(HttpHeaders.CONTENT_TYPE, mimeType);

			//parsing the file
			parser.parse(inputstream, handler, metadata, context);
			inputstream.close();
			//bw.write(handler.toString());
		//	System.out.println("File content : " + handler.toString());
		}catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getmetadata()
	{
		//Parser method parameters
		Parser parser = new AutoDetectParser();
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		FileInputStream inputstream;
		ParseContext context = new ParseContext();
		try {
			inputstream = new FileInputStream(file);
			parser.parse(inputstream, handler, metadata, context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(handler.toString());

		//getting the list of all meta data elements 
		String[] metadataNames = metadata.names();

		for(String name : metadataNames) {		        
			System.out.println(name + ": " + metadata.get(name));
		}
	}

	public static void languageDetect()
	{
		try{
			//Parser method parameters
			Parser parser = new AutoDetectParser();
			BodyContentHandler handler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			FileInputStream content = new FileInputStream(file);

			//Parsing the given document
			parser.parse(content, handler, metadata, new ParseContext());

			LanguageIdentifier object = new LanguageIdentifier(handler.toString());
			System.out.println("Language name :" + object.getLanguage());
		} catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
