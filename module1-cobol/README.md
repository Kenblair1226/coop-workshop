# Module 1：COBOL 範例 — 活儲計息批次

 Module 1 demo 用的真實風格 COBOL 程式及對應 Java 重寫成果。

## 檔案

| 檔案 | 說明 |
|------|------|
| `ACCT-INT.cbl` | 活儲計息批次主程式（約 200 行，含 EXEC SQL、PERFORM 子程式、業務規則） |
| `schema.sql` | 對應 ACCOUNT / INT_LOG 表 DDL + 範例資料 |
| `reference-java/AccountInterestProcessor.java` | Java 17 重寫版本（給講師示範用） |
| `reference-java/AccountInterestProcessorTest.java` | JUnit 5 測試案例，驗證 R-001~R-003 行為一致 |

## 程式情境

pwd

- **R-001 分段利率**
  - 餘額 < 50 萬 → 0.08% 年息
  - 50 萬 ~ 300 萬 → 0.12% 年息
  - ≥ 300 萬 → 0.15% 年息
- **R-002 薪轉戶**（`ACCT_TYPE = 'SA'`）加碼 0.05%
- **R-003 透支戶**（餘額為負）扣 1% 違約金，不計息
- **R-004** 結算結果寫入 `INT_LOG` 表供月底核對

## VS Code 開啟建議

#重寫成果。 
 COBOL 語法高亮：
- `bitlang.cobol` — COBOL Language Support

```bash
code --install-extension bitlang.cobol
```

## 講師預跑流程

1. 用 VS Code 開啟本目錄
2. Copilot Chat 操作：
   - 對 `ACCT-INT.cbl` 跑 `/explain`
   - 請 Copilot 對應到 `schema.sql` 的欄位
   - 請 Copilot 轉為 Java（對照 `reference-java/` 看品質）
3. 用 `reference-java/AccountInterestProcessorTest.java` 驗證重寫正確性

## 注意

.git .gitignore README.md agenda.md handson module1-cobol  **教學用脫敏範例**，非合庫實際程式。所有客戶 ID、帳號、金額均為虛構。instructor-only/
