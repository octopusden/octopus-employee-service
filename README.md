# Employee Service

### Build

```shell

./gradlew build dockerBuildImage -Pdocker.registry={DOCKER_REGISTRY} -Ppublishing.docker.registry={PUBLISHING_DOCKER_REGISTRY}
```

{DOCKER_REGISTRY} - the host of a docker registry where base images will be pulled(example: docker.io)
{PUBLISHING_DOCKER_REGISTRY} - the host and the namespace of a docker registry where the current image will be pushed(example: ghcr.io/octopusden)

### Environment variables

-----
AUTH_SERVER_URL - the URL of an auth server
AUTH_SERVER_REALM - the name of realm to call from an auth server
TECHNICAL_USER_BEARER - the token for a technical user