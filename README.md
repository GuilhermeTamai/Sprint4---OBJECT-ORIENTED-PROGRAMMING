# 🌱 VeroAI (Sistema MOTIVA) — Sprint 4

API REST para monitoramento e priorização de roçada de vegetação em rodovias.
Na **Sprint 4** o protótipo de console com JDBC puro (Sprint 3) foi migrado para **Spring Boot 4.1 + Spring Data JPA + API REST**, reaproveitando as tabelas Oracle da Sprint 3.

| Sprint | O que foi construído |
|---|---|
| 1 | Domínio base (`TrechoRodovia`), encapsulamento, protótipo console |
| 2 | Motor de regras: classe abstrata, interface IoT, polimorfismo |
| 3 | Persistência com JDBC puro + pattern DAO no Oracle |
| **4** | **Spring Boot + JPA + REST (este projeto)** |

## 👥 Integrantes
- Vitor Komura – RM563694
- Caio Castelão – RM563630
- Mirella Mascarenhas – RM562092
- Guilherme Tamai – RM563276
- André Gouveia – RM564219
- André Nobrega – RM561754

---

## 🧰 Stack
Java 17 · Spring Boot 4.1 (`webmvc`, `data-jpa`, `validation`) · Hibernate 7 · Maven · Oracle (driver `ojdbc17` via Maven) · H2 (somente testes)

> **Nota:** no Spring Boot 4 o antigo `spring-boot-starter-web` passou a se chamar `spring-boot-starter-webmvc`. É o mesmo starter do Spring MVC, apenas com o nome novo.

## ▶️ Como executar

### Pré-requisitos
- JDK 17+ e Maven (ou o Maven embutido da IDE)
- Rede/VPN da FIAP (acesso a `oracle.fiap.com.br:1521`)

### 1. Banco de dados
As tabelas são as da Sprint 3. Se ainda não existirem no seu schema, execute no SQL Developer, nesta ordem:
1. `VeroAI/SQL/seu-script-criacao.sql`
2. `VeroAI/SQL/script-dados.sql`

### 2. Credenciais (nunca no código!)
O `application.properties` **não contém usuário nem senha**: ele lê `DB_USER` e `DB_PASSWORD` de variáveis de ambiente (e, opcionalmente, `DB_HOST`, `DB_PORT` e `DB_SID`).

```powershell
# Windows PowerShell
$env:DB_USER="rmXXXXXX"
$env:DB_PASSWORD="sua_senha"
```
```bash
# Linux / Mac / Git Bash
export DB_USER=rmXXXXXX
export DB_PASSWORD=sua_senha
```

### 3. Rodar (dentro da pasta `VeroAI/`, onde está o `pom.xml`)
```bash
cd VeroAI
mvn spring-boot:run
```
Na IDE (Eclipse/IntelliJ): importe como *Existing Maven Project* e execute `br.com.rodovia.principal.SistemaPrincipal`.
A API sobe em `http://localhost:8080`.

### 4. Testes (não precisam do Oracle — usam H2 em memória)
```bash
mvn test
```
Resultado: **29 testes, 0 falhas** (`TrechoApiTest`, `IntervencaoApiTest`, `RelatorioApiTest`, `DominioTest`, `SistemaPrincipalTests`).

---

## 🧱 Arquitetura em camadas

```
VeroAI/
├── pom.xml
├── SQL/                         # scripts da Sprint 3 (seu-script-criacao.sql, script-dados.sql)
├── docs/prints/                 # evidências das requisições (Insomnia)
└── src/main/java/br/com/rodovia/
    ├── principal/SistemaPrincipal.java     # @SpringBootApplication (antes: main de console)
    ├── modelo/                    # @Entity: TrechoRodovia, TrechoIoT, IntervencaoOperacional (abstrata),
    │                              #          RocadaMecanizada, Pulverizacao, EquipeManutencao, RelatorioPrioridade
    ├── repository/                # interfaces JpaRepository (sem SQL) + derived queries
    ├── servico/                   # @Service: GerenciadorPrioridade (motor), TrechoService, EquipeService, IntervencaoService
    ├── controller/                # @RestController: só HTTP
    ├── dto/                       # records de entrada/saída + Bean Validation
    └── exception/                 # exceções de negócio + @RestControllerAdvice
```
Fluxo: **Controller → Service → Repository → Banco**. Nenhuma camada pula a vizinha.

