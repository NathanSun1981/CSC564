

public class DiningPhilosophers1 {
	
    public static void main(String[] args) {
    	DiningPhilosophers1 dp = new DiningPhilosophers1();
    	
    	Fork[] fork = new Fork[5];
	    for (int i = 0; i < 5; i++) {
	    	fork[i] = dp.new Fork(i);     
	    }
	    for (int i = 0; i < 5; i++) {
	        new Thread(dp.new Philosopher(i, fork[i], fork[(i + 1) % 5])).start();    
	    }
	 }

	class Fork {
		private int id;

	    public Fork(int id) {
	        this.id=id;
	    }
	    public int getId() {
	        return id;
	    }
	    public void setId(int id) {
	        this.id = id;
	    }

	}
	class Philosopher implements Runnable {

	    private Fork left;
	    private Fork right;	    
	    private int  id;   
 	    public Philosopher(int id, Fork left, Fork right) {
	    	this.id = id;
	    	if(left.getId()<right.getId()){
	            this.left = left;this.right = right;
	        }else{
	            this.left=right;this.right=left;
	        }

	    }
	    public void run() {
	        while (true) {
	        	try {
	                Thread.sleep(100);
	            } catch (InterruptedException e1) {
	                e1.printStackTrace();
	            }
	        	// System.out.println("Philosopher " + id + " is thinking"); 
	            synchronized (left) {  //
	            	//take the left fork
	            	try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
	            	//System.out.println("Philosopher " + id + " take the left fork"); 
	            	synchronized (right) {   //take the right fork
	            		try {
	                        Thread.sleep(100);
	                    } catch (InterruptedException e1) {
	                        e1.printStackTrace();
	                    }
	            		//System.out.println("Philosopher " + id + " take the right fork"); 
	            		System.out.println("Philosopher " + id + " is eating"); 
	            	}
	            }
	        } 
	    }
	}
}
