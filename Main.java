import java.util.*;

/**
 * Extended LRU Cache demo:
 * - O(1) get/put using HashMap + Doubly Linked List
 * - Hit/Miss counters
 * - Eviction log (which keys got evicted)
 * - Dynamic resize()
 *
 * Usage notes:
 * - Most recently used (MRU) item sits near the head
 * - Least recently used (LRU) item sits near the tail
 */
public class Main {
    public static void main(String[] args) {
        // ===== Demo 1: Core LRU behavior =====
        LRUCache cache = new LRUCache(2);
        cache.put(1, 10);
        cache.put(2, 20);
        System.out.println("Initial:        " + cache); // [2=20, 1=10]

        System.out.println("get(1) => " + cache.get(1)); // 10 (hit)
        System.out.println("After get(1):   " + cache); // [1=10, 2=20]  (1 becomes MRU)

        cache.put(3, 30); // evicts key 2 (LRU)
        System.out.println("After put(3):   " + cache); // [3=30, 1=10]
        System.out.println("get(2) => " + cache.get(2)); // -1 (miss)

        cache.put(4, 40); // evicts key 1
        System.out.println("After put(4):   " + cache); // [4=40, 3=30]

        System.out.println("get(1) => " + cache.get(1)); // -1 (evicted)
        System.out.println("get(3) => " + cache.get(3)); // 30
        System.out.println("get(4) => " + cache.get(4)); // 40

        System.out.println("Hits=" + cache.getHitCount() +
                " Misses=" + cache.getMissCount() +
                " Evicted=" + cache.getEvictionLog());

        // ===== Demo 2: Dynamic resize =====
        cache.resize(3); // expand capacity
        cache.put(5, 50);
        cache.put(6, 60);
        System.out.println("After resize+adds: " + cache); // [6=60, 5=50, 4=40]

        cache.resize(2); // shrink capacity -> will evict LRU until size fits
        System.out.println("After shrink:      " + cache);
        System.out.println("EvictionLog:       " + cache.getEvictionLog());

        // ===== Demo 3: Update existing key keeps O(1) and MRU move =====
        cache.put(4, 400); // update value of existing key 4 if present
        System.out.println("After update 4:    " + cache);
        System.out.println("get(4) => " + cache.get(4));
    }
}

/**
 * LRUCache with:
 * - O(1) get/put
 * - hit/miss counters
 * - eviction log
 * - dynamic resize
 *
 * Implementation details:
 * - Doubly Linked List (with dummy head/tail) maintains recency order:
 *   head <-> MRU ... LRU <-> tail
 * - HashMap maps key -> Node reference (for O(1) access/moves)
 */
class LRUCache {
    // Doubly Linked List node
    private static class Node {
        int key;
        int value;
        Node prev, next;
        Node(int k, int v) { key = k; value = v; }
    }

    private final Map<Integer, Node> map;
    private final List<Integer> evictionLog;
    private Node head, tail;      // sentinels
    private int capacity;
    private int size;
    private long hits;
    private long misses;

    public LRUCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be >= 1");
        }
        this.capacity = capacity;
        this.size = 0;
        this.map = new HashMap<>();
        this.evictionLog = new ArrayList<>();
        initList();
    }

    // Create dummy head/tail and link them
    private void initList() {
        head = new Node(0, 0);
        tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    // Public API

    /** O(1) get: returns value or -1 if not present */
    public int get(int key) {
        Node node = map.get(key);
        if (node == null) {
            misses++;
            return -1;
        }
        // Move node to MRU position
        moveToHead(node);
        hits++;
        return node.value;
    }

    /** O(1) put: insert/update; evict LRU if needed */
    public void put(int key, int value) {
        Node node = map.get(key);
        if (node != null) {
            // update value + move to MRU
            node.value = value;
            moveToHead(node);
            return;
        }

        // new insert
        Node fresh = new Node(key, value);
        map.put(key, fresh);
        addToHead(fresh);
        size++;

        if (size > capacity) {
            Node lru = popTail();          // remove least recently used
            map.remove(lru.key);
            evictionLog.add(lru.key);
            size--;
        }
    }
    private int removeLast() {
        Node lru = tail.prev;
        removeNode(lru);   // unlink from DLL
        return lru.key;
    }


    /** Change capacity at runtime; evict LRU items if shrinking */
    public void resize(int newCapacity) {
        this.capacity = newCapacity;

        // While size > capacity, evict LRU
        while (map.size() > capacity) {
            int lruKey = removeLast();   // use your own helper
            map.remove(lruKey);
            evictionLog.add(lruKey);         // keep track of evicted keys
        }
    }


    /** Stats & helpers */
    public long getHitCount()   { return hits; }
    public long getMissCount()  { return misses; }
    public int  size()          { return size; }
    public int  capacity()      { return capacity; }
    public List<Integer> getEvictionLog() {
        // return a snapshot to avoid external modification
        return Collections.unmodifiableList(new ArrayList<>(evictionLog));
    }
    public void clearEvictionLog() { evictionLog.clear(); }

    // ===== Doubly Linked List ops (all O(1)) =====
    private void addToHead(Node node) {
        node.prev = head;
        node.next = head.next;

        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node node) {
        Node p = node.prev;
        Node n = node.next;
        p.next = n;
        n.prev = p;
        node.prev = node.next = null; // help GC
    }

    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }

    /** Remove LRU (node before tail) */
    private Node popTail() {
        Node lru = tail.prev;
        if (lru == head) return null; // shouldn't happen when size>0
        removeNode(lru);
        return lru;
    }

    /** For debugging/printing: MRU -> ... -> LRU */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        Node cur = head.next;
        boolean first = true;
        while (cur != null && cur != tail) {
            if (!first) sb.append(", ");
            sb.append(cur.key).append('=').append(cur.value);
            first = false;
            cur = cur.next;
        }
        sb.append(']');
        return sb.toString();
    }
}
