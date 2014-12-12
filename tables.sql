/* TABELA USERS*/

create table users
( id_user NUMBER(10),
name VARCHAR2(30) CONSTRAINT nn_name NOT NULL,
username VARCHAR2(30) CONSTRAINT nn_username NOT NULL
CONSTRAINT uni_username UNIQUE,
pass VARCHAR2(30) CONSTRAINT nn_pass NOT NULL, 
job VARCHAR2(30),
CONSTRAINT pk_users PRIMARY KEY(id_user)
);


/*TABELA MEETINGS */


create table meeting
(id_meeting NUMBER(10),
title VARCHAR2(50) CONSTRAINT nn_title NOT NULL,
desiredOutcome VARCHAR2(50) CONSTRAINT nn_outcome  NOT NULL,
dat VARCHAR2(30),
leader VARCHAR2(20),
location VARCHAR2(50) CONSTRAINT nn_location NOT NULL,
closed NUMBER(1) CONSTRAINT check_closed CHECK (closed in (-1,0,1)),
CONSTRAINT pk_meeting PRIMARY KEY(id_meeting),
CONSTRAINT fk_name FOREIGN KEY (leader) REFERENCES users(username)
);


/*TABELA USER_MEETING*/

create table user_meeting
(id_user NUMBER(10),
id_meeting NUMBER(10),
going NUMBER(1) CONSTRAINT check_going CHECK (going in (0,1)),
CONSTRAINT pk_meeting_user PRIMARY KEY(id_user,id_meeting),
CONSTRAINT fk_user FOREIGN KEY (id_user)  REFERENCES users(id_user),
CONSTRAINT fk_meeting FOREIGN KEY (id_meeting) REFERENCES meeting(id_meeting)
);


/*TABELA AGENDAITEM*/

create table agenda_item
(id_agenda NUMBER(20),
id_meeting NUMBER(10),
title VARCHAR2(50) CONSTRAINT nn_agenda_title NOT NULL,
CONSTRAINT pk_agenda PRIMARY KEY(id_agenda),
CONSTRAINT fk_agenda_meeting FOREIGN KEY (id_meeting) REFERENCES meeting(id_meeting)
);

/* TABELA KEY DECISION */

create table key_decision
(id_key NUMBER(20),
id_agenda NUMBER(20),
decision VARCHAR2(50) CONSTRAINT nn_decision NOT NULL,
CONSTRAINT pk_key PRIMARY KEY(id_key),
CONSTRAINT fk_agenda_key FOREIGN KEY (id_agenda) REFERENCES agenda_item(id_agenda) ON DELETE CASCADE
); 

/*TABELA ACTION ITEMS*/

create table action_item
(id_action NUMBER(20),
id_agenda NUMBER(20),
id_user NUMBER(10),
action VARCHAR2(50) CONSTRAINT nn_action NOT NULL,
done NUMBER(1)  CONSTRAINT check_done CHECK (done in (0,1)),
unseen NUMBER(1)  CONSTRAINT check_unseen CHECK (unseen in (0,1)),
CONSTRAINT pk_action PRIMARY KEY(id_action),
CONSTRAINT fk_agenda_action FOREIGN KEY (id_agenda) REFERENCES agenda_item(id_agenda) ON DELETE CASCADE,
CONSTRAINT fk_user_action FOREIGN KEY (id_user) REFERENCES users(id_user)
);

/*TABELA MESSAGE */

create table message
(id_message NUMBER(20),
id_agenda NUMBER(20),
message VARCHAR2(300) CONSTRAINT nn_msg NOT NULL,
CONSTRAINT pk_message PRIMARY KEY(id_message),
CONSTRAINT fk_agenda_message FOREIGN KEY (id_agenda) REFERENCES agenda_item(id_agenda) ON DELETE CASCADE
);

/*TABELA UNSEENMESSAGE*/

create table unseen_message
(id_message NUMBER(20),
id_user NUMBER(10),
CONSTRAINT pk_unseen_message PRIMARY KEY(id_message,id_user),
CONSTRAINT fk_unseen_message FOREIGN KEY (id_message) REFERENCES message(id_message) ON DELETE CASCADE,
CONSTRAINT fk_unseen_user FOREIGN KEY (id_user) REFERENCES users(id_user)
);