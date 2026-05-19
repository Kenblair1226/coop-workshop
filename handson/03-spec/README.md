# Module 3 Hands-on：規格書 → 程式骨架

**時長**：15 min
**前置**：VS Code + Copilot + JDK 17

---

## 情境

業管單位送來新需求 — **定期定額轉帳服務**規格書（`sample-spec.md`），你需要在今天 sprint planning 前產出：
1. 規格書摘要（給 PM / QA review）
2. Java skeleton（DTO、Service interface、Exception）
3. 一份初步 implementation plan

過去做這件事要花 1 天，今天看 Copilot 能不能 30 分鐘內完成。

---

## 檔案

| 檔案 | 說明 |
|------|------|
| `sample-spec.md` | 業務需求規格書 |
| `reference-skeleton/` | 參考解（卡關時可看） |

---

## 步驟

### Step 1：規格書摘要與澄清（3 min）

1. 在 VS Code 開 Copilot Chat
2. 拖入 `sample-spec.md`
3. 提問：
   ```
   #file:sample-spec.md
   請以 PM 角度幫我做：
   1. 一段 100 字內的需求摘要
   2. 列出 5 個規格中「描述模糊或可能有歧義」的點
   3. 列出 3 個技術風險或邊界條件
   ```

✅ **成功標準**：Copilot 找出至少 1 個你沒注意到的歧義或風險（如：跨月底 31 號的處理、OTP 過期時間、結束日期空值的語意）

---

### Step 2：產出 Java Skeleton（5 min）

切換到 **Plan Mode**（VS Code Copilot Chat 左下角切換），讓 Copilot 先列出檔案計畫再產 code。

Prompt：
```
#file:sample-spec.md

請依此規格書，產出 Java 17 的程式骨架：
- package: com.tcb.netbanking.scheduledtransfer
- 使用 Spring Boot 3 風格
- 至少包含：
  * Entity / DTO
  * Service interface
  * Controller
  * 自訂 Exception (含錯誤碼對應)
  * Repository interface (Spring Data JPA)
- 不要實作 method body，只給 skeleton + JavaDoc
- 每個 method JavaDoc 需註明對應規格書段落（例：對應 §2.3）
- 業務規則 R-001 ~ R-006 用 inline 註解標示在會檢查它們的位置
```

切到 **Agent Mode** 執行，觀察 Copilot 自動建立多個檔案。

✅ **成功標準**：產生至少 5 個 Java 檔，且每個 method 都有 §對應的 JavaDoc

📁 **參考解**：`reference-skeleton/` 內的三個檔案

---

### Step 3：Implementation Plan（4 min）

Prompt：
```
#codebase
基於現在已產出的 skeleton 與規格書，請給我：
1. 拆解為可獨立 PR 的 task list（每個 task 1~3 天可完成）
2. task 之間的相依關係
3. 哪些 task 適合多人並行
4. 建議的測試策略（unit / integration / e2e 各占多少）
```

✅ **成功標準**：得到一份可直接搬到 sprint planning 用的 task list

---

### Step 4：將規格書轉為測試案例（3 min）

挑一個 method（建議 `create()`），請 Copilot 從規格書反推測試案例：

```
#file:sample-spec.md
#file:src/main/java/.../ScheduledTransferService.java

針對 ScheduledTransferService.create() method，依規格書產生 JUnit 5 測試案例骨架（不需 implementation）：
- 每個業務規則 R-001~R-006 至少一個測試
- 每個錯誤碼 E001~E005 至少一個測試
- 包含 Happy Path 至少 2 個（每月固定日、每季）
- 加上 boundary case：金額剛好 1 元、剛好 200,000 元
測試 method 命名格式：should_[expected]_when_[condition]
```

✅ **成功標準**：產出至少 12 個 `@Test` 骨架，命名清楚

---

## 加分題

- 將規格書中 §2.4 的 SQL DDL 請 Copilot 轉為 Flyway migration script
- 規格書中沒寫到的「契約結束自動清理」邏輯，請 Copilot 提案實作方式
- 試著用 PDF 版本規格書（如有）跑一次，比較 markdown vs PDF 的解析品質