**O que foi preservado da Sprint 2:** a herança continua sendo a mesma, agora persistida com `SINGLE_TABLE`:
- `TrechoRodovia` / `TrechoIoT` (discriminador = coluna `is_iot`; `TrechoIoT` implementa `MonitoravelViaIoT`).
- `IntervencaoOperacional` (abstrata) / `RocadaMecanizada` / `Pulverizacao` (discriminador = coluna `tipo_intervencao`).
- `GerenciadorPrioridade` continua sendo o motor de regras, agora com `@Service`.

> Consequência: o tipo IoT de um trecho e o tipo de uma intervenção já registrada **não podem ser alterados via PUT** (retorna 400); é preciso remover e recriar.

**Geração de IDs:** as tabelas da Sprint 3 usam `NUMBER GENERATED ALWAYS AS IDENTITY` (coluna de identidade do Oracle) e não sequences nomeadas. Por isso as entidades usam `@GeneratedValue(strategy = GenerationType.IDENTITY)`, que é o mapeamento fiel ao nosso banco. Um `@SequenceGenerator` apontaria para uma sequence que não existe no nosso modelo. Já `TrechoRodovia` tem chave natural (`identificador`, texto informado pelo cliente).

## 📡 Endpoints

### Trechos
| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/trechos` | Lista todos (`?nivelMinimo=30` filtra por vegetação ≥ valor) | 200 | 400 |
| GET | `/api/trechos/{id}` | Busca por identificador | 200 | 404 |
| POST | `/api/trechos` | Cria | 201 | 400, 409 |
| PUT | `/api/trechos/{id}` | Atualiza | 200 | 400, 404 |
| DELETE | `/api/trechos/{id}` | Remove | 204 | 404, 409 (tem intervenções) |
| POST | `/api/trechos/{id}/crescimento?dias=15` | Simula crescimento (Sprint 2) e grava | 200 | 400, 404 |

### Equipes
| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/equipes` | Lista todas | 200 | |
| GET | `/api/equipes/{id}` | Busca por ID | 200 | 404 |
| POST | `/api/equipes` | Cria | 201 | 400, 409 (nome duplicado) |
| PUT | `/api/equipes/{id}` | Atualiza | 200 | 400, 404, 409 |
| DELETE | `/api/equipes/{id}` | Remove | 204 | 404, 409 (tem intervenções) |

### Intervenções
| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/intervencoes` | Lista (`?tipo=PULVERIZACAO` filtra) | 200 | 400 |
| GET | `/api/intervencoes/{id}` | Busca por ID | 200 | 404 |
| POST | `/api/intervencoes` | Registra **e aplica** o efeito no trecho | 201 | 400, 404 |
| PUT | `/api/intervencoes/{id}` | Corrige o registro | 200 | 400, 404 |
| DELETE | `/api/intervencoes/{id}` | Remove | 204 | 404 |

Tipos válidos: `ROCADA_MECANIZADA` (zera a vegetação) e `PULVERIZACAO` (reduz para 5 cm).

### Relatórios de prioridade (motor de regras)
| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/relatorios` | Classifica os trechos atuais e **persiste** no histórico. Com `?aplicarIntervencoes=true` também executa as intervenções nos trechos críticos | 201 | |
| GET | `/api/relatorios` | Histórico (mais recente primeiro) | 200 | |
| GET | `/api/relatorios/{id}` | Busca por ID | 200 | 404 |
| GET | `/api/relatorios/periodo?inicio=2026-10-01&fim=2026-10-31` | Histórico por período | 200 | 400 |

**Limiares (Sprint 2):** URGENTE ≥ 45 cm · CRÍTICO ≥ 30 cm · ATENÇÃO ≥ 15 cm · NORMAL < 15 cm.
Trechos com sensor IoT usam a leitura transmitida (`MonitoravelViaIoT`). Toda a classificação vive no `@Service` (`GerenciadorPrioridade`), nunca no Controller.

