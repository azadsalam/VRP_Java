import java.io.PrintWriter;
import java.util.Scanner;


public class Individual 
{
	boolean periodAssignment[][];
	int permutation[][];
	int routePartition[];
	double cost;
	
	double costWithPenalty;
//	Utility utility;
	boolean isFeasible;
//	boolean feasibilitySet;

	double loadViolation[][];
	double totalLoadViolation;

	//double totalRouteTime;
	
	double totalRouteTimeViolation;
	
	ProblemInstance problemInstance;


	public Individual()
	{
		cost = -1;
		costWithPenalty = -1;
		//feasibilitySet = false;
		//isFeasible = false;	
	}
	
	
	public void initialise() 
	{
		// TODO Auto-generated method stub

		int i,j;
		
		for( i=0;i<problemInstance.periodCount;i++)
		{
			// initially every permutation is identity permutation
			for( j=0;j<problemInstance.customerCount;j++)
			{
				permutation[i][j] = j;
			}
		}
		
		// NOW INITIALISE WITH VALUES

		//initialize period assignment

		int freq,allocated,random,tmp;

		//Randomly allocate period to clients equal to their frequencies
		for(int client=0; client < problemInstance.customerCount; client++)
		{
			freq = problemInstance.frequencyAllocation[client];
			allocated=0;

			while(allocated!=freq)
			{
				random = Utility.randomIntInclusive(problemInstance.periodCount-1);
				
				if(periodAssignment[random][client]==false)
				{
					periodAssignment[random][client]=true;
					allocated++;
				}
			}
		}
		
		
		//initialize permutation map - KNUTH SHUFFLE
		for(int period=0; period < problemInstance.periodCount;period++)
		{
			//apply knuths shuffle
			for( i = problemInstance.customerCount -1 ;i>0 ;i-- )
			{
				j = Utility.randomIntInclusive(i);

				
				if(i==j)continue;

				tmp = permutation[period][i];
				permutation[period][i] = permutation[period][j];
				permutation[period][j] = tmp;
			}
		}
		
		//NEED TO GENERATE #vehicle-1 (not distinct - distinct) random numbers in increasing order from [0,#customer - 1]
		// DEVICE some faster and smarter algorithm

		// route for vehicle i is  [ routePartition[i-1]+1 , routePartition[i] ]
		// given that routePartition[i-1]+1 <= routePartition[i]

		//bool found;
		allocated = 0;
		while(allocated != problemInstance.vehicleCount-1)
		{
			random = Utility.randomIntInclusive(problemInstance.customerCount-1);

			routePartition[allocated]=random;
			sort(random,allocated);
			allocated++;
		}
		routePartition[problemInstance.vehicleCount-1] = problemInstance.customerCount-1;

		calculateCost();

	}
	
