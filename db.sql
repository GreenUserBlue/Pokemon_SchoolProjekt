DROP DATABASE IF EXISTS pokemonDB;
create DATABASE IF NOT EXISTS pokemonDB CHARACTER SET utf8 collate utf8_general_ci;
use pokemonDB;
create Table User
(
    PK_User_ID int auto_increment,
    name       varchar(255) UNICODE unique,
    password   varchar(255),
    email      varchar(255) UNICODE unique,
    PRIMARY KEY (PK_User_ID),
    CHECK (name rlike '.{4,}'),
    CHECK ( name rlike '^[a-zA-Z0-9][a-zA-Z0-9_]*$'),
    CHECK ( password rlike '.{8,}'),
    CHECK ( email rlike '^[A-Za-z0-9.]+@[A-Za-z0-9]+\\.[A-Za-z0-9.]+$')
--     , check ((select User.name, COUNT(*)
--              from User
--              where User.name = name
--                 OR User.email = name))
);

create TABLE Player
(
    PK_Player_ID int AUTO_INCREMENT,
    posX         int,
    posY         int,
    skinID       int,
    FK_User_ID   int,
    language     varchar(3),
    PRIMARY KEY (PK_Player_ID),
    foreign key (FK_User_ID) REFERENCES User (PK_User_ID),
    check ( skinID >= 0 )
);

CREATE TABLE World
(
    PK_World_ID int,
    seed        int,
    PRIMARY KEY (PK_World_ID)
);
--  CREATE TABLE City
--  (
--      PK_City_ID INT AUTO_INCREMENT,
--      posX       int,
--      posY       int,
--      FK_World_ID int,
--      PRIMARY KEY (PK_City_ID),
--      FOREIGN KEY (FK_World_ID) REFERENCES World(PK_World_ID)
--  );

CREATE TABLE House
(
    PK_House_ID    int AUTO_INCREMENT,
    houseIDInWorld int,
    FK_World_ID    int,
    FK_Owner_ID    int,
    PRIMARY KEY (PK_House_ID),
    FOREIGN KEY (FK_World_ID) REFERENCES World (PK_World_ID),
    FOREIGN KEY (FK_Owner_ID) REFERENCES Player (PK_Player_ID)
);
insert into User (name, password, email)
values ('abcde', 'hallo123', 'a@g.co'),
       ('abcded', 'hallo123', 'a@g.com');

insert into Player (posX, posY, FK_User_ID, language, skinID)
values (10, 10, 3, 'eng', 0),
       (12, 10, 4, 'eng', 0);

select *
from World
         inner join House H on World.PK_World_ID = H.FK_World_ID
         inner join Player p on H.FK_Owner_ID = p.PK_Player_ID
         inner join User U on U.PK_User_ID = p.FK_User_ID
where PK_World_ID = 1 && H.PK_House_ID = 2;

insert into world (seed) VALUE (1234);


select *
from User
         inner join Player P on User.PK_User_ID = FK_User_ID;

-- first select
select *
from User;

-- second select
select *
from Player;

-- delete from User where name='abcde' OR email='a@g.co';

select *
from world
         inner join House H on World.PK_World_ID = H.FK_World_ID;

insert into house (houseIDInWorld, FK_World_ID, FK_Owner_ID) VALUE (3, 1, (select PK_Player_ID
                                                                           from Player
                                                                                    inner join User U on Player.FK_User_ID = U.PK_User_ID
                                                                           where U.name = 'Name'));
(select PK_Player_ID
 from Player
          inner join User U on Player.FK_User_ID = U.PK_User_ID
 where U.name = 'Name2');
delete
from house;

select *
from World
         inner join House H on World.PK_World_ID = H.FK_World_ID
where PK_World_ID = 1;

update House
set FK_Owner_ID=(Select PK_Player_ID
                 From User
                          inner join Player P on User.PK_User_ID = P.FK_User_ID
                 where name = 'Name2')
where FK_World_ID = 1 && houseIDInWorld = 2;

(select name
 from User
          inner join Player on Player.FK_User_ID = PK_User_ID
 where PK_Player_ID = 8);


update world
set seed=124
where PK_World_ID = 1;
-- second select
select *
from User;

update Player
set posX=posX + 2;
update Player
set posY=posY - 4;