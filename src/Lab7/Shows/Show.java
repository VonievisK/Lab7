package Lab7.Shows;

import Lab7.Commands.Commands;

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
     * @param rating
     * @param theme
     * @param place
     */
    Show(int rating, ThemesList theme, String place) {
        this.place = place;
        this.dateOfCreating = LocalDateTime.now();
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
        this.theme = theme;
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