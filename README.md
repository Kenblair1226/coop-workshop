# 合庫 GHCP Workshop

GitHub Copilot 在金融開發 SDLC 的落地應用 — 半天工作坊教材。

## 目標對象
合作金庫資訊處 — COBOL／Java 異質環境、Fortify 弱點修補、規格書與文件自動化、Custom Agent 客製化。

## 議程

| 區段 | 時長 | Hands-on |
|------|------|---------|
| Opening & GitHub Copilot 全貌 | 20 min | — |
| Module 1：Code 理解與重構 | 40 min | — |
| Module 2：Fortify 弱點修補 | 40 min | ✋ |
| ☕ 休息 | 10 min | — |
| Module 3：規格書與文件自動化 | 35 min | ✋ |
| Module 4：Custom Agent 客製化應用 | 35 min | ✋ |

詳細議程請見 [agenda.md](./agenda.md)。

## Repo 結構

```
coop/
├── README.md                  # 本檔
├── agenda.md                  # 詳細議程
├── handson/                   # 學員 Hands-on 教材與範例
│   ├── 02-fortify/
│   ├── 03-spec/
│   └── 04-custom-agent/
└── module1-cobol/             # COBOL 範例（待客戶提供帳務範例補入）
```

## 環境需求（學員開課前準備）

### 必備
- [ ] **VS Code**（最新版）
- [ ] **GitHub Copilot 擴充套件**（Copilot + Copilot Chat）
- [ ] **GitHub 帳號** 已綁定 Copilot Business / Enterprise 授權
- [ ] **JDK 17+**（Module 2、3、4 範例使用）
- [ ] **Git**

### 建議
- [ ] **Maven 3.9+** 或 **Gradle**（編譯範例 Java）
- [ ] 可瀏覽 `github.com` 與 `*.githubcopilot.com`（公司網路白名單）

### 開課前檢查指令
```bash
code --version
java -version
git --version
gh auth status        # 若有裝 GitHub CLI
```

在 VS Code 中按 `Ctrl+Shift+P` → 輸入 `Copilot: Show Status`，確認顯示 **Ready**。

## Hands-on 使用方式

每個 module 的 hands-on 都是獨立可執行的子資料夾：
```bash
cd handson/02-fortify   # 或 03-spec / 04-custom-agent
# 在 VS Code 中開啟此資料夾，依 README.md 步驟操作
```
