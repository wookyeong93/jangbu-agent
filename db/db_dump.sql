INSERT INTO tb_group_code (group_code, group_name, sort_order, use_yn)
VALUES ('TRX_TYPE', '거래 구분', 1, 'Y');

INSERT INTO tb_code (group_code, code, code_name, sort_order, use_yn) VALUES
('TRX_TYPE', 'PURCHASE', '매입', 1, 'Y'),
('TRX_TYPE', 'SALE',     '매출', 2, 'Y'),
('TRX_TYPE', 'EXPENSE',  '지출', 3, 'Y');