
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JTextField;



public class MyProblem {
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();   
	
	private static int loop;
	private static long startTime;

    public static void main(String[] args) {    	
    	MyProblem mp = new MyProblem();
    	loop = 0;
    	startTime=System.currentTimeMillis();  

    	for(int i = 0; i < 3; i++)
        {
        	new Thread(mp.new Writer(i)).start();
        }
        for(int i = 0; i < 10; i++)
        {
        	
	       	new Thread(mp.new Reader(i)).start();
        }
    }
  
    
    class Writer implements Runnable {
    	private int  id;    	
    	JTextField text;
        public Writer(int id)
        {
            this.id = id;
        }
        public void run()  
        {     
        	while(true)
        	{   		
	        	try {
	                Thread.sleep(1);
	            } catch (InterruptedException e1) {
	                e1.printStackTrace();
	            } 	        	
	        	lock.writeLock().lock();      
	        	System.out.println("please input your message:");
	    		Scanner sc = new Scanner(System.in);	
	            System.out.println("User" + id + "input is :" + sc.nextLine()); 
	            loop++;
	            if (loop == 10)
	            {
	            	long endTime=System.currentTimeMillis();       
	                System.out.println("All threads elapsed: "+(endTime-startTime)+"ms");
	            }
	            lock.writeLock().unlock();  
        	}
        }  
    }
  
    class Reader implements Runnable {   
    	private int  id;
    	
        public Reader(int id)
        {
            this.id = id;
        }

    	public void run()  
        {   
    		while(true)
    		{
		    	try {
		           Thread.sleep(1);
		        } catch (InterruptedException e1) {
		           e1.printStackTrace();
		        }		
		    	lock.readLock().lock();
		        System.out.println("User " + id + " is reading"); 
		        lock.readLock().unlock();
    		}
        } 
    }
}

