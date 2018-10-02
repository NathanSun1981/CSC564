package Assign1;

import java.util.LinkedList;  
import java.util.concurrent.locks.Condition;  
import java.util.concurrent.locks.Lock;  
import java.util.concurrent.locks.ReentrantLock;  

public class Cache {

    private final int MAX_SIZE = 4;  
    
    private LinkedList<Object> list = new LinkedList<Object>();  
    private final Lock lock = new ReentrantLock();    
    private final Condition fullLock = lock.newCondition();    
    private final Condition emptyLock = lock.newCondition();  
    
    public static void main(String[] args) {
        Cache cache = new Cache();
        for(int i = 0; i < 4; i++)
        {
	      	new Thread(cache.new Producer(i)).start();
	       	new Thread(cache.new Consumer(i)).start();
        }

    }
  
    class Producer implements Runnable {
    	
    	private int  id;
        public Producer(int id)
        {
            this.id = id;
        }
    	
        @Override
        public void run()  
        {  
        	for (int i = 0; i < 10; i++) {
        		try {
                    Thread.sleep(1);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
	        	lock.lock();        
	            while (list.size() >= MAX_SIZE)  
	            {  
	                System.out.println("the size of cache = " + list.size() + ", cannot insert, should wait");  
	                try  
	                {  
	                	fullLock.await();  
	                }  
	                catch (InterruptedException e)  
	                {  
	                    e.printStackTrace();  
	                }  
	            }  
	      
	            list.add(new Object());  
	            System.out.println("producer" + id + " successfully produce one product, the size of cache = " + list.size());        
	            fullLock.signalAll();  
	            emptyLock.signalAll();          	
	            lock.unlock();  
        	}
        }  
    }
  
    class Consumer implements Runnable {   
    	private int  id;
        public Consumer(int id)
        {
            this.id = id;
        }
    	
    	@Override
    	public void run()  
        {   
    		for (int i = 0; i < 10; i++) {
    			try {
                    Thread.sleep(1);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
	            lock.lock();        
	            while (list.size() == 0)  
	            {  
	                System.out.println("the size of cache = " + list.size() + ", cannot get, should wait");  
	                try  
	                {  
	                	emptyLock.await();  
	                }  
	                catch (InterruptedException e)  
	                {  
	                    e.printStackTrace();  
	                }  
	            }  
	            list.remove();  
	            System.out.println("customer" + id + " successfully consume one product, the size of cache = " + list.size());        
	            fullLock.signalAll();  
	            emptyLock.signalAll();        
	            lock.unlock();  
        	}
        }     	
    }
}
