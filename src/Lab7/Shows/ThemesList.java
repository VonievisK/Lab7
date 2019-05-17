package Lab7.Shows;

/**
 * список тем, на которые может быть шоу
 */
public enum ThemesList {
    SPACE, DANCING, NEWS, HUMOR;

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
            default:
                return null;
        }
    }
}