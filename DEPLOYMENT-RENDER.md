# Despliegue de Datronix Back-Office en Render

El servicio Render usa el `Dockerfile` de la raíz y el nombre `datronix-backoffice`.
La imagen compila con Java 21, ejecuta las pruebas Maven y corre con un usuario no
privilegiado. Render proporciona `PORT`; la aplicación publica el health check en
`/actuator/health`.

## Variables de entorno

Al crear el Blueprint desde `render.yaml`, completa sus variables `sync: false`
desde el panel de Render:

- `SPRING_DATASOURCE_URL`: JDBC CockroachDB con TLS, por ejemplo
  `jdbc:postgresql://<host>:26257/defaultdb?sslmode=verify-full&sslrootcert=/etc/secrets/root.crt`.
- `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`: usuario y contraseña
  de base de datos.
- `SPRING_MAIL_USERNAME` y `SPRING_MAIL_PASSWORD`: cuenta SMTP y contraseña de
  aplicación del proveedor de correo.
- `DATRONIX_CORS_ALLOWED_ORIGINS`: origen o lista de orígenes del frontend,
  separados por comas.

Sube el certificado raíz de CockroachDB como un **Secret File** de Render llamado
`root.crt`; Render lo monta como `/etc/secrets/root.crt`. No agregues el certificado
ni credenciales al repositorio o a la imagen Docker. La URL local que usa Windows
no es válida dentro del contenedor, por eso Render debe recibir su propia
`SPRING_DATASOURCE_URL`.

La configuración prueba STARTTLS en el puerto 587 y establece tiempos máximos de
conexión, lectura y escritura. La compilación no prueba la entrega SMTP: después
del despliegue, confirma que el plan y el proveedor permitan salida al puerto 587.
Si la conexión está bloqueada, se necesitará un proveedor de correo accesible por
HTTPS/API.

La aplicación conserva `spring.jpa.hibernate.ddl-auto=update`; el usuario de base
de datos debe tener permisos para consultar y actualizar el esquema. Revisa el
health check y los logs del servicio tras el primer despliegue.
