package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class BSTNode {
        BSTNode left;
        BSTNode right;
        K key;
        V val;

        BSTNode() {
            this.key = null;
            this.val = null;
            this.left = null;
            this.right = null;
        }
        BSTNode(K key, V val) {
            this.key = key;
            this.val = val;
            this.left = null;
            this.right = null;
        }
    }

    private int size;
    private BSTNode root;


    @Override
    public void clear() {
        root = null;
        size = 0;
    }
    @Override
    public boolean containsKey(K key) {
        BSTNode temp = root;
        while (temp != null) {
            if (temp.key.equals(key)) {
                return true;
            }
            temp = key.compareTo(temp.key) < 0 ? temp.left : temp.right;
        }
        return false;
    }
    @Override
    public V get(K key) {
        BSTNode temp = root;
        while (temp != null) {
            if (temp.key.equals(key)) {
                return temp.val;
            }
            temp = key.compareTo(temp.key) < 0 ? temp.left : temp.right;
        }
        return null;
    }
    @Override
    public int size() {
        return size;
    }
    @Override
    public void put(K key, V value) {
        BSTNode newNode = new BSTNode(key, value);
        if (containsKey(key)) {
            return;
        }
        if (root == null) {
            root = newNode;
            size++;
            return;
        }
        BSTNode temp = root, insertPos = temp;
        int flag = 0; // 0 left, 1 right
        while (temp != null) {
            insertPos = temp;
            if (key.compareTo(temp.key) < 0) {
                temp = temp.left;
                flag = 0;
            } else {
                temp = temp.right;
                flag = 1;
            }
        }
        if (flag == 0) {
            insertPos.left = newNode;
        } else {
            insertPos.right = newNode;
        }
        size++;
    }

    private void addKeys(BSTNode node, Set<K> set) {
        //inOrder Traverse
        if (node != null) {
            addKeys(node.left, set);
            set.add(node.key);;
            addKeys(node.right, set);
        }
    }
    @Override
    public Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        addKeys(root, set);
        return set;
    }

    private boolean isLeft(BSTNode prev, BSTNode cur) {
        return cur.key.compareTo(prev.key) < 0;
    }

    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        BSTNode cur = root, prev = cur, successor = null, suc_prev = successor;
        while (cur.key != key) {
            prev = cur;
            cur = key.compareTo(cur.key) < 0 ? cur.left : cur.right;
        }
        V res = cur.val;
        if (cur.right == null) {
            if (cur == root) {
                root = cur.left;
            } else {
                if (isLeft(prev, cur)) {
                    prev.left = cur.left;
                } else {
                    prev.right = cur.left;
                }
            }
        } else {
            //find the successor
            suc_prev = cur;
            successor = cur.right;
            while (successor.left != null) {
                suc_prev = successor;
                successor = successor.left;
            }
            if (cur == root) {
                cur.key = successor.key;
            } else {
                prev.key = successor.key;
            }
            //deletes the successor
            if (isLeft(suc_prev, successor)) {
                suc_prev.left = null;
            } else {
                suc_prev.right = successor.right;
            }
        }
        size--;
        return res;
    }
    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        }
        BSTNode cur = root, prev = cur, successor = null, suc_prev = successor;
        while (cur.key != key) {
            prev = cur;
            cur = key.compareTo(cur.key) < 0 ? cur.left : cur.right;
        }
        V res = cur.val;
        if (res != value) {
            return null;
        }
        if (cur.right == null) {
            if (isLeft(prev, cur)) {
                prev.left = cur.left;
            } else {
                prev.right = cur.left;
            }
        } else {
            //find the successor
            suc_prev = cur;
            successor = cur.right;
            while (successor.left != null) {
                suc_prev = successor;
                successor = successor.left;
            }
            if (cur == root) {
                cur.key = successor.key;
            } else {
                prev.key = successor.key;
            }
            //deletes the successor
            if (isLeft(suc_prev, successor)) {
                suc_prev.left = null;
            } else {
                suc_prev.right = successor.right;
            }
        }
        size--;
        return res;
    }
    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    private void InOrderTraverse(BSTNode node) {
        if (node != null) {
            InOrderTraverse(node.left);
            System.out.println(node.val);
            InOrderTraverse(node.right);
        }
    }

    public void printInOrder() {
        //prints the BSTMap in order of increasing key
        //inorder traverse
        InOrderTraverse(root);
    }
}
