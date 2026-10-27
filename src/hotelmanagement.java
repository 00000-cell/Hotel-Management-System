
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class HotelManagement {

    static final String URL = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
    static final String USER = "SYSTEM";
    static final String PASSWORD = System.getenv("ORACLE_PASSWORD");

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Class.forName("oracle.jdbc.OracleDriver");

            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
                System.out.println("Connected to Hotel Database!");

                int choice;

                do {
                    System.out.println("\n===== HOTEL MANAGEMENT SYSTEM =====");
                    System.out.println("1. Add Room");
                    System.out.println("2. View Rooms");
                    System.out.println("3. Check Room Availability");
                    System.out.println("4. Book Room");
                    System.out.println("5. View Bookings");
                    System.out.println("6. Exit");
                    System.out.print("Enter your choice: ");

                    choice = Integer.parseInt(sc.nextLine());

                    switch (choice) {
                        case 1:
                            addRoom(con);
                            break;
                        case 2:
                            viewRooms(con);
                            break;
                        case 3:
                            checkAvailability(con);
                            break;
                        case 4:
                            bookRoom(con);
                            break;
                        case 5:
                            viewBookings(con);
                            break;
                        case 6:
                            System.out.println("Thank you for using the system!");
                            break;
                        default:
                            System.out.println("Invalid choice!");
                    }
                } while (choice != 6);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void addRoom(Connection con) throws SQLException {
        System.out.print("Enter room number: ");
        int roomNo = Integer.parseInt(sc.nextLine());

        System.out.print("Enter room type: ");
        String roomType = sc.nextLine();

        System.out.print("Enter tariff per night: ");
        double tariff = Double.parseDouble(sc.nextLine());

        if (tariff <= 0) {
            System.out.println("Tariff must be greater than zero!");
            return;
        }

        String sql = "INSERT INTO SYSTEM.HOTEL_ROOMS VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomNo);
            ps.setString(2, roomType);
            ps.setDouble(3, tariff);
            ps.executeUpdate();
            System.out.println("Room added successfully!");
        }
    }

    static void viewRooms(Connection con) throws SQLException {
        String sql = "SELECT ROOM_NO, ROOM_TYPE, TARIFF " +
                     "FROM SYSTEM.HOTEL_ROOMS ORDER BY ROOM_NO";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\nROOM NO | ROOM TYPE | TARIFF PER NIGHT");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("ROOM_NO") + " | " +
                    rs.getString("ROOM_TYPE") + " | Rs. " +
                    rs.getDouble("TARIFF")
                );
            }
        }
    }

    static double getTariff(Connection con, int roomNo) throws SQLException {
        String sql = "SELECT TARIFF FROM SYSTEM.HOTEL_ROOMS WHERE ROOM_NO = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("TARIFF");
                }
                return -1;
            }
        }
    }

    static boolean isAvailable(Connection con, int roomNo,
                               LocalDate checkIn, LocalDate checkOut)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM SYSTEM.HOTEL_BOOKINGS " +
                     "WHERE ROOM_NO = ? AND CHECK_IN < ? AND CHECK_OUT > ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomNo);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    static boolean validDates(LocalDate checkIn, LocalDate checkOut) {
        return !checkIn.isBefore(LocalDate.now()) &&
               checkOut.isAfter(checkIn);
    }

    static void checkAvailability(Connection con) throws SQLException {
        System.out.print("Enter room number: ");
        int roomNo = Integer.parseInt(sc.nextLine());

        System.out.print("Enter check-in date (YYYY-MM-DD): ");
        LocalDate checkIn = LocalDate.parse(sc.nextLine());

        System.out.print("Enter check-out date (YYYY-MM-DD): ");
        LocalDate checkOut = LocalDate.parse(sc.nextLine());

        if (!validDates(checkIn, checkOut)) {
            System.out.println("Invalid dates!");
            return;
        }

        if (getTariff(con, roomNo) < 0) {
            System.out.println("Room does not exist!");
        } else if (isAvailable(con, roomNo, checkIn, checkOut)) {
            System.out.println("Room is available!");
        } else {
            System.out.println("Room is already booked for those dates!");
        }
    }

    static void bookRoom(Connection con) throws SQLException {
        System.out.print("Enter guest name: ");
        String guestName = sc.nextLine();

        System.out.print("Enter room number: ");
        int roomNo = Integer.parseInt(sc.nextLine());

        System.out.print("Enter check-in date (YYYY-MM-DD): ");
        LocalDate checkIn = LocalDate.parse(sc.nextLine());

        System.out.print("Enter check-out date (YYYY-MM-DD): ");
        LocalDate checkOut = LocalDate.parse(sc.nextLine());

        if (guestName.trim().isEmpty() || !validDates(checkIn, checkOut)) {
            System.out.println("Invalid guest name or dates!");
            return;
        }

        double tariff = getTariff(con, roomNo);

        if (tariff < 0) {
            System.out.println("Room does not exist!");
            return;
        }

        if (!isAvailable(con, roomNo, checkIn, checkOut)) {
            System.out.println("Room is not available!");
            return;
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalCost = nights * tariff;

        String sql = "INSERT INTO SYSTEM.HOTEL_BOOKINGS " +
                     "(GUEST_NAME, ROOM_NO, CHECK_IN, CHECK_OUT, TOTAL_COST) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, guestName);
            ps.setInt(2, roomNo);
            ps.setDate(3, Date.valueOf(checkIn));
            ps.setDate(4, Date.valueOf(checkOut));
            ps.setDouble(5, totalCost);
            ps.executeUpdate();
        }

        System.out.println("\nRoom booked successfully!");
        System.out.println("Guest Name: " + guestName);
        System.out.println("Room Number: " + roomNo);
        System.out.println("Number of Nights: " + nights);
        System.out.println("Tariff Per Night: Rs. " + tariff);
        System.out.println("Total Cost: Rs. " + totalCost);
    }

    static void viewBookings(Connection con) throws SQLException {
        String sql = "SELECT BOOKING_ID, GUEST_NAME, ROOM_NO, " +
                     "CHECK_IN, CHECK_OUT, TOTAL_COST " +
                     "FROM SYSTEM.HOTEL_BOOKINGS ORDER BY BOOKING_ID";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\n===== HOTEL BOOKINGS =====");

            while (rs.next()) {
                System.out.println(
                    "Booking ID: " + rs.getInt("BOOKING_ID") +
                    " | Guest: " + rs.getString("GUEST_NAME") +
                    " | Room: " + rs.getInt("ROOM_NO") +
                    " | Check-in: " + rs.getDate("CHECK_IN") +
                    " | Check-out: " + rs.getDate("CHECK_OUT") +
                    " | Total: Rs. " + rs.getDouble("TOTAL_COST")
                );
            }
        }
    }
}
