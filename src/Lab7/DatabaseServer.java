package Lab7;

import Lab7.Commands.Commands;
import Lab7.Commands.PasswordGenerator;
import Lab7.Shows.Show;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.sql.Connection;

public class DatabaseServer {
    public static void main(String[] args) {
        /**
         * подключаемся к базе данных в качестве одмена
         */
        String URL = "jdbc:postgresql://localhost:5433/studs";
        String userLogin = "postgres";
        String userPassword = "admin";
        ConnectToDatabase newConnection = new ConnectToDatabase(URL, userLogin, userPassword);
        newConnection.getConnection();
        /**
         * РОСТИК, при хранении это надо хэшировать
         */
        PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
                .useLower(true)
                .useUpper(true)
                .useDigits(true)
                .build();
        String password = passwordGenerator.generate(6);
        System.out.println(password);
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