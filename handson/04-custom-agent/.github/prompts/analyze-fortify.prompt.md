---
agent: 'agent'
description: 'Fortify 報告分析 — 排序 + 摘要 + 修補建議'
---

請依以下步驟分析我提供的 Fortify report：

## 步驟 1：排序
依**修補優先序**重新排列所有弱點，排序原則：
1. Critical > High > Medium > Low
2. 同等級時，影響使用者資料的優先

## 步驟 2：每個弱點摘要
| 欄位 | 內容 |
|------|------|
| ID | Fortify class ID |
| 等級 | Critical / High / Medium / Low |
| 一句話描述 | 不超過 30 字 |
| 修補核心動作 | 不超過 20 字 |
| 修補影響範圍 | 是否會破壞現有 API / 行為 |
| 估時 | 0.5d / 1d / 2d / 3d+ |

## 步驟 3：產出表格
用 Markdown table 呈現，最後加一句總結：
> 共 N 個弱點，預估 X 人天。建議先處理 [...]
