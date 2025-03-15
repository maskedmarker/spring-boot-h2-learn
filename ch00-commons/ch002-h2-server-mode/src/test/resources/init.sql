create table t_user (
  id INTEGER NOT NULL AUTO_INCREMENT,
  name VARCHAR(16),
  email VARCHAR(128),
  user_status VARCHAR(1),
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

insert into t_user (name, email, user_status) values ('001', '001@qq.com', '0');
insert into t_user (name, email, user_status) values ('002', '002@qq.com', '1');
insert into t_user (name, email, user_status) values ('003', '003@qq.com', '1');
insert into t_user (name, email, user_status) values ('004', '004@qq.com', '1');
insert into t_user (name, email, user_status) values ('005', '005@qq.com', '2');