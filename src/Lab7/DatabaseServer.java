package Lab7;

import Lab7.Commands.Commands;
import Lab7.Commands.MakeStringIntoTheme;
import Lab7.Commands.PasswordGenerator;
import Lab7.Shows.DancingShow;
import Lab7.Shows.Show;
import Lab7.Shows.ThemesList;

import javax.jws.soap.SOAPBinding;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.sql.Connection;

public class DatabaseServer {
    public static void main(String[] args) {
        /**
         * подключаемся к базе данных
         * для гелиоса следующие параметры:
         * String URL = "jdbc:postgresql://pg";
         * String userLogin = "s265949";
         * String userPassword = "coj266";
         * @database - переменная для обращения к базе данных и получение из нее данных
         */
        String URL = "jdbc:postgresql://localhost:5433/studs";
        String userLogin = "postgres";
        String userPassword = "";
        Connection database = new ConnectToDatabase(URL, userLogin, userPassword).getConnection();

        /**
         * импорт или создание базы данных
         */
        CopyOnWriteArrayList<Show> listOfShows = new CopyOnWriteArrayList<>();
        HashMap<String, String> Users = new HashMap<>(); // список пользоваталей
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
                    if (data.next()) System.err.println("таблица Shows уже существует");
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
                    if (data.next()) System.err.println("таблица Users уже существует");
                    else {
                        database.createStatement().executeUpdate(
                                "create table if not exists \"Users\" (\n" +
                                        "\"LOGIN\" varchar not null unique,\n" +
                                        "\"PASSWORD\" varchar not null)"
                        );
                        System.out.println("Успешно создана таблица Users");
                    }
                    break;
                } else if (serverCommand.equals("Start")) {
                    /**
                     * заполнение коллекции исходя из таблиц в базе данных
                     */
                    ResultSet data = database.createStatement().executeQuery("select * from \"Shows\""
                    );
                    String name;
                    ThemesList theme;
                    int rating;
                    String place;
                    LocalDateTime dateOfCreation;
                    String creator;
                    while (data.next()) {
                        name = data.getString("NAME");
                        theme = MakeStringIntoTheme.stringIntoTheme(data.getString("THEME"));
                        rating = data.getInt("RATING");
                        place = data.getString("PLACE");
                        dateOfCreation = LocalDateTime.parse(data.getString("DATEOFCREATION"));
                        creator = data.getString("CREATOR");
                        if (ThemesList.DANCING.equals(theme)) {
                            listOfShows.add(new DancingShow(name, rating, theme, place, creator, dateOfCreation));
                        } else if (ThemesList.HUMOR.equals(theme)) {
                            //тут должно быть не dancing show, а humor show
                            listOfShows.add(new DancingShow(name, rating, theme, place, creator, dateOfCreation));
                        } else if (ThemesList.NEWS.equals(theme)) {
                            //тут должно быть не dancing show, а news show
                            listOfShows.add(new DancingShow(name, rating, theme, place, creator, dateOfCreation));
                        } else if (ThemesList.SPACE.equals(theme)) {
                            //тут должно быть не dancing show, а space show
                            listOfShows.add(new DancingShow(name, rating, theme, place, creator, dateOfCreation));
                        } else {
                            //тут должно быть шоу без темы
                            listOfShows.add(new DancingShow(name, rating, theme, place, creator));
                        }
                    }
                    System.out.println("База данных успешно загружена");
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
            Users = Commands.updateUsersList(database);
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
        int port = 7767;
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
            Thread thread = new Thread(new MyThread(serverSocket, clientSocket, listOfShows, database, Users));
            thread.start();
        }
    }
}
    /*public static void main(String[] args){
        Date date = new Date();
        CopyOnWriteArrayList<Show> listOfShows = Commands.makeLinkedListFromFile();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                System.out.println("Сервер Остановлен");
                ///home/s265949/Lab4/infoAboutShows.txt
                ///Users/vonievisk/Library/Mobile Documents/com~apple~CloudDocs/Учеба/Java/lab3-5/src/Lab4/finalList.txt
                try (FileWriter writer = new FileWriter("/Users/vonievisk/Library/Mobile Documents/com~apple~CloudDocs/Учеба/Java/lab 7/src/Lab7/Files/infoAboutShows.txt", false)) {
                    for (int i = 0; i < listOfShows.size(); i++) {
                        writer.write(listOfShows.get(i).getRating() + ", ");
                        writer.write(listOfShows.get(i).getTheme().toString() + ", ");
                        writer.write(listOfShows.get(i).getPlace());
                        writer.write("\n");
                    }
                    writer.flush();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        });

        int port = 7767;

        //Проверяем доступность порта
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Порт: " + port + " - ошибка подключения");
            System.exit(-1);
        }

        //Создание клиента
        Socket clientSocket = null;

        while(!serverSocket.isClosed()){
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Порт: " + port + " - ошибка подключения");
                System.exit(-1);
            }

            MyThread myThread = new MyThread(serverSocket, clientSocket, listOfShows, date);
            Thread thread = new Thread(myThread);
            thread.start();
        }
    }
}

*/