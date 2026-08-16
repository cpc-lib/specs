-- Run against ev_charging_platform after a chaos scenario.
-- The intent is to locate recovery debt; nonzero rows require investigation, not automatic deletion.

SELECT 'asset_outbox_stuck' metric, COUNT(*) value
FROM event_outbox
WHERE status=1 AND locked_until < NOW(3)
UNION ALL
SELECT 'core_active_connector_without_session', COUNT(*)
FROM connector_active_session a
LEFT JOIN charging_session s ON s.id=a.session_id
WHERE s.id IS NULL
UNION ALL
SELECT 'payment_unknown', COUNT(*)
FROM payment_order WHERE status='UNKNOWN'
UNION ALL
SELECT 'open_callback_dead', COUNT(*)
FROM open_partner_callback_task WHERE status='DEAD'
UNION ALL
SELECT 'regulatory_dead', COUNT(*)
FROM open_regulatory_report_task WHERE status='DEAD';
