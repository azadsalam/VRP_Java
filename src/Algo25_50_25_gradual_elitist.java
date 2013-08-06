import java.io.PrintWriter;
import java.util.Scanner;


public class Algo25_50_25_gradual_elitist 
{
	PrintWriter out; 
	
	int BEST = 0;
	int MODERATE = 1;
	int WORST = 2; 
	
	int STEPS =25 ; 
	double bestPercentage  = 50;
	double worstPercentage = 50;
	double increment;
	double stepSize;
	
	int bestStart,moderateStart,worstStart;
	int bestInterval,moderateInterval,worstInterval; 
	
	int POPULATION_SIZE = 200;//must be even
	int NUMBER_OF_OFFSPRING = POPULATION_SIZE;   
	int NUMBER_OF_GENERATION = 1000;
	
	ProblemInstance problemInstance;
	Individual population[];

	
	double loadPenaltyFactor;
	double routeTimePenaltyFactor;
	
	
	Individual parent1,parent2;
	
	boolean outputToFile = false;
	public Algo25_50_25_gradual_elitist(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;
		
		population = new Individual[POPULATION_SIZE+NUMBER_OF_OFFSPRING];
				
		loadPenaltyFactor = 10;
		routeTimePenaltyFactor = 1;
		
		
		increment = (double)50 / STEPS;
		stepSize = (double)NUMBER_OF_GENERATION / (STEPS+1);
		
		//if pop = 100
		//[0 - 25]
		bestStart = 0;
		bestInterval = POPULATION_SIZE/4  ;
		
		//[26 - 75]
		moderateStart = bestInterval + 1;
		moderateInterval = POPULATION_SIZE/2 - 1 ;
		
		//[76-100]
		worstStart = moderateStart + moderateInterval + 1;
		worstInterval = bestInterval;
		
		Solver.exportToCsv.init(NUMBER_OF_GENERATION+1);

	}

	public Individual run() 
	{
		
		int i,generation;
//		double multiplier;  for gradual change
		int multiplier;
		
		Individual offspring1,offspring2;

		initialisePopulation();
		sort(population,0,POPULATION_SIZE);
		
		for( generation=0;generation<NUMBER_OF_GENERATION;generation++)
		{
			//sort function uses selection sort, replace with some O(n lg n) sort algthm
			calculateCostWithPenalty(0,POPULATION_SIZE,generation,true);
			
			i=0;
			while(i<NUMBER_OF_OFFSPRING)
			{
				selectParent();
				
				offspring1 = new Individual(problemInstance);
				offspring2 = new Individual(problemInstance);
				Individual.crossOver(problemInstance, parent1, parent2, offspring1, offspring2);	
				
				applyMutation(offspring1);
				applyMutation(offspring2);
				
				population[i+POPULATION_SIZE] = offspring1;
				i++;
				population[i+POPULATION_SIZE] = offspring2;
				i++;
			}

			
			calculateCostWithPenalty(POPULATION_SIZE, POPULATION_SIZE, generation,false);
			
			
			sort(population	,0, POPULATION_SIZE*2);
			
			
			multiplier = (int)(generation/stepSize);
			
			int count = (int)(((worstPercentage - (increment * multiplier))*POPULATION_SIZE)/100);
			
			//double per = (worstPercentage - (increment * multiplier));
			//System.out.format("Generation : %d Worst percntg : %f Count : %d\n",generation,per,count);
			//Initially select 50% best and 50% worst
			// Gradually become more elitist
			// end in 100% best and 0% worst
			// change the percentages STEPS times
			
			
			for(i=0;i<count;i++)
			{
				population[POPULATION_SIZE-1-i] = population[(2*POPULATION_SIZE)-1-i];
			}
			
				
		}


		
		//sort(population);
		if(outputToFile)
		{
			out.print("\n\n\n\n\n--------------------------------------------------\n");
			calculateCostWithPenalty(0, POPULATION_SIZE, generation, true);
			out.print("\n\n\nFINAL POPULATION\n\n");
			for( i=0;i<POPULATION_SIZE;i++)
			{
				out.println("\n\nIndividual : "+i);
				population[i].print();
			}
		}
		return population[0];

	}
	
