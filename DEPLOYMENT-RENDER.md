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
- `RESEND_API_KEY`: API key de Resend; configúrala como variable secreta en Render.
- `RESEND_FROM_ADDRESS`: dirección remitente autorizada en Resend. El valor
  predeterminado del Blueprint, `onboarding@resend.dev`, solo sirve para pruebas
  limitadas; para enviar a usuarios reales verifica un dominio en Resend y usa
  una dirección de ese dominio.
- `DATRONIX_CORS_ALLOWED_ORIGINS`: origen o lista de orígenes del frontend,
  separados por comas.

El certificado se obtiene por HTTPS desde el endpoint público de CockroachDB y
queda incluido en la imagen; no es una credencial. Las contraseñas de base de datos
y correo permanecen como variables de entorno y no se copian a la imagen.

La aplicación envía correo mediante la API HTTPS de Resend por el puerto 443, no
por SMTP. La API key no debe guardarse en el repositorio ni en la imagen; debe
configurarse en Render como `RESEND_API_KEY`. La compilación y las pruebas
automatizadas no confirman la entrega real: tras el despliegue, solicita una
recuperación y revisa el estado del mensaje en Resend.

La descarga del certificado ocurre al construir la imagen, así que la construcción
requiere acceso HTTPS al endpoint de CockroachDB. La aplicación conserva
`spring.jpa.hibernate.ddl-auto=update`; el usuario de base
de datos debe tener permisos para consultar y actualizar el esquema. Revisa el
health check y los logs del servicio tras el primer despliegue.
