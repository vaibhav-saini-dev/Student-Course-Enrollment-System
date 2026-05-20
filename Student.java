import java.io.*;
import java.sql.*;
import java.util.Scanner;

import javax.naming.spi.DirStateFactory.Result;

/* 	SCHEMA

Students(sid:integer, sname:string)

Courses(cid:integer, cname:string, credits:integer)

Enrolled(sid:integer, cid:integer)

*/

class Student {
	// the host name of the server and the server instance name/id
	public static final String oracleServer = "dbs3.cs.umb.edu";
	public static final String oracleServerSid = "dbs3";

	public static void main(String args[]) {
		Connection conn = null;
		conn = getConnection();
		if (conn == null)
			System.exit(1);

		// now execute query
		Scanner input = new Scanner(System.in);

		try {
			// get student id from user
			System.out.print("Student ID = ");
			int student_id = input.nextInt();
			input.nextLine();

			// Create new student
			if (student_id == -1) {
				System.out.println("Create new student. Student ID = ");
				student_id = input.nextInt();
				input.nextLine();

				while (studentExists(conn, student_id)) {
					System.out.println("Student ID already exists. Create new student. Student ID = ");
					student_id = input.nextInt();
					input.nextLine();
				}

				createStudent(conn, input, student_id);
			} else {
				String sql = "SELECT sid FROM Students WHERE sid = ?";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.clearParameters();
				pstmt.setInt(1, student_id);

				ResultSet rs = pstmt.executeQuery();

				// Adds student if this is their first time using the menu
				if (!rs.next()) {
					System.out.println("Student ID does not exist. Creating student with that ID.\n");
					createStudent(conn, input, student_id);
				}
			}

			boolean running = true;

			while (running) {
				displayMenu();

				String option = input.nextLine().toUpperCase();
				System.out.println();
				
				if (option.equals("L")) {
					listRecords(conn);
				} else if (option.equals("E")) {
					enrollStudent(conn, input, student_id);
				} else if (option.equals("W")) {
					withdrawStudent(conn, input, student_id);
				} else if (option.equals("S")) {
					searchCourse(conn, input);
				} else if (option.equals("M")) {
					myClasses(conn, student_id);
				} else if (option.equals("X")) {
					running = false;
					System.out.println("Exited program.\n");
				} else {
					System.out.println("Invalid input. Please enter either L, E, W, S, M, or X\n");
				}
			}

			conn.close();
		} catch (SQLException e) {
			System.out.println("ERROR OCCURRED");
			e.printStackTrace();
		}
	}

	public static boolean studentExists(Connection conn, int sid) throws SQLException {
		String sql = "SELECT sid FROM Students WHERE sid = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.clearParameters();
		pstmt.setInt(1, sid);

		boolean foundStudent = false;

		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			foundStudent = true;
		}

