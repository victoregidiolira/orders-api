-- Tabela de clientes
CREATE TABLE customers (
                           id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                           name        VARCHAR(100) NOT NULL,
                           email       VARCHAR(150) NOT NULL UNIQUE,
                           phone       VARCHAR(20),
                           created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
                           updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- Tabela de pedidos
CREATE TABLE orders (
                        id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                        customer_id     UUID            NOT NULL REFERENCES customers(id),
                        status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
                        total_amount    NUMERIC(10,2)   NOT NULL DEFAULT 0.00,
                        notes           TEXT,
                        created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                        updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Tabela de itens do pedido
CREATE TABLE order_items (
                             id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                             order_id    UUID            NOT NULL REFERENCES orders(id),
                             product_name    VARCHAR(150)    NOT NULL,
                             quantity    INTEGER         NOT NULL CHECK (quantity > 0),
                             unit_price  NUMERIC(10,2)   NOT NULL CHECK (unit_price >= 0),
                             created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Índices para performance
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);