	void selectParent()
	{
		int random = Utility.randomIntInclusive(100);
		int p1,p2;
		
		if(random<10)	  { p1 = BEST ; p2 = BEST;}
		else if(random<30){ p1 = BEST ; p2 = WORST;}
		else if(random<50){ p1 = BEST ; p2 = MODERATE;}
		else if(random<65){ p1 = MODERATE ; p2 = MODERATE;}
		else if(random<80){ p1 = MODERATE ; p2 = WORST;}
		else 			  { p1 = WORST ; p2 = WORST;}
		
		parent1 = selectParent(p1);
		parent2 = selectParent(p2);
	}
	
	Individual selectParent(int category)
	{
		
		int index;
		if(category == BEST)
		{
			index = Utility.randomIntInclusive(bestInterval) + bestStart;
		}
		else if(category == MODERATE)
		{
			index = Utility.randomIntInclusive(moderateInterval) + moderateStart;
		}
		else
		{
			index = Utility.randomIntInclusive(worstInterval) + worstStart;
		}
		
		if(index>=POPULATION_SIZE) index = Utility.randomIntInclusive(POPULATION_SIZE-1); // NEVER SHOULD HAPPEN
		
		return population[index];
	}
	
	
	// determines fitness and penalty to calculate costWithPenalty
	void calculateCostWithPenalty(int start, int length, int generation,boolean print)
	{
		double sum=0,avg,penalty;
		double min =0xFFFFFFF;
		double max = -1;
		int feasibleCount = 0;
		
		for(int i=start; i<start+length; i++)
		{
			population[i].calculateCost();
			
			penalty = 0;
			penalty += population[i].totalLoadViolation * loadPenaltyFactor;
			penalty += population[i].totalRouteTimeViolation * routeTimePenaltyFactor;
			//penalty *= (generation+1);
			
			population[i].costWithPenalty = population[i].cost + penalty;
			
			sum += population[i].costWithPenalty;
			if(population[i].costWithPenalty > max) max = population[i].costWithPenalty;
			if(population[i].costWithPenalty < min) min = population[i].costWithPenalty;
			if(population[i].isFeasible) feasibleCount++;
		}
		
		avg = sum / POPULATION_SIZE;

		if(print && outputToFile)	out.format("Generation %d : Min : %f Avg : %f  Max : %f Feasible : %d \n",generation,min,avg,max,feasibleCount);
		if(print)
		{
			Solver.exportToCsv.min[generation] = min;
			Solver.exportToCsv.avg[generation] = avg;
			Solver.exportToCsv.max[generation] = max;
			Solver.exportToCsv.feasibleCount[generation] = feasibleCount;
		}
	}
	
	//SORT THE INDIVIDUALS ON ASCENDING ORDER OF COST
	//BETTER INDIVIDUALS HAVE LOWER INDEX
	//COST LESS, INDEX LESS ;-)
	void sort(Individual[] array,int start,int length)
	{
		Individual temp;
		//FOR NOW DONE SELECTION SORT
		//AFTERWARDS REPLACE IT WITH QUICK SORT OR SOME OTHER O(n logn) sort
		for(int i=start;i<start+length;i++)
		{
			for(int j=i+1;j<start+length;j++)
			{
				if(array[i].costWithPenalty > array[j].costWithPenalty)
				{
					temp = array[i];
					array[i] =array[j];
					array[j] = temp;
				}
			}
		}

	}

	
	// for now not applying periodAssignment Mutation operator
	// for now working with only MDVRP ->  period = 1
	void applyMutation(Individual offspring)
	{
		int selectedMutationOperator = selectMutationOperator();
		
		if(selectedMutationOperator==0)offspring.mutateRoutePartition();
		else if (selectedMutationOperator == 1)
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			offspring.mutatePermutation(period);//for now single period
		}
		else if (selectedMutationOperator == 2)
		{
			//int client = Utility.randomIntInclusive(problemInstance.customerCount-1);
			//offspring.mutatePeriodAssignment(client);
			offspring.mutateRoutePartition();
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			offspring.mutatePermutation(period);//for now single period			
		}
		else if (selectedMutationOperator == 3){}
		
	}

	//0 -> route partition
	//1 ->	permutation
	//2 -> route partition + permutation
	//3 -> none
	int selectMutationOperator()
	{
		return Utility.randomIntInclusive(3);
	}

	void initialisePopulation()
	{
	//	out.print("Initial population : \n");
		for(int i=0; i<POPULATION_SIZE; i++)
		{
			population[i] = new Individual(problemInstance);
			population[i].initialise();
			//out.println("Printing individual "+ i +" : \n");
			//population[i].miniPrint();
		}
	}

}
