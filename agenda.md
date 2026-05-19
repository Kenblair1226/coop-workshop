# 合庫 GHCP Workshop 議程

**時間**：半天 3 小時（含 10 分鐘休息）
**地點**：合作金庫資訊處
**講師**：Microsoft / GitHub Copilot 團隊

---

## Opening & GitHub Copilot 全貌（20 min）

- GitHub Copilot 產品線總覽：Chat / Edit / Agent / CLI
- 與現行 TFS 瀑布式開發流程的切入點
- 本場 workshop 範圍說明（不涵蓋 Cloud Agent）

---

## Module 1：Code 理解與重構（40 min）

- COBOL 程式解讀與 Java 重寫示範
- Table schema 反查與資料結構說明
- 業管需求 → 程式邏輯反向溯源

📁 範例：[module1-cobol/](./module1-cobol/)（待補 COBOL 帳務範例）

---

## Module 2：Fortify 弱點修補（40 min，含 Hands-on）

- Fortify Report（FPR / PDF）解讀流程
- 引導式弱點修補對話技巧（避免修補建議過於範本化）
- 修補後驗證與回歸測試
- ✋ **Hands-on**：學員實作修補一段含 SQL Injection / XSS / Path Traversal 的 Java 程式

📁 學員包：[handson/02-fortify/](./handson/02-fortify/)

---

## ☕ 休息（10 min）

---

## Module 3：規格書與文件自動化（35 min，含 Hands-on）

- PDF → Markdown Skill 應用
- 規格書 → Java skeleton 程式骨架產出
- Plan Mode → Agent Mode 工作流（本機）
- 會議紀錄 / 文件整理流程
- ✋ **Hands-on**：以範例規格書產出 Markdown 與程式骨架

📁 學員包：[handson/03-spec/](./handson/03-spec/)

---

## Module 4：Custom Agent 客製化應用（35 min，含 Hands-on）

- Custom Agents（VS Code 2026 起，原 Custom Chat Modes）/ Custom Instructions / Prompt Files 概念與差異
- 角色化 Agent 範例：Code Reviewer、Tech Lead、QA、Security Auditor
- 多 Agent 協作工作流（如：PM 拆需求 → Engineer 實作 → Reviewer 審查）
- 在 VS Code Copilot Chat 中呼叫 Custom Agent
- ✋ **Hands-on**：建立並使用一個自訂的 Code Reviewer Agent 審視自己的程式

📁 學員包：[handson/04-custom-agent/](./handson/04-custom-agent/)
