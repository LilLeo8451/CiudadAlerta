# Web API de Ciudad Alerta

La app consume un recurso REST `reportes`. La URL base se cambia en un solo lugar:
`app/src/main/java/com/example/ciudadalerta/data/remote/NetworkModule.kt` → `ApiConfig.BASE_URL` (debe terminar en `/`).

## Endpoints

| Acción     | Método | Ruta                       | Body          | Respuesta       |
|------------|--------|----------------------------|---------------|-----------------|
| Listar     | GET    | `reportes`                 | —             | `[ReporteDto]`  |
| Por usuario| GET    | `reportes?usuarioId={id}`  | —             | `[ReporteDto]`  |
| Ver        | GET    | `reportes/{id}`            | —             | `ReporteDto`    |
| Agregar    | POST   | `reportes`                 | `ReporteDto`  | `ReporteDto` (con `id`) |
| Editar     | PUT    | `reportes/{id}`            | `ReporteDto`  | `ReporteDto`    |
| Eliminar   | DELETE | `reportes/{id}`            | —             | 200 / 204       |

## JSON

```json
{
  "id": 1,
  "categoria": "BACHE",
  "descripcion": "Bache profundo en la calle principal",
  "latitud": null,
  "longitud": null,
  "fecha": "25/10/2026",
  "hora": "10:00",
  "estatus": "PENDIENTE",
  "usuarioId": 2
}
```

- `categoria`: `BACHE` | `ALUMBRADO` | `BASURA` | `FUGA_AGUA`
- `estatus`: `PENDIENTE` | `EN_REVISION` | `RESUELTO`
- `id` lo asigna el servidor. En la app se guarda en `ReporteEntity.remoteId`; el `id` de Room es otro.

## Opción A: MockAPI (recomendada, no requiere servidor propio)

1. Entra a https://mockapi.io, crea un proyecto y un recurso llamado `reportes`.
2. En el esquema agrega los campos: `categoria` (String), `descripcion` (String), `latitud` (Number),
   `longitud` (Number), `fecha` (String), `hora` (String), `estatus` (String), `usuarioId` (Number).
3. Copia la URL del proyecto, por ejemplo `https://6720abcd1234.mockapi.io/api/v1/`, y pégala en `ApiConfig.BASE_URL`.

## Opción B: json-server en tu computadora

```bash
npm install -g json-server@0.17.4
json-server --watch db.json --port 3000 --host 0.0.0.0
```

- `BASE_URL = "http://10.0.2.2:3000/"` (10.0.2.2 es tu PC vista desde el emulador; en un teléfono físico usa la IP de tu PC).
- Como es `http` y no `https`, agrega `android:usesCleartextTraffic="true"` en `<application>` del AndroidManifest.

## Cómo funciona la sincronización (ReportRepositoryImpl)

1. Crear, editar, cambiar estatus o eliminar se hace **primero en Room**, así que la UI responde al instante aunque no haya internet.
2. Después se manda a la API (POST si no tiene `remoteId`, PUT si ya lo tiene). Si responde bien, se marca `sincronizado = true`.
3. Al abrir las pantallas, `sincronizar()` sube lo pendiente y baja lo que hay en el servidor
   (inserta lo nuevo, actualiza lo que cambió y borra lo que ya no existe allá).
4. Las peticiones y su JSON se ven en Logcat filtrando por `okhttp`.

## Fotos en la nube (Cloudinary)

MockAPI no guarda archivos, así que las fotos se suben a **Cloudinary** y en el reporte solo viajan sus URLs.

- Endpoint (Retrofit, `CloudinaryApiService`): `POST https://api.cloudinary.com/v1_1/{cloudName}/image/upload` con `@Multipart` (`file` + `upload_preset`).
- Configuración en `NetworkModule.kt` → `CloudinaryConfig` (`CLOUD_NAME`, `UPLOAD_PRESET`). El preset es **sin firma**, así que la app no guarda ninguna clave secreta.
- En MockAPI el recurso `reportes` tiene un campo `fotos` (lista de URLs):

```json
"fotos": ["https://res.cloudinary.com/ujqvxmq5/image/upload/v1/ciudadalerta/abc123.jpg"]
```

Flujo: al sincronizar un reporte, `MultimediaRepository.subirPendientes()` sube a Cloudinary las fotos que no tienen `urlRemota`, guarda la URL en Room y luego se hace el POST/PUT del reporte con la lista `fotos`. Los demás teléfonos reciben las URLs en `sincronizar()` y Coil muestra las fotos desde internet.
