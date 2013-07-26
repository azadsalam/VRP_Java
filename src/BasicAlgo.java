import java.io.PrintWriter;
import java.util.Scanner;


public class BasicAlgo 
{
	PrintWriter out; 
	
	int POPULATION_SIZE = 3;
	int NUMBER_OF_OFFSPRING = 2;
	int NUMBER_OF_GENERATION = 10;
	
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
	
	
	public BasicAlgo(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;
		
		population = new Individual[POPULATION_SIZE];
		offspringPopulation = new Individual[NUMBER_OF_OFFSPRING];
		temporaryPopulation = new Individual[NUMBER_OF_GENERATION];
		
		fitness = new double[POPULATION_SIZE];
		cdf = new double[POPULATION_SIZE];
		
		loadPenaltyFactor = 0.6;
		routeTimePenaltyFactor = 0.6;
		
	}

	public void run() 
	{
		
		int selectedParent;
		int i;
		
		Individual parent,offspring;

		// INITIALISE POPULATION
		initialisePopulation();

		sort(population);

		
		for(int generation=0;generation<NUMBER_OF_GENERATION;generation++)
		{
			//sort function uses selection sort, replace with some O(n lg n) sort algthm

			//cout << "--------------------------\nGENERATION : "<<generation<<"\n";

			//Select a parent and apply genetic operator
			for( i=0;i<NUMBER_OF_OFFSPRING;i++)
			{
					selectedParent=rouletteWheelSelection(generation);

					parent = population[selectedParent];
					offspring = new Individual(parent);

					applyMutation(offspring);
					//cout << "Selected Parent : " << selectedParent <<endl;
					//parent.print();
					offspring.calculateFitness();
					//cout << "Offspring : "<<i<<"\n";
					//offspring.print();

					offspringPopulation[i] = offspring;

			}


			//cout <<"\n\n\n\n\n---TESTING (lamba + miu)"<<endl<<endl;

			//////////////////////////////////////////////////////////////////////////
			/*
			cout <<"PRINTING PARENT POPULATION\n";
			for( i=0;i<POPULATION_SIZE;i++)
			{
				cout << "parent " << i << " :\n";
				population[i].print();
			}
			cout << endl<<endl;
			*/
			//////////////////////////////////////////////////////////////////////////////

			//TAKE THE BEST "POPULATION_SIZE" individuals from the set of all parents and children
			sort(offspringPopulation);


			//////////////////////////////////////////////////////////////////////////////
			/*
			cout <<"PRINTING OFFSPRING POPULATION\n";
			for( i=0;i<NUMBER_OF_OFFSPRING;i++)
			{
				cout << "offspring " << i << " :\n";
				offspringPopulation[i].print();
			}
			cout << endl<<endl;
			*/
			//////////////////////////////////////////////////////////////////////////////

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
				else if(population[i].cost <= offspringPopulation[j].cost)
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


			//////////////////////////////////////////////////////////////////////////
			/*
			cout <<"PRINTING NEW GENERATION\n";
			for( i=0;i<POPULATION_SIZE;i++)
			{
				cout << "parent " << i << " :\n";
				population[i].print();
			}
			cout << endl<<endl;
			*/
			//////////////////////////////////////////////////////////////////////////////



		}


		//cout<<"\n\n\n\n\n--------------------------------------------------\n";
		out.print("\n\n\nFINAL POPULATION\n\n");
		for( i=0;i<POPULATION_SIZE;i++)
		{
			out.println("Individual : "+i);
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
				if(array[i].cost > array[j].cost)
				{
					temp = array[i];
					array[i] =array[j];
					array[j] = temp;
				}
			}
		}

	}

	// it also calculates cost of every individual
	int rouletteWheelSelection(int generation)
	{
		int i,j;
		//SELECTION -> Roulette wheel
		double sumOfFitness = 0,sumOfCost=0;
		double sumOfProability = 0;

		//cout<< "SELECTION\nCost : ";
		for( i=0;i<POPULATION_SIZE;i++)
		{
			population[i].calculateFitness();
			fitness[i] = population[i].cost;
			// incorporate penalty
	
			double penalty = loadPenaltyFactor * (generation+1) * population[i].totalLoadViolation;
			if(penalty>0)fitness[i] += penalty;
			
			penalty = routeTimePenaltyFactor * (generation+1) * population[i].totalRouteTimeViolation;
			if(penalty>0)fitness[i] += penalty;
		
			sumOfCost += fitness[i];
			//cout << " "<<fitness[i];
		}
		//cout <<"   Total cost : "<<sumOfCost<<endl;

//		cout<< "Fitness : ";
		for( i=0;i<POPULATION_SIZE;i++)
		{
			fitness[i] = sumOfCost / fitness[i]; // the original fitness			
			sumOfFitness += fitness[i];
		//	cout << " "<< fitness[i];
		}
		//cout <<"    Total fitness : "<<sumOfFitness<<endl;

		for( i=0;i<POPULATION_SIZE;i++)
		{
			sumOfProability = cdf[i] = sumOfProability + ((double)fitness[i]/sumOfFitness);
		}

		double num = Utility.randomIntInclusive(100); // generate random number from [0,100]
		double indicator = num/100;

		//find the smallest index i, with cdf[i] greater than indicator

		int par =  findParent(indicator);
		//cout <<"Selected Parent : "<< par<<endl;
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
			int client = Utility.randomIntInclusive(problemInstance.customerCount-1);
			offspring.mutatePeriodAssignment(client);
		}
	}

	//0 -> route partition
	//1 ->	permutation
	//2 -> period
	int selectMutationOperator()
	{
		return Utility.randomIntInclusive(2);
	}

	void initialisePopulation()
	{
		out.print("Initial population : \n");
		for(int i=0; i<POPULATION_SIZE; i++)
		{
			population[i] = new Individual(problemInstance);
			out.println("Printing individual "+ i +" : ");
			population[i].print();
		}
	}

}
