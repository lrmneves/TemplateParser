package main.java.chiron.pereval;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateParser {
    public static final String TIMESTAMP = "<name>Timestamp</name><value>\\d+</value>";
    public static final String UTCTIME = "<name>UTC Time</name>";
    public static final String PID = "<name>pid</name><value>\\d+</value>";
    public static void main(String[] args) {
        try {

            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your PostgreSQL JDBC Driver? "
                    + "Include in your library path!");
            e.printStackTrace();
            return;

        }
        System.out.println("PostgreSQL JDBC Driver Registered!");
        Connection connection = null;

        try {

            connection = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5433/postgres", "postgres",
                    "postgres");

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;

        }

        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }

        try{
            BufferedReader br = new BufferedReader(new FileReader("resources/tau-template-supernode.txt"));

            String line;
            Pattern timestampPat = Pattern.compile(TIMESTAMP);
            Pattern pidPat = Pattern.compile(PID);
            while ( (line = br.readLine()) != null) {
                Matcher timestampMatcher = timestampPat.matcher(line);
                Matcher pidMatcher = pidPat.matcher(line);

                // check all occurance
                while (timestampMatcher.find()) {
                    System.out.println(timestampMatcher.group());
                }
                while (pidMatcher.find()) {
                    System.out.println(pidMatcher.group());
                }
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
