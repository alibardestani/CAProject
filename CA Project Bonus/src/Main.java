//public class Main {
//    public static void main(String[] args) {
//        final ClockManager clockManager;
//        clockManager = new ClockManager(0);
//        CPU cpu = new CPU(1,clockManager);
//
//        cpu.getRegisters().put("R1", 5);
//        cpu.getRegisters().put("R2", 10);
//        cpu.getRegisters().put("R3", 0);
//        cpu.getRegisters().put("R4", 0);
//        cpu.getRegisters().put("R5", 0);
//
//
//        cpu.loadInstruction(new Instruction("0x001", "add", new String[]{"R3", "R1", "R2"}));
//
//        cpu.loadInstruction(new Instruction("0x002", "lw", new String[]{"R4", "0x100"}));
//
//        cpu.loadInstruction(new Instruction("0x003", "lw", new String[]{"R5", "0x108"}));
//
//        cpu.loadInstruction(new Instruction("0x004", "add", new String[]{"R6", "R4", "R5"}));
//
//        cpu.loadInstruction(new Instruction("0x005", "sw", new String[]{"R6", "0x110"}));
//        cpu.loadInstruction(new Instruction("0x006", "lw", new String[]{"R7", "0x110"}));
//
//        cpu.loadInstruction(new Instruction("0x007", "beq", new String[]{"R1", "R2", "0x002"}));
//        cpu.loadInstruction(new Instruction("0x008", "jmp", new String[]{"0x004"}));
//
//        cpu.start();
//    }
//}
