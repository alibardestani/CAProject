import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CPU {
    private Queue<Instruction>[] stages;
    private Cache cache;
    private Map<String, Integer> registers;
    private boolean running;
    private int programCounter;

    private int clockCycle = 0;

    private boolean isMemoryUnitBusy = false;
    private final Instruction NOP = new Instruction("NOP", "NOP", new String[0]);

    @SuppressWarnings("unchecked")
    public CPU() {
        stages = (Queue<Instruction>[]) new Queue[PipelineStage.values().length];
        for (int i = 0; i < stages.length; i++) {
            stages[i] = new LinkedList<>();
        }
        cache = new Cache(16, 2);
        registers = new HashMap<>();
        programCounter = 0;
        running = true;
    }

    public void loadInstruction(Instruction instruction) {
        stages[PipelineStage.FETCH.ordinal()].add(instruction);
    }

    public void start() {
        System.out.println("Initial Register States:");
        printRegistersStart();

        while (running) {
//            System.out.println("\n\n");
//            for (int i = 0; i < stages.length; i++) {
//                System.out.println("Stage " + i + ":");
//                Queue<Instruction> stage = stages[i];
//                for (Instruction instruction : stage) {
//                    System.out.println(instruction);
//                }
//            }
//            System.out.println("\n\n");
            processPipeline();
            checkForCompletion();
        }
    }

    private void printRegistersStart() {
        for (Map.Entry<String, Integer> entry : registers.entrySet()) {
            System.out.println("Register " + entry.getKey() + ": " + entry.getValue());
        }
    }

    private void processPipeline() {

        System.out.println("Clock Cycle: " + clockCycle);

        if (!stages[PipelineStage.WRITE_BACK.ordinal()].isEmpty()) {
            writeBack();
        }

        if (stages[PipelineStage.MEMORY.ordinal()].isEmpty() || !isMemoryUnitBusy) {
            memoryAccess();
        } else {
            System.out.println("  Memory unit busy, inserting stall...");
            insertStall(PipelineStage.EXECUTE.ordinal());
        }
        if (!stages[PipelineStage.EXECUTE.ordinal()].isEmpty()) {
            execute();
        }
        if (!stages[PipelineStage.DECODE.ordinal()].isEmpty()) {
            decode();
        }

        if (!isMemoryUnitBusy) {
            fetch();
        } else {
            System.out.println("  Fetch delayed due to memory unit busy...");
        }

        clockCycle++;
        System.out.println("--------------------");
    }

    private boolean detectDataHazard() {
        // دیکود و اکزیکوت رو چک میکنه
        Instruction decodeStageInstruction = stages[PipelineStage.DECODE.ordinal()].peek();
        if (decodeStageInstruction == null) {
            return false;
        }

        String[] decodeOperands = decodeStageInstruction.getOperands();
        if (decodeOperands.length == 0) {
            return false;
        }

        Instruction executeStageInstruction = stages[PipelineStage.EXECUTE.ordinal()].peek();
        if (executeStageInstruction == null) {
            return false;
        }

        String executeDestReg = executeStageInstruction.getOperands()[0];

        for (String operand : decodeOperands) {
            if (operand.equals(executeDestReg)) {
                return true;
            }
        }

        return false;
    }


    private void insertBubble() {
        stages[PipelineStage.EXECUTE.ordinal()].add(NOP);
    }

    private void checkForCompletion() {
        boolean isPipelineEmpty = true;
        for (Queue<Instruction> stage : stages) {
            if (!stage.isEmpty()) {
                isPipelineEmpty = false;
                break;
            }
        }

        if (isPipelineEmpty) {
            System.out.println("All instructions have been processed. Stopping CPU.");
            printRegisters();
            running = false;
        }
    }

    private void printRegisters() {
        System.out.println("Final Register States:");
        for (Map.Entry<String, Integer> entry : registers.entrySet()) {
            System.out.println("Register " + entry.getKey() + ": " + entry.getValue());
        }
    }

    private void fetch() {
        if (!stages[PipelineStage.FETCH.ordinal()].isEmpty()) {
            Instruction instruction = stages[PipelineStage.FETCH.ordinal()].poll();
            System.out.println("Fetching: " + instruction);
            stages[PipelineStage.DECODE.ordinal()].add(instruction);
            programCounter++;
        }
    }

    private void decode() {
        if (!stages[PipelineStage.DECODE.ordinal()].isEmpty()) {
            Instruction instruction = stages[PipelineStage.DECODE.ordinal()].peek();

            forwardOperands(instruction);

            instruction = stages[PipelineStage.DECODE.ordinal()].poll();
            System.out.println("Decoding: " + instruction);
            stages[PipelineStage.EXECUTE.ordinal()].add(instruction);
        }
    }

    private void execute() {
        if (!stages[PipelineStage.EXECUTE.ordinal()].isEmpty()) {
            Instruction instruction = stages[PipelineStage.EXECUTE.ordinal()].poll();
            System.out.println("Executing: " + instruction);

            String op = instruction.getOperation();
            String[] operands = instruction.getOperands();
            int result;

            switch (op) {
                case "lw":
                    String address = operands[1];
                    result = cache.readFromCache(address);
                    instruction.setResult(result);
                    break;
                case "sw":
                    address = operands[1];
                    int value = registers.getOrDefault(operands[0], 0);
                    cache.writeToCache(address, value);
                    break;
                case "add":
                    result = registers.getOrDefault(operands[1], 0) + registers.getOrDefault(operands[2], 0);
                    instruction.setResult(result);
                    break;
                case "sub":
                    result = registers.getOrDefault(operands[1], 0) - registers.getOrDefault(operands[2], 0);
                    instruction.setResult(result);
                    break;
                case "and":
                    result = registers.getOrDefault(operands[1], 0) & registers.getOrDefault(operands[2], 0);
                    instruction.setResult(result);
                    break;
                case "or":
                    result = registers.getOrDefault(operands[1], 0) | registers.getOrDefault(operands[2], 0);
                    instruction.setResult(result);
                    break;
                case "jmp":
                    programCounter = Integer.parseInt(operands[0].substring(2), 16);
                    break;
                case "beq":
                    if (registers.getOrDefault(operands[0], 0).equals(registers.getOrDefault(operands[1], 0))) {
                        programCounter = Integer.parseInt(operands[2]);
                    }
                    break;
                case "bne":
                    if (!registers.getOrDefault(operands[0], 0).equals(registers.getOrDefault(operands[1], 0))) {
                        programCounter = Integer.parseInt(operands[2]);
                    }
                    break;
                case "addi":
                    result = registers.getOrDefault(operands[1], 0) + Integer.parseInt(operands[2]);
                    instruction.setResult(result);
                    registers.put(operands[0], result);
                    break;
                default:
                    System.out.println("Instruction not valid: " + op);
                    break;
            }

            if (op.equals("add") || op.equals("sub") || op.equals("and") || op.equals("or")) {
                registers.put(operands[0], instruction.getResult());
            }

            stages[PipelineStage.MEMORY.ordinal()].add(instruction);
        }
    }


    private void memoryAccess() {
        if (!stages[PipelineStage.MEMORY.ordinal()].isEmpty()) {
            Instruction instruction = stages[PipelineStage.MEMORY.ordinal()].poll();
            System.out.println("Memory Access: " + instruction);
            handleMemoryOperation(instruction);
            stages[PipelineStage.WRITE_BACK.ordinal()].add(instruction);
        }
        isMemoryUnitBusy = false;
    }

    private void writeBack() {
        if (!stages[PipelineStage.WRITE_BACK.ordinal()].isEmpty()) {
            Instruction instruction = stages[PipelineStage.WRITE_BACK.ordinal()].poll();
            System.out.println("Write Back: " + instruction);
            if (instruction.getOperation().equals("add") || instruction.getOperation().equals("lw")) {
                registers.put(instruction.getOperands()[0], instruction.getResult());
            }
        }
    }

    private void insertStall(int stageIndex) {
        stages[stageIndex].add(NOP);
        System.out.println("Stall inserted at stage " + stageIndex + " due to a data hazard.");
    }

    private void handleMemoryOperation(Instruction instruction) {
        String op = instruction.getOperation();
        String[] operands = instruction.getOperands();

        switch (op) {
            case "lw":
                int data = cache.readFromCache(operands[1]);
                instruction.setResult(data);
                break;
            case "sw":
                cache.writeToCache(operands[1], registers.getOrDefault(operands[0], 0));
                break;
        }
    }

    public Map<String, Integer> getRegisters() {
        return registers;
    }

    private void forwardOperands(Instruction instruction) {
        String[] operands = instruction.getOperands();
        for (int i = 0; i < operands.length; i++) {
            Instruction executeStageInstruction = stages[PipelineStage.EXECUTE.ordinal()].peek();
            if (executeStageInstruction != null && executeStageInstruction.getOperands().length > 0 && executeStageInstruction.getOperands()[0].equals(operands[i])) {
                System.out.println("Forwarding result from EXECUTE to DECODE for operand " + operands[i]);
                operands[i] = String.valueOf(executeStageInstruction.getResult());
            }
        }
    }

}
