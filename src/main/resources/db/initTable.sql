DROP TABLE IF EXISTS "kv";
CREATE TABLE "kv" (
                      "key" text(36) NOT NULL,
                      "value" TEXT,
                      "inChange" INT2,
                      "lastModifiedTime" INTEGER,
                      PRIMARY KEY ("key")
);

insert into kv(key, value, inChange, lastModifiedTime) values ('k', 'v1', 0, 0);