### chapter 17
```shell
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout ./tls.key -out ./tls.crt -subj "/CN=songko.com/O=songko.com"
```

```shell
openssl req -x509 -nodes -days 365 -newkey rsa:2048 `
  -keyout ./tls.key -out ./tls.crt `
  -subj "/CN=songko.com" `
  -addext "subjectAltName=DNS:songko.com,DNS:www.songko.com"
```

``` 
https://github.com/kubernetes/dashboard/blob/master/docs/user/access-control/creating-sample-user.md
https://kubernetes.io/ko/docs/tasks/access-application-cluster/web-ui-dashboard/
```

### chapter 16
```
docker tag my-space/config my-space/config:v1
docker tag my-space/gateway my-space/gateway:v1
docker tag my-space/product-composite-service my-space/product-composite-service:v1
docker tag my-space/product-service my-space/product-service:v1
docker tag my-space/recommendation-service my-space/recommendation-service:v1
docker tag my-space/review-service my-space/review-service:v1

docker tag my-space/product-service:v1 my-space/product-service:v2

kubectl get pods -o json | ConvertFrom-Json | Select-Object -ExpandProperty items | ForEach-Object {
$_.spec.containers | ForEach-Object {
$_.image
}
}

kubectl get pod -l app=product -o jsonpath='{.items[*].spec.containers[*].image}'
```

### chapter 11
```shell
keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore edge.p12 -validity 3650
```