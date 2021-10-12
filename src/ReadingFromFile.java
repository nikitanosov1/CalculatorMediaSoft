import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ReadingFromFile {
    private double exchangeRateUsdToRub;

    public ReadingFromFile(){
        exchangeRateUsdToRub = 0;
    }

    public double getExchangeRateUsdToRub() {
        return exchangeRateUsdToRub;
    }

    public void inputExchangeRateUsdToRub(String exchangeRateTxtName){
        try{
            File file = new File(exchangeRateTxtName);
            Scanner scanner = new Scanner(file);
            exchangeRateUsdToRub = scanner.nextDouble();
        }
        catch (IOException e){
            System.out.print("Error: " + e);
        }

        if (exchangeRateUsdToRub <= 0){
            throw new RuntimeException("invalid course");
        }
    }
}
