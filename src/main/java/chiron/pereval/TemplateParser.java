package main.java.chiron.pereval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateParser {
    public static final String TIMESTAMP = "<name>Timestamp</name><value>\\d+</value>";
    public static final String UTCTIME = "<name>UTC Time</name>";
    public static final String PID = "<name>pid</name><value>\\d+</value>";
    public static final String JDBC = "jdbc:postgresql://127.0.0.1:5433/base-scc2";
    public static final String DB_USER = "postgres";
    public static final String DB_PASS = "postgres";
    public static final String TEMPLATE_PATH = "resources/tau-template-supernode.txt";
    public static final String OUTPUT_PATH = "resources/output-test.txt";
    public static final String CALL = "1";
    public static final String SUBCALL = "0";
    public static HashMap<String,String> methodNameMap = new HashMap<String, String>();
    public static HashMap<String,String> methodClassMap = new HashMap<String, String>();


    public static void main(String[] args) {

        Connection connection = null;
        try {
            connection = init();
            if (connection == null) throw new Exception();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            StringBuffer buffer = new StringBuffer();
            BufferedReader br = new BufferedReader(new FileReader(TEMPLATE_PATH));

            String line;
            Pattern timestampPat = Pattern.compile(TIMESTAMP);
            Pattern pidPat = Pattern.compile(PID);
            while ( (line = br.readLine()) != null) {
                boolean read = false;
                Matcher timestampMatcher = timestampPat.matcher(line);
                Matcher pidMatcher = pidPat.matcher(line);
                // check all occurance
                while (timestampMatcher.find()) {
                    System.out.println(timestampMatcher.group());
                }
                while (pidMatcher.find()) {
                    System.out.println(pidMatcher.group());
                }
                if(!read){
                    buffer.append(line);
                }
            }
            br.close();

            PreparedStatement pst = null;
            ResultSet rs = null;
            pst = connection.prepareStatement("SELECT metric,etime,provfunction FROM eperfeval");
            rs = pst.executeQuery();

            while (rs.next()) {
                //"method [{filepath} {,}]" calls subcalls exclusivetime inclusivetime 0 GROUP="groupname"
                String row = "\""+rs.getString(3) + " [" + methodClassMap.get(rs.getString(1)) + "]\"" + " " + CALL + " " + SUBCALL
                        +" " +rs.getInt(2) + " " + rs.getInt(2) + " 0 GROUP=\"" + methodNameMap.get(rs.getString(1)) + "\"";
                buffer.append("\n"+row);
            }
            System.out.println(buffer.toString());
            BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PATH));
            bw.append(buffer.toString());
            bw.flush();
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static Connection init()throws ClassNotFoundException,SQLException{


        Class.forName("org.postgresql.Driver");

        Connection connection = null;
        connection = DriverManager.getConnection(
                JDBC, DB_USER,
                DB_PASS);

        String prov = "TPROV";
        String comm = "TCOMM";
        String comp = "TCOMP";

        methodClassMap.put(prov,"{EProvenance.java}{1,0}");
        methodClassMap.put(comm,"{EBody.java}{1,0}");
        methodClassMap.put(comp,"{EHead.java}{1,0}");

        methodNameMap.put(prov,"Provenance");
        methodNameMap.put(comm,"Communication");
        methodNameMap.put(comp,"Computation");

        return connection;
    }
}
