-- Features that can be toggled per-role
CREATE TABLE IF NOT EXISTS features (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        feature_key VARCHAR(128) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    category VARCHAR(32) NOT NULL,        -- SIDENAV | BUTTON | FIELD | DROPDOWN
    group_name VARCHAR(64) NULL,          -- e.g., Sales, Invoice, Items
    description VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- Per-role overrides: visible/hidden
CREATE TABLE IF NOT EXISTS role_feature_visibility (
                                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                       role_name VARCHAR(32) NOT NULL,       -- ADMIN | SUPERUSER | STAFF
    feature_key VARCHAR(128) NOT NULL,
    visible BOOLEAN NOT NULL,             -- true => show; false => hide
    updated_by VARCHAR(64) NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_role_feature UNIQUE (role_name, feature_key),
    CONSTRAINT fk_rfv_feature FOREIGN KEY (feature_key) REFERENCES features(feature_key)
    );

-- Seed a small set to start (add more later)
INSERT INTO features (feature_key, label, category, group_name, description) VALUES
                                                                                 ('SIDENAV.DASHBOARD', 'Dashboard link', 'SIDENAV', 'Navigation', ''),
                                                                                 ('SIDENAV.REPORTS', 'Reports link', 'SIDENAV', 'Navigation', ''),
                                                                                 ('SIDENAV.PERMISSIONS', 'Permissions link', 'SIDENAV', 'Administration', 'Visible to Admin only (not hideable)'),
                                                                                 ('BTN.SALES.ADD', 'Add Sale button', 'BUTTON', 'Sales', ''),
                                                                                 ('BTN.SALES.DELETE', 'Delete Sale button', 'BUTTON', 'Sales', ''),
                                                                                 ('FIELD.INVOICE.COST_PRICE', 'Cost Price field', 'FIELD', 'Invoice', ''),
                                                                                 ('DROPDOWN.ITEM.ITEM_TYPE', 'Item Type dropdown', 'DROPDOWN', 'Items', '')
    ON DUPLICATE KEY UPDATE label=VALUES(label), category=VALUES(category), group_name=VALUES(group_name), description=VALUES(description);
