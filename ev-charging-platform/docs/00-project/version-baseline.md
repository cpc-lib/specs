# SPEC 7.2 Runtime Baseline

| Component | Pinned baseline |
|---|---:|
| Java | 21 |
| Apache Maven bootstrap | 3.9.16 |
| Spring Boot | 3.5.16 |
| Spring Cloud | 2025.0.3 |
| Spring Cloud Alibaba | 2025.0.0.0 |
| MyBatis-Plus | 3.5.17 |
| Testcontainers | 2.0.5 |

## Compatibility decisions

- SPEC 7.2 remains on the Spring Boot 3.5 generation rather than moving to Boot 4 during foundation work.
- Nacos Config uses `spring.config.import=optional:nacos:...` semantics.
- Gateway `lb://` routes explicitly include Spring Cloud LoadBalancer.
- Testcontainers 2.x MySQL tests use `org.testcontainers.mysql.MySQLContainer`.

Changing any baseline requires an ADR and a full release-gate rerun.

> Lifecycle note: the 3.5/2025.0 line is intentionally retained for this foundation RC to minimize simultaneous major-version migration risk. Before production go-live, reassess migration to the Spring Boot 4 / Spring Cloud 2025.1 / SCA 2025.1 line.


## Frontend pinned releases

- React 19.2.8
- React Router DOM 7.18.2
- Ant Design 6.5.0
- Axios 1.19.0
- Zustand 5.0.14
- Vite 8.1.5
- @vitejs/plugin-react 6.0.5
- TypeScript 5.8.3

These are pinned rather than using `latest`; upgrades require the frontend build gate.
