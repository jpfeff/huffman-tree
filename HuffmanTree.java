/**
 * A binary tree specifically designed to hold character and frequency values for Huffman encoding
 *
 * @author Josh Pfefferkorn
 * Dartmouth CS10, Fall 2020
 *
 * Based off BinaryTree code by
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, Spring 2016, minor updates to testing
 * @author Tim Pierson, Winter 2018, added code to manually build tree in main
 */

public class HuffmanTree<Character, Integer> {
    private HuffmanTree<Character, Integer> left, right;    // children; can be null
    Character character;
    Integer frequency;

    /**
     * Constructs leaf node -- left and right are null
     */
    public HuffmanTree(Character character, Integer frequency) {
        this.character = character;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    /**
     * Constructs inner node
     */
    public HuffmanTree(Character character, Integer frequency, HuffmanTree<Character, Integer> left, HuffmanTree<Character, Integer> right) {
        this.character = character;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    /**
     * Is it an inner node?
     */
    public boolean isInner() {
        return left != null || right != null;
    }

    /**
     * Is it a leaf node?
     */
    public boolean isLeaf() {
        return left == null && right == null;
    }

    /**
     * Does it have a left child?
     */
    public boolean hasLeft() {
        return left != null;
    }

    /**
     * Does it have a right child?
     */
    public boolean hasRight() {
        return right != null;
    }

    /**
     * Same structure and data?
     */
    public boolean equalsTree(HuffmanTree<Character, Integer> t2) {
        if (hasLeft() != t2.hasLeft() || hasRight() != t2.hasRight()) return false;
        if (!character.equals(t2.character) || !frequency.equals(t2.frequency)) return false;
        if (hasLeft() && !left.equalsTree(t2.left)) return false;
        if (hasRight() && !right.equalsTree(t2.right)) return false;
        return true;
    }

    public HuffmanTree<Character, Integer> getLeft() {
        return left;
    }

    public HuffmanTree<Character, Integer> getRight() {
        return right;
    }

    public Character getCharacter() {
        return character;
    }

    public Integer getFrequency() {
        return frequency;
    }

    /**
     * Returns a string representation of the tree
     */
    public String toString() {
        return toStringHelper("");
    }

    /**
     * Recursively constructs a String representation of the tree from this node,
     * starting with the given indentation and indenting further going down the tree
     */
    public String toStringHelper(String indent) {
        String res = indent + character + ", " + frequency + "\n";
        if (hasLeft()) res += left.toStringHelper(indent+"  ");
        if (hasRight()) res += right.toStringHelper(indent+"  ");
        return res;
    }
}