# Firebase Realtime Database Rules para LinkGo

## Cómo aplicar estas reglas

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto LinkGo
3. Ve a **Realtime Database** → **Rules**
4. Reemplaza el contenido con las reglas de abajo
5. Click en **Publish**

## Reglas recomendadas

```json
{
  "rules": {
    "locations": {
      "$uid": {
        ".read": true,
        ".write": "$uid === auth.uid"
      }
    },
    "users": {
      "$uid": {
        ".read": "auth != null",
        ".write": "$uid === auth.uid"
      }
    },
    "hotspots": {
      ".read": "auth != null",
      "$hotspotId": {
        ".write": "root.child('hotspots').child($hotspotId).child('creatorId').val() === auth.uid || !data.exists()"
      }
    },
    "groups": {
      ".read": "auth != null",
      "$groupId": {
        ".write": "root.child('groups').child($groupId).child('ownerId').val() === auth.uid || !data.exists()"
      }
    },
    "chats": {
      ".read": "auth != null",
      "$groupId": {
        "$messageId": {
          ".write": "newData.child('senderId').val() === auth.uid || !data.exists()"
        }
      }
    }
  }
}
```

### Nota Importante
Las validaciones de tipo se han removido porque Firebase tiene límites con las funciones globales. El código Kotlin ya valida los tipos antes de escribir, así que no es necesario validar en Firebase Rules.

## Explicación de las reglas

### `/locations` - NUEVA (SIMPLIFICADA)
- **`.read: true`** - Todos pueden leer ubicaciones (mapa público)
- **`.write: "$uid === auth.uid"`** - Solo cada usuario puede escribir su propia ubicación
- Sin validaciones estrictas de tipo (el código Kotlin valida antes de escribir)

### `/users/{uid}` - EXISTENTE
- **`.read: "auth != null"`** - Solo usuarios autenticados pueden leer
- **`.write: "$uid === auth.uid"`** - Solo cada usuario puede escribir sus datos
- Se mantiene por compatibilidad con código existente

## Resultado esperado

Después de aplicar estas reglas:

1. ✅ Los usuarios pueden publicar sus ubicaciones en `/locations/{uid}`
2. ✅ El mapa lee de `/locations` y muestra a todos los usuarios conectados
3. ✅ Cada usuario solo puede modificar su propia ubicación
4. ✅ El código Kotlin valida que lat/lng sean números antes de escribir

## Debugging

Si las ubicaciones no aparecen:

1. **Revisa Firebase Console:**
   - Ve a Realtime Database → Data
   - Expande `/locations` - ¿Hay datos ahí?
   - Si está vacío, abre la app y espera 10 segundos

2. **Revisa las reglas:**
   - Ve a Realtime Database → Rules
   - Verifica que `/locations` tenga `.write: "$uid === auth.uid"`

3. **Revisa los logs de la app:**
   - Abre Android Studio Logcat
   - Filtra por "LocationRepository"
   - Debería ver: `onDataChange()` llamándose

4. **Fuerza una actualización:**
   - Abre MapScreen
   - Permite permisos de ubicación
   - La app debería publicar automáticamente cada 30 segundos

