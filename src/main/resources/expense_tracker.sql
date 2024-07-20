CREATE TABLE IF NOT EXISTS "user" (
	"id" uuid PRIMARY KEY,
	"username" varchar(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "account" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid NOT NULL,
	"is_active" boolean NOT NULL DEFAULT TRUE,
	"name" varchar(255) NOT NULL,
	"parent_id" uuid NULL,
	"type" enum (
		'CASH',
		'DEBT',
		'BANK',
		'CRYPTO'
	) NOT NULL,
	CONSTRAINT fk_account_user_id_user FOREIGN KEY ("user_id") REFERENCES "user"("id"),
	CONSTRAINT fk_account_parent_id_account FOREIGN KEY ("parent_id") REFERENCES "account"("id")
);

CREATE TABLE IF NOT EXISTS "category" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid NOT NULL,
	"is_active" bool NOT NULL DEFAULT TRUE,
	"parent_id" uuid NULL,
	"name" varchar(255) NOT NULL,
	CONSTRAINT fk_category_parent_id_category FOREIGN KEY ("parent_id") REFERENCES "category"("id"),
	CONSTRAINT fk_category_user_id_user FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

CREATE TABLE IF NOT EXISTS "currency" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid NOT NULL,
	"is_active" bool NOT NULL DEFAULT TRUE,
	"name" varchar(255) NOT NULL,
	"code" varchar(4) NULL,
	"symbol" varchar(1) NULL,
	"order" tinyint NULL,
	CONSTRAINT fk_currency_user_id_user FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

CREATE TABLE IF NOT EXISTS "profile" (
	"id" uuid PRIMARY KEY,
	"base_currency_id" uuid NULL,
	"default_currency_id" uuid NULL,
	"default_account_id" uuid NULL,
	CONSTRAINT fk_profile_base_currency_id_currency FOREIGN KEY ("base_currency_id") REFERENCES "currency"("id"),
	CONSTRAINT fk_profile_default_account_id_account FOREIGN KEY ("default_account_id") REFERENCES "account"("id"),
	CONSTRAINT fk_profile_default_currency_id_currency FOREIGN KEY ("default_currency_id") REFERENCES "currency"("id"),
	CONSTRAINT fk_profile_id_user FOREIGN KEY ("id") REFERENCES "user"("id")
);

CREATE TABLE IF NOT EXISTS "record" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid NOT NULL,
	"date" date NOT NULL,
	"type" ENUM(
		'EXPENSE',
		'INCOME',
		'TRANSFER',
		'EXCHANGE'
	) NOT NULL,
	"comment" varchar(255) NULL,
	"group_id" uuid NULL,
	"created_at" timestamp DEFAULT CURRENT_TIMESTAMP ,
	CONSTRAINT fk_record_user_id_user FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

CREATE TABLE IF NOT EXISTS "tag" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid NOT NULL,
	"is_active" bool NOT NULL DEFAULT TRUE,
	"name" varchar(255) NOT NULL,
	CONSTRAINT fk_tag_user_id_user FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

CREATE TABLE IF NOT EXISTS "entry" (
	"id" INTEGER AUTO_INCREMENT PRIMARY KEY,
	"record_id" uuid NOT NULL,
	"account_id" uuid NOT NULL,
	"amount" NUMERIC(
		19,
		2
	) NOT NULL DEFAULT '0'::NUMERIC,
	"currency_id" uuid NOT NULL,
	"category_id" uuid NULL,
	"comment" varchar(255) NULL,
	CONSTRAINT fk_entry_account_id_account FOREIGN KEY ("account_id") REFERENCES "account"("id"),
	CONSTRAINT fk_entry_category_id_category FOREIGN KEY ("category_id") REFERENCES "category"("id"),
	CONSTRAINT fk_entry_currency_id_currency FOREIGN KEY ("currency_id") REFERENCES "currency"("id"),
	CONSTRAINT fk_entry_record_id_record FOREIGN KEY ("record_id") REFERENCES "record"("id") ON
	DELETE
		CASCADE
);

CREATE TABLE IF NOT EXISTS "entry_tag" (
	"entry_id" INTEGER NOT NULL,
	"tag_id" uuid NOT NULL,
	CONSTRAINT pk_entry_tag PRIMARY KEY (
		"entry_id",
		"tag_id"
	),
	CONSTRAINT fk_entry_tag_entry_id_entry FOREIGN KEY ("entry_id") REFERENCES "entry"("id") ON
	DELETE
		CASCADE,
		CONSTRAINT fk_entry_tag_tag_id_tag FOREIGN KEY ("tag_id") REFERENCES "tag"("id")
);