		return foundStudent;
	}

	public static void createStudent(Connection conn, Scanner input, int sid) throws SQLException {
		System.out.println("Student Name = ");
		String student_name = input.nextLine();
		
		// L14-Apr09-slides.pdf Slide 12
		String sql = "INSERT INTO Students VALUES(?, ?)";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.clearParameters();
		pstmt.setInt(1, sid);
		pstmt.setString(2, student_name);

		pstmt.executeUpdate();

		System.out.println("Successfully created student " + sid + ".\n");
	}

	public static void displayMenu() {
		System.out.println(
				"L – List: lists all records in the course table\n" +
						"E – Enroll: enrolls the active student in a course; user is prompted for course ID; check for conflicts, i.e., student cannot enroll twice in same course\n"
						+
						"W – Withdraw: deletes an entry in the Enrolled table corresponding to active student; student is prompted for course ID to be withdrawn from\n"
						+
						"S – Search: search course based on substring of course name which is given by user; list all matching courses\n"
						+
						"M – My Classes: lists all classes enrolled in by the active student.\n" +
						"X – Exit: exit application\n");
	}

	public static void listRecords(Connection conn) throws SQLException {
		String sql = "SELECT cid, cname, credits FROM Courses";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		
		boolean foundRecord = false;
		
		while (rs.next()) {
			int cid = rs.getInt("cid");
			String name = rs.getString("cname");
			int credits = rs.getInt("credits");

			System.out.println("Course ID: " + cid + " | Course Name: " + name + " | Credits: " + credits);
			foundRecord = true;
		}

		if (foundRecord) {
			System.out.println("Successfully listed all courses in records.\n");
		} else {
			System.out.println("There were no courses in records to list.\n");
		}
	}

	public static boolean isEnrolled(Connection conn, Integer sid, Integer cid) throws SQLException {
		String sql = "SELECT sid, cid FROM Enrolled WHERE sid = ? AND cid = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.clearParameters();
		pstmt.setInt(1, sid);
		pstmt.setInt(2, cid);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean courseExists(Connection conn, Integer cid) throws SQLException {
		String sql = "SELECT cid FROM Courses WHERE cid = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.clearParameters();
		pstmt.setInt(1, cid);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			return true;
		} else {
			return false;
		}
	}

	public static void enrollStudent(Connection conn, Scanner input, Integer sid) throws SQLException {
		System.out.println("Enter course id = ");
		int course_id = input.nextInt();
		input.nextLine();

		if (!courseExists(conn, course_id)) {
			System.out.println("Course does not exist for student " + sid + " to enroll in.\n");
		} else if (isEnrolled(conn, sid, course_id)) {
			System.out.println("Student is already enrolled in course " + course_id + ".\n");
		} else {
			String sql = "INSERT INTO Enrolled VALUES(?, ?)";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.clearParameters();
			pstmt.setInt(1, sid);
			pstmt.setInt(2, course_id);
			int rows = pstmt.executeUpdate();

			if (rows > 0) {
				System.out.println("Successfully enrolled student " + sid + " to course " + course_id + ".\n");
			} else {
				System.out.println("Could not enroll student " + sid + " to course " + course_id + ". Course may not exist.\n");
			}
		}

	}

	public static void withdrawStudent(Connection conn, Scanner input, Integer sid) throws SQLException {
		System.out.println("Enter course id = ");
		int course_id = input.nextInt();
		input.nextLine();

		String sql = "DELETE FROM Enrolled WHERE sid = ? AND cid = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.clearParameters();
		pstmt.setInt(1, sid);
		pstmt.setInt(2, course_id);
		
		int rows = pstmt.executeUpdate();

		if (rows > 0) {
			System.out.println("Successfully withdrawn student " + sid + " from course " + course_id + ".\n");
		} else {
			System.out.println("Student " + sid + " was not enrolled in course " + course_id + " to be withdrawn from.\n");
		}
	}

	public static void searchCourse(Connection conn, Scanner input) throws SQLException {
		System.out.println("Enter course name to search = ");
		String course_name = input.nextLine();

		String sql = "SELECT cid, cname, credits FROM Courses WHERE LOWER(cname) LIKE LOWER(?)";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.clearParameters();
		pstmt.setString(1, "%" + course_name + "%");

		ResultSet rs = pstmt.executeQuery();

		boolean foundCourse = false;
		
		while (rs.next()) {
			System.out.println("Course ID: " + rs.getInt("cid") + " | Course Name: " + rs.getString("cname")
					+ " | Credits: " + rs.getInt("credits"));
			foundCourse = true;
		}

		if (foundCourse) {
			System.out.println("Successfully listed all courses that contain " + course_name + " as a substring.\n");
		} else {
			System.out.println("Found no courses that contain " + course_name + " as a substring.\n");
		}

	}

	public static void myClasses(Connection conn, int sid) throws SQLException {
		String sql = "SELECT Courses.cid, Courses.cname, Courses.credits FROM Courses, Enrolled WHERE Enrolled.sid = ? AND Enrolled.cid = Courses.cid";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.clearParameters();
		pstmt.setInt(1, sid);

		ResultSet rs = pstmt.executeQuery();

		boolean foundClass = false;

		while (rs.next()) {
			System.out.println("Course ID: " + rs.getInt("cid") + " | Course Name: " + rs.getString("cname")
					+ " | Credits: " + rs.getInt("credits"));
			foundClass = true;
		}

		if (foundClass) {
			System.out.println("Successfully listed all courses that student " + sid + " is enrolled in.\n");
		} else {
			System.out.println("Found no courses enrolled for student " + sid + ".\n");
		}
	}

	public static Connection getConnection() {

		// first we need to load the driver
		String jdbcDriver = "oracle.jdbc.OracleDriver";
		try {
			Class.forName(jdbcDriver);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Get username and password
		Scanner input = new Scanner(System.in);
		System.out.print("Username:");
		String username = input.nextLine();
		System.out.print("Password:");
		// the following is used to mask the password
		Console console = System.console();
		String password = new String(console.readPassword());
		String connString = "jdbc:oracle:thin:@" + oracleServer + ":1521:"
				+ oracleServerSid;

		System.out.println("Connecting to the database...");

		Connection conn;
		// Connect to the database
		try {
			conn = DriverManager.getConnection(connString, username, password);
			System.out.println("Connection Successful");
		} catch (SQLException e) {
			System.out.println("Connection ERROR");
			e.printStackTrace();
			return null;
		}

		return conn;
	}
}
