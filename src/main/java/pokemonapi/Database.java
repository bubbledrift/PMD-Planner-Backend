package pokemonapi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

    Connection connection;

    public Database() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String urlToDB = "jdbc:sqlite:data/RTDXPopularity.sqlite3";
        this.connection = DriverManager.getConnection(urlToDB);
    }

}
