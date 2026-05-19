package com.tcb.netbanking.scheduledtransfer;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 定期定額轉帳契約
 *
 * 對應規格書 TCB-NB-2026-0042 §2.1
 */
public class ScheduledTransfer {

    public enum Frequency {
        MONTHLY,        // 每月
        BIMONTHLY,      // 每兩月
        QUARTERLY       // 每季
    }

    public enum Status {
        ACTIVE,
        SUSPENDED,
        TERMINATED
    }

    private String scheduleId;       // SCH + yyyyMMdd + 6 碼流水
    private String customerId;
    private String fromAccount;
    private String toAccount;
    private String toBankCode;
    private BigDecimal amount;
    private Frequency frequency;
    private int transferDay;         // 1~31
    private LocalDate startDate;
    private LocalDate endDate;       // nullable
    private Status status;
    private LocalDate nextRunDate;
    private int failCount;

    // TODO: getters / setters / equals / hashCode
}