### Queries Spring Data (derived queries)
| Repository | Método | Usado em |
|---|---|---|
| `TrechoRodoviaRepository` | `findByNivelVegetacaoGreaterThanEqual(Double)` | `GET /api/trechos?nivelMinimo=` |
| `IntervencaoOperacionalRepository` | `findByTipoIntervencao(String)`, `findByTrechoIdentificador(String)`, `existsByTrechoIdentificador(String)`, `existsByEquipeIdEquipe(Long)` | `GET /api/intervencoes?tipo=` e regras de integridade (409) |
| `EquipeManutencaoRepository` | `findByNomeEquipe(String)`, `existsByNomeEquipe(String)` | unicidade do nome e equipes do motor |
| `RelatorioPrioridadeRepository` | `findAllByOrderByDataGeracaoDesc()`, `findByDataGeracaoBetweenOrderByDataGeracaoDesc(inicio, fim)` | histórico e consulta por período |

### Formato padrão de erro
```json
{
  "timestamp": "2026-10-03T14:20:11",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados de entrada invalidos.",
  "detalhes": ["nivelVegetacao: nao pode ser negativo"]
}
```

## 🧪 Exemplos cURL

```bash
# 1) Criar trecho (201)
curl -i -X POST http://localhost:8080/api/trechos -H "Content-Type: application/json" \
  -d '{"identificador":"Raposo-Km10","kmInicial":10,"kmFinal":20,"nivelVegetacao":32.5,"umido":true,"iot":false}'

# 2) Listar trechos (200) e filtrar por nível mínimo (derived query)
curl -i http://localhost:8080/api/trechos
curl -i "http://localhost:8080/api/trechos?nivelMinimo=30"

# 3) Buscar por ID (200) / inexistente (404)
curl -i http://localhost:8080/api/trechos/Raposo-Km10
curl -i http://localhost:8080/api/trechos/NAO-EXISTE

# 4) Atualizar (200)
curl -i -X PUT http://localhost:8080/api/trechos/Raposo-Km10 -H "Content-Type: application/json" \
  -d '{"kmInicial":10,"kmFinal":20,"nivelVegetacao":48,"umido":true,"iot":false}'

# 5) Validação de negócio: KM final menor que inicial (400)
curl -i -X POST http://localhost:8080/api/trechos -H "Content-Type: application/json" \
  -d '{"identificador":"Invalido","kmInicial":20,"kmFinal":10,"nivelVegetacao":5,"umido":false}'

# 6) Criar equipe (201) e registrar intervenção (201) — troque X pelo idEquipe devolvido
curl -i -X POST http://localhost:8080/api/equipes -H "Content-Type: application/json" \
  -d '{"nomeEquipe":"Equipe-Drone-Gamma"}'
curl -i -X POST http://localhost:8080/api/intervencoes -H "Content-Type: application/json" \
  -d '{"tipoIntervencao":"PULVERIZACAO","idEquipe":X,"idTrecho":"Raposo-Km10"}'

# 7) Gerar relatório (201) e consultar histórico / período
curl -i -X POST http://localhost:8080/api/relatorios
curl -i http://localhost:8080/api/relatorios
curl -i "http://localhost:8080/api/relatorios/periodo?inicio=2026-10-01&fim=2026-10-31"

# 8) Integridade referencial (409) e remoção (204) — primeiro quem depende, depois o referenciado
curl -i -X DELETE http://localhost:8080/api/trechos/Raposo-Km10      # 409: possui intervenção
curl -i -X DELETE http://localhost:8080/api/intervencoes/Y           # 204 (Y = idIntervencao)
curl -i -X DELETE http://localhost:8080/api/trechos/Raposo-Km10      # 204
```
> No PowerShell use `curl.exe` (e não o alias `curl`) ou, mais simples, o Postman/Insomnia/Git Bash.

## 📸 Evidências (requisições funcionando)

Roteiro executado no **Insomnia** contra o Oracle da FIAP, em ordem. Os 20 prints estão em [`VeroAI/docs/prints/`](VeroAI/docs/prints/).

