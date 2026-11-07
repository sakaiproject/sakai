-- Bootstrap databases and the sakai application user (MariaDB 10+ / MySQL 8+).
-- Runs only on first start when the data volume is empty.

CREATE DATABASE IF NOT EXISTS sakai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS sakai12 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS sakai19 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS sakai20 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'sakai'@'%' IDENTIFIED BY 'ironchef';
CREATE USER IF NOT EXISTS 'sakai'@'localhost' IDENTIFIED BY 'ironchef';
CREATE USER IF NOT EXISTS 'sakai'@'127.0.0.1' IDENTIFIED BY 'ironchef';

GRANT ALL PRIVILEGES ON `sakai%`.* TO 'sakai'@'%';
GRANT ALL PRIVILEGES ON `sakai%`.* TO 'sakai'@'localhost';
GRANT ALL PRIVILEGES ON `sakai%`.* TO 'sakai'@'127.0.0.1';

FLUSH PRIVILEGES;
