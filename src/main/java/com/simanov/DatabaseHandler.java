package com.simanov;
import java.sql.*;

public class DatabaseHandler extends Configs{
    Connection dbConnection;

    public Connection getDbConnection() throws ClassNotFoundException, SQLException{
        String connectionString = "jdbc:postgresql://" + dbHost + ":"
                + dbPort + "/" + dbName;
        Class.forName("org.postgresql.Driver");
        dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPass);
        System.out.println("getDbConnection done");
        return dbConnection;
    }

    public void selectQuery(Connection connection) throws SQLException, ClassNotFoundException {
        String select = "SELECT * FROM " + Const.USER_TABLE + ";";
        PreparedStatement prSt = connection.prepareStatement(select);
        ResultSet resultSet = prSt.executeQuery();
        while(resultSet.next()){
            System.out.println(resultSet.getString(Const.USER_NAME));
        }
    }
}
