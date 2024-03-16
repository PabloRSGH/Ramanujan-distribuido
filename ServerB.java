import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.*;

public class ServerB {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8082);
        System.out.println("ServerB escuchando en el puerto 8082");
        while (true) {
            final Socket clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado a ServerB");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        String request = in.readLine();
                        if (request.startsWith("GET /pi")) {
                            long t1 = System.currentTimeMillis();
                            BigDecimal sum = BigDecimal.ZERO;
                            Thread[] threads = new Thread[3];
                            BigDecimal[] results = new BigDecimal[3];
                            String[] serverAddresses = { "localhost", "localhost", "localhost" };
                            for (int i = 0; i < 3; i++) {
                                int kInitial = i * 1000 ;
                                int kFinal = (i + 1) * 1000;
                                int finalI = i;
                                String serverAddress = serverAddresses[i];
                                threads[i] = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try (Socket serverASocket = new Socket(serverAddress, 8080);
                                                PrintWriter serverAOut = new PrintWriter(
                                                        serverASocket.getOutputStream(), true);
                                                BufferedReader serverAIn = new BufferedReader(
                                                        new InputStreamReader(serverASocket.getInputStream()))) {
                                            String dataToSend = "GET /" + kInitial + "/" + kFinal;
                                            System.out.println("Datos enviados a ServerA: " + dataToSend);
                                            serverAOut.println(dataToSend);
                                            String result = serverAIn.readLine();
                                            System.out.println("Datos recibidos de ServerA: " + result);
                                            if (result != null && !result.startsWith("Error")) {
                                                results[finalI] = new BigDecimal(result);
                                            }
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });
                                threads[i].start();
                            }
                            for (int i = 0; i < 3; i++) {
                                threads[i].join();
                                if (results[i] != null) {
                                    sum = sum.add(results[i]);
                                }
                            }
                            MathContext mc = new MathContext(100);
                            BigDecimal sqrt2 = BigDecimal.valueOf(Math.sqrt(2));
                            BigDecimal piApproximation = BigDecimal.ONE
                                    .divide(sqrt2.multiply(BigDecimal.valueOf(9801)), mc).multiply(sum);
                            BigDecimal piApproximation_cm = BigDecimal.ONE.divide(piApproximation, mc)
                                    .divide(BigDecimal.valueOf(4), mc);
                            out.println("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n"
                                    + piApproximation_cm.toString());
                            long t2 = System.currentTimeMillis();
                            System.out.println("Valor aproximado de PI: " + piApproximation_cm);
                            System.out.println("Tiempo de respuesta: " + (t2 - t1) + " ms");
                        } else {
                            out.println("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nError: Not Found");
                        }

                        clientSocket.close();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static BigDecimal sqrt(BigDecimal value) {
        BigDecimal x = new BigDecimal(Math.sqrt(value.doubleValue()));
        return x.add(new BigDecimal(value.subtract(x.multiply(x)).doubleValue() / (x.doubleValue() * 2.0)));
    }
}
