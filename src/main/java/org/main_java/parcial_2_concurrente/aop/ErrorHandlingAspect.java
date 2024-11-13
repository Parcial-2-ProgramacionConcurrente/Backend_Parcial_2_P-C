package org.main_java.parcial_2_concurrente.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ErrorHandlingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingAspect.class);

    // Definir un pointcut para interceptar métodos dentro del paquete "service"
    @Pointcut("execution(* org.main_java.parcial_2_concurrente.service..*(..))")
    public void serviceLayerMethods() {}

    /**
     * Manejo centralizado de excepciones. Registra el error con detalles del metodo y envía alertas si es necesario.
     *
     * @param joinPoint El punto de ejecución donde ocurrió el error.
     * @param exception La excepción lanzada en el metodo interceptado.
     */
    @AfterThrowing(pointcut = "serviceLayerMethods()", throwing = "exception")
    public void handleException(JoinPoint joinPoint, Exception exception) {
        // Obtener el nombre del metodo donde ocurrió la excepción
        String methodName = joinPoint.getSignature().getName();

        // Obtener los argumentos pasados al metodo
        Object[] methodArgs = joinPoint.getArgs();

        // Loggear el error con detalles adicionales
        logger.error("Error en el método: {} con argumentos: {}", methodName, methodArgs, exception);

        // Opcional: Notificar sobre errores críticos si se detectan excepciones específicas
        if (exception instanceof RuntimeException) {
            enviarNotificacionErrorCritico(methodName, exception);
        }
    }

    /**
     * Envía una notificación si se detecta un error crítico. Esto puede integrarse con un sistema de notificaciones.
     *
     * @param methodName Nombre del metodo donde ocurrió el error.
     * @param exception Excepción que fue lanzada.
     */
    private void enviarNotificacionErrorCritico(String methodName, Exception exception) {
        String mensaje = "Error crítico en el método: " + methodName + ". Detalles: " + exception.getMessage();
        // Simulación de notificación
        logger.warn("Notificación de error crítico enviada: {}", mensaje);
    }
}
