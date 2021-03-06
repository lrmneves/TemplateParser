package main.java.chiron.pereval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateParser {
    public static final String TIMESTAMP = "<name>Timestamp</name><value>\\d+</value>";
    public static final String UTCTIME = "<name>UTC Time</name>";
    public static final String PID = "<name>pid</name><value>\\d+</value>";
    public static final String JDBC = "jdbc:postgresql://127.0.0.1:5433/base-scc2";
    public static final String DB_USER = "postgres";
    public static final String DB_PASS = "postgres";
    public static final String SUPER_TEMPLATE_PATH = "resources/tau-template-supernode.txt";
    public static final String NODE_TEMPLATE_PATH = "resources/tau-template.txt";
    public static final String SUPERNODE_OUTPUT_PATH = "output/superNodeOutput.txt";
    public static final String NODE_OUTPUT_PATH = "output/outputNode";
    public static final String CALL = "1";
    public static final String SUBCALL = "0";
    public static HashMap<String,String> methodNameMap = new HashMap<String, String>();
    public static HashMap<String,String> methodClassMap = new HashMap<String, String>();
    public static LinkedHashMap<Integer,StringBuilder> bufferMap= new LinkedHashMap<Integer,StringBuilder>();

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
            StringBuilder superNode = new StringBuilder();
            BufferedReader superBr = new BufferedReader(new FileReader(SUPER_TEMPLATE_PATH));
            String line;
            while ( (line = superBr.readLine()) != null) {
//                boolean read = false;
//                Matcher timestampMatcher = timestampPat.matcher(line);
//                Matcher pidMatcher = pidPat.matcher(line);
//                // check all occurance
//                while (timestampMatcher.find()) {
//                    System.out.println(timestampMatcher.group());
//                }
//                while (pidMatcher.find()) {
//                    System.out.println(pidMatcher.group());
//                }
//                if(!read){
                    superNode.append(line);
//                }
            }
            superBr.close();

            StringBuilder node = new StringBuilder();
            BufferedReader nodeBr = new BufferedReader(new FileReader(NODE_TEMPLATE_PATH));
            while((line = nodeBr.readLine()) != null){
                node.append(line);
            }
            nodeBr.close();

            PreparedStatement pst = null;
            ResultSet rs = null;
            pst = connection.prepareStatement("SELECT metric,etime,provfunction,machineid FROM eperfeval");
            rs = pst.executeQuery();
            while (rs.next()) {
                if(!bufferMap.containsKey(rs.getInt(4))){
                    bufferMap.put(rs.getInt(4),new StringBuilder());
                }
                //"method [{filepath} {,}]" calls subcalls exclusivetime inclusivetime 0 GROUP="groupname"
                String row = "\""+ (rs.getString(3) != null?rs.getString(3)+" ":"") + "[" + methodClassMap.get(rs.getString(1)) + "]\"" + " " + CALL + " " + SUBCALL
                        +" " +rs.getInt(2) + " " + rs.getInt(2) + " 0 GROUP=\"" + methodNameMap.get(rs.getString(1)) + "\"";
                bufferMap.get(rs.getInt(4)).append("\n" + row);
            }

            boolean supernode = true;

            for(int k : bufferMap.keySet()){
                if(supernode){
                    BufferedWriter bw = new BufferedWriter(new FileWriter(SUPERNODE_OUTPUT_PATH));
                    bw.append(superNode.toString() + bufferMap.get(k).toString());
                    bw.flush();
                    bw.close();
                    supernode = false;
                }
                else{
                    BufferedWriter bw = new BufferedWriter(new FileWriter(NODE_OUTPUT_PATH+k+".txt"));
                    bw.append(node.toString() + bufferMap.get(k).toString());
                    bw.flush();
                    bw.close();
                }
            }
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
