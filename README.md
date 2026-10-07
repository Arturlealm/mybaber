# MyBarber

Sistema web de agendamento para barbearias.

## Tecnologias

- Java 25 e Spring Boot 4
- PostgreSQL 17
- Liquibase para versionamento do banco
- Spring Security com JWT
- Testcontainers para testes de integração
- Frontend em React 19 + TypeScript + Vite (pasta `frontend`)

## Estrutura

```
mybarber/
├── src/        API (Spring Boot)
└── frontend/   Aplicação web (React)
```

## Como executar

1. Copie o arquivo `.env.example` para `.env` e preencha as variáveis.
2. Suba o banco de dados:

   ```bash
   docker compose up -d
   ```

3. Inicie a API (porta 8080):

   ```bash
   ./mvnw spring-boot:run
   ```

4. Em outro terminal, inicie o frontend (porta 5173):

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. Acesse http://localhost:5173. Clientes criam a conta na tela de cadastro; o administrador entra em "Sou da equipe" com o email e a senha definidos em `ADMIN_EMAIL` e `ADMIN_SENHA`.

Na primeira execução, o Liquibase cria as tabelas e o sistema cadastra o administrador inicial com os dados das variáveis `ADMIN_*`.

## Telas

| Perfil | Telas |
|---|---|
| Cliente | Agendar (serviço, barbeiro, dia e horário) e Meus agendamentos |
| Barbeiro | Agenda do dia (concluir atendimento confirmando o valor ou informando desconto) |
| Administrador | Calendário (fechar/abrir dias para todos ou por barbeiro e horário padrão), Agenda do dia, Serviços, Funcionários e Relatórios |

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

Com a aplicação rodando, a documentação interativa fica em http://localhost:8080/swagger-ui.html.
Faça login em **Autenticação**, copie o `tokenAcesso` e clique em **Authorize**.

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
| GET | `/api/clientes?busca=&page=&size=` (nome ou telefone) | Barbeiro e administrador |
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

### Filiais

| Método | Rota | Acesso |
|---|---|---|
| GET | `/api/filiais` | Autenticado |
| POST, PUT, DELETE | `/api/filiais` | Administrador |

### Serviços

| Método | Rota | Acesso |
|---|---|---|
| GET | `/api/servicos` | Público |
| GET | `/api/servicos/combinacoes` | Público (opções de um clique: Cabelo, Barba, Cabelo e barba) |
| POST, PUT, DELETE | `/api/servicos` e `/api/servicos/combinacoes` | Administrador |

### Agenda

| Método | Rota | Acesso |
|---|---|---|
| GET | `/api/agenda/calendario?inicio=&fim=&filialId=&funcionarioId=` | Barbeiro e administrador |
| GET | `/api/agenda/jornadas/funcionarios/{id}` | Administrador ou o próprio barbeiro |
| PUT | `/api/agenda/jornadas/funcionarios/{id}` | Administrador |
| GET | `/api/agenda/ajustes?inicio=&fim=` | Barbeiro e administrador |
| POST | `/api/agenda/ajustes` | Administrador (fechar ou abrir um dia para todos ou para um barbeiro) |
| DELETE | `/api/agenda/ajustes/{id}` | Administrador |

### Agendamentos

| Método | Rota | Acesso |
|---|---|---|
| GET | `/api/agendamentos/horarios-disponiveis?funcionarioId=&data=&servicoIds=` | Autenticado |
| POST | `/api/agendamentos` | Autenticado (funcionário informa o `clienteId`) |
| GET | `/api/agendamentos/me` | Cliente (histórico) |
| GET | `/api/agendamentos/agenda-do-dia?data=` | Barbeiro (própria agenda) e administrador |
| GET | `/api/agendamentos/{id}` | Envolvidos e administrador |
| PATCH | `/api/agendamentos/{id}/cancelamento` | Cliente (até 2h antes) e funcionários |
| PATCH | `/api/agendamentos/{id}/conclusao` | Barbeiro e administrador |
| PATCH | `/api/agendamentos/{id}/nao-comparecimento` | Barbeiro e administrador |

## Regras de agenda

- Horários oferecidos de 30 em 30 minutos; o agendamento ocupa a duração total dos serviços.
  Exemplo: cabelo e barba às 08:30 ocupa até 09:30, que passa a ser o próximo horário livre.
- Prioridade do expediente: ajuste do barbeiro no dia, depois ajuste geral da filial (ex.: feriado), depois a jornada semanal.
- O banco impede dois agendamentos sobrepostos para o mesmo barbeiro.
- Na conclusão, o barbeiro confirma o valor de tabela ou informa o valor realmente cobrado (desconto).
- A equipe pode agendar pelo balcão para clientes cadastrados, buscando por nome ou telefone.
- Barbeiros sem horário próprio seguem o horário padrão da barbearia; ambos aceitam pausa para almoço.

## Segurança

- Após 5 senhas erradas para o mesmo email, o login fica bloqueado por 15 minutos (`mybarber.autenticacao.*`).
  O controle é feito em memória: com mais de uma instância da API, cada uma conta as tentativas separadamente.

## Padrões do projeto

- Tabelas do banco sempre no plural.
- Pacotes organizados por funcionalidade (`cliente`, `funcionario`, `autenticacao`, `compartilhado`).
- Classes com nomes específicos do domínio, sem nomes genéricos.
- Comentários somente quando essenciais, no formato `/* */`.
- Registros não são excluídos, e sim inativados, para preservar o histórico.
- Alterações no banco somente por novos changesets do Liquibase.
