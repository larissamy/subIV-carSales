# subIV-carSales

Serviço principal da plataforma de revenda de veículos para a Fase 4 do Tech Challenge SOAT.

Este repositório concentra as funcionalidades principais do software: cadastro, edição e controle de status dos veículos. As funcionalidades de listagem comercial, compra e webhook de pagamento ficam isoladas no serviço `subIV-carSales-integration`, conforme a separação de responsabilidades exigida para a arquitetura de microsserviços.

## Responsabilidade deste serviço

- Cadastrar veículo para venda.
- Editar os dados de um veículo.
- Consultar veículo por id.
- Expor endpoints internos para o serviço de venda reservar, vender ou liberar veículos.
- Persistir os dados em um banco segregado do serviço de vendas.

## Banco de dados

Por padrão, o serviço usa SQLite em arquivo:

```yaml
jdbc:sqlite:cars-main.db
```

No Docker/Kubernetes, o arquivo fica em `/data/cars-main.db`.

## Endpoints principais

### Software principal

```http
POST /api/cars
GET /api/cars/{id}
PUT /api/cars/{id}
```

### Integração HTTP com o serviço de venda

```http
GET /api/integration/cars/{id}
GET /api/integration/cars/available
GET /api/integration/cars/sold
PATCH /api/integration/cars/{id}/reserve
PATCH /api/integration/cars/{id}/sold
PATCH /api/integration/cars/{id}/available
```

## Rodando localmente

```bash
mvn clean package
mvn spring-boot:run
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## Rodando com Docker

```bash
docker build -t subiv-carsales:local .
docker run -p 8080:8080 subiv-carsales:local
```

Ou:

```bash
docker compose up --build
```

## Testes e cobertura

```bash
mvn clean verify
```

Relatório JaCoCo:

```text
target/site/jacoco/index.html
```

A regra de cobertura está configurada para mínimo de 80%.

## Kubernetes

```bash
kubectl apply -f k8s/
kubectl get pods
kubectl port-forward svc/carsales-api 8080:80
```

## CI/CD

O workflow em `.github/workflows/ci.yml` executa build, testes, validação dos manifests Kubernetes, publicação da imagem Docker e deploy após merge na branch principal.
