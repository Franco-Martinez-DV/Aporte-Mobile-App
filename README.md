# APORTE Mobile - Aplicación Nativa Android

Aplicación móvil nativa para Android desarrollada en **Java**, que forma parte del ecosistema **APORTE** para el crowdfunding deportivo amateur. Permite a usuarios, deportistas y patrocinadores consultar campañas activas en tiempo real, realizar aportes económicos, patrocinar espacios publicitarios en la **Camiseta Virtual**, declarar ingresos externos y compartir campañas mediante código QR e integración nativa con redes sociales

---

## Arquitectura Técnica Resumida

La aplicación móvil actúa como cliente nativo conectado a la misma API RESTful (`Node.js / Express`) y base de datos (`Cloud Firestore`) que la versión web, garantizando sincronización multiplataforma en tiempo real

Está estructurada mediante separación de responsabilidades en paquetes:

* **`com.aporte.app` (Capa de Presentación / Activities):** Controladores de interfaz de usuario (`MainActivity`, `DetalleCampana`) encargados del ciclo de vida de pantallas, renderizado de progreso y diálogos interactivos
* **`com.aporte.app.data.api` (Capa de Red):** Configuración del cliente HTTP mediante **Retrofit 2** (`RetrofitClient`) y definición de contratos de endpoints (`ApiService`)
* **`com.aporte.app.model.data` (Capa de Modelos / DTOs):** Clases serializables mapeadas con **Gson** (`ApiResponse`, `Campana`, `SlotIndumentaria`)
* **`res/` (Recursos Visuales y Temas):** Layouts XML basados en `MaterialCardView` y `RecyclerView`, temas globales en `themes.xml` y tipografía descargable **Inter** (`res/font/`)

---

## Librerías Principales Utilizadas

* **Retrofit 2:** Cliente HTTP tipado para el consumo asíncrono de la API REST
* **Gson Converter:** Serialización y deserialización automática entre objetos Java y JSON
* **AndroidX AppCompat & Material Design 3:** Componentes de interfaz modernos y compatibilidad entre versiones de Android
* **CardView & RecyclerView (`androidx.cardview`, `androidx.recyclerview`):** Renderizado eficiente de listados y tarjetas de campañas
* **Downloadable Fonts (Google Play Services):** Carga dinámica de la familia tipográfica **Inter**

---

## Requisitos Previos

* **Android Studio** (versión Koala / Ladybug o superior recomendada)
* **JDK 17** o superior configurado en Gradle
* **Android SDK** con nivel de API mínimo **24 (Android 7.0)** y compilación en **API 34/35**
* **Servidor Backend** (`Aporte-Web-Backend`) ejecutándose localmente en el puerto `5000`

---

## Instrucciones de Instalación y Ejecución

### 1. Clonar el Repositorio
```bash
git clone https://github.com/Franco-Martinez-DV/Aporte-Mobile-App.git
cd Aporte-Mobile-App
```

### 2. Abrir y Sincronizar en Android Studio
1. Iniciá **Android Studio** y seleccioná **Open** (Abrir proyecto existente)
2. Seleccioná la carpeta raíz `aporte-mobile-android`
3. Esperá a que **Gradle** descargue las dependencias y finalice la sincronización (*Sync Project with Gradle Files*)

### 3. Configurar la URL del Backend Local
Verificá la constante `BASE_URL` en `app/src/main/java/com/aporte/app/data/api/RetrofitClient.java`:
* **Si ejecutás en el Emulador oficial de Android Studio:** Utilizá `[http://10.0.2.2:5000/api/v1/](http://10.0.2.2:5000/api/v1/)` (dirección puente que apunta al `localhost:5000` de tu computadora)
* **Si ejecutás en un dispositivo físico por USB:** Reemplazá `10.0.2.2` por la dirección IPv4 local de tu PC dentro de la misma red Wi-Fi (por ejemplo, `[http://192.168.1.45:5000/api/v1/](http://192.168.1.45:5000/api/v1/)`)

### 4. Ejecutar la Aplicación
1. Asegurate de tener levantado el servidor backend (`npm run dev` en `Aporte-Web-App/backend`)
2. Seleccioná un dispositivo virtual (AVD) o físico en la barra superior de Android Studio
3. Presioná **Run 'app'** (`Shift + F10`) para compilar e instalar la aplicación
