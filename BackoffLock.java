import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

class Backoff {
    int max;
    int min;
    int limit;
    public Backoff(int min, int max, int limit) {
        this.max = max;
        this.min = min;
        this.limit = limit;
    }
    public void backoff() {
        int delay = new Random().nextInt(limit);
        limit = Math.min(max, 2 * limit);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
public class BackoffLock implements Lock{
    private AtomicBoolean state = new AtomicBoolean(false);

    @Override
    public void lock() {
        Backoff backoff = new Backoff(1000, 5000, 100);
        while (true) {
            while (state.get()) {};
            if (!state.getAndSet(true)) {
                return;
            } else {
                backoff.backoff();
            }
        }
    }
    @Override
    public void unlock() {
        state.set(false);
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
    public Condition newCondition() {
        // TODO Auto-generated method stub
        return null;
    }
}