# Local environment
`docker compose up -d`

Only MySQL, Redis, Nacos and MinIO are included in framework v1.0. This is intentional.
Use separate databases/schemas per service when implementing real bounded contexts; `marketplace_system` is only the initial local bootstrap database.
