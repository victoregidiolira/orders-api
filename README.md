O README ficou duplicado — parece que colou duas vezes. Aqui está a versão final e limpa com o link do EC2:
markdown

# 🛒 Orders API

> API REST de gerenciamento de pedidos integrada com os principais serviços da AWS — construída como projeto de portfólio backend.

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)

---

## 📌 Sobre o projeto

API de e-commerce simplificado para gerenciamento de clientes e pedidos, com autenticação JWT, upload de arquivos, mensageria assíncrona e notificações por email — tudo integrado com serviços reais da AWS.

Desenvolvido do zero como projeto de portfólio para demonstrar habilidades em **Java backend** e **cloud AWS**.

---

## 🏗️ Arquitetura

Cliente
│
▼
Spring Boot (EC2)

├── Cognito ──────── Autenticação JWT

├── RDS PostgreSQL ── Persistência de dados

├── S3 ───────────── Upload de arquivos

├── SQS ──────────── Fila de eventos

│⠀ ⠀ ⠀ └── SNS ────── Notificações por email

└── CloudWatch ───── Logs e monitoramento


---

## 🚀 Stack

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4 |
| Banco de dados | PostgreSQL 16 (AWS RDS) |
| Migrations | Flyway |
| Autenticação | AWS Cognito + JWT |
| Storage | AWS S3 |
| Mensageria | AWS SQS + SNS |
| Servidor | AWS EC2 |
| Monitoramento | AWS CloudWatch |
| CI/CD | GitHub Actions |
| Documentação | Swagger / OpenAPI 3.1 |
| Build | Maven |

---

## ✨ Funcionalidades

### Clientes
- `POST /api/v1/customers` — cadastrar cliente
- `GET /api/v1/customers` — listar clientes
- `GET /api/v1/customers/{id}` — buscar cliente
- `PUT /api/v1/customers/{id}` — atualizar cliente
- `DELETE /api/v1/customers/{id}` — remover cliente

### Pedidos
- `POST /api/v1/orders` — criar pedido
- `GET /api/v1/orders` — listar pedidos
- `GET /api/v1/orders/{id}` — buscar pedido
- `GET /api/v1/orders/customer/{customerId}` — pedidos por cliente
- `PATCH /api/v1/orders/{id}/status` — atualizar status
- `DELETE /api/v1/orders/{id}` — remover pedido
- `POST /api/v1/orders/{id}/attachment` — upload de arquivo
- `GET /api/v1/orders/{id}/attachment/url` — URL assinada do arquivo

### Autenticação
- `POST /api/v1/auth/register` — registrar usuário
- `POST /api/v1/auth/login` — autenticar e obter JWT

---

## 🔐 Autenticação

Todos os endpoints (exceto `/auth/**`) exigem token JWT no header:
```http
Authorization: Bearer <access_token>
```

O token é obtido via `POST /api/v1/auth/login` e validado automaticamente pelo Spring Security usando a chave pública do AWS Cognito.

---

## 📖 Documentação

A API está disponível em produção no AWS EC2. Acesse a documentação interativa diretamente:

http://3.235.173.87:8080/swagger-ui.html


Para autenticar no Swagger:
1. Faça login via `POST /api/v1/auth/login` e copie o `accessToken`
2. Clique no botão **Authorize** 🔒 no topo da página
3. Cole o token no campo **bearerAuth** e clique em **Authorize**
4. Todos os endpoints estarão liberados para teste

---

## 📨 Fluxo de mensageria

Ao criar ou atualizar um pedido, a API publica um evento no SQS. Um consumer interno processa a mensagem e publica uma notificação no SNS, que entrega um email ao cliente automaticamente.

Pedido criado/atualizado
↓
SQS (orders-events)
↓
Consumer processa
↓
SNS publica notificação
↓
Email entregue ao cliente 📧


---

## ⚙️ Rodando localmente

### Pré-requisitos
- Java 21
- Maven
- PostgreSQL
- AWS CLI configurado

### Variáveis de ambiente

Configure as seguintes variáveis antes de rodar:
```bash
DB_HOST=seu-host-rds
DB_NAME=ordersdb
DB_USER=postgres
DB_PASSWORD=sua-senha
COGNITO_USER_POOL_ID=us-east-1_xxxxxxxxx
COGNITO_CLIENT_ID=seu-client-id
SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/...
SNS_TOPIC_ARN=arn:aws:sns:us-east-1:...
S3_BUCKET_NAME=seu-bucket
```

### Rodando
```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## 🔄 CI/CD

O pipeline de deploy é automático via **GitHub Actions**:

1. Push na branch `main`
2. GitHub Actions compila o projeto com Maven
3. JAR é enviado para o EC2 via SCP
4. Aplicação é reiniciada automaticamente via systemd

---

## 📊 Monitoramento

- **Logs** da aplicação enviados ao **CloudWatch Logs** (`orders-api-logs`)
- **Métricas** de CPU e memória coletadas pelo CloudWatch Agent
- **Alarme** configurado para alertar quando CPU ultrapassar 80%

---

## 🗂️ Estrutura do projeto

src/main/java/com/vito/orders_api/
├── controller/      # Endpoints REST
├── service/         # Regras de negócio
├── repository/      # Acesso ao banco
├── domain/          # Entidades JPA
├── dto/             # Objetos de transferência
├── config/          # Configurações (AWS, Security)
└── exception/       # Tratamento de erros global

src/main/resources/
├── application.properties
└── db/migration/    # Scripts Flyway


---

## 👨‍💻 Autor

Feito com 💚 por **Victor Egidio**

[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/victoregidiolira)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/victoregidiolira)
