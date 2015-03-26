package com.extraction;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
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

/**
 * A simple example of how to extract information from the CAS. This example retrieves all
 * annotations of a specified type from a CAS and prints them (along with all of their features) to
 * a PrintStream.
 * 
 * 
 */
public class ProcessAnnotations {

	Pattern pattern = Pattern.compile("[a-zA-Z]+");
	String unigram;
	DocInfo doc;
	Integer wordcnt,doccnt;

	/**
	 * Prints all Annotations of a specified Type to a PrintStream.
	 * 
	 * @param aCAS
	 *          the CAS containing the FeatureStructures to print
	 * @param aAnnotType
	 *          the Type of Annotation to be printed
	 * @param aOut
	 *          the PrintStream to which output will be written
	 */
	public void processAnnotations(CAS aCAS, Type aAnnotType, PrintStream aOut) {
		// get iterator over annotations
		FSIterator iter = aCAS.getAnnotationIndex(aAnnotType).iterator();

		// iterate
		while (iter.isValid()) {
			FeatureStructure fs = iter.get();
			unigram=printFS(fs, aCAS, 0, aOut);
			if(unigram!=null)
			{
				doc.totalWords=doc.totalWords+1;
				wordcnt=doc.wordCount.get(unigram);
				if(wordcnt==null)
				{
					doc.wordCount.put(unigram, 1);
					doccnt=MainFile.uniqWords.get(unigram);
					if(doccnt==null)
					{
						MainFile.uniqWords.put(unigram, 1);
						MainFile.totalUniqWords=MainFile.totalUniqWords+1;
					}
					else
					{
						doccnt=doccnt+1;
						MainFile.uniqWords.put(unigram, doccnt);
					}    			  
				}
				else
				{
					wordcnt=wordcnt+1;
					doc.wordCount.put(unigram, wordcnt);
				}
			}
			iter.moveToNext();
		}
	}

	/**
	 * Prints a FeatureStructure to a PrintStream.
	 * 
	 * @param aFS
	 *          the FeatureStructure to print
	 * @param aCAS
	 *          the CAS containing the FeatureStructure
	 * @param aNestingLevel
	 *          number of tabs to print before each line
	 * @param aOut
	 *          the PrintStream to which output will be written
	 */
	public String printFS(FeatureStructure aFS, CAS aCAS, int aNestingLevel, PrintStream aOut) {
		Type stringType = aCAS.getTypeSystem().getType(CAS.TYPE_NAME_STRING);

		//printTabs(aNestingLevel, aOut);
		if(aFS.getType().getName().equalsIgnoreCase("uima.tcas.DocumentAnnotation"))
			return null;
		//  aOut.println(aFS.getType().getName());


		// if it's an annotation, print the first 64 chars of its covered text
		if (aFS instanceof AnnotationFS) {
			AnnotationFS annot = (AnnotationFS) aFS;
			String st = new String(annot.getCoveredText().toLowerCase());
			Matcher matcher = pattern.matcher(st);
			boolean matches = matcher.matches();
			if(matches==true)
			{
				if(StopWords.sw.get(st)==null)
				{
					st=MainFile.myStem.stem(st);
					return st;
				}
				else
					return null;
			}
		}
		return null;
	}

	/**
	 * Main program for testing this class. Ther are two required arguments - the path to the XML
	 * descriptor for the TAE to run and an input file. Additional arguments are Type or Feature names
	 * to be included in the ResultSpecification passed to the TAE.
	 */
	public void analyze(String[] args) {
		try {
			File taeDescriptor = new File(args[0]);
			File inputFile = new File(args[1]);

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
			String document = FileUtils.file2String(inputFile);

			// send doc through the AE
			cas.setDocumentText(document);
			ae.process(cas, resultSpec);

			doc=new DocInfo();

			// print results
			Type annotationType = cas.getTypeSystem().getType(CAS.TYPE_NAME_ANNOTATION);
			processAnnotations(cas, annotationType, System.out);

			if(doc.totalWords!=0)
				MainFile.docWords.put(inputFile.getName(), doc);

			// destroy AE
			ae.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
