-- NEXT VALUE FOR CUSTOMER_SEQ
-- CURRENT VALUE FOR CUSTOMER_SEQ

INSERT INTO CUSTOMER (ID, CUSTOMER_NO, NAME, EMAIL)
VALUES (1, 'C-10001', 'Max Mustermann', 'max.mustermann@example.com'),
       (2, 'C-10002', 'Erika Musterfrau', 'erika.musterfrau@example.com'),
       (3, 'C-10003', 'Hans Meier', 'h.meier@example.com');


INSERT INTO ORDERS (ID, CUSTOMER_ID, ORDER_NO, ORDER_DATE, STATUS)
VALUES
-- Bestellungen von Max Mustermann
(1, 1, 'O-90001', DATE '2025-01-10', 'NEW'),
(2, 1, 'O-90002', DATE '2025-01-15', 'SHIPPED'),

-- Bestellung von Erika Musterfrau
(3, 2, 'O-90003', DATE '2025-01-20', 'NEW'),

-- Bestellung von Hans Meier
(4, 3, 'O-90004', DATE '2025-01-22', 'CANCELLED')
;

INSERT
INTO ORDER_ITEM (ID, ORDER_ID, POSITION_NO, PRODUCT_CODE, QUANTITY, UNIT_PRICE)
VALUES
-- Order O-90001
(1, 1, 1, 'P-100', 2, 19.99),
(2, 1, 2, 'P-200', 1, 49.90),

-- Order O-90002
(3, 2, 1, 'P-300', 3, 9.99),
(4, 2, 2, 'P-400', 1, 199.00),

-- Order O-90003
(5, 3, 1, 'P-100', 5, 19.99),

-- Order O-90004
(6, 4, 1, 'P-500', 1, 299.00),
(7, 4, 2, 'P-600', 2, 14.50);
