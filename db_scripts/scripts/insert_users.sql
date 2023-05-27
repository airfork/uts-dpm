-- password is - testAccount
INSERT INTO users (managerid, username, password, firstname, lastname, fulltime, changed, points, added)
VALUES (1, 'test@account.com', '$2a$12$djE8/mmxWnOlPCOUYyyMy.1ROf6JIExV31p6At84COkCosbwP2s4W', 'Test',
        'Account', false, true, 0, now());

INSERT INTO users (managerid, username, password, firstname, lastname, fulltime, changed, points, added)
VALUES (1, 'jane@doe.com', '$2a$12$0n2HQ0OP67NOotsWCY5t4usnh7wX8chxfrpGafNErjp01y0T0GweG', 'Jane',
        'Doe', false, true, 0, now());

INSERT INTO users (managerid, username, password, firstname, lastname, fulltime, changed, points, added)
VALUES (1, 'john@doe.com', '$2a$12$rEnM6gHwGc3buQylcZOvYuNDa.0l4Xyy61AQuM8Pel4ulD5.hXyda', 'John',
        'Doe', true, true, 0, now());

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1)