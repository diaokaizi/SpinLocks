import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Test {
    public static void main(String[] args) {
        long[] res = new long[21];
        for(int x = 0; x < 10; x++){
            for(int i = 1; i <= 20; i++){
                Testlock testlock = new Testlock();
                for(int j = 0; j < i; j++){
                    new Thread(testlock).start();
                }
                try {
                    Thread.sleep(1000);
                    res[i] += testlock.getruntime();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        for(int i = 1; i <= 20; i++){
            System.out.println(i + ":" + res[i]);
        }
    }
}

class Testlock implements Runnable {
    int ticketNums = 1000000;
    long startTime = System.currentTimeMillis();
    long endTime = 0;
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
        endTime = System.currentTimeMillis();
    }
    public long getruntime(){
        return endTime - startTime;
    }
}