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

## Materiais

`GET /api/materials` lista os materiais sem exigir autenticação.
`POST /api/materials` cadastra um material e exige um token de administrador
no cabeçalho `Authorization`; `PUT /api/materials/{id}` atualiza um cadastro
existente com as mesmas permissões. Ambos recebem `name`, `description`,
`quantity` e `unit`; `description` pode ser uma string vazia. Cadastros e
alterações são registrados na auditoria do relatório.

## Pedidos de empréstimo

Usuários autenticados podem criar pedidos com `POST /api/loans` e consultar os
próprios pedidos em `GET /api/loans/mine`. Administradores consultam todos os
pedidos com `GET /api/loans` e aprovam ou recusam um pedido pendente com
`PATCH /api/loans/{id}/status`, enviando `{"status":"APPROVED"}` ou
`{"status":"REJECTED","adminNotes":"..."}`. Um pedido inclui uma lista de
máquinas e/ou materiais, `pickupDate`, `returnDate` e `reason`. O backend
confere disponibilidade no momento da aprovação, desconta do estoque os
materiais aprovados e impede períodos de empréstimo aprovados que se sobreponham
para a mesma máquina.

## Pedidos de fabricação

Usuários autenticados podem enviar pedidos de corte a laser, impressão 3D ou
usinagem CNC, mesmo antes de anexar um arquivo. Podem incluir dimensões e
observações e, enquanto o pedido estiver aberto, editar os dados, anexar ou
substituir o arquivo e conversar com a equipe. Os arquivos são armazenados no
PostgreSQL junto ao pedido e aceitam até 100 MB. Pedidos de impressão 3D e CNC
precisam indicar uma máquina cadastrada na seção Máquinas e disponível para
uso. Administradores consultam os pedidos, baixam os arquivos e podem aprovar,
recusar, pedir informações adicionais ou reabrir pedidos encerrados. Recusas e
pedidos de atualização exigem uma explicação. O download é permitido somente
ao solicitante ou a um administrador.

Na inicialização, `schema.sql` atualiza as restrições de pedidos existentes
para aceitar os novos status e tipos e permitir solicitações sem anexo. Essa
migração não remove nem recria o volume PostgreSQL.

- `POST /api/orders` recebe `multipart/form-data` com `type` (`LASER_CUTTING`,
  `THREE_D_PRINTING` ou `CNC_MACHINING`), `file` (opcional), `notes` e
  `dimensions` (opcionais, até 2000 caracteres cada). Impressão 3D e CNC
  exigem `machineId`. Arquivos não têm restrição de extensão ou formato e
  podem ter até 100 MiB; a API retorna `413` se o limite for excedido.
- `GET /api/orders/mine` lista os pedidos do usuário autenticado.
- `GET /api/orders` lista todos os pedidos e exige perfil de administrador.
- `PUT /api/orders/{id}` permite ao solicitante atualizar os dados e,
  opcionalmente, anexar/substituir o arquivo enquanto o pedido estiver
  `PENDING` ou `WAITING_FOR_UPDATE`. Ao completar um pedido aguardando
  informações, o status volta para `PENDING`.
- `POST /api/orders/{id}/comments` registra uma mensagem na conversa do pedido.
  Solicitantes só podem comentar nos próprios pedidos abertos; administradores
  podem comentar em qualquer pedido.
- `GET /api/orders/{id}/file` baixa o anexo; somente o solicitante ou um
  administrador pode acessar o conteúdo. Pedidos sem arquivo retornam `400`.
- `PATCH /api/orders/{id}/status` exige perfil de administrador e recebe
  `{"status":"PENDING|WAITING_FOR_UPDATE|APPROVED|REJECTED","adminNotes":"..."}`.
  Administradores podem reabrir um pedido alterando o status para `PENDING`.
  `adminNotes` é obrigatória para `REJECTED` e `WAITING_FOR_UPDATE`, aceita até
  500 caracteres e também é salva na conversa como mensagem da equipe.

As respostas de criação, listagem e revisão usam o mesmo formato do frontend:
`id`, `requesterName`, `requesterEmail`, `type`, `machineId`, `machineName`,
`fileName`, `fileSize`, `notes`, `dimensions`, `status`, `adminNotes`,
`comments`, `createdAt` e `updatedAt`. Pedidos de corte a laser não recebem
`machineId`.

## Relatório de atividades

Administradores podem consultar `GET /api/reports/activity`, opcionalmente
informando `from=AAAA-MM-DD` e `to=AAAA-MM-DD`. As datas são inclusivas. O
relatório reúne cadastros e pedidos existentes, além dos acessos e alterações
registrados pela auditoria. Datas de criação de registros existentes podem ser
consultadas, mas alterações ocorridas antes da implantação da auditoria não
podem ser recuperadas.

Comandos úteis, executados no diretório `backend`:

```sh
docker compose logs -f app       # acompanhar os logs da API
docker compose logs -f postgres  # acompanhar os logs do banco
docker compose down              # parar os containers, preservando os dados
```

## Frontend

O frontend tem um Compose próprio. Inicie a API e o banco primeiro, depois siga
as instruções em [`../frontend/README.md`](../frontend/README.md).
