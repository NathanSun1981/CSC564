
import java.util.concurrent.locks.ReentrantLock;


import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class DiningPhilosophers2 {
    public static void main(String[] args) {
    	DiningPhilosophers2 dp = new DiningPhilosophers2();
    	ReentrantLock[] fork =  new ReentrantLock[5];
    	long startTime=System.currentTimeMillis();  
    	Vector<Thread> threads = new Vector<Thread>();
    	
	    for (int i = 0; i < 5; i++) {
	    	fork[i] = new ReentrantLock();  
	    }

	    for (int i = 0; i < 5; i++) {
	        Thread iThread =  new Thread(dp.new Philosopher(i, fork[i], fork[(i + 1) % 5]));
        	threads.add(iThread);    
        	iThread.start();
	    }
	    
	    for (Thread iThread : threads) {
            try {
              iThread.join();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
        }
         
        long endTime=System.currentTimeMillis();       
        System.out.println("\"All threads elapsed: "+(endTime-startTime)+"ms");
	 }
    
	class Fork {
	    public Fork() {	
	    }
	}

	class Philosopher implements Runnable {
	    private ReentrantLock left;
	    private ReentrantLock right; 
	    private int  id;   
	    
 	    public Philosopher(int id, ReentrantLock left, ReentrantLock right) {
	    	this.id = id;
	    	this.left = left;
	    	this.right = right;
	    }

	    public void run() {
	        //while (true) {
	        for (int i = 0; i < 100; i++) 
	        {
	        	try {
	                Thread.sleep(1);
	            } catch (InterruptedException e1) {
	                e1.printStackTrace();
	            }

	        	//System.out.println("Philosopher " + id + " is thinking"); 
	            left.lock(); 
	            //take the left fork
	            //System.out.println("Philosopher " + id + " take the left fork"); 
	            try {
                    Thread.sleep(1);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

	            try{
	            	//System.out.println("Philosopher " + id + " try to get the right fork");
                    if(right.tryLock(100,TimeUnit.MILLISECONDS)){
                        try{
                            Thread.sleep(1);//
                            //System.out.println("Philosopher " + id + " take the right fork");
                            System.out.println("Philosopher " + id + " is eating"); 
                        }finally {
                        	//System.out.println("Philosopher " + id + " put the right fork");
                            right.unlock();
                        }
                    }else{
                    	//System.out.println("Philosopher " + id + " put the left fork when not get");
                    	left.unlock();
                    	continue; //continue thinking
                    }
                } catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	            //System.out.println("Philosopher " + id + " put the left fork when get");
                left.unlock();
	        } 
	    }
	}
}


