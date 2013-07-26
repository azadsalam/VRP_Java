import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.plaf.metal.MetalIconFactory.FileIcon16;

public class Solver 
{
	String inputFileName = "in.txt";
	String outputFileName = "out.txt";
	
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
			output = new PrintWriter(System.out);//for console output
			//output = new PrintWriter(outputFile);//for file output
						
			
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
		problemInstance.print();
		
		Individual individual = new Individual(problemInstance);
		individual.print();
		
		individual.mutatePeriodAssignment(0);
		individual.calculateFitness();
		individual.print();
		
		individual.mutatePeriodAssignment(1);
		individual.calculateFitness();
		individual.print();
		
		output.close();
		
	}
}
