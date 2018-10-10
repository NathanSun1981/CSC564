

import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReaderWriter {
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();   

    public static void main(String[] args) {    
    	
    	long startTime=System.currentTimeMillis();  
    	Vector<Thread> threads = new Vector<Thread>();
    	
    	ReaderWriter rw = new ReaderWriter();

    	for(int i = 0; i < 500; i++)
        {

        	Thread iThread =  new Thread(rw.new Writer(i));
        	threads.add(iThread);    
        	iThread.start();
        		
        }
        for(int i = 0; i < 1000; i++)
        {
	       	Thread iThread =  new Thread(rw.new Reader(i));
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
  
    class Writer implements Runnable {
    	private int  id;    	
        public Writer(int id)
        {
            this.id = id;
        }
        public void run()  
        {       	
        	try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } 
        	System.out.println("writer " + id + " is ready to write"); 
        	lock.writeLock().lock();
	        System.out.println("writer " + id + " is writing"); 
	        lock.writeLock().unlock();  
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
    		try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
    		System.out.println("reader " + id + " is ready to read"); 

    		lock.readLock().lock();
            System.out.println("reader " + id + " is reading"); 
            lock.readLock().unlock();
        } 
    }
}

