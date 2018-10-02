package Assign1;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class CigaretteSmoker {
	
	private final Lock ingredientlock = new ReentrantLock();
	private final Condition tobacco = ingredientlock.newCondition();
	private final Condition paper = ingredientlock.newCondition();
	private final Condition match = ingredientlock.newCondition();
	private final Condition tobaccosem = ingredientlock.newCondition();
	private final Condition papersem = ingredientlock.newCondition();
	private final Condition matchsem = ingredientlock.newCondition();
	private boolean isPaper, isMatch, isTobacco = false;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CigaretteSmoker cs = new CigaretteSmoker();
		ReentrantLock AgentLock =  new ReentrantLock();
		ReentrantLock PuserLock =  new ReentrantLock();
		
		
		
		new Thread(cs.new Smoker(AgentLock, "Tobacco")).start();
		new Thread(cs.new Smoker(AgentLock, "Paper")).start();
		new Thread(cs.new Smoker(AgentLock, "Match")).start();
		new Thread(cs.new Pusher(PuserLock, "Tobacco")).start();
		new Thread(cs.new Pusher(PuserLock, "Paper")).start();
		new Thread(cs.new Pusher(PuserLock, "Match")).start();
		new Thread(cs.new Agent(AgentLock)).start(); 
		
	}
	
	class Pusher implements Runnable{

		private ReentrantLock PusherLock;
		private String wakeupIngredient;

		public Pusher(ReentrantLock PuserLock, String Ingredient) {
			this.PusherLock = PuserLock;	
			this.wakeupIngredient = Ingredient;
		}

		public void run() {
			while (true) {
				if (wakeupIngredient.equals("Tobacco")) {
					try {
						tobacco.await();
						PusherLock.lock();
						if (isPaper)
						{
							isPaper = false;
							matchsem.signal();
							System.out.println("Tobacco pusher wake up, find paper, signal mathch");
						}
						else if (isMatch)
						{
							isMatch = false;
							papersem.signal();
							System.out.println("Tobacco pusher wake up, find match, signal paper");
						}
						else
						{
							isTobacco = true;
						}
						PusherLock.unlock();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				else if (wakeupIngredient.equals("Paper")) {					
					try {
						paper.await();
						PusherLock.lock();
						if (isTobacco)
						{
							isTobacco = false;
							matchsem.signal();
							System.out.println("paper pusher wake up, find paper, signal match");
						}
						else if (isMatch)
						{
							isMatch = false;
							tobaccosem.signal();
							System.out.println("paper pusher wake up, find match, signal tobacco");
						}
						else
						{
							isPaper= true;
						}
						PusherLock.unlock();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				else if (wakeupIngredient.equals("Match")) {
					
					try {
						match.await();
						PusherLock.lock();
						if (isTobacco)
						{
							isTobacco = false;
							papersem.signal();
							System.out.println("match pusher wake up, find tobacco, signal paper");
						}
						else if (isPaper)
						{
							isPaper = false;
							tobaccosem.signal();
							System.out.println("match pusher wake up, find paper, signal tobacco");
						}
						else
						{
							isMatch= true;
						}
						PusherLock.unlock();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}		
			}
		}
	}
	
	class Smoker implements Runnable{

		private ReentrantLock agentLock;
		private String ownIngredient;

		public Smoker(ReentrantLock agentLock, String ownIngredient) {

			this.agentLock = agentLock;	
			this.ownIngredient = ownIngredient;
		}

		public void run() {
			while (true) {
				if (ownIngredient.equals("Tobacco")) {
					//making Cigarette
					try {
						tobaccosem.await();
						System.out.println("Tobacco smoker get Paper and Matches, start to make cigarette...");
						agentLock.unlock();					
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				else if (ownIngredient.equals("Paper")) {
					try {
						papersem.await();
						System.out.println("Paper smoker get Tobacco and Matches, start to make cigarette...");
						agentLock.unlock();					
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}									
				}
				else if (ownIngredient.equals("Match")) {
					try {
						matchsem.await();
						System.out.println("Paper smoker get Tobacco and Matches, start to make cigarette...");
						agentLock.unlock();					
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}										
				}	
				agentLock.unlock();		//enable agent to dispose ingredients		
			}
		}
	}
	
	class Agent implements Runnable{
		private ReentrantLock agentLock;
		
	
		public Agent(ReentrantLock agentLock)
		{
			this.agentLock = agentLock;	

		}
		
		public void run() {			
			int currentIngredients;
			while(true)
			{
					agentLock.lock();
					//dispose times
					Random random = new Random();
					currentIngredients = random.nextInt(3);
					if (currentIngredients == 0) {
						System.out.println("Disposed Ingredients: " + "Paper and Matches");
						paper.signal();
						match.signal();
						
					}
					if (currentIngredients == 1) {
						System.out.println("Disposed Ingredients: " + "Tabacco and Matches");
						tobacco.signal();
						match.signal();
					}
					if (currentIngredients == 2) {
						System.out.println("Disposed Ingredients: " + "Paper and Tabacco");
						paper.signal();
						tobacco.signal();
					}		
			}
		}
	}
}