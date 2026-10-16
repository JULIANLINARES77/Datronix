# Despliegue de Datronix Back-Office en Render

El servicio Render usa el `Dockerfile` de la raíz y el nombre `datronix-backoffice`.
La imagen compila con Java 21, ejecuta las pruebas Maven y corre con un usuario no
privilegiado. Render proporciona `PORT`; la aplicación publica el health check en
`/actuator/health`.

## Variables de entorno

El Dockerfile descarga automáticamente el certificado raíz público del clúster
CockroachDB durante la construcción de la imagen y lo deja en `/app/root.crt`.
La URL JDBC del Blueprint ya referencia ese archivo, por lo que no necesitas
cargar un Secret File de certificado ni configurar manualmente su ruta.

Al crear el Blueprint desde `render.yaml`, completa las variables marcadas como
`sync: false` desde el panel de Render:

- `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`: usuario y contraseña
  de base de datos.
- `SPRING_MAIL_USERNAME` y `SPRING_MAIL_PASSWORD`: cuenta SMTP y contraseña de
  aplicación del proveedor de correo.
- `DATRONIX_CORS_ALLOWED_ORIGINS`: origen o lista de orígenes del frontend,
  separados por comas.

El certificado se obtiene por HTTPS desde el endpoint público de CockroachDB y
queda incluido en la imagen; no es una credencial. Las contraseñas de base de datos
y correo permanecen como variables de entorno y no se copian a la imagen.

La configuración prueba STARTTLS en el puerto 587 y establece tiempos máximos de
conexión, lectura y escritura. La compilación no prueba la entrega SMTP: después
del despliegue, confirma que el plan y el proveedor permitan salida al puerto 587.
Si la conexión está bloqueada, se necesitará un proveedor de correo accesible por
HTTPS/API.

La descarga del certificado ocurre al construir la imagen, así que la construcción
requiere acceso HTTPS al endpoint de CockroachDB. La aplicación conserva
`spring.jpa.hibernate.ddl-auto=update`; el usuario de base
de datos debe tener permisos para consultar y actualizar el esquema. Revisa el
health check y los logs del servicio tras el primer despliegue.
