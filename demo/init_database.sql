-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS appmemory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE appmemory_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255),
    token_expire DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建考试记录表
CREATE TABLE IF NOT EXISTS exam_record (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    score INT NOT NULL,
    exam_time DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_exam_time (exam_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建验证码表
CREATE TABLE IF NOT EXISTS verification_code (
    id INT AUTO_INCREMENT PRIMARY KEY,
    target VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    type TINYINT NOT NULL,
    expiry_time DATETIME NOT NULL,
    used TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target_type (target, type),
    INDEX idx_expiry_time (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试数据
INSERT IGNORE INTO user (nickname, phone, email, password) VALUES 
('测试用户1', '13812345678', 'test1@example.com', '$2a$10$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWX'),
('测试用户2', '13987654321', 'test2@example.com', '$2a$10$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWX');

-- 显示创建结果
SELECT '数据库初始化完成' as message;
SELECT COUNT(*) as user_count FROM user;
SELECT COUNT(*) as exam_record_count FROM exam_record;
SELECT COUNT(*) as verification_code_count FROM verification_code;