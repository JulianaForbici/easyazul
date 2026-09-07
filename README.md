# 💙 EasyAzul

O **EasyAzul** é uma aplicação web desenvolvida para facilitar o gerenciamento de vagas da Área Azul, centralizando informações sobre usuários, veículos, zonas de estacionamento, tickets, reservas e pagamentos.

O projeto conta com **backend em Java com Spring Boot**, **frontend em Angular** e **banco de dados PostgreSQL**, oferecendo autenticação com JWT, controle de acesso por perfil, integração com mapas e funcionalidades específicas para motoristas, fiscais e administradores.

A aplicação também possui suporte a **Docker**, permitindo executar frontend, backend e banco de dados de forma integrada, além de utilizar **GitHub Actions**, testes automatizados, Pull Requests, proteção da branch principal e versionamento automático.

---

# 🎥 Demonstração do MVP

Confira abaixo uma demonstração do EasyAzul em funcionamento, apresentando as principais telas e funcionalidades implementadas no projeto.

https://github.com/user-attachments/assets/b2a7f083-7915-46f6-88bd-683b21e2a59e

---

## 🚀 Principais funcionalidades

### 👤 Usuários e autenticação

* Cadastro e autenticação de usuários
* Login com geração de token JWT
* Controle de acesso por perfil
* Perfis de motorista, fiscal e administrador
* Validações específicas durante o cadastro
* Navegação adaptada conforme o usuário autenticado

### 🚗 Veículos

* Cadastro e gerenciamento de veículos
* Consulta de veículos vinculados ao usuário
* Identificação automática do veículo utilizado recentemente
* Seleção de veículo durante o processo de estacionamento

### 🗺️ Zonas de estacionamento

* Visualização de zonas no mapa
* Integração com OpenStreetMap
* Consulta das informações da zona
* Controle de vagas e disponibilidade
* Visualização das áreas disponíveis para estacionamento

### 🎫 Tickets e reservas

* Criação e acompanhamento de tickets
* Reserva de estacionamento
* Controle do status dos tickets
* Cancelamento e encerramento de tickets
* Renovação do estacionamento em **+30 minutos** ou **+1 hora**
* Contador de tempo restante
* Barra de progresso do ticket ativo
* Alertas quando o ticket estiver próximo do vencimento
* Avisos de atenção e estado crítico conforme o tempo restante

### 💳 Pagamentos

* Consulta de pagamentos
* Gerenciamento dos pagamentos vinculados aos tickets
* Controle do status das operações

### 🛡️ Área administrativa

* Gerenciamento de usuários
* Consulta de veículos
* Gerenciamento de tickets
* Consulta e acompanhamento de pagamentos
* Funcionalidades específicas conforme o perfil de acesso

---

## 🛠️ Tecnologias utilizadas

### Backend

* Java 17
* Spring Boot
* Spring Security
* JWT
* Spring Data JPA
* Hibernate
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

### Testes

* JUnit
* Mockito
* Angular Tests

### Infraestrutura e ferramentas

* Docker
* Docker Compose
* Git
* GitHub
* GitHub Actions
* IntelliJ IDEA
* DBeaver
* Postman / Insomnia

---

## 🏗️ Arquitetura do projeto

O EasyAzul utiliza uma arquitetura separada entre frontend, backend e banco de dados.

```text
Usuário
   ↓
Angular
Frontend
   ↓ HTTP / JSON
Spring Boot
Backend REST
   ↓
PostgreSQL
```

O **frontend Angular** é responsável pela interface e interação com o usuário.

O **backend Spring Boot** concentra as regras de negócio, autenticação, autorização e comunicação com o banco de dados.

O **PostgreSQL** é responsável pela persistência das informações da aplicação.

---

## 📦 Estrutura geral do projeto

```text
easyazul/
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── autoversion.yml
│
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       └── test/
│
├── frontend/
│   ├── Dockerfile
│   ├── package.json
│   ├── angular.json
│   └── src/
│       ├── app/
│       ├── assets/
│       └── environments/
│
├── docker-compose.yml
├── CHANGELOG.md
└── README.md
```

---

# 🐳 Como executar o projeto com Docker

## Pré-requisitos

Para executar o EasyAzul utilizando Docker, é necessário ter instalado:

