package data_types;

public class MutableInt {
    private int value;

    public MutableInt(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public int incrementAndGet() {
        value++;
        return value;
    }

    public int decrementAndGet() {
        value--;
        return value;
    }

    public void set(int value) {
        this.value = value;
    }
}
