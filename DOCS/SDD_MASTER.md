Documento Maestro (SDD) – Ecosistema de Crédito BBVA
Introducción

El Banco BBVA desea evolucionar su ecosistema de microcréditos para operar de forma totalmente móvil. En el estado actual existen tres aplicaciones separadas —App Clientes (homebanking escrita en Kotlin), App Fuerza de Ventas (para asesores de campo, en Flutter) y un dashboard administrativo (web en Flutter para Chrome)— que comparten una única base de datos en Supabase. Este Documento Maestro de Especificación (SDD) describe la arquitectura objetivo y las historias de usuario necesarias para que el agente Codex implemente, mediante spec‑driven development, un sistema integrado que cumpla la rúbrica de evaluación del Proyecto Final Móvil y permita ejecutar los 30 casos de originación detallados en los enunciados.

La rúbrica exige que las tres piezas se comporten como un único sistema con flujo de extremo a extremo, compartiendo la misma base de datos y sincronizándose con el núcleo financiero a través de un puente. Un asesor registra la solicitud en campo, el core la evalúa y decide, y el cliente visualiza el crédito, cronograma y movimientos en su app. Además, cada aplicación debe seguir principios de arquitectura en capas/MVVM, implementar seguridad con JWT y RBAC, mantener la integridad referencial de la base de datos y documentar las decisiones de diseño. Este documento traduce esos criterios a especificaciones concretas adaptadas a la arquitectura con Supabase y BBVA.

Objetivos y criterios de evaluación
Integración end‑to‑end – Las apps Clientes, Fuerza de Ventas y el Core (APIs) deben compartir la misma bd_core_mobile y permitir que una solicitud de crédito atraviese todas las capas sin rupturas. Se debe encolar la solicitud en sync_outbox y sincronizarla con el núcleo financiero; el desembolso se refleja en tablas espejo cr_* y aparece en la App Clientes.
Originación en campo – La App Fuerza de Ventas debe proporcionar un flujo completo de originación: cartera offline‑first, ficha del cliente, pre‑evaluación de elegibilidad, consulta de buró/listas (con consentimiento), simulador de cronograma (amortización francesa) y generación de expediente firmado.
Autoservicio en App Clientes – El cliente autenticado podrá consultar su perfil, cuentas de ahorro, créditos (con cronograma de cuotas), movimientos, tarjetas y notificaciones, y registrar solicitudes de crédito u operaciones que impacten la BD.
Seguridad y control de acceso (RBAC + JWT) – Implementar autenticación por DNI/código de asesor, emisión de tokens JWT, almacenamiento seguro (por ejemplo flutter_secure_storage), bloqueo por intentos fallidos y matriz de permisos por rol. Endpoints y pantallas deben validar los permisos en el backend y devolver 401/403 cuando corresponda.
Calidad de datos, arquitectura y documentación – La base de datos debe mantener integridad referencial y consistencia en tablas espejo cr_*, sync_outbox y sync_log. La arquitectura del Core seguirá capas (rutas→controladores→servicios→repositorios→BD) y las apps Flutter/Kotlin seguirán MVVM/offline‑first (domain/data/presentation). Se versionarán los scripts DDL/seed y se documentarán historias de usuario, requisitos funcionales y diagramas.
Arquitectura objetivo
Componentes
Componente	Descripción
Supabase (PostgreSQL)	Almacena la bd_core_mobile, que incluye tablas de clientes (clientes), asesores (asesores), solicitudes de crédito (solicitudes_credito), cronogramas (cr_cronograma_pagos), cuentas de ahorro (cr_cuentas_ahorro) y tablas de sincronización (sync_outbox, sync_log). La integridad referencial se asegura mediante claves foráneas en todo el esquema. Seed data preparado para los 30 casos permitirá pruebas reproducibles.
API Core (FastAPI / Kotlin/Node)	Expuesto como microservicio que actúa como “puente” entre las apps y el núcleo financiero. Contiene endpoints REST para autenticación, operaciones bancarias, originación de crédito y sincronización con el núcleo. Implementa lógica de negocio (pre‑evaluación, cálculo de cuotas, consulta de buró) y encola operaciones en sync_outbox.
App Clientes (Kotlin)	Aplicación Android para usuarios finales del banco. Permite iniciar sesión con DNI/clave, ver cuentas de ahorro, créditos, cronogramas y movimientos; recibir notificaciones; registrar solicitudes de crédito con datos como monto, plazo, destino y garantía; y realizar operaciones de pago/transferencia. Se comunica con la API usando JWT y persiste datos en caché local (Room) para trabajo offline.
App Fuerza de Ventas (Flutter)	Aplicación móvil para asesores de negocios. Gestiona la cartera diaria de clientes, muestra fichas de clientes, registra visitas con geolocalización, realiza pre‑evaluación y consulta de buró, captura documentos/firma, calcula el cronograma de pagos (interfaz de simulador) y envía expedientes al core. Opera offline‑first usando Riverpod y sincroniza cambios con Supabase cuando haya conexión.
Dashboard administrativo (Flutter Web)	Aplicación web para supervisores/administradores. Gestiona usuarios y roles (RBAC), asigna cartera a asesores, aprueba solicitudes pre‑aprobadas, consulta reportes y controla el flujo de sincronización. Permite ver logs de sync_outbox/sync_log y forzar promociones al núcleo financiero.
Núcleo financiero (BBVA core)	Sistema bancario propietario que procesa créditos y pagos. No se modifica directamente, pero se sincroniza mediante sync_outbox/sync_log: el Core envía solicitudes y recibe estados (aprobado, condicionado o rechazado). Tablas espejo cr_* en la bd mobile reflejan los créditos desembolsados y sus cronogramas.
Diferencias respecto al ejemplo del proyecto
Entidad bancaria – Este proyecto usa BBVA en lugar del Banco Andino. En los endpoints y textos, reemplace cualquier referencia a “Banco Andino” por “BBVA”.
Tecnologías cliente – La app de clientes está en Kotlin (Android nativo) y no en Flutter. Se deben usar patrones MVVM con ViewModel, LiveData/StateFlow y persistencia Room. La app de fuerza de ventas sigue en Flutter; la web administrativa también en Flutter pero ejecutada en Chrome.
Motor de base de datos – Se utiliza Supabase (PostgreSQL gestionado) como base única compartida. No hay base separada para el core y el móvil; en su lugar, las tablas cr_* actúan como espejos del núcleo financiero. Asegúrese de definir funciones PostgreSQL para la lógica que requiera ejecución cercana a los datos (p. ej., triggers para actualizar sync_outbox).
Conector MCPO – El agente Codex tiene un MCPO conectado; este debe usar esta especificación para generar automáticamente código y migraciones en ambos lenguajes.
Flujos clave de negocio
Flujo de originación de crédito (30 casos)

