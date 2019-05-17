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
     * удаляет первый элемент нашей строки
     *
     * @param listOfShows коллекция объектов класса Show
     */
    public static void removeFirst(CopyOnWriteArrayList<Show> listOfShows, Socket clientSocket) {
        listOfShows.remove(0);
        sendMessageToClient("Успешно удален первый элемент из коллекции", clientSocket);
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
     * удаляет элемент по его номеру
     *
     * @param command     введеная пользователем команда (строка)
     * @param listOfShows коллекция объектов класса Show
     */
    public static void remove(String command, CopyOnWriteArrayList<Show> listOfShows, Socket clientSocket) {
        if (command.startsWith("remove {")) {
            int numberOfElement;
            String[] keyAndValue = CompareToCommand.readJSON(command);
            //получаем число, которое содержится в фигурных скобках
            keyAndValue[1] = keyAndValue[1].substring(0, keyAndValue.length - 1);
            numberOfElement = Integer.parseInt(keyAndValue[1]);
            if (numberOfElement > listOfShows.size() - 1) {
                Commands.sendMessageToClient("Такого элемента не существует", clientSocket);
            } else {
                listOfShows.remove(numberOfElement);
                Commands.sendMessageToClient("Успешно удален " + numberOfElement + " элемент из коллекции", clientSocket);
            }
        } else {
            int numberOfElementGreaterLower;
            numberOfElementGreaterLower = Integer.parseInt(command.replaceAll("\\D+", ""));
            if (command.startsWith("remove_g")) {
                if (numberOfElementGreaterLower > listOfShows.size() - 1) {
                    Commands.sendMessageToClient("Такого элемента не существует", clientSocket);
                } else if (numberOfElementGreaterLower == listOfShows.size() - 1) {
                    Commands.sendMessageToClient("Элементов, больших данного, не существует", clientSocket);
                } else {
                    for (int i = listOfShows.size() - 1; i > numberOfElementGreaterLower; i--) {
                        listOfShows.remove(i);
                    }
                    Commands.sendMessageToClient("Успешно удалены элементы, большие чем " + numberOfElementGreaterLower, clientSocket);
                }
            } else {
                if (numberOfElementGreaterLower > listOfShows.size() - 1) {
                    Commands.sendMessageToClient("Такого элемента не существует", clientSocket);
                } else if (numberOfElementGreaterLower == 0) {
                    Commands.sendMessageToClient("Элементов, меньших данного, не существует", clientSocket);
                } else {
                    for (int i = 0; i < numberOfElementGreaterLower; i++) {
                        listOfShows.remove(0);
                    }
                    Commands.sendMessageToClient("Успешно удалены элементы, меньшие чем " + numberOfElementGreaterLower, clientSocket);
                }
            }
        }
    }
    /**
     * добавить элемент в нашу коллекцию. Сначала делаем массив строк, из которого получаем имя шоу и его рейтинг.
     * после создаем шоу и добавляем его в коллекцию
     *
     * @param command     введеная пользователем команда (строка)
     * @param listOfShows коллекция объектов класса Show
     */
    public static boolean addElement(String command, CopyOnWriteArrayList<Show> listOfShows) {
        String creator = "dsd";
        String name = null;
        int rating = 0;
        ThemesList theme = null;
        String place = null;
        String[] nameAndRating = CompareToCommand.readJSON(command.substring(5, command.length() - 1));
        for (int i = 0; i < 6; i++) {
            // System.err.println(nameAndRating[i]);
            switch (nameAndRating[i]) {
                case "Theme":
                    theme = MakeStringIntoTheme.stringIntoTheme(nameAndRating[i + 1]);
                    break;
                case "Rating":
                    rating = Integer.parseInt(nameAndRating[i + 1]);
                    break;
                case "Place":
                    place = nameAndRating[i + 1];
                    break;
                case "Name":
                    name = "ss";
                    break;
            }
        }
        if (name == null) {
            System.err.println("Не введена тема шоу");
            return false;
        } else {
            listOfShows.add(new DancingShow(name, rating, theme, place, creator));
            return true;
        }
    }

    /**
     * метод, в котором мы отправляем String клиенту
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
        PrintWriter writer = null;
        writer = new PrintWriter(outStream, true);
        //чтобы отправлять String клиенту
        DataOutputStream outStringStream = new DataOutputStream(outStream);
        try {
            outStringStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CopyOnWriteArrayList<Show> makeLinkedListFromFile(){
        CopyOnWriteArrayList<Show> listOfShows = new CopyOnWriteArrayList<>();
        // /Users/vonievisk/Library/Mobile Documents/com~apple~CloudDocs/Учеба/Java/lab3-5/src/Lab4/infoAboutShows.txt - дефолтный путь
        // /home/s265949/Lab4/infoAboutShows.txt - путь на гелиосе
        // заполняем коллекцию из файла
        try (BufferedReader reader = new BufferedReader(new FileReader("C:/Users/derro/IdeaProjects/SuperTest/src/infoAboutShows.txt"))) {
            //readline - берем нашу строку, replaceAll - удаляем все пробелы, split - разделяем по запятой
            ThemesList theme = null;
            String creator = "sss";
            int rating = 0;
            int index = 0;
            String placeOfShow = null;
            String inLine;
            while ((inLine = reader.readLine()) != null){
                Scanner scanner = new Scanner(inLine);
                scanner.useDelimiter(", ");
                while (scanner.hasNext()){
                    String data = scanner.next();
                    if(index == 0){
                        rating = Integer.parseInt(data);
                    } else if (index == 1){
                        theme = MakeStringIntoTheme.stringIntoTheme(data);
                    } else if (index == 2){
                        placeOfShow = data;
                    } else {
                        System.out.println("В файле некорректно введены данные о шоу");
                    }
                    index++;
                }
                index = 0;
                String name = "s";
                listOfShows.add(new DancingShow(name, rating, theme, placeOfShow, creator));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return listOfShows;
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

    public static HashMap<String, String> updateUsersList(Connection database){
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
}