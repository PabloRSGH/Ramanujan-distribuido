import java.math.BigDecimal;
import java.math.MathContext;

public class RamanujanPi {
    private static final int TOTAL_ITERATIONS = 3000;
    private static final int NUM_THREADS = 3;
    private static BigDecimal totalSum = BigDecimal.ZERO;

    public static void main(String[] args) throws InterruptedException {
        long t1 = System.currentTimeMillis();

        Thread[] threads = new Thread[NUM_THREADS];
        int iterationsPerThread = TOTAL_ITERATIONS / NUM_THREADS;

        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * iterationsPerThread;
            int end = (i == NUM_THREADS - 1) ? TOTAL_ITERATIONS : start + iterationsPerThread;
            threads[i] = new Thread(new PiCalculator(start, end));
            System.out.println("Starting Thread " + (i + 1) + " with range [" + start + ", " + (end - 1) + "]");
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        BigDecimal pi = finalizeCalculation(totalSum);
        long t2 = System.currentTimeMillis();
        System.out.println("Valor aproximado de PI: " + pi);
        System.out.println("Time taken: " + (t2 - t1) + " ms");
    }

    private static class PiCalculator implements Runnable {
        private int start;
        private int end;
        private BigDecimal threadSum;

        public PiCalculator(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal sqrt2 = BigDecimal.valueOf(Math.sqrt(2));
            BigDecimal a, b;
            MathContext mc = new MathContext(100);
            BigDecimal term;

            for (int k = start; k < end; k++) {
                a = factorial(4 * k).multiply(BigDecimal.valueOf(1103 + 26390 * k));
                b = factorial(k).pow(4).multiply(BigDecimal.valueOf(396).pow(4 * k));
                term = a.divide(b, mc);
                sum = sum.add(term);
            }

            threadSum = sum;

            synchronized (totalSum) {
                totalSum = totalSum.add(sum);
            }

            System.out.println("Thread " + Thread.currentThread().getId() + " result: " + sum);
        }
    }

    private static BigDecimal finalizeCalculation(BigDecimal sum) {
        BigDecimal sqrt2 = BigDecimal.valueOf(Math.sqrt(2));
        MathContext mc = new MathContext(100);
        BigDecimal result = BigDecimal.ONE.divide(sqrt2.multiply(BigDecimal.valueOf(9801)), mc).multiply(sum);
        return BigDecimal.ONE.divide(result, mc).divide(BigDecimal.valueOf(4), mc);
    }

    public static BigDecimal factorial(int n) {
        BigDecimal result = BigDecimal.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigDecimal.valueOf(i));
        }
        return result;
    }
}