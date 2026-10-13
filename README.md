# backend

## Run with Docker Compose

From this directory, start the Spring Boot API and PostgreSQL:

```sh
docker compose up --build -d
```

The API is available at <http://localhost:8081>. PostgreSQL data is stored in
the `postgres_data` volume and is preserved when the services are stopped.

To start the frontend, use its separate Compose file:

```sh
cd ../frontend
docker compose up --build -d
```

The frontend is available at <http://localhost:3000> and uses
`http://localhost:8081/api` by default. Set `NEXT_PUBLIC_API_BASE_URL` in
`frontend/.env` to override the API URL when needed. Follow frontend logs with
`docker compose logs -f frontend` from the frontend directory.