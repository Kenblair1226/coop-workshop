# Module 4 Hands-on：Custom Agent 客製化應用

**時長**：15 min
**前置**：VS Code + Copilot + 完成 Module 2、3

---

## 情境

你已經學會用 Copilot 寫程式、修弱點、解規格書，但每次都要在 prompt 裡重複交代「我們是金融業、要用 BigDecimal、要寫 audit log...」。

本練習要建立合庫專屬的 **Custom Agents**（VS Code 2026 起，舊稱 Custom Chat Modes），讓 Copilot 自動扮演對的角色，省去重複叮嚀。

---

## 預設提供的 Custom Agent

開啟此資料夾後，Copilot 會自動載入：

| 檔案 | Agent | 用途 |
|------|-------|------|
| `.github/copilot-instructions.md` | 全域指引 | 所有對話都自動套用合庫規範 |
| `.github/agents/tcb-code-reviewer.agent.md` | **TCB Code Reviewer** | 程式碼審查 |
| `.github/agents/tcb-security-auditor.agent.md` | **TCB Security Auditor** | 資安弱點分析 |
| `.github/agents/tcb-spec-analyst.agent.md` | **TCB Spec Analyst** | 規格書分析 |
| `.github/agents/pdf-to-md.agent.md` | **PDF to Markdown** | 用 markitdown 轉 PDF/Office → MD，可 handoff 給 Spec Analyst |
| `.github/prompts/analyze-fortify.prompt.md` | 一鍵指令 | 分析 Fortify 報告 |
| `.github/prompts/spec-to-tests.prompt.md` | 一鍵指令 | 規格 → 測試案例 |
| `.copilot/skills/markitdown/SKILL.md` | **Skill 範例** | 微軟 markitdown 包成 Copilot CLI Skill（全域可用） |

---

## 步驟

### Step 1：感受 copilot-instructions 的差異（3 min）

1. 開啟 VS Code，把整個 `handson/04-custom-agent/` 資料夾當 workspace 開
2. 開 Copilot Chat
3. 問：
   ```
   幫我寫一個簡單的金額累加 method
   ```
4. 觀察 Copilot 回的程式 — 它應該**自動**：
   - 用 `BigDecimal`（不是 `double`）
   - 設定 `RoundingMode`
   - 加 JavaDoc

5. 對照：開另一個沒有 `copilot-instructions.md` 的資料夾，問同樣問題，比較差異

✅ **成功標準**：明顯感受到 instruction 控制了程式風格

---

### Step 2：使用 TCB Code Reviewer Agent（5 min）

1. 開 `src/CrossBankTransferService.java`（這檔故意寫得很爛）
2. 在 Copilot Chat 輸入框**下方**的 agent 下拉選單，選 **TCB Code Reviewer**
3. 拖入此檔，輸入：
   ```
   #file:src/CrossBankTransferService.java
   請審查這份程式
   ```
4. 觀察輸出 — 應該包含：
   - 🔴 SQL Injection
   - 🔴 hard-coded API key
   - 🔴 用 double 處理金額
   - 🟡 ConcurrentHashMap thread safety
   - 🟡 Exception swallow
   - 🟡 缺 audit log
   - 🟡 query method 用 random 假資料

✅ **成功標準**：找出至少 5 個問題，且輸出格式符合 agent 設定（Blocker / Suggestion / 做得好 / 待釐清）

---

### Step 3：建立你自己的 Custom Agent（5 min）

挑一個你工作中常用的角色，自己寫一個 agent。範例題目（任選）：
- **TCB QA Engineer**：專門寫測試案例與測試計畫
- **TCB Tech Writer**：將程式產出 API 文件 / 操作手冊
- **TCB Refactor Specialist**：找出可重構的點，提供改寫方案

#### 建立步驟
1. 在 `.github/agents/` 新增 `tcb-XXX.agent.md`
2. 套用以下模板：
   ```markdown
   ---
   name: TCB XXX
   description: '一句話說明這個 agent 是誰'
   tools: ['codebase', 'search']
   ---

   # 角色
   [這個 agent 的身份與專長]

   ## 工作流程
   [收到任務時，依什麼步驟處理]

   ## 輸出格式
   [規定輸出結構，避免每次格式不一]

   ## 你不會做的事
   [明確劃定界線，避免越權]
   ```
3. 存檔後在 agent 下拉選單中選擇新 agent
4. 試跑一個任務驗證

✅ **成功標準**：成功建立並使用一個自己的 agent

---

### Step 4：(進階) 多 Agent 接力（2 min）

模擬一個真實工作流：

1. 切到 **TCB Spec Analyst**，丟入 `../03-spec/sample-spec.md` → 取得 task 拆解
2. 切到 **GitHub Copilot**（預設模式），請它依某個 task 寫 skeleton
3. 切到 **TCB Code Reviewer**，請它審查剛產出的 skeleton
4. 切到 **TCB Security Auditor**，請它做資安初檢

✅ **成功標準**：體會「**單純對話 → 角色化協作**」的差異

---

## 加分題

- 將 `.github/prompts/analyze-fortify.prompt.md` 套用到 Module 2 的 fortify-report.md，看一鍵指令的便利性
- 改寫 `tcb-code-reviewer.agent.md`，加入「會議記錄產出」能力
- 試用 **handoffs**：在 `tcb-spec-analyst.agent.md` 的 frontmatter 加 `handoffs:` 區塊，宣告下一棒交給 Code Reviewer / Security Auditor，體驗一鍵帶 context 換 agent
- 嘗試把這些 agent 設定提交到你們真實 repo（注意 `.github/` 是否會與其他工具衝突）

---

## 名詞對照（VS Code 2026 更新）

| 舊稱 | 新稱 | 變更 |
|------|------|------|
| Custom Chat Modes | **Custom Agents** | 同一個東西，改名 |
| `.chatmode.md` | `.agent.md` | 副檔名 |
| `.github/chatmodes/` | `.github/agents/` | 預設資料夾 |
| 從 mode 選單切換 | 從 agent 下拉切換 / `@mention` | 觸發方式擴充 |

舊的 `.chatmode.md` 檔案仍可使用，但建議照新規範重整以使用 handoffs、subagent 等新功能。
