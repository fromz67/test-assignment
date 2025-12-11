/*
 * Custom implementation of NumberList interface.
 * Circular doubly linked list of digits in a positional numeral system.
 * Author: Vlad Tymchuk, group IA-34, 20
 */

package ua.kpi.comsys.test2.implementation;

import ua.kpi.comsys.test2.NumberList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class NumberListImpl implements NumberList {

    /**
     * Internal element of the circular doubly linked list.
     * Stores a single digit in the current numeral system (0..base-1).
     */
    private static class Node {
        byte digit;
        Node next;
        Node prev;

        Node(byte digit) {
            this.digit = digit;
        }
    }

    /** Head of the circular list (most significant digit). */
    private Node head;
    /** Number of digits in the list. */
    private int size;
    /** Current numeral base used to represent digits in the list. */
    private int base;
    /** Decimal value of the number (single source of truth). */
    private BigInteger value;

    private static final int DEFAULT_BASE = 2;

    /**
     * Initializes an empty list.
     */
    private void initEmpty() {
        head = null;
        size = 0;
        base = DEFAULT_BASE;
        value = BigInteger.ZERO;
    }

    /**
     * Builds a digit list from the {@link #value} field in the current base {@link #base}.
     * The value 0 is stored as an empty list (size == 0).
     */
    private void buildDigitsFromValue() {
        head = null;
        size = 0;
        if (value == null || value.signum() == 0) {
            return;
        }

        String repr = value.toString(base);
        for (int i = 0; i < repr.length(); i++) {
            char c = repr.charAt(i);
            byte d = (byte) (c - '0'); // 0..2
            appendDigitInternal(d);
        }
    }

    /**
     * Appends a digit to the end of the list (used only when building from value,
     * without recomputing value).
     */
    private void appendDigitInternal(byte digit) {
        Node node = new Node(digit);
        if (head == null) {
            head = node;
            head.next = head;
            head.prev = head;
            size = 1;
        } else {
            Node tail = head.prev;
            tail.next = node;
            node.prev = tail;
            node.next = head;
            head.prev = node;
            size++;
        }
    }

    /**
     * Recomputes {@link #value} from the current digits in the list.
     */
    private void recalcValueFromDigits() {
        if (size == 0) {
            value = BigInteger.ZERO;
            return;
        }
        BigInteger v = BigInteger.ZERO;
        BigInteger b = BigInteger.valueOf(base);
        Node curr = head;
        for (int i = 0; i < size; i++) {
            int d = curr.digit;
            v = v.multiply(b).add(BigInteger.valueOf(d));
            curr = curr.next;
        }
        value = v;
    }

    /**
     * Initializes the list from a decimal string.
     * If the string is invalid (contains non-digits or a minus sign),
     * the list remains empty.
     */
    private void initFromDecimalString(String s) {
        initEmpty();
        if (s == null) {
            return;
        }
        s = s.trim();
        if (s.isEmpty()) {
            return;
        }
        if (s.startsWith("-") || !s.matches("\\d+")) {
            return;
        }
        value = new BigInteger(s);
        if (value.signum() == 0) {
            return;
        }
        base = DEFAULT_BASE;
        buildDigitsFromValue(); // digits in current base
    }

    /**
     * Default constructor – creates an empty list.
     */
    public NumberListImpl() {
        initEmpty();
    }

    /**
     * File-based constructor: the file contains a decimal number (single line).
     * If the file is missing / empty / invalid, the list is empty.
     */
    public NumberListImpl(File file) {
        initEmpty();
        if (file == null || !file.exists()) {
            return;
        }
        try {
            String s = Files.readString(file.toPath());
            initFromDecimalString(s);
        } catch (IOException e) {
            initEmpty();
        }
    }

    /**
     * Constructor from a decimal string.
     */
    public NumberListImpl(String value) {
        initFromDecimalString(value);
    }

    /**
     * Internal constructor for changing the numeral system without reparsing from string.
     */
    private NumberListImpl(BigInteger decimalValue, int base) {
        initEmpty();
        this.base = base;
        this.value = (decimalValue == null) ? BigInteger.ZERO : decimalValue;
        if (this.value.signum() != 0) {
            buildDigitsFromValue();
        }
    }

    /**
     * Saves the number into a file in decimal notation.
     */
    public void saveList(File file) {
        if (file == null) return;
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(toDecimalString());
        } catch (IOException e) {
            throw new RuntimeException("Cannot save number to file", e);
        }
    }

    public static int getRecordBookNumber() {
        return 4220;
    }

    /**
     * Changes numeral system. For this variant:
     * binary → ternary.
     */
    public NumberListImpl changeScale() {
        int variant = getRecordBookNumber() % 5;
        if (variant == 0) {
            return new NumberListImpl(this.value, 3);
        }
        switch (variant) {
            case 1:
                return new NumberListImpl(this.value, 8);
            case 2:
                return new NumberListImpl(this.value, 10);
            case 3:
                return new NumberListImpl(this.value, 16);
            case 4:
                return new NumberListImpl(this.value, 2);
            default:
                return new NumberListImpl(this.value, this.base);
        }
    }

    /**
     * Additional operation – for this variant it is algebraic and logical OR.
     * Here logical (bitwise) OR on the numbers is implemented.
     * The operands are not modified; a new list is returned.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        if (!(arg instanceof NumberListImpl)) {
            throw new IllegalArgumentException("Unsupported NumberList implementation");
        }
        NumberListImpl other = (NumberListImpl) arg;

        int variant = getRecordBookNumber() % 7;
        BigInteger a = this.value;
        BigInteger b = other.value;
        BigInteger res;

        if (variant == 6) {
            res = a.or(b);
        } else {
            switch (variant) {
                case 0:
                    res = a.add(b);
                    break;
                case 1:
                    res = a.subtract(b);
                    break;
                case 2:
                    res = a.multiply(b);
                    break;
                case 3:
                    if (b.equals(BigInteger.ZERO)) {
                        throw new ArithmeticException("Division by zero");
                    }
                    res = a.divide(b);
                    break;
                case 4:
                    if (b.equals(BigInteger.ZERO)) {
                        throw new ArithmeticException("Modulo by zero");
                    }
                    res = a.mod(b);
                    break;
                case 5:
                    res = a.and(b);
                    break;
                default:
                    res = a.or(b);
            }
        }

        return new NumberListImpl(res.toString());
    }

    /**
     * Decimal string representation of the number.
     */
    public String toDecimalString() {
        if (value == null) return "0";
        return value.toString();
    }

    /**
     * String representation in the current numeral system (base).
     * Used in tests as:
     *  - binary string (BINARY) for the initial list
     *  - ternary string (TERNARY) for the result of changeScale()
     */
    @Override
    public String toString() {
        if (value == null || value.signum() == 0) {
            return (size == 0) ? "0" : "0";
        }
        return value.toString(base);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberListImpl other)) return false;
        return this.toDecimalString().equals(other.toDecimalString());
    }

    @Override
    public int hashCode() {
        return toDecimalString().hashCode();
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Byte)) return false;
        byte target = (Byte) o;
        Node curr = head;
        for (int i = 0; i < size; i++) {
            if (curr.digit == target) return true;
            curr = curr.next;
        }
        return false;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private Node curr = head;
            private int visited = 0;

            @Override
            public boolean hasNext() {
                return visited < size;
            }

            @Override
            public Byte next() {
                byte d = curr.digit;
                curr = curr.next;
                visited++;
                return d;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        Node curr = head;
        for (int i = 0; i < size; i++) {
            arr[i] = curr.digit;
            curr = curr.next;
        }
        return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            throw new UnsupportedOperationException("Generic toArray is not supported");
        }
        Node curr = head;
        int i = 0;
        while (i < size) {
            @SuppressWarnings("unchecked")
            T val = (T) Byte.valueOf(curr.digit);
            a[i++] = val;
            curr = curr.next;
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    private Node nodeAt(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("index: " + index);
        Node curr;
        if (index < size / 2) {
            curr = head;
            for (int i = 0; i < index; i++) {
                curr = curr.next;
            }
        } else {
            curr = head.prev;
            for (int i = size - 1; i > index; i--) {
                curr = curr.prev;
            }
        }
        return curr;
    }

    @Override
    public boolean add(Byte e) {
        if (e == null) throw new NullPointerException("Digit cannot be null");
        if (e < 0 || e >= base)
            throw new IllegalArgumentException("Digit " + e + " is out of range for base " + base);

        add(size, e);
        return true;
    }

    @Override
    public void add(int index, Byte element) {
        if (element == null) throw new NullPointerException("Digit cannot be null");
        if (element < 0 || element >= base)
            throw new IllegalArgumentException("Digit " + element + " is out of range for base " + base);

        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("index: " + index);

        Node node = new Node(element);
        if (size == 0) {
            head = node;
            head.next = head;
            head.prev = head;
            size = 1;
        } else if (index == size) {
            Node tail = head.prev;
            tail.next = node;
            node.prev = tail;
            node.next = head;
            head.prev = node;
            size++;
        } else if (index == 0) {
            Node tail = head.prev;
            node.next = head;
            node.prev = tail;
            tail.next = node;
            head.prev = node;
            head = node;
            size++;
        } else {
            Node curr = nodeAt(index);
            Node prev = curr.prev;
            prev.next = node;
            node.prev = prev;
            node.next = curr;
            curr.prev = node;
            size++;
        }
        recalcValueFromDigits();
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Byte)) return false;
        byte target = (Byte) o;
        if (size == 0) return false;

        Node curr = head;
        for (int i = 0; i < size; i++) {
            if (curr.digit == target) {
                removeNode(curr);
                recalcValueFromDigits();
                return true;
            }
            curr = curr.next;
        }
        return false;
    }

    private void removeNode(Node node) {
        if (size == 1) {
            head = null;
            size = 0;
            return;
        }
        Node prev = node.prev;
        Node next = node.next;
        prev.next = next;
        next.prev = prev;
        if (node == head) {
            head = next;
        }
        size--;
    }

    @Override
    public Byte remove(int index) {
        Node node = nodeAt(index);
        byte d = node.digit;
        removeNode(node);
        recalcValueFromDigits();
        return d;
    }

    @Override
    public void clear() {
        initEmpty();
    }

    @Override
    public Byte get(int index) {
        return nodeAt(index).digit;
    }

    @Override
    public Byte set(int index, Byte element) {
        if (element == null) throw new NullPointerException("Digit cannot be null");
        if (element < 0 || element >= base)
            throw new IllegalArgumentException("Digit " + element + " is out of range for base " + base);
        Node node = nodeAt(index);
        byte old = node.digit;
        node.digit = element;
        recalcValueFromDigits();
        return old;
    }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        byte target = (Byte) o;
        Node curr = head;
        for (int i = 0; i < size; i++) {
            if (curr.digit == target) return i;
            curr = curr.next;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte)) return -1;
        byte target = (Byte) o;
        if (size == 0) return -1;
        Node curr = head.prev;
        for (int i = size - 1; i >= 0; i--) {
            if (curr.digit == target) return i;
            curr = curr.prev;
        }
        return -1;
    }

    @Override
    public ListIterator<Byte> listIterator() {
        throw new UnsupportedOperationException("listIterator not implemented");
    }

    @Override
    public ListIterator<Byte> listIterator(int index) {
        throw new UnsupportedOperationException("listIterator not implemented");
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("subList not implemented");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c == null) return true;
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        if (c == null || c.isEmpty()) return false;
        for (Byte b : c) {
            add(b);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        if (c == null || c.isEmpty()) return false;
        int idx = index;
        for (Byte b : c) {
            add(idx++, b);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c == null || c.isEmpty()) return false;
        boolean changed = false;
        for (Object o : c) {
            while (remove(o)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (c == null) {
            clear();
            return true;
        }
        boolean changed = false;
        Node curr = head;
        for (int i = 0; i < size; ) {
            if (!c.contains(curr.digit)) {
                Node toRemove = curr;
                curr = curr.next;
                removeNode(toRemove);
                changed = true;
            } else {
                curr = curr.next;
                i++;
            }
        }
        if (changed) recalcValueFromDigits();
        return changed;
    }


    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) return false;
        if (index1 == index2) return true;
        Node n1 = nodeAt(index1);
        Node n2 = nodeAt(index2);
        byte tmp = n1.digit;
        n1.digit = n2.digit;
        n2.digit = tmp;
        recalcValueFromDigits();
        return true;
    }

    @Override
    public void sortAscending() {
        if (size < 2) return;
        boolean swapped;
        do {
            swapped = false;
            Node curr = head;
            for (int i = 0; i < size - 1; i++) {
                if (curr.digit > curr.next.digit) {
                    byte tmp = curr.digit;
                    curr.digit = curr.next.digit;
                    curr.next.digit = tmp;
                    swapped = true;
                }
                curr = curr.next;
            }
        } while (swapped);
        recalcValueFromDigits();
    }

    @Override
    public void sortDescending() {
        if (size < 2) return;
        boolean swapped;
        do {
            swapped = false;
            Node curr = head;
            for (int i = 0; i < size - 1; i++) {
                if (curr.digit < curr.next.digit) {
                    byte tmp = curr.digit;
                    curr.digit = curr.next.digit;
                    curr.next.digit = tmp;
                    swapped = true;
                }
                curr = curr.next;
            }
        } while (swapped);
        recalcValueFromDigits();
    }

    @Override
    public void shiftLeft() {
        if (size > 0) {
            head = head.next;
            recalcValueFromDigits();
        }
    }

    @Override
    public void shiftRight() {
        if (size > 0) {
            head = head.prev;
            recalcValueFromDigits();
        }
    }
}
