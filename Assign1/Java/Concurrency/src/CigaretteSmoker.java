

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
	private final Condition agent = ingredientlock.newCondition();

	private boolean isPaper, isMatch, isTobacco = false;

	public static void main(String[] args) {
		ReentrantLock PuserLock =  new ReentrantLock();
		// TODO Auto-generated method stub
		CigaretteSmoker cs = new CigaretteSmoker();
		new Thread(cs.new Smoker("Tobacco")).start();
		new Thread(cs.new Smoker("Paper")).start();
		new Thread(cs.new Smoker("Match")).start();
		new Thread(cs.new Pusher(PuserLock, "Tobacco")).start();
		new Thread(cs.new Pusher(PuserLock, "Paper")).start();
		new Thread(cs.new Pusher(PuserLock, "Match")).start();
		new Thread(cs.new Agent()).start(); 
		
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
					ingredientlock.lock();
					//System.out.println("tobacco start");
					try {	
						tobacco.await();
						System.out.println("tobacco get signal");
						PusherLock.lock();
						//System.out.println("tobacco lock pusher = " + isPaper + isMatch + isTobacco);
						if (isPaper)
						{
							isPaper = false;
							System.out.println("Tobacco pusher wake up, find paper, signal mathch");
							matchsem.signal();
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
						//System.out.println("final resutlt =" + isPaper + isMatch + isTobacco);
						
						PusherLock.unlock();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
					finally
					{
						//System.out.println("tobacco release lock");
						ingredientlock.unlock();	
					}
				}
				else if (wakeupIngredient.equals("Paper")) {
					//System.out.println("paper start");
					ingredientlock.lock();
					try {
						paper.await();
						System.out.println("paper get signal");
						PusherLock.lock();
						//System.out.println("paper lock pusher = " + isPaper + isMatch + isTobacco);
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
						//System.out.println("final resutlt =" + isPaper + isMatch + isTobacco);
						try {
			                Thread.sleep(10);
			            } catch (InterruptedException e1) {
			                e1.printStackTrace();
			            }
						PusherLock.unlock();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally
					{
						//System.out.println("paper release lock");
						ingredientlock.unlock();
					}

				}
				else if (wakeupIngredient.equals("Match")) {
					//System.out.println("match start");
					ingredientlock.lock();
					try {
						match.await();
						System.out.println("match get signal");
						PusherLock.lock();
						//System.out.println("match lock pusher = " + isPaper + isMatch + isTobacco);
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
						//System.out.println("final resutlt =" + isPaper + isMatch + isTobacco);
						try {
			                Thread.sleep(10);
			            } catch (InterruptedException e1) {
			                e1.printStackTrace();
			            }
						PusherLock.unlock();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally
					{
						//System.out.println("match release lock");
						ingredientlock.unlock();
					}
				}		
			}
		}
	}
	
	class Smoker implements Runnable{

		private String ownIngredient;
		public Smoker(String ownIngredient) {
			this.ownIngredient = ownIngredient;
		}

		public void run() {
			while (true) {
				if (ownIngredient.equals("Tobacco")) {
					//making Cigarette
					ingredientlock.lock();
					try {
						tobaccosem.await();
						System.out.println("Tobacco smoker get Paper and Matches, start to make cigarette...");
						agent.signal();//enable agent to dispose ingredients				
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
					finally
					{
						ingredientlock.unlock();
					}
				}
				else if (ownIngredient.equals("Paper")) {
					try {
		                Thread.sleep(100);
		            } catch (InterruptedException e1) {
		                e1.printStackTrace();
		            }
					ingredientlock.lock();
					try {
						papersem.await();
						System.out.println("Paper smoker get Tobacco and Matches, start to make cigarette...");
						agent.signal();//enable agent to dispose ingredients			
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
					finally
					{
						ingredientlock.unlock();
					}
				}
				else if (ownIngredient.equals("Match")) {
					try {
		                Thread.sleep(100);
		            } catch (InterruptedException e1) {
		                e1.printStackTrace();
		            }
					ingredientlock.lock();
					try {
						matchsem.await();
						System.out.println("Match smoker get Tobacco and Matches, start to make cigarette...");
						agent.signal();//enable agent to dispose ingredients					
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
					finally
					{
						ingredientlock.unlock();
					}
				}	
	
			}
		}
	}
	
	class Agent implements Runnable{
		public Agent()
		{
		}
		
		public void run() {			
			int currentIngredients;
			while(true)
			{
				try {
	                Thread.sleep(100);
	            } catch (InterruptedException e1) {
	                e1.printStackTrace();
	            }
				ingredientlock.lock();
				try {
					//agentLock.lock();
					//dispose times
					Random random = new Random();
					currentIngredients = random.nextInt(3);
					if (currentIngredients == 0) {
						System.out.println("Disposed Ingredients: " + "Paper and Matches");
						paper.signal();
						match.signal();
						agent.await();
						
					}
					if (currentIngredients == 1) {
						System.out.println("Disposed Ingredients: " + "Tabacco and Matches");
						tobacco.signal();
						match.signal();
						agent.await();
					}
					if (currentIngredients == 2) {
						System.out.println("Disposed Ingredients: " + "Paper and Tabacco");
						paper.signal();
						tobacco.signal();
						agent.await();
					}		
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					ingredientlock.unlock();
				}
				
			}

		}
	}
}