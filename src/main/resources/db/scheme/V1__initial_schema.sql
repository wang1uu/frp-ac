-- host记录表
CREATE TABLE "HOST" (
                        "ip" CHAR(40) NOT NULL,
                        "port" INTEGER DEFAULT NULL,
                        "flag" CHAR(1) NOT NULL,
                        "expiry" TIMESTAMP DEFAULT NULL,
                        "last_access_time" TIMESTAMP DEFAULT NULL,
                        "note" CHAR(128) DEFAULT NULL,
                        "created_by" CHAR(32) DEFAULT NULL,
                        "created_time" TIMESTAMP DEFAULT NULL,
                        "updated_by" CHAR(32) DEFAULT NULL,
                        "updated_time" TIMESTAMP DEFAULT NULL,
                        PRIMARY KEY ("ip") ON CONFLICT ROLLBACK
);
CREATE INDEX "idx_ip_flag_expiry"
    ON "HOST" (
               "ip" ASC,
               "flag" ASC,
               "expiry" DESC
        );

-- 用户表
CREATE TABLE "USER" (
                                 "user_id" CHAR(16) NOT NULL,
                                 "password" CHAR(128) NOT NULL,
                                 "access_token" CHAR(128),
                                 "access_token_expiry" TIMESTAMP DEFAULT NULL,
                                 PRIMARY KEY ("user_id") ON CONFLICT ROLLBACK
);
CREATE UNIQUE INDEX "idx_user_id"
    ON "USER" (
                 "user_id"
        );
CREATE UNIQUE INDEX "idx_access_token"
    ON "USER" (
                 "access_token"
        );
INSERT INTO "USER" ("user_id", "password", "access_token", "access_token_expiry") VALUES ('paidax', 'd8b42a27a7819049b9e79efa2266ebbc', '3314f729a7eeb0608f2971bc6c2d46a2', '-1');