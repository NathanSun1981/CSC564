

import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.locks.Condition;  
import java.util.concurrent.locks.Lock;  
import java.util.concurrent.locks.ReentrantLock;

public class ProducerCustomer {

    private final int MAX_SIZE = 4;  
    
    private LinkedList<Object> list = new LinkedList<Object>();  
    private final Lock lock = new ReentrantLock();    
    private final Condition fullLock = lock.newCondition();    
    private final Condition emptyLock = lock.newCondition();  
    
    public static void main(String[] args) {
    	
    	long startTime=System.currentTimeMillis();  
    	Vector<Thread> threads = new Vector<Thread>();
    	
        ProducerCustomer pc = new ProducerCustomer();
        for(int i = 0; i < 100; i++)
        {
	      	Thread iThread =  new Thread(pc.new Producer(i));
        	threads.add(iThread);    
        	iThread.start();   	
	       	
	       	Thread jThread =  new Thread(pc.new Consumer(i));
        	threads.add(jThread);    
        	jThread.start();
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
  
    class Producer implements Runnable {
    	
    	private int  id;
        public Producer(int id)
        {
            this.id = id;
        }
    	
        @Override
        public void run()  
        {  
        	for (int i = 0; i < 100; i++) {
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
    		for (int i = 0; i < 100; i++) {
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
