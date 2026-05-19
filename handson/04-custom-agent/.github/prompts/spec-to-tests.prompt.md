---
agent: 'agent'
description: '從規格書反推 JUnit 測試案例'
---

請對指定的 Service method（我會在對話中指明），依規格書產生 JUnit 5 測試案例骨架：

## 要求

1. **每個業務規則** R-001 ~ R-XXX 至少 1 個測試
2. **每個錯誤碼** E001 ~ E0XX 至少 1 個測試
3. **每個 Happy Path** 至少 2 個測試（不同合理輸入組合）
4. **邊界值測試**：所有有上下限的欄位（金額、日期、長度）
5. 測試 method 命名：`should_[expected]_when_[condition]`
   - 業務規則類加上規則編號：`should_reject_when_R001_exceeds_limit`

## 輸出格式

每個測試只寫骨架（不要 implementation）：

```java
@Test
@DisplayName("R-001: 超過 20 筆 ACTIVE 契約時應拒絕建立")
void should_reject_when_R001_exceeds_active_limit() {
    // given:
    //   customer 已有 20 筆 ACTIVE 契約
    // when:
    //   呼叫 create(req)
    // then:
    //   拋 ScheduledTransferException(code=E003, status=409)
}
```

最後附一段總結：「共 N 個測試，建議分 X 個 test class」。
