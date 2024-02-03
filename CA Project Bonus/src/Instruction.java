public class Instruction {

    private String address;
    private String operation;
    private String[] operands;
    private int result;

    public Instruction(String address, String operation, String[] operands) {
        this.address = address;
        this.operation = operation;
        this.operands = operands;
    }

    public String getAddress() {
        return address;
    }

    public String getOperation() {
        return operation;
    }

    public String[] getOperands() {
        return operands;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "address='" + address + '\'' +
                ", operation='" + operation + '\'' +
                ", operands=" + java.util.Arrays.toString(operands) +
                '}';
    }
}
