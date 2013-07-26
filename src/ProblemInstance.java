import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;


public class ProblemInstance 
{
	Scanner in;
	public PrintWriter out;
	
	int depotCount,customerCount,periodCount,nodeCount,vehicleCount;
	int vehicleAllocation[]; // kon depot er koyta kore vehicle
	double costMatrix[][];
	double travellingTimeMatrix[][];
	int depotAllocation[]; // kon vehicle kon depot er under a
	double loadCapacity[]; // kon vehicle max koto load nite parbe
	double serviceTime[];  // kon client kototuk time lage service pete
	double demand[]; 	  // kon client koto demand
	double timeConstraintsOfVehicles[][]; // periodCount * vehicleCount

	
	int frequencyAllocation[];

	
	public ProblemInstance(Scanner input,PrintWriter output) throws FileNotFoundException
	{
		int i,j;
		// TODO Auto-generated constructor stub
		this.in = input;
		this.out = output;
		
		
		periodCount = in.nextInt();
		
		
		escapeComment(in);
		
		depotCount = in.nextInt();
		escapeComment(in);

		vehicleCount = in.nextInt();
		escapeComment(in);
		
		
		
		//vehicle per depot
		
		vehicleAllocation = new int[depotCount];
		depotAllocation = new int[vehicleCount];
		
		int vehicleCursor = 0;

		for( j=0;j<depotCount;j++)
		{
			vehicleAllocation[j] = in.nextInt();

			for( i=0;i<vehicleAllocation[j];i++)
			{
				depotAllocation[vehicleCursor]=j;
				vehicleCursor++;
			}
		}
		escapeComment(in);


		//String tmp = in.nextLine();
		//out.println("HERE ->"+tmp);
		
		//capacity of vehicle
		loadCapacity = new double[vehicleCount];
		for( i=0;i<vehicleCount;i++)
		{
			loadCapacity[i] = in.nextDouble();
		}
		escapeComment(in);

		

		//time constraints
        escapeComment(in); // escape the line "; t(total period) lines containg  v (total vehicle)
                         //values each referring maximum time limit for that day for that vehicle (NEW)"

        //read periodCount lines
        timeConstraintsOfVehicles = new double[periodCount][vehicleCount];
        for(i=0;i<periodCount;i++)
        {
            for(j=0;j<vehicleCount;j++)
            {
                timeConstraintsOfVehicles[i][j] = in.nextDouble();
            }
        }
        
        
		//CLIENT COUNT
		customerCount = in.nextInt();
		escapeComment(in);

		//frequency
		frequencyAllocation = new int[customerCount];
		for( i=0 ; i<customerCount; i++)
			frequencyAllocation[i]= in.nextInt();
		escapeComment(in);


		//service time
		serviceTime = new double[customerCount];
		for( i=0; i<customerCount; i++)
		{
			serviceTime[i]=in.nextDouble();
		}
		escapeComment(in);

		//demand
		demand = new double[customerCount];
		for( i=0 ; i<customerCount; i++)
		{
			demand[i] = in.nextDouble();
		}
		escapeComment(in);

		// cost matrix
		escapeComment(in); // escapes the line ";cost matrix"
		nodeCount = customerCount+depotCount;
		costMatrix = new double[nodeCount][nodeCount];

		int row,col;

		for( row=0;row<nodeCount;row++)
			for( col=0;col<nodeCount;col++)
				costMatrix[row][col] = in.nextDouble();

		//for now travel time == cost
		
		
		travellingTimeMatrix = costMatrix;
		//print();
	}
	
	public void print() 
	{
		int i,j;
		out.println("Problem Instance : ");
		out.println("Period : "+ periodCount);
		out.println("Depot : " + depotCount);
		out.println("Vehicle Count : "+vehicleCount);
		
		out.print("Vehicle per depot :");
		for(i=0;i<depotCount;i++) out.print(" "+vehicleAllocation[i]);
		out.print("\n");
		
		out.print("Depot allocation per depot :");
		for(i=0;i<vehicleCount;i++) out.print(" "+depotAllocation[i]);
		out.print("\n");

		out.print("Load capacity per vehicle :");
		for(i=0;i<vehicleCount;i++) out.print(" "+loadCapacity[i]);
		out.print("\n");
		
		out.print("Time constraints per vehicle :\n");
		for(i=0;i<periodCount;i++)
        {
            for(j=0;j<vehicleCount;j++)
            {
            	out.print(timeConstraintsOfVehicles[i][j]+" ");
            }
            out.print("\n");
        }
		
		
		out.print("Load capacity per vehicle :");
		for(i=0;i<vehicleCount;i++) out.print(" "+loadCapacity[i]);
		out.print("\n");
		
		
		
		
		out.println("Clients : "+customerCount);

		out.print("Frequency allocation : ");
		for( i =0;i<customerCount ;i++) out.print(frequencyAllocation[i] + " ");
		out.println();

		out.print("Service Time : ");
		for( i =0;i<customerCount ;i++) out.print( serviceTime[i] + " ");
		out.println();

		out.print ("Demand (load) : ");
		for( i =0;i<customerCount ;i++) out.print(demand[i] + " ");
		out.println();

		out.print("Printing cost matrix : \n");

		int row,col;
		for( row=0;row<nodeCount;row++)
		{
			for( col=0;col<nodeCount;col++)
				out.print(costMatrix[row][col]+" ");
			out.println();
		}
		out.println();
		
		
				
	}
	
	public void escapeComment(Scanner input) 
	{
		input.nextLine();
		//String comment = input.nextLine();
		//System.out.println("COMMENT : "+comment);
	}
	
	public PrintWriter getPrintWriter() {
		return this.out;
		
	}
	
	
	
	
	
}
