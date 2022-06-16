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

    skinID       int        default (0),
    startPokID   int,
    FK_User_ID   int,
    money        int        default (0),
    language     varchar(3) default ('eng'),
    PRIMARY KEY (PK_Player_ID),
    foreign key (FK_User_ID) REFERENCES User (PK_User_ID),
    check ( skinID >= 0 )
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
    Message      VARCHAR(50),
    FK_Player_ID int,
    PRIMARY KEY (PK_Poke_ID),
    FOREIGN KEY (FK_Player_ID) REFERENCES Player (PK_Player_ID)
);

CREATE TABLE World
(
    PK_World_ID int auto_increment,
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

CREATE TABLE MyPosition
(
    FK_PK_Player_ID int,
    FK_PK_World_ID  int,
    posX            int,
    posY            int,
    PRIMARY KEY (FK_PK_Player_ID, FK_PK_World_ID),
    FOREIGN KEY (FK_PK_Player_ID) REFERENCES Player (PK_Player_ID),
    FOREIGN KEY (FK_PK_World_ID) REFERENCES World (PK_World_ID)
);


CREATE TABLE ItemToPlayer
(
    PK_ItemToPlayer_ID int auto_increment,
    Item_ID            int,
    FK_Player          int,
    quantity           int,
#     ItemTypName        varchar(255),
    PRIMARY KEY (PK_ItemToPlayer_ID),
    FOREIGN KEY (FK_Player) REFERENCES Player (PK_Player_ID),
#     check ( ItemTypName in ('Ball', 'Potion', 'WaterItem')),
    check ( quantity >= 0 )
);

insert into User (name, password, email)
values ('Name', '$2a$06$fUbqoClTr0U0.CWyp5PdPekyWHpXhPdr53.d.S7pkRgwmyyRCo9My', 'a@g.co'),
       ('Name1', '$2a$06$G3g9wHJXL24IK1fpssrgtufueu2z5fojxBd0bHgkBlF8daukHQAPS', 'f@t.x'),
       ('Name2', '$2a$06$G3g9wHJXL24IK1fpssrgtufueu2z5fojxBd0bHgkBlF8daukHQAPS', 'f@t.xd'),
       ('Name3', '$2a$06$zWhy4d6V4vyOIFtqPR5CleF7FMTC4m7TcMfIFa.ie7Xoxp8Id7nZa', 'a@g.com');


insert into Player (FK_User_ID, language, skinID, startPokID, money)
values (1, 'eng', 0, 0, 17000),
       (1, 'eng', 0, 2, 2000),
       (3, 'eng', 0, 2, 10);

insert into Badge (badgeTypeID, FK_Player_ID)
VALUES (1, 2),
       (3, 1),
       (2, 2);

insert into player (skinID, startPokID, FK_User_ID, language, money)
    VALUE (
           0, 2, (select PK_User_ID from User where name = 'Name'), 'eng', 200
    );

insert into world(seed, FK_User_ID) value (69420, 1);

insert into MyPosition(FK_PK_Player_ID, FK_PK_World_ID, posX, posY)
VALUES (1, 1, 10, 15),
       (2, 1, 10, 10),
       (3, 1, 10, 15);

insert into ItemToPlayer (Item_ID, FK_Player, quantity)
VALUES (1, 1, 10),
       (2, 1, 16),
       (5, 1, 12),
       (2, 2, 230),
       (4, 2, 134),
       (5, 3, 15),
       (9, 3, 198),
       (6, 4, 156);

select user.name, Item_ID, quantity
from user
         inner join Player P on User.PK_User_ID = P.FK_User_ID
         inner join ItemToPlayer ITP on P.PK_Player_ID = ITP.FK_Player
where PK_Player_ID = 1;

select *
from User
         inner join Player P on User.PK_User_ID = P.FK_User_ID
where P.startPokID = 1 && User.name = 'Name2';

INSERT INTO Pokemon (Message, FK_Player_ID)
VALUES ('10;3;81,40§;0;1,2,7,15,15,8;15;7', 1),
       ('1;5;13,10§14,20§;142;5,15,11,11,15,3;5;19', 3);

select *
from MyPosition;

select *
from Pokemon;
insert into Pokemon (Message, FK_Player_ID)
VALUES ('10;6;81,40§20,6§153,4§;200;1,2,7,15,15,8;15;15', 1);

insert into ItemToPlayer (Item_ID, FK_Player, quantity)
VALUES (1, 1, 10),
       (2, 1, 16),
       (5, 1, 12),
       (2, 2, 230),
       (4, 2, 134),
       (5, 3, 15),
       (9, 3, 198),
       (6, 4, 156);

select * from Player;

select * from ItemToPlayer;

# update Player set money =100 where PK_Player_ID = 2;
# select count(PK_Badge_ID) as nbr from Badge inner join Player P on Badge.FK_Player_ID = P.PK_Player_ID where P.PK_Player_ID = 4;

# select count(*) as nbr from User inner join Player P on User.PK_User_ID = P.FK_User_ID where P.startPokID = 1 && User.name = 'Name';

# update Player set name='Set Username' where (select count(*)from User inner join Player P on User.PK_User_ID = Player.FK_User_ID where Player.startPokID = 1 && User.name = 'Name') > 0;

# select *from World inner join User U on World.FK_User_ID = U.PK_User_ID;

# delete from World;


# select PK_User_ID from User where name = 'Name';

# insert into world (seed, FK_User_ID) VALUE (1234, (select PK_User_ID from User where name = 'Name17'));select *from world;

# select *from World where PK_World_ID = 2;

# select *from World where FK_User_ID = (select PK_User_ID from User where name = 'Name')

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