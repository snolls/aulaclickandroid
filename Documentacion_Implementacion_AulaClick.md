# Memoria de Implementación - Proyecto AulaClick

## Aspectos de Desarrollo de Especial Interés

El desarrollo de **AulaClick**, un sistema de reserva de recursos y aulas, ha requerido la integración de una arquitectura cliente-servidor robusta, combinando una aplicación móvil nativa en **Android** con un backend RESTful desarrollado en **Spring Boot**. 

Entre los aspectos más destacados del desarrollo se encuentran:
- **Arquitectura y Diseño:** Se ha seguido el patrón Modelo-Vista-Controlador (MVC) en el backend y una arquitectura limpia en Android.
- **Reserva Dinámica de Recursos:** Se ha implementado un sistema complejo que tiene en cuenta las reglas operativas de cada recurso (como la disponibilidad en fines de semana, horas de apertura y cierre).
- **Validación en Tiempo Real:** En la aplicación Android se ha integrado un sistema de validación cliente-servidor que evita la superposición de reservas y garantiza la coherencia de los datos introducidos por el usuario antes de procesar la solicitud en el backend.
- **Interfaz de Usuario (UI) y Experiencia de Usuario (UX):** Se han aplicado guías de Material Design, con un enfoque particular en el manejo de errores (por ejemplo, validación visual en el inicio de sesión utilizando `TextInputLayout`) y el filtrado por fechas utilizando `MaterialDatePicker`.

## Principales Problemas Encontrados y Soluciones Aplicadas

Durante el ciclo de desarrollo se identificaron y resolvieron diversos retos técnicos:

1. **Errores de Serialización de Fechas entre Android y Spring Boot:**
   - *Problema:* Las fechas y horas de las reservas no se deserializaban correctamente al enviarse desde la aplicación Android al backend, causando errores `400 Bad Request`.
   - *Solución:* Se implementaron anotaciones `@JsonFormat` en las entidades y DTOs del backend para asegurar un formato ISO estandarizado, y se ajustaron los mapeos en Android para procesar estos formatos adecuadamente.

2. **Fallos en la Persistencia de Reservas:**
   - *Problema:* Al intentar crear una reserva enviando la entidad completa, se producían errores debido a relaciones complejas (lazy loading) e información no inicializada.
   - *Solución:* Se migró el proceso de creación de reservas al uso del patrón DTO (`ReservaCrearDTO`). El servicio backend ahora recibe el DTO, busca manualmente los objetos relacionados (Usuario, Recurso) en la base de datos y construye la entidad de forma segura antes de persistirla.

3. **Inconsistencias en el Modelo de Datos (Campos de Imagen):**
   - *Problema:* Las referencias a `imagenUrl` generaban errores de compilación y visualización debido a cambios en los requerimientos.
   - *Solución:* Se realizó una limpieza exhaustiva del código eliminando la lógica depreciada de manejo dinámico de imágenes individuales en `TipoRecurso`, `Reserva` y `RecursoAdapter`, estandarizando temporalmente placeholders para asegurar la estabilidad del UI y simplificar el modelo.

4. **Problemas con el Historial y Seguimiento en Git:**
   - *Problema:* Archivos no rastreados por configuraciones en `.gitignore` y metadatos de autor erróneos en los commits del historial.
   - *Solución:* Revisión y reconfiguración del archivo `.gitignore`. Se utilizó la reescritura de historial (`git filter-branch`) para corregir los nombres y correos de los contribuyentes.

## Funciones Planificadas No Implementadas y Futuras Mejoras

Para futuras iteraciones del proyecto, quedan planteadas las siguientes mejoras y funcionalidades:

1. **Gestión Avanzada de Imágenes y Archivos Multimedia:**
   - Implementar la subida y alojamiento real de imágenes en el servidor o mediante un servicio en la nube (como AWS S3 o Firebase Storage) para personalizar los recursos y perfiles de usuario.

2. **Roles y Permisos Más Granulares:**
   - Añadir una jerarquía de roles más compleja (ej. Super Administrador, Administrador de Departamento, Profesor, Alumno) con vistas y permisos específicos.

3. **Sistema de Notificaciones Push:**
   - Integrar notificaciones (vía Firebase Cloud Messaging) para avisar al usuario sobre la confirmación, modificación o cancelación de sus reservas, así como recordatorios de reservas próximas.

4. **Integración con Calendarios Externos:**
   - Permitir la exportación o sincronización de las reservas con Google Calendar u Outlook.

5. **Generación Automática de Informes:**
   - Un panel para administradores desde el cual puedan exportar a PDF o Excel estadísticas de uso de los recursos.
