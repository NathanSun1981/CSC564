import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class H2O {
	
	private final static Lock lock = new ReentrantLock();
	private final Condition makewater = lock.newCondition();
	private final static Condition OxStart = lock.newCondition();
	private final static Condition Hystart = lock.newCondition();
	
	private int oxnum = 0;
	private int hynum = 0;
	
	public static void main(String[] args) {
        H2O h2o = new H2O();
        int gas;
            
        new Thread(h2o.new Maker()).start();  
        
    	while(true)
    	{
            try {	
            	lock.lock();
            		
    			Random random = new Random();
    			gas = random.nextInt(2);
    			if (gas == 0) {
    				OxStart.await();
    				System.out.println("Provide Oxgen");
    		        new Thread(h2o.new Oxygen()).start();  
    			}
    			if (gas == 1)
    			{
    				Hystart.await();
    				System.out.println("Provide Oxgen");
    		        new Thread(h2o.new Hydrogen()).start();  				
    			}
            }
            catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
            }
            finally
            {
            	lock.unlock();
            }

    	}
        
	}
	
	 class Maker implements Runnable {
		 	//make h2o
	        public Maker(){}	        	
	        public void run()  
	        {   
	        	while(true)
	        	{
	        		lock.lock();
	        		if (oxnum == 0 || oxnum == 1)
	        		{
	        			System.out.println("nofity to provide O");
	        			OxStart.signalAll();
	        		}
	        		if (hynum == 0)
	        		{
	        			System.out.println("nofity to provide H");
	        			Hystart.signalAll();	        			
	        		}
	        		try {
						makewater.await();				
						System.out.println("start to make H2O");
						oxnum -= 2;
						hynum--;						
	        		} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	        		
	        		lock.unlock();
	        	}        	
	        }
	        	
	    }
        
    class Oxygen implements Runnable {

        public Oxygen(){}
        	
        public void run()  
        {   
        	lock.lock();
        	try {
	        	System.out.println("start to provide Oxygen");
	        	oxnum++;
	        	System.out.println("Oxygen num = " + oxnum);
	        	if (oxnum >= 1  && hynum >= 2)
	        	{
	        		makewater.signal();
	        	}
	        	else
	        	{
	        		OxStart.signal();
	        	}
        	}
         	finally
        	{
        		lock.unlock();
        	}    	
        }
        	
    }
    
    class Hydrogen implements Runnable {

        public Hydrogen(){}
        	
        public void run()  
        {    
        	lock.lock();
        	try
        	{
        		System.out.println("start to provide Hydrogen");
	        	hynum++;
	        	System.out.println("Hydrogen num = " + hynum);
	        	if (oxnum >= 1  && hynum >= 2)
	        	{
	        		makewater.signal();
	        	}
	        	else
	        	{
	        		Hystart.signal();
	        	}
        	}
        	
        	finally
        	{
        		lock.unlock();
        	}
	
        }
        	
    }


}
