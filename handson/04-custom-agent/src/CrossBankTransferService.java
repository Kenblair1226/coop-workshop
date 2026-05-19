package com.tcb.workshop.module4;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 跨行轉帳服務（待 Review 版本）
 *
 * 此檔故意保留多個程式品質、安全性、業務正確性問題，
 * 供 hands-on 練習：用 Custom Agent (TCB Code Reviewer) 進行審查。
 */
public class CrossBankTransferService {

    private static final String FEE_API_KEY = "tcb-fee-api-7f3a9b";

    private Map<String, Double> dailyTransferAmount = new HashMap<>();

    public boolean transfer(String fromAccount, String toAccount,
                            String toBankCode, double amount, String userInput) {

        if (amount <= 0) return false;

        String sql = "INSERT INTO transfer_log (from_acc, to_acc, amount, memo) VALUES ('"
                + fromAccount + "','" + toAccount + "'," + amount + ",'" + userInput + "')";
        executeSql(sql);

        double fee = 15.0;
        double total = amount + fee;

        Double accumulated = dailyTransferAmount.get(fromAccount);
        if (accumulated == null) accumulated = 0.0;
        if (accumulated + total > 3000000) {
            System.out.println("over limit");
            return false;
        }
        dailyTransferAmount.put(fromAccount, accumulated + total);

        try {
            Thread.sleep(100);
            callExternalBank(toBankCode, toAccount, amount);
        } catch (Exception e) {
            // ignore
        }

        System.out.println("transfer ok: " + fromAccount + " -> " + toAccount + " amount=" + amount);
        return true;
    }

    private void executeSql(String sql) {
        // pretend to execute
    }

    private void callExternalBank(String bankCode, String account, double amount) throws Exception {
        // pretend to call FISC
    }

    public java.util.List<String> queryTransferHistory(String account, int days) {
        java.util.List<String> result = new java.util.ArrayList<>();
        for (int i = 0; i < days; i++) {
            result.add("2026-05-" + (15 - i) + " amount=" + Math.random() * 10000);
        }
        return result;
    }
}
