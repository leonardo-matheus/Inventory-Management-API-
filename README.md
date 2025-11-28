# 📦 Inventory Management API

API de gerenciamento de estoques desenvolvida para o **Desafio Técnico – Desenvolvedor Sênior Move Mais**.

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.8-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 3.5.8" />
  <img src="https://img.shields.io/badge/Status-Completo-blue" alt="Status Completo" />
  <img src="https://img.shields.io/badge/Testes-JUnit%205-yellow" alt="Testes JUnit 5" />
</p>

---

## 🚀 Tecnologias

A aplicação foi construída utilizando:

* **Java 21**
* **Spring Boot 3.5.8**
    * Spring Web
    * Spring Data JPA
    * Spring Validation
    * Spring Security (**JWT**)
* **H2 Database** (em memória)
* **Flyway** (migração de banco de dados + *seed* inicial)
* **springdoc-openapi (Swagger UI)** para documentação da API
* **JUnit 5 / Spring Test** para testes automatizados

---

## ▶️ Como Executar

Para rodar a aplicação localmente:

1.  **Executar os testes** (opcional):
    ```bash
    mvn clean test
    ```
2.  **Iniciar a aplicação**:
    ```bash
    mvn spring-boot:run
    ```

A aplicação estará acessível em:
👉 **http://localhost:8080**

### 🔧 Perfis de Execução

| Perfil | Descrição |
| :--- | :--- |
| `dev` | **(Padrão)** Utiliza **H2 em memória** e habilita o **H2 Console**. |
| `test` | Executa testes automatizados. |

---

## 📘 Documentação da API

A documentação da API é gerada automaticamente pelo **Swagger UI**:

* **Swagger UI**: **http://localhost:8080/swagger-ui/index.html**
* **OpenAPI JSON**: **http://localhost:8080/v3/api-docs**

---

## 🗄️ Acesso ao H2 Console

Enquanto o perfil `dev` (padrão) estiver ativo:

* **URL**: **http://localhost:8080/h2-console**
* **JDBC URL**: `jdbc:h2:mem:estoque-dev`
* **Usuário**: `sa`
* **Senha**: (vazia)

---

## 🔐 Autenticação (JWT)

A API utiliza **JSON Web Tokens (JWT)**.

### 1. Login

Envie a requisição `POST /auth/login` para obter o token:

**Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
