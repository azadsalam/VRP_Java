import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.plaf.metal.MetalIconFactory.FileIcon16;

public class Solver 
{
	String inputFileName = "in1.txt";
	String outputFileName = "out1.txt";
	
	File inputFile,outputFile;	
	Scanner input;
	PrintWriter output;
	
	
		
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
		
		Algo25_50_25_elitist ga = new Algo25_50_25_elitist(problemInstance);
		ga.run();
		
		output.close();
		
	}
}
