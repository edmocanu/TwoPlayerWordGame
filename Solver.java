import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

//Solver class is in charge of doing all the word processing and finding the highest-scoring word given a set of letters
public class Solver implements Serializable {
    private Character[] letters = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    private Integer[] points = {1,3,3,2,1,4,2,4,1,8,5,1,3,1,1,3,10,3,1,1,1,4,4,8,4,10};
    private Character[] chosenLetters = new Character[10];
    private HashMap<String,Integer> wordScores = new HashMap<String,Integer>();
  
    //the constructor fills the HashMap with words from the text file
    public Solver() throws Exception {
        this.getWords(this.wordScores);
    }
  
    //randomly chooses 10 letters to find the highest-scoring word(s) from
    public void setChosenLetters() {
        for (int i=0; i<10; i++) {
            chosenLetters[i] = this.letters[(int)(Math.random()*26)]; 
        }
    }
  
    //getters
    
    public Character[] getChosenLetters() {
        return this.chosenLetters;
    }
    
    public HashMap<String,Integer> getWordScores() {
        return this.wordScores;
    }
  
    public Character[] getLetters() {
        return this.letters;
    }
  
    public Integer[] getPoints() {
        return this.points;
    }
  
    //this method finds the highest-scoring words
    public HashSet<String> solve(Character[] letters) {
        ArrayList<String> possibleWords = new ArrayList<String>();
        this.getFourLetterWords(0,1,2,3,letters,possibleWords);
        ArrayList<String> permutations = new ArrayList<String>();
        int bestScore = 0;
        HashSet<String> bestWords = new HashSet<String>();
        for (int i=0; i<possibleWords.size(); i++) {
            permutations.clear();
            this.getPermutations(possibleWords.get(i).toCharArray(),permutations,4);
            
            //for every permutation for every combination of 4 letters, if it is a valid word, its score is calculated and compared to other words found so far
            for (int j=0; j<permutations.size(); j++) {
                String currentPerm = permutations.get(j);
                if (wordScores.containsKey(currentPerm)) {
                    if (wordScores.get(currentPerm) > bestScore) {
                      bestScore = wordScores.get(currentPerm);
                      bestWords.clear();
                      bestWords.add(currentPerm);
                    }else if (wordScores.get(currentPerm) == bestScore) {
                        bestWords.add(currentPerm);
                    }
                }
            }
        }
        return bestWords; 
    }
  
    //this method fills a HashMap with every key being every word in a text file and every value being that word's total score 
    private void getWords(HashMap<String,Integer> wordScores) throws IOException {
        HashMap<Character,Integer> letterScores = new HashMap<Character,Integer>();
        for (int i=0; i<26; i++) {
            letterScores.put(this.letters[i],points[i]);
        }
    
        Scanner input = new Scanner(new File("fourLetterWords.txt"));
        while (input.hasNext()) {
            String currentWord = input.nextLine().substring(0,4);
            Integer wordScore = 0;
            for (int i=0; i<4; i++) {
                wordScore += letterScores.get(currentWord.charAt(i));
            }                
            wordScores.put(currentWord,wordScore);  
        }
    }
  
    //this method gets all combinations of 4 letters from a set of 10 letters by recursively choosing letters from the set
    private void getFourLetterWords(int i1, int i2, int i3, int i4, Character[] letters, ArrayList<String> words) {
        String newI1 = Character.toString(letters[i1]);
        String newI2 = Character.toString(letters[i2]);
        String newI3 = Character.toString(letters[i3]);
        String newI4 = Character.toString(letters[i4]);
        if (i1 == 6) {
            String word = "";
            word += newI1 + newI2 + newI3 + newI4;
            words.add(word);
            return;
        }
        String word = "";
        word += newI1 + newI2 + newI3 + newI4;
        words.add(word);
        if (i4 != 9) {
            getFourLetterWords(i1,i2,i3,i4+1,letters,words);
        }else {
            if (i3 != 8) {
                getFourLetterWords(i1,i2,i3+1,i3+2,letters,words);
            }else {
                if (i2 != 7) {
                    getFourLetterWords(i1,i2+1,i2+2,i2+3,letters,words);
                }else {
                    getFourLetterWords(i1+1,i1+2,i1+3,i1+4,letters,words);
                }
            }
        }
    }
  
    //this method implements Heap's Algorithm to get all permutations of any 4-letter word
    private void getPermutations(char[] charArray, ArrayList<String> permsList, int num) {
        if (num==1) {
            String word = "";
            for (int i=0; i<4; i++) {
                word += charArray[i];
            }
            permsList.add(word);
        }else {
            getPermutations(charArray,permsList,num-1);
            for (int i=0;i<num-1; i++) {
                if (num%2==0) {
                  char temp = charArray[i];
                  charArray[i] = charArray[num-1];
                  charArray[num-1] = temp;
                }else {
                  char temp = charArray[0];
                  charArray[0] = charArray[num-1];
                  charArray[num-1] = temp;
                }
                getPermutations(charArray,permsList,num-1);
            }
        }
    }
}
