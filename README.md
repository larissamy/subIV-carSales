# subIV-carSales

Serviço principal da plataforma de revenda de veículos para a Fase 4 do Tech Challenge SOAT.

Este repositório concentra as funcionalidades principais do software: cadastro, edição, consulta e controle de status dos veículos. As funcionalidades de listagem comercial, compra e webhook de pagamento ficam isoladas no serviço `subIV-carSales-integration`, respeitando a separação de responsabilidades da arquitetura de microsserviços.

## Responsabilidade deste serviço

- Cadastrar veículo para venda.
- Editar os dados de um veículo.
- Consultar veículo por id.
- Expor endpoints internos para o serviço de venda reservar, vender ou liberar veículos.
- Persistir os dados em banco próprio, separado do banco do serviço de vendas.

## Banco de dados e persistência real

Por padrão, o serviço usa SQLite em arquivo:

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

Isso evita a perda dos dados a cada reinício do container/pod.

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
docker run -p 8080:8080 -v cars-main-data:/data subiv-carsales:local
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
kubectl get pvc
kubectl port-forward svc/carsales-api 8080:80
```

Os manifests incluem:

- `deployment.yaml`;
- `service.yaml`;
- `configmap.yaml`;
- `secret.yaml`;
- `pvc.yaml` com `PersistentVolume` e `PersistentVolumeClaim`.

## CI/CD

O workflow `.github/workflows/ci.yml` executa:

1. build, testes e cobertura com `mvn clean verify`;
2. validação dos manifests Kubernetes com `kind`;
3. build da imagem Docker em Pull Requests;
4. build e push da imagem Docker em merges/pushes para `main` ou `develop`;
5. deploy smoke test em Kubernetes local temporário com `kind`;
6. deploy opcional para cluster real quando a variável `ENABLE_REAL_K8S_DEPLOY=true` estiver configurada.

### Secrets necessários para push no Docker Hub

Configure no GitHub em `Settings > Secrets and variables > Actions`:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

A imagem publicada usa o nome:

```text
larissay/subiv-carsales:latest
larissay/subiv-carsales:<github-sha>
```

### Deploy em cluster real

Para habilitar o deploy real, configure:

```text
Repository variable: ENABLE_REAL_K8S_DEPLOY=true
Repository secret: KUBE_CONFIG=<kubeconfig em base64>
```

Sem essa variável, o workflow ainda executa o deploy smoke test usando `kind`, que serve como evidência de deploy efetivo na esteira.