El conjunto de 30 casos describe operaciones de crédito empresarial (microempresa) que recorren el flujo completo del ecosistema. Cada caso define el perfil del cliente, el monto, el plazo, la tasa TEA (40.92 % con seguro o 43.92 % sin seguro), la garantía y el destino del crédito. Todas las cuotas son fijas bajo amortización francesa y la cuota se calcula con la fórmula TEM = (1 + TEA)^(1/12) – 1.

El flujo general que debe implementarse en el sistema es:

Registro de solicitud en la App Clientes: el cliente inicia sesión con su DNI y clave y registra una nueva solicitud de crédito seleccionando monto, plazo, destino y garantía. El canal de la solicitud se marca como cliente y el expediente se crea en estado enviado.
Recepción en el Core: la solicitud llega al Core a través de la API, se encola en sync_outbox y se asigna automáticamente a una agencia y a un asesor. El estado pasa a recibido_comite.
Cartera en la App Fuerza de Ventas: el asesor inicia sesión con su código de empleado y visualiza la cartera diaria. La nueva solicitud aparece con tipo de gestión NUEVA_SOLICITUD y prioridad (alta/media/normal) en la lista.
Visita en campo: el asesor visita el negocio del cliente, registra el resultado de la visita (visitado, no_encontrado, etc.), toma geolocalización y agrega observaciones.
Pre‑evaluación y consulta de buró: el sistema ejecuta una pre‑evaluación de capacidad de pago (ingreso estimado, gastos, ratio) y consulta el buró/listas. El último dígito del documento determina la calificación (p. ej., Normal, Deficiente) y, si el cliente está en lista negra, se bloquea la solicitud. La App debe mostrar el resultado al asesor para que confirme que coincide con el caso.
Documentos y firma: el asesor captura fotos del DNI por ambos lados, documentos del negocio, fotos del negocio y de la visita, y recoge la firma digital del cliente. Se almacenan en Supabase Storage y se relacionan a solicitudes_documentos.
Envío al núcleo y comité: el asesor promueve la solicitud al núcleo financiero. El expediente cambia de estado a recibido_comite→en_evaluacion→aprobado/condicionado/rechazado.
Decisión y desembolso: si se aprueba, se genera el cronograma de pagos y se desembolsa a la cuenta de ahorros del cliente. Si se condiciona, se registra la condición (condicion_adicional). Si se rechaza, se anota el motivo y se cierra el expediente. La tabla cr_creditos y cr_cronograma_pagos reciben los datos del crédito desembolsado.
Reflejo en la App Clientes: la App Clientes consulta la tabla cr_creditos y muestra el nuevo crédito, su saldo, estado y cronograma. También permite realizar pagos mensuales que actualizan la tabla cr_movimientos y generan operaciones_cliente.
Flujos adicionales de autoservicio
Gestión de cuentas de ahorro: el cliente puede consultar su saldo, movimientos y abrir una nueva cuenta (cr_cuentas_ahorro). También puede realizar transferencias o recargas, generando registros en cr_movimientos y operaciones_cliente.
Notificaciones y alertas: el sistema envía notificaciones push (Firebase/FCM) al cliente o asesor cuando ocurren eventos como aprobación de crédito, vencimiento de cuota o alerta de mora. Estas notificaciones se almacenan en la tabla notificaciones y se marcan como leídas desde la aplicación.
Pre‑aprobados y campañas: la tabla creditos_preaprobados permite almacenar ofertas de crédito pre‑aprobadas. El dashboard administrativo puede asignarlas a clientes y generar campañas (campanas_activas). La App Clientes mostrará la oferta y permitirá aceptarla.
Cobranza y seguimiento de mora: la App Fuerza de Ventas debe permitir registrar gestiones de cobranza (acciones_cobranza) y alertar al asesor por moras (alertas_cartera). El cronograma debe reflejar días en mora y el asesor puede registrar compromisos de pago.
Diseño de base de datos y modificaciones

