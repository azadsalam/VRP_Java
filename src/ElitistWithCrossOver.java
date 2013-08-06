import java.io.PrintWriter;
import java.util.Scanner;


public class ElitistWithCrossOver 
{
	PrintWriter out; 
	
	int POPULATION_SIZE = 500;
	int NUMBER_OF_OFFSPRING = 300; //must be even
	int NUMBER_OF_GENERATION = 1000;
	
	ProblemInstance problemInstance;
	Individual population[];

	//for storing new generated offsprings
	Individual offspringPopulation[];

	//for temporary storing
	Individual temporaryPopulation[];

	// for selection - roulette wheel
	double fitness[];
	double cdf[];

	double loadPenaltyFactor;
	double routeTimePenaltyFactor;
	
	
	public ElitistWithCrossOver(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;
		
		population = new Individual[POPULATION_SIZE];
		offspringPopulation = new Individual[NUMBER_OF_OFFSPRING];
		temporaryPopulation = new Individual[POPULATION_SIZE];
		
		fitness = new double[POPULATION_SIZE];
		cdf = new double[POPULATION_SIZE];
		
		loadPenaltyFactor = 20;
		routeTimePenaltyFactor = 0.6;
		
	}

	public void run() 
	{
		
		int selectedParent1,selectedParent2;
		int i;
		
		Individual parent1,parent2,offspring1,offspring2;

		// INITIALISE POPULATION
		initialisePopulation();

		sort(population);

		
		for(int generation=0;generation<NUMBER_OF_GENERATION;generation++)
		{
			//sort function uses selection sort, replace with some O(n lg n) sort algthm

			initialiseRouletteWheelSelection(generation);
			//Select a parent and apply genetic operator
			for( i=0;i<NUMBER_OF_OFFSPRING;i++)
			{
					selectedParent1=rouletteWheelSelection();

					do
					{
						selectedParent2 = rouletteWheelSelection();
					}while(selectedParent1==selectedParent2);
					
					
					parent1 = population[selectedParent1];
					parent2 = population[selectedParent2];
					
					offspring1 = new Individual(problemInstance);
					offspring2 = new Individual(problemInstance);

					Individual.crossOver(problemInstance, parent1, parent2, offspring1, offspring2);
					
					/*
					 out.print("--------------------CHECK CROSSOVER\n");
					out.print("Parent 1 : \n");
					parent1.miniPrint();
					out.print("Parent 2 : \n");
					parent2.miniPrint();
					out.print("child 1 : \n");
					offspring1.miniPrint();
					out.print("child 2 : \n");
					offspring2.miniPrint();
					*/
					
					applyMutation(offspring1);
					applyMutation(offspring2);
					
					
					//parent.print();
					offspring1.calculateCost();
					offspring2.calculateCost();
					//offspring.print();
					offspringPopulation[i] = offspring1;
					i++;
					offspringPopulation[i]=offspring2;
					
					
					
					
					
			}


			//TAKE THE BEST "POPULATION_SIZE" individuals from the set of all parents and children
			sort(offspringPopulation);

			//first select best indivdls in the temporary array
			//afterwards replace population with it
			i = 0;
			int j = 0;
			int cursor = 0;

			while(cursor<POPULATION_SIZE)
			{
				if(i == POPULATION_SIZE)//NEVER GONNA HAPPEN 
				{
					temporaryPopulation[cursor] = offspringPopulation[j];
					j++;
				}
				else if(j== NUMBER_OF_OFFSPRING)
				{
					temporaryPopulation[cursor] = population[i];
					i++;
				}
				else if(population[i].costWithPenalty <= offspringPopulation[j].costWithPenalty)
				{
					temporaryPopulation[cursor] = population[i];
					i++;
				}
				else
				{
					temporaryPopulation[cursor] = offspringPopulation[j];
					j++;
				}
				cursor++;
			}

			//replace population with temporary array
			for(i=0;i<POPULATION_SIZE;i++)
			{
				population[i] = temporaryPopulation[i];
			}
		}


		sort(population);
		out.print("\n\n\n\n\n--------------------------------------------------\n");
		out.print("\n\n\nFINAL POPULATION\n\n");
		for( i=0;i<POPULATION_SIZE;i++)
		{
			out.println("\n\nIndividual : "+i);
			population[i].print();
		}

	}
	
	
	
	//SORT THE INDIVIDUALS ON ASCENDING ORDER OF COST
	//BETTER INDIVIDUALS HAVE LOWER INDEX
	//COST LESS, INDEX LESS ;-)
	void sort(Individual[] array)
	{
		Individual temp;
		//FOR NOW DONE SELECTION SORT
		//AFTERWARDS REPLACE IT WITH QUICK SORT OR SOME OTHER O(n logn) sort
		for(int i=0;i<array.length;i++)
		{
			for(int j=i+1;j<array.length;j++)
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

	void initialiseRouletteWheelSelection(int generation)
	{
		int i,j;
		//SELECTION -> Roulette wheel
		double sumOfFitness = 0,sumOfCost=0;
		double sumOfProability = 0;

		//cout<< "SELECTION\nCost : ";
		for( i=0;i<POPULATION_SIZE;i++)
		{
			population[i].calculateCost();
			fitness[i] = population[i].cost;
			// incorporate penalty
	
			double penalty = loadPenaltyFactor * (generation+1) * population[i].totalLoadViolation;
			if(penalty>0)fitness[i] += penalty;
			
			penalty = routeTimePenaltyFactor * (generation+1) * population[i].totalRouteTimeViolation;
			if(penalty>0)fitness[i] += penalty;
		
			
			//store the cost with penalty in the individual
			population[i].costWithPenalty = fitness[i];
			sumOfCost += fitness[i];
			
		}

		for( i=0;i<POPULATION_SIZE;i++)
		{
			fitness[i] = sumOfCost / fitness[i]; // the original fitness			
			sumOfFitness += fitness[i];
		}
		
		
		for( i=0;i<POPULATION_SIZE;i++)
		{
			sumOfProability = cdf[i] = sumOfProability + ((double)fitness[i]/sumOfFitness);
		}


	}
	// it also calculates cost of every individual
	int rouletteWheelSelection()
	{
		double num = Utility.randomIntInclusive(100); // generate random number from [0,100]
		double indicator = num/100;

		//find the smallest index i, with cdf[i] greater than indicator
		int par =  findParent(indicator);
		return par;

	}

	//binary search for smallest index i, having cdf[i] greater than indicator
	int findParent(double indicator)
	{
		//for now linear search, do binary search later
		for(int i=0;i<POPULATION_SIZE;i++)
			if(cdf[i]>=indicator)
				return i;
		return POPULATION_SIZE-1;
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
		out.print("Initial population : \n");
		for(int i=0; i<POPULATION_SIZE; i++)
		{
			population[i] = new Individual(problemInstance);
			population[i].initialise();
			out.println("Printing individual "+ i +" : \n");
			population[i].miniPrint();
		}
	}

}
