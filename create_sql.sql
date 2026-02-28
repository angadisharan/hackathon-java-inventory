-- Setup script for local PostgreSQL for hackathon-java-assignment
-- Run these commands as a superuser (e.g. postgres).

-- 1) Create application user
CREATE ROLE quarkus_test WITH LOGIN PASSWORD 'quarkus_test';

-- 2) Create application database owned by that user
CREATE DATABASE quarkus_test OWNER quarkus_test;

-- 3) (Optional) Explicitly grant privileges on the database
GRANT ALL PRIVILEGES ON DATABASE quarkus_test TO quarkus_test;

