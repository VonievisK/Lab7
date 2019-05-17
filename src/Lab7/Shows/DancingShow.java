package Lab7.Shows;

import java.time.LocalDateTime;
import java.util.Objects;

public class DancingShow extends Show {
    public DancingShow(String name, int rating, ThemesList theme, String place, String creator) {
        super(name, rating, theme, place, creator);
    }
    public DancingShow(String name, int rating, ThemesList theme, String place, String creator, LocalDateTime dateOfCreation) {
        super(name, rating, theme, place, creator, dateOfCreation);
    }
    public void previewShow() {
        System.out.println("Послышалась музыка. На экране появились танцующие пары.");
    }

    public void startShow() {
        System.out.println("Пошел адский флекс. Все начинают слэмиться");
    }

    @Override
    public String toString() {
        return (getName() + " " + getRating() + " " + getTheme().toString() + " " + getPlace() + " " +  getData() +
                "\n");
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTheme(), getRating());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        DancingShow other = (DancingShow) obj;
        if (this.getRating() != other.getRating())
            return false;
        if (this.getTheme() != other.getTheme())
            return false;
        return true;
    }
}