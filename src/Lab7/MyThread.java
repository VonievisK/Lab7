package Lab7;

import Lab7.Commands.Commands;
import Lab7.Commands.CompareToCommand;
import Lab7.Shows.Show;
import Lab7.Shows.ShowComparator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyThread implements Runnable {

    ServerSocket serverSocket;
    Socket clientSocket;
    Date date;
    CopyOnWriteArrayList<Show> listOfShows;

    public MyThread(ServerSocket serverSocket, Socket clientSocket, CopyOnWriteArrayList<Show> listOfShows, Date date) {
        this.clientSocket = clientSocket;
        this.serverSocket = serverSocket;
        this.listOfShows = listOfShows;
        this.date = date;
    }

    @Override
    public void run() {
        InputStream inStream = null;
        try {
            inStream = clientSocket.getInputStream();
        } catch (IOException e) {
            System.out.println("Невозможно получить поток ввода");
            System.exit(-1);
        }
        //Поток вывода клиенту, отправляем ему сообщения
        //Читаем поток и отправляем ответ
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(inStream));
        String readClientStream = null;
        synchronized (listOfShows) {
            try {
                while (!serverSocket.isClosed()) {
                    OutputStream outClientStream = null;
                    outClientStream = clientSocket.getOutputStream();
                    DataOutputStream outDataClientStream = new DataOutputStream(outClientStream);
                    while ((readClientStream = reader.readLine()) != null) {
                        Date dateOfChanging = new Date();
                        //удалить первый элемент
                        if (readClientStream.equals("remove_first")) {
                            Commands.removeFirst(listOfShows, clientSocket);
                        }//информация о коллекции
                        else if (readClientStream.equals("info")) {
                            Commands.info(listOfShows, date, dateOfChanging, clientSocket);
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
