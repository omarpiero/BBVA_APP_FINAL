El estudiante actúa en dos roles sobre el mismo expediente: primero como cliente que registra la solicitud
desde su app, y luego como asesor de negocios que la recibe en su cartera, la evalúa en campo y la lleva
hasta el desembolso.
Tarifario aplicado
Crédito Empresarial — Microempresa. TEA 40.92 % (con seguro de desgravamen) o 43.92 % (sin seguro de
desgravamen). Todas las cuotas son iguales (cuota fija, amortización francesa). La cuota se calcula con la tasa
efectiva mensual TEM = (1 + TEA)^(1/12) − 1.
Flujo que debe seguir el estudiante en cada caso
1. App Clientes — registrar la solicitud. Inicia sesión como cliente (documento + clave) y registra la
solicitud de crédito con los datos del caso (monto, plazo, destino, garantía). El canal de la solicitud
queda como cliente y nace en estado enviado. El sistema devuelve un número de expediente.
2. Core — recepción. La solicitud llega al core y se encola para promoverse al núcleo; queda visible para
la agencia y se asigna al asesor responsable.
3. App Fuerza de Ventas — cartera del día. Inicia sesión como asesor (código de empleado + clave). La
solicitud aparece en la cartera del día con tipo de gestión NUEVA_SOLICITUD. Ubica al cliente y abre su
ficha.
4. Visita en campo. Registra el resultado de la visita (visitado), con la observación y las coordenadas del
negocio del caso.
5. Pre-evaluación y buró. Ejecuta la pre-evaluación por capacidad de pago y la consulta de buró y listas.
Verifica que el resultado coincida con el esperado del caso.
6. Documentos y firma. Adjunta los documentos indicados (documento de identidad por ambos lados,
sustento del negocio, foto del negocio y de la visita) y captura la firma del cliente.
ENUNCIADOS_30_CASOS_CREDITO_FLUJO_MOVIL.md 2026-06-16

1 / 21

7. Envío al core y comité. Promueve la solicitud al núcleo. El expediente avanza por los estados
recibido_comite → en_evaluacion → decisión.
8. Decisión y desembolso. Según la decisión del comité del caso: si es aprobado (o condicionado),
registra el desembolso y genera el cronograma de pagos; si es rechazado, registra el motivo y cierra el
expediente.
Estados del expediente: borrador → enviado → recibido_comite → en_evaluacion → aprobado /
condicionado / rechazado → desembolsado.
Nota sobre el buró simulado: la calificación depende del último dígito del documento del cliente, por
lo que cada caso produce un perfil de buró determinista. Un cliente en lista de inhabilitados bloquea la
solicitud en el paso 5.

Caso 1
Solicitante (rol cliente). Anaximandro Quispe · Documento 40118120 · Teléfono 964110201. Negocio:
Bodega «Bodega Don Anaxi», en El Tambo, 48 meses de antigüedad. Ingreso mensual estimado S/ 2,200.00;
gasto mensual S/ 900.00.
Solicitud registrada desde la App Clientes. Producto Crédito Empresarial — Microempresa. Monto
solicitado S/ 1,000.00; plazo 12 meses; TEA 43.92 % (sin seguro de desgravamen); garantía: sin garantia;
destino: Capital de trabajo: compra de mercaderia. Cuota de referencia mostrada al cliente: S/ 100.95. Estado
inicial: enviado.
Asignación al asesor. Tipo de gestión NUEVA_SOLICITUD, prioridad normal. Visita: resultado visitado;
ubicación del negocio lat -12.0581, lng -75.2027.
Pre-evaluación esperada: APTO (puntaje 85). Buró esperado: NORMAL, 1 entidad(es) con deuda, deuda
total S/ 4,500.00, 0 día(s) de mayor mora.
Decisión del comité: APROBADO. Monto aprobado S/ 1,000.00.
Desembolso el 02/02/2026; cuotas a pagar el día 03 de cada mes, empezando el mes siguiente.
Cuota mensual: S/ 100.95 · Cronograma final (las cuotas son iguales):
N° Cuota Fecha de pago Cuota Capital Interés Saldo
1 03/03/2026 100.95 70.14 30.81 929.86
2 03/04/2026 100.95 72.31 28.64 857.55
3 03/05/2026 100.95 74.53 26.42 783.02
... ... ... ... ... ...
12 03/02/2027 100.95 97.87 3.01 0.00

