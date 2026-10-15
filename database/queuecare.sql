-- Run this script in MySQL Workbench or the MySQL command line.
CREATE DATABASE IF NOT EXISTS queuecare
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE queuecare;
-- Spring Boot / Hibernate creates and updates the doctors and patients tables on startup.
-- Three starter doctors are inserted automatically when the backend first starts.
