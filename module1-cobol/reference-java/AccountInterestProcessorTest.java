package com.tcb.workshop.module1;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 單元測試：驗證 Java 重寫版本與 COBOL 原版行為一致
 * 對應 COBOL: ACCT-INT.cbl §3000-CALC-INTEREST
 */
class AccountInterestProcessorTest {

    private final AccountInterestProcessor processor =
        new AccountInterestProcessor(null);   // 此處測純函數，DataSource 不會被用到

    @Test
    @DisplayName("R-001: 餘額 25 萬一般戶適用 Tier-1 (0.08%)")
    void should_apply_tier1_when_balance_below_500k() {
        AccountInterestProcessor.Account acct = new AccountInterestProcessor.Account(
            "00012345678901", "NS", new BigDecimal("250000.00"), "CUST01");

        AccountInterestProcessor.InterestResult r = processor.calcInterest(acct);

        assertThat(r.rate()).isEqualByComparingTo("0.000800");
        // 250000 * 0.0008 / 12 = 16.6667 → 16.67
        assertThat(r.interest()).isEqualByComparingTo("16.67");
        assertThat(r.overdraftFee()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("R-001: 餘額 120 萬一般戶適用 Tier-2 (0.12%)")
    void should_apply_tier2_when_balance_between_500k_and_3m() {
        AccountInterestProcessor.Account acct = new AccountInterestProcessor.Account(
            "00012345678902", "NS", new BigDecimal("1200000.00"), "CUST02");

        AccountInterestProcessor.InterestResult r = processor.calcInterest(acct);

        assertThat(r.rate()).isEqualByComparingTo("0.001200");
        // 1200000 * 0.0012 / 12 = 120.00
        assertThat(r.interest()).isEqualByComparingTo("120.00");
    }

    @Test
    @DisplayName("R-001: 餘額 580 萬一般戶適用 Tier-3 (0.15%)")
    void should_apply_tier3_when_balance_above_3m() {
        AccountInterestProcessor.Account acct = new AccountInterestProcessor.Account(
            "00012345678903", "NS", new BigDecimal("5800000.00"), "CUST03");

        AccountInterestProcessor.InterestResult r = processor.calcInterest(acct);

        assertThat(r.rate()).isEqualByComparingTo("0.001500");
    }

    @Test
    @DisplayName("R-002: 薪轉戶加碼 0.05%")
    void should_add_salary_bonus_when_R002_salary_account() {
        AccountInterestProcessor.Account acct = new AccountInterestProcessor.Account(
            "00012345678905", "SA", new BigDecimal("85000.00"), "CUST05");

        AccountInterestProcessor.InterestResult r = processor.calcInterest(acct);

        // Tier-1 0.0008 + bonus 0.0005 = 0.0013
        assertThat(r.rate()).isEqualByComparingTo("0.001300");
    }

    @Test
    @DisplayName("R-003: 透支戶扣 1% 違約金，不計息")
    void should_charge_overdraft_fee_when_R003_balance_negative() {
        AccountInterestProcessor.Account acct = new AccountInterestProcessor.Account(
            "00012345678904", "NS", new BigDecimal("-3500.00"), "CUST04");

        AccountInterestProcessor.InterestResult r = processor.calcInterest(acct);

        assertThat(r.interest()).isEqualByComparingTo("0");
        // |-3500| * 0.01 = 35.00
        assertThat(r.overdraftFee()).isEqualByComparingTo("35.00");
    }

    @Test
    @DisplayName("邊界：餘額剛好 500000 應落入 Tier-2 (與 COBOL < 比較一致)")
    void should_apply_tier2_when_balance_equals_500k_boundary() {
        AccountInterestProcessor.Account acct = new AccountInterestProcessor.Account(
            "00012345678906", "NS", new BigDecimal("500000.00"), "CUST06");

        AccountInterestProcessor.InterestResult r = processor.calcInterest(acct);

        assertThat(r.rate()).isEqualByComparingTo("0.001200");
    }
}
