package org.course.junitapp.examples.models;

import org.course.junitapp.examples.exceptions.NotEnoughMoneyException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountTest {

    Account account;

    @BeforeEach
    void initMethodTest() {
        this.account = new Account("Txema", new BigDecimal("1000.1234"));
        System.out.println("Initialising this test method");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Finishing this test method");
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("Initialising tests");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Finishing tests");
    }

    @Nested
    @DisplayName("Test account and balance methods")
    class AccountTestNameBalance {

        @Test
        @DisplayName("Test account name")
        void testAccountName() {
//        account.setPerson("Txema");
            String spected = "Txema";
            String real = account.getPerson();

            assertEquals(spected, real, () -> "Value spected: " + spected + " is not equal to: " + real);
            assertTrue(real.equals("Txema"), () -> "Value spected: " + spected + " is not equal to: " + real);
        }

        @Test
        @DisplayName("Test account balance")
        void testAccountBalance() {
            assertEquals(1000.1234, account.getBalance().doubleValue());
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
            assertFalse(account.getBalance().compareTo(BigDecimal.ZERO) < 0);
        }

        @Test
        void testAccountReference() {
            Account account = new Account("Jhon Doe", new BigDecimal("8900.9997"));
            Account account1 = new Account("Jhon Doe", new BigDecimal("8900.9997"));

            assertEquals(account, account1);
        }

        @Test
        void testAccountDebit() {
            account.debit(new BigDecimal(100));
            assertNotNull(account.getBalance());
            assertEquals(900, account.getBalance().intValue());
            assertEquals("900.1234", account.getBalance().toPlainString());
        }

        @Test
        void testAccountCredit() {
            account.credit(new BigDecimal(100));
            assertNotNull(account.getBalance());
            assertEquals(1100, account.getBalance().intValue());
            assertEquals("1100.1234", account.getBalance().toPlainString());
        }

        @Test
        void testNotEnoughMoneyException() {
            Exception exception = assertThrows(NotEnoughMoneyException.class, () -> {
                account.debit(new BigDecimal(1500));
            });
            String actual = exception.getMessage();
            String expected = "Not enough money";
            assertEquals(expected, actual);
        }

        @Test
        void testAccountTransfer() {
            Account account = new Account("Txema", new BigDecimal("2500"));
            Account account2 = new Account("Jhon Doe", new BigDecimal("1500.8989"));

            Bank bank = new Bank();
            bank.setName("Santander");
            bank.transfer(account2, account, new BigDecimal(500));
            assertEquals("1000.8989", account2.getBalance().toPlainString());
            assertEquals("3000", account.getBalance().toPlainString());
        }
    }

    @Test
    //@Disabled
    void testRelationBankAccount() {
        //fail();
        Account account = new Account("Txema", new BigDecimal("2500"));
        Account account2 = new Account("Jhon Doe", new BigDecimal("1500.8989"));

        Bank bank = new Bank();
        bank.addAccount(account);
        bank.addAccount(account2);
        bank.setName("Santander");
        bank.transfer(account2, account, new BigDecimal(500));

        assertAll(
                () -> assertEquals("1000.8989", account2.getBalance().toPlainString()),
                () -> assertEquals("3000", account.getBalance().toPlainString()),
                () -> assertEquals(2, bank.getAccounts().size()),
                () -> assertEquals("Santander", account.getBank().getName()),
                () -> assertTrue(bank.getAccounts().stream()
                        .anyMatch(acc -> acc.getPerson().equals("Txema"))),
                () -> assertEquals("Jhon Doe", bank.getAccounts().stream()
                        .filter(acc -> acc.getPerson().equals("Jhon Doe"))
                        .findFirst()
                        .get().getPerson())
        );
    }

    @Nested
    class OperationalSystem {

        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testOnlyWindows() {
        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testOnliyLinuxMac() {
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    @Nested
    class EnviromentSystemTest {

        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void onlyJre8() {
        }

        @Test
        @EnabledOnJre(JRE.JAVA_17)
        void onlyJre17() {
        }

        @Test
        @DisabledOnJre(JRE.JAVA_17)
        void disableJre17() {
        }

        @Test
        void printSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k,v) -> System.out.println(k + ":" + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = "17.0.6")
        void testJavaVersion() {
        }
    }

    @Test
    void testAccountTransferDev() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(esDev);
        Account account = new Account("Txema", new BigDecimal("2500"));
        Account account2 = new Account("Jhon Doe", new BigDecimal("1500.8989"));

        Bank bank = new Bank();
        bank.setName("Santander");
        bank.transfer(account2, account, new BigDecimal(500));
        assertEquals("1000.8989", account2.getBalance().toPlainString());
        assertEquals("3000", account.getBalance().toPlainString());
    }

    @Test
    void testAccountTransferDev2() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        Account account = new Account("Txema", new BigDecimal("2500"));
        Account account2 = new Account("Jhon Doe", new BigDecimal("1500.8989"));

        Bank bank = new Bank();
        bank.setName("Santander");
        bank.transfer(account2, account, new BigDecimal(500));
        assumingThat(esDev, () ->{
            assertEquals("1000.8989", account2.getBalance().toPlainString());
            assertEquals("3000", account.getBalance().toPlainString());
        });
    }

    @DisplayName("Repeat")
    @RepeatedTest(value=5, name = "{displayName} - Repeat number {currentRepetition} of {totalRepetitions}")
    void testAccountDebitRepeat(RepetitionInfo info) {
        if (info.getCurrentRepetition() == 3) {
            System.out.println("We are in "+info.getCurrentRepetition()+"th repeat");
        }
        account.debit(new BigDecimal(100));
        assertNotNull(account.getBalance());
        assertEquals(900, account.getBalance().intValue());
        assertEquals("900.1234", account.getBalance().toPlainString());
    }

    @ParameterizedTest(name = "Number {index} axwcuting with value {0} - {argumentsWithNames}")
    @ValueSource(strings = {"100", "200", "300", "500", "700", "1000"})
    void testAccountDebitParametrized(String amount) {
        account.debit(new BigDecimal(amount));
        assertNotNull(account.getBalance());
        assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "Number {index} axwcuting with value {0} - {argumentsWithNames}")
    @CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000"})
    void testAccountDebitCSVParametrized(String index,String amount) {
        System.out.println(index + " -> " + amount);
        account.debit(new BigDecimal(amount));
        assertNotNull(account.getBalance());
        assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @Nested
    class TimeOutTests {
        @Test
        @Timeout(1)
        void testTimeOut() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(995);
        }

        @Test
        @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
        void testTimeOut2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(495);
        }

        @Test
        void testTimeoutAssertions() {
            assertTimeout(Duration.ofSeconds(5), () -> {
                TimeUnit.MILLISECONDS.sleep(4995);
            });
        }
    }
}