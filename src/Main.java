import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static HashMap<String, Function> functionMap;
    public static double exchangeRateUsdToRub;
    public static void main(String[] args){
        // String test = "toDollars(737р + toRubles($85.4 + $0))";
        // String test = "2p + -(4p + 3p)";

        functionMap = getFunctionMap();  // HashMap со всеми реализованными функциями

        ReadingFromFile file = new ReadingFromFile();
        file.inputExchangeRateUsdToRub("configuration.txt");
        exchangeRateUsdToRub = file.getExchangeRateUsdToRub();

        Scanner reader = new Scanner(System.in);
        System.out.print("Enter the line to count: ");
        String test = reader.nextLine();

        List<Lexeme> lexemes = lexAnalysis(test);    // Лексический анализатор
        Lexeme answer = expression(new LexemeBuffer(lexemes));    // Синтаксический анализатор
        switch (answer.type){
            case NUMBER_IN_RUB:
                System.out.println(String.format("%.2f",Double.parseDouble(answer.value)) + "p");
                break;
            case NUMBER_IN_USD:
                System.out.println("$" + String.format("%.2f",Double.parseDouble(answer.value)));
                break;
        }
    }

    public interface Function{
        Lexeme apply(List<Lexeme> args);
    }

    public static HashMap<String, Function> getFunctionMap(){
        HashMap<String, Function> functionTable = new HashMap<>();
        functionTable.put("toRubles", args -> {
            if (args.isEmpty()){
                throw new RuntimeException("no args");
            }
            Lexeme temp = args.get(0);
            if (temp.type == LexemeType.NUMBER_IN_USD) {
                temp.type = LexemeType.NUMBER_IN_RUB;
                temp.value = Double.toString(Double.parseDouble(temp.value) * exchangeRateUsdToRub);
            }
            return temp;
        });

        functionTable.put("toDollars", args -> {
            if (args.isEmpty()){
                throw new RuntimeException("no args");
            }
            Lexeme temp = args.get(0);
            if (temp.type == LexemeType.NUMBER_IN_RUB) {
                temp.type = LexemeType.NUMBER_IN_USD;
                temp.value = Double.toString(Double.parseDouble(temp.value) / exchangeRateUsdToRub);
            }
            return temp;
        });
        return functionTable;
    }


    public enum LexemeType{
        LEFT_BRACKET, RIGHT_BRACKET,
        OP_PLUS, OP_MINUS,
        NUMBER_IN_RUB, NUMBER_IN_USD, FUNCTION, COMMA,
        EOF
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
                case ',':
                    lexemes.add(new Lexeme(LexemeType.COMMA, c));
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
                        break;
                    }else {
                        if( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')){
                            StringBuilder builderFunction = new StringBuilder();
                            do {
                                builderFunction.append(c);
                                ++pos;
                                if (pos >= text.length()) {
                                    break;
                                }
                                c = text.charAt(pos);
                            } while ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));

                            if (functionMap.containsKey(builderFunction.toString())) {
                                lexemes.add(new Lexeme(LexemeType.FUNCTION, builderFunction.toString()));
                            }else{
                                throw new RuntimeException("name func incorrect");
                            }
                            break;
                        }else{
                            throw new RuntimeException("none");
                        }
                    }
            }
        }
        lexemes.add(new Lexeme(LexemeType.EOF, ""));
        return lexemes;
    }
    public static class LexemeBuffer{
        private int pos;

        public List<Lexeme> lexemes;

        public LexemeBuffer(List<Lexeme> lexemes){
            this.lexemes = lexemes;
        }

        public Lexeme next(){
            return lexemes.get(pos++);
        }

        public void back(){
            --pos;
        }

        public int getPos(){
            return pos;
        }
    }

    /*
    =====================================================
                        <PARSER RULES>
    =====================================================
    expression: element* EOF;
    element: component + element | component - element | component
    component: function | '(' element ')' | NUMBER_RUB | NUMBER_USD | '-' component
    function: NAME '('element, element, ...')'
    */

    public static Lexeme expression(LexemeBuffer lexemes){
        Lexeme lexeme = lexemes.next();
        if (lexeme.type == LexemeType.EOF){
            return new Lexeme(LexemeType.NUMBER_IN_USD, "0");
        }else{
            lexemes.back();
            return element(lexemes);
        }
    }

    public static Lexeme element(LexemeBuffer lexemes){
        Lexeme value = component(lexemes);
        while (true) {
            Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case OP_MINUS:
                    Lexeme temp = component(lexemes);
                    if (temp.type != value.type){
                        throw new RuntimeException("usd + rub error");
                    }
                    value.value = Double.toString(Double.parseDouble(value.value) - Double.parseDouble(temp.value));
                    break;
                case OP_PLUS:
                    Lexeme temp2 = component(lexemes);
                    if (temp2.type != value.type){
                        throw new RuntimeException("usd+rub error");
                    }
                    value.value = Double.toString(Double.parseDouble(value.value) + Double.parseDouble(temp2.value));
                    break;
                default:
                    lexemes.back();
                    return value;
            }
        }
    }

    public static Lexeme component(LexemeBuffer lexemes){
        Lexeme lexeme = lexemes.next();
        Lexeme answerLexeme = lexeme;
        switch (lexeme.type){
            case FUNCTION:
                lexemes.back();
                return function(lexemes);
            case OP_MINUS:
                answerLexeme = component(lexemes);
                answerLexeme.value = Double.toString(-Double.parseDouble(answerLexeme.value));
                return answerLexeme;
            case NUMBER_IN_RUB:
            case NUMBER_IN_USD:
                return answerLexeme;
            case LEFT_BRACKET:
                Lexeme value = element(lexemes);
                lexeme = lexemes.next();
                if (lexeme.type != LexemeType.RIGHT_BRACKET){
                    throw new RuntimeException("Unexpected token: " + lexeme.value + " at position: " + lexemes.getPos());
                }
                return value;
            default:
                throw new RuntimeException("Unexpected token: " + lexeme.value + " at position: " + lexemes.getPos());
        }
    }

    public static Lexeme function(LexemeBuffer lexemes){
        String name = lexemes.next().value;
        Lexeme lexeme = lexemes.next();
        if (lexeme.type != LexemeType.LEFT_BRACKET){
            throw new RuntimeException("incorrect function");
        }

        ArrayList<Lexeme> args = new ArrayList<>();
        lexeme = lexemes.next();
        if (lexeme.type != LexemeType.RIGHT_BRACKET){
            lexemes.back();
            do{
                args.add(element(lexemes));
                lexeme = lexemes.next();
                if ((lexeme.type != LexemeType.COMMA) && (lexeme.type != LexemeType.RIGHT_BRACKET)){
                    throw new RuntimeException("incorrect syntax in func");
                }
            } while (lexeme.type == LexemeType.COMMA);
        }
        return functionMap.get(name).apply(args);
    }
}
