# Debugging: Por qué no aparecen los otros miembros del grupo

He agregado logging extenso para entender dónde está el problema. Aquí está cómo debuggearlo:

## Paso 1: Abrir Logcat en Android Studio

1. Abre Android Studio
2. Windows > Logcat (abajo de la pantalla)
3. Filtra por: `MapViewModel` y `LocationRepository`

## Paso 2: Instala y abre la app

```powershell
./gradlew.bat :app:assembleDebug
# Luego instala manualmente en tu dispositivo
```

## Paso 3: Abre MapScreen

Abre la app y navega a MapScreen. En Logcat verás algo como esto:

### Escenario 1: Correcto (debería funcionar)

```
MapViewModel: observeGroupsAndLocations: Starting for myUid=jOsLmNI8zwZg3h90mkEEJO70p8n1

LocationRepository: observeAllLocations: Adding ValueEventListener to /locations
LocationRepository: observeAllLocations.onDataChange: snapshot has 2 children
LocationRepository: Processing uid=jOsLmNI8zwZg3h90mkEEJO70p8n1: lat=40.4168, lng=-3.7038
LocationRepository:   ✓ Created UserLocation for jOsLmNI8zwZg3h90mkEEJO70p8n1: name=Juan, photoUrl=https://...
LocationRepository: Processing uid=qM3UYgbPRfWeEAsqQMR5IGjpTQx2: lat=40.4170, lng=-3.7040
LocationRepository:   ✓ Created UserLocation for qM3UYgbPRfWeEAsqQMR5IGjpTQx2: name=Maria, photoUrl=https://...
LocationRepository: observeAllLocations.onDataChange: Returning 2 locations

MapViewModel: observeAllLocations callback: 2 locations received
MapViewModel:   Location: uid=jOsLmNI8zwZg3h90mkEEJO70p8n1, name=Juan, lat=40.4168, lng=-3.7038
MapViewModel:   Location: uid=qM3UYgbPRfWeEAsqQMR5IGjpTQx2, name=Maria, lat=40.4170, lng=-3.7040
MapViewModel: Updated allLocations: now has 2 entries
MapViewModel: recomputeMemberLocations: selectedGroupId=-Qu9otvwHFbid4AItrHS, foundGroup=true
MapViewModel:   Group members (active): [jOsLmNI8zwZg3h90mkEEJO70p8n1, qM3UYgbPRfWeEAsqQMR5IGjpTQx2]
MapViewModel:   Searching in allLocations: [jOsLmNI8zwZg3h90mkEEJO70p8n1, qM3UYgbPRfWeEAsqQMR5IGjpTQx2]
MapViewModel:   ✓ Found jOsLmNI8zwZg3h90mkEEJO70p8n1 in Firebase: Juan
MapViewModel:   ✓ Using GPS for current user qM3UYgbPRfWeEAsqQMR5IGjpTQx2
MapViewModel:   Final result: 2 locations in groupMemberLocations
```

### Escenario 2: Problema - No hay ubicaciones de Firebase

Si ves:
```
LocationRepository: observeAllLocations.onDataChange: snapshot has 0 children
LocationRepository: observeAllLocations.onDataChange: Returning 0 locations

MapViewModel: recomputeMemberLocations: selectedGroupId=-Qu9otvwHFbid4AItrHS, foundGroup=true
MapViewModel:   Group members (active): [jOsLmNI8zwZg3h90mkEEJO70p8n1, qM3UYgbPRfWeEAsqQMR5IGjpTQx2]
MapViewModel:   Searching in allLocations: []
```

**Solución:** Las ubicaciones NO se están sincronizando desde Firebase. Verifica:
- ¿Existen datos en Firebase Console → Data → /locations?
- ¿Son públicamente legibles? (revisa las reglas)

### Escenario 3: Problema - UIDs no coinciden

Si ves miembros del grupo pero allLocations está vacío:
```
MapViewModel:   Group members (active): [jOsLmNI8zwZg3h90mkEEJO70p8n1, qM3UYgbPRfWeEAsqQMR5IGjpTQx2]
MapViewModel:   Searching in allLocations: [otherUid1, otherUid2]
MapViewModel:   ✗ No location for jOsLmNI8zwZg3h90mkEEJO70p8n1 (not in Firebase, not current user, or no GPS)
```

**Solución:** Los UIDs no coinciden. Compara los valores en Firebase vs los en tu grupo.

## Paso 4: Analiza los logs y reporta

### Si está en Escenario 1: ✅ Todo debería funcionar
Es probable que haya un problema visual (foto no carga) o de renderizado. Continuamos con el siguiente paso.

### Si está en Escenario 2: ❌ Firebase vacío
Los datos NO están llegando a Firebase. Verifica:
1. ¿Las ubicaciones se escriben en `/locations` cada vez que cambias de posición?
2. ¿Hay permisos de ubicación?
3. Mira Logcat en `LocationRepository` para ver si `writeLocation()` se llama

### Si está en Escenario 3: ❌ UIDs no coinciden
Las ubicaciones están en Firebase pero con UIDs diferentes. Esto es un problema de estructura de datos.

---

## Cambios que hice

1. **LocationRepository.kt**
   - Ahora `writeLocation()` INCLUYE `profilePhotoUrl` del usuario actual
   - Agregué logging extenso para debuggear

2. **MapViewModel.kt**
   - Agregué logging en `observeGroupsAndLocations()`
   - Agregué logging en `recomputeMemberLocations()`
   - Ahora llama a `recomputeMemberLocations()` cuando se actualiza la ubicación del usuario (GPS)

---

## Próximos pasos

Una vez identifiques el escenario, dime qué logs ves exactamente y continuaremos desde ahí.

### TL;DR

1. Compila: `./gradlew.bat :app:assembleDebug`
2. Instala y abre la app
3. Abre Logcat
4. Filtra por "MapViewModel" o "LocationRepository"
5. Navega a MapScreen
6. Comparte los logs conmigo

