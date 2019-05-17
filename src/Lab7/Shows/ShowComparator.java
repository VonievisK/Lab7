package Lab7.Shows;

import java.util.Comparator;

/**
 * сравнивает шоу по определенному параметру
 */
public class ShowComparator implements Comparator<Show> {
        /**
         * список параметров сортировки
         */
        public enum Order{
                Rating, Theme
        }
        private Order sortingBy = Order.Rating;
        /**
         * @param show1 первое шоу
         * @param show2 второе шоу
         * @return либо число, большее нуля (объект ставится перед сравниваемым), либо 0 (объекты равны), либо меньшее
         * нуля (объект ставится после сравниваемого)
         */
        @Override
        public int compare(Show show1, Show show2) {
                try{
                        switch (sortingBy){
                                case Rating: return Integer.compare(show2.getRating(), show1.getRating());
                                case Theme: return show1.getTheme().toString().compareTo(show2.getTheme().toString());
                        }
                } catch (NullPointerException e){
                        System.err.println(e);
                        System.exit(0);
                }
                return 1;
        }

        /**
         * установить параметр сортировки
         * @param sortBy параметр, по которому сортируем
         */
        public void setSortingBy(Order sortBy){
                this.sortingBy = sortBy;
        }
}



