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

Usuários autenticados podem enviar pedidos de corte a laser ou impressão 3D
com qualquer tipo de arquivo (até 100 MB), incluir observações e acompanhar
suas solicitações. Os arquivos são armazenados no PostgreSQL junto ao pedido.
Pedidos de impressão 3D precisam indicar uma impressora cadastrada na seção
Máquinas e disponível para uso. Administradores consultam os pedidos, baixam
os arquivos enviados e aprovam ou recusam cada solicitação. O download é
permitido somente ao solicitante ou a um administrador.

- `POST /api/orders` recebe `multipart/form-data` com `type` (`LASER_CUTTING`
  ou `THREE_D_PRINTING`), `file`, `notes` (opcional, até 2000 caracteres) e,
  para impressão 3D, `machineId`. Arquivos não têm restrição de extensão ou
  formato e podem ter até 100 MiB. A API retorna `400` se o arquivo estiver
  ausente e `413` se o limite de tamanho for excedido.
- `GET /api/orders/mine` lista os pedidos do usuário autenticado.
- `GET /api/orders` lista todos os pedidos e exige perfil de administrador.
- `GET /api/orders/{id}/file` baixa o anexo; somente o solicitante ou um
  administrador pode acessar o conteúdo.
- `PATCH /api/orders/{id}/status` exige perfil de administrador e recebe
  `{"status":"APPROVED","adminNotes":"..."}` ou
  `{"status":"REJECTED","adminNotes":"..."}`. Apenas pedidos `PENDING` podem
  ser revisados; `adminNotes` é opcional e aceita até 500 caracteres.

As respostas de criação, listagem e revisão usam o mesmo formato do frontend:
`id`, `requesterName`, `requesterEmail`, `type`, `machineId`, `machineName`,
`fileName`, `fileSize`, `notes`, `status`, `adminNotes`, `createdAt` e
`updatedAt`. Pedidos de corte a laser não recebem `machineId`.

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
