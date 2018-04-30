create table user_account (
  id            bigint                  not null,
  login         varchar(32) unique      not null,
  password      varchar(32)             not null,
  name          varchar(64)             not null,
  email         varchar(32)             not null,
  register_date timestamp default now() not null,

  primary key (id)
);

create sequence sq_user_account
  start with 1000;

create table user_role (
  user_id bigint      not null,
  role    varchar(32) not null,

  primary key (user_id, role),
  foreign key (user_id) references user_account (id)
)
