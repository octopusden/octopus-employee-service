# Employee Service

## JDK version

11

## Project properties

| Name                       | Description                                                                          | UT    | FT    | BUILD AND RELEASE |
|----------------------------|--------------------------------------------------------------------------------------|-------|-------|-------------------|
| docker.registry            | Host of a docker registry where 3rd-party base images will be pulled from.           | **+** | **+** | **+**             |
| publishing.docker.registry | Host and the namespace of a docker registry where the built image will be pushed to. |       | **+** | **+**             |
| auth-server.url            | Auth server URL.                                                                     | **+** | **+** |                   |
| auth-server.realm          | Auth server realm.                                                                   | **+** | **+** |                   |
| auth-server.client-id      | octopus-api-gateway client Id.                                                       |       | **+** |                   |
| auth-server.client-secret  | octopus-api-gateway client secret.                                                   |       | **+** |                   |
| employee-service.user      | octopus-employee-service user.                                                       |       | **+** |                   |
| employee-service.password  | octopus-employee-service user password.                                              |       | **+** |                   |