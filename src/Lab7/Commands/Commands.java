package Lab7.Commands;

import Lab7.Shows.DancingShow;
import Lab7.Shows.Show;
import Lab7.Shows.ThemesList;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Commands{
    /**
     * удаляет первый элемент
     * @param listOfShows коллекция, из которой удаляем элемент
     * @param clientSocket клиент, которому отправляем результат выполнения
     * @param userName имя пользователя, который хочет взаимодействовать с объектом
     */
    public static void removeFirst(CopyOnWriteArrayList<Show> listOfShows, Socket clientSocket, String userName) {
        if (listOfShows.get(0).getCreator().equals(userName)) {
            listOfShows.remove(0);
            sendMessageToClient("Успешно удален первый элемент из коллекции", clientSocket);
        } else {
            sendMessageToClient("Не удалось удалить элемент. Нет доступа к элементу", clientSocket);
        }
    }

    /**
     * Костыль для парсинга
     * считаем кол-во кавычек в строке
     * @param a - строка
     * @return кол-во кавычек
     */
    public static int countK(String a){
        int summ = 0;
        char[] symbolArray = a.toCharArray();
        for(int i = 0; i < a.length(); i++){
            if ('\"' == symbolArray[i]) {
                summ++;
            }
        }
        return summ;
    }
    /**
     * выводит информацию о коллекции
     * @param listOfShows    коллекция объектов класса Show
     */
    public static void info(CopyOnWriteArrayList<Show> listOfShows, Socket clientSocket) {
        String allInfo = "Тип коллекции: " + listOfShows.getClass() + "\n" + "Кол-во элементов: " + listOfShows.size();
        Commands.sendMessageToClient(allInfo, clientSocket);
    }

    /**
     * вывести на экран нашу коллекцию
     * @param listOfShows коллекция объектов класса Show
     */
    public static void show(CopyOnWriteArrayList<Show> listOfShows, Socket clientSocket) {
        StringBuilder message = new StringBuilder();
        /*for (int i = 0; i < listOfShows.size(); i++) {
            message += listOfShows.get(i).toString();
        }*/
        listOfShows.stream().forEach(show -> message.append(show.toString()));
        Commands.sendMessageToClient(message.toString(), clientSocket);
    }

    /**
     * удаляет элемент по индексу, либо больший(меньший) данного индекса
     * @param command строка пользователя, из нее получаем индекс
     * @param userName имя пользователя, проверяем, может ли он удалить элемент
     * @param listOfShows коллекция наших элементов
     * @param clientSocket сокет клиента, которму отправляем результат
     */
    public static void remove(String command, String userName,
                              CopyOnWriteArrayList<Show> listOfShows, Socket clientSocket) {
        //кол-во неудаленных элементов для remove_greater и remove_lower
        int amountOfNonDeletedElements = 0;
        /**
         * выполняем комманду remove
         */
        if (command.startsWith("remove {")) {
            int numberOfElement;
            String[] keyAndValue = CompareToCommand.readJSON(command);
            //получаем число, которое содержится в фигурных скобках
            numberOfElement = Integer.parseInt(keyAndValue[1].substring(0, keyAndValue[1].length() - 1));
            if (numberOfElement > listOfShows.size() - 1) {
                Commands.sendMessageToClient("Такого элемента не существует", clientSocket);
            } else if (!listOfShows.get(numberOfElement).getCreator().equals(userName)){
                Commands.sendMessageToClient("Нет доступа к данному элементу", clientSocket);
            } else {
                listOfShows.remove(numberOfElement);
                Commands.sendMessageToClient("Успешно удален " + numberOfElement + " элемент из коллекции", clientSocket);
            }
        }
        /**
         * выполяем комманду remove_greater
         */
        else {
            int numberOfElementGreaterLower;
            numberOfElementGreaterLower = Integer.parseInt(command.replaceAll("\\D+", ""));
            if (command.startsWith("remove_g")) {
                if (numberOfElementGreaterLower > listOfShows.size() - 1) {
                    Commands.sendMessageToClient("Такого элемента не существует", clientSocket);
                } else if (numberOfElementGreaterLower == listOfShows.size() - 1) {
                    Commands.sendMessageToClient("Элементов, больших данного, не существует", clientSocket);
                } else {
                    for (int i = listOfShows.size() - 1; i > numberOfElementGreaterLower; i--) {
                        if (listOfShows.get(i).getCreator().equals(userName)) {
                            listOfShows.remove(i);
                        } else {
                            amountOfNonDeletedElements++;
                        }
                    }
                    if (amountOfNonDeletedElements == 0) {
                        Commands.sendMessageToClient("Успешно удалены элементы, большие чем "
                                + numberOfElementGreaterLower, clientSocket);
                    } else {
                        Commands.sendMessageToClient("Удалены не все элементы. Это связано с доступом к ним." +
                                " Колличество не удаленных " +
                                "элементов: " + amountOfNonDeletedElements, clientSocket);
                    }
                }
            }
            /**
             * выполянем комманду remove_lower
             */
            else {
                if (numberOfElementGreaterLower > listOfShows.size() - 1) {
                    Commands.sendMessageToClient("Такого элемента не существует", clientSocket);
                } else if (numberOfElementGreaterLower == 0) {
                    Commands.sendMessageToClient("Элементов, меньших данного, не существует", clientSocket);
                } else {
                    for (int i = 0; i < numberOfElementGreaterLower; i++) {
                        if (listOfShows.get(i).getCreator().equals(userName)) {
                            listOfShows.remove(0);
                        } else {
                            amountOfNonDeletedElements++;
                        }
                    }
                    if (amountOfNonDeletedElements == 0) {
                        Commands.sendMessageToClient("Успешно удалены элементы, меньшие чем "
                                + numberOfElementGreaterLower, clientSocket);
                    } else {
                        Commands.sendMessageToClient("Удалены не все элементы. Это связано с доступом к ним." +
                                " Колличество не удаленных " +
                                "элементов: " + amountOfNonDeletedElements, clientSocket);
                    }
                }
            }
        }
    }

    /**
     * добавить элемент в нашу коллекцию. Сначала делаем массив строк, из которого получаем имя шоу и его рейтинг.
     * после создаем шоу и добавляем его в коллекцию
     * @param command комманда пользователя
     * @param listOfShows коллекция, в которую мы добавляем шоу
     * @param creator создатель объекта
     * @return
     */
    public static boolean addElement(String command, CopyOnWriteArrayList<Show> listOfShows, String creator) {
        String name = null;
        int rating = 0;
        ThemesList theme = null;
        String place = null;
        String[] nameAndRating = CompareToCommand.readJSON(command.substring(5, command.length() - 1));
        for (int i = 0; i < 8; i++) {
            // System.err.println(nameAndRating[i]);
            switch (nameAndRating[i]) {
                case "Theme":
                    theme = Commands.stringIntoTheme(nameAndRating[i + 1]);
                    break;
                case "Rating":
                    rating = Integer.parseInt(nameAndRating[i + 1]);
                    break;
                case "Place":
                    place = nameAndRating[i + 1];
                    break;
                case "Name":
                    name = nameAndRating[i + 1];
                    break;
            }
        }
        if (name == null || theme == null || place == null || name.equals("") || place.equals("")) {
            System.err.println("Не верно введены параметры шоу");
            return false;
        } else {
            listOfShows.add(new DancingShow(name, rating, theme, place, creator));
            return true;
        }
    }

    /**
     * метод, в котором мы отправляем String клиенту
     * КОГДА МЫ ОТПРАВЛЯЕМ ЕМУ ЧЕРТОВО СООБЩЕНИЕ, мы снова ждем от него ввода. Если два раза использовать этот метод,
     * то происходит дичь и мы не можем дальше нормально работать. (сбивается последовательность наших действий)
     * @param message - строка, которую надо отправить
     * @param clientSocket - сокет клиента, которому надо отправить ответ
     */
    public static void sendMessageToClient(String message, Socket clientSocket) {
        OutputStream outStream = null;
        try {
            outStream = clientSocket.getOutputStream();
        } catch (IOException e){
            System.out.println("Невозможно отправить ответ клиенту");
            System.exit(-1);
        }
        //чтобы отправлять String клиенту
        DataOutputStream outStringStream = new DataOutputStream(outStream);
        try {
            outStringStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Т.к. у нас темы хранятся в ENUM, то когда к нам приходит строка, нужно как-то сравнивать ее с объектом
     * класса ThemeList, и получать из строки тему шоу
     * @param stringTheme строка, которую мы хотим преобразовать в тему из ENUM
     * @return тема шоу типа ThemeList
     */
    public static ThemesList stringIntoTheme(String stringTheme) {
        ThemesList theme;
        switch (stringTheme) {
            case "Space":
                theme = ThemesList.SPACE;
                return theme;
            case "Dancing":
                theme = ThemesList.DANCING;
                return theme;
            case "News":
                theme = ThemesList.NEWS;
                return theme;
            case "Humor":
                theme = ThemesList.HUMOR;
                return theme;
            default:
                System.err.println("Введена неверная тема шоу. Шоу без темы. Это можно изменить" +
                        "коммандой show.changeTheme(* НОВАЯ ТЕМА ШОУ *)");
                return ThemesList.NOTHEME;
        }
    }
}