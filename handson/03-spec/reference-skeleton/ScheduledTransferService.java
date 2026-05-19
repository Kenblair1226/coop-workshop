package com.tcb.netbanking.scheduledtransfer;

import java.util.List;

/**
 * 定期定額轉帳服務
 *
 * 對應規格書 TCB-NB-2026-0042 §2.3 (對外 API)
 *
 * 業務規則：
 * - R-001：每位客戶最多 20 筆 ACTIVE 契約
 * - R-005：契約建立後 24 小時內可取消，過後僅能停用
 * - R-006：金額異動需重新 OTP 驗證
 */
public interface ScheduledTransferService {

    /**
     * 建立一筆定期定額轉帳契約
     *
     * @throws ScheduledTransferException 詳見錯誤碼 E001~E005
     */
    ScheduledTransfer create(CreateScheduledTransferRequest req);

    /**
     * 查詢單筆契約
     */
    ScheduledTransfer findById(String scheduleId);

    /**
     * 取消（建立 24h 內）或停用（24h 後）契約
     */
    void cancelOrSuspend(String scheduleId);

    /**
     * 列出本人所有契約，可選擇依 status 過濾
     */
    List<ScheduledTransfer> listByCustomer(String customerId, ScheduledTransfer.Status statusFilter);
}
