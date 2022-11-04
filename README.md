---
title: 自旋锁
date: 2022-10-07 20:52:46
tags:
---
### Test-and-Set Lock

最基本的锁，每个线程共用一把锁，进入临界区之前看没有有线程在临界区，如果没有，则进入，并上锁，如果有则等待。AtomicBoolean的原子特性保证一次只有一个线程可以获得到锁。

```java
public class TASLock{
    AtomicBoolean state = new AtomicBoolean(false);
    public void lock() {
        while (state.getAndSet(true)) {
        }            
    }
    public void unlock() {
        state.set(false);
    }
}
```

### Test-Test-and-Set Lock

第一步先去检测 lock是否空闲，只有一个线程先观测到lock是空闲的，该线程才会尝试的去获取它，从而消除一部分回写操作。

```java
public class TTASLock {
    private AtomicBoolean state = new AtomicBoolean( false );
    public void lock() {
        while ( true ) {
            while (state.get()) {
            }
            if (!state.getAndSet( true )) {
                break ;
            }
        }
    }
    public void unlock() {
        state.set( false );
    }
}
```

### Exponential Backoff Lock

给后进入自旋的线程一个延迟避让，使用避让策略为指数回退。

```java
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
}
```

### 测试与分析

1. 并发对象：计数器（counter），初始值为0
2. 并发任务：n线程互斥访问并发计数器，总共访问1百万次临界区：counter = counter + 1。针对每

针对每一种锁，设置线程数从1-20，统计所需执行时间。并重复10次实验取平均值。

```java
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
            System.out.println(i + ":" + res[i]/10);
        }
    }
}

class Testlock implements Runnable {
    int ticketNums = 1000000;
    long startTime = System.currentTimeMillis();
    long endTime = 0;
    //定义lock锁 
    private final TASLock lock = new TASLock();
    //private final TTASLock lock = new TTASLock();
    //private final BackoffLock lock = new BackoffLock();
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
```

上面的计时有问题，应该对任务统一计时：

```java
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
```

最后得到如下结果：

<img src="https://cdn.jsdelivr.net/gh/diaokaizi/image@main/image-20221007204425768.png" alt="image-20221007204425768" style="zoom:67%;" />

随着线程数的增加，耗时也呈增加的趋势，且整体耗时TAS>TTAS>Backoff。

TAS算法存在着大量的总线占用，每个线程每一次自旋都会产生大量的总线流量，从而延迟其他线程。TTAS 的优势在于第一步只尝试读取数据，每次都从CPU cache 中进行读取，而不会占用总线。Backoff使用的办法是让尝试获取锁没有成功的线程 后退一段时间之后,再去尝试获取锁,由此来降低争用。

