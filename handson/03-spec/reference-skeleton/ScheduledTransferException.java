package com.tcb.netbanking.scheduledtransfer;

/**
 * 規格書 §2.3 錯誤碼
 *
 * E001 轉出帳號非本人台幣活儲       400
 * E002 金額超出限額                  400
 * E003 已達契約數上限                409
 * E004 OTP 驗證失敗                  401
 * E005 起始日期不合法                400
 */
public class ScheduledTransferException extends RuntimeException {

    private final String code;
    private final int httpStatus;

    public ScheduledTransferException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }

    public static ScheduledTransferException invalidFromAccount() {
        return new ScheduledTransferException("E001", "轉出帳號非本人台幣活儲", 400);
    }
    public static ScheduledTransferException amountOutOfLimit() {
        return new ScheduledTransferException("E002", "金額超出限額", 400);
    }
    public static ScheduledTransferException tooManyContracts() {
        return new ScheduledTransferException("E003", "已達契約數上限", 409);
    }
    public static ScheduledTransferException otpFailed() {
        return new ScheduledTransferException("E004", "OTP 驗證失敗", 401);
    }
    public static ScheduledTransferException invalidStartDate() {
        return new ScheduledTransferException("E005", "起始日期不合法", 400);
    }
}
