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

## Testes

Os testes unitários do backend usam JUnit 5 e Mockito. Execute-os e valide a
cobertura mínima de 80% das linhas com:

```powershell
.\mvnw.cmd verify
```

O JaCoCo gera o relatório detalhado em `target/site/jacoco/index.html`; a
verificação do Maven falha se a cobertura total de linhas ficar abaixo de 80%.

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

## Empréstimos e materiais

Usuários autenticados podem consultar materiais e máquinas, enviar empréstimos
e ver os próprios pedidos. Administradores podem cadastrar/atualizar materiais,
consultar todos os pedidos e aprová-los ou recusá-los. A aprovação desconta a
quantidade de materiais do estoque; pedidos recusados ou pendentes não alteram
o estoque. Máquinas não podem ter pedidos aprovados com datas conflitantes.

As tabelas de materiais e empréstimos são criadas automaticamente pelo
Hibernate ao iniciar a aplicação.

## Relatório de atividades

Administradores podem consultar `/api/reports/activity`, opcionalmente
informando `from` e `to` no formato `YYYY-MM-DD`. O relatório agrega cadastros,
alterações detectáveis pelos registros atuais, agendamentos e pedidos de
empréstimo. A data final informada é inclusiva.

## Frontend

O frontend tem um Compose próprio. Inicie a API e o banco primeiro, depois siga
as instruções em [`../frontend/README.md`](../frontend/README.md).
