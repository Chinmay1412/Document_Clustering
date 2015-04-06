package com.extraction;

import java.util.HashMap;

public class DocInfo
{
	public HashMap<String, Integer> wordCount;
	public Integer totalWords;
	public DocInfo()
	{
		this.wordCount=new HashMap<String, Integer>();
		this.totalWords=0;
	}
}
