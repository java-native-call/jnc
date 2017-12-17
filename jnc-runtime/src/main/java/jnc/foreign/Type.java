package jnc.foreign;

public interface Type {

    int size();

    int alignment();

    @Override
    String toString();

}
