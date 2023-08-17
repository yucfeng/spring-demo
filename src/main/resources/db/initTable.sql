DROP TABLE IF EXISTS "kv";
CREATE TABLE "kv" (
                      "key" text(36) NOT NULL,
                      "value" TEXT,
                      "lastModifiedTime" INTEGER,
                      PRIMARY KEY ("key")
);