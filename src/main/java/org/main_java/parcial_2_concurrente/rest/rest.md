# Clases REST (Resource) en el Proyecto

## ¿Qué son las Clases REST?

Las clases REST, también conocidas como controladores REST, son componentes que manejan las solicitudes HTTP y proporcionan respuestas HTTP. Actúan como intermediarios entre el cliente y la lógica de negocio de la aplicación, mapeando URLs a métodos específicos y delegando la lógica a los servicios.

### Funciones Principales de las Clases REST

- **Manejo de Solicitudes HTTP**: Las clases REST reciben solicitudes HTTP (GET, POST, PUT, DELETE) y las procesan.
- **Mapeo de URLs**: Utilizan anotaciones como `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc., para mapear las solicitudes a métodos específicos.
- **Interacción con Servicios**: Delegan la lógica de negocio a los servicios (`Service`) de la aplicación.
- **Conversión de Datos**: Transforman los datos de las entidades a DTOs (Data Transfer Objects) y viceversa, para asegurar que solo la información necesaria se envíe al cliente.
- **Manejo de Respuestas**: Devuelven respuestas HTTP adecuadas, como `ResponseEntity`, que pueden incluir códigos de estado HTTP y cuerpos de respuesta.

## ¿Por qué no usamos Clases REST en este Proyecto?

### No Utilizamos Recursos Externos

En este proyecto, no utilizamos recursos externos como APIs, scripts u otros servicios. Todos los datos provienen de nuestra propia base de datos reactiva (MongoDB). Nuestra API se centra en enviar datos al frontend y no en recibir datos de fuentes externas. Esto simplifica la arquitectura y elimina la necesidad de implementar clases REST para manejar datos externos.

### Enfoque en la Base de Datos Reactiva

Todos los datos en este proyecto provienen de una base de datos reactiva (MongoDB). Nuestra API se centra en enviar datos al frontend y no en recibir datos de fuentes externas. Esto simplifica la arquitectura y se alinea mejor con el enfoque reactivo de WebFlux.

### Responsabilidad de los Controladores

Los controladores en WebFlux manejan las solicitudes HTTP de manera reactiva, delegando la lógica de negocio a los servicios. La interacción con la base de datos y la lógica de negocio se manejan de manera reactiva en los servicios, reduciendo la complejidad de los controladores.

### Documentación y Claridad

En lugar de implementar clases REST que no se utilizarán, hemos documentado claramente las razones por las cuales no se implementan estas clases en este contexto. Esto proporciona claridad y evita confusiones futuras sobre el diseño y las decisiones arquitectónicas del proyecto.

## Conclusión

La decisión de no implementar clases REST en este proyecto se basa en que no utilizamos recursos externos (APIs, scripts, etc.) y en la naturaleza de nuestra fuente de datos (base de datos reactiva). Esta decisión mejora la escalabilidad y la eficiencia de la aplicación, alineándose con los principios de programación reactiva.