La base de datos proporcionada en schema.sql incluye la mayoría de las tablas necesarias. El agente Codex deberá:

Mantener integridad referencial: todas las tablas tienen claves foráneas que garantizan la coherencia (por ejemplo, solicitudes_credito.asesor_id referencia a asesores.id y solicitudes_credito.cliente_id a clientes.id). No elimine registros de clientes ni asesores sin manejar sus dependencias.
Añadir campos específicos a BBVA: si el banco requiere campos adicionales (p. ej., número de contrato BBVA, sucursal de desembolso), agréguelos mediante migraciones. Use migraciones versionadas para que Supabase registre cambios de esquema.
Triggers de sincronización: cree triggers y stored procedures en PostgreSQL para poblar sync_outbox automáticamente cuando se inserten/actualicen solicitudes, créditos, pagos u operaciones. Ejemplo: CREATE OR REPLACE FUNCTION enqueue_sync() ... que inserte una fila en sync_outbox con entidad y payload correspondiente.
Vistas y materializaciones: para mejorar el rendimiento en la App Clientes, defina vistas que combinen créditos y cronograma (p. ej., vw_creditos_cliente) y otra que calcule el resumen de cuentas. Estas vistas se exponen a través de Supabase rest.
Seed data de pruebas: cargue datos base para clientes, asesores, agencias y tablas espejo. Cree 30 expedientes en solicitudes_credito con estados iniciales según los casos. Esto permitirá al agente Codex validar los flujos.
Diseño de API (Core)

El API se implementará siguiendo principios REST y se protegerá con JWT. A continuación se describen los endpoints principales (resumen, no exhaustivo) que el agente Codex debe implementar o adaptar:

