-- Create databases used by services
CREATE DATABASE "account";
CREATE DATABASE "schedule-service";
CREATE DATABASE "booking-service";
CREATE DATABASE "clinic";
CREATE DATABASE "expertise";
CREATE DATABASE "notification";
CREATE DATABASE "package";
CREATE DATABASE "payment";

-- Grant privileges to postgres user
GRANT ALL PRIVILEGES ON DATABASE "account" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "schedule-service" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "booking-service" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "clinic" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "expertise" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "notification" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "package" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "payment" TO postgres;