### Fase 1 — Trechos
| # | Requisição | Resultado | Print |
|---|---|---|---|
| 1 | `GET /api/trechos` (estado inicial: 3 trechos) | 200 | [01](VeroAI/docs/prints/01-get-trechos-200.png) |
| 2 | `POST /api/trechos` (Raposo-Km10, 32,5 cm → `critico: true`) | 201 | [02](VeroAI/docs/prints/02-post-trecho-201.png) |
| 3 | `GET /api/trechos/Raposo-Km10` | 200 | [03](VeroAI/docs/prints/03-get-trecho-id-200.png) |
| 4 | `GET /api/trechos/NAO-EXISTE` (JSON de erro padrão) | 404 | [04](VeroAI/docs/prints/04-get-trecho-404.png) |
| 5 | `PUT /api/trechos/Raposo-Km10` (vegetação → 48 cm) | 200 | [05](VeroAI/docs/prints/05-put-trecho-200.png) |
| 6 | `POST /api/trechos` com KM final < KM inicial (regra no Service) | 400 | [06](VeroAI/docs/prints/06-post-trecho-400.png) |
| 7 | `GET /api/trechos?nivelMinimo=30` (derived query) | 200 | [07](VeroAI/docs/prints/07-get-trechos-filtro-200.png) |

### Fase 2 — Relatórios (motor de prioridade)
| # | Requisição | Resultado | Print |
|---|---|---|---|
| 8 | `POST /api/relatorios` (1 urgente, 1 crítico, 1 atenção, 1 normal) | 201 | [08](VeroAI/docs/prints/08-post-relatorio-201.png) |
| 9 | `GET /api/relatorios` (histórico, mais recente primeiro) | 200 | [09](VeroAI/docs/prints/09-get-relatorios-200.png) |
| 10 | `GET /api/relatorios/periodo?inicio=2026-10-19&fim=2026-10-19` | 200 | [10](VeroAI/docs/prints/10-get-relatorios-periodo-200.png) |

### Fase 3 — Equipes
| # | Requisição | Resultado | Print |
|---|---|---|---|
| 11 | `GET /api/equipes` | 200 | [11](VeroAI/docs/prints/11-get-equipes-200.png) |
| 12 | `POST /api/equipes` (`Equipe-Drone-Gamma`) | 201 | [12](VeroAI/docs/prints/12-post-equipe-201.png) |
| 13 | `PUT /api/equipes/21` (renomeia para `Equipe-Drone-Delta`) | 200 | [13](VeroAI/docs/prints/13-put-equipe-200.png) |

### Fase 4 — Intervenções
| # | Requisição | Resultado | Print |
|---|---|---|---|
| 14 | `POST /api/intervencoes` (PULVERIZACAO no Raposo-Km10) | 201 | [14](VeroAI/docs/prints/14-post-intervencao-201.png) |
| 15 | `GET /api/trechos/Raposo-Km10` após a intervenção (vegetação 5 cm, `critico: false`) | 200 | [15](VeroAI/docs/prints/15-trecho-apos-intervencao.png) |
| 16 | `GET /api/intervencoes?tipo=PULVERIZACAO` (derived query) | 200 | [16](VeroAI/docs/prints/16-get-intervencoes-tipo-200.png) |
| 17 | `PUT /api/intervencoes/21` (troca a equipe para a Química-Beta) | 200 | [17](VeroAI/docs/prints/17-put-intervencao-200.png) |
| 18 | `DELETE /api/trechos/Raposo-Km10` com intervenção registrada | 409 | [18](VeroAI/docs/prints/18-delete-trecho-409.png) |

### Fase 5 — Remoções
| # | Requisição | Resultado | Print |
|---|---|---|---|
| 19 | `DELETE /api/intervencoes/21` | 204 | [19](VeroAI/docs/prints/19-delete-intervencao-204.png) |
| 20 | `DELETE /api/trechos/Raposo-Km10` | 204 | [20](VeroAI/docs/prints/20-delete-trecho-204.png) |

> Em alguns prints de `DELETE`/`GET` o painel *Body* do Insomnia mostra um JSON residual da requisição anterior. Ele **não é enviado** nesses métodos e não interfere no resultado.

