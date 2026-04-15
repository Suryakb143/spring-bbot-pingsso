# Deployment Guide

This guide covers deploying the PingSSO workspace applications to various environments.

## Table of Contents

1. [Local Development](#local-development)
2. [Docker Compose](#docker-compose)
3. [Kubernetes](#kubernetes)
4. [Cloud Platforms](#cloud-platforms)
5. [Production Checklist](#production-checklist)

## Local Development

### Prerequisites

- Node.js 18+ (Angular)
- Java 17+ (Spring Boot)
- Maven 3.8+ (Spring Boot)

### Setup

#### Angular Application

```bash
cd angular-pingsso-app

# Install dependencies
npm install

# Start development server
npm start

# Navigate to http://localhost:4200
```

#### Spring Boot Application

```bash
cd springboot-pingsso-app

# Build the application
mvn clean install

# Run the application
mvn spring-boot:run

# API available at http://localhost:8080
```

### Accessing the Application

1. Open browser to `http://localhost:4200`
2. Click "Login with PingSSO"
3. Or click "Demo Login" for testing without PingSSO

## Docker Compose

### Prerequisites

- Docker 20.10+
- Docker Compose 1.29+

### Configuration

Create `.env` file in project root:

```bash
# PingSSO Configuration
PINGSSO_CLIENT_ID=your-client-id
PINGSSO_CLIENT_SECRET=your-client-secret
PINGSSO_DISCOVERY_URL=https://pingsso.example.com/.well-known/openid-configuration
PINGSSO_AUTHORIZATION_URI=https://pingsso.example.com/as/authorization.oauth2
PINGSSO_TOKEN_URI=https://pingsso.example.com/as/token.oauth2
PINGSSO_USER_INFO_URI=https://pingsso.example.com/idp/userinfo.openid

# URLs
PINGSSO_REDIRECT_URI=http://localhost:4200/callback
PINGSSO_AUTH_ENDPOINT=https://pingsso.example.com/as/authorization.oauth2
PINGSSO_TOKEN_ENDPOINT=https://pingsso.example.com/as/token.oauth2
PINGSSO_USERINFO_ENDPOINT=https://pingsso.example.com/idp/userinfo.openid
API_URL=http://localhost:8080/api
```

### Deployment

```bash
# Build images
docker-compose build

# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Accessing Services

- Frontend: `http://localhost:4200`
- API: `http://localhost:8080`
- H2 Database: `http://localhost:8080/h2-console`

### Database Console

Access H2 database at `http://localhost:8080/h2-console`:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## Kubernetes

### Prerequisites

- Kubernetes 1.20+
- kubectl CLI
- Docker images pushed to registry

### Building and Pushing Images

```bash
# Set your registry
REGISTRY=docker.io/username

# Build and push Spring Boot
docker build -t $REGISTRY/pingsso-api:latest springboot-pingsso-app/
docker push $REGISTRY/pingsso-api:latest

# Build and push Angular
docker build -t $REGISTRY/pingsso-app:latest angular-pingsso-app/
docker push $REGISTRY/pingsso-app:latest
```

### Create ConfigMap for Configuration

```bash
# Create namespace
kubectl create namespace pingsso

# Create ConfigMap
kubectl create configmap pingsso-config \
  --from-literal=PINGSSO_CLIENT_ID="your-client-id" \
  --from-literal=API_URL="http://pingsso-api:8080/api" \
  -n pingsso
```

### Create Secret for Sensitive Data

```bash
kubectl create secret generic pingsso-secrets \
  --from-literal=PINGSSO_CLIENT_SECRET="your-client-secret" \
  -n pingsso
```

### Deploy with Kubernetes Manifests

Create `k8s/deployment.yaml`:

```yaml
---
apiVersion: v1
kind: Service
metadata:
  name: pingsso-api
  namespace: pingsso
spec:
  selector:
    app: pingsso-api
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
  type: ClusterIP

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pingsso-api
  namespace: pingsso
spec:
  replicas: 2
  selector:
    matchLabels:
      app: pingsso-api
  template:
    metadata:
      labels:
        app: pingsso-api
    spec:
      containers:
      - name: pingsso-api
        image: docker.io/username/pingsso-api:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: pingsso-config
        - secretRef:
            name: pingsso-secrets
        livenessProbe:
          httpGet:
            path: /api/users/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/users/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"

---
apiVersion: v1
kind: Service
metadata:
  name: pingsso-app
  namespace: pingsso
spec:
  selector:
    app: pingsso-app
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
  type: LoadBalancer

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pingsso-app
  namespace: pingsso
spec:
  replicas: 2
  selector:
    matchLabels:
      app: pingsso-app
  template:
    metadata:
      labels:
        app: pingsso-app
    spec:
      containers:
      - name: pingsso-app
        image: docker.io/username/pingsso-app:latest
        ports:
        - containerPort: 80
        env:
        - name: API_URL
          valueFrom:
            configMapKeyRef:
              name: pingsso-config
              key: API_URL
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
```

Deploy:

```bash
kubectl apply -f k8s/deployment.yaml
```

### Access the Application

```bash
# port forward
kubectl port-forward -n pingsso svc/pingsso-app 4200:80
kubectl port-forward -n pingsso svc/pingsso-api 8080:8080

# Or get load balancer IP
kubectl get svc -n pingsso pingsso-app
```

## Cloud Platforms

### AWS (EC2 + RDS)

#### Prerequisites
- AWS Account
- AWS CLI configured
- EC2 key pair

#### Deploy Spring Boot to EC2

```bash
# Create T3 instance
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t3.medium \
  --key-name my-key-pair \
  --security-groups web-sg

# Connect to instance
ssh -i my-key-pair.pem ec2-user@<instance-ip>

# Install Java
sudo yum install java-17-amazon-corretto-devel

# Copy JAR
scp -i my-key-pair.pem \
  target/pingsso-springboot-app-1.0.0.jar \
  ec2-user@<instance-ip>:~/

# Run application
java -jar pingsso-springboot-app-1.0.0.jar
```

#### Deploy Angular to S3 + CloudFront

```bash
# Build Angular app
cd angular-pingsso-app
npm run build -- --configuration production

# Create S3 bucket
aws s3 mb s3://pingsso-app --region us-east-1

# Upload files
aws s3 sync dist/angular-pingsso-app s3://pingsso-app/ --delete

# Create CloudFront distribution (via AWS Console)
# Point origin to S3 bucket
# Set default root object to index.html
# Add error pages for routing
```

### Heroku

#### Prerequisites
- Heroku CLI installed
- Git repository

#### Deploy Spring Boot

```bash
# Create app
heroku create pingsso-api

# Set environment variables
heroku config:set \
  PINGSSO_CLIENT_ID="your-client-id" \
  PINGSSO_CLIENT_SECRET="your-client-secret" \
  -a pingsso-api

# Create Procfile
echo "web: java -jar target/pingsso-springboot-app-1.0.0.jar" > Procfile

# Deploy
git push heroku main

# View logs
heroku logs -a pingsso-api --tail
```

#### Deploy Angular

```bash
# Create app
heroku create pingsso-app

# Add buildpack
heroku buildpacks:add heroku/nodejs -a pingsso-app

# Set environment variables
heroku config:set \
  API_URL="https://pingsso-api.herokuapp.com/api" \
  -a pingsso-app

# Create Procfile
echo "web: npm start -- --prod" > Procfile

# Deploy
git push heroku main
```

### Google Cloud Run

#### Build and Push Docker Images

```bash
# Set project ID
PROJECT_ID=my-gcp-project

# Build and push API
docker build -t gcr.io/$PROJECT_ID/pingsso-api:latest springboot-pingsso-app/
docker push gcr.io/$PROJECT_ID/pingsso-api:latest

# Build and push App
docker build -t gcr.io/$PROJECT_ID/pingsso-app:latest angular-pingsso-app/
docker push gcr.io/$PROJECT_ID/pingsso-app:latest
```

#### Deploy to Cloud Run

```bash
# Deploy API
gcloud run deploy pingsso-api \
  --image gcr.io/$PROJECT_ID/pingsso-api:latest \
  --platform managed \
  --region us-central1 \
  --set-env-vars PINGSSO_CLIENT_ID="your-client-id",PINGSSO_CLIENT_SECRET="your-client-secret"

# Deploy App
gcloud run deploy pingsso-app \
  --image gcr.io/$PROJECT_ID/pingsso-app:latest \
  --platform managed \
  --region us-central1 \
  --set-env-vars API_URL="https://pingsso-api-xxxxx.a.run.app/api"
```

## Production Checklist

### Security

- [ ] Enable HTTPS/TLS
- [ ] Store secrets in secure vault (not in code)
- [ ] Use environment-specific configurations
- [ ] Enable firewall rules
- [ ] Restrict CORS origins to known domains
- [ ] Implement rate limiting
- [ ] Enable security headers
- [ ] Use security scanning tools (OWASP, npm audit)
- [ ] Implement WAF (Web Application Firewall)
- [ ] Enable logging and monitoring

### Performance

- [ ] Enable gzip compression
- [ ] Minify and bundle Angular assets
- [ ] Use CDN for static assets
- [ ] Implement caching strategies
- [ ] Set appropriate database indexes
- [ ] Monitor response times
- [ ] Implement load balancing
- [ ] Use connection pooling

### Reliability

- [ ] Implement health checks
- [ ] Configure auto-scaling
- [ ] Set up backup and disaster recovery
- [ ] Implement monitoring and alerts
- [ ] Set up log aggregation
- [ ] Configure retry logic
- [ ] Implement circuit breakers
- [ ] Test failover scenarios

### Compliance

- [ ] Review data privacy regulations (GDPR, etc.)
- [ ] Implement data retention policies
- [ ] Audit logging for compliance
- [ ] Document security practices
- [ ] Implement access controls
- [ ] Regular security audits
- [ ] Vendor assessment (PingSSO)
- [ ] Compliance certification (if needed)

### Monitoring & Logging

```yaml
# Example monitoring stack setup
version: '3.8'
services:
  prometheus:
    image: prom/prometheus
    volumes:
      - prometheus.yml:/etc/prometheus/prometheus.yml
  
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
  
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch
    environment:
      - discovery.type=single-node
  
  kibana:
    image: docker.elastic.co/kibana/kibana
    ports:
      - "5601:5601"
```

### Backup & Recovery

```bash
# Backup database
mysqldump -u root -p database_name > backup.sql

# Backup file storage
tar -czf application-backup.tar.gz /path/to/files

# Test restore
# Recover database
mysql -u root -p database_name < backup.sql
```

## Troubleshooting Deployment Issues

### Application won't start
1. Check logs for errors
2. Verify configuration is correct
3. Check environment variables
4. Verify database connectivity

### Health checks failing
1. Verify health check endpoint exists
2. Check application logs
3. Verify network connectivity
4. Check firewall rules

### High memory usage
1. Monitor heap usage
2. Check for memory leaks
3. Adjust JVM heap size
4. Review query performance

### Slow response times
1. Check database query performance
2. Implement caching
3. Check network latency
4. Monitor resource utilization

## Support & Resources

- [Spring Boot Deployment](https://spring.io/guides/topicals/spring-boot-docker/)
- [Angular Production Builds](https://angular.io/guide/build)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
