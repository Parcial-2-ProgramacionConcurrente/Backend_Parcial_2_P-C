package org.main_java.parcial_2_concurrente.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceMonitoringAspect {

    // Definir un pointcut para interceptar métodos dentro del paquete "service"
    @Pointcut("execution(* org.main_java.parcial_2_concurrente.service..*(..))")
    public void serviceLayerMethods() {}

    // Medir el tiempo de ejecución de los métodos interceptados
    @Around("serviceLayerMethods()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed(); // Ejecuta el metodo original

        long executionTime = System.currentTimeMillis() - start;
        System.out.println("Método " + joinPoint.getSignature() + " ejecutado en " + executionTime + "ms");

        return result;
    }
}
