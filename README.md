# APORTE Mobile - Aplicación Nativa Android

Aplicación móvil nativa para Android desarrollada en **Java**, que forma parte del ecosistema **APORTE** para el crowdfunding deportivo amateur. Permite a usuarios, deportistas y patrocinadores consultar campañas activas en tiempo real, crear campañas tanto de Donación Estándar como de Patrocinio (con espacios publicitarios dinámicos según la categoría: indumentaria para deportistas individuales, camiseta y/o instalaciones/predio para clubes, y naming/cartelería para ligas o torneos), realizar aportes económicos, patrocinar espacios publicitarios en la **Camiseta Virtual**, declarar ingresos externos y compartir campañas mediante código QR e integración nativa con redes sociales, rendir gastos adjuntando facturas en **PDF o imagen**, y generar folletos imprimibles con código QR

---

## Arquitectura Técnica Resumida

La aplicación móvil actúa como cliente nativo conectado a la misma API RESTful (`Node.js / Express`) y base de datos (`Cloud Firestore`) y almacenamiento multimedia (`Cloudinary`) que la versión web, garantizando sincronización multiplataforma en tiempo real

Está estructurada mediante separación de responsabilidades en paquetes:

* **`com.aporte.app` (Capa de Presentación / Activities):** Controladores de interfaz de usuario (`MainActivity`, `DetalleCampana`,`CrearCampana` ) encargados del ciclo de vida de pantallas, configuración dinámica de modalidades de patrocinio por categoría, renderizado de progreso y diálogos interactivos, filtrado dinámico de acciones según rol
* **`com.aporte.app.data.api` (Capa de Red):** Configuración del cliente HTTP mediante **Retrofit 2** (`RetrofitClient`), con interceptor automático de tokens JWT de Firebase Auth y definición de contratos de endpoints (`ApiService`)
* **`com.aporte.app.model.data` (Capa de Modelos / DTOs):** Clases serializables mapeadas con **Gson** (`ApiResponse`, `Campana`, `SlotIndumentaria`,`IngresoExterno`, `Gasto`)
* **`com.aporte.app.utils` (Utilidades y Servicios Auxiliares):** Gestión de diálogos de inicio de sesión/registro (`AuthDialogHelper`), subida de imágenes y comprobantes PDF a Cloudinary (`ImageHelper`) y formateo de moneda/fechas (`FormatUtils`)
* **`res/` (Recursos Visuales y Temas):** Layouts XML basados en `MaterialCardView` y `RecyclerView`, temas globales en `themes.xml` y tipografía descargable **Inter** (`res/font/`)

---

## Librerías Principales Utilizadas

* **Firebase Authentication (`firebase-auth`):** Registro, inicio de sesión y obtención de tokens JWT para autenticar peticiones contra la API
* **Retrofit 2:** Cliente HTTP tipado para el consumo asíncrono de la API REST
* **Gson Converter:** Serialización y deserialización automática entre objetos Java y JSON
* **AndroidX AppCompat & Material Design 3:** Componentes de interfaz modernos y compatibilidad entre versiones de Android
* **WebView & PrintManager (Nativo Android):** Renderizado HTML y exportación nativa a PDF para folletos de difusión.
* **CardView & RecyclerView (`androidx.cardview`, `androidx.recyclerview`):** Renderizado eficiente de listados, tarjetas de campañas y actualización por deslizamiento
* **Downloadable Fonts (Google Play Services):** Carga dinámica de la familia tipográfica **Inter**

---

## Variables de Entorno y Credenciales Necesarias

Por motivos de seguridad, las credenciales y archivos de entorno locales están excluidos del control de versiones mediante `.gitignore`

1. Por seguridad, el archivo de configuración de Firebase está excluido del repositorio. Descargá google-services.json (El archivo se encuentra subido en el campus virtual), y ubicalo en
   ```text
   app/google-services.json
   ```

---

## Requisitos Previos

* **Android Studio** (versión Koala / Ladybug o superior recomendada)
* **JDK 17** o superior configurado en Gradle
* **Android SDK** con nivel de API mínimo **27 (Android 8.1)** y compilación en **API 36**
* **Servidor Backend** (`Aporte-Web-Backend`) ejecutándose localmente en el puerto `5000`

---

## Instrucciones de Instalación y Ejecución

### 1. Clonar el Repositorio
```bash
git clone https://github.com/Franco-Martinez-DV/Aporte-Mobile-App.git
cd Aporte-Mobile-App
```

### 2. Abrir y Sincronizar en Android Studio
1. Copiá el archivo google-services.json dentro de Aporte-Mobile-App/app/
2. Iniciá **Android Studio** y seleccioná **Open** (Abrir proyecto existente)
3. Seleccioná la carpeta raíz `Aporte-Mobile-App`
4. Esperá a que **Gradle** descargue las dependencias y finalice la sincronización (*Sync Project with Gradle Files*)

### 3. Configurar la URL del Backend Local
Verificá la constante `BASE_URL` en `app/src/main/java/com/aporte/app/data/api/RetrofitClient.java`:
* **Si ejecutás en el Emulador oficial de Android Studio:** Utilizá `[http://10.0.2.2:5000/api/v1/](http://10.0.2.2:5000/api/v1/)` (dirección puente que apunta al `localhost:5000` de tu computadora)
* **Si ejecutás en un dispositivo físico por USB:** Reemplazá `10.0.2.2` por la dirección IPv4 local de tu PC dentro de la misma red Wi-Fi (por ejemplo, `[http://192.168.1.45:5000/api/v1/](http://192.168.1.45:5000/api/v1/)`)

### 4. Ejecutar la Aplicación
1. Asegurate de tener levantado el servidor backend (`npm run dev` en `Aporte-Web-App/backend`)
2. Seleccioná un dispositivo virtual (AVD) o físico en la barra superior de Android Studio
3. Presioná **Run 'app'** (`Shift + F10`) para compilar e instalar la aplicación
