# Fortify Static Code Analyzer — 弱點報告（簡化版）

> 本檔為 Workshop hands-on 用簡化 Fortify report。完整 FPR XML 樣本見 [`fortify-report.fpr.xml`](./fortify-report.fpr.xml)。

**掃描專案**：TCB-NetBanking-Backend
**掃描時間**：2026-05-10 02:30:15
**掃描檔案**：1
**總弱點數**：6

---

## 弱點摘要

| 等級 | 數量 |
|------|------|
| Critical | 2 |
| High | 3 |
| Medium | 1 |

---

## 弱點明細

### SQLI-001 — SQL Injection
- **等級**：Critical
- **檔案**：`AccountQueryService.java:31`
- **Method**：`queryAccount(String)`
- **描述**：使用者輸入 `accountId` 直接串接到 SQL 字串中，攻擊者可注入任意 SQL，繞過驗證或讀取整張表。
- **Sink**：`Statement.executeQuery(String)`
- **Source**：method parameter `accountId`
- **CWE**：CWE-89
- **建議**：改用 `PreparedStatement` 並以 `?` 參數化查詢。

### HP-001 — Password Management: Hardcoded Password
- **等級**：Critical
- **檔案**：`AccountQueryService.java:16`
- **描述**：資料庫密碼以明文寫死於原始碼，反編譯或 repo 外洩即可取得。
- **CWE**：CWE-798
- **建議**：改用環境變數、Vault、或 Spring `@Value` 注入。

### XSS-001 — Cross-Site Scripting: Reflected
- **等級**：High
- **檔案**：`AccountQueryService.java:48`
- **Method**：`showHolderName(HttpServletRequest, HttpServletResponse)`
- **描述**：`name` 參數未經編碼直接輸出至 HTML，攻擊者可注入 JavaScript。
- **Sink**：`PrintWriter.println(String)`
- **Source**：`HttpServletRequest.getParameter`
- **CWE**：CWE-79
- **建議**：使用 OWASP Java Encoder `Encode.forHtml(name)` 進行 HTML escape。

### PATH-001 — Path Manipulation
- **等級**：High
- **檔案**：`AccountQueryService.java:60`
- **Method**：`downloadStatement(String)`
- **描述**：使用者輸入 `filename` 直接拼接到檔案路徑，可透過 `../` 讀取系統任意檔案。
- **Sink**：`new File(String)`
- **CWE**：CWE-22
- **建議**：白名單驗證 + `Path.normalize()` 後檢查是否仍位於允許目錄內。

### CRYPTO-001 — Weak Cryptographic Hash
- **等級**：High
- **檔案**：`AccountQueryService.java:79`
- **Method**：`generateSignature(String)`
- **描述**：MD5 已知存在碰撞攻擊，不適用於數位簽章或安全雜湊用途。
- **CWE**：CWE-327
- **建議**：改用 SHA-256 或 SHA-3；若是 MAC 用途請改 HMAC-SHA256。

### LOG-001 — Log Forging
- **等級**：Medium
- **檔案**：`AccountQueryService.java:69`
- **Method**：`logQuery(String, String)`
- **描述**：使用者輸入直接寫入 log，攻擊者可插入換行字元偽造其他 log entry。
- **Sink**：`System.out.println`
- **CWE**：CWE-117
- **建議**：將輸入中的 `\r` `\n` 替換為安全字元（如 `_`），或使用結構化 logging。