Autenticación
Método	Endpoint	Descripción
POST	/auth/login-cliente	Recibe numero_documento y clave. Valida credenciales en usuarios_cliente y devuelve JWT con rol cliente. Bloquea el acceso por 5 intentos fallidos y registra timestamp de último acceso.
POST	/auth/login-asesor	Recibe codigo_empleado y clave. Valida en asesores, devuelve JWT con rol (operador, super_operador, supervisor, administrador) e incluye permisos.
POST	/auth/refresh	Recibe token de refresh y emite nuevo JWT.
POST	/auth/logout	Invalida el token y limpia sesión.
Gestión de clientes y cuentas
Método	Endpoint	Descripción
GET	/clientes/{id}	Devuelve datos de un cliente (perfil, cuentas, créditos, cronograma, tarjetas).
POST	/clientes	Crea un cliente prospecto (usado por asesores en campo). Verifica duplicados por DNI.
PUT	/clientes/{id}	Actualiza datos del cliente (dirección, negocio, contactos). Requiere rol de asesor o administrador.
GET	/cuentas-credito/{clienteId}	Devuelve lista de créditos (cr_creditos) y sus cronogramas (cr_cronograma_pagos).
GET	/cuentas-ahorro/{clienteId}	Devuelve cuentas de ahorro (cr_cuentas_ahorro) con saldos y transacciones (cr_movimientos).
POST	/transferencias	Crea una transferencia o pago desde la App Clientes. Valida saldo y crea registro en operaciones_cliente y cr_movimientos.
Originación de crédito
Método	Endpoint	Descripción
POST	/solicitudes	Crea una solicitud de crédito. Recibe datos del cliente, negocio, monto, plazo, garantía y destino. Calcula la cuota estimada usando TEA y amortización francesa. Inserta registro en solicitudes_credito con estado enviado o borrador, canal cliente o asesor según el origen y encola en sync_outbox.
GET	/solicitudes/cartera/{asesorId}	Devuelve la cartera diaria del asesor (cartera_diaria) con filtros por estado y prioridad.
GET	/solicitudes/{id}	Devuelve detalles de una solicitud, incluyendo pre‑evaluación, buró, documentos y notas internas.
POST	/solicitudes/{id}/visita	Registra resultado de visita (visitado, reagendado, etc.), geolocalización y observaciones. Actualiza cartera_diaria y visitas.
POST	/solicitudes/{id}/pre-evaluacion	Ejecuta la pre‑evaluación: calcula el ratio de gastos sobre ingresos, consulta el score transaccional en scores_transaccionales y determina la elegibilidad (APTO si el puntaje es suficiente). Devuelve puntaje y recomendaciones.
POST	/solicitudes/{id}/consulta-buro	Realiza la consulta de buró y listas simuladas. Devuelve calificación (NORMAL, etc.), número de entidades con deuda, deuda total y días de mayor mora. Si el cliente está en lista negra, cambia el estado a rechazado y registra motivo de bloqueo.
POST	/solicitudes/{id}/documentos	Sube documentos e imágenes a Supabase Storage. Registra los metadatos en solicitudes_documentos.
POST	/solicitudes/{id}/firma	Registra la firma digital del cliente (base64) y actualiza solicitudes_credito.firma_cliente_base64.
POST	/solicitudes/{id}/promover	Envía la solicitud al comité/núcleo. Cambia estado a recibido_comite y encola en sync_outbox.
POST	/solicitudes/{id}/comite	Recibe la resolución del comité (aprobado, condicionado, rechazado). En caso de aprobación, genera registro en cr_creditos y cr_cronograma_pagos con cronograma final (cuotas fijas) y actualiza solicitudes_credito con monto_aprobado, plazo, condicion_adicional si corresponde.
Sincronización con núcleo financiero
Los endpoints /sync/export y /sync/import manejarán la comunicación con el núcleo financiero. El exportador lee filas pendientes de sync_outbox, las envía al núcleo y actualiza estado a procesando/aplicado/error. El importador lee notificaciones del núcleo (p. ej., desembolsos, pagos) y actualiza las tablas espejo cr_*, generando entradas en sync_log.
Las tablas sync_queue y sync_outbox se utilizarán para la sincronización offline de las apps móviles. En el patrón offline‑first, la App Fuerza de Ventas registra localmente las operaciones y, al volver la conexión, un worker sincroniza las operaciones pendientes con el servidor y actualiza la BD local.
Seguridad y control de acceso (RBAC + JWT)
Roles definidos – cliente, asesor, supervisor, administrador. Los permisos se definen en una matriz; por ejemplo, un asesor puede crear solicitudes, registrar visitas y pre‑evaluaciones, pero no puede aprobar créditos ni editar otros asesores. Un cliente puede consultar solamente sus propios productos y crear solicitudes; un supervisor puede aprobar solicitudes condicionales y generar reportes.
Autenticación – Utilice Supabase Auth para gestionar usuarios de clientes y asesores. Emita tokens JWT con claims de rol y expire en 60 minutos. Asegúrese de almacenar los tokens en almacenamiento seguro (flutter_secure_storage en Flutter y EncryptedSharedPreferences en Kotlin).
Autorización en backend – Todos los endpoints deben verificar el rol y las claims del JWT y devolver 401/403 si el usuario no tiene permiso. La lógica no debe confiar en el frontend para bloquear acciones.
Protección contra fuerza bruta – Registre intentos fallidos en asesores y usuarios_cliente. Después de 5 intentos fallidos consecutivos, bloquee la cuenta durante 30 minutos y devuelva un mensaje informativo al usuario.
Auditoría y logs – Use sync_log y un nuevo audit_log (a crear) para registrar acciones importantes (creación de solicitud, aprobación, rechazo, pagos). Incluya timestamp, usuario, rol y detalles de la acción.
Requisitos no funcionales
Rendimiento y escalabilidad: la solución debe manejar al menos 30 casos de solicitudes simultáneas sin degradar el rendimiento. Utilice paginación en listados, consultas parametrizadas y JOIN optimizados. Las apps móviles deben cachear datos y paginar la cartera diaria.
Offline‑first: tanto la App Clientes como la App Fuerza de Ventas deben poder operar sin conexión. Deben persistir datos en bases locales (Room y Hive) y sincronizar al reconectarse, manejando conflictos de manera determinista (preferir últimos cambios con timestamp o idempotencia).
Usabilidad y diseño: siga las guías de diseño de BBVA. Utilice componentes reutilizables y navegación clara. El simulador de cuotas debe mostrar una tabla de cronograma similar a la del enunciado (número de cuota, fecha de pago, cuota, capital, interés y saldo).
Accesibilidad: asegure contrastes adecuados y soporte para lectores de pantalla. Permita ampliar fuentes y usar lenguaje inclusivo.
Internacionalización: soporte idioma español; evite textos duros y use archivos de localización.
Historias de usuario y criterios de aceptación