* [Docker Desktop](https://docs.docker.com/desktop/setup/install/windows-install/)
* [Git](https://git-scm.com/downloads)

> Não é necessário instalar Java, Maven, Node.js, Angular CLI ou PostgreSQL separadamente para executar o projeto via Docker.

Ferramentas como IntelliJ IDEA e DBeaver são opcionais e podem ser utilizadas para desenvolvimento e acesso ao banco de dados.

---

## 1️⃣ Clonar o projeto

```bash
git clone https://github.com/JulianaForbici/easyazul.git
```

Acesse a pasta do projeto:

```bash
cd easyazul
```

---

## 2️⃣ Iniciar o Docker Desktop

Antes de executar o projeto, certifique-se de que o Docker Desktop está aberto e que o Docker Engine foi iniciado.

---

## 3️⃣ Subir a aplicação

Na raiz do projeto, execute:

```bash
docker compose up --build
```

Na primeira execução, o processo pode levar alguns minutos, pois as imagens e dependências serão baixadas e configuradas.

O Docker Compose iniciará três serviços:

* **easyazul-frontend** — aplicação Angular
* **easyazul-backend** — API Spring Boot
* **easyazul-db** — banco PostgreSQL

Nas próximas execuções, caso não existam alterações que exijam uma nova build:

```bash
docker compose up
```

---

## 4️⃣ Acessar a aplicação

Após a inicialização dos containers:

* 🌐 **Frontend:** http://localhost:4200
* ⚙️ **Backend:** http://localhost:8081
* 📚 **Swagger:** http://localhost:8081/swagger-ui/index.html
* 🗄️ **PostgreSQL:** localhost:5433

---

# 🗄️ Banco de dados

O PostgreSQL é criado e configurado automaticamente pelo Docker Compose.

Para conexão externa, por exemplo utilizando o DBeaver:

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

Os dados do PostgreSQL são mantidos em um volume Docker e não são apagados ao simplesmente parar os containers.

### Acessar o banco pelo terminal

```bash
docker exec -it easyazul-db psql -U postgres -d easyazul
```

---

# ⏹️ Parar a aplicação

Para interromper a execução no terminal:

```text
Ctrl + C
```

Para remover os containers:

```bash
docker compose down
```

Os dados do banco continuarão preservados.

Caso seja necessário remover também o volume do PostgreSQL:

```bash
docker compose down -v
```

> ⚠️ **Atenção:** este comando remove os dados armazenados no banco de dados.

---

# 🔐 Autenticação

O EasyAzul utiliza autenticação baseada em **JWT**.

Após o login com e-mail e senha, a API retorna um token utilizado nas próximas requisições para acessar rotas protegidas.

O **Spring Security** é utilizado para controlar o acesso às funcionalidades de acordo com o perfil do usuário.

---

# 🧩 Perfis de usuário

### 🚗 Motorista

Pode cadastrar veículos, visualizar zonas, criar e acompanhar tickets, realizar reservas, renovar o estacionamento e consultar pagamentos.

### 🔎 Fiscal

Pode consultar informações de veículos e tickets para apoiar a fiscalização do estacionamento rotativo.

### 🛡️ Administrador

Possui acesso às funcionalidades administrativas para gerenciamento de usuários, veículos, tickets e pagamentos.

---

# 🧪 Testes automatizados

O projeto possui testes automatizados no backend e frontend.

### Backend

Linux/macOS:

```bash
cd backend
./mvnw test
```

Windows:

```powershell
cd backend
.\mvnw.cmd test
```

### Frontend

```bash
cd frontend
npm test -- --watch=false
```

Os testes também são executados automaticamente pelo GitHub Actions durante pushes e Pull Requests.

---

# 🔄 Integração contínua

O EasyAzul utiliza **GitHub Actions** para validar automaticamente alterações realizadas no projeto.

O workflow possui dois processos principais:

### Frontend - Build and Tests

```text
Instalação das dependências
        ↓
Build Angular
        ↓
Execução dos testes
```

### Backend - Build and Tests

```text
Inicialização do PostgreSQL
        ↓
Configuração do Java 17
        ↓
Execução dos testes Maven
```

As validações são executadas durante pushes e Pull Requests nas branches `develop` e `main`.

---

# 🔀 Fluxo de desenvolvimento

O projeto utiliza branches e Pull Requests para organizar o desenvolvimento.

```text
feature / bugfix
       ↓
    develop
       ↓
Pull Request
       ↓
      main
```

A branch `main` possui regras de proteção, evitando alterações diretas sem passar pelo fluxo definido.

Antes do merge, as alterações devem passar pelas validações configuradas no repositório.

```text
Pull Request
     ↓
Code Review
     ↓
Frontend CI ✅
Backend CI  ✅
     ↓
Merge na main
```

---

# 🛡️ Proteção da branch principal

A branch `main` utiliza **Rulesets do GitHub** para aumentar a segurança do processo de desenvolvimento.

A proteção impede alterações diretas na branch principal e exige que o código seja integrado através de Pull Requests, revisão e validações automatizadas.

Isso ajuda a evitar que alterações com erros sejam adicionadas diretamente à versão principal do projeto.

---

# 🏷️ Versionamento automático

O EasyAzul possui um workflow responsável pelo versionamento automático do projeto.

As versões seguem o formato:

```text
vMAJOR.MINOR.PATCH
```

Exemplo:

```text
v1.5.0
```

O workflow é responsável por:

* Identificar a versão atual
* Calcular a próxima versão
* Criar uma nova tag no Git
* Atualizar o `CHANGELOG.md`
* Registrar as principais alterações da versão

---

# 📝 Changelog

As alterações realizadas entre as versões do projeto são registradas no arquivo:

```text
CHANGELOG.md
```

O changelog permite acompanhar a evolução das funcionalidades, correções e melhorias realizadas durante o desenvolvimento.

---

# 🗺️ Mapas

O EasyAzul utiliza integração com mapas através do **OpenStreetMap**.

A funcionalidade permite visualizar as zonas de estacionamento, consultar informações de disponibilidade e facilitar a escolha de onde estacionar.

---

# 📌 Status do projeto

🟡 **Projeto em desenvolvimento**

O EasyAzul está sendo desenvolvido como parte do **Projeto Integrador do curso de Tecnologia em Análise e Desenvolvimento de Sistemas**, com evolução contínua de funcionalidades e melhorias na experiência dos usuários.

---

# 👥 Equipe

* Anaís Queiroz Goedert
* Fernando Kenichi Takenouchi
* Guilherme Duarte da Costa
* Juliana Cristina Forbici

---

# 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos, de estudo e portfólio.
