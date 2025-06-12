--tabela criada para guardar os dados de autenticação dos usuarios
/*bsbs*/
CREATE TABLE USERS(
id TEXT PRIMARY KEY UNIQUE NOT NULL,
login TEXT NOT NULL UNIQUE,
senha TEXT NOT NULL,
role TEXT NOT NULL

);