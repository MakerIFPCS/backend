ALTER TABLE production_orders
    ALTER COLUMN file_name DROP NOT NULL,
    ALTER COLUMN content_type DROP NOT NULL,
    ALTER COLUMN file_data DROP NOT NULL;

ALTER TABLE production_orders
    DROP CONSTRAINT IF EXISTS production_orders_status_check,
    DROP CONSTRAINT IF EXISTS production_orders_type_check;

ALTER TABLE production_orders
    ADD CONSTRAINT production_orders_status_check
        CHECK (status IN ('PENDING', 'WAITING_FOR_UPDATE', 'APPROVED', 'REJECTED')),
    ADD CONSTRAINT production_orders_type_check
        CHECK (type IN ('LASER_CUTTING', 'THREE_D_PRINTING', 'CNC_MACHINING'));
