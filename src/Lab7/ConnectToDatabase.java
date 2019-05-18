package Lab7;

import java.sql.*;

public class ConnectToDatabase {
    private String URL;
    private String login;
    private String password;

    public ConnectToDatabase(String URL, String login, String password){
        this.URL = URL;
        this.login = login;
        this.password = password;
    }

    public Connection getConnection(){
        System.out.println("Проверяем, есть ли подключение к PostgreSQL JDBC");
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC не найден");
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println("PostgreSQL JDBC Driver подключен");

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(URL, login, password);
        } catch (SQLException e) {
            System.out.println("Не удалось соединиться");
            e.printStackTrace();
            System.exit(0);
        }

        if (connection != null) {
            System.out.println("Успешное подключение к базе данных");
            return connection;
        } else {
            System.out.println("Не удалось подключиться к базе данных");
            return null;
        }
    }
}