	public Individual(ProblemInstance problemInstance)
	{
		this.problemInstance = problemInstance;
		
		// ALLOCATING periodCount * customerCount Matrix for Period Assignment
		periodAssignment = new boolean[problemInstance.periodCount][problemInstance.customerCount];
		
		//ALLOCATING permutation map matrix -> period * customer
		permutation = new int[problemInstance.periodCount][problemInstance.customerCount];
		
		
		//allocating routeAllocation
		routePartition = new int[problemInstance.vehicleCount];

		loadViolation = new double[problemInstance.periodCount][problemInstance.vehicleCount];
	}
	
	
	// make a copy cat individual
		//copies problem instance, periodAssignment, permutation, routePartition
	public Individual(Individual original)
	{
	    int i,j;
		problemInstance = original.problemInstance;

		periodAssignment = new boolean[problemInstance.periodCount][problemInstance.customerCount];
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				periodAssignment[i][j] = original.periodAssignment[i][j];
			}
		}



		permutation = new int[problemInstance.periodCount][problemInstance.customerCount];
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				permutation[i][j] = original.permutation[i][j];
			}
		}


		routePartition = new int[problemInstance.vehicleCount];
		for( i=0;i<problemInstance.vehicleCount;i++)
		{
			routePartition[i]=original.routePartition[i];
		}

		cost = original.cost;
		costWithPenalty = original.costWithPenalty;

		//allocate demanViolationMatrix

        loadViolation = new double[problemInstance.periodCount][problemInstance.vehicleCount];

	}

	//calculate and return cost of individual
	void calculateCost()
	{
		double tempCost = 0;

		totalLoadViolation = 0;
		totalRouteTimeViolation = 0;
        
		//double temlLoad;
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.vehicleCount;j++)
			{
				tempCost += calculateFitness(i,j);
                //calculate the total load violation
                //Add only when actually the load is violated i.e. violation is positive
                if(loadViolation[i][j]>0) totalLoadViolation += loadViolation[i][j];
			}
		}
		
		cost = tempCost;

		if(totalLoadViolation>0  || totalRouteTimeViolation > 0)
		{
			isFeasible = false;
		}
		else isFeasible = true;

		
		//costWithPenalty = cost + totalLoadViolation + totalRouteTimeViolation;
		
	}

	//calcuate fitness for each period for each vehicle
	// route for vehicle i is  [ routePartition[i-1]+1 , routePartition[i] ]
	// given that routePartition[i-1]+1 <= routePartition[i]

	double calculateFitness(int period,int vehicle)
	{
		int assignedDepot;
		assignedDepot = problemInstance.depotAllocation[vehicle];
		double costForPV = 0;
		int start,end; // marks the first and last position of corresponding route for the array permutation

		if(vehicle == 0) start = 0;
		else start = routePartition[vehicle-1]+1;

		end = routePartition[vehicle];

		if(end<start) return 0;

		int activeStart=-1,activeEnd=-1,previous=-1,clientNode;

        double clientDemand=0;
		double totalRouteTime=0;
		for(int i=start;i<=end;i++)
		{
			clientNode = permutation[period][i];
			if(periodAssignment[period][clientNode]==false) continue;

			if(activeStart == -1) activeStart = clientNode;
			activeEnd = clientNode;

			totalRouteTime += problemInstance.serviceTime[clientNode]; //adding service time for that node

            //Caluculate total client demand for corresponding period,vehicle
            clientDemand += problemInstance.demand[clientNode];

			if(previous == -1)
			{
				previous = clientNode;
				continue;
			}

			costForPV +=	problemInstance.costMatrix[previous+problemInstance.depotCount][clientNode+problemInstance.depotCount];
			
			//ignoring travelling time for now - for cordeau MDVRP
			//totalRouteTime += problemInstance.travellingTimeMatrix[previous+problemInstance.depotCount][clientNode+problemInstance.depotCount];
			
			previous = clientNode;

		}

        if(activeStart!=-1 && activeEnd != -1)
        {
            costForPV += problemInstance.costMatrix[assignedDepot][activeStart+problemInstance.depotCount];
            costForPV += problemInstance.costMatrix[activeEnd+problemInstance.depotCount][assignedDepot];

//			totalRouteTime += problemInstance.travellingTimeMatrix[assignedDepot][activeStart+problemInstance.depotCount];
//            totalRouteTime += problemInstance.travellingTimeMatrix[activeEnd+problemInstance.depotCount][assignedDepot];
        }
        loadViolation[period][vehicle] = clientDemand - problemInstance.loadCapacity[vehicle];

		double routeTimeViolation = totalRouteTime - problemInstance.timeConstraintsOfVehicles[period][vehicle] ;
		if(routeTimeViolation>0) totalRouteTimeViolation += routeTimeViolation;

		return costForPV;

	}

	// sorts the array routePartition in increasing order
	// input -> routePartition array [0, upperbound ], with,n inserted at the last in the array
	// output -> sorted array [0, upperbound]
	void sort(int n,int upperbound)
	{
		int tmp;
		for(int i = upperbound-1;i>=0;i--)
		{
			if(routePartition[i]>routePartition[i+1])
			{
				tmp = routePartition[i];
				routePartition[i] = routePartition[i+1];
				routePartition[i+1] = tmp;
			}
			else
				break;
		}
	}
	
	void print()
	{
		//if(problemInstance == null) System.out.println("OUT IS NULL");
		PrintWriter out = this.problemInstance.getPrintWriter();
		int i,j;
		
		out.println("PERIOD ASSIGMENT : ");
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				if(periodAssignment[i][j])	out.print("1 ");
				else out.print("0 ");
				
			}
			out.println();
		}

		out.print("Permutation : \n");
		for( i=0; i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				out.print(permutation[i][j]+" ");
			}
			out.println();
		}

		out.print("Route partition : ");
		for( i=0;i<problemInstance.vehicleCount;i++)out.print(routePartition[i] +" ");
		out.println();
		

        // print load violation

		out.print("LOAD VIOLATION MATRIX : \n");
        for( i=0;i<problemInstance.periodCount;i++)
        {
            for( j=0;j<problemInstance.vehicleCount;j++)
            {
            	out.print(loadViolation[i][j]+" ");
            }
            out.println();
        }

        out.println("Is Feasible : "+isFeasible);
        out.println("Total Load Violation : "+totalLoadViolation);        
        out.println("Total route time violation : "+totalRouteTimeViolation);		
		out.println("Cost : " + cost);
		out.println("Cost with penalty : "+costWithPenalty);
		out.println();
		
	}
	
	void miniPrint()
	{
		PrintWriter out = this.problemInstance.getPrintWriter();
		int i,j;
		
		out.println("PERIOD ASSIGMENT : ");
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				if(periodAssignment[i][j])	out.print("1 ");
				else out.print("0 ");
				
			}
			out.println();
		}

		out.print("Permutation : \n");
		for( i=0; i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				out.print(permutation[i][j]+" ");
			}
			out.println();
		}

		out.print("Route partition : ");
		for( i=0;i<problemInstance.vehicleCount;i++)out.print(routePartition[i] +" ");
		out.println();
		

        // print load violation
        out.println("Is Feasible : "+isFeasible);
        out.println("Total Load Violation : "+totalLoadViolation);        
        out.println("Total route time violation : "+totalRouteTimeViolation);		
		out.println("Cost : " + cost);
		out.println("Cost with penalty : "+costWithPenalty);
		out.println("\n");
		
	}
	
	
	void mutatePermutation(int period)
	{
		int first = Utility.randomIntInclusive(problemInstance.customerCount-1);

		int second;
		int count=0;
		do
		{
			second = Utility.randomIntInclusive(problemInstance.customerCount-1);
			count++;
			if(count==problemInstance.customerCount)break;
		}
		while(periodAssignment[period][second]==false || second == first);

		int temp = permutation[period][first];
		permutation[period][first] = permutation[period][second];
		permutation[period][second] = temp;

		// FITNESS CAN BE UPDATED HERE
	}

	
	//moves some red line
	//no effect if only one vehicle
	void mutateRoutePartition()
	{
		//nothing to do if only one vehicle
		if(problemInstance.vehicleCount == 1) return ;

		//pick a red line/seperator
		//generate random number in [0,vehicleCount-1)


		int distance,increment;

		while(true)
		{
			int seperatorIndex = Utility.randomIntInclusive(problemInstance.vehicleCount-2);
			int dir = Utility.randomIntInclusive(1); // 0-> left , 1-> right
			if(dir==0)//move the seperator left
			{
				if(seperatorIndex==0) distance = routePartition[0] ;
				else distance = routePartition[seperatorIndex] - routePartition[seperatorIndex-1];
				// if the line can not merge with the previous one ,
				// difference = routePartition[seperatorIndex] - 1 - routePartition[seperatorIndex-1]

				// increment should be in [1,distance]
				if(distance==0)continue;
				increment = 1 + Utility.randomIntInclusive(distance-1);
				routePartition[seperatorIndex] -= increment;
				return;
			}
			else	//move the seperator right
			{
				distance = routePartition[seperatorIndex+1] - routePartition[seperatorIndex] ;
				if(distance==0)continue;
				increment = 1 + Utility.randomIntInclusive(distance-1);
				routePartition[seperatorIndex] += increment;
				return;
			}
		}

	}


	//returns 0 if it couldnt mutate as period == freq
	int mutatePeriodAssignment(int clientNo)
	{
		//no way to mutate per. ass. as freq. == period
		if(problemInstance.frequencyAllocation[clientNo] == problemInstance.periodCount) return 0;
		if(problemInstance.frequencyAllocation[clientNo] == 0) return 0;		

		int previouslyAssigned; // one period that was assigned to client
		do
		{
			previouslyAssigned = Utility.randomIntInclusive(problemInstance.periodCount-1);
		} while (periodAssignment[previouslyAssigned][clientNo]==false);

		int previouslyUnassigned;//one period that was NOT assigned to client
		do
		{
			previouslyUnassigned = Utility.randomIntInclusive(problemInstance.periodCount-1);
		} while (periodAssignment[previouslyUnassigned][clientNo]==true);

		periodAssignment[previouslyAssigned][clientNo] = false;
		periodAssignment[previouslyUnassigned][clientNo]= true;

		return 1;
	}
	
	/** 
	 * 
	  */
	static void crossOver(ProblemInstance problemInstance,Individual parent1,Individual parent2,Individual child1,Individual child2)
	{
		//with 50% probability swap parents
		int ran = Utility.randomIntInclusive(1);
		if(ran ==1)
		{
			Individual temp = parent1;
			parent1 = parent2;
			parent2 = temp;
		}
		
		
		//child 1 gets first n customers assignment from parent 1 and rest from parent 2
		//child 2 gets first n customers assignment from parent 2 and rest from parent 1
		int n = Utility.randomIntInclusive(problemInstance.customerCount);
		
		
		copyPeriodAssignmentFromParents(child1, parent1, parent2, n ,problemInstance);
		copyPeriodAssignmentFromParents(child2, parent2, parent1, n ,problemInstance);
		
		//child 1 gets permutation of first n period from parent 1
		n = Utility.randomIntInclusive(problemInstance.periodCount);
		
		copyPermutation(child1, parent1, parent2, n, problemInstance);
		copyPermutation(child2, parent2, parent1, n, problemInstance);
		
		
		// crossover route partition
		
		int temp[] = new int[problemInstance.vehicleCount*2];
		int i;
		for(i=0;i<problemInstance.vehicleCount;i++) temp[i] = parent1.routePartition[i];
		for(i=0;i<problemInstance.vehicleCount;i++) temp[i+problemInstance.vehicleCount] = parent2.routePartition[i];
		
		
		for(i=0;i<problemInstance.vehicleCount*2;i++)
		{
			for(int j=i+1;j<problemInstance.vehicleCount*2;j++)
			{
				if(temp[i]>temp[j])
				{
					int tmp = temp[i];
					temp[i] = temp[j];
					temp[j]=tmp;
				}
			}
		}
		
		for( i=0;i<problemInstance.vehicleCount;i++) child1.routePartition[i] = temp[2*i];
		for( i=0;i<problemInstance.vehicleCount;i++) child2.routePartition[i] = temp[2*i+1];
		
		
		//System.out.println(" "+n);
	}

	//copy first n row from parent 1's permutation
	private static void copyPermutation(Individual child, Individual parent1, Individual parent2,int n,ProblemInstance problemInstance) 
	{
		int i;
		
		for(i=0;i<problemInstance.customerCount;i++)
		{
			for(int period = 0;period<n;period++)
			{
				child.permutation[period][i] = parent1.permutation[period][i];
			}
		}
		
		for(i=0;i<problemInstance.customerCount;i++)
		{
			for(int period = n;period<problemInstance.periodCount;period++)
			{
				child.permutation[period][i] = parent2.permutation[period][i];
			}
		}
		
	}
	
	//copies first n columns from parent1 and rest of them from parent 2 
	private static  void copyPeriodAssignmentFromParents(Individual child, Individual parent1, Individual parent2,int n,ProblemInstance problemInstance)
	{
		int i;
		for(int period = 0; period<problemInstance.periodCount; period++)
		{
			for(i=0;i<n;i++)
			{
				//if(parent1==null)System.out.print("nul");
				child.periodAssignment[period][i] = parent1.periodAssignment[period][i];
			}
		}
		for(int period = 0; period<problemInstance.periodCount; period++)
		{
			for(i=n;i<problemInstance.customerCount;i++)
			{
				child.periodAssignment[period][i] = parent2.periodAssignment[period][i];
			}
		}
	}

}
