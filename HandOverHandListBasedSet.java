package linkedlists.lockbased;

import contention.abstractions.CompositionalSortedSet;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import contention.abstractions.AbstractCompositionalIntSet;

public class HandOverHandListBasedSet extends AbstractCompositionalIntSet {

    // sentinel nodes
    private Node head;
    private Node tail;
    private Lock lock = new ReentrantLock();

    public HandOverHandListBasedSet(){     
	    head = new Node(Integer.MIN_VALUE);
	    tail = new Node(Integer.MAX_VALUE);
        head.next = tail;
    }
    
    /*
     * Insert
     * 
     * @see contention.abstractions.CompositionalIntSet#addInt(int)
     */
    @Override
    public boolean addInt(int item){
            int key = item;
            head.lock.lock();
            Node pred = head;
            try {
                Node curr = pred.next;
                curr.lock.lock();
                try {
                    while (curr.key < key) {
                        pred.lock.unlock();
                        pred = curr;
                        curr = curr.next;
                        curr.lock.lock();
                    }
                    if (curr.key == key) {
                        return false;
                    }
                    Node newNode = new Node(item);
                    newNode.next = curr;
                    pred.next = newNode;
                    return true;
                } finally {
                    curr.lock.unlock();
                }
            } finally {
                pred.lock.unlock();
            }
    }
    
    /*
     * Remove
     * 
     * @see contention.abstractions.CompositionalIntSet#removeInt(int)
     */
    @Override
    public boolean removeInt(int item){
        Node pred = null, curr = null;
        int key = item;
        head.lock.lock();
        try {
            pred = head;
            curr = pred.next;
            curr.lock.lock();
            try {
                while (curr.key < key) {
                    pred.lock.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                if (curr.key == key) {
                    pred.next = curr.next;
                    return true;
                }
                return false;
            } finally {
                curr.lock.unlock();
            }
        } finally {
            pred.lock.unlock();
        }

    }
    
    /*
     * Contains
     * 
     * @see contention.abstractions.CompositionalIntSet#containsInt(int)
     */
    @Override
    public boolean containsInt(int item) {
        head.lock.lock();
        Node pred = head;
        Node curr = pred.next;
        try {
            curr.lock.lock();
            try {
                while (curr.key < item) {
                    pred.lock.unlock();
                    pred = curr;
                    curr = pred.next;
                    curr.lock.lock();
                }
                return (curr.key == item);
            } finally {
                curr.lock.unlock();
            }
        } finally {
            pred.lock.unlock();
        }
    }

    private class Node {
        Node(int item) {
            key = item;
            next = null;
        }
        public int key;
        public Node next;
        public Lock lock = new ReentrantLock();

    }

    @Override
    public void clear() {
       head = new Node(Integer.MIN_VALUE);
       head.next = new Node(Integer.MAX_VALUE);
    }

    /**
     * Non atomic and thread-unsafe
     */
    @Override
    public int size() {
        int count = 0;

        Node curr = head.next;
        while (curr.key != Integer.MAX_VALUE) {
            curr = curr.next;
            count++;
        }
        return count;
    }
}
