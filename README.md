# LobbyCore

![Version](https://img.shields.io/badge/version-0.1--SNAPSHOT-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.20+-green)
![Paper](https://img.shields.io/badge/paper-compatible-orange)
![Velocity](https://img.shields.io/badge/velocity-3.x-purple)
![BungeeCord](https://img.shields.io/badge/bungeecord-latest-red)

Sistema moderno y escalable de mensajes de bienvenida para lobbies con integración completa a **GrivyzomCore** y soporte para **placeholders dinámicos en tiempo real**.

## 🌟 Características Principales

### 📡 Integración GrivyzomCore
- **Placeholders dinámicos** que se actualizan automáticamente
- **Datos en tiempo real** del network (jugadores, economía, rankings)
- **Cache inteligente** con TTL para optimizar rendimiento
- **Fallback automático** a datos simulados realistas

### 🎨 Sistema de Bienvenida Avanzado
- **Mensajes personalizables** con gradientes y efectos especiales
- **Títulos animados** con configuración de tiempos
- **Mensajes basados en la hora** del día
- **Tratamiento especial** para jugadores nuevos y VIP
- **Fuegos artificiales** configurables de bienvenida

### 🔧 Items de Acción Inteligentes
- **Sistema de items** completamente configurable
- **Acciones múltiples** (click derecho, izquierdo, shift)
- **Protección avanzada** (anti-drop, anti-move, keep-on-death)
- **Conexión automática** a servidores del proxy
- **Comandos con permisos** temporales de OP

### 🌐 Soporte Multi-Proxy
- **Auto-detección** de Velocity y BungeeCord
- **Canales modernos** y legacy para máxima compatibilidad
- **Manejo inteligente** de errores de conexión
- **Reconexión automática** con sistema de reintentos

## 📊 Placeholders Dinámicos Disponibles

### 👤 Datos del Jugador
```
%grivyzom_player_name%     - Nombre del jugador
%grivyzom_coins%           - Monedas del jugador
%grivyzom_gems%            - Gemas del jugador
%grivyzom_rank%            - Rango del jugador
%grivyzom_level%           - Nivel del jugador
%grivyzom_playtime%        - Tiempo de juego total
```

### 🌐 Datos del Network
```
%grivyzom_online%          - Jugadores online totales
%grivyzom_servers%         - Servidores activos
%grivyzom_status%          - Estado del network
%grivyzom_connection_status% - Estado visual de conexión (●)
%grivyzom_server_name%     - Nombre del servidor actual
%grivyzom_server_uptime%   - Tiempo de actividad
```

### 🏆 Rankings en Tiempo Real
```
%grivyzom_top_coins_1%     - Nombre del #1 en monedas
%grivyzom_top_coins_1_amount% - Cantidad del #1
%grivyzom_top_gems_1%      - Nombre del #1 en gemas
%grivyzom_top_gems_1_amount%  - Cantidad del #1
```

### 💰 Economía Global
```
%grivyzom_economy_total_coins% - Total de monedas del servidor
%grivyzom_economy_total_gems%  - Total de gemas del servidor
%grivyzom_economy_circulation% - Porcentaje en circulación
```

### ⚡ Datos en Tiempo Real
```
%grivyzom_realtime_players% - Jugadores actuales (local)
%grivyzom_realtime_tps%     - TPS del servidor
%grivyzom_realtime_memory%  - Uso de memoria
```

## 🚀 Instalación

1. **Descarga** el archivo `.jar` desde releases
2. **Coloca** el archivo en la carpeta `plugins/` de tu servidor
3. **Reinicia** el servidor para generar archivos de configuración
4. **Configura** `config.yml` e `items.yml` según tus necesidades
5. **¡Listo!** El plugin funciona de inmediato

### 📋 Dependencias Opcionales
- **PlaceholderAPI** - Para soporte completo de placeholders
- **Vault** - Para economía y permisos
- **GrivyzomCore** - Para datos dinámicos del network

## ⚙️ Configuración Rápida

### Configuración Básica
```yaml
welcome:
  enabled: true
  delay: 1
  
  title:
    enabled: true
    title: "&#FF6B6B¡Bienvenido!"
    subtitle: "&7¡Disfruta tu estadía en &b{SERVER}&7!"
  
  messages:
    - "&f¡Hola &b{PLAYER}&f!"
    - "&7Jugadores online: &a{grivyzom_online}"
    - "&7Tus monedas: &e{grivyzom_coins}"
```

### Items de Acción
```yaml
server_selector:
  material: NETHER_STAR
  display-name: '&d🌐 &fSelector de Servidores'
  slot: 0
  flags:
    give-on-join: true
    prevent-drop: true
  actions:
    right-click:
      - '[MESSAGE]&a¡Conectando al servidor!'
      - '[SERVER]survival'
```

## 🎮 Comandos

### Comandos Principales
```
/lobbycore reload              - Recarga la configuración
/lobbycore info                - Información del plugin
/lobbycore test [jugador]      - Prueba mensajes de bienvenida
/lobbycore version             - Versión del plugin
```

### Gestión de Items
```
/lobbycore items list          - Lista items disponibles
/lobbycore items give <item>   - Da un item específico
/lobbycore items reload        - Recarga items de acción
```

### Integración GrivyzomCore
```
/lobbycore grivyzom ping       - Probar conexión
/lobbycore grivyzom status     - Estado de integración
/lobbycore grivyzom stats      - Estadísticas del network
/lobbycore grivyzom data       - Datos de jugador
/lobbycore grivyzom reconnect  - Forzar reconexión
```

### Placeholders Testing
```
/lobbycore placeholders test          - Probar todos
/lobbycore placeholders test player   - Datos del jugador
/lobbycore placeholders test network  - Datos del network
/lobbycore placeholders test economy  - Datos de economía
/lobbycore placeholders list         - Listar disponibles
/lobbycore placeholders refresh      - Actualizar datos
/lobbycore placeholders stats        - Estadísticas del cache
```

## 🔐 Permisos

### Permisos Básicos
```
lobbycore.use              - Uso básico del plugin
lobbycore.admin            - Comandos administrativos
lobbycore.reload           - Recargar configuración
lobbycore.test             - Probar funciones
```

### Permisos de Conexión
```
lobbycore.server.connect           - Usar items de conexión
lobbycore.server.connect.survival  - Conectar a Survival
lobbycore.server.connect.skyblock  - Conectar a SkyBlock
lobbycore.server.connect.minigames - Conectar a Minijuegos
```

## 🎯 Acciones de Items Disponibles

### Comandos y Mensajes
```yaml
actions:
  right-click:
    - '[COMMAND]help'              # Ejecutar como jugador
    - '[COMMAND_OP]gamemode 1'     # Ejecutar con OP temporal
    - '[CONSOLE]give {PLAYER} diamond' # Ejecutar desde consola
    - '[MESSAGE]&a¡Hola {PLAYER}!' # Enviar mensaje
    - '[BROADCAST]&e¡Evento!'      # Mensaje global
```

### Funciones Especiales
```yaml
actions:
  right-click:
    - '[SOUND]ENTITY_PLAYER_LEVELUP:1.0:1.2' # Reproducir sonido
    - '[SERVER]survival'                      # Conectar a servidor
    - '[TELEPORT]0:100:0:world'              # Teletransportar
    - '[GIVE_ITEM]welcome_kit'               # Dar otro item
    - '[CLOSE_INVENTORY]'                    # Cerrar inventario
    - '[DELAY]5:[MESSAGE]&aMensaje tardío!'  # Acción con delay
```

## 🔧 Características Avanzadas

### Sistema de Cache Inteligente
- **TTL automático** para diferentes tipos de datos
- **Limpieza automática** de datos expirados
- **Fallback realista** cuando no hay conexión
- **Métricas de rendimiento** en tiempo real

### Auto-detección de Proxy
- **Detección automática** de Velocity/BungeeCord
- **Canales múltiples** para máxima compatibilidad
- **Manejo de errores** y reconexión automática
- **Configuración manual** si la auto-detección falla

### Sistema de Notificaciones
- **Eventos del network** (hitos de jugadores)
- **Logros de economía** (jugadores en top)
- **Cambios de estado** (conexión/desconexión GrivyzomCore)
- **Alertas administrativas** personalizables

## 📈 Métricas y Debugging

### Estadísticas Disponibles
- **Cache hit/miss ratio** del sistema de placeholders
- **Latencia de conexión** con GrivyzomCore
- **Tiempo de respuesta** de diferentes endpoints
- **Estadísticas de uso** por comando y función

### Debugging
```yaml
debug:
  enabled: true
  placeholder-logging:
    enabled: true
    log-cache-stats: true
  integration-logging:
    enabled: true
    log-messages: true
```

## 🌟 Características Únicas

### 🎨 Efectos de Texto
- **Gradientes personalizables** con colores hex
- **Texto arcoíris** animado
- **Soporte completo** para colores modernos
- **Placeholders en tiempo real** en todos los textos

### 🎆 Fuegos Artificiales Inteligentes
- **Tipos variados** (BALL, STAR, BURST, BALL_LARGE)
- **Colores aleatorios** de una paleta configurable
- **Efectos especiales** (flicker, trail)
- **Posicionamiento inteligente** alrededor del jugador

### 📱 Mensajes Contextuales
- **Mensajes por hora** del día (mañana, tarde, noche)
- **Tratamiento especial** para jugadores VIP
- **Mensajes de regreso** para jugadores ausentes
- **Integración completa** con datos del network

## 🔄 Actualización Automática

### Sistema de TTL (Time To Live)
- **Datos de jugador**: 5 minutos
- **Datos de network**: 1 minuto
- **Top players**: 2 minutos
- **Economía global**: 3 minutos
- **Datos en tiempo real**: Instantáneo

### Sincronización Inteligente
- **Actualización bajo demanda** al usar placeholders
- **Refresh automático** en intervalos configurables
- **Cache predictivo** para placeholders populares
- **Optimización automática** según uso del servidor

## 🛠️ Desarrollo y API

### Integración con Otros Plugins
```java
// Obtener instancia del plugin
LobbyCore lobbyCore = (LobbyCore) Bukkit.getPluginManager().getPlugin("LobbyCore");

// Acceder a managers
ItemActionManager itemManager = lobbyCore.getItemActionManager();
WelcomeMessageManager welcomeManager = lobbyCore.getWelcomeMessageManager();

// Verificar integración GrivyzomCore
boolean isConnected = lobbyCore.isGrivyzomIntegrationActive();
```

### Eventos Personalizados
El plugin dispara eventos personalizados que otros plugins pueden escuchar:
- `PlayerWelcomeEvent` - Cuando se envía mensaje de bienvenida
- `GrivyzomDataUpdateEvent` - Cuando se actualizan datos del network
- `ItemActionExecuteEvent` - Cuando se ejecuta acción de item

## 🐛 Solución de Problemas

### Problemas Comunes

**Los placeholders no se actualizan:**
1. Verifica que PlaceholderAPI esté instalado
2. Usa `/lobbycore grivyzom status` para verificar conexión
3. Ejecuta `/lobbycore placeholders refresh` para forzar actualización

**Items no aparecen al conectarse:**
1. Verifica `action-items.enabled: true` en config.yml
2. Comprueba que `give-on-join: true` en items.yml
3. Revisa permisos del jugador

**Error de conexión con servidores:**
1. Verifica configuración del proxy en config.yml
2. Comprueba que los canales estén registrados correctamente
3. Usa `/lobbycore grivyzom ping` para diagnosticar

### Logs Útiles
```yaml
debug:
  enabled: true
  integration-logging:
    log-messages: true
    log-connection-changes: true
```

## 📞 Soporte

- **Autor**: Francisco Fuentes
- **Web**: [www.grivyzom.com](https://www.grivyzom.com)
- **Discord**: `grivyzom`
- **GitHub**: [Issues y sugerencias](https://github.com/grivyzom/lobbycore)

## 📄 Licencia

Este proyecto está bajo licencia MIT. Ver archivo `LICENSE` para más detalles.

## 🚀 Próximas Funciones

- [ ] **Base de datos** para persistencia de datos
- [ ] **API REST** para integración externa
- [ ] **Sistema de achievements** basado en placeholders
- [ ] **Hologramas dinámicos** con datos en tiempo real
- [ ] **Scoreboard/TAB** integrado con placeholders
- [ ] **Sistema de colas** para conexión a servidores
- [ ] **Multi-idioma** completo con i18n
- [ ] **Editor visual** para configuración de items

---

**¿Te gusta LobbyCore?** ⭐ ¡Dale una estrella al repositorio y compártelo con otros servidores!

---

*Plugin desarrollado con ❤️ para la comunidad de Minecraft por el equipo de GrivyzomNetwork*