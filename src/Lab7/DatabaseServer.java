package Lab7;

import Lab7.Commands.Commands;
import Lab7.Commands.ConnectToDatabase;
import Lab7.Commands.DatabaseCommands;
import Lab7.Commands.PasswordGenerator;
import Lab7.Shows.Show;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.sql.Connection;

public class DatabaseServer {
    public static void main(String[] args) {
        /**
         * подключаемся к базе данных
         * для гелиоса следующие параметры:
         * String URL = "jdbc:postgresql://pg/studs"; jdbc:postgresql://localhost:5433/studs"
         * String userLogin = "s265949"; postgres
         * String userPassword = "coj266";
         * @database - переменная для обращения к базе данных и получение из нее данных
         */
        String URL = "jdbc:postgresql://localhost:5433/studs";
        String userLogin = "postgres";
        String userPassword = "coj266";
        Connection database = new ConnectToDatabase(URL, userLogin, userPassword).getConnection();

        /**
         * импорт или создание базы данных
         */
        CopyOnWriteArrayList<Show> listOfShows = new CopyOnWriteArrayList<>();
        HashMap<String, String> Users = new HashMap<>(); // список пользоваталей, храним в хэшмапе, чтобы был доступ
                                                         // к пароль по логину
        BufferedReader serverCommandReader = new BufferedReader(new InputStreamReader(System.in));
        String serverCommand;
        System.out.println("Создать базу данных или использовать готовую?(Create/Start)");
        try {
            while ((serverCommand = serverCommandReader.readLine()) != null) {
                if (serverCommand.equals("Create")) {
                    /**
                     * создание таблиц, если их не существует, с помощью кода SQL
                     * сначала проверяем, существуют ли наши таблицы
                     */
                    ResultSet data = database.createStatement().executeQuery("select * from " +
                            "INFORMATION_SCHEMA.TABLES where table_name = 'Shows'");
                    if (data.next()) {
                        System.err.println("таблица Shows уже существует");
                        DatabaseCommands.ImportDatabase(database, listOfShows);
                    }
                    else {
                        database.createStatement().executeUpdate(
                                "create table if not exists \"Shows\" (\n" +
                                        "\"NAME\" varchar not null,\n" +
                                        "\"THEME\" varchar not null,\n" +
                                        "\"RATING\" varchar not null,\n" +
                                        "\"PLACE\" varchar not null,\n" +
                                        "\"DATEOFCREATION\" varchar not null,\n" +
                                        "\"CREATOR\" varchar not null)"
                        );
                        System.out.println("Успешно создана таблица Shows");
                    }
                    data = database.createStatement().executeQuery("select * from " +
                            "INFORMATION_SCHEMA.TABLES where table_name = 'Users'");
                    if (data.next()){
                        System.err.println("таблица Users уже существует");
                        Users = DatabaseCommands.importUsers(database);
                    }
                    else {
                        database.createStatement().executeUpdate(
                                "create table if not exists \"Users\" (\n" +
                                        "\"LOGIN\" varchar not null unique,\n" +
                                        "\"PASSWORD\" varchar not null)"
                        );
                        System.out.println("Успешно создана таблица Users");
                    }
                    System.out.println("База данных успешно загружена");
                    break;
                } else if (serverCommand.equals("Start")) {
                    /**
                     * заполнение коллекции исходя из таблиц в базе данных
                     */
                    DatabaseCommands.ImportDatabase(database, listOfShows);
                    break;
                } else {
                    System.out.println("Введена неверная комманда");
                    System.out.println("Создать базу данных или использовать готовую?(Create/Start)");
                }
            }
            System.out.println("Сканируем список пользователей");
            /**
             * загрузка списка пользователей
             */
            Users = DatabaseCommands.importUsers(database);
            System.out.println("Список пользователей успешно загружен");
        } catch (IOException e) {
            System.err.println("Что-то пошло не так при вводе команды");
        } catch (SQLException e) {
            System.err.println("Возникла проблема при взаимодейтсвии с базой данных");
            e.printStackTrace();
        }

        /**
         * создание сокета сервера
         */
        int port = 7769;
        //Проверяем доступность порта
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Порт: " + port + " - ошибка подключения");
            System.exit(-1);
        }
        /**
         * На случай, если все крашнулось и надо, чтобы изменения сохранились
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                DatabaseCommands.UploadShows(database, listOfShows);
            }
        });

        /**
         * создание клиента
         */
        Socket clientSocket = null;
        while (!serverSocket.isClosed()) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Порт: " + port + " - ошибка подключения");
                System.exit(-1);
            }
            /**
             * запускаем для клиента отдельный поток, в котором он будет работать
             */
            MyThread myThread = new MyThread(serverSocket, clientSocket, listOfShows, database, Users);
            Thread thread = new Thread(myThread);
            thread.start();
        }
    }
}
