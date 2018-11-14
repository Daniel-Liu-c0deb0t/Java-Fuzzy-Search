package javafuzzysearch.utils;

public interface Edit{
    public int getX();
    public int getY();

    public static abstract class SingleEdit implements Edit{
        protected char c;

        public SingleEdit(char c){
            this.c = c;
        }

        public char getChar(){
            return c;
        }
    }

    public static abstract class DoubleEdit implements Edit{
        protected char c1, c2;

        public DoubleEdit(char c1, char c2){
            this.c1 = c1;
            this.c2 = c2;
        }

        public char getChar1(){
            return c1;
        }

        public char getChar2(){
            return c2;
        }
    }

    public static class Same extends SingleEdit{
        public Same(char c){
            super(c);
        }

        @Override
        public int getX(){
            return -1;
        }

        @Override
        public int getY(){
            return -1;
        }

        @Override
        public String toString(){
            return c + "";
        }
    }

    public static class Insert extends SingleEdit{
        public Insert(char c){
            super(c);
        }

        @Override
        public int getX(){
            return -1;
        }

        @Override
        public int getY(){
            return 0;
        }

        @Override
        public String toString(){
            return "+ " + c;
        }
    }

    public static class Delete extends SingleEdit{
        public Delete(char c){
            super(c);
        }

        @Override
        public int getX(){
            return 0;
        }

        @Override
        public int getY(){
            return -1;
        }

        @Override
        public String toString(){
            return "- " + c;
        }
    }

    public static class Substitute extends DoubleEdit{
        public Substitute(char c1, char c2){
            super(c1, c2);
        }

        @Override
        public int getX(){
            return -1;
        }

        @Override
        public int getY(){
            return -1;
        }

        @Override
        public String toString(){
            return c1 + " -> " + c2;
        }
    }

    public static class Transpose extends DoubleEdit{
        // stores the resulting configuration
        public Transpose(char c1, char c2){
            super(c1, c2);
        }

        @Override
        public int getX(){
            return -2;
        }

        @Override
        public int getY(){
            return -2;
        }

        @Override
        public String toString(){
            return c2 + "" + c1 + " -> " + c1 + "" + c2;
        }
    }
}
