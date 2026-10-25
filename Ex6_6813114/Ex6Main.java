package Ex6_6813114;    

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Rawiphon Yaiying 6813114
 */

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
    
    // Setter methods
    public void setRounds(int rn)                    { rounds = rn; }
    public void setRefundRole(boolean role)          { refundRole = role; }
    public void setFirstSim(boolean first)           { firstSim = first; }
    public void setCont(boolean c)                   { cont = c; }

    // Getter methods
    public int getRounds()          { return rounds; }
    public boolean isRefundRole()   { return refundRole; }
    public boolean isFirstSim()     { return firstSim; }
    public boolean isCont()         { return cont; }
    
    public void run() {
        // Loop for shopping simulation in the following 5 steps.
        // Threads must wait for each other to complete each step together.
        while (cont)
        {
            try{
                //  (1) Wait for input from main thread:
                barrier.await(); 
                if (!cont) break;
        
        
                //  (2) Skipped in the first simulation. Otherwise,
                doExchange();
                barrier.await();
        
                //  (3) Each thread identifies its workingBasket in this simulation.
                doCheckBasket();
                barrier.await();
        
        
                //  (4) Each thrad works for #rounds. In each round:
                for(int r = 1; r<= rounds; r++){
                    doCallShop(r);
                    barrier.await();
                }

                //  (5) Refunding thread reset workingBasket back to myBasket
                    if (refundRole) {
                        workingBasket = myBasket;
                    }
                    barrier.await();
                
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    
    
    public void doExchange() { 
        if (!firstSim && refundRole && exchanger != null) {
            System.out.printf("%s >> exchange basket\n", getName());
            try {
                workingBasket = exchanger.exchange(myBasket);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            workingBasket = myBasket;
        }
    }
    
    public void doCheckBasket() { 
        System.out.printf("%s >> working basket = %s balance = %4d\n", 
                          getName(), workingBasket.getName(), workingBasket.getItems());
    }
    
    public void doCallShop(int currentRound) { 
        if (refundRole) {
            sharedShop.refund(workingBasket, currentRound);
        } else {
            sharedShop.buy(workingBasket, currentRound);
        }
    }
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
        items = 100;
        System.out.printf("%s >> reset shop balance to 100\n", Thread.currentThread().getName());

    }
    
        // - Random #items to buy from 1 to 20, but this amount must not
        //   exceed shop balance; update shop balance.
        // - Add #items to thread's workingBasket; update basket balance.
        // - Report thread activity (see example output).
        //
        // - But if shop balance is already 0, report buying failure.
    public synchronized void buy(Basket workingBasket, int round) {
        if (items == 0) {
            System.out.printf("%s >> round %d    cannot buy    Shop balance is 0\n", Thread.currentThread().getName(), round);
            return;
        }

        int buyAmount = (int)(Math.random() * 20) + 1;
        if (buyAmount > items) {
            buyAmount = items;
        }
        
        items -= buyAmount;
        workingBasket.add(buyAmount);
        
        System.out.printf("%s >> round %d    buy %6d    shop balance = %4d    %s balance = %4d\n",
                Thread.currentThread().getName(), round, buyAmount, 
                this.items, workingBasket.getName(), workingBasket.getItems());
    }
    
        // - Random #items to refund from 1 to 20, but this amount must not 
        //   exceed thread's workingBasket balance; update shop balance.
        // - Remove #items from thread's workingBasket; update basket balance.
        // - Report thread activity (see example output).
        //
        // - But if basket balance is already 0, report refunding failure.
    public synchronized void refund(Basket workingBasket, int round)
    {
        if (workingBasket.getItems() == 0) {
            System.out.printf("%s >> round %d    no item to refund    %s balance is 0\n", 
                    Thread.currentThread().getName(), round, workingBasket.getName());
            return;
        }

        int refundAmount = (int)(Math.random() * 20) + 1;
        if (refundAmount > workingBasket.getItems()) {
            refundAmount = workingBasket.getItems();
        }
        
        workingBasket.remove(refundAmount);
        items += refundAmount;

        System.out.printf("%s >> round %d    refund %3d    shop balance = %4d    %s balance = %4d\n",
            Thread.currentThread().getName(), round, refundAmount, 
            this.items, workingBasket.getName(), workingBasket.getItems());
    }
}

////////////////////////////////////////////////////////////////////////////////
class Basket {
    private String name;
    private int    items;
    
    public Basket(String id)    { name  = id; }    
    public String getName()     { return name; }
    public int    getItems()    { return items; } 
    
    public void add(int amount)
    {
        // Add items to basket
        items += amount;
    }

    public void remove(int amount)
    {
        items -= amount;
    }
}
////////////////////////////////////////////////////////////////////////////////
public class Ex6Main {
    ArrayList<ResellerThread> allThreads;
    ArrayList<Basket>         allBaskets;
    Exchanger<Basket>         exchanger;
    CyclicBarrier             barrier;
    Shop                      shop;    
    
    
    public static void main(String[] args) {
        Ex6Main mainApp = new Ex6Main();
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
        for (ResellerThread t : allThreads) {
            t.start();
        }

        boolean firstSim = true, cont = true;
        Scanner sc = new Scanner(System.in);
        
        // Each round of while loop = each simulation
        while (cont)
        {
            //  (1) Main thread gets #rounds from user. 
            //      If rounds > 0, proceed to next steps. Otherwise, print summary.
            System.out.print("main >> Enter #rounds for a new simulation (<=0 to quit) = ");
                int rounds = sc.nextInt();
            
                if (rounds <= 0) {
                    cont = false;
                    for (ResellerThread t : allThreads) {
                        t.setCont(false);
                    }
                    try { 
                        barrier.await(); 
                    } catch (Exception e) {}
                    break;
                }

            //  (2) Main thread reset shop's balance.
            shop.reset();
        
            //  (3) If not firstSim, get IDs of 2 threads who will do refunding. 
            if (!firstSim) {
                int id1 = -1, id2 = -1;
                while (id1 < 0 || id1 > 3) {
                    System.out.print("main >> Enter 1st thread ID (0-3) to do refunding = ");
                    id1 = sc.nextInt();
                }
                while (id2 < 0 || id2 > 3 || id2 == id1) {
                    System.out.print("main >> Enter 2nd thread ID (0-3) to do refunding = ");
                    id2 = sc.nextInt();
                }
                
                for (int i = 0; i < 4; i++) {
                    ResellerThread t = allThreads.get(i);
                    t.setRounds(rounds);
                    t.setFirstSim(false);
                    if (i == id1 || i == id2) {
                        t.setRefundRole(true);
                        t.setExchanger(exchanger);
                    } else {
                        t.setRefundRole(false);
                        t.setExchanger(null);
                    }
                }
            } else {
                for (ResellerThread t : allThreads) {
                    t.setRounds(rounds);
                    t.setFirstSim(true);
                    t.setRefundRole(false);
                    t.setExchanger(null);
                }
            }

            //  (4) Main thread waits until all ResellerThreads complete #rounds.
            try {
                barrier.await();
                barrier.await();
                barrier.await();
                
                for (int r = 1; r <= rounds; r++) {
                    barrier.await(); 
                }
                
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            firstSim = false;
        }

        
        // After all ResellerThreads finish, print final balance of all  baskets
        for (ResellerThread t : allThreads) {
            try { t.join(); } catch (Exception e) {}
        }
        printSummary();       
    }
    
    
    public void printSummary()   
    {
        for (Basket b : allBaskets) {
            System.out.printf("main >> %s balance = %4d\n", b.getName(), b.getItems());
        }
    }
}
