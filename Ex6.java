// Rename the package
package Ex6;    

import java.util.*;
import java.util.concurrent.*;

////////////////////////////////////////////////////////////////////////////////
class ResellerThread extends Thread {
    private Shop                sharedShop;    
    private Basket              myBasket, workingBasket;
    private Exchanger<Basket>   exchanger;         
    private CyclicBarrier       barrier;
    private int                 rounds;
    private boolean             cont = true, firstSim = true, refundRole = false;

    public ResellerThread(String n, Shop sa, Basket bk) { 
        super(n); 
        sharedShop = sa;
        myBasket   = workingBasket = bk; 
    }
    
    public void setBarrier(CyclicBarrier ba)         { barrier = ba; }
    public void setExchanger(Exchanger<Basket> ex)   { exchanger = ex; }
    
    // Add other setter + getter methods
    
    public void run() {
        // Loop for shopping simulation in the following 5 steps.
        // Threads must wait for each other to complete each step together.
        while (cont)
        {
            //  (1) Wait for input from main thread:
            //      - Whether to continue (cont?) + #rounds for simulation
            //      - Thread's role (refundRole?) + Exchanger object
            //      - In the first simulation, all threads are normal threads 
            //        i.e. refundRole = false & Exchanger object = null
        
        
            //  (2) Skipped in the first simulation. Otherwise, 
            //      - Refunding thread (with Exchanger) exchanges baskets.
            //        workingBasket = basket received from the other side
            //
            //      - Normal thread (Exchanger is null) does nothing.
            //        workingBasket = myBasket
            doExchange();
            
        
            //  (3) Each thread identifies its workingBasket in this simulation.
            doCheckBasket();
        
        
            //  (4) Each thrad works for #rounds. In each round:
            //      - Normal thread buys items from sharedShop to workingBasket.
            //      - Refunding thread returns items from workingBasket to sharedShop.
            //      - Update the balances of sharedShop & workingBasket.
            doCallShop();
        
        
            //  (5) Refunding thread reset workingBasket back to myBasket
        }
    }
    
    
    public void doExchange()       { }
    public void doCheckBasket()    { }
    public void doCallShop()       { }
}

////////////////////////////////////////////////////////////////////////////////
class Shop {
    private int items;       //remaining items
     
    public Shop()            { }
    public int getItems()    { return items; }
    
    public void reset() 
    {
        // Reset remaining items to 100
        // Report thread activity (see example output)
    }
    
    public void buy()
    {
        // - Random #items to buy from 1 to 20, but this amount must not
        //   exceed shop balance; update shop balance.
        // - Add #items to thread's workingBasket; update basket balance.
        // - Report thread activity (see example output).
        //
        // - But if shop balance is already 0, report buying failure.
    }
    
    public void refund()
    {
        // - Random #items to refund from 1 to 20, but this amount must not 
        //   exceed thread's workingBasket balance; update shop balance.
        // - Remove #items from thread's workingBasket; update basket balance.
        // - Report thread activity (see example output).
        //
        // - But if basket balance is already 0, report refunding failure.
    }
}

////////////////////////////////////////////////////////////////////////////////
class Basket {
    private String name;
    private int    items;
    
    public Basket(String id)    { name  = id; }    
    public String getName()     { return name; }
    public int    getItems()    { return items; } 
    
    public void add()
    {
        // Add items to basket
    }
}
////////////////////////////////////////////////////////////////////////////////
public class Ex6 {
    ArrayList<ResellerThread> allThreads;
    ArrayList<Basket>         allBaskets;
    Exchanger<Basket>         exchanger;
    CyclicBarrier             barrier;
    Shop                      shop;    
    
    
    public static void main(String[] args) {
        Ex6 mainApp = new Ex6();
        mainApp.initialize();
        mainApp.runSimulation();
    }

    
    public void initialize()
    {
        shop = new Shop();          
        exchanger  = new Exchanger<>();        
        allThreads = new ArrayList<>();
        allBaskets = new ArrayList<>();
        
        for(int i=0; i < 4; i++) 
        {
            Basket basket         = new Basket("B"+i);
            ResellerThread thread = new ResellerThread("T"+i, shop, basket);
            allBaskets.add( basket );
            allThreads.add( thread );            
        }    
        
        barrier = new CyclicBarrier( allThreads.size()+1 );
        for(ResellerThread thread : allThreads)  
        {
            thread.setBarrier(barrier);
        }        
    }
    
    
    public void runSimulation()
    {    
        // Start all ResellerThreads

        boolean firstSim = true, cont = true;
        
        // Each round of while loop = each simulation
        while (cont)
        {
            //  (1) Main thread gets #rounds from user. 
            //      If rounds > 0, proceed to next steps. Otherwise, print summary.
        
            //  (2) Main thread reset shop's balance.
        
            //  (3) If not firstSim, get IDs of 2 threads who will do refunding. 
            //      Make sure that the IDs are valid.
            //      - Pass rounds, refundRole?, Exchanger to ResellerThreads
            //      - Only 2 threads who will do refunding get Exchanger object.
            //        The others get null for Exchanger.
        
            //  (4) Main thread waits until all ResellerThreads complete #rounds.
        }
        
        // After all ResellerThreads finish, print final balance of all baskets
        printSummary();       
    }
    
    
    public void printSummary()   { }
}
