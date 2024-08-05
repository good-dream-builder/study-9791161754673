minikube start
minikube docker-env | Invoke-Expression

kubectl delete namespace ingress-nginx
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
kubectl get pods --namespace ingress-nginx
kubectl get svc --namespace ingress-nginx

kubectl delete namespace my-space
kubectl create namespace my-space
kubectl config set-context $(kubectl config current-context) --namespace=my-space
docker-compose build

kubectl create configmap config-repo-auth              --from-file=config-repo/application.yml --from-file=config-repo/auth.yml --save-config
kubectl create configmap config-repo-gateway           --from-file=config-repo/application.yml --from-file=config-repo/gateway.yml --save-config
kubectl create configmap config-repo-product-composite --from-file=config-repo/application.yml --from-file=config-repo/product-composite.yml --save-config
kubectl create configmap config-repo-product           --from-file=config-repo/application.yml --from-file=config-repo/product.yml --save-config
kubectl create configmap config-repo-recommendation    --from-file=config-repo/application.yml --from-file=config-repo/recommendation.yml --save-config
kubectl create configmap config-repo-review            --from-file=config-repo/application.yml --from-file=config-repo/review.yml --save-config

kubectl create secret generic rabbitmq-server-credentials `
    --from-literal=RABBITMQ_DEFAULT_USER=rabbit-user-dev `
    --from-literal=RABBITMQ_DEFAULT_PASS=rabbit-pwd-dev `
    --save-config

kubectl create secret generic rabbitmq-credentials `
    --from-literal=SPRING_RABBITMQ_USERNAME=rabbit-user-dev `
    --from-literal=SPRING_RABBITMQ_PASSWORD=rabbit-pwd-dev `
    --save-config

kubectl create secret generic rabbitmq-zipkin-credentials `
    --from-literal=RABBIT_USER=rabbit-user-dev `
    --from-literal=RABBIT_PASSWORD=rabbit-pwd-dev `
    --save-config

kubectl create secret generic mongodb-server-credentials `
    --from-literal=MONGO_INITDB_ROOT_USERNAME=mongodb-user-dev `
    --from-literal=MONGO_INITDB_ROOT_PASSWORD=mongodb-pwd-dev `
    --save-config

kubectl create secret generic mongodb-credentials `
    --from-literal=SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin `
    --from-literal=SPRING_DATA_MONGODB_USERNAME=mongodb-user-dev `
    --from-literal=SPRING_DATA_MONGODB_PASSWORD=mongodb-pwd-dev `
    --save-config

kubectl create secret generic mysql-server-credentials `
    --from-literal=MYSQL_ROOT_PASSWORD=rootpwd `
    --from-literal=MYSQL_DATABASE=review-db `
    --from-literal=MYSQL_USER=mysql-user-dev `
    --from-literal=MYSQL_PASSWORD=mysql-pwd-dev `
    --save-config

kubectl create secret generic mysql-credentials `
    --from-literal=SPRING_DATASOURCE_USERNAME=mysql-user-dev `
    --from-literal=SPRING_DATASOURCE_PASSWORD=mysql-pwd-dev `
    --save-config

kubectl create secret tls tls-certificate --key kubernetes/cert/tls.key --cert kubernetes/cert/tls.crt

kubectl apply -f kubernetes/services/overlays/dev/rabbitmq-dev.yml
kubectl apply -f kubernetes/services/overlays/dev/mongodb-dev.yml
kubectl apply -f kubernetes/services/overlays/dev/mysql-dev.yml
kubectl wait --timeout=600s --for=condition=ready pod --all

kubectl apply -k kubernetes/services/overlays/dev
kubectl wait --timeout=600s --for=condition=ready pod --all