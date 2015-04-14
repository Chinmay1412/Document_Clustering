package com.features;

/* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.FloatArrayFS;
import org.apache.uima.cas.IntArrayFS;
import org.apache.uima.cas.StringArrayFS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.XMLInputSource;
import com.extraction.*;

/**
* A simple example of how to extract information from the CAS. This example retrieves all
* annotations of a specified type from a CAS and prints them (along with all of their features) to
* a PrintStream.
* 
* 
*/
public class Unigram {

	String word;
	Integer wordcnt,doccnt;
	Pattern pattern;
	Matcher matcher;
	boolean matches;
	int maxLength=20;
	DocInfo doc;
	
	public Unigram()
	{
		pattern = Pattern.compile("[A-Za-z_]+");
	}

	public void processAnnotations(CAS aCAS, Type aAnnotType, PrintStream aOut) {
		// get iterator over annotations
		FSIterator iter = aCAS.getAnnotationIndex(aAnnotType).iterator();
		
		doc=MainFile.docUnigrams.get(MainFile.currFilename);
		// iterate
		while (iter.isValid()) {
			FeatureStructure fs = iter.get();
			word=getFS(fs, aCAS, 0, aOut);
			if(word!=null)
			{
				doc.totalWords=doc.totalWords+1;
				wordcnt=doc.wordCount.get(word);
				if(wordcnt==null)
				{
					doc.wordCount.put(word, 1);
					doccnt=MainFile.uniqUnigrams.get(word);
					if(doccnt==null)
					{
						MainFile.uniqUnigrams.put(word, 1);
						MainFile.totalFeatures=MainFile.totalFeatures+1;
					}
					else
					{
						doccnt=doccnt+1;
						MainFile.uniqUnigrams.put(word, doccnt);
					}    			  
				}
				else
				{
					wordcnt=wordcnt+1;
					doc.wordCount.put(word, wordcnt);
				}
			}
			iter.moveToNext();
		}
	}

	public String getFS(FeatureStructure aFS, CAS aCAS, int aNestingLevel, PrintStream aOut) {
		Type stringType = aCAS.getTypeSystem().getType(CAS.TYPE_NAME_STRING);

		if(aFS.getType().getName().equalsIgnoreCase("uima.tcas.DocumentAnnotation"))
			return null;
	

		if (aFS instanceof AnnotationFS) {
			AnnotationFS annot = (AnnotationFS) aFS;
			String st = new String(annot.getCoveredText());
			//if(st.contains("-\n"))
			//	st=st.replace("-\n", "");
			
			matcher = pattern.matcher(st);
			matches = matcher.matches();
			if(matches!=true)
				return null;
			
			if(StopWords.sw.get(st)==null)
			{
				if(st.length()>=maxLength)
					return null;
				st=MainFile.myStem.stem(st);
				return st;
			}
			else
				return null;
		}
		return null;
	}

	/**
	 * Main program for testing this class. There are two required arguments - the path to the XML
	 * descriptor for the TAE to run and an input file. Additional arguments are Type or Feature names
	 * to be included in the ResultSpecification passed to the TAE.
	 */
	public void analyze(String[] args) {
		try {
			File taeDescriptor = new File(args[0]);
		
			// get Resource Specifier from XML file or TEAR
			XMLInputSource in = new XMLInputSource(taeDescriptor);
			ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);

			// create Analysis Engine
			AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(specifier);
			// create a CAS
			CAS cas = ae.newCAS();

			// build ResultSpec if Type and Feature names were specified on commandline
			ResultSpecification resultSpec = null;
			if (args.length > 2) {
				resultSpec = ae.createResultSpecification(cas.getTypeSystem());
				for (int i = 2; i < args.length; i++) {
					if (args[i].indexOf(':') > 0) // feature name
					{
						resultSpec.addResultFeature(args[i]);
					} else {
						resultSpec.addResultType(args[i], false);
					}
				}
			}

			// read contents of file
			String document =new String(MainFile.currentDoc.toLowerCase());

			if(document!=null)
			{
				// send doc through the AE
				cas.setDocumentText(document);
				ae.process(cas, resultSpec);
	
				Type annotationType = cas.getTypeSystem().getType(CAS.TYPE_NAME_ANNOTATION);
				processAnnotations(cas, annotationType, System.out);
			}
			
			// destroy AE
			ae.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

