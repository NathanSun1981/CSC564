import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class H2O {
	
	private final static Lock lock = new ReentrantLock();
	private final Semaphore Makewater =  new Semaphore(0);
	private final static Semaphore Oxygen =  new Semaphore(1);
	private final static Semaphore Hydrogen =  new Semaphore(2);
	
	
	private int oxnum = 0;
	private int hynum = 0;
	private static long startTime;
	
	public static void main(String[] args) {
        H2O h2o = new H2O();
        
        startTime=System.currentTimeMillis(); 
            
        new Thread(h2o.new Maker()).start();  
            		          	
        for(int i = 0; i < 100; i++)
        {
            new Thread(h2o.new Oxygen()).start();  
        }
            for(int i = 0; i < 200; i++)
        {
            new Thread(h2o.new Hydrogen()).start();  
        }	
            
        
	}
	
	 class Maker implements Runnable {
		 	//make h2o
		 
		 	private int i = 0;
	        public Maker(){}	  
	        
	        public void run()  
	        {   
	        	while(true)
	        	{       			   		
	        		try {
	        			Makewater.acquire();			
						i++;
						System.out.println("-------start to make H2O, Num = " + i);
						if (i == 100)
						{
							long endTime=System.currentTimeMillis();       
						    System.out.println("All threads elapsed: "+(endTime-startTime)+"ms");
						}
						oxnum--;
						hynum -= 2;
						Oxygen.release();
						Hydrogen.release(2);
	        		}
					catch (InterruptedException e1) {
			                e1.printStackTrace();
			        }
	        		
	        	}        	
	        }
	        	
	    }
        
    class Oxygen implements Runnable {

        public Oxygen(){}
        	
        public void run()  
        {   
			try {
				Oxygen.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	lock.lock();
        
	        System.out.println("------------start to provide Oxygen-----------");
	        oxnum++;
	        System.out.println("Oxygen num = " + oxnum);
	        if (hynum >= 2)
	        {
	        	System.out.println("notify to make H2O");
	        	Makewater.release();
	        }
	        
        	lock.unlock();
	
        }
        	
    }
    
    class Hydrogen implements Runnable {

        public Hydrogen(){}
        	
        public void run()  
        {    
			try {
				Hydrogen.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	lock.lock();

        	System.out.println("-------start to provide Hydrogen------");
	        hynum++;
	        System.out.println("Hydrogen num = " + hynum);
	        if (oxnum >= 1 && hynum >= 2)
	        {
	        	System.out.println("notify to make H2O");
	        	Makewater.release();
	        }
	       
        	lock.unlock();
	
        }
        	
    }


}
