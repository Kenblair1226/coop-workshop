# 業務需求規格書 — 定期定額轉帳服務

| 項目 | 內容 |
|------|------|
| 文件編號 | TCB-NB-2026-0042 |
| 版本 | v1.2 |
| 提出單位 | 個人金融部 |
| 開發單位 | 資訊處網銀科 |
| 撰寫日期 | 2026-04-15 |
| 預計上線 | 2026-Q3 |

---

## 1. 需求背景

近年小額定期投資（如定期定額買基金、儲蓄險扣款）需求增加。客戶反映目前網銀僅能單筆轉帳，每月需手動操作，使用體驗不佳。

業管單位希望提供「**定期定額轉帳**」服務，讓客戶設定一次後系統自動執行。

## 2. 功能需求

### 2.1 客戶可設定一筆「定期定額轉帳」契約，欄位包含：

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| 約定編號 | String(20) | 系統自動產生 | 格式：`SCH` + yyyyMMdd + 6 碼流水 |
| 轉出帳號 | String(14) | Y | 限本行台幣活儲 |
| 轉入帳號 | String(20) | Y | 本行 / 跨行皆可 |
| 轉入銀行代碼 | String(7) | Y | 跨行需填 |
| 轉帳金額 | BigDecimal | Y | 單次 1 元 ~ 200,000 元 |
| 轉帳頻率 | Enum | Y | 每月 / 每兩月 / 每季 |
| 轉帳日 | Integer | Y | 1~31，遇假日順延至下一營業日 |
| 起始日期 | LocalDate | Y | 不可早於明日 |
| 結束日期 | LocalDate | N | 空值代表永久執行 |
| 契約狀態 | Enum | 系統管理 | ACTIVE / SUSPENDED / TERMINATED |

### 2.2 業務規則

- **R-001**：每位客戶最多 20 筆 ACTIVE 契約
- **R-002**：跨行轉帳需收手續費 NT$ 15，由轉出帳戶扣繳
- **R-003**：轉出帳戶餘額不足時，本次扣款失敗並記錄，**不重試**
- **R-004**：連續 3 次扣款失敗，契約自動轉為 SUSPENDED 並通知客戶
- **R-005**：契約建立後 24 小時內可無條件取消，過後僅能停用
- **R-006**：金額異動需重新進行 OTP 驗證

### 2.3 對外 API

#### POST /api/v1/scheduled-transfer
建立契約

**Request**：
```json
{
  "fromAccount": "12345678901234",
  "toAccount": "98765432109876",
  "toBankCode": "0050000",
  "amount": 5000,
  "frequency": "MONTHLY",
  "transferDay": 5,
  "startDate": "2026-06-01",
  "endDate": null,
  "otpToken": "abc123"
}
```

**Response (201)**：
```json
{
  "scheduleId": "SCH20260415000042",
  "status": "ACTIVE",
  "nextRunDate": "2026-06-05"
}
```

**錯誤碼**：
| 代碼 | 說明 | HTTP Status |
|------|------|-------------|
| E001 | 轉出帳號非本人台幣活儲 | 400 |
| E002 | 金額超出限額 | 400 |
| E003 | 已達契約數上限 | 409 |
| E004 | OTP 驗證失敗 | 401 |
| E005 | 起始日期不合法 | 400 |

#### GET /api/v1/scheduled-transfer/{scheduleId}
查詢單筆契約

#### DELETE /api/v1/scheduled-transfer/{scheduleId}
取消 / 停用契約（依建立時間判斷）

#### GET /api/v1/scheduled-transfer
列出本人所有契約（支援 status 過濾）

### 2.4 排程執行

- 每日凌晨 02:00 由 batch job 掃描當日應執行契約
- 每筆契約獨立 transaction，失敗不影響其他
- 執行結果寫入 `SCHEDULED_TRANSFER_LOG` 表
- 失敗需發送通知（簡訊 / app push）

## 3. 非功能需求

- 單筆 API 回應時間 < 500ms (P95)
- Batch 處理量：10,000 筆 / 30 分鐘內完成
- 所有金額異動需保留至少 7 年
- 所有 API 需通過 Fortify 弱掃 mid 等級以上

## 4. 資料表

```sql
CREATE TABLE scheduled_transfer (
  schedule_id      VARCHAR(20)  PRIMARY KEY,
  customer_id      VARCHAR(20)  NOT NULL,
  from_account     VARCHAR(14)  NOT NULL,
  to_account       VARCHAR(20)  NOT NULL,
  to_bank_code     VARCHAR(7)   NOT NULL,
  amount           DECIMAL(15,2) NOT NULL,
  frequency        VARCHAR(20)  NOT NULL,
  transfer_day     SMALLINT     NOT NULL,
  start_date       DATE         NOT NULL,
  end_date         DATE,
  status           VARCHAR(20)  NOT NULL,
  next_run_date    DATE,
  fail_count       SMALLINT     DEFAULT 0,
  created_at       TIMESTAMP    NOT NULL,
  updated_at       TIMESTAMP    NOT NULL
);

CREATE TABLE scheduled_transfer_log (
  log_id           BIGSERIAL    PRIMARY KEY,
  schedule_id      VARCHAR(20)  NOT NULL,
  run_date         DATE         NOT NULL,
  status           VARCHAR(20)  NOT NULL,
  fail_reason      VARCHAR(200),
  amount           DECIMAL(15,2),
  fee              DECIMAL(15,2),
  created_at       TIMESTAMP    NOT NULL
);
```

## 5. UAT 驗收條件

- 設定一筆每月 5 號扣款 → 隔月 5 號帳戶被扣
- 設定 31 號扣款於 2 月 → 順延至 3 月初營業日
- 連續 3 次餘額不足 → 自動 SUSPENDED 並收到通知
- 跨行轉帳 → 手續費 15 元同時扣除
