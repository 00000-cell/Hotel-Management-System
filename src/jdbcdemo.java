
import java.sql.*;
import java.util.Scanner;

public class jdbcdemo {

    public static void main(String[] args) {

        String url = "jdbc:oracle:thin:@//10.3.1.83:1521/FREEPDB1";
        String username = "SYSTEM";
        String password = "Niggacmon@1";

        try (Scanner sc = new Scanner(System.in)) {

            // Connect to Oracle Database
            Connection con = DriverManager.getConnection(
                url, username, password
            );

            System.out.println("Connected to Oracle Database!");

            // Create table
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(
                    "CREATE TABLE STUDENTS_JDBC (" +
                    "ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                    "NAME VARCHAR2(50), " +
                    "MARKS NUMBER(3))"
                );

                System.out.println("Table created successfully.");

            } catch (SQLException e) {
                if (e.getErrorCode() == 955) {
                    System.out.println("Table already exists.");
                } else {
                    throw e;
                }
            }

            // Input student details
            System.out.print("Enter student name: ");
            String name = sc.nextLine();

            System.out.print("Enter marks: ");
            int marks = Integer.parseInt(sc.nextLine());

            if (marks < 0 || marks > 100) {
                System.out.println("Marks must be between 0 and 100.");
                return;
            }

            // Insert student record
            String sql =
                "INSERT INTO STUDENTS_JDBC (NAME, MARKS) VALUES (?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setInt(2, marks);

                int rows = ps.executeUpdate();
                System.out.println(rows + " record inserted.");
            }

            // Display student records
            String query =
                "SELECT ID, NAME, MARKS FROM STUDENTS_JDBC ORDER BY ID";

            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("\n--- Student Records ---");

                while (rs.next()) {
                    System.out.println(
                        rs.getInt("ID") + " | " +
                        rs.getString("NAME") + " | " +
                        rs.getInt("MARKS")
                    );
                }
            }

            con.close();
            System.out.println("\nConnection closed.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
