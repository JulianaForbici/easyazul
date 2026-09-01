# 💙 EasyAzul

EasyAzul é uma aplicação web desenvolvida para facilitar o gerenciamento de vagas da Área Azul, com controle de usuários, veículos, zonas de estacionamento, tickets e pagamentos.

O projeto conta com backend em Java com Spring Boot, frontend em Angular e banco de dados PostgreSQL, oferecendo autenticação segura com JWT, controle de acesso por perfil e integração com mapa para visualização das zonas de estacionamento.

A aplicação também possui suporte a Docker, permitindo executar frontend, backend e banco de dados de forma integrada com um único comando.

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
* Flyway
* Maven
* API REST

### Frontend

* Angular
* TypeScript
* HTML
* SCSS

### Banco de dados

* PostgreSQL 16

### Infraestrutura e ferramentas

* Docker
* Docker Compose
* Git
* GitHub
* IntelliJ IDEA
* DBeaver
* Postman/Insomnia

## 📦 Estrutura geral do projeto

```text
easyazul/
├── docker-compose.yml
│
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
└── frontend/
    ├── Dockerfile
    ├── package.json
    ├── angular.json
    └── src/
        ├── app/
        ├── assets/
        └── environments/
```

## 🐳 Como executar o projeto com Docker

### Pré-requisitos

Para executar o EasyAzul utilizando Docker, é necessário ter instalado:

* [Docker Desktop](https://docs.docker.com/desktop/setup/install/windows-install/)
* [Git](https://git-scm.com/downloads)

> Não é necessário instalar Java, Maven, Node.js, Angular CLI ou PostgreSQL separadamente para executar o projeto via Docker.

Ferramentas como IntelliJ IDEA e DBeaver são opcionais e podem ser utilizadas para desenvolvimento e acesso ao banco de dados.

### 1. Clonar o projeto

```bash
git clone https://github.com/JulianaForbici/easyazul.git
```

Acesse a pasta do projeto:

```bash
cd easyazul
```

### 2. Iniciar o Docker Desktop

Antes de executar o projeto, certifique-se de que o Docker Desktop está aberto e que o Docker Engine foi iniciado.

### 3. Subir a aplicação

Na raiz do projeto, execute:

```bash
docker compose up --build
```

Na primeira execução, o processo pode levar alguns minutos, pois as imagens e dependências serão baixadas e configuradas.

O Docker Compose iniciará três serviços:

* **easyazul-frontend** — aplicação Angular
* **easyazul-backend** — API Spring Boot
* **easyazul-db** — banco PostgreSQL

### 4. Acessar a aplicação

Após a inicialização dos containers:

* **Frontend:** http://localhost:4200
* **Backend:** http://localhost:8081
* **Swagger:** http://localhost:8081/swagger-ui/index.html

Nas próximas execuções, caso não existam alterações que exijam uma nova build, basta executar:

```bash
docker compose up
```

## 🗄️ Banco de dados

O PostgreSQL é criado e configurado automaticamente pelo Docker Compose.

As configurações para conexão externa, por exemplo utilizando o DBeaver, são:

```text
Host: localhost
Porta: 5433
Banco: easyazul
Usuário: postgres
Senha: postgres
```

Dentro da rede Docker, o backend acessa o banco através de:

```text
db:5432
```

Os dados do PostgreSQL são mantidos em um volume Docker, portanto não são apagados ao simplesmente parar os containers.

Para acessar diretamente o PostgreSQL pelo terminal:

```bash
docker exec -it easyazul-db psql -U postgres -d easyazul
```

## ⏹️ Parar a aplicação

Para interromper a execução no terminal, utilize:

```text
Ctrl + C
```

Para remover os containers criados pelo Compose:

```bash
docker compose down
```

O banco de dados continuará preservado.

Caso seja necessário remover também o volume do banco e recriar o ambiente do zero:

```bash
docker compose down -v
```

> Atenção: o comando acima remove os dados armazenados no banco PostgreSQL do Docker.

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