A continuación se presentan historias de usuario representativas. El agente Codex debe ampliar esta lista en las especificaciones detalladas para cubrir los 30 casos. Cada historia incluye su criterio de aceptación y los endpoints afectados.

HU‑01: Registro de solicitud de crédito (cliente)

Como cliente autenticado, quiero registrar una solicitud de crédito con monto, plazo, destino y garantía, para que BBVA evalúe mi crédito empresarial.

Criterio de aceptación:

El formulario solicita monto (mayor a 500 PEN), plazo (múltiplos de 3 meses hasta 36), destino y garantía.
Al enviar, calcula la cuota referencial usando la tasa TEA (40.92 % o 43.92 % según seguro) y amortización francesa.
Inserta un registro en solicitudes_credito con estado enviado, canal cliente, monto solicitado y cuota estimada. Devuelve el número de expediente.
La solicitud aparece en la cartera del asesor correspondiente en la App Fuerza de Ventas (paso 3 del flujo).
Se genera un evento en sync_outbox para sincronización con el núcleo financiero.

Endpoints involucrados: POST /solicitudes (cliente), GET /solicitudes/cartera/{asesorId}.

HU‑02: Pre‑evaluación de solicitud (asesor)

Como asesor de negocios, quiero ejecutar una pre‑evaluación de la solicitud durante la visita, para determinar la capacidad de pago y elegibilidad antes de enviarla al comité.

Criterio de aceptación:

Desde la ficha del cliente, el asesor ingresa ingresos y gastos mensuales y la aplicación calcula el ratio de gastos (gastos/ingresos) y un puntaje preliminar.
Se consulta el score transaccional del cliente (scores_transaccionales) y se suma a los puntos de campo para obtener el score_final.
La app determina si el cliente es apto (APTO) o no (NO_APTO) según un umbral configurable (p. ej., 400 puntos).
El resultado de la pre‑evaluación se guarda en fichas_campo (campos score_final, segmento_resultante) y se envía al backend.
Si el cliente es NO_APTO, la app sugiere rechazar la solicitud; si es APTO, permite continuar con la consulta de buró.

