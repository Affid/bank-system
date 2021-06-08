CREATE TABLE IF NOT EXISTS Passport(
    id char(10) PRIMARY KEY NOT NULL,
    last_name varchar(20) NOT NULL,
	first_name varchar(20) NOT NULL,
	second_name varchar(20) NOT NULL,
	issue_date DATE NOT NULL,
	birth_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS Client (
	id INT8 AUTO_INCREMENT NOT NULL,
    passport CHAR(10) UNIQUE,
    phone char(11) UNIQUE,
    FOREIGN KEY (passport) REFERENCES Passport(id),
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS Currency (
    id char(3) PRIMARY KEY NOT NULL,
    name varchar(30) NOT NULL UNIQUE,
    abbreviation varchar(3) NOT NULL UNIQUE
);



CREATE TABLE IF NOT EXISTS Account (
	account_b char(5) NOT NULL,
	currency char(3) NOT NULL,
	key integer NOT NULL,
	division char(4) NOT NULL,
	personal_acc char(7) NOT NULL,
	owner int8 NOT NULL,
	id int8 NOT NULL AUTO_INCREMENT,
	balance numeric(16, 5) NOT NULL,
	FOREIGN KEY (currency) REFERENCES Currency(id),
	FOREIGN KEY (owner) REFERENCES Client(id),
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Card_state(
    id int NOT NULL PRIMARY KEY,
    description varchar(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Card (
	pay_system integer NOT NULL,
	bank_id char(5) NOT NULL,
	id char(9) PRIMARY KEY NOT NULL,
	control char(1) NOT NULL,
	cvv char(3) NOT NULL,
	account_id int NOT NULL,
	validity DATE NOT NULL,
	balance DECIMAL NOT NULL,
	state int NOT NULL,
	FOREIGN KEY (state) REFERENCES Card_state(id),
	FOREIGN KEY (account_id) REFERENCES Account(id)
);

CREATE TABLE IF NOT EXISTS Def_Account (
	account_id int8 NOT NULL,
	client_id int8 NOT NULL UNIQUE,
	FOREIGN KEY (account_id) REFERENCES Account(id),
	FOREIGN KEY (client_id) REFERENCES Client(id),
	PRIMARY KEY (account_id, client_id)
);

CREATE TABLE IF NOT EXISTS Contragent(
    id1 int8 NOT NULL REFERENCES Client(id),
    id2 int8 NOT NULL REFERENCES Client(id),
    PRIMARY KEY (id1, id2),
    CONSTRAINT Chk_IDs CHECK (id1 != id2)
);