package org.main_java.parcial_2_concurrente.config;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

/**
 * Configuración de desarrollo local que permite el acceso al sistema de archivos del proyecto
 * para uso en desarrollo sin depender de motores de vista.
 */
@Configuration
@Profile("local")
public class LocalDevConfig {

    @SneakyThrows
    public LocalDevConfig() {
        // Ubica el archivo application.yml en el classpath
        final ClassPathResource applicationYml = new ClassPathResource("application.yml");

        if (applicationYml.isFile()) {
            File sourceRoot = applicationYml.getFile().getParentFile();

            // Busca la raíz del proyecto (donde se encuentra el archivo "mvnw")
            while (!new File(sourceRoot, "mvnw").exists() && sourceRoot.getParentFile() != null) {
                sourceRoot = sourceRoot.getParentFile();
            }

            // Comprueba si se ha localizado el directorio raíz correctamente
            if (new File(sourceRoot, "mvnw").exists()) {
                System.out.println("Directorio raíz del proyecto encontrado: " + sourceRoot.getPath());
            } else {
                System.err.println("No se encontró el directorio raíz del proyecto. Verifica la estructura del proyecto.");
            }
        } else {
            System.err.println("Archivo application.yml no encontrado en el classpath.");
        }
    }
}
