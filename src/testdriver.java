
import java.sql.Driver;
import java.sql.DriverManager;

public class TestDriver {
    public static void main(String[] args) {
        try {
            Driver driver = DriverManager.getDriver(
                "jdbc:oracle:thin:@localhost:1521/FREEPDB1"
            );

            System.out.println("Oracle JDBC driver is working!");
            System.out.println(driver.getClass().getName());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
