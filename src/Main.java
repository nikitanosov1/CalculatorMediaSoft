import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        /*
        <PARSER RULES>


        */

        //String test = "toDollars(737р + toRubles($85,4))";
        String test = "$73.7 + 65p -       3425,23p";
        List<Lexeme> lexemes = lexAnalysis(test);

        //String t2 = ",21";
        //System.out.println(Double.parseDouble(t2) + 2);

        /*
        Scanner reader = new Scanner(System.in);
        System.out.print("Enter the line to count: ");
        String a = reader.nextLine();
        System.out.println(a);
    */
    }


    public enum LexemeType{
        LEFT_BRACKET, RIGHT_BRACKET,
        OP_PLUS, OP_MINUS,
        NUMBER_IN_RUB, NUMBER_IN_USD,
        EOF;
    }

    public static class Lexeme{
        LexemeType type;
        String value;

        public Lexeme(LexemeType type, String value){
            this.type = type;
            this.value = value;
        }

        public Lexeme(LexemeType type, Character value){
            this.type = type;
            this.value = value.toString();
        }
    }

    public static List<Lexeme> lexAnalysis(String text){
        ArrayList<Lexeme> lexemes = new ArrayList<>();
        int pos = 0;
        while (pos < text.length()){
            char c = text.charAt(pos);
            switch (c){
                case ' ':
                    ++pos;
                    continue;
                case '(':
                    lexemes.add(new Lexeme(LexemeType.LEFT_BRACKET, c));
                    ++pos;
                    continue;
                case ')':
                    lexemes.add(new Lexeme(LexemeType.RIGHT_BRACKET, c));
                    ++pos;
                    continue;
                case '+':
                    lexemes.add(new Lexeme(LexemeType.OP_PLUS, c));
                    ++pos;
                    continue;
                case '-':
                    lexemes.add(new Lexeme(LexemeType.OP_MINUS, c));
                    ++pos;
                    continue;
                case '$':
                    ++pos;
                    if (pos >= text.length()){
                        throw new RuntimeException("Incorrect number in USD");
                    }
                    c = text.charAt(pos);
                    StringBuilder builderNumberUSD = new StringBuilder();
                    if ((c >= '0') && (c <= '9')) {
                        int counterOfDots = 0;
                        do {
                            if ((c == '.') || (c == ',')){
                                ++counterOfDots;
                                builderNumberUSD.append('.');
                            }else{
                                builderNumberUSD.append(c);
                            }
                            ++pos;
                            if (pos >= text.length()) {
                                break;
                            }
                            c = text.charAt(pos);
                        } while (((c >= '0') && (c <= '9')) || (c == '.') || (c == ','));
                        if (counterOfDots > 1) throw new RuntimeException("Incorrect fractional number in USD");
                        lexemes.add(new Lexeme(LexemeType.NUMBER_IN_USD, builderNumberUSD.toString()));
                        continue;
                    }else{
                        throw new RuntimeException("Incorrect number in USD");
                    }
                default:
                    if ((c >= '0') && (c <= '9')) {
                        StringBuilder builderNumberRUB = new StringBuilder();
                        int counterOfDots = 0;
                        do {
                            if ((c == '.') || (c == ',')){
                                ++counterOfDots;
                                builderNumberRUB.append('.');
                            }else{
                                builderNumberRUB.append(c);
                            }
                            ++pos;
                            if (pos >= text.length()) {
                                break;
                            }
                            c = text.charAt(pos);
                        } while (((c >= '0') && (c <= '9')) || (c == '.') || (c == ','));
                        if (counterOfDots > 1) throw new RuntimeException("Incorrect fractional number in RUB");
                        if (c != 'р' && c != 'p') throw new RuntimeException("Incorrect number in RUB");
                        lexemes.add(new Lexeme(LexemeType.NUMBER_IN_RUB, builderNumberRUB.toString()));
                        ++pos;
                        continue;
                    }else
                    {
                        throw new RuntimeException("none");
                    }
            }
        }
        lexemes.add(new Lexeme(LexemeType.EOF, ""));
        return lexemes;
    }

}
