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