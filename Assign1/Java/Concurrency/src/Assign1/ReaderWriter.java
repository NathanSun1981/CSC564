package Assign1;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReaderWriter {
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();   

    public static void main(String[] args) {    	
    	ReaderWriter rw = new ReaderWriter();

    	for(int i = 0; i < 3; i++)
        {
        	new Thread(rw.new Writer(i)).start();
        }
        for(int i = 0; i < 10; i++)
        {
	       	new Thread(rw.new Reader(i)).start();
        }
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
