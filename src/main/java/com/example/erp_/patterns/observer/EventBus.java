package com.example.erp_.patterns.observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {
    private static EventBus instance;
    private final Map<String, CopyOnWriteArrayList<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    public static synchronized EventBus getInstance() {
        if (instance == null) instance = new EventBus();
        return instance;
    }

    public void publish(String topic, Object payload) {
        var list = listeners.get(topic);
        if (list != null) {
            for (Consumer<Object> c : list) {
                try { c.accept(payload); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    public void subscribe(String topic, Consumer<Object> handler) {
        listeners.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public void unsubscribe(String topic, Consumer<Object> handler) {
        var list = listeners.get(topic);
        if (list != null) list.remove(handler);
    }
}

