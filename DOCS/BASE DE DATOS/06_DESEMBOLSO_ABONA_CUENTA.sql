-- Refleja los creditos desembolsados en el saldo visible de la app cliente.
-- Este script es idempotente por referencia de credito:
-- public.transacciones.referencia = public.cr_creditos.cod_cuenta_credito.

create unique index if not exists transacciones_desembolso_credito_ref_uniq
on public.transacciones (referencia)
where categoria = 'desembolso_credito' and referencia is not null;

create or replace function public.bbva_reflejar_desembolso_cuenta()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_auth_user_id uuid;
  v_cuenta_id uuid;
begin
  if coalesce(new.monto_desembolsado, 0) <= 0 then
    return new;
  end if;

  if coalesce(new.estado, '') not in ('vigente', 'desembolsado') then
    return new;
  end if;

  if new.fecha_desembolso is null then
    return new;
  end if;

  if exists (
    select 1
    from public.transacciones t
    where t.categoria = 'desembolso_credito'
      and t.referencia = new.cod_cuenta_credito
  ) then
    return new;
  end if;

  select c.auth_user_id
    into v_auth_user_id
  from public.clientes c
  where c.id = new.cliente_id;

  if v_auth_user_id is null then
    return new;
  end if;

  select cu.id
    into v_cuenta_id
  from public.cuentas cu
  where cu.user_id = v_auth_user_id
    and cu.estado = 'activa'
  order by
    case cu.tipo_cuenta
      when 'ahorros' then 0
      when 'corriente' then 1
      else 2
    end,
    cu.created_at
  limit 1
  for update;

  if v_cuenta_id is null then
    insert into public.cuentas (
      user_id,
      numero_cuenta,
      tipo_cuenta,
      moneda,
      saldo,
      estado
    ) values (
      v_auth_user_id,
      'BBVA' || right(replace(new.cliente_id::text, '-', ''), 10),
      'ahorros',
      'PEN',
      0,
      'activa'
    )
    returning id into v_cuenta_id;
  end if;

  update public.cuentas
  set saldo = coalesce(saldo, 0) + new.monto_desembolsado,
      updated_at = now()
  where id = v_cuenta_id;

  insert into public.transacciones (
    user_id,
    cuenta_id,
    tipo,
    monto,
    descripcion,
    categoria,
    fecha,
    referencia
  ) values (
    v_auth_user_id,
    v_cuenta_id,
    'credito',
    new.monto_desembolsado,
    'Desembolso credito ' || new.cod_cuenta_credito,
    'desembolso_credito',
    new.fecha_desembolso,
    new.cod_cuenta_credito
  )
  on conflict do nothing;

  return new;
end;
$$;

revoke all on function public.bbva_reflejar_desembolso_cuenta() from public;
revoke all on function public.bbva_reflejar_desembolso_cuenta() from anon;
revoke all on function public.bbva_reflejar_desembolso_cuenta() from authenticated;

drop trigger if exists trg_bbva_reflejar_desembolso_cuenta on public.cr_creditos;

create trigger trg_bbva_reflejar_desembolso_cuenta
after insert or update of estado, fecha_desembolso, monto_desembolsado
on public.cr_creditos
for each row
execute function public.bbva_reflejar_desembolso_cuenta();

-- Backfill para creditos ya desembolsados antes de crear el trigger.
update public.cr_creditos
set estado = estado
where estado in ('vigente', 'desembolsado')
  and fecha_desembolso is not null
  and not exists (
    select 1
    from public.transacciones t
    where t.categoria = 'desembolso_credito'
      and t.referencia = cr_creditos.cod_cuenta_credito
  );
