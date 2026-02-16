package tracker.HistoryManager;

import tracker.issue.ReadableIssue;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    static class Node<T extends ReadableIssue> {
        private final T data;
        private Node<T> next;
        private Node<T> prev;

        public Node(T issue, Node<T> next, Node<T> prev) {
            data = issue;
            this.next = next;
            this.prev = prev;
        }
    }

    static class InternalLinkedList<T extends ReadableIssue> {
        private Node<T> head;
        private Node<T> tail;

        InternalLinkedList() {
            head = null;
            tail = null;
        }

        Node<T> linkLast(T issue) {
            Node<T> newNode = new Node<>(issue, null, tail);
            if (tail != null) {
                tail.next = newNode;
                tail = newNode;
            } else {
                head = tail = newNode;
            }

            return newNode;
        }

        void remove(Node<T> node) {
            Node<T> prev = node.prev;
            Node<T> next = node.next;

            if (prev == null && next == null) {
                head = tail = null;
                return;
            }

            if (prev != null) {
                prev.next = next;
            } else {
                head = head.next;
            }

            if (next != null) {
                next.prev = prev;
            } else {
                tail = tail.prev;
            }
        }

        List<ReadableIssue> asList() {
            List<ReadableIssue> result = new ArrayList<>();
            Node<T> curr = head;
            while (curr != null) {
                result.add(curr.data);
                curr = curr.next;
            }
            return result;
        }
    }

    private final HashMap<Integer, Node<ReadableIssue>> idToNode;
    private final InternalLinkedList<ReadableIssue> list;

    public InMemoryHistoryManager() {
        this.idToNode = new HashMap<>();
        this.list = new InternalLinkedList<>();
    }

    @Override
    public void add(ReadableIssue issue) {
        if (issue == null) {
            return;
        }

        if (idToNode.containsKey(issue.getId())) {
            Node<ReadableIssue> node = idToNode.remove(issue.getId());
            removeNode(node);
        }

        Node<ReadableIssue> node = list.linkLast(issue);
        idToNode.put(issue.getId(), node);
    }

    @Override
    public void remove(int id) {
        if (!idToNode.containsKey(id)) {
            return;
        }

        Node<ReadableIssue> node = idToNode.remove(id);
        removeNode(node);
    }

    @Override
    public List<ReadableIssue> getHistory() {
        return list.asList();
    }

    private void removeNode(Node<ReadableIssue> node) {
        list.remove(node);
    }
}