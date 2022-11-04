import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Test {
    public static void main(String[] args) {
        int tn = 10;
        long[] res = new long[tn + 1];
        for(int i = 1; i <= tn; i++){
            Testlock testlock = new Testlock();
            List<Thread> ts = new ArrayList<Thread>();
            for(int j = 0; j < i; j++){
                ts.add(new Thread(testlock));
            }
            long startTime = System.currentTimeMillis();
            ts.forEach(Thread::start);
            ts.forEach(t -> {                    
                try{
                    t.join();
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }
            });
            long endtime = System.currentTimeMillis();
            res[i] += endtime - startTime;
        }
        for(int i = 1; i <= tn; i++){
            System.out.println(i + ":" + res[i]);
        }
    }
}

class Testlock implements Runnable {
    int ticketNums = 1000000;
    //定义lock锁
    private final BackoffLock lock = new BackoffLock();
    @Override
    public void run() {
        while (true) {
            //加锁
            lock.lock();
            try {
                if (ticketNums>0){
                    ticketNums--;
                }
                else break;
            }finally {
                //解锁
                lock.unlock();
            }
        }
    }
}