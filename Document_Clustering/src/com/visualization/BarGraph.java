package com.visualization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.extraction.*;

public class BarGraph extends Application {

	Stage stage;
	BufferedReader br;
	String line;
	StringTokenizer st;
	int i,j;
	List<String> parameters;
	ArrayList<String> key;
	ArrayList<Number> value = null;
	
	@Override
	public void start(Stage arg0) throws Exception {
		// TODO Auto-generated method stub
		parameters = getParameters().getRaw();
		//System.out.println(parameters);
		key = new ArrayList<String>();
				
		stage=arg0;
		stage.setTitle("Cluster Description");
		final CategoryAxis xAxis = new CategoryAxis();
		final NumberAxis yAxis = new NumberAxis();
		final BarChart<String,Number> bc = new BarChart<String,Number>(xAxis,yAxis);

		bc.setTitle("Cluster "+parameters.get(1)+" Summary");
		bc.setAnimated(false);
		xAxis.setLabel("Feature");
		yAxis.setLabel("Value");

		XYChart.Series<String, Number> series = new XYChart.Series<>();
		switch(Integer.parseInt(parameters.get(0))){
			case 0: series.setName("Unigram");
					makeData(parameters.get(2)+File.separator+"unigrams.txt",parameters.get(2)+File.separator+"unigram_tf.txt",",");
					break;
			case 1: series.setName("Bigram");
					makeData(parameters.get(2)+File.separator+"bigrams.txt",parameters.get(2)+File.separator+"bigram_tf.txt",",");
					break;
			case 2: series.setName("Trigram");
					makeData(parameters.get(2)+File.separator+"trigrams.txt",parameters.get(2)+File.separator+"trigram_tf.txt",",");
					break;
			case 3: series.setName("#Sentences");
					makeData(parameters.get(2)+File.separator+"sentences.txt",parameters.get(2)+File.separator+"sentence_tf.txt","\n");
					break;
			case 4: series.setName("POS frequency per total POS tags");
					makeData(parameters.get(2)+File.separator+"POS.txt",parameters.get(2)+File.separator+"POS_tf.txt"," ");
					break;
			case 5: series.setName("Punctuations frequency per total Punctuations");
					makeData(parameters.get(2)+File.separator+"punctuations.txt",parameters.get(2)+File.separator+"punctuation_tf.txt"," ");
					break;
			case 6: series.setName("Capitalization");
					makeData(parameters.get(2)+File.separator+"capitals.txt",parameters.get(2)+File.separator+"capital_tf.txt",",");
					break;
		}
		
		for( int k=0; k < key.size(); k++){
			series.getData().add(new XYChart.Data(key.get(k), value.get(k)));
		}

		Scene scene  = new Scene(bc,800,600);
		bc.getData().addAll(series);

		stage.setScene(scene);
		//stage.show();
		WritableImage snapShot = scene.snapshot(null);
		ImageIO.write(SwingFXUtils.fromFXImage(snapShot,null), "png", new File("test.png"));
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	          public void handle(WindowEvent we) {
	              //System.out.println("Stage is closing");
	              stage.close();
	              Platform.exit();
	          }
	      });   
		stage.getOnCloseRequest().handle(new WindowEvent(stage,WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	public void caller(String[] args)
	{
		//Automatically call start function
		launch(args);
	}
	
	void makeData(String keyFile,String valueFile,String valueSeperator)
	{
		try {
			i=0;
			br=new BufferedReader(new FileReader(keyFile));
			while ((line = br.readLine()) != null) {
				key.add(line); 
				i++;
			}
			br.close();
			value=new ArrayList<Number>(i);
			for(j=0;j<i;j++)
				value.add(0);
			i=0;
			br=new BufferedReader(new FileReader(valueFile));
			while ((line = br.readLine()) != null) {
				if(MainFile.cluster.get(Integer.parseInt(parameters.get(1))).contains(i))
				{
					st=new StringTokenizer(line,valueSeperator);
					j=0;
					while(st.hasMoreTokens()) {
						value.set(j,Double.parseDouble(st.nextToken())+value.get(j).doubleValue());
						j++;
					}							
				}
				i++;
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}					
	}
}
