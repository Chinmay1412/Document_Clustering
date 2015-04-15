package com.visualization;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.drew.lang.BufferReader;
import com.extraction.*;
import com.sun.javafx.font.Disposer;

/*
 * Draws word cloud for specified cluster
 * Output is stored as image named 'wordclud_<cluster_no>.png'
 */
public class WordCloud {
	int j;
	public static HashMap<String, Integer> wordmap = new HashMap<String, Integer>();
	public static List<JFrame> frames = new ArrayList<JFrame>();
	String patternStr = "[A-Za-z][A-Za-z_]*(?=[-\\s!\"#$%&\'()*+,./:;<=>?@\\[\\]\\^_`{|}~])";
	Pattern pattern;
	Matcher match;
	String st;
	int maxLength=20;
	BufferedReader br;

	public WordCloud() {
		// TODO Auto-generated constructor stub
		pattern=Pattern.compile(patternStr);
		wordmap = new HashMap<String, Integer>(200);
	}

	public static void initUI(String cluster_no) throws IOException {

		JFrame frame = new JFrame(WordCloud.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		Cloud cloud = new Cloud();
		panel.setBackground(Color.WHITE);
		panel.setOpaque(false);
		cloud.setMaxWeight(12.0);
		cloud.setMaxTagsToDisplay(100);
		cloud.setMinWeight(1.0);

		for(Map.Entry<String, Integer> entry : wordmap.entrySet() ){
			String key = entry.getKey().toString();
			Integer value = entry.getValue();
			// for (int i = value; i > 0; i--) {
				cloud.addTag(new Tag(key,value));
				// }
		}
		for (Tag tag : cloud.tags()) {
			final JLabel label = new JLabel(tag.getName());
			label.setOpaque(false);
			label.setFont(label.getFont().deriveFont((float) tag.getWeight() * 10));
			panel.add(label);
		}
		frame.add(panel);
		frame.setSize(1000, 800);
		//frame.pack();
		//frame.setLocation(-5000, 6000);
		frame.setVisible(true);
		BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		frame.print(graphics);
		graphics.dispose();
		/*Graphics gr = image.getGraphics();
        frame.printAll(gr);
        gr.dispose();*/
		String fileName = new String("wordcloud_"+cluster_no + ".png");
		ImageIO.write(image, "png", new File(fileName));
		frame.dispose();
	}

	public void draw(String[] args)
	{
		try {
			String folder = args[1]+File.separator+args[0];
			wordmap.clear();
			File[] files = new File(folder).listFiles();
			if (files != null) {
				for (File child : files) {
					String line;
					br = new BufferedReader(new FileReader(child));
					while ((line = br.readLine()) != null) {
						match = pattern.matcher(line+"\n");
						while (match.find()) {
							st=match.group().toLowerCase();
							if(StopWords.sw.get(st)!=null)
								continue;
							if(st.length()>=maxLength)
								continue;
							//st=MainFile.myStem.stem(st);                     			
							if(wordmap.get(st)==null)
								wordmap.put(st,1);
							else
								wordmap.put(st, wordmap.get(st)+1);
						}
					}
					br.close();
				}
				initUI(args[0]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
