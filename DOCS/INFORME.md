# 🏦 INFORME DE ARQUITECTURA Y FUNCIONAMIENTO: Ecosistema BBVA Ventas

Este documento detalla la estructura, funcionamiento, despliegue y flujos de negocio del Ecosistema de Fuerza de Ventas desarrollado para el Banco Andino (BBVA). El proyecto se compone de tres plataformas principales interconectadas mediante una base de datos central en **Supabase**.

---

## 🏗️ 1. Arquitectura del Ecosistema (Las 3 Apps)

El ecosistema está fragmentado en tres plataformas (y ramas) independientes que apuntan al mismo backend central en la nube.

### A. Admin Dashboard (Panel Web)
- **Tecnología:** Flutter Web / Dart.
- **Rama en Git:** `AppAdmin` (Carpeta: `admin_dashboard`).
- **Despliegue:** 🟢 **En Producción (Vercel)**
- **URL de Acceso:** [https://dashboardcorebbva.vercel.app](https://dashboardcorebbva.vercel.app)
- **Propósito:** Permite a los supervisores de agencia o administradores monitorear los indicadores principales (KPIs), gestionar la lista de asesores, visualizar el embudo de créditos (Leads -> Aprobados -> Desembolsados) y asignar o delegar de forma manual la cartera a los asesores de campo.

### B. App Fuerza de Ventas (Asesores)
- **Tecnología:** Flutter (Mobile - Android).
- **Rama en Git:** `AppAsesores` (Carpeta raíz `lib/`).
- **Despliegue:** APK Android (Compilación local y distribución interna).
- **Propósito:** Es la herramienta de trabajo "Offline-first" para los analistas de crédito. Les permite visualizar sus tareas del día, registrar su ubicación (GPS), llenar fichas de evaluación y levantar documentos de negocio. Cuenta con un sistema de cola (`sync_outbox`) impulsado por Workmanager para funcionar sin internet y sincronizar todo al final del día.

### C. App Clientes (Banca Móvil)
- **Tecnología:** Native Android (Kotlin / Jetpack Compose).
- **Rama en Git:** `AppClientes` (Carpeta: `KOTLINUSER`).
- **Despliegue:** APK Android nativo.
- **Propósito:** Aplicación final directa al cliente. Permite explorar productos, solicitar nuevos préstamos, consultar el estado de su cuenta bancaria (movimientos, ahorros), acceder a funcionalidades de pago (ej. Plin) y seguir el estado de sus evaluaciones de crédito en tiempo real.

---

## 🔄 2. Flujos Principales de Negocio

La magia del ecosistema radica en la integración a través de la base de datos PostgreSQL de Supabase. A continuación, los flujos clave:

### Flujo 1: Solicitud de Crédito por el Cliente (App Kotlin)
1. El **Cliente** inicia sesión en la App nativa de Banca Móvil (Kotlin).
2. Navega a la sección de "Préstamos" y completa un formulario solicitando un monto y plazo (Simulador).
3. El registro se guarda en la tabla `solicitudes_credito` o `solicitudes_prestamo` en Supabase bajo el estado `Pendiente de Evaluación`.

### Flujo 2: Delegación y Asignación de Tareas (Admin Dashboard)
1. El **Administrador / Supervisor** ingresa al panel web desde la URL de Vercel.
2. Revisa la lista general de nuevas solicitudes entrantes y de campañas vigentes.
3. Selecciona una solicitud, un Lead o un "Caso" (tabla `bbva_casos_credito`) y lo **delega o asigna manualmente** a un Asesor de campo específico, basándose en la geografía o la carga de trabajo de su equipo.
4. Supabase actualiza el registro con el ID del Asesor.

### Flujo 3: Evaluación y Aprobación en Campo (App Flutter Asesores)
1. El **Asesor** abre su App Flutter (incluso sin internet) y carga su cartera diaria (`cartera_diaria` y casos asignados).
2. Se dirige físicamente al local del cliente usando las funciones de enrutamiento y mapas en la app.
3. Durante la visita, ejecuta la *Preevaluación*: toma fotos del DNI, fotografías del negocio (`solicitudes_documentos`), captura firmas y ajusta la oferta del crédito en base al flujo de caja.
4. El Asesor marca el crédito como `Aprobado` (o Pre-aprobado) directamente en el celular.
5. Los datos se envían a Supabase (o se encolan si no hay señal). Una vez impactados, el **Cliente recibe instantáneamente la notificación en su App de Kotlin** y el **Administrador ve reflejada la métrica de éxito en Vercel**.

---

## 🔑 3. Credenciales de Prueba (Demo)

Para evaluar y probar el ecosistema en todas sus plataformas, se han habilitado cuentas de prueba en el módulo Supabase Authentication. La contraseña por defecto de demo para ingresar en cualquiera de estos perfiles es genérica (usualmente `123456`, `password123` o la configurada en tu ambiente de desarrollo).

### 👑 Administrador (Probar en Dashboard Vercel)
Ideal para verificar las tablas globales de usuarios y derivar casos de ventas:
* **Usuario:** `admin.demo@bbva.pe`
* *(Abre el link de Vercel y utiliza este correo para gestionar el panel).*

### 💼 Asesores de Campo (Probar en App Flutter)
Con estas cuentas se puede evaluar la carga de la cartera diaria y los flujos offline:
* **Asesor 1:** `asesor01@asesores.pe`
* **Asesor 2:** `asesor02@asesores.pe`
* **Asesor 3:** `ana.quiroz19@asesores.pe` o `and.pallan1@asesores.pe`

### 👤 Clientes (Probar en App Kotlin)
Con estas cuentas se ingresa directamente desde la app instalada en el dispositivo móvil Android para simular ser un usuario del banco:
* **Cliente Caso 1:** `caso01.cliente@bbva.pe` (Recomendado para simular nueva solicitud)
* **Cliente Caso 2:** `caso02.cliente@bbva.pe`
* **Cliente Caso 3:** `caso03.cliente@bbva.pe`
* **Cliente General:** `client.demo@bbva.pe`
