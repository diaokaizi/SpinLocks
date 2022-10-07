import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ALock implements Lock {
    AtomicInteger next = new AtomicInteger(0);
    int mySlot;
    boolean[] flag;
    int size;
    ALock (int capacity) {
        size = capacity;
        flag = new boolean[capacity];
        flag[0] = true;
    }
    @Override
    public void lock() {
        mySlot = next.getAndIncrement();
        while (!flag[mySlot % size]) {};
        flag[mySlot % size] = false;
       
    }
    @Override
    public void lockInterruptibly() throws InterruptedException {
        // TODO Auto-generated method stub
        
    }
    @Override
    public boolean tryLock() {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public void unlock() {
        // TODO Auto-generated method stub
        flag[(mySlot+1) % size] = true;
    }
    @Override
    public Condition newCondition() {
        // TODO Auto-generated method stub
        return null;
    }
}