package com.tcb.workshop.module2;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * 帳戶查詢 Service
 *
 * 模擬合庫網銀後端的帳戶查詢功能。
 * 本檔故意保留多個 Fortify 高風險弱點，供學員練習修補。
 *
 * 對應 Fortify Report：handson/02-fortify/fortify-report.md
 */
public class AccountQueryService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/tcb";
    private static final String DB_USER = "tcb_app";
    // FORTIFY: Hardcoded Password (Critical) - HP-001
    private static final String DB_PASSWORD = "P@ssw0rd123!";

    /**
     * 依帳號查詢帳戶資料
     *
     * FORTIFY: SQL Injection (Critical) - SQLI-001
     */
    public Map<String, Object> queryAccount(String accountId) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Statement stmt = conn.createStatement();

        String sql = "SELECT account_id, holder_name, balance FROM account "
                   + "WHERE account_id = '" + accountId + "'";
        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            result.put("accountId", rs.getString("account_id"));
            result.put("holderName", rs.getString("holder_name"));
            result.put("balance", rs.getBigDecimal("balance"));
        }
        return result;
    }

    /**
     * 顯示帳戶持有人姓名（HTML 輸出）
     *
     * FORTIFY: Cross-Site Scripting: Reflected (High) - XSS-001
     */
    public void showHolderName(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>歡迎，" + name + "</h1>");
        out.println("</body></html>");
    }

    /**
     * 下載對帳單 PDF
     *
     * FORTIFY: Path Manipulation (High) - PATH-001
     */
    public byte[] downloadStatement(String filename) throws IOException {
        File file = new File("/var/tcb/statements/" + filename);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = fis.readAllBytes();
        fis.close();
        return data;
    }

    /**
     * 記錄查詢日誌
     *
     * FORTIFY: Log Forging (Medium) - LOG-001
     */
    public void logQuery(String userId, String accountId) {
        System.out.println("User " + userId + " queried account " + accountId);
    }

    /**
     * 使用 MD5 加密交易簽章
     *
     * FORTIFY: Weak Cryptographic Hash (High) - CRYPTO-001
     */
    public String generateSignature(String payload) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(payload.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
