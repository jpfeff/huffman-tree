import java.io.*;
import java.util.*;

/**
 * A class that holds static Huffman Tree encoding methods for each step,
 * combined into master "compress()" and "decompress()" methods at end
 *
 * @author Josh Pfefferkorn
 * Dartmouth CS10, Fall 2020
 */

public class Compressor {

    // instance variables used to store the Huffman code tree and final code map
    static HuffmanTree<Character,Integer> huffmanTree;
    static Map<Character, String> codeMap;

    // used to store the current file being compressed / decompressed
    static final String PATHNAME = "inputs/USConstitution.txt";

    /**
     * Generates a map from a passed-in text file with characters as keys and their frequencies as values
     */

    public static Map<Character, Integer> generateFrequencyMap(String pathName) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(pathName)); // reader for file
        HashMap<Character,Integer> charFrequencies = new HashMap<>(); // map for characters and their keys
        int cur; // tracker for current character
        while ((cur=input.read()) != -1) { // reads from input, storing into cur and proceeding if not at end
            char c = (char)cur; // cast to char
            if (charFrequencies.containsKey(c)) { // if already in map
                int value = charFrequencies.get(c);
                charFrequencies.put(c, value+1); // increment frequency
            }
            else {
                charFrequencies.put(c, 1); // otherwise add to map
            }
        }
        input.close(); // close reader
        return charFrequencies; // return completed map
    }

    /**
     * Creates a single-node tree for each map entry and places those into a priority queue
     */

    public static PriorityQueue generatePriorityQueue(Map<Character,Integer> charFrequencies) {
        // comparator for HuffmanTrees so that they are compatible with Java PriorityQueue operations
        class TreeComparator implements Comparator<HuffmanTree>{
            public int compare(HuffmanTree t1, HuffmanTree t2) {
                if ((int)t1.frequency < (int)t2.frequency) {
                    return -1;
                }
                else if ((int)t1.frequency > (int)t2.frequency) {
                    return 1;
                }
                else return 0;
            }
        }
        Comparator<HuffmanTree> freqCompare = new TreeComparator(); // initialize comparator
        PriorityQueue<HuffmanTree> pq = new PriorityQueue<>(freqCompare); // initialize priority queue
        Iterator<Character> iterator = charFrequencies.keySet().iterator(); // initialize iterator for looping over characters
        while (iterator.hasNext()) {
            char curCharacter = iterator.next(); // store character
            HuffmanTree curTree = new HuffmanTree(curCharacter,charFrequencies.get(curCharacter)); // create tree with character and its frequency
            pq.add(curTree); // add this tree to priority queue
        }
        return pq; // return completed priority queue
    }

    /**
     * Combines trees from the priority queue until left with single tree
     * Characters used most frequently are found towards the top of the tree
     * Characters used less frequently are found deeper down in the tree
     */

    public static HuffmanTree generateHuffmanTree(PriorityQueue<HuffmanTree> pq) {
        // if the text file (and therefore the priority queue) is empty, return null
        if (pq.isEmpty()) {
            return null;
        }
        // if there is only 1 character in the text file
        else if (pq.size() == 1) {
            HuffmanTree leaf = pq.remove(); // make a tree with that character and its frequency
            // add it to a root with an empty character value
            // and a frequency value which equals the one character's frequency
            huffmanTree = new HuffmanTree(null, leaf.frequency, leaf,null);
            return huffmanTree; // return that tree
        }
        while (pq.size() > 1) { // while there are at least 2 trees in the queue to combine
            HuffmanTree t1 = pq.remove(); // remove tree with smallest frequency
            HuffmanTree t2 = pq.remove(); // remove tree with second smallest frequency
            // combine into new tree with root being sum of frequencies of leaves
            HuffmanTree t = new HuffmanTree(null, (int)t1.frequency + (int)t2.frequency, t1,t2);
            pq.add(t); // add this new combination back into the priority queue
        }
        huffmanTree = pq.remove(); // pull out the final tree
        return huffmanTree; // and return it
    }

    /**
     * Generates a map with characters as keys and their codes (derived from their location in the tree)
     * as values
     */

    public static Map<Character, String> generateCodeMap(HuffmanTree huffmanTree) {
        if (huffmanTree != null) {
            codeMap = new HashMap<>(); // initialize map to hold characters and their codes
            // call helper method so map can be filled recursively without creating a new map each time
            generateCodeMapHelper(huffmanTree, codeMap, "");
            return codeMap; // return completed map
        }
        return null; // if the tree was null, return null
    }

    /**
     * Helper method for generateCodeMap(), filling up the map with entries
     */

    public static void generateCodeMapHelper(HuffmanTree huffmanTree, Map<Character, String> codeMap, String s) {
        if (huffmanTree != null) {
            if (huffmanTree.isLeaf()) { // if the current node is a leaf
                codeMap.put((Character) huffmanTree.getCharacter(), s); // add its character and code to the map
            } else {
                // otherwise recursively call method for left child, appending "0" to code
                generateCodeMapHelper(huffmanTree.getLeft(), codeMap, s + "0");
                // recursively call method for right child, appending "1" to code
                generateCodeMapHelper(huffmanTree.getRight(), codeMap, s + "1");
            }
        }
    }

    /**
     * Combines previous 4 methods in order to generate a code map
     * Then reads through characters in a text file, writing them out as the bits
     * found in their encoding sequences
     */

    public static void compress(String pathName) throws IOException {
        // call methods sequentially to generate final code map
        // generate compressed path name using helper method
        String compressedPathName = getCompressedPathName(pathName);

        // initialize bit writer
        BufferedBitWriter bitOutput = new BufferedBitWriter(compressedPathName);

        Map freqMap = generateFrequencyMap(pathName);
        PriorityQueue pq = generatePriorityQueue(freqMap);
        huffmanTree = generateHuffmanTree(pq);
        codeMap = generateCodeMap(huffmanTree);

        // if the code map isn't null (i.e. the original text file wasn't empty)
        if (codeMap != null) {
            BufferedReader input = new BufferedReader(new FileReader(pathName)); // initialize text reader
            int cur; // define cur as tracker for current character
            while ((cur = input.read()) != -1) { // update cur to be the current character (still in int form)
                char c = (char) cur; // cast to char
                String s = (String) codeMap.get(c); // get that character's code from the code map as a string
                Boolean bit; // declare bit to store current binary value
                for (int k = 0; k < s.length(); k++) { // loop through the code
                    if (s.substring(k, k + 1).equals("0")) { // if the current character is a 0
                        bit = false; // set bit to false
                    } else {
                        bit = true; // otherwise (if the character is a 1) set bit to true
                    }
                    bitOutput.writeBit(bit); // write bit into output file
                }
            }
            input.close(); // close reader
            bitOutput.close(); // close bit writer
        }
    }

    /**
     * Undoes the work of compress(), reading bits and writing their associated characters into
     * a new text file
     */

    public static void decompress(String compressedPathName) throws IOException {
        // generate name for decompressed file
        String decompressedPathName = getDecompressedPathName(compressedPathName);

        // initialize text writer
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedPathName));


        // if the tree isn't null (i.e. the original text file wasn't empty)
        if (huffmanTree != null) {
            // initialize bit reader
            BufferedBitReader bitInput = new BufferedBitReader(compressedPathName);

            // create copy of Huffman tree for traversal
            HuffmanTree temp = new HuffmanTree(huffmanTree.character, huffmanTree.frequency, huffmanTree.getLeft(), huffmanTree.getRight());

            while (bitInput.hasNext()) { // while there are still bits to read
                boolean bit = bitInput.readBit(); // store current bit into boolean value
                if (!bit) { // if bit = 0 = false
                    temp = temp.getLeft(); // go left
                } else { // if bit = 1 = true
                    temp = temp.getRight(); // go right
                }
                if (temp.isLeaf()) { // if we have reached a leaf
                    output.write((char) temp.character); // write associated character
                    // reset traversal tree
                    temp = new HuffmanTree(huffmanTree.character, huffmanTree.frequency, huffmanTree.getLeft(), huffmanTree.getRight());
                }
            }
            bitInput.close(); // close bit reader
            output.close(); // close writer
        }
    }

    /**
     * Generates name for compressed file given original text file name
     */
    public static String getCompressedPathName(String pathName) {
        return (pathName.substring(0, pathName.length()-4) + "_compressed.txt");
    }

    /**
     * Generates name for decompressed file given compressed file name
     */
    public static String getDecompressedPathName(String compressedPathName) {
        return (compressedPathName.substring(0, compressedPathName.length()-15) + "_decompressed.txt");
    }

    /**
     * Finds original path name given compressed file name
     */
    public static String getPathName (String compressedPathName) {
        return (compressedPathName.substring(0, compressedPathName.length()-15) + ".txt");
    }

    public static void main(String[] args) throws IOException {
        // try-catch block to ensure file exists
        try {
            // quick check that the file exists
            FileReader check = new FileReader(PATHNAME);

            // print each step to ensure methods are working correctly
            System.out.println("Frequency map:");
            System.out.println(generateFrequencyMap(PATHNAME) + "\n");
            System.out.println("Priority queue:");
            System.out.println(generatePriorityQueue(generateFrequencyMap(PATHNAME)) + "\n");
            System.out.println("Huffman tree");
            System.out.println(generateHuffmanTree(generatePriorityQueue(generateFrequencyMap(PATHNAME))));
            System.out.println("Code map:");
            System.out.println(generateCodeMap(generateHuffmanTree(generatePriorityQueue(generateFrequencyMap(PATHNAME)))) + "\n");

            // call master methods
            compress(PATHNAME);
            decompress(getCompressedPathName(PATHNAME));
        }
        // catch exception if file doesn't exist
        catch (FileNotFoundException e) {
            System.out.println("File " + PATHNAME + " found!");
        }


        // a short text file for testing
        String test = "inputs/test.txt";
        try {
            FileReader check = new FileReader(test);
            compress(test);
            decompress(getCompressedPathName(test));
        }
        catch (FileNotFoundException e) {
            System.out.println("File " + test + " not found!");
        }

        // an empty text file for boundary case testing
        String boundaryCase0 = "inputs/boundarycase0.txt";
        try {
            FileReader check = new FileReader(boundaryCase0);
            compress(boundaryCase0);
            decompress(getCompressedPathName(boundaryCase0));
        }
        catch (FileNotFoundException e) {
            System.out.println("File " + boundaryCase0 + " not found!");
        }

        // a text file with a single character for boundary case testing
        String boundaryCase1 = "inputs/boundarycase1.txt";
        try {
            FileReader check = new FileReader(boundaryCase1);
            compress(boundaryCase1);
            decompress(getCompressedPathName(boundaryCase1));
        }
        catch (FileNotFoundException e) {
            System.out.println("File + " + boundaryCase1 + " not found!");
        }

        // a text file with a single character repeated multiple times for boundary case testing
        String boundaryCase2 = "inputs/boundarycase2.txt";
        try {
            FileReader check = new FileReader(boundaryCase2);
            compress(boundaryCase2);
            decompress(getCompressedPathName(boundaryCase2));
        }
        catch (FileNotFoundException e) {
            System.out.println("File " + boundaryCase2 + " not found!");
        }

        // a text file that actually doesn't exist to ensure that try-catch block is functioning correctly
        String boundaryCase3 = "inputs/boundarycase3.txt";
        try {
            FileReader check = new FileReader(boundaryCase3);
            compress(boundaryCase3);
            decompress(getCompressedPathName(boundaryCase3));
        }
        catch (FileNotFoundException e) {
            System.out.println("File " + boundaryCase3 + " not found!");
        }
    }
}
