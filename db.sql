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
    name         varchar(25) unique,
    posX         int,
    posY         int,
    skinID       int,
    startPokID   int,
    FK_User_ID   int,
    language     varchar(3),
    PRIMARY KEY (PK_Player_ID),
    foreign key (FK_User_ID) REFERENCES User (PK_User_ID),
    check ( skinID >= 0 ),
    CHECK ( name rlike '^[a-zA-Z0-9][a-zA-Z0-9_]*$')
);

create Table Badge
(
    PK_Badge_ID  int auto_increment,
    badgeTypeID  int,
    FK_Player_ID int,
    PRIMARY KEY (PK_Badge_ID),
    FOREIGN KEY (FK_Player_ID) REFERENCES Player (PK_Player_ID)
);

create table Pokemon
(
    PK_Poke_ID   int auto_increment,
    name         varchar(25),
    pokeID       int,
    nature       int,
    FK_Player_ID int,
    PRIMARY KEY (PK_Poke_ID),
    FOREIGN KEY (FK_Player_ID) REFERENCES Player (PK_Player_ID),
    check ( Pokemon.pokeID >= 0 )
);

CREATE TABLE World
(
    PK_World_ID int,
    seed        int,
    FK_User_ID  int,
    PRIMARY KEY (PK_World_ID),
    FOREIGN KEY (FK_User_ID) REFERENCES User (PK_User_ID)
);

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
values ('Name', '$2a$06$fUbqoClTr0U0.CWyp5PdPekyWHpXhPdr53.d.S7pkRgwmyyRCo9My', 'a@g.co'),
       ('Nam3', '$2a$06$G3g9wHJXL24IK1fpssrgtufueu2z5fojxBd0bHgkBlF8daukHQAPS', 'f@t.x'),
       ('Name2', '$2a$06$zWhy4d6V4vyOIFtqPR5CleF7FMTC4m7TcMfIFa.ie7Xoxp8Id7nZa', 'a@g.com');

insert into Player (name, posX, posY, FK_User_ID, language, skinID, startPokID)
values ('Fra', 10, 10, 1, 'eng', 0, 0),
       ('Fra2', 12, 11, 1, 'eng', 0, 2),
       ('Fra3', 12, 10, 2, 'eng', 0, 2);

insert into Badge (badgeTypeID, FK_Player_ID)
VALUES (1, 2),
       (3, 1),
       (2, 2);

insert into Pokemon (name, pokeID, nature, FK_Player_ID)
VALUES ('Franz', 1, 3, 1),
       ('Franz2', 2, 3, 1),
       ('Franz2', 2, 3, 2),
       ('Franz3', 2, 3, 1);

select count(PK_Badge_ID) as nbr
from Badge
         inner join Player P on Badge.FK_Player_ID = P.PK_Player_ID
where P.PK_Player_ID = 4;

# select PK_Player_ID,Player.name,skinID,language from Player INNER JOIN User U on Player.FK_User_ID = U.PK_User_ID where U.name='Name' OR email='Name';
# select *
# from World
#          inner join House H on World.PK_World_ID = H.FK_World_ID
#          inner join Player p on H.FK_Owner_ID = p.PK_Player_ID
#          inner join User U on U.PK_User_ID = p.FK_User_ID
# where PK_World_ID = 1 && H.PK_House_ID = 2;

# insert into world (seed) VALUE (1234);


# select *
# from User
#          inner join Player P on User.PK_User_ID = FK_User_ID;

-- first select
# select *
# from User;
#
-- second select
# select *
# from Player;
#
-- delete from User where name='abcde' OR email='a@g.co';

# select *
# from world
#          inner join House H on World.PK_World_ID = H.FK_World_ID;

# insert into house (houseIDInWorld, FK_World_ID, FK_Owner_ID) VALUE (3, 1, (select PK_Player_ID
#                                                                            from Player
#                                                                                     inner join User U on Player.FK_User_ID = U.PK_User_ID
#                                                                            where U.name = 'Name'));
# (select PK_Player_ID
#  from Player
#           inner join User U on Player.FK_User_ID = U.PK_User_ID
#  where U.name = 'Name2');
# delete
# from house;

# select *
# from World
#          inner join House H on World.PK_World_ID = H.FK_World_ID
# where PK_World_ID = 1;

# update House
# set FK_Owner_ID=(Select PK_Player_ID
#                  From User
#                           inner join Player P on User.PK_User_ID = P.FK_User_ID
#                  where name = 'Name2')
# where FK_World_ID = 1 && houseIDInWorld = 2;

# (select name
#  from User
#           inner join Player on Player.FK_User_ID = PK_User_ID
#  where PK_Player_ID = 8);


# update world
# set seed=124
# where PK_World_ID = 1;
-- second select
# select *
# from User;

# update Player
# set posX=posX + 2;
# update Player
# set posY=posY - 4;