package Lab7;

import Lab7.Commands.*;
import Lab7.Shows.Show;
import Lab7.Shows.ShowComparator;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * для каждого клиента будет запущен отдельный поток
 */
public class MyThread implements Runnable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private CopyOnWriteArrayList<Show> listOfShows;
    private Connection database;
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
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(inStream));
        String readClientStream;
        /**
         * авторизируем пользователя
         * key1 - пользователь выбирает либо залогиниться, либо зарегестрироваться
         * если он выбрал что-то из этого, далее он попадает в key2
         * key2 - пользователь вводит либо почту для входа, либо, если он ввел свою почту, добавляем его в базу данных
         * key3 - ввод пароля и авторизация
         * так же он может вернуться на выбор регистрации или логина с помощью комманды back
         */
        boolean key1 = true;
        boolean key2 = false;
        boolean key3 = false;
        boolean keyQuit = false;
        String userChoice = "";
        String userMail = "";
        try {
            while (!clientSocket.isClosed()) {
                if (keyQuit) break;
                while ((readClientStream = reader.readLine()) != null) {
                    /**
                     * фаза 1 - залогиниться/зарегаться
                     */
                    if (key1) {
                        if (readClientStream.equals("Login")) {
                            //переход во вторую фазу
                            Commands.sendMessageToClient("Введите логин", clientSocket);
                            key1 = false;
                            userChoice = "Login";
                        } else if (readClientStream.equals("Register")) {
                            //переход во вторую фазу
                            Commands.sendMessageToClient("Укажите почту для регистрации. На нее придет пароль",
                                    clientSocket);
                            key1 = false;
                            userChoice = "Register";
                        } else {
                            Commands.sendMessageToClient("Введена неверна команда. " +
                                    "Попробуйте еще раз", clientSocket);
                        }
                    }
                    /**
                     * фаза 2 - введение логина, либо, если выбрал регистрацию,
                     * проверяется, нет ли такого пользователя в базе данных
                     * При введении логина он запоминается, дабы в 3 фазе можно было проверить, соответствует ли
                     * введеный пароль логину пользователя
                     * back откатывает нас на 1 фазу назад
                     */
                    if (key2) {
                        if (readClientStream.equals("back")) {
                            //возврат в первую фазу
                            Commands.sendMessageToClient("Войти или зарегестрироваться?" +
                                    " (Login/Register)", clientSocket);
                            key1 = true;
                            key2 = false;
                            userChoice = "";
                            userMail = "";
                        } else if (Users.get(readClientStream) == null && userChoice.equals("Login")) {
                            Commands.sendMessageToClient("Нет пользователя с таким логином, " +
                                    "попробуйте еще раз" + "\n" + "Вы так же можете" +
                                    "вернуться к выбору Login/Register с помощью комманды back", clientSocket);
                        } else if (userChoice.equals("Login")) {
                            //переход в третью фазу по логину
                            Commands.sendMessageToClient("Введите пароль", clientSocket);
                            key2 = false;
                            userChoice = "Password";
                            userMail = readClientStream;
                        } else if (Users.get(readClientStream) != null){
                            Commands.sendMessageToClient("Пользователь с таким логином уже существует. " +
                                    "\nВойти или зарегистрироваться? (Login/Register)", clientSocket);
                            key1 = true;
                            key2 = false;
                            userChoice = "";
                            userMail = "";
                        } else {
                            //переход в третью фазу по регистрации
                            key2 = false;
                            userChoice = "getPasswordOnEmail";
                            userMail = readClientStream;
                            //сразу вписываем пользователя в базу данных, тут же генирируя ему пароль
                            /**
                             * генерируем пароль
                             */
                            PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
                                    .useLower(true)
                                    .useUpper(true)
                                    .useDigits(true)
                                    .build();
                            String password = passwordGenerator.generate(6);
                            /**
                             * отправляем его на почту
                             */
                            System.err.println(password);
                            EmailSender sendPassword = new EmailSender();
                            System.out.println(sendPassword.sendEmail("vov4ik.tereschenko@yandex.ru", password));
                            /**
                             * хэшируем пароль по алгоритму
                             */
                            String passwordHax = DatabaseCommands.MD5hash(password);
                            /**
                             * записываем хэшированный пароль и имя в базу данных
                             */
                            try {
                                /**
                                 * подготавливаем строку к записи в базу данных
                                 */
                                PreparedStatement pstmt = database.prepareStatement("insert into " +
                                                "\"Users\"(\"LOGIN\", \"PASSWORD\") values (?, ?)");
                                pstmt.setString(1, userMail);
                                pstmt.setString(2, passwordHax);
                                pstmt.executeUpdate();
                            } catch (SQLException e){
                                e.printStackTrace();
                            }
                            Users = DatabaseCommands.importUsers(database);
                            Commands.sendMessageToClient("Введите пароль", clientSocket);
                        }
                    }
                    if (userChoice.equals("Login") || userChoice.equals("Register")) key2 = true;
                    /**
                     * третья фаза - либо юзверь вводит пароль и заходит, либо нет
                     */
                    if (key3){
                        if ((Users.get(userMail)).equals(DatabaseCommands.MD5hash(readClientStream))){
                            Commands.sendMessageToClient("Успешная авторизация.\n" +
                                    "список доступных комманд:\n" +
                                    "show | info |remove_first | remove | remove_greater | remove_lower | " +
                                    "stop | sort_by_theme | sort_by_rating ", clientSocket);
                            System.out.println("Пользователь " + userMail + " авторизовался");
                            keyQuit = true;
                            break;
                        } else if (readClientStream.equals("back")){
                            //возврат в первую фазу
                            Commands.sendMessageToClient("Войти или зарегестрироваться?" +
                                    " (Login/Register)", clientSocket);
                            key1 = true;
                            key2 = false;
                            key3 = false;
                            userChoice = "";
                            userMail = "";
                        } else {
                            Commands.sendMessageToClient("Неверный пароль. Попробуйте снова " +
                                    "\nВы так же можете вернуться к выбору Login/Password" +
                                    " с помощью комманды back", clientSocket);
                        }
                    }
                    if (userChoice.equals("Password") || userChoice.equals("getPasswordOnEmail")) key3 = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * теперь, когда мы авторизовались, можем работать с коллекцией
         */
        try {
            while (!serverSocket.isClosed()) {
                while ((readClientStream = reader.readLine()) != null) {
                    //удалить первый элемент
                    if (readClientStream.equals("remove_first")) {
                        Commands.removeFirst(listOfShows, clientSocket, userMail);
                    } else if (readClientStream.equals("Save")){
                        DatabaseCommands.UploadShows(database, listOfShows, clientSocket);
                    }
                    //информация о коллекции
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
                    else if (CompareToCommand.compareRemove(readClientStream)) {
                        Commands.remove(readClientStream, userMail, listOfShows, clientSocket);
                    } //добавляем объект в нашу коллекцию
                    else if (CompareToCommand.compareAdd(readClientStream)) {
                        if (Commands.addElement(readClientStream, listOfShows, userMail)) {
                            Commands.sendMessageToClient("Успешно добавлен элемент в коллецию", clientSocket);
                        } else {
                            Commands.sendMessageToClient("Введена неверная комманда", clientSocket);
                        }
                    } //остановить программу
                    else if (readClientStream.equals("stop")) {
                        Commands.sendMessageToClient("Вы завершили работу. Идите с богом.", clientSocket);
                        reader.close();
                        clientSocket.close();
                        clientSocket = null;
                    } else {
                        Commands.sendMessageToClient("Введена неверная комманда", clientSocket);
                    }
                }
                System.out.println("Пользователь " + userMail + " отключился");
            }
        } catch (Exception e){
            System.out.println("Пользователь " + userMail + " отключился");
        }
    }
}
