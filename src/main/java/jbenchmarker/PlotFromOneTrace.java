package jbenchmarker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.ImageTerminal;
import com.panayotis.gnuplot.utils.Debug;

public class PlotFromOneTrace {

	public static void main(String[] args) throws Exception {

		ArrayList<String> algoList = new ArrayList<String>();
		algoList.add("RGA");
		algoList.add("RgaS");
		algoList.add("RGATreeList");
		algoList.add("RgaTreeSplitBalanced");
		algoList.add("LogootS");
		algoList.add("LogootSplitAVL");
		algoList.add("Treedoc");
		
		String file = "TraceBPB20000-0";
		String path = System.getProperty("user.dir")+ File.separator+"ResultTest"+ File.separator ;

		int pas = 100;
		plotAWholeGraph(algoList, pas, path, file, true);
		plotAWholeGraph(algoList, pas, path, file, false);
	}

	public static void plotAWholeGraph(ArrayList<String> algoList, int pas, String path, String file, boolean plotLocal ) throws FileNotFoundException{
		JavaPlot p = new JavaPlot();
		path = path +File.separator+ file + File.separator;
		//p.set("term", "x11 persist");
		for (String el : algoList){		
			String name = el + "-"+ file;
			Scanner scanner=new Scanner(new File( path + name + File.separator + name +"-usr.res"));
			int nbLine = 0;
			while (scanner.hasNextLine()) {
				nbLine++;
				scanner.nextLine();
			}
			scanner.close();
			double[][] tab = new double[nbLine/pas][2];
			if(plotLocal){
			scanner=new Scanner(new File( path + name + "//" + name +"-gen.res"));
			} else {
			scanner=new Scanner(new File( path + name + "//" + name +"-usr.res"));
			}
			
			int abs = 0 ;
			int sum = 0;
			while (scanner.hasNextLine()) {	
				String line = scanner.nextLine();
				StringTokenizer st = new StringTokenizer(line, "	");
				int i = 0 ;
				while ( i < st.countTokens() - 1){
					st.nextToken();
				}
				if (pas==1){
					tab[abs][0]= abs;
					tab[abs][1]= Double.parseDouble(st.nextToken());
				} else if (abs%pas==0){
					tab[abs/pas][0]= abs;
					tab[abs/pas][1]= sum/pas;
					sum=0;
				
				} else {
					sum+=Double.parseDouble(st.nextToken());
				}
				abs++;
			}
			if (plotLocal){
				p=plotOneGraph(p, "Local performance", "Number of operations", " Local execution time (ns)", tab, el);
			}	else {
				p=plotOneGraph(p, "Remote performance", "Number of operations", " Remote integration time (ns)", tab, el);
			}
			scanner.close();	
		}
		
		p.plot();
	}

	private static JavaPlot plotOneGraph(JavaPlot p, String title, String xName, String yName, double[][] dataPlot, String dataName){

		p.setTitle(title, "Arial", 14);
		p.getAxis("x").setLabel(xName, "Arial", 12);
		p.getAxis("y").setLabel(yName, "Arial", 12);
		p.getAxis("y").setLogScale(true);
		p.getAxis("x").setBoundaries(0, java.lang.Math.round(dataPlot[dataPlot.length-2][0]/1000)*1000);
		//p.getAxis("y").setBoundaries(0, 50000);
		p.setKey(JavaPlot.Key.OUTSIDE);
		PlotStyle myPlotStyle = new PlotStyle();
		myPlotStyle.setStyle(Style.LINES);
		DataSetPlot s = new DataSetPlot(dataPlot);
		s.setTitle(dataName);
		myPlotStyle.setLineWidth(2);
		s.setPlotStyle(myPlotStyle);
		p.addPlot(s);
		return p;
	}



}
