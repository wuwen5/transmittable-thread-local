package com.alibaba.ttl.perf.tps;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.perf.Utils;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class CreateTransmittableThreadLocalInstanceTps {

    private CreateTransmittableThreadLocalInstanceTps() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static void main(String[] args) throws Exception {
        TpsCounter tpsCounter = new TpsCounter(2);
        tpsCounter.run(() -> {
            TransmittableThreadLocal<String> threadLocal = new TransmittableThreadLocal<>();
            threadLocal.set(Utils.getRandomString());
        });

        while (true) {
            long start = tpsCounter.getCount();
            Thread.sleep(1000);
            System.out.printf("tps: %d\n", tpsCounter.getCount() - start);
        }
    }
}
