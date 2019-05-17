package Lab7;

import Lab7.Commands.Commands;
import Lab7.Commands.CompareToCommand;
import Lab7.Shows.Show;
import Lab7.Shows.ShowComparator;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * для каждого клиента будет запущен отдельный поток
 */
public class MyThread implements Runnable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private CopyOnWriteArrayList<Show> listOfShows;
    private Connection database = null;
    private HashMap<String, String> Users;

    public MyThread(ServerSocket serverSocket, Socket clientSocket, CopyOnWriteArrayList<Show> listOfShows,
                    Connection database, HashMap<String, String> Users) {
        this.clientSocket = clientSocket;
        this.serverSocket = serverSocket;
        this.listOfShows = listOfShows;
        this.database = database;
        this.Users = Users;
    }

    @Override
    public void run() {
        System.out.println("Пользователь подключился");
        /**
         * получаем поток ввода от клиента
         */
        InputStream inStream = null;
        try {
            inStream = clientSocket.getInputStream();
        } catch (IOException e) {
            System.out.println("Невозможно получить поток ввода");
            System.exit(-1);
        }
        /**
         * Поток вывода клиенту, отправляем ему сообщения
         * Читаем поток и отправляем ответ
         */
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(inStream));
        String readClientStream = null;
        /**
         * авторизируем пользователя
         * key1 - пользователь выбирает либо залогиниться, либо зарегестрироваться
         * если он выбрал что-то из этого, далее он попадает в key2
         * key2 - пользователь вводит либо почту для входа, либо почту для получения письма с паролем.
         * так же он может вернуться на выбор регистрации или логина с помощью комманды back
         */
        boolean key1 = true;
        boolean key2 = false;
        String userChoice = null;
        try {
            while ((readClientStream = reader.readLine()) != null){
                while (key1) {
                    if (readClientStream.equals("Login")) {
                        Commands.sendMessageToClient("Введите логин", clientSocket);
                        key2 = true;
                        key1 = false;
                        userChoice = "Login";
                        break;
                    } else if (readClientStream.equals("Register")) {
                        Commands.sendMessageToClient("Укажите почту для регистрации. На нее придет пароль",
                                clientSocket);
                        key1 = false;
                        key2 = true;
                        userChoice = "Register";
                        break;
                    } else {
                        System.out.println(readClientStream);
                        Commands.sendMessageToClient("Введена неверна команда. " +
                                "Попробуйте еще раз", clientSocket);
                    }
                }
                while (key2) {
                    switch (userChoice) {
                        case "Login":
                            if (Users.get(readClientStream) == null) {
                                Commands.sendMessageToClient("Нет пользователя с таким логином " +
                                        "попробуйте еще раз" + "\n" + "Вы так же можете" +
                                        "вернуться к выбору Login/Passwords с помощью комманды back", clientSocket);
                                userChoice = "";
                                if (readClientStream.equals("back")){
                                    key1 = true;
                                    Commands.sendMessageToClient("Войти или зарегестрироваться?" +
                                            " (Login/Register)", clientSocket);
                                    break;
                                }
                            }
                            break;
                        case "Register":
                            Commands.sendMessageToClient("Введите свою почту", clientSocket);
                            if (readClientStream.equals("back")){
                                key1 = true;
                                Commands.sendMessageToClient("Войти или зарегестрироваться?" +
                                        " (Login/Register)", clientSocket);
                                break;
                            }
                            break;
                        default:
                            Commands.sendMessageToClient("Введена неверна команда." +
                                    "Попробуйте еще раз", clientSocket);
                            if (readClientStream.equals("back")){
                                key1 = true;
                                Commands.sendMessageToClient("Войти или зарегестрироваться?" +
                                        " (Login/Register)", clientSocket);
                                break;
                            }
                    }
                }
            }
        } catch (IOException e) {
        }
        synchronized (listOfShows) {
            try {
                while (!serverSocket.isClosed()) {
                    OutputStream outClientStream = null;
                    outClientStream = clientSocket.getOutputStream();
                    DataOutputStream outDataClientStream = new DataOutputStream(outClientStream);
                    while ((readClientStream = reader.readLine()) != null) {

                        //удалить первый элемент
                        if (readClientStream.equals("remove_first")) {
                            Commands.removeFirst(listOfShows, clientSocket);
                        }//информация о коллекции
                        else if (readClientStream.equals("info")) {
                            Commands.info(listOfShows, clientSocket);
                        } //вывести лист
                        else if (readClientStream.equals("show")) {
                            Commands.show(listOfShows, clientSocket);
                        } //сортировка по рейтингу
                        else if (readClientStream.equals("sort_by_rating")) {
                            listOfShows = Show.sortShowsBy(ShowComparator.Order.Rating, listOfShows, clientSocket);
                        } //сортировка по теме
                        else if (readClientStream.equals("sort_by_theme")) {
                            listOfShows = Show.sortShowsBy(ShowComparator.Order.Theme, listOfShows, clientSocket);
                        } //реализация удаления элемента по номеру, большего или меньшего чем данный
                        else if (CompareToCommand.compareRemove(readClientStream, clientSocket)) {
                            Commands.remove(readClientStream, listOfShows, clientSocket);
                        } //добавляем объект в нашу коллекцию
                        else if (CompareToCommand.compareAdd(readClientStream)) {
                            if (Commands.addElement(readClientStream, listOfShows)) {
                                Commands.sendMessageToClient("Успешно добавлен элемент в коллецию", clientSocket);
                            } else {
                                Commands.sendMessageToClient("Введена неверная комманда", clientSocket);
                            }
                        } //остановить программу
                        else if (readClientStream.equals("stop")) {
                            Commands.sendMessageToClient("Вы завершили работу. Идите с богом.", clientSocket);
                            clientSocket.close();
                            clientSocket = null;
                        } else {
                            Commands.sendMessageToClient("                                                   ▄████████▄ \n" +
                                    "                                                  ███████████ \n" +
                                    "                                                 ████████████ \n" +
                                    "                                                █████████████ \n" +
                                    "                                              ██████████████ \n" +
                                    "                                              ██▒▒▒▒▒▒▒▒▒▒▒██ \n" +
                                    "                                             ██▒▒▒▒▒▒▒▒▒▒▒▒▒▒██ \n" +
                                    " $$$$$$    $$   $$    $$  $$  $$    $$   $$  ██▒████▒▒████▒▒▒▒██ \n" +
                                    " $$  $$    $$  $$$    $$  $$  $$    $$  $$$  █▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒██ \n" +
                                    " $$  $$    $$ $ $$    $$  $$  $$    $$ $ $$  █▒       ▒▒      ▒██ \n" +
                                    " $$  $$    $$$  $$    $$  $$  $$    $$$  $$  ████    ▒▒██     ▒██ \n" +
                                    " $$  $$    $$   $$    $$$$$$$$$$    $$   $$  █▒       ▒▒      ▒██ \n" +
                                    "                                            █▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒██ \n" +
                                    "                                          ██▒▒▒████████▒▒▒▒▒▒▒██ \n" +
                                    "                                         ██▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒███ \n" +
                                    "                                       ██▒▒██▒▒▒▒▒▒▒▒▒▒▒▒██▒▒▒██ \n" +
                                    "                                     ██▒▒▒▒██▒▒▒▒▒▒▒▒▒▒▒██▒▒▒▒██ \n" +
                                    "                                    █▒▒▒▒██▒▒▒▒▒▒▒▒▒▒▒▒▒▒██▒▒▒▒█ \n" +
                                    "                                    █▒▒▒▒██▒▒▒▒▒▒▒▒▒▒▒▒▒▒██▒▒▒▒█ \n" +
                                    "                                    █▒▒████▒▒▒▒▒▒▒▒▒▒▒▒▒▒████▒▒▒█ \n" +
                                    "                                    ▀████▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒████▀", clientSocket);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Какая-то плохая ошибка: " + e);
            }
        }
    }
}
