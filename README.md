# FIAP - SUB IV - carSales

Serviço principal da plataforma de revenda de veículos.

Este repositório concentra as funcionalidades principais do software: cadastro, edição, consulta e controle de status dos veículos. As funcionalidades de listagem comercial, compra e webhook de pagamento ficam isoladas no serviço [`subIV-carSales-integration`](https://github.com/larissamy/subIV-carSales-integration), respeitando a separação de responsabilidades da arquitetura de microsserviços.

---

## Responsabilidade deste serviço

O `subIV-carSales` é o **serviço principal da solução**. Ele é responsável por:

- cadastrar veículo para venda;
- editar os dados de um veículo;
- consultar veículo por id;
- controlar o status dos veículos;
- expor endpoints internos para o serviço de venda reservar, vender ou liberar veículos;
- persistir os dados em banco próprio, separado do banco do serviço de vendas.

As operações de compra, listagem comercial de veículos disponíveis/vendidos e webhook de pagamento são responsabilidade do serviço `subIV-carSales-integration`.

---

## Arquitetura da solução

A solução é composta por dois serviços:

| Serviço | Responsabilidade | Porta |
|---|---|---|
| `subIV-carSales` | Serviço principal de veículos | `8080` |
| `subIV-carSales-integration` | Serviço isolado de vendas, listagens, compradores e webhook de pagamento | `8081` |

A comunicação entre os serviços acontece via **requisições HTTP**.

O serviço de venda consulta e atualiza veículos por meio dos endpoints de integração expostos por este serviço. Os bancos de dados são segregados: cada microsserviço possui seu próprio banco SQLite em arquivo.

---

## Banco de dados e persistência real

Este serviço utiliza um banco SQLite próprio:

```text
cars-main.db
```

Por padrão, em execução local, a aplicação usa:

```yaml
jdbc:sqlite:cars-main.db
```

No Docker e no Kubernetes, o arquivo fica em:

```text
/data/cars-main.db
```

A persistência foi configurada para não depender de banco em memória:

- execução local: arquivo `cars-main.db`;
- Docker Compose: volume nomeado `cars-main-data`;
- Kubernetes: `PersistentVolume` + `PersistentVolumeClaim` em `k8s/pvc.yaml`.

Este banco **não é compartilhado** com o serviço de vendas. O serviço `subIV-carSales-integration` utiliza outro banco, chamado `sales-service.db`.

---

## Endpoints principais

### Software principal

```http
POST /api/cars
GET /api/cars/{id}
PUT /api/cars/{id}
PATCH /api/cars/{id}
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

---

## Como executar o projeto

O `subIV-carSales` é o serviço principal da solução. Ele é responsável pelo cadastro, edição, consulta e controle de status dos veículos.

A API roda na porta `8080`.

Swagger disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Run local com Maven

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

A aplicação ficará disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Run Docker - Opção A: Docker Compose

```bash
docker compose up --build
```

A API ficará disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Run Docker - Opção B: Docker manual

```bash
mvn clean package -DskipTests
docker build -t subiv-carsales:local .
docker run --rm -p 8080:8080 -v cars-main-data:/data -e SPRING_DATASOURCE_URL=jdbc:sqlite:/data/cars-main.db subiv-carsales:local
```

A API ficará disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Run Kubernetes

```bash
mvn clean package -DskipTests
docker build -t subiv-carsales:local .
kubectl apply -f k8s/
kubectl set image deployment/carsales-api carsales-api=subiv-carsales:local
kubectl get pods
kubectl port-forward svc/carsales-api 8080:80
```

A API ficará disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

> Observação: o comando `kubectl port-forward` mantém o terminal ocupado enquanto o túnel estiver ativo. Para encerrar, pressione `Ctrl + C`.

---

## Testes e cobertura

Para rodar os testes automatizados e gerar o relatório de cobertura:

```bash
mvn clean verify
```

Relatório JaCoCo:

```text
target/site/jacoco/index.html
```

A cobertura é validada automaticamente pelo JaCoCo durante o build. O requisito mínimo da entrega é cobertura de pelo menos 80%.

---

## Kubernetes

Os manifests Kubernetes ficam no diretório:

```text
k8s/
```

Arquivos principais:

- `deployment.yaml`;
- `service.yaml`;
- `configmap.yaml`;
- `secret.yaml`;
- `pvc.yaml`.

O deployment utiliza volume persistente para manter o banco SQLite em arquivo no caminho:

```text
/data/cars-main.db
```

O container roda como usuário não-root e o volume recebe ajuste de permissão por `initContainer`, permitindo que a aplicação grave o arquivo SQLite com segurança.

Comandos úteis:

```bash
kubectl apply -f k8s/
kubectl get pods
kubectl get svc
kubectl get pvc
kubectl port-forward svc/carsales-api 8080:80
```

---

## Docker Hub

A imagem Docker publicada pela esteira CI/CD usa o nome:

```text
larissay/subiv-carsales
```

Tags publicadas:

```text
larissay/subiv-carsales:latest
larissay/subiv-carsales:<github-sha>
```

---

## CI/CD

O workflow principal fica em:

```text
.github/workflows/ci.yml
```

A esteira executa:

1. build, testes e cobertura com `mvn clean verify`;
2. upload do relatório JaCoCo como artefato;
3. validação dos manifests Kubernetes;
4. build da imagem Docker;
5. push da imagem Docker para o Docker Hub em pushes/merges para `main` ou `develop`;
6. deploy smoke test opcional em Kubernetes local temporário com `kind`;
7. deploy opcional para cluster Kubernetes real.

### Secrets necessários para push no Docker Hub

Configure no GitHub em:

```text
Settings > Secrets and variables > Actions
```

Secrets:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

### Deploy smoke test com kind

O deploy smoke test com `kind` é opcional e pode ser habilitado com a variável:

```text
ENABLE_KIND_DEPLOY=true
```

Quando desabilitado, a esteira continua validando build, testes, cobertura, manifests Kubernetes e publicação da imagem Docker.

### Deploy em cluster real

Para habilitar o deploy em um cluster Kubernetes real, configure:

```text
Repository variable: ENABLE_REAL_K8S_DEPLOY=true
Repository secret: KUBE_CONFIG=<kubeconfig em base64>
```

Sem essa configuração, o deploy real não é executado.

---

## Fluxo esperado com o serviço de vendas

1. O veículo é cadastrado no `subIV-carSales`.
2. O serviço `subIV-carSales-integration` consulta os veículos disponíveis via HTTP.
3. Ao iniciar uma compra, o serviço de vendas solicita a reserva do veículo.
4. Após o webhook de pagamento aprovado, o serviço de vendas solicita a atualização do veículo para `SOLD`.
5. Se o pagamento for cancelado, o serviço de vendas solicita a liberação do veículo para `AVAILABLE`.

---

## Swagger

Com a aplicação rodando localmente, acesse:

```text
http://localhost:8080/swagger-ui/index.html
```
