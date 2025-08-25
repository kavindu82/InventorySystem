
INSERT INTO features (feature_key, label, category, group_name, description) VALUES
                                                                                 ('BTN.SALES.EDIT', 'Edit Sale button', 'BUTTON', 'Sales', ''),
                                                                                 ('BTN.SALES.DELETE', 'Delete Sale button', 'BUTTON', 'Sales', ''),
                                                                                 ('BTN.SALES.VIEW', 'View Sale button', 'BUTTON', 'Sales', ''),
                                                                                 ('BTN.INVOICE_ITEM.EDIT', 'Edit Invoice Item button', 'BUTTON', 'Invoice Item', ''),
                                                                                 ('BTN.INVOICE_ITEM.DELETE', 'Delete Invoice Item button', 'BUTTON', 'Invoice Item', ''),
                                                                                 ('BTN.ITEM.EDIT', 'Edit Item button', 'BUTTON', 'Item', ''),
                                                                                 ('BTN.ITEM.DELETE', 'Item Item button', 'BUTTON', 'Item', ''),
                                                                                 ('BTN.SUPPLIER.EDIT', 'Edit Supplier button', 'BUTTON', 'Supplier', ''),
                                                                                 ('BTN.SUPPLIER.DELETE', 'Delete Supplier button', 'BUTTON', 'Supplier', ''),
                                                                                 ('BTN.QUOTATION.VIEW', 'view Quotation button', 'BUTTON', 'Quotation', ''),
                                                                                 ('BTN.QUOTATION.DELETE', 'delete Quotation button', 'BUTTON', 'Quotation', ''),
                                                                                 ('BTN.QUOTATION.EDIT', 'Edit Quotation button', 'BUTTON', 'Quotation', ''),
                                                                                 ('BTN.QUOTATION.CONVERT', 'Convert Quotation button', 'BUTTON', 'Quotation', '')
    ON DUPLICATE KEY UPDATE label=VALUES(label), category=VALUES(category), group_name=VALUES(group_name), description=VALUES(description);
