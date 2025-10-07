# eds-middleware

Middleware HTTP que faz proxy/integração com o **eds-server**.

## Endpoints

- `GET /middleware/health` — Healthcheck do middleware.
- `POST /middleware/users` — Cria um usuário no *eds-server*. Body:
  ```json
  { "login": "alice", "password": "secret" }
  ```
- `GET /middleware/users` — Lista usuários a partir do *eds-server*.

## Configuração

- Porta: `8080`
- Variável de base: `eds.server.base-url` (default: `http://localhost:8081`)

## Como rodar (dev)

```bash
# 1) Rodar o servidor de downstream primeiro (ver README do eds-server)
# 2) Subir o middleware:
./mvnw spring-boot:run
```

## Teste manual

```bash
# Criar usuário via middleware
curl -X POST http://localhost:8080/middleware/users \
     -H "Content-Type: application/json" \
     -d '{ "login": "alice", "password": "123" }'

# Listar usuários (via middleware -> server)
curl http://localhost:8080/middleware/users
```

