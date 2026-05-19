# 合庫網銀後端開發指引

> 本檔為 GitHub Copilot 的 repo-level instructions，所有 Chat / Edit / Agent 對話都會自動套用。
> 對應 VS Code 設定：`github.copilot.chat.codeGeneration.useInstructionFiles: true`

## 技術棧
- **Java 17** + **Spring Boot 3**
- **PostgreSQL** + **Spring Data JPA**（不直接寫 JDBC）
- **Maven** 建置
- **JUnit 5** + **Mockito** + **AssertJ** 測試
- **SLF4J** + **Logback** 記錄日誌

## 程式風格
- 所有金額一律使用 `BigDecimal`，禁用 `double` / `float`
- 日期時間使用 `java.time.*`，禁用 `java.util.Date`
- DTO 使用 Java `record`
- Service 介面用 `interface`，實作類加 `Impl` 後綴
- Controller 用 `@RestController`，不混 view rendering
- Exception 一律繼承自定義的 `TcbBusinessException`，含 `errorCode` 與 `httpStatus`

## 命名慣例
- 類別：UpperCamelCase
- 方法 / 變數：lowerCamelCase
- 常數：UPPER_SNAKE_CASE
- 資料表：snake_case
- API path：kebab-case
- 測試 method：`should_[expected]_when_[condition]`

## 安全要求（金融業合規）
- **任何使用者輸入到 SQL 的場景**，必須使用 `PreparedStatement` 或 JPA query parameter，禁止字串拼接
- **任何使用者輸入到 HTML / log 的場景**，必須先做 escape
- **絕對不可** hard-code 密碼、API key、token；一律從 `application.properties` 或環境變數讀取
- 雜湊只能用 SHA-256 以上；MAC 用 HMAC-SHA256
- 對外 API 必須有 OTP 或 token 驗證
- 任何金額異動需寫入 audit log

## 測試要求
- 新增 / 修改 method 必須有對應單元測試
- 業務規則類測試命名需註明規則編號（如 `should_reject_when_R001_exceeds_limit`）
- 修補資安弱點必須附攻擊字串測試

## 註解
- 公開 method 必須有 JavaDoc
- 對應到業務規格書的程式，JavaDoc 第一行需註明 `對應規格書 [編號] §[段落]`
- 修補 Fortify 弱點，於修改處 inline 註解標示 Fortify ID

## 提交（給 Copilot 寫 commit message 時參考）
- 使用 Conventional Commits：`feat:`、`fix:`、`refactor:`、`test:`、`docs:`、`chore:`
- subject line 用英文，body 可用中文
- 修補資安弱點：`fix(security): [Fortify-ID] [描述]`
