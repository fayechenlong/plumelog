package com.beeplay.easylog.core.disruptor;

import com.beeplay.easylog.core.LogMessage;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName LongEventMain
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 20:15
 * @Version 1.0
 **/
public class LogMessageEventMain {
    public static void main(String[] args) throws Exception
    {
        //1.为这次Event创建一个Factory
        LogMessageEventFactory factory = new LogMessageEventFactory();

        //指定环形缓冲区的大小，必须为2的幂次方，因此底层会通过取模运算来确定位置，2的幂次方性能会更好
        int bufferSize = 1024;

        /**
         *
         * BlockingWaitStrategy 是最低效的策略，但其对CPU的消耗最小并且在各种不同部署环境中能提供更加一致的性能表现
         * WaitStrategy BLOCKING_WAIT = new BlockingWaitStrategy();
         * SleepingWaitStrategy 的性能表现跟BlockingWaitStrategy差不多，对CPU的消耗也类似，但其对生产者线程的影响最小，适合用于异步日志类似的场景
         * WaitStrategy SLEEPING_WAIT = new SleepingWaitStrategy();
         * YieldingWaitStrategy 的性能是最好的，适合用于低延迟的系统。在要求极高性能且事件处理线数小于CPU逻辑核心数的场景中，推荐使用此策略；例如，CPU开启超线程的特性
         * WaitStrategy YIELDING_WAIT = new YieldingWaitStrategy();
         */

        //2.创建disruptor
        //第一个参数为工厂对象，用于创建LongEvent，LongEvent承载着消费的数据
        //第二个参数为环形缓冲区大小
        //第三个参数为线程工厂
        //第四个参数SINGLE(单个生产者)和MULTI(多个生产者)
        //第五个参数定义一种关于生成和消费的策略
//        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, DaemonThreadFactory.INSTANCE);
        Disruptor<LogMessageEvent> disruptor = new Disruptor<>(factory, bufferSize, new DisruptorThreadFactory(), ProducerType.SINGLE,new SleepingWaitStrategy());
        //JDK1.8的Lambdas写法
//        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, new DisruptorThreadFactory(), ProducerType.SINGLE,new YieldingWaitStrategy());

        //3.连接handler，也就是注册消费者(监听器，类似于观察者模式)
        for (int i=1; i <10; i++){  //100w个Client
            disruptor.handleEventsWith(new LogMessageEventHandler("Client"+i+":"));
        }
        //JDK1.8的Lambdas写法
//        disruptor.handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Event: " + event.get()));


        //4.Start the Disruptor, starts all threads running
        disruptor.start();

        //5.从Disruptor获取用于事件发布的环形缓冲区(ringBuffer).
        RingBuffer<LogMessageEvent> ringBuffer = disruptor.getRingBuffer();

        //6.自定义并创建一个事件提供者对象用来发布事件
        LogMessageEventProducer producer = new LogMessageEventProducer(ringBuffer);

        //7.创建一个数据缓冲区用来缓存需要发布的数据
        ByteBuffer bb = ByteBuffer.allocate(8);
        long start = System.currentTimeMillis();
        for (long l = 0; l < 6000000; l++)
        {
            //8.向缓冲区添加需要发布的数据
            LogMessage logMessage=new LogMessage();

            logMessage.setAppName("sasasasas");

            //bb.p;
            //9.发布数据
            producer.onData(bb);//9-1
            //JDK1.8的两种Lambda写法
//            ringBuffer.publishEvent((event, sequence, buffer) -> event.set(buffer.getLong(0)), bb);//9-2
            //这将创建一个捕获Lambda，这意味着在将Lambda传递给publishEvent()调用时，需要实例化一个对象来保存
            //变量bb。这将创建额外的(不必要的垃圾)，因此，如果需要low GC压力，那么传递参数给Lambda的调用应该是首选的
//            ringBuffer.publishEvent((event, sequence) -> event.set(bb.getLong(0)));//9-3


//            TimeUnit.SECONDS.sleep(1);
        }
        //10.关闭disruptor，方法会阻塞，直到所有的Event都被处理完毕
        disruptor.shutdown();
        System.out.println("耗时："+(Long)(System.currentTimeMillis()-start) + "ms");
    }

    //线程工厂，为了保证创建出来的线程都具有相同的特性，譬如是否为守护进程，优先级设置等
    public static class DisruptorThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolnum = new AtomicInteger(1);//线程数目
        private final ThreadGroup group;
        private final AtomicInteger threadnum = new AtomicInteger(1);//线程数目
        private final String namePrefix;//为每个创建的线程添加前缀

        DisruptorThreadFactory(){
            SecurityManager sm = System.getSecurityManager();
            //取得线程组
            group = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + poolnum.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            //创建线程，设置线程组和线程名
            Thread t = new Thread(group, r, namePrefix + threadnum.getAndIncrement(),0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
