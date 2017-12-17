package jnc.example;

public class Memcpy {

    public static void main(String[] args) {
        Libc libc = Libc.INSTANCE;
        long start = System.nanoTime();
        for (int i = 0; i < 100000; ++i) {
            libc.memcpy(0, 0, 0);
        }
        System.out.println((System.nanoTime() - start) / 1e6 + " ms");
    }

}
