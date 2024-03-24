/**
 * 2024, Rivera Sánchez Pablo
 */
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.*;

public class ServerA {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("ServerA escuchando en el puerto 8080");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado a ServerA");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        String request = in.readLine();
                        System.out.println("Datos recibidos: " + request);
                        if (request.startsWith("GET /favicon.ico")) {
                            out.println("HTTP/1.1 204 No Content");
                            out.println("\r\n");
                        } else if (request.startsWith("GET /") && request.split("/").length > 2 &&request.endsWith("1.1")) {
                            String[] parts = request.split(" ")[1].split("/");
                            int kInitial = Integer.parseInt(parts[1]);
                            int kFinal = Integer.parseInt(parts[2]);
                            BigDecimal sum = calculateSum(kInitial, kFinal);
                            System.out.println("Datos enviados: " + sum.toString());
                            //out.println(sum);
                            out.println("HTTP/1.1 200 OK");
                            out.println("Content-Type: text/html");
                            out.println("\r\n");
                            out.println("<!DOCTYPE html><html><body><h1>Resultado: " + sum.toString() + "</h1></body></html>");
                        }
                        else if (request.startsWith("GET /") && request.split("/").length > 2 ) {
                            String[] parts = request.split(" ")[1].split("/");
                            int kInitial = Integer.parseInt(parts[1]);
                            int kFinal = Integer.parseInt(parts[2]);
                            BigDecimal sum = calculateSum(kInitial, kFinal);
                            System.out.println("Datos enviados: " + sum.toString());
                            out.println(sum);
                            out.println("HTTP/1.1 200 OK");
                            out.println("Content-Type: text/html");
                            out.println("\r\n");
                            out.println("<!DOCTYPE html><html><body><h1>Resultado: " + sum.toString() + "</h1></body></html>");
                        } 
                        else {
                            out.println("HTTP/1.1 400 Bad Request");
                            out.println("Content-Type: text/html");
                            out.println("\r\n");
                            out.println("<!DOCTYPE html><html><body><h1>Error: Formato de petición incorrecto</h1></body></html>");
                        }
                        out.flush();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static BigDecimal calculateSum(int kInitial, int kFinal) {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal sqrt2 = BigDecimal.valueOf(Math.sqrt(2));
        BigDecimal a, b;
        MathContext mc = new MathContext(100); // Precision of 100 digits
        BigDecimal term;

        for (int k = kInitial; k < kFinal; k++) {
            a = factorial(4 * k).multiply(BigDecimal.valueOf(1103 + 26390 * k));
            b = factorial(k).pow(4).multiply(BigDecimal.valueOf(396).pow(4 * k));
            term = a.divide(b, mc);
            sum = sum.add(term);
        }
        System.out.println("Thread " + Thread.currentThread().getId() + " result: " + sum);

        //BigDecimal result = BigDecimal.ONE.divide(sqrt2.multiply(BigDecimal.valueOf(9801)), mc).multiply(sum);
        //return BigDecimal.ONE.divide(result, mc).divide(BigDecimal.valueOf(4), mc);
        return sum;

    }
    public static BigDecimal factorial(int n) {
        BigDecimal result = BigDecimal.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigDecimal.valueOf(i));
        }
        return result;
    }

}
