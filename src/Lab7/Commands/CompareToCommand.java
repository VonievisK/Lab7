package Lab7.Commands;

import Lab7.Shows.ThemesList;

import java.net.Socket;

public class CompareToCommand{

    /**
     * парсим строку JSON
     * @param a
     * @return возвращает массив строк, разделенных так, как предполагает JSON
     */
    public static String[] readJSON(String a){
        if (a.contains(",")){
            a = a.replaceAll(",", ":");
        }
        return a.replaceAll("\"", "").split(":");
    }

    public static boolean compareRemove(String a, Socket clientSocket) {
        if ((a.startsWith("remove {") || a.startsWith("remove_greater {") || a.startsWith("remove_lower {")) && a.endsWith("}")) {
            if (a.startsWith("remove {")) a = a.substring(8, a.length() - 1);
            else if (a.startsWith("remove_greater {")) a = a.substring(16, a.length() - 1);
            else a = a.substring(14, a.length() - 1);
            String[] keyAndValue = CompareToCommand.readJSON(a);
            if (keyAndValue.length != 2) {
                System.err.println("Неверный параметр");
                return false;
            }
            if (!keyAndValue[0].equals("NumberOfElement")) {
                System.err.println("Неверный ключ");
                return false;
            }
            if (keyAndValue[1].replaceAll("\\D+", "").equals("")) {
                System.err.println("Введено неверное значение");
                return false;
            }
            return true;
        } else return false;
    }
    /**
     * проверяем, соответствует ли введенная строка нужной нам команде вида add{THEME(класса ThemeList} RATING(int)}
     * @param a команда пользователя
     * @return true, если команду выполняем, false если комманда введеная некорректно
     */
    public static boolean compareAdd(String a){
        if(a.startsWith("add {") && a.endsWith("}")){
            if (Commands.countK(a) != 12) {
                System.err.println("Вы забыли поставить кавычки у ключа");
                return false;
            }
            String[] nameAndRating = CompareToCommand.readJSON(a.substring(5, a.length() - 1));
            if (nameAndRating.length != 6){
                System.err.println("Введены некорректные данные");
                return false;
            }
            for (int i = 0; i < 6; i++){
                switch (nameAndRating[i]) {
                    case "Theme":
                        if (MakeStringIntoTheme.stringIntoTheme(nameAndRating[i + 1]) instanceof ThemesList) {
                            return true;
                        } else {
                            System.err.println("Невозможно добавить объект. Такого типа шоу не существует");
                            return false;
                        }
                    case "Rating":
                        if (nameAndRating[i + 1].replaceAll("\\D+", "").equals("")) {
                            System.err.println("Введено неверное значение");
                            return false;
                        }
                }
            } return true;
        } else return false;
    }
}

