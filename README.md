# Employee Service

### Build

```shell

./gradlew build dockerBuildImage -Pdocker.registry={DOCKER REGISTRY}
```

{DOCKER REGISTRY} - the host of a docker registry where the image will be pulled(example: ghcr.io/octopusden)

### Environment variables

-----
AUTH_SERVER_URL - the URL of an auth server
AUTH_SERVER_REALM - the name of realm to call from an auth server
TECHNICAL_USER_BEARER - the token for a technical user