## ⭐ Bônus implementados
- **DTOs** (`dto/`) separando entidade do contrato da API.
- **Bean Validation** (`@Valid`, `@NotNull`, `@NotBlank`, `@PositiveOrZero`, `@Positive`, `@Size`, `@DecimalMax`).
- **`@RestControllerAdvice`** (`GlobalExceptionHandler`) com JSON de erro padronizado (400, 404, 409).
- **Testes de API** com `@SpringBootTest` + `MockMvc` (`VeroAI/src/test`). Os antigos testes manuais com `main` viraram testes JUnit.

## 🔀 Boas práticas Git
- O projeto foi migrado e versionado de uma vez ao final da sprint; reconhecemos que isso não seguiu o
  princípio de commits incrementais.
- `.gitignore` cobre `target/`, arquivos de IDE (`.idea/`, `.vscode/`, `.classpath`, `.project`, `.settings/`) e credenciais locais (`.env`).
- **Nenhuma senha é versionada:** o `application.properties` usa apenas as variáveis `${DB_USER}` e `${DB_PASSWORD}`.

---

## 🤔 Perguntas de Reflexão

### 1. Por que o Repository é uma interface e não uma classe? Quem escreve a implementação e quando?
Porque o que importa para o nosso código é o **contrato** (`save`, `findById`, `findByTipoIntervencao`...), não o "como". Nós declaramos só a interface e o **Spring Data** escreve a implementação **em tempo de execução, na inicialização da aplicação**: ao subir o contexto, ele varre as interfaces que estendem `JpaRepository`, cria um *proxy* dinâmico para cada uma (com base na `SimpleJpaRepository`) e o registra como bean. Por isso podemos injetar `TrechoRodoviaRepository` no Service sem nunca termos escrito uma linha de implementação.

### 2. O pattern DAO da Sprint 3 "morreu" na migração ou só mudou de forma?
**Só mudou de forma.** O objetivo do DAO continua existindo: isolar o acesso a dados do resto do sistema. Na Sprint 3 nós escrevíamos tudo à mão (SQL, `PreparedStatement`, `ResultSet`, `try/finally`, `ConexaoBD`, `Record`). Agora o Repository é um DAO **gerado pelo framework**: a interface faz o papel do DAO, a `@Entity` substitui o `Record`, e o `application.properties` + pool de conexões do Spring substituem o `ConexaoBD`. Passamos de ~375 linhas por DAO para ~5 linhas por interface, mantendo a mesma separação de responsabilidades.

### 3. Por que a validação de `nivelVegetacao >= 0` (Sprint 1) deve ficar no Service e não no Controller?
Porque é uma **regra de negócio**, e não um detalhe de HTTP. O Controller é só a "porta de entrada" que traduz HTTP ↔ objetos. Se a regra ficasse nele, qualquer outro caminho que chegasse ao Service (outro controller, o motor de prioridade, um job agendado, um teste) poderia gravar dados inválidos. No Service a regra existe em **um único lugar**, vale para todas as portas de entrada e pode ser testada sem subir servidor web. (A Bean Validation nos DTOs cobre só a validação de formato — campo ausente, tipo errado — e o Service continua sendo a garantia final, por exemplo para `kmFinal > kmInicial`, como mostra o print 06.)

### 4. No JDBC puro vocês escreviam SQL. Onde está o SQL do `findByTipo()`? Quem o gerou?
Ele não está em nenhum arquivo nosso. No projeto o método se chama `findByTipoIntervencao(String)`. Quando a aplicação sobe, o Spring Data faz o *parse* do **nome do método** (`find` + `By` + propriedade `tipoIntervencao`), monta uma consulta JPA equivalente a `select i from IntervencaoOperacional i where i.tipoIntervencao = ?1` e o **Hibernate** traduz isso para o SQL do dialeto Oracle (`select ... from TB_INTERVENCAO_OPERACIONAL where tipo_intervencao=?`), usando os mapeamentos `@Table`/`@Column`. Dá para ver o SQL real no console graças a `spring.jpa.show-sql=true`.
