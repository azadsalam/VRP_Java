import java.util.Random;

public class Utility 
{
	static Random randomGenerator = new Random(System.currentTimeMillis());
	//returns a random numbor between [m,n] 
	
	public static int randomIntInclusive(int m,int n)
	{
		int random = randomGenerator.nextInt(n-m+1);
		return m+random;
	}
	
	
	//[0,n]
	public static int randomIntInclusive(int n)
	{
		int random = randomGenerator.nextInt(n+1);
		return random;
	}
}
