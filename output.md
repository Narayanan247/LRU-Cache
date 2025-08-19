# Sample Run of LRU Cache

- Initial:        [2=20, 1=10]
- get(1) => 10
- After get(1):   [1=10, 2=20]
- After put(3):   [3=30, 1=10]
- get(2) => -1
- After put(4):   [4=40, 3=30]
- get(1) => -1
- get(3) => 30
- get(4) => 40
- Hits=3 Misses=2 Evicted=[2, 1]
- After resize+adds: [6=60, 5=50, 4=40]
- After shrink:      [6=60, 5=50]
- EvictionLog:       [2, 1, 3, 4]
- After update 4:    [4=400, 6=60]
- get(4) => 400

