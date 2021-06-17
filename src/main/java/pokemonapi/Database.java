package pokemonapi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

    Connection connection;

//    public Database() throws SQLException, ClassNotFoundException {
//        Class.forName("org.sqlite.JDBC");
//        String urlToDB = "jdbc:sqlite:data/counter.sqlite3";
//        this.connection = DriverManager.getConnection(urlToDB);
//        PreparedStatement prep = connection.prepareStatement("CREATE TABLE IF NOT EXISTS counter ("
//                + "cnt INTEGER,"
//                + "id STRING,"
//                + "PRIMARY KEY (id));");
//        prep.executeUpdate();
//        prep.close();
//
//
//        PreparedStatement checkEntries = connection.prepareStatement("SELECT COUNT(*) FROM counter;");
//        ResultSet rs = checkEntries.executeQuery();
//        int entries =-1;
//        while (rs.next()) {
//            entries = rs.getInt(1);
//        }
//        checkEntries.close();
//        rs.close();
//
//        if (entries == 0) {
//            PreparedStatement addCount = connection.prepareStatement("INSERT INTO counter (cnt, id) "
//                + "VALUES (0, 'test');");
//            addCount.executeUpdate();
//            addCount.close();
//        } else if (entries != 1) {
//            throw new SQLException("Number of entries in counter is not 0 or 1.");
//        }
//    }

}
