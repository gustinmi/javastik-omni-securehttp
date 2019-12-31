import java.util.concurrent.TimeUnit;

public class ThreadExample implements Runnable {

    private String greeting; // Message to print to console

    public ThreadExample(String greeting) {
        this.greeting = greeting;
    }

    public void run() {
        while (true) {
            System.out.println(Thread.currentThread().getName() + ":  " + greeting);
            try {
                // Sleep 0 to 100 milliseconds
                TimeUnit.MILLISECONDS.sleep(((long) Math.random() * 100));
            } catch (InterruptedException e) {} // Should not happen
        }
    }

    public static void main(String[] args) {
        new Thread(new ThreadExample("Hello")).start();
        new Thread(new ThreadExample("Aloha")).start();
        new Thread(new ThreadExample("Ciao")).start();
    }
}
