# MyTrains - Gestión de Entrenamiento Personalizado 

**MyTrains** es una aplicación nativa para Android diseñada para entrenadores personales que necesitan gestionar de forma privada y eficiente el seguimiento de sus clientes, rutinas y progresos.

> **Estado del proyecto:** En desarrollo (Work in Progress). Actualmente integrando módulos de geolocalización y sincronización en la nube.

---

##  Tecnologías Implementadas

Este proyecto hace uso de tecnologías avanzadas para ofrecer una experiencia robusta y moderna:

* **Firebase & Google Auth:** Sistema de autenticación seguro para que el entrenador y los clientes accedan a sus perfiles.
* **Google Maps API:** Implementada para la localización de puntos de entrenamiento, gimnasios asociados o seguimiento de rutas externas.
* **Room Database:** Persistencia de datos local para garantizar que el usuario pueda consultar sus tablas y ejercicios incluso sin conexión a internet.
* **Firebase Realtime Database / Firestore:** Sincronización en tiempo real de los entrenamientos entre dispositivos.
* **Kotlin:** Desarrollado íntegramente en Kotlin siguiendo las mejores prácticas de desarrollo Android.

##  Características (WIP)

* [ ] **Perfil del Entrenador:** Panel para gestionar múltiples clientes.
* [ ] **Seguimiento Geográfico:** Visualización de rutas o centros de entrenamiento mediante Google Maps.
* [ ] **Base de datos local:** Almacenamiento de ejercicios y marcas personales mediante Room.
* [ ] **Login con Google:** Acceso rápido y seguro.

---

##  Instalación y Configuración

Debido a que el proyecto utiliza servicios de Google y Firebase, para ejecutarlo localmente necesitarás:

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/SaraChaghale/MyTrains.git](https://github.com/SaraChaghale/MyTrains.git)
    ```
2.  **Configurar Firebase:** Añadir tu propio archivo `google-services.json` en la carpeta `/app`.
3.  **API de Google Maps:** Añadir tu clave de API en el archivo `local.properties`:
    ```properties
    MAPS_API_KEY=TU_API_KEY_AQUI
    ```

---
*Desarrollado por [Sara Chaghale](https://github.com/SaraChaghale)*
