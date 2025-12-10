CREATE INDEX idx_history_municipality_issuer_created
    ON history(municipality_id, issuer, created_at);

CREATE INDEX idx_history_municipality_party_created
    ON history(municipality_id, party_id, created_at);

CREATE INDEX idx_history_municipality_message
    ON history(municipality_id, message_id);

CREATE INDEX idx_history_municipality_batch
    ON history(municipality_id, batch_id);

CREATE INDEX idx_history_municipality_delivery
    ON history(municipality_id, delivery_id);

CREATE INDEX idx_history_municipality_batch_issuer_created
    ON history(municipality_id, batch_id, issuer, created_at);

CREATE INDEX idx_history_municipality_message_type
    ON history(municipality_id, message_id, message_type);

CREATE INDEX idx_history_municipality_message_issuer
    ON history(municipality_id, message_id, issuer);

DROP INDEX idx_history_municipality_id ON history;
DROP INDEX idx_history_message_id ON history;
DROP INDEX idx_history_batch_id ON history;
DROP INDEX idx_history_delivery_id ON history;
DROP INDEX idx_history_issuer ON history;
