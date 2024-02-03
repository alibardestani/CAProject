import java.util.HashMap;
import java.util.Map;

public class Cache {
    private CacheBlock[][] blocks;
    private final int setSize;
    private final int ways;
    private Map<String, Integer> simulatedMemory;

    public Cache(int setSize, int ways) {
        this.setSize = setSize;
        this.ways = ways;
        blocks = new CacheBlock[setSize][ways];
        simulatedMemory = new HashMap<>();
        initializeSimulatedMemory();
        for (int i = 0; i < setSize; i++) {
            for (int j = 0; j < ways; j++) {
                blocks[i][j] = new CacheBlock(CacheBlock.State.INVALID, "", 0);
            }
        }
    }

    private void initializeSimulatedMemory() {
        simulatedMemory.put("0x100", 42);
        simulatedMemory.put("0x104", 84);
        simulatedMemory.put("0x108", 30);
    }

    public int readFromCache(String address) {
        int setIndex = getSetIndex(address);
        for (int i = 0; i < ways; i++) {
            CacheBlock block = blocks[setIndex][i];
            if (block.getAddress().equals(address) && block.getState() != CacheBlock.State.INVALID) {
                if (block.getState() != CacheBlock.State.SHARED) {
                    block.setState(CacheBlock.State.SHARED);
                    printStateChange(block, "SHARED");
                }
                return block.getData();
            }
        }
        return handleCacheMiss(address, setIndex);
    }

    public void writeToCache(String address, int data) {
        int setIndex = getSetIndex(address);
        for (int i = 0; i < ways; i++) {
            CacheBlock block = blocks[setIndex][i];
            if (block.getAddress().equals(address)) {
                block.setData(data);
                if (block.getState() != CacheBlock.State.MODIFIED) {
                    block.setState(CacheBlock.State.MODIFIED);
                    printStateChange(block, "MODIFIED");
                }
                return;
            }
        }
        loadToCache(address, setIndex, data, true);
    }

    private int handleCacheMiss(String address, int setIndex) {
        int data = readFromMemory(address);
        loadToCache(address, setIndex, data, false);
        return data;
    }

    private void loadToCache(String address, int setIndex, int data, boolean isWrite) {
        for (int i = 0; i < ways; i++) {
            CacheBlock block = blocks[setIndex][i];
            if (block.getState() == CacheBlock.State.INVALID || i == ways - 1) {
                block.setAddress(address);
                block.setData(data);
                CacheBlock.State newState = isWrite ? CacheBlock.State.MODIFIED : CacheBlock.State.SHARED;
                block.setState(newState);
                printStateChange(block, newState.toString());
                return;
            }
        }
    }

    private int readFromMemory(String address) {
        return simulatedMemory.getOrDefault(address, 0);
    }

    private int getSetIndex(String address) {
        return Math.abs(address.hashCode()) % setSize;
    }

    private void printStateChange(CacheBlock block, String newState) {
        System.out.println("Cache Block Update: Address " + block.getAddress() + " changed to " + newState + " state.");
    }
}
