package Assign1;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class UnisexBathroom {

    public static Semaphore FEMALE = new Semaphore(3);
    public static Semaphore MALE = new Semaphore(3);
    public static ReentrantLock lock = new ReentrantLock(true);
    private final Condition inuse = lock.newCondition();
    private final Condition ismale = lock.newCondition();
    private final Condition isfemale = lock.newCondition();
   
    public static int a = 0;
    public static int b = 0;
    
    public static boolean isusing= false;
    public static int sex = 0;  //0: male; 1: female

    public static int VACANCY  = 3;
    public static int USERAMOUNT= 200;
    

    public static void main(String args[]) {

    	UnisexBathroom ub = new UnisexBathroom();
    	
    	new Thread(ub.new BATHROOM()).start();

        for (int i = 1; i < USERAMOUNT; i++) {
        	//new Thread(ub.new MEN(i)).start();
        	
            Random random = new Random();
            int sex = random.nextInt(2);
            if (sex == 0) {  //0: Man
                a++;
                new Thread(ub.new MEN(a)).start();
            } else {  //1: Woman
                b++;
                new Thread(ub.new WOMEN(b)).start();
            }

        }
    }
  
    class BATHROOM implements  Runnable{
    	public BATHROOM()
    	{   		
    	} 	
    	public void run() {
    		while (true)
    		{
    			try {
    				lock.lock();
    				inuse.await();
    				if (sex == 0)
    				{
    					ismale.signalAll();
    				}
    				else
    				{
    					isfemale.signalAll();
    				}
                    lock.unlock();
                } catch (InterruptedException ex) {
                	ex.printStackTrace();
                }
	
    		}   		
    	}
    }

    
    class MEN implements Runnable{
        int iD;
        public MEN(int i) {
            iD = i;
        }
        public void run() {
            try {     
            	lock.lock();
            	if (!isusing)
            	{
            		inuse.signal();  
                	sex = 0;
            	}
            	
            	ismale.await();
            	
 
            	if (sex == 0)
            	{
	            	MALE.acquire();
	                System.out.println("MAN " + this.iD + " Is USING");
	                
	                lock.lock();
	                VACANCY++;
	                lock.unlock();
	                
	                Thread.sleep(1000);
	                //take a bath then release  
	                System.out.println("MAN " + this.iD + " is done and leaving");
	                MALE.release();
	                
	                lock.lock();
	                VACANCY++;
	                lock.unlock();
            	}

             } catch (InterruptedException ex) {
            	 ex.printStackTrace();
             }
            finally
            {
            	lock.unlock();
            }
        }

    }
    
    class WOMEN implements Runnable{
        int iD;
        boolean notUsed = true;
        public WOMEN(int i) {
            iD = i;
        }
        public void run() {
            try {
	            	lock.lock();
	            	if (!isusing)
	            	{
	            		inuse.signal();  
	                	sex = 1;
	            	}
	            	lock.unlock();
	            	if (sex == 1)
	            	{
	              		FEMALE.acquire();
	                    System.out.println("WOMAN " + this.iD + " Is USING");    
	                    VACANCY--;
	                    Thread.sleep(1000);
	                    //take a bath then release  
	                    System.out.println("WOMAN " + this.iD + " is done and leaving");
	                    FEMALE.release();  
	            	}

             } catch (InterruptedException ex) {
            	 ex.printStackTrace();
             }
        }
    }
 
}