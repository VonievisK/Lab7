package Lab7.Commands;

import Lab7.Shows.ThemesList;

/**
 * Т.к. у нас темы хранятся в ENUM, то когда к нам приходит строка, нужно как-то сравнивать ее с объектом
 * класса ThemeList, и получать из строки тему шоу
 */
public class MakeStringIntoTheme{
    public static ThemesList stringIntoTheme(String name) {
        ThemesList theme;
        switch (name) {
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
                //System.out.println("Не получилось преобразовать строку в тему");
                return null;
        }
    }

    @Override
    public String toString() {
        return "Making string into the theme";
    }
}