Endpoints involucrados: POST /solicitudes/{id}/pre-evaluacion, GET /scores_transaccionales/{clienteId} (nuevo), PUT /fichas_campo/{id}.

HU‑03: Consulta de buró y listas (asesor)

Como asesor de negocios, quiero consultar el buró de créditos simulado y las listas negras, para conocer el historial crediticio y si el cliente está inhabilitado.

Criterio de aceptación:

Al solicitar la consulta, la app pide el consentimiento del cliente (firma digital).
La API calcula la calificación del buró basándose en el último dígito del documento del cliente y devuelve: calificación (Normal, Deficiente, etc.), número de entidades con deuda, deuda total y días de mayor mora.
Si el cliente está en lista negra, el endpoint devuelve en_lista_negra = true y la solicitud se actualiza a rechazado con motivo_bloqueo.
Los resultados se almacenan en consultas_buro junto con el JSON de la consulta.
El asesor puede visualizar el resultado y decidir continuar o no.

Endpoints involucrados: POST /solicitudes/{id}/consulta-buro, POST /solicitudes/{id}/firma.

HU‑04: Promoción de solicitud al comité y decisión

Como asesor de negocios, quiero enviar la solicitud al comité/núcleo una vez completada la ficha, para que se evalúe y decida su aprobación.

Criterio de aceptación:

El asesor solo puede promover solicitudes con pre‑evaluación apta y sin lista negra.
El endpoint cambia el estado a recibido_comite y agrega una entrada en sync_outbox.
Un proceso de backend envía la solicitud al núcleo y actualiza sync_log con resultado.
El comité decide (aprobado, condicionado, rechazado) y actualiza la solicitud vía /solicitudes/{id}/comite.
Si se aprueba, se genera cronograma de pagos y se inserta un registro en cr_creditos y cr_cronograma_pagos con cronograma final (cuotas fijas) y actualiza solicitudes_credito con monto_aprobado, plazo, condicion_adicional si corresponde.
Si es condicionado, se registra condicion_adicional y se notifica al cliente para completar la condición; si se rechaza, se actualiza el estado y se notifica.
El cliente puede ver el resultado en su app.

Endpoints involucrados: POST /solicitudes/{id}/promover, POST /solicitudes/{id}/comite, GET /cr_creditos/{clienteId}.

HU‑05: Pagos de cuotas (cliente)

Como cliente con un crédito activo, quiero pagar mis cuotas desde la App Clientes, para reducir mi saldo y evitar moras.

Criterio de aceptación:

La app muestra el cronograma de pagos con el saldo actual y el estado de cada cuota (pendiente, pagada, en_mora).
Al seleccionar una cuota pendiente, la app permite elegir una cuenta de ahorro como origen y confirma el monto exacto a pagar.
El pago crea un registro en operaciones_cliente con tipo pago_cuota y actualiza la tabla cr_cronograma_pagos (marca la cuota como pagada) y cr_movimientos (movimiento de débito).
Si la cuota se paga después de la fecha de vencimiento, se genera una alerta de mora (alertas_cartera) para el asesor.
El cliente puede consultar el historial de pagos en la app.

Endpoints involucrados: GET /cr_cronograma_pagos/{codCuentaCredito}, POST /operaciones_cliente, POST /cr_movimientos.

HU‑06: Gestión de cartera diaria (asesor)

Como asesor, quiero visualizar mi cartera diaria con las solicitudes y créditos asignados, para priorizar mis visitas y gestiones.

Criterio de aceptación:

La API /solicitudes/cartera/{asesorId} devuelve una lista de cartera_diaria ordenada por prioridad y tipo_gestion.
El asesor puede filtrar por estado (pendiente, visitado, en_evaluacion, etc.) y por tipo de gestión (NUEVA_SOLICITUD, SEGUIMIENTO, RECUPERACION_MORA).
La app permite marcar cada entrada como visitada, reagendar o cancelar, lo cual actualiza cartera_diaria.estado_visita.
Las coordenadas de visita se guardan en visitas.
Los cambios se sincronizan con el backend cuando la app recupera conexión.

