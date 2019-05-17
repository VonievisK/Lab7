package Lab7.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PasswordGenerator{

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private boolean useLower;
    private boolean useUpper;
    private boolean useDigits;

    private PasswordGenerator(PasswordGeneratorBuilder builder) {
        this.useLower = builder.useLower;
        this.useUpper = builder.useUpper;
        this.useDigits = builder.useDigits;
    }

    public static class PasswordGeneratorBuilder {

        private boolean useLower;
        private boolean useUpper;
        private boolean useDigits;

        public PasswordGeneratorBuilder() {
            this.useLower = false;
            this.useUpper = false;
            this.useDigits = false;
        }

        /**
         * @param useLower true, если мы хотим использовать строчные буквы. Изначально не используем
         * @return изменный создатель пароля
         */
        public PasswordGeneratorBuilder useLower(boolean useLower) {
            this.useLower = useLower;
            return this;
        }

        /**
         * @param useUpper true, если мы хотим использовать заглавные буквы. Изначально не используем
         * @return изменный создатель пароля
         */
        public PasswordGeneratorBuilder useUpper(boolean useUpper) {
            this.useUpper = useUpper;
            return this;
        }

        /**
         * @param useDigits true, если мы хотим использовать цифры. Изначально не используем
         * @return измененный создатель пароля
         */
        public PasswordGeneratorBuilder useDigits(boolean useDigits) {
            this.useDigits = useDigits;
            return this;
        }

        /**
         * @return создает объект для построения строки
         */
        public PasswordGenerator build() {
            return new PasswordGenerator(this);
        }
    }

    /**
     * метод генерирует нам пароль
     * @param length длина пароля
     * @return пароль с заданными параметрами
     */
    public String generate(int length) {
        // проверка аргумента
        if (length <= 0) {
            return "";
        }

        // переменные
        StringBuilder password = new StringBuilder(length);
        Random random = new Random(System.nanoTime());

        // смотрим, какие параметры использовать
        List<String> charCategories = new ArrayList<>(3);
        if (useLower) {
            charCategories.add(LOWER);
        }
        if (useUpper) {
            charCategories.add(UPPER);
        }
        if (useDigits) {
            charCategories.add(DIGITS);
        }

        // создание пароля
        for (int i = 0; i < length; i++) {
            String charCategory = charCategories.get(random.nextInt(charCategories.size()));
            int position = random.nextInt(charCategory.length());
            password.append(charCategory.charAt(position));
        }
        return new String(password);
    }
}