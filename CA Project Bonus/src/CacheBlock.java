public class CacheBlock {
    public enum State { MODIFIED, SHARED, INVALID }
    private State state;
    private String address;
    private int data;

    public CacheBlock(State state, String address, int data) {
        this.state = state;
        this.address = address;
        this.data = data;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }
}
