CREATE TABLE IF NOT EXISTS domain (
	internalId BIGINT AUTO_INCREMENT NOT NULL, 
	id VARCHAR(36) NOT NULL, 
	updatedDate TIMESTAMP NOT NULL, 
	name VARCHAR(36) NOT NULL,
	PRIMARY KEY (id), 	
	UNIQUE (name), 
	UNIQUE (internalId)
);

CREATE TABLE IF NOT EXISTS service (
    internalId BIGINT AUTO_INCREMENT NOT NULL, 
    id VARCHAR(36) NOT NULL, 
    updatedDate TIMESTAMP NOT NULL, 
    name VARCHAR(36) NOT NULL, 
    domain_id VARCHAR(36) NULL,
    detail_id VARCHAR(36) NULL, 
    PRIMARY KEY (id), 
    UNIQUE (internalId), 
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS service_detail (
    internalId BIGINT AUTO_INCREMENT NOT NULL, 
    id VARCHAR(36) NOT NULL, 
    updatedDate TIMESTAMP NOT NULL, 
    name VARCHAR(100) NOT NULL, 
    PRIMARY KEY (id), 
    UNIQUE (internalId)
);

CREATE TABLE IF NOT EXISTS user (
    internalId BIGINT AUTO_INCREMENT NOT NULL, 
    id VARCHAR(36) NOT NULL, 
    updatedDate TIMESTAMP NOT NULL, 
    name VARCHAR(36) NOT NULL, 
    PRIMARY KEY (id), 
    UNIQUE (internalId), 
    UNIQUE (name)
);


CREATE TABLE IF NOT EXISTS service_user (
    internalId BIGINT AUTO_INCREMENT NOT NULL, 
    updatedDate TIMESTAMP NOT NULL DEFAULT NOW(), 
    service_id VARCHAR(36) NOT NULL, 
    user_id VARCHAR(36) NOT NULL, 
    PRIMARY KEY (internalId, service_id, user_id), 
    UNIQUE (internalId)
);

ALTER TABLE service ADD FOREIGN KEY (domain_id) REFERENCES domain (id);
ALTER TABLE service_detail ADD FOREIGN KEY (id) REFERENCES service (id);
ALTER TABLE service_user ADD FOREIGN KEY (service_id) REFERENCES service (id);
ALTER TABLE service_user ADD FOREIGN KEY (user_id) REFERENCES user (id);





