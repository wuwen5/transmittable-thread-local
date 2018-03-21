package com.alibaba.ttl.threadpool.agent.demo;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public final class AgentDemo {
	
    private static TransmittableThreadLocal<String> stringTransmittableThreadLocal = new TransmittableThreadLocal<>();

    private static TransmittableThreadLocal<Person> personReferenceTransmittableThreadLocal = new TransmittableThreadLocal<Person>() {
        @Override
        protected Person initialValue() {
            return new Person("unnamed", -1);
        }
    };

    private static TransmittableThreadLocal<Person> personCopyTransmittableThreadLocal = new TransmittableThreadLocal<Person>() {
        @Override
        protected Person copy(Person parentValue) {
            // copy value to child thread
            return new Person(parentValue.getName(), parentValue.getAge());
        }

        @Override
        protected Person initialValue() {
            return new Person("unnamed", -1);
        }
    };
    
    private AgentDemo() {
    	throw new InstantiationError( "Must not instantiate this class" );
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        expandThreadPool(executorService);

        stringTransmittableThreadLocal.set("foo - main");
        personReferenceTransmittableThreadLocal.set(new Person("jerry - reference", 1));
        personCopyTransmittableThreadLocal.set(new Person("Tom - value", 2));

        printTtlInstancesInfo("Main - Before execution of thread pool");

        Future<?> submit = executorService.submit(() -> {
            printTtlInstancesInfo("Thread Pool - enter");
            stringTransmittableThreadLocal.set("foo - modified in thread pool");
            personReferenceTransmittableThreadLocal.get().setName("jerry - reference - modified in thread pool");
            personCopyTransmittableThreadLocal.get().setName("Tom - value - modified in thread pool");
            printTtlInstancesInfo("Thread Pool - leave");
        });
        submit.get();

        printTtlInstancesInfo("Main - After execution of thread pool");

        executorService.shutdown();
        if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
            System.exit(1);
        }
    }

    private static void expandThreadPool(ExecutorService executor) throws Exception {
        List<Future<?>> ret = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            Future<?> submit = executor.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            ret.add(submit);
        }
        for (Future<?> future : ret) {
            future.get();
        }
    }

    private static void printTtlInstancesInfo(String msg) {
        System.out.println("====================================================");
        System.out.println(msg);
        System.out.println("====================================================");
        System.out.println("stringTransmittableThreadLocal: " + stringTransmittableThreadLocal.get());
        System.out.println("personReferenceTransmittableThreadLocal: " + personReferenceTransmittableThreadLocal.get());
        System.out.println("personCopyTransmittableThreadLocal: " + personCopyTransmittableThreadLocal.get());
    }

    public static class Person {
        String name;
        int age;

        public String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
