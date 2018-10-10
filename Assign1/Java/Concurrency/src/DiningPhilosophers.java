import java.util.Vector;


public class DiningPhilosophers {
	
    public static void main(String[] args) {
    	DiningPhilosophers dp = new DiningPhilosophers();
    	long startTime=System.currentTimeMillis();  
    	Vector<Thread> threads = new Vector<Thread>();
    	
    	Fork[] fork = new Fork[5];
	    for (int i = 0; i < 5; i++) {
	    	fork[i] = dp.new Fork();     //total 5 chopsticks 
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

	    private Fork left;
	    private Fork right;	    
	    private int  id;   
 	    public Philosopher(int id, Fork left, Fork right) {
	    	this.id = id;
	        this.left = left;
	        this.right = right;
	    }
	    public void run() {
	        while (true) {
	        	try {
	                Thread.sleep(100);
	            } catch (InterruptedException e1) {
	                e1.printStackTrace();
	            }
	        	System.out.println("Philosopher " + id + " is thinking"); 
	            synchronized (left) {  //
	            	//take the left fork
	            	try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
	            	System.out.println("Philosopher " + id + " take the left fork"); 
	            	synchronized (right) {   //take the right fork
	            		try {
	                        Thread.sleep(100);
	                    } catch (InterruptedException e1) {
	                        e1.printStackTrace();
	                    }
	            		System.out.println("Philosopher " + id + " take the right fork"); 
	            		System.out.println("Philosopher " + id + " is eating"); 
	            	}
	            }
	        } 
	    }
	}
}

