package Lab7;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class DatabaseClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 7769;

        //Создаем сокет
        String data;
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            System.out.println("Успешное подключение к серверу");
        } catch (UnknownHostException e) {
            System.out.println("Неизвестный хост: " + host);
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(e);
            System.out.println("Ошибка ввода/вывода при создании сокета " + host
                    + ":" + port);
            System.exit(-1);
        }
        /**
         * чтобы пользователь мог вводить команды в консоль создаем reader
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        /**
         * чтобы пользователь мог отправлять сообщеня серверу, создаем поток вывода
         */
        OutputStream out = null;
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Невозможно получить поток вывода");
            System.exit(-1);
        }
        /**
         * ?????
          */
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        String ln = null;
        System.out.println("Войти или зарегистрироваться?(Login/Register)");
        /**
         * если все пошло не так
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    writer.write("superStop");
                    writer.flush();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        try {
            while ((ln = reader.readLine()) != null) {
                writer.write(ln + "\n");
                writer.flush();
                //Читаем обратное сообщение от сервера
                try {
                    InputStream iStream = socket.getInputStream();
                    DataInputStream inStream = new DataInputStream(iStream);
                    data = inStream.readUTF();
                    System.out.println("Сервер ответил: \n" + data);
                    if (data.equals("Вы завершили работу. Идите с богом.")){;
                        System.exit(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Погоди, браток, возможно, сервера упали");
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка записи сообщения!");
            System.exit(-1);
        }
    }
}



