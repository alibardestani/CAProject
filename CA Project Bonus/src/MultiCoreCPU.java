import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiCoreCPU {
    private List<CPU> cores;
    private final ClockManager clockManager;
    private ExecutorService executor = Executors.newFixedThreadPool(2);


    public MultiCoreCPU(int coreCount) {
        cores = new ArrayList<>();
        clockManager = new ClockManager(coreCount);
        executor = Executors.newFixedThreadPool(coreCount);
        for (int i = 0; i < coreCount; i++) {
            cores.add(new CPU(i, clockManager));
        }
    }

    public void startCores() {
        for (CPU core : cores) {
            executor.submit(core::start);
        }
        executor.shutdown();
    }

    public void loadInstructionsToCore(int coreId, List<Instruction> instructions) {
        CPU core = cores.get(coreId);
        instructions.forEach(core::loadInstruction);
    }

    public static void main(String[] args) {
        MultiCoreCPU multiCoreCPU = new MultiCoreCPU(2);

        List<Instruction> core1Instructions = Arrays.asList(
                new Instruction("0x001", "lw", new String[]{"R1", "0x100"}),
                new Instruction("0x002", "lw", new String[]{"R2", "0x104"}),
                new Instruction("0x003", "add", new String[]{"R3", "R1", "R2"}),
                new Instruction("0x004", "sw", new String[]{"R3", "0x110"}),
                new Instruction("0x005", "lw", new String[]{"R4", "0x110"}),
                new Instruction("0x006", "add", new String[]{"R5", "R4", "R1"})
        );

        List<Instruction> core2Instructions = Arrays.asList(
                new Instruction("0x007", "lw", new String[]{"R6", "0x108"}),
                new Instruction("0x008", "sw", new String[]{"R7", "0x100"}),
                new Instruction("0x009", "lw", new String[]{"R7", "0x100"}),
                new Instruction("0x00A", "add", new String[]{"R8", "R6", "R7"}),
                new Instruction("0x00B", "sw", new String[]{"R8", "0x114"}),
                new Instruction("0x00C", "lw", new String[]{"R9", "0x114"})
        );

        multiCoreCPU.loadInstructionsToCore(0, core1Instructions);
        multiCoreCPU.loadInstructionsToCore(1, core2Instructions);

        multiCoreCPU.startCores();
    }

}
