CREATE TABLE "accounts" (
                            "id" VARCHAR(255) PRIMARY KEY,
                            "user_id" VARCHAR(255) NOT NULL,
                            "username" VARCHAR(255),
                            "password" VARCHAR(255),
                            "role_id" VARCHAR(255) NOT NULL,
                            "is_deleted" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE "roles" (
                         "id" VARCHAR(255) PRIMARY KEY,
                         "name" VARCHAR(255),
                         "is_deleted" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE "users" (
                         "id" VARCHAR(255) PRIMARY KEY,
                         "fullname" VARCHAR(255),
                         "birthdate" DATE,
                         "gender" VARCHAR(10),
                         "phonenumber" VARCHAR(255),
                         "email" VARCHAR(255),
                         "address" VARCHAR(255),
                         "image" VARCHAR(255),
                         "is_deleted" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE "refresh_tokens" (
                                  "id" VARCHAR(255) PRIMARY KEY,
                                  "user_id" VARCHAR(255) NOT NULL,
                                  "refresh_token" VARCHAR(255) NOT NULL,
                                  "is_deleted" BOOLEAN NOT NULL DEFAULT FALSE
);


ALTER TABLE "accounts" ADD FOREIGN KEY ("role_id") REFERENCES "roles" ("id");

ALTER TABLE "accounts" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "refresh_tokens" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");