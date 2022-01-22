-- TEMP
insert into crime_authorization.users(id, name, email, image_url, email_verified, "password", provider) values(1, 'Gabor Fekete', 'gabor.fekete85@gmail.com', '', true, '$2a$10$m22VRnBmvRTf4cHvXaHWM.aOpp.og6NetWpYwVw7dNAfr5ynODsLa', 'local'); -- abc234

-- FINAL
insert into security.users(user_id, email, first_name, last_name, password) values('987152d7-e391-4853-b1bd-ca3c1aeea3d9', 'gabor.fekete85@gmail.com', 'Gabor', 'Fekete', '$2a$10$m22VRnBmvRTf4cHvXaHWM.aOpp.og6NetWpYwVw7dNAfr5ynODsLa'); -- abc234

insert into security.roles(id, name) values( 1, 'ROLE_ADMIN');
insert into security.roles(id, name) values( 2, 'ROLE_MODERATOR');
insert into security.roles(id, name) values( 3, 'ROLE_USER');

insert into security.user_roles(user_id, role_id) values('987152d7-e391-4853-b1bd-ca3c1aeea3d9', 1);
insert into security.user_roles(user_id, role_id) values('987152d7-e391-4853-b1bd-ca3c1aeea3d9', 2);
insert into security.user_roles(user_id, role_id) values('987152d7-e391-4853-b1bd-ca3c1aeea3d9', 3);