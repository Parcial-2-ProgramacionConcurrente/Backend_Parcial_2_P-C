package org.main_java.parcial_2_concurrente.aop.sync;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Component
public class SynchronizationAspect {
    private final ReentrantLock lock = new ReentrantLock();

    @Around("@annotation(SynchronizedExecution)")
    public Object synchronizeExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        lock.lock();
        try {
            System.out.println("Synchronized execution started from " + joinPoint.getSignature().getName());
            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }
    }
}
