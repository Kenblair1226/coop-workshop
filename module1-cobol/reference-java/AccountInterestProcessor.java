package com.tcb.workshop.module1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java 17 重寫版本：ACCT-INT.cbl
 *
 * 對應 COBOL 程式：module1-cobol/ACCT-INT.cbl
 * 對應規格書：TCB-CORE-2014-A03 §3.2 活儲計息規則
 *
 * 業務規則：
 *   R-001 一般戶分段利率（&lt; 50 萬 0.08% / &lt; 300 萬 0.12% / 其餘 0.15%）
 *   R-002 薪轉戶加碼 0.05%
 *   R-003 透支戶扣 1% 違約金，不計息
 *   R-004 結算後寫入 INT_LOG
 */
public class AccountInterestProcessor {

    private static final Logger log = LoggerFactory.getLogger(AccountInterestProcessor.class);

    // R-001 利率表
    private static final BigDecimal RATE_TIER_1 = new BigDecimal("0.000800");
    private static final BigDecimal RATE_TIER_2 = new BigDecimal("0.001200");
    private static final BigDecimal RATE_TIER_3 = new BigDecimal("0.001500");
    private static final BigDecimal SALARY_BONUS = new BigDecimal("0.000500");
    private static final BigDecimal OVERDRAFT_PENALTY = new BigDecimal("0.010000");

    private static final BigDecimal TIER_1_CEILING = new BigDecimal("500000");
    private static final BigDecimal TIER_2_CEILING = new BigDecimal("3000000");
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);

    private static final String SQL_SELECT =
        "SELECT ACCOUNT_ID, ACCT_TYPE, BALANCE, CUSTOMER_ID "
      + "  FROM ACCOUNT WHERE STATUS = 'A' AND CURRENCY = 'TWD' ORDER BY ACCOUNT_ID";

    private static final String SQL_UPDATE =
        "UPDATE ACCOUNT SET BALANCE = ?, LAST_INT_DATE = ? WHERE ACCOUNT_ID = ?";

    private static final String SQL_INSERT_LOG =
        "INSERT INTO INT_LOG (ACCOUNT_ID, RUN_YYMM, INT_AMOUNT, OVERDRAFT_FEE, RATE_APPLIED, ACCT_TYPE) "
      + "VALUES (?, ?, ?, ?, ?, ?)";

    private final DataSource dataSource;

    public AccountInterestProcessor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 主入口，等同 COBOL 的 0000-MAIN
     */
    public Summary run(YearMonth runMonth) {
        int processed = 0;
        int salary = 0;
        int overdraft = 0;
        int runDate = Integer.parseInt(runMonth.format(DateTimeFormatter.ofPattern("yyyyMM")) + "01");

        log.info("ACCT-INT started for {}", runMonth);

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement selectStmt = conn.prepareStatement(SQL_SELECT);
                 ResultSet rs = selectStmt.executeQuery()) {

                while (rs.next()) {
                    Account acct = readAccount(rs);
                    InterestResult result = calcInterest(acct);
                    BigDecimal newBalance = updateBalance(acct.balance(), result);

                    updateAccount(conn, acct.accountId(), newBalance, runDate);
                    writeLog(conn, acct, result, runMonth);

                    processed++;
                    if (result.overdraftFee().signum() > 0) overdraft++;
                    if ("SA".equals(acct.acctType()) && result.interest().signum() > 0) salary++;
                }
            }
            conn.commit();
        } catch (SQLException e) {
            log.error("ACCT-INT failed", e);
            throw new RuntimeException(e);
        }

        Summary summary = new Summary(processed, salary, overdraft);
        log.info("---- ACCT-INT summary ----");
        log.info("  Processed     : {}", summary.processed());
        log.info("  Salary bonus  : {}", summary.salaryBonus());
        log.info("  Overdraft fee : {}", summary.overdraftFee());
        return summary;
    }

    // 對應 COBOL 3000-CALC-INTEREST
    InterestResult calcInterest(Account acct) {
        if (acct.balance().signum() < 0) {
            // R-003 透支戶
            BigDecimal fee = acct.balance().abs()
                .multiply(OVERDRAFT_PENALTY)
                .setScale(2, RoundingMode.HALF_UP);
            return new InterestResult(BigDecimal.ZERO, fee, OVERDRAFT_PENALTY);
        }

        // R-001 分段
        BigDecimal rate;
        if (acct.balance().compareTo(TIER_1_CEILING) < 0) {
            rate = RATE_TIER_1;
        } else if (acct.balance().compareTo(TIER_2_CEILING) < 0) {
            rate = RATE_TIER_2;
        } else {
            rate = RATE_TIER_3;
        }

        // R-002 薪轉戶加碼
        if ("SA".equals(acct.acctType())) {
            rate = rate.add(SALARY_BONUS);
        }

        BigDecimal interest = acct.balance()
            .multiply(rate)
            .divide(MONTHS_PER_YEAR, 2, RoundingMode.HALF_UP);

        return new InterestResult(interest, BigDecimal.ZERO, rate);
    }

    private BigDecimal updateBalance(BigDecimal balance, InterestResult r) {
        return r.overdraftFee().signum() > 0
            ? balance.subtract(r.overdraftFee())
            : balance.add(r.interest());
    }

    private Account readAccount(ResultSet rs) throws SQLException {
        return new Account(
            rs.getString("ACCOUNT_ID").trim(),
            rs.getString("ACCT_TYPE").trim(),
            rs.getBigDecimal("BALANCE"),
            rs.getString("CUSTOMER_ID").trim());
    }

    private void updateAccount(Connection conn, String accountId,
                               BigDecimal newBalance, int runDate) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, runDate);
            ps.setString(3, accountId);
            ps.executeUpdate();
        }
    }

    private void writeLog(Connection conn, Account acct, InterestResult r,
                          YearMonth runMonth) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_LOG)) {
            ps.setString(1, acct.accountId());
            ps.setString(2, runMonth.format(DateTimeFormatter.ofPattern("yyyyMM")));
            ps.setBigDecimal(3, r.interest());
            ps.setBigDecimal(4, r.overdraftFee());
            ps.setBigDecimal(5, r.rate());
            ps.setString(6, acct.acctType());
            ps.executeUpdate();
        }
    }

    public record Account(String accountId, String acctType,
                          BigDecimal balance, String customerId) {}

    public record InterestResult(BigDecimal interest, BigDecimal overdraftFee, BigDecimal rate) {}

    public record Summary(int processed, int salaryBonus, int overdraftFee) {}
}
