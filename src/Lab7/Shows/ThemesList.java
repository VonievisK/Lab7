package Lab7.Shows;

/**
 * список тем, на которые может быть шоу
 */
public enum ThemesList {
    SPACE, DANCING, NEWS, HUMOR, NOTHEME;

    @Override
    public String toString() {
        switch (this){
            case SPACE:
                return "Space";
            case DANCING:
                return "Dancing";
            case NEWS:
                return "News";
            case HUMOR:
                return "Humor";
            case NOTHEME:
                return "NoTheme";
            default:
                return null;
        }
    }
}