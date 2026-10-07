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
| Barbeiro | Meus agendamentos (semana ou mês) e Agenda do dia (concluir atendimento confirmando o valor ou informando desconto) |
| Administrador | Calendário (fechar/abrir dias para todos ou por barbeiro e horário padrão), Agendamentos (semana ou mês), Agenda do dia, Serviços, Funcionários e Relatórios |

## Publicar no Render (demonstração gratuita)

O arquivo `render.yaml` cria o site e o banco de uma vez. A API e as telas ficam no mesmo endereço.

1. Crie uma conta em https://render.com (pode entrar com o GitHub).
2. No painel, clique em **New → Blueprint** e escolha o repositório `mybaber`, branch `main`.
3. Preencha os campos pedidos: `ADMIN_EMAIL`, `ADMIN_TELEFONE` (só números, com DDD) e `ADMIN_SENHA` (mínimo 8 caracteres).
4. Clique em **Deploy Blueprint**. O primeiro deploy leva alguns minutos.
5. Abra o endereço `https://mybarber-xxxx.onrender.com` mostrado no serviço e entre em "Sou da equipe" com o administrador.

Plano grátis:

- O site dorme após 15 minutos sem acessos e acorda sozinho ao abrir o link (leva 1 a 2 minutos).
  Antes de apresentar, abra o link e espere a tela de login aparecer.
- O banco grátis expira em 30 dias.
- Emails e lembretes ficam desligados.

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DB_URL` | URL JDBC do PostgreSQL (ou `DB_HOST`, `DB_PORTA` e `DB_NOME` separados) |
| `PORT` | Porta HTTP da aplicação (padrão 8080) |
| `DB_USUARIO` | Usuário do banco |
| `DB_SENHA` | Senha do banco |
| `JWT_SEGREDO` | Segredo de assinatura dos tokens (mínimo 32 caracteres) |
| `JWT_EXPIRACAO` | Tempo de validade do token, por exemplo `8h` |
| `CORS_ORIGENS` | Origens do frontend permitidas, separadas por vírgula |
| `ADMIN_NOME`, `ADMIN_EMAIL`, `ADMIN_TELEFONE`, `ADMIN_SENHA` | Dados do administrador inicial |
| `EMAIL_HABILITADO`, `EMAIL_REMETENTE`, `EMAIL_NOME_REMETENTE` | Envio de emails (desligado por padrão) |
| `SMTP_HOST`, `SMTP_PORTA`, `SMTP_USUARIO`, `SMTP_SENHA` | Servidor SMTP (Gmail por padrão) |
| `FRONTEND_URL` | Endereço do frontend usado nos links enviados por email |
| `LEMBRETE_HABILITADO`, `LEMBRETE_ANTECEDENCIA`, `LEMBRETE_LIMITE_DIARIO` | Lembretes de agendamento por email |

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
| POST | `/api/autenticacao/redefinicao-senha/solicitacao` | Público (envia o link por email) |
| POST | `/api/autenticacao/redefinicao-senha` | Público (cria a nova senha com o token do link) |

O token retornado deve ser enviado no cabeçalho `Authorization: Bearer <token>`.

### Clientes

| Método | Rota | Acesso |
|---|---|---|
| POST | `/api/clientes` | Público (cadastro) |
| POST | `/api/clientes/balcao` | Administrador (cadastro rápido com senha padrão) |
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
| GET | `/api/agendamentos/periodo?inicio=&fim=` | Barbeiro (própria agenda) e administrador, até 62 dias |
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
- Registro retroativo pelo balcão (somente horários vagos e dentro do expediente):
  - barbeiro: horários já passados apenas do dia atual e só na própria agenda;
  - administrador: datas passadas até 90 dias (`mybarber.agenda.dias-maximos-retroativos-administrador`).
  Ao escolher um horário passado, o atendimento pode ser salvo direto como concluído, com o valor cobrado.
- O administrador cadastra clientes novos no balcão com nome, telefone e email (CPF opcional).
  Email obrigatório. O cliente recebe a senha padrão `123456789` (`SENHA_PADRAO_CLIENTE_BALCAO`), informada ao
  administrador na tela para repassar. No login com a senha padrão, o cliente vê o aviso "Deseja trocar?" e pode criar
  a senha na hora (nova senha e confirmação). Se recusar, o aviso volta no próximo login e a senha padrão continua valendo.
- Barbeiros sem horário próprio seguem o horário padrão da barbearia; ambos aceitam pausa para almoço.

## Envio de emails (redefinição de senha)

Com `EMAIL_HABILITADO=false` (padrão), nenhum email é enviado: o conteúdo, incluindo o link de redefinição, aparece no log da API.

Para enviar pelo Gmail:

1. Ative a verificação em duas etapas na conta Google que vai enviar os emails.
2. Crie uma **senha de app** em https://myaccount.google.com/apppasswords.
3. Preencha no `.env`:

   ```
   EMAIL_HABILITADO=true
   EMAIL_REMETENTE=sua-conta@gmail.com
   SMTP_USUARIO=sua-conta@gmail.com
   SMTP_SENHA=senha-de-app-gerada
   FRONTEND_URL=http://localhost:5173
   ```

O Gmail permite cerca de 500 envios por dia. O link de redefinição vale por 30 minutos e só pode ser usado uma vez.

### Lembretes de agendamento

- Enviados 24h antes do horário (`LEMBRETE_ANTECEDENCIA`), verificando a cada 10 minutos.
- Agendamentos feitos com menos de 3h de antecedência não recebem lembrete.
- Limite diário de 300 lembretes (`LEMBRETE_LIMITE_DIARIO`), deixando margem para os emails de redefinição de senha dentro da cota do Gmail. O que passar do limite é enviado quando houver saldo, se ainda estiver dentro da janela.
- Para desligar: `LEMBRETE_HABILITADO=false`.

Para trocar o Gmail por outro serviço (Brevo, Amazon SES, Resend), basta alterar as variáveis `SMTP_*`: todos oferecem SMTP.

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
