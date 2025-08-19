# 🗂️ LRU Cache in Java

An implementation of **LRU (Least Recently Used) Cache** in Java, built for understanding caching mechanisms and real-world applications.
The cache supports `get`, `put`, `resize`, and eviction logging with **O(1) operations** using **HashMap + Doubly Linked List**.

---

## 🚀 Features
- 📌 **O(1) Time Complexity** for `get` and `put`
- 🔄 **Automatic Eviction** of least recently used keys
- 📊 **Cache Statistics**: hits, misses, evicted keys
- ⚡ **Dynamic Resizing** of cache capacity
- 🧪 **Test Cases Included**


---

## ⚙️ How It Works
- A **HashMap** provides O(1) lookup for keys.
- A **Doubly Linked List** maintains the order of usage:
  - Most recently used at the head
  - Least recently used at the tail
- On `get(key)`, the key is moved to the front.
- On `put(key, value)`, if the cache is full, the LRU item is evicted.

---

