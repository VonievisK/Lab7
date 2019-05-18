package Lab7.Commands;

import Lab7.Shows.Show;

import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DatabaseCommands {
    /**
     * добавляем пользователей в список пользователей на сревре из таблицы в базе данных
     * отдельный список пользователей на сервере в коллекции HashMap (ключ логин, пароль) нужен для проверки
     * авторизации пользователя
     * @param database база данны, из которой мы будем выгружать список пользователей
     * @return обновленный список пользователей HashMap
     */
    public static HashMap<String, String> importUsers(Connection database){
        HashMap<String, String> Users = new HashMap<>();
        try {
            ResultSet data = database.createStatement().executeQuery("select * from \"Users\"");
            String login;
            String password;
            while (data.next()) {
                login = data.getString("LOGIN");
                password = data.getString("PASSWORD");
                Users.put(login, password);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return Users;
    }

    /**
     * хэширование пароля
     * @param st строка, которую будем хэшировать
     * @return хэшированная строка
     */
    public static String MD5hash(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // ошибка возникает, если передаваемый алгоритм в getInstance(...) не существует
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }
        return md5Hex;
    }

    /**
     * метод для добавления шоу в базу данных
     * @param database база, в которую будем загружать
     * @param listOfShows коллекция наших шоу
     */
    public static void UploadShows(Connection database, CopyOnWriteArrayList<Show> listOfShows, Socket clientSocket){
        try {
            database.createStatement().executeUpdate("delete from \"Shows\"");
        } catch (SQLException e){
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < listOfShows.size(); i++) {
                PreparedStatement pstmt = database.prepareStatement("insert into " +
                        "\"Shows\"(\"NAME\", \"THEME\", \"RATING\", \"PLACE\", \"DATEOFCREATION\", \"CREATOR\")" +
                        " values (?, ?, ?, ?, ?, ?)");
                pstmt.setString(1, listOfShows.get(i).getName());
                pstmt.setString(2, listOfShows.get(i).getTheme().toString());
                pstmt.setString(3, Integer.toString(listOfShows.get(i).getRating()));
                pstmt.setString(4, listOfShows.get(i).getPlace());
                pstmt.setString(5, listOfShows.get(i).getData().toString());
                pstmt.setString(6, listOfShows.get(i).getCreator());
                pstmt.executeUpdate();
            }
            Commands.sendMessageToClient("Коллекция успешно загружена в базу данных", clientSocket);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}