CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    external_key VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS templates (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    cover_url VARCHAR(500) NOT NULL,
    description VARCHAR(500) NOT NULL,
    required_image_count INT NOT NULL,
    accepted_mime_types VARCHAR(500) NOT NULL,
    max_file_size_bytes BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_templates_image_count CHECK (required_image_count > 0),
    CONSTRAINT chk_templates_file_size CHECK (max_file_size_bytes > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    upload_status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_assets_user_created (user_id, created_at),
    CONSTRAINT fk_assets_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_assets_file_size CHECK (file_size_bytes > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tasks (
    id CHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    template_id VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL,
    current_stage VARCHAR(40) NULL,
    failure_stage VARCHAR(40) NULL,
    failure_reason VARCHAR(1000) NULL,
    result_url VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    finished_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_tasks_user_created (user_id, created_at),
    KEY idx_tasks_user_status (user_id, status),
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_tasks_template FOREIGN KEY (template_id) REFERENCES templates (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS task_assets (
    task_id CHAR(36) NOT NULL,
    asset_id BIGINT NOT NULL,
    asset_order INT NOT NULL,
    PRIMARY KEY (task_id, asset_id),
    CONSTRAINT fk_task_assets_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_task_assets_asset FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT chk_task_assets_order CHECK (asset_order > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS task_steps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id CHAR(36) NOT NULL,
    step_order INT NOT NULL,
    step_name VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP NULL,
    finished_at TIMESTAMP NULL,
    error_message VARCHAR(1000) NULL,
    UNIQUE KEY uk_task_steps_order (task_id, step_order),
    KEY idx_task_steps_task_status (task_id, status),
    CONSTRAINT fk_task_steps_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT chk_task_steps_order CHECK (step_order > 0)
) ENGINE=InnoDB;