Endpoints involucrados: GET /solicitudes/cartera/{asesorId}, POST /solicitudes/{id}/visita, PUT /cartera_diaria/{id}.

HU‑07: Administración y supervisión (dashboard)

Como supervisor, quiero gestionar usuarios, roles, campañas y visualizar reportes, para controlar el flujo de originación y seguimiento de créditos.

Criterio de aceptación:

El supervisor puede crear y editar usuarios (asesores, clientes) y asignar roles.
Puede consultar el estado de cada solicitud, aprobar créditos condicionados, asignar cartera y reasignar agencias.
Visualiza reportes de morosidad, rendimiento de asesores, monto desembolsado y número de solicitudes por estado.
Puede gestionar campañas activas (campanas_activas) y ofertar créditos pre‑aprobados.
Solo usuarios con rol supervisor o administrador acceden a estas funciones.

Endpoints involucrados: GET/POST/PUT /asesores, GET/POST/PUT /clientes, GET /reportes, POST /creditos_preaprobados, POST /campanas_activas.

Pautas de implementación para el agente Codex

Para evolucionar la aplicación al estado deseado, el agente Codex debe seguir estas pautas:

Spec‑driven development: Cada historia de usuario debe convertirse en un módulo autocontenible con descripción de comportamiento, inputs/outputs, validaciones y estados. Antes de codificar, genere una especificación de API y de modelo de datos para esa historia y valide que cumpla los criterios de la rúbrica.
Compatibilidad con tecnologías: Codex debe generar código Kotlin para la App Clientes (con ViewModel, Retrofit, Room y Coroutines), Flutter/Dart para la App Fuerza de Ventas y el dashboard (Riverpod, Freezed, Flutter Secure Storage), y Python/FastAPI o Node para el API Core. Asegúrese de que las llamadas a Supabase usen bibliotecas oficiales (supabase_flutter, postgrest o supabase-kt).
Versionamiento y migraciones: Use migraciones (por ejemplo, con dbmate o Supabase CLI) para modificar el esquema. Incluya nuevas tablas y triggers en archivos de migración versionados. Documente el propósito de cada migración.
Pruebas y validación: Para cada módulo generado, escriba pruebas unitarias y pruebas de integración que validen los casos de la rúbrica. Por ejemplo, pruebe que una solicitud rechazada por lista negra no pueda ser promovida, que la cuota calculada coincida con el cronograma especificado y que un pago en mora genere una alerta.
Documentación y diagramas: Produzca diagramas UML (clase, secuencia, estados) y diagramas de componentes que reflejen la arquitectura final. Documente las API con OpenAPI/Swagger. Incluya un README que explique cómo levantar cada componente localmente y cómo ejecutar las pruebas.
Datos de prueba: Cargue los 30 casos del enunciado en la base de datos de desarrollo y automatice su ejecución mediante scripts o fixtures. Asegúrese de que la aplicación maneje correctamente casos aprobados, condicionados y rechazados.
Seguridad y privacidad: No exponga datos sensibles ni claves en el código. Use variables de entorno para claves API y certificados. Aplique encriptación en reposo y en tránsito.
Gestión de errores y resiliencia: Maneje errores de red, conflictos de sincronización y respuestas del núcleo financiero de forma robusta. Implemente reintentos con backoff exponencial en la sincronización y reporte fallas en sync_log.
Internacionalización y diseño: Utilice archivos de localización para textos y un estilo visual coherente con BBVA. Incluya accesibilidad (captchas leíbles, etiquetas) y soporte para modo oscuro.
Conclusión

Este documento maestro define los requisitos funcionales y no funcionales para evolucionar el ecosistema móvil de microcréditos de BBVA mediante spec‑driven development. Al implementar las historias de usuario y flujos descritos, y siguiendo las pautas de seguridad, arquitectura y documentación, el agente Codex podrá generar el código en Kotlin, Flutter y API que cumpla con la rúbrica de evaluación y soporte los 30 casos de originación de crédito detallados en los PDF. La correcta sincronización con el núcleo financiero y la coherencia de datos en Supabase garantizarán que las tres aplicaciones funcionen como un solo sistema integrado y confiable.