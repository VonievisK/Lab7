package Lab7.Shows;

import Lab7.Commands.Commands;
import Lab7.Commands.MakeStringIntoTheme;

import java.time.LocalDateTime;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Show implements Startable{
    private int rating;
    private ThemesList theme;
    private LocalDateTime dateOfCreating;
    private String place;
    private String creator;
    private String name;
    /**
     * отсортировать шоу по одному из двух параметров - рейтинг (от большего к меньшему) или тема (по алфавиту)
     * @param sortBy параметр сортировки, либо rating, либо theme
     * @param shows коллекция объектов класса Show
     * @param clientSocket клиент, которому мы сортируем шоу
     */
    public static CopyOnWriteArrayList<Show> sortShowsBy(ShowComparator.Order sortBy, CopyOnWriteArrayList<Show> shows, Socket clientSocket){
        Commands.sendMessageToClient("коллекция успешно отсортирована по параметру " + sortBy, clientSocket);
        ShowComparator comparator = new ShowComparator();
        comparator.setSortingBy(sortBy);
        Stream<Show> showStream = shows.stream().sorted(comparator);
        return showStream.collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    /**
     * объект нашего шоу
     * абстрактный класс шоу для всех рограмм, каждая программа является шоу
     * добавляем интерфейс Startable, т.к. любое шоу может начаться по-своему
     * @param rating - рейтинг шоу
     * @param theme - тема шоу
     * @param place - место проведения шоу
     * @param creator - создатель объекта
     * @param name - название шоу
     */
    Show(String name, int rating, ThemesList theme, String place, String creator) {
        this.name = name;
        this.theme = theme;
        try {
            this.rating = rating;
            if (this.rating > 100) {
                throw new TooMuchRatingException();
            }
            if (this.rating < 0) {
                throw new TooLowRatingException();
            }
        } catch(TooMuchRatingException e){
            this.rating = 100;
            System.err.println("Рейтинг был понижен до 100");
        } catch (TooLowRatingException e){
            this.rating = 0;
            System.err.println(e + "Рейтинг был поднят до 0");
        }
        this.place = place;
        this.dateOfCreating = LocalDateTime.now();
        this.creator = creator;
    }

    /**
     * в слуаче, если мы берем запись из базы данных, конструктор меняется, добавляя не свою дату создания, а
     * дату создания объекта из базы данных
     * @param rating - рейтинг шоу
     * @param theme - тема шоу
     * @param place - место проведения шоу
     * @param creator - создатель объекта
     * @param name - название шоу
     * @param dateOfCreation - дата создания объекта. Обязательно типа LocalDateTime
     */
    Show(String name, int rating, ThemesList theme, String place, String creator, LocalDateTime dateOfCreation) {
        this.name = name;
        this.theme = theme;
        this.dateOfCreating = dateOfCreation;
        try {
            this.rating = rating;
            if (this.rating > 100) {
                throw new TooMuchRatingException();
            }
            if (this.rating < 0) {
                throw new TooLowRatingException();
            }
        } catch(TooMuchRatingException e){
            this.rating = 100;
            System.err.println("Рейтинг был понижен до 100");
        } catch (TooLowRatingException e){
            this.rating = 0;
            System.err.println(e + "Рейтинг был поднят до 0");
        }
        this.place = place;
        this.creator = creator;
    }

    /**
     * можно изменить тему шоу
     * @param theme - тема шоу в строчном виде
     */
    public void changeTheme(String theme){
        this.theme = MakeStringIntoTheme.stringIntoTheme(theme);
    }

    /**
     * @return получаем тему шоу
     */
    public ThemesList getTheme() {
        return theme;
    }

    /**
     * @return получаем рейтинг шоу
     */
    public int getRating() {
        return rating;
    }

    /**
     * @return получаем дату создания шоу
     */
    public LocalDateTime getData(){
        return dateOfCreating;
    }

    /**
     * @return получаем место шоу
     */
    public String getPlace(){
        return place;
    }

    /**
     * @return создатель объекта
     */
    public String getCreator(){
        return creator;
    }

    /**
     * @return название шоу
     */
    public String getName(){
        return name;
    }
}

class TooMuchRatingException extends Exception{
    TooMuchRatingException(){
            System.err.println("Рейтинг больше 100");
    }
}
class TooLowRatingException extends Exception{
    TooLowRatingException(){
        System.err.println("Рейтинг отрицательный");
    }
}