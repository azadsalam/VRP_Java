import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.plaf.metal.MetalIconFactory.FileIcon16;

public class Solver 
{
	String inputFileName = "in8.txt";
	String outputFileName = "out8.txt";
	
	File inputFile,outputFile;	
	Scanner input;
	PrintWriter output;
	
	public static ExportToCsv exportToCsv;
		
	ProblemInstance problemInstance;
	public void initialise() 
	{
		try
		{
			inputFile = new File(inputFileName);
			input = new Scanner(inputFile);
			
			outputFile = new File(outputFileName);
			//output = new PrintWriter(System.out);//for console output
			output = new PrintWriter(outputFile);//for file output
						
			
			int testCases = input.nextInt(); 
			input.nextLine(); // escaping comment
			// FOR NOW IGNORE TEST CASES, ASSUME ONLY ONE TEST CASE
			output.println("Test cases (Now ignored): "+ testCases);

			
			exportToCsv = new ExportToCsv(inputFileName);
			//exportToCsv.createCSV(10);
			problemInstance = new ProblemInstance(input,output);
			
			
		}
		catch (FileNotFoundException e)
		{
			System.out.println("FILE DOESNT EXIST !! EXCEPTION!!\n");
		}
		catch (Exception e) 
		{
			// TODO: handle exception
			System.out.println("EXCEPTION!!\n");
			e.printStackTrace();
		}
	}
	public void solve() 
	{
	//	problemInstance.print();
		
		double min = 0xFFFFFF;
		double max = -1;
		double sum = 0;
		double avg;
		int feasibleCount=0;
		
		for(int i=0; i<100; i++)
		{
			Algo25_50_25_elitist ga = new Algo25_50_25_elitist(problemInstance);
			
			Individual sol = ga.run();
			
			if(sol.isFeasible==true)
			{
				feasibleCount++;
				sum += sol.cost;
				if(sol.cost>max) max = sol.cost;
				if(sol.cost<min) min = sol.cost;
			}
				
		}
		avg = sum/feasibleCount;
		
		
		System.out.format("Min : %f Avg : %f  Max : %f Feasible : %d \n",min,avg,max,feasibleCount);
		
		//exportToCsv.createCSV();
		
		output.close();
		
	}
}
