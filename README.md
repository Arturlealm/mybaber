# MyBarber

Sistema web de agendamento para barbearias.

## Tecnologias

- Java 25 e Spring Boot 4
- PostgreSQL 17
- Liquibase para versionamento do banco
- Spring Security com JWT
- Testcontainers para testes de integração

## Como executar

1. Copie o arquivo `.env.example` para `.env` e preencha as variáveis.
2. Suba o banco de dados:

   ```bash
   docker compose up -d
   ```

3. Inicie a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

Na primeira execução, o Liquibase cria as tabelas e o sistema cadastra o administrador inicial com os dados das variáveis `ADMIN_*`.

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DB_URL` | URL JDBC do PostgreSQL |
| `DB_USUARIO` | Usuário do banco |
| `DB_SENHA` | Senha do banco |
| `JWT_SEGREDO` | Segredo de assinatura dos tokens (mínimo 32 caracteres) |
| `JWT_EXPIRACAO` | Tempo de validade do token, por exemplo `8h` |
| `CORS_ORIGENS` | Origens do frontend permitidas, separadas por vírgula |
| `ADMIN_NOME`, `ADMIN_EMAIL`, `ADMIN_TELEFONE`, `ADMIN_SENHA` | Dados do administrador inicial |

## Testes

```bash
./mvnw test
```

Os testes de integração usam Testcontainers e só rodam quando o Docker está ativo.

## API

### Autenticação

| Método | Rota | Acesso |
|---|---|---|
| POST | `/api/autenticacao/clientes/login` | Público |
| POST | `/api/autenticacao/funcionarios/login` | Público |

O token retornado deve ser enviado no cabeçalho `Authorization: Bearer <token>`.

### Clientes

| Método | Rota | Acesso |
|---|---|---|
| POST | `/api/clientes` | Público (cadastro) |
| GET | `/api/clientes/me` | Cliente |
| PUT | `/api/clientes/me` | Cliente |
| PUT | `/api/clientes/me/senha` | Cliente |
| GET | `/api/clientes?nome=&page=&size=` | Barbeiro e administrador |
| GET | `/api/clientes/{id}` | Barbeiro e administrador |
| PUT | `/api/clientes/{id}` | Administrador |
| DELETE | `/api/clientes/{id}` | Administrador (inativa) |

### Funcionários

| Método | Rota | Acesso |
|---|---|---|
| GET | `/api/funcionarios/barbeiros` | Autenticado |
| GET | `/api/funcionarios/me` | Barbeiro e administrador |
| PUT | `/api/funcionarios/me/senha` | Barbeiro e administrador |
| GET | `/api/funcionarios` | Administrador |
| GET | `/api/funcionarios/{id}` | Administrador |
| POST | `/api/funcionarios` | Administrador |
| PUT | `/api/funcionarios/{id}` | Administrador |
| DELETE | `/api/funcionarios/{id}` | Administrador (inativa) |

## Padrões do projeto

- Tabelas do banco sempre no plural.
- Pacotes organizados por funcionalidade (`cliente`, `funcionario`, `autenticacao`, `compartilhado`).
- Classes com nomes específicos do domínio, sem nomes genéricos.
- Comentários somente quando essenciais, no formato `/* */`.
- Registros não são excluídos, e sim inativados, para preservar o histórico.
- Alterações no banco somente por novos changesets do Liquibase.
