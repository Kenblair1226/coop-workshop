# Module 2 Hands-on：Fortify 弱點修補

**時長**：15 min（含驗證）
**前置**：VS Code + Copilot + JDK 17

---

## 情境

你是合庫網銀後端開發者。資安團隊送來了一份 Fortify 弱掃報告，指出 `AccountQueryService.java` 共有 **6 個弱點**（2 Critical / 3 High / 1 Medium），需於本次 sprint 修復。

過去你嘗試過直接把整份 PDF 丟給 Copilot 請它修，得到的結果常常太「**範本化**」（例如把所有 SQL 都換成 PreparedStatement，但忽略業務情境）。本練習要學會**引導式修補對話**。

---

## 檔案

| 檔案 | 說明 |
|------|------|
| `src/AccountQueryService.java` | 待修補的 Java 程式（含 6 個弱點） |
| `fortify-report.md` | 簡化版 Fortify 報告 |
| `fortify-report.fpr.xml` | FPR XML 樣本（進階學員可玩） |

---

## 步驟

### Step 1：讓 Copilot 解讀 Fortify 報告（2 min）

1. 在 VS Code 開啟此資料夾
2. 開 Copilot Chat
3. 將 `fortify-report.md` 拖入 chat（或用 `#file:fortify-report.md`）
4. 提問：
   ```
   #file:fortify-report.md
   請幫我把這份 Fortify 報告依「修補優先順序」排序，
   並針對每個弱點告訴我：
   1. 修補的核心動作（一句話）
   2. 影響的程式行為（會不會破壞既有功能）
   3. 是否有單元測試需要新增
   ```

✅ **成功標準**：Copilot 給出 6 個弱點的優先序與修補摘要

---

### Step 2：引導式修補 — SQL Injection（5 min）

🔑 **關鍵教學點**：不要說「修這個弱點」，要**指明風險、要求方式、加上限制**。

1. 開 `src/AccountQueryService.java`
2. 選取 `queryAccount` method
3. 按 `Ctrl+I` 開 inline chat，輸入：

   ```
   這個 method 有 SQL Injection 風險（Fortify SQLI-001）。
   請改為使用 PreparedStatement 修補，要求：
   - 保留原本的 method signature 與回傳結構
   - 使用 try-with-resources 確保 Connection / Statement / ResultSet 都關閉
   - 加上一個 JavaDoc 註解，說明此修改對應 Fortify ID
   - 不要動其他 method
   ```

4. 檢視 diff，按 Accept
5. 追問：
   ```
   請補一個 JUnit 5 測試案例，驗證輸入 "'; DROP TABLE account; --"
   不會造成 SQL Injection
   ```

✅ **成功標準**：程式改用 `PreparedStatement`，且測試案例可以證明攻擊字串被當成普通字串處理

---

### Step 3：批次修補 — High / Medium 弱點（5 min）

挑兩個你想練的（建議 XSS-001 與 PATH-001），用同樣的「**指明 ID + 要求 + 限制**」格式請 Copilot 修補。

範例 prompt：
```
#file:src/AccountQueryService.java
請修補 PATH-001（Path Manipulation）：
- 用白名單方式：只允許 *.pdf 且檔名僅含英數字底線
- 使用 Path.normalize() 後驗證 startsWith("/var/tcb/statements/")
- 違規時拋 IllegalArgumentException("invalid filename")
- 不要動其他 method
```

✅ **成功標準**：完成至少 2 個額外弱點修補，且每個都有對應的 prompt 紀錄

---

### Step 4：自我審查（3 min）

對修補後的整份程式問 Copilot：
```
#file:src/AccountQueryService.java
這份程式還有沒有任何資安疑慮（不限於 Fortify 找出的）？
請列出潛在問題，但不要直接修，先讓我評估。
```

觀察 Copilot 是否找出 Fortify 沒列的問題（例如 `DriverManager.getConnection` 沒處理連線洩漏、`logQuery` 應改用 logger framework 等）。

✅ **成功標準**：學員體會到「Copilot 是第二雙眼睛」的價值

---

## 加分題（時間允許）

- 將 `DB_PASSWORD` 改為從環境變數讀取（HP-001）
- 將 MD5 改為 HMAC-SHA256（CRYPTO-001），並請 Copilot 解釋為什麼 HMAC 比純 hash 安全
- 將整個 class 重構為使用 Spring JdbcTemplate（進階）
