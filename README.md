http://localhost:8080/swagger-ui/index.html#/rewards-controller/previewPoints


## Reward Rules

| Transaction Amount | Points Earned |
|--------------------|---------------|
| $0 – $50.00        | 0 points      |
| $50.01 – $100.00   | 1 point per dollar above $50 |
| $100.01+           | 1 point per dollar in the $50–$100 band **plus** 2 points per dollar above $100 |

**Example — $120 purchase:**
```
$50–$100 band : 50 × 1 pt = 50
>$100 band    : 20 × 2 pt = 40
Total         = 90 points
```

---

## Tech Stack

| Layer        | Technology                  |
|--------------|-----------------------------|
| Language     | Java 17                     |
| Framework    | Spring Boot 3.2             |
| Persistence  | Spring Data JPA + H2 (dev)  |
| Validation   | Jakarta Bean Validation     |
| Boilerplate  | Lombok                      |
| Testing      | JUnit 5 + MockMvc           |
| Build        | Maven                       |
