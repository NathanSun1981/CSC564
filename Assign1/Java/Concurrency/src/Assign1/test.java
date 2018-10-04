package Assign1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


public class test {
	public static void main(String[] args) {   
		 ExecutorService executorService = Executors.newCachedThreadPool();  
		  final Semaphore semaphore = new Semaphore(5);
		    for (int index = 0; index < 20; index++) {  
		      final int NO = index;      
		  Runnable runnable = new Runnable() {   
		         @Override        
		    public void run() {  
		              try {       
		             semaphore.acquire();        
		            System.out.println("Accessing:" + NO);        
		            Thread.sleep((long) (Math.random() * 10000));        
		            semaphore.release();             
		          System.out.println("------------------" + semaphore.availablePermits());   
		             } catch (InterruptedException e) {            
		        e.printStackTrace();                }  
		          }      
		  };      
		  executorService.execute(runnable);   
		 }    executorService.shutdown();
		}
}

