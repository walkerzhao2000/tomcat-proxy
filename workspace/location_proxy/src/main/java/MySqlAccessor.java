
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Logger;

public class MySqlAccessor implements Closeable {
    private static final Logger Log = Logger.getLogger(MySqlAccessor.class.getName());
    private Connection connect = null;

    MySqlAccessor() throws ClassNotFoundException, SQLException {
        // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        // Setup the connection with the DB
        connect = DriverManager
                .getConnection("");
    }

    @Override
    public void close() throws IOException {
        try {
            if (connect != null) {
                connect.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void addUser(String username, String passwordHex, String saltHex, String recovery, int privilege) {
        if (username.length() > 20
                || passwordHex.length() > 20 * 2 // Hex string
                || saltHex.length() > 8 * 2 // Hex string
                || recovery.length() > 40
                || privilege < 0
                || privilege > 127) {
            Log.info("addUser input parameter length: " + username.length() + "," + passwordHex.length() + "," + saltHex.length() + "," + recovery.length());
            Log.info("addUser failed (invalid parameters): username=" + username);
            return;
        }

        // insert into users (username,password,salt,recovery,create_d,access_d,privilege) values ('wjao', X'E24F................................0DFA', X'E0........15', 'wjao@myskyoo.com', NOW(), NOW(), 127);
        Statement statement = null;
        try {
            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            // Build SQL statement
            String sql = String.format("INSERT INTO users (username,password,salt,recovery,create_d,access_d,privilege) "
                    + "values ('%s', X'%s', X'%s', '%s', NOW(), NOW(), %d)",
                    username, passwordHex, saltHex, recovery, privilege);
            if (statement.executeUpdate(sql) < 1) {
                Log.info("addUser failed: username=" + username);
            } else {
                Log.info("addUser succeeded: username=" + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement, null);
        }
    }

    byte[] readCredential(final String username) {
        Statement statement = null;
        ResultSet resultSet = null;
        byte[] password = new byte[]{};
        try {
            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement.executeQuery("SELECT username, password FROM angelguardian.users WHERE username='" + username + "'");
            while (resultSet.next()) {
                password = resultSet.getBytes("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement, resultSet);
        }
        return password;
    }

    void addUserDevice(String username, String deviceidHex, String actions) {
        if (deviceidHex.length() > 6 * 2     // Hex string
                || actions.length() > 256    // JSON string
                || username.length() > 20) {
            Log.info("addUserDevice input parameter length: " + username.length() + "," + deviceidHex.length() + "," + actions.length());
            Log.info("addUserDevice failed (invalid parameters): username=" + username + ", deviceidHex=" + deviceidHex);
            return;
        }

        // insert into users (username,password,salt,recovery,create_d,access_d,privilege) values ('wjao', X'E24F................................0DFA', X'E0........15', 'wjao@myskyoo.com', NOW(), NOW(), 127);
        Statement statement = null;
        try {
            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            // Build SQL statement
            String sql = String.format("INSERT INTO user_device (username,deviceid,latest_d,actions) "
                            + "values ('%s', X'%s', NOW(), %s)",
                    username, deviceidHex, actions);
            if (statement.executeUpdate(sql) < 1) {
                Log.info("addUserDevice failed: username=" + username + ", deviceidHex=" + deviceidHex);
            } else {
                Log.info("addUserDevice succeeded: username=" + username + ", deviceidHex=" + deviceidHex);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement, null);
        }
    }

    void addBill(String username, int frequency) {
        if (frequency < 2     // max frequency per user is 2 seconds
                || username.length() > 20) {
            Log.info("addBill input parameter length: " + username.length() + "," + frequency);
            Log.info("addBill failed (invalid parameters): username=" + username);
            return;
        }

        // insert into users (username,password,salt,recovery,create_d,access_d,privilege) values ('wjao', X'E24F................................0DFA', X'E0........15', 'wjao@myskyoo.com', NOW(), NOW(), 127);
        Statement statement = null;
        try {
            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            // Run SQL query statement
            int count = 0;
            ResultSet resultSet = statement.executeQuery("SELECT count FROM bills WHERE username = " + username);
            while (resultSet.next()) {
                // It is possible to get the columns via name
                // also possible to get the columns via the column number
                // which starts at 1
                // e.g. resultSet.getString(2);
                count = Integer.parseInt(resultSet.getString("count"));
            }
            // Build SQL statement
            String sql = String.format("INSERT INTO bills (username,latest_d,freq,count) "
                            + "values ('%s', NOW(), %d, %d)",
                    username, frequency, count);
            if (statement.executeUpdate(sql) < 1) {
                Log.info("addBill failed: username=" + username);
            } else {
                Log.info("addBill succeeded: username=" + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement, null);
        }
    }

    // You need to close the resultSet
    private void closeStatement(Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }







    public void readDataBase() throws SQLException, ClassNotFoundException {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement.executeQuery("select username, password from angelguardian.users");
            writeResultSet(resultSet);

            // PreparedStatements can use variables and are more efficient
            PreparedStatement preparedStatement = connect.prepareStatement("insert into  feedback.comments values (default, ?, ?, ?, ? , ?, ?)");
            // "myuser, webpage, datum, summary, COMMENTS from feedback.comments");
            // Parameters start with 1
            preparedStatement.setString(1, "Test");
            preparedStatement.setString(2, "TestEmail");
            preparedStatement.setString(3, "TestWebpage");
            preparedStatement.setTimestamp(4, new Timestamp(2009, 12, 11, 23, 23, 23, 0));
            preparedStatement.setString(5, "TestSummary");
            preparedStatement.setString(6, "TestComment");
            preparedStatement.executeUpdate();

            preparedStatement = connect.prepareStatement("SELECT myuser, webpage, datum, summary, COMMENTS from feedback.comments");
            resultSet = preparedStatement.executeQuery();
            writeResultSet(resultSet);

            // Remove again the insert comment
            preparedStatement = connect.prepareStatement("delete from feedback.comments where myuser= ? ; ");
            preparedStatement.setString(1, "Test");
            preparedStatement.executeUpdate();

            resultSet = statement.executeQuery("select * from feedback.comments");
            writeMetaData(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeStatement(statement, resultSet);
        }

    }

    private void writeMetaData(ResultSet resultSet) throws SQLException {
        //  Now get some metadata from the database
        // Result set get the result of the SQL query

        System.out.println("The columns in the table are: ");

        System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
        for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
            System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
        }
    }

    private void writeResultSet(ResultSet resultSet) throws SQLException {
        // ResultSet is initially before the first data set
        while (resultSet.next()) {
            // It is possible to get the columns via name
            // also possible to get the columns via the column number
            // which starts at 1
            // e.g. resultSet.getSTring(2);
            String user = resultSet.getString("myuser");
            String website = resultSet.getString("webpage");
            String summary = resultSet.getString("summary");
            Timestamp date = resultSet.getTimestamp("datum");
            String comment = resultSet.getString("comments");
            System.out.println("User: " + user);
            System.out.println("Website: " + website);
            System.out.println("summary: " + summary);
            System.out.println("Date: " + date);
            System.out.println("Comment: " + comment);
        }
    }

}
