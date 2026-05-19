      ******************************************************************
      * Program  : ACCT-INT (Account Interest Calculation)
      * Author   : TCB Information Department
      * Purpose  : 計算台幣活期儲蓄存款月息，並依規則更新帳戶餘額
      *            1. 一般帳戶：依存款餘額分段計息 (R-001)
      *            2. 薪轉戶：享 0.05% 加碼利率 (R-002)
      *            3. 餘額為負(透支)：扣 1% 透支違約金，不計息 (R-003)
      *            4. 結算後寫入 INT-LOG，供月底批次核對 (R-004)
      *
      * Invocation:
      *   ./ACCT-INT  yyyymm
      * Example   :
      *   ./ACCT-INT  202604
      *
      * 對應規格: TCB-CORE-2014-A03  §3.2 活儲計息規則
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. ACCT-INT.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. IBM-Z15.
       OBJECT-COMPUTER. IBM-Z15.

       DATA DIVISION.
       WORKING-STORAGE SECTION.

      *---- 程式參數 -----------------------------------------------------
       01  WS-RUN-YYMM            PIC X(6).
       01  WS-RUN-DATE            PIC 9(8).
       01  WS-EOF-FLAG            PIC X      VALUE 'N'.
           88  WS-EOF                     VALUE 'Y'.

      *---- 帳戶資料 (DB → host) ----------------------------------------
       01  WS-ACCT-REC.
           05  WS-ACCT-ID         PIC X(14).
           05  WS-ACCT-TYPE       PIC X(2).
              88  ACCT-NORMAL              VALUE 'NS'.
              88  ACCT-SALARY              VALUE 'SA'.
           05  WS-BALANCE         PIC S9(13)V99 COMP-3.
           05  WS-CUST-ID         PIC X(20).

      *---- 計息中間結果 -------------------------------------------------
       01  WS-INT-AMOUNT          PIC S9(11)V99 COMP-3 VALUE ZERO.
       01  WS-RATE                PIC S9(1)V9(6)  COMP-3 VALUE ZERO.
       01  WS-OVERDRAFT-FEE       PIC S9(11)V99 COMP-3 VALUE ZERO.

      *---- 利率表 (R-001 分段) ------------------------------------------
       01  WS-RATE-TIER-1         PIC 9V9(6) VALUE 0.000800.
       01  WS-RATE-TIER-2         PIC 9V9(6) VALUE 0.001200.
       01  WS-RATE-TIER-3         PIC 9V9(6) VALUE 0.001500.
       01  WS-SALARY-BONUS        PIC 9V9(6) VALUE 0.000500.
       01  WS-OVERDRAFT-PENALTY   PIC 9V9(6) VALUE 0.010000.

      *---- 計數器 -------------------------------------------------------
       01  WS-CNT-PROCESSED       PIC 9(9) COMP-3 VALUE ZERO.
       01  WS-CNT-OVERDRAFT       PIC 9(9) COMP-3 VALUE ZERO.
       01  WS-CNT-SALARY          PIC 9(9) COMP-3 VALUE ZERO.

      *---- SQL 通訊區 ---------------------------------------------------
           EXEC SQL INCLUDE SQLCA END-EXEC.

      *---- Cursor 宣告 --------------------------------------------------
           EXEC SQL
              DECLARE C-ACCT CURSOR FOR
              SELECT ACCOUNT_ID, ACCT_TYPE, BALANCE, CUSTOMER_ID
                FROM ACCOUNT
               WHERE STATUS = 'A'
                 AND CURRENCY = 'TWD'
               ORDER BY ACCOUNT_ID
           END-EXEC.

       LINKAGE SECTION.
       01  L-PARM.
           05  L-PARM-LEN         PIC 9(4) COMP.
           05  L-PARM-DATA        PIC X(6).

      ******************************************************************
       PROCEDURE DIVISION USING L-PARM.

       0000-MAIN.
           PERFORM 1000-INIT
           PERFORM 2000-PROCESS-ACCOUNTS UNTIL WS-EOF
           PERFORM 9000-TERMINATE
           GOBACK.

      ******************************************************************
      * 1000 初始化：讀參數、開 cursor
      ******************************************************************
       1000-INIT.
           MOVE L-PARM-DATA TO WS-RUN-YYMM.
           IF WS-RUN-YYMM = SPACES
              DISPLAY 'ERROR: missing yyyymm parameter'
              MOVE 16 TO RETURN-CODE
              GOBACK
           END-IF.

           COMPUTE WS-RUN-DATE = FUNCTION NUMVAL(WS-RUN-YYMM) * 100 + 1.

           EXEC SQL OPEN C-ACCT END-EXEC.
           IF SQLCODE NOT = 0
              DISPLAY 'OPEN CURSOR FAILED SQLCODE=' SQLCODE
              MOVE 12 TO RETURN-CODE
              GOBACK
           END-IF.
           DISPLAY 'ACCT-INT started for ' WS-RUN-YYMM.

      ******************************************************************
      * 2000 主迴圈：逐筆帳戶處理
      ******************************************************************
       2000-PROCESS-ACCOUNTS.
           EXEC SQL
              FETCH C-ACCT INTO :WS-ACCT-ID,
                                :WS-ACCT-TYPE,
                                :WS-BALANCE,
                                :WS-CUST-ID
           END-EXEC.

           EVALUATE SQLCODE
              WHEN 0
                 PERFORM 3000-CALC-INTEREST
                 PERFORM 4000-UPDATE-ACCOUNT
                 PERFORM 5000-WRITE-LOG
                 ADD 1 TO WS-CNT-PROCESSED
              WHEN 100
                 SET WS-EOF TO TRUE
              WHEN OTHER
                 DISPLAY 'FETCH FAILED SQLCODE=' SQLCODE
                         ' ACCT=' WS-ACCT-ID
                 SET WS-EOF TO TRUE
                 MOVE 12 TO RETURN-CODE
           END-EVALUATE.

      ******************************************************************
      * 3000 計息：實作 R-001 / R-002 / R-003
      ******************************************************************
       3000-CALC-INTEREST.
           IF WS-BALANCE < 0
      *        R-003 透支戶：扣 1% 違約金，不計息
              COMPUTE WS-OVERDRAFT-FEE =
                  FUNCTION ABS(WS-BALANCE) * WS-OVERDRAFT-PENALTY
              MOVE 0 TO WS-INT-AMOUNT
              ADD 1 TO WS-CNT-OVERDRAFT
           ELSE
      *        R-001 分段利率
              EVALUATE TRUE
                 WHEN WS-BALANCE <    500000
                    MOVE WS-RATE-TIER-1 TO WS-RATE
                 WHEN WS-BALANCE <   3000000
                    MOVE WS-RATE-TIER-2 TO WS-RATE
                 WHEN OTHER
                    MOVE WS-RATE-TIER-3 TO WS-RATE
              END-EVALUATE

      *        R-002 薪轉戶加碼
              IF ACCT-SALARY
                 COMPUTE WS-RATE = WS-RATE + WS-SALARY-BONUS
                 ADD 1 TO WS-CNT-SALARY
              END-IF

              COMPUTE WS-INT-AMOUNT ROUNDED =
                  WS-BALANCE * WS-RATE / 12
              MOVE 0 TO WS-OVERDRAFT-FEE
           END-IF.

      ******************************************************************
      * 4000 更新帳戶餘額
      ******************************************************************
       4000-UPDATE-ACCOUNT.
           IF WS-OVERDRAFT-FEE > 0
              COMPUTE WS-BALANCE = WS-BALANCE - WS-OVERDRAFT-FEE
           ELSE
              COMPUTE WS-BALANCE = WS-BALANCE + WS-INT-AMOUNT
           END-IF.

           EXEC SQL
              UPDATE ACCOUNT
                 SET BALANCE       = :WS-BALANCE,
                     LAST_INT_DATE = :WS-RUN-DATE
               WHERE ACCOUNT_ID    = :WS-ACCT-ID
           END-EXEC.
           IF SQLCODE NOT = 0
              DISPLAY 'UPDATE FAILED ACCT=' WS-ACCT-ID
                      ' SQLCODE=' SQLCODE
           END-IF.

      ******************************************************************
      * 5000 寫 INT-LOG (R-004)
      ******************************************************************
       5000-WRITE-LOG.
           EXEC SQL
              INSERT INTO INT_LOG
                 (ACCOUNT_ID, RUN_YYMM, INT_AMOUNT,
                  OVERDRAFT_FEE, RATE_APPLIED, ACCT_TYPE)
              VALUES
                 (:WS-ACCT-ID,  :WS-RUN-YYMM,  :WS-INT-AMOUNT,
                  :WS-OVERDRAFT-FEE, :WS-RATE,  :WS-ACCT-TYPE)
           END-EXEC.
           IF SQLCODE NOT = 0
              DISPLAY 'LOG INSERT FAILED ACCT=' WS-ACCT-ID
           END-IF.

      ******************************************************************
      * 9000 收尾：關 cursor，印計數
      ******************************************************************
       9000-TERMINATE.
           EXEC SQL CLOSE C-ACCT END-EXEC.

           EXEC SQL COMMIT END-EXEC.

           DISPLAY '---- ACCT-INT summary ----'.
           DISPLAY '  Processed       : ' WS-CNT-PROCESSED.
           DISPLAY '  Salary bonus    : ' WS-CNT-SALARY.
           DISPLAY '  Overdraft fee   : ' WS-CNT-OVERDRAFT.
           DISPLAY '--------------------------'.
