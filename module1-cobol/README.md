# Module 1：COBOL 範例（待補）

## 待補內容

請客戶（合庫一科）提供一段**帳務處理 COBOL** 程式，置於本目錄。

### 期望特性

- 約 100–300 行
- 包含：
  - 主要 PROCEDURE DIVISION 與至少 2 個 PERFORM 子程式
  - EXEC SQL 區塊（對 ACCOUNT 或類似表）
  - 至少 1 個明顯的業務規則（如：帳戶透支、利息計算、轉帳）
- 不含敏感資訊（客戶帳號、姓名、實際金額需脫敏）

### 預期檔案結構

```
module1-cobol/
├── README.md                # 本檔
├── ACCOUNT-PROCESS.cbl      # 主要範例程式
├── schema.sql               # 對應 DDL（脫敏假表）
└── reference-java/          # 講師預跑的 Java 重寫成果（給學員觀摩）
    └── AccountProcessor.java
```

### 講師預跑流程

1. 確認 COBOL 在 VS Code 可開啟（建議擴充套件：`bitlang.cobol`）
2. 用 `/explain` 跑過一遍，記錄輸出品質
3. 跑 COBOL → Java 轉換，將結果存入 `reference-java/` 作為參考解
4. 確認 Java 可以 `javac` 編譯通過


