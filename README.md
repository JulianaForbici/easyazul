# 💙 EasyAzul

EasyAzul é uma aplicação web desenvolvida para facilitar o gerenciamento de vagas da Área Azul, com controle de usuários, veículos, zonas de estacionamento, tickets e pagamentos.

O projeto conta com backend em Java com Spring Boot e frontend em Angular, oferecendo autenticação segura com JWT, controle de acesso por perfil e integração com mapa para visualização das zonas de estacionamento.

# 🎥 Demonstração do MVP

Confira abaixo uma demonstração do EasyAzul em funcionamento, apresentando as principais telas e funcionalidades implementadas no projeto.

https://github.com/user-attachments/assets/b2a7f083-7915-46f6-88bd-683b21e2a59e

## 🚀 Principais funcionalidades

* Cadastro e autenticação de usuários
* Login com geração de token JWT
* Controle de acesso por perfil de usuário
* Gestão de motoristas, fiscais e administradores
* Cadastro e consulta de veículos
* Visualização de zonas de estacionamento no mapa
* Controle de vagas e disponibilidade por zona
* Criação, reserva e acompanhamento de tickets
* Controle de status dos tickets
* Consulta e gerenciamento de pagamentos
* Área administrativa para gerenciamento do sistema
* Integração com mapas utilizando OpenStreetMap

## 🛠️ Tecnologias utilizadas

### Backend

* Java 17
* Spring Boot
* Spring Security
* JWT
* JPA/Hibernate
* PostgreSQL
* Flyway
* Maven
* API REST

### Frontend

* Angular
* TypeScript
* HTML
* CSS

### Ferramentas

* Git
* GitHub
* IntelliJ IDEA
* Postman/Insomnia

## 📦 Estrutura geral do projeto

```bash
easyazul/
├── backend/
│   └── src/main/java/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── infra/security/
│       ├── repository/
│       ├── service/
│       └── ...
│
└── frontend/
    └── src/
        ├── app/
        ├── assets/
        └── ...
```

> A estrutura pode variar conforme a organização das pastas do projeto.

## ⚡ Como executar o projeto

### Pré-requisitos

Antes de iniciar, é necessário ter instalado:

* Java 17+
* Node.js
* Angular CLI
* PostgreSQL
* Maven

## 🔙 Backend

Acesse a pasta do backend:

```bash
cd backend
```

Execute a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

A API será iniciada em:

```bash
http://localhost:8081
```

## 🔜 Frontend

Acesse a pasta do frontend:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Execute o projeto:

```bash
ng serve
```

A aplicação será iniciada em:

```bash
http://localhost:4200
```

## 🔐 Autenticação

O sistema utiliza autenticação JWT.

Após realizar login com e-mail e senha, a API retorna um token que deve ser enviado nas próximas requisições para acessar rotas protegidas.

## 🗺️ Mapas

O EasyAzul utiliza integração com mapas para exibir zonas de estacionamento e facilitar a visualização das áreas disponíveis.

A proposta é permitir que o usuário consulte zonas, visualize informações de disponibilidade e selecione a melhor opção para estacionar.

## 🧩 Perfis de usuário

O sistema possui controle de acesso para diferentes perfis:

* **Motorista:** pode cadastrar veículos, visualizar zonas, criar tickets e consultar pagamentos.
* **Fiscal:** pode consultar veículos e tickets para apoiar a fiscalização.
* **Administrador:** pode gerenciar usuários, veículos, tickets e pagamentos.

## 📌 Status do projeto

Projeto em desenvolvimento.

## 👩‍💻 Autora

Juliana Cristina Forbici


## 📄 Licença

Este projeto está disponível apenas para fins de estudo e portfólio.