Caso 2
ENUNCIADOS_30_CASOS_CREDITO_FLUJO_MOVIL.md 2026-06-16

2 / 21

Solicitante (rol cliente). Eulalia Mamani · Documento 41223341 · Teléfono 964110202. Negocio: Restaurante
«Picanteria La Eulalia», en Chilca, 36 meses de antigüedad. Ingreso mensual estimado S/ 3,000.00; gasto
mensual S/ 1,400.00.
Solicitud registrada desde la App Clientes. Producto Crédito Empresarial — Microempresa. Monto
solicitado S/ 3,000.00; plazo 12 meses; TEA 40.92 % (con seguro de desgravamen); garantía: sin garantia;
destino: Compra de cocina industrial. Cuota de referencia mostrada al cliente: S/ 299.59. Estado inicial:
enviado.
Asignación al asesor. Tipo de gestión NUEVA_SOLICITUD, prioridad media. Visita: resultado visitado;
ubicación del negocio lat -12.0921, lng -75.2105.
Pre-evaluación esperada: APTO (puntaje 85). Buró esperado: NORMAL, 2 entidad(es) con deuda, deuda
total S/ 12,000.00, 0 día(s) de mayor mora.
Decisión del comité: APROBADO. Monto aprobado S/ 3,000.00.
Desembolso el 05/02/2026; cuotas a pagar el día 05 de cada mes, empezando el mes siguiente.
Cuota mensual: S/ 299.59 · Cronograma final (las cuotas son iguales):
N° Cuota Fecha de pago Cuota Capital Interés Saldo
1 05/03/2026 299.59 212.60 86.99 2,787.40
2 05/04/2026 299.59 218.76 80.83 2,568.64
3 05/05/2026 299.59 225.11 74.48 2,343.53
... ... ... ... ... ...
12 05/02/2027 299.59 291.10 8.44 0.00

Caso 3
Solicitante (rol cliente). Teofilo Huaman · Documento 42330336 · Teléfono 964110203. Negocio: Carpinteria
«Maderas Huaman», en Pilcomayo, 60 meses de antigüedad. Ingreso mensual estimado S/ 4,200.00; gasto
mensual S/ 1,800.00.
Solicitud registrada desde la App Clientes. Producto Crédito Empresarial — Microempresa. Monto
solicitado S/ 5,000.00; plazo 18 meses; TEA 43.92 % (sin seguro de desgravamen); garantía: sin garantia;
destino: Maquinaria: sierra y cepillo. Cuota de referencia mostrada al cliente: S/ 366.02. Estado inicial:
enviado.
Asignación al asesor. Tipo de gestión NUEVA_SOLICITUD, prioridad media. Visita: resultado visitado;
ubicación del negocio lat -12.0496, lng -75.2486.
Pre-evaluación esperada: APTO (puntaje 85). Buró esperado: NORMAL, 1 entidad(es) con deuda, deuda
total S/ 6,000.00, 0 día(s) de mayor mora.
Decisión del comité: APROBADO. Monto aprobado S/ 5,000.00.
Desembolso el 10/02/2026; cuotas a pagar el día 10 de cada mes, empezando el mes siguiente.
ENUNCIADOS_30_CASOS_CREDITO_FLUJO_MOVIL.md 2026-06-16

3 / 21

Cuota mensual: S/ 366.02 · Cronograma final (las cuotas son iguales):
N° Cuota Fecha de pago Cuota Capital Interés Saldo
1 10/03/2026 366.02 211.99 154.03 4,788.01
2 10/04/2026 366.02 218.52 147.50 4,569.49
3 10/05/2026 366.02 225.25 140.77 4,344.24
... ... ... ... ... ...
18 10/08/2027 366.02 355.18 10.94 0.00