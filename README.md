# Backend

API REST em Spring Boot com PostgreSQL.

## Iniciar API e banco

Na primeira execução, clone o projeto e rode:

```sh
cd backend
docker compose up --build -d
```

A API ficará disponível em <http://localhost:8081>. O PostgreSQL usa o volume
Docker `postgres_data`, que preserva os dados entre reinicializações e não é
versionado no Git.

Sem um arquivo `.env`, o Compose usa o banco `sitemaker`, o usuário
`sitemaker` e a senha de desenvolvimento `sitemaker_dev_password`. Esses
padrões são apenas para desenvolvimento local.

Na inicialização, o backend cria o esquema, carrega sete máquinas e seis
agendamentos fictícios de `src/main/resources/data.sql` e cria as contas
mock abaixo, caso ainda não existam:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| Admin | `admin@sitemaker.local` | `admin12345678` |
| Usuário | `usuario@sitemaker.local` | `usuario123` |

Esses são valores de desenvolvimento. Para alterá-los, defina `ADMIN_EMAIL`,
`ADMIN_PASSWORD`, `DEMO_USER_EMAIL` e/ou `DEMO_USER_PASSWORD` no arquivo
`backend/.env`. Os registros de demonstração só são inseridos quando seus
códigos ou e-mails ainda não existem; reiniciar os containers não sobrescreve
nem duplica os dados.

Comandos úteis, executados no diretório `backend`:

```sh
docker compose logs -f app       # acompanhar os logs da API
docker compose logs -f postgres  # acompanhar os logs do banco
docker compose down              # parar os containers, preservando os dados
```

## Frontend

O frontend tem um Compose próprio. Inicie a API e o banco primeiro, depois siga
as instruções em [`../frontend/README.md`](../frontend/README.md).
