package javafuzzysearch.searchers;

/**
 * Implementation of the Bitap fuzzy matching algorithm for Hamming distance.
 */
public class BitapSearcher{
    private double maxEdits;
    
    public BitapSearcher(double maxEdits){
        this.maxEdits = maxEdits;
    }
    
    public List<FuzzyMatch> search(String text, String pattern){
        int totalEdit = (int)(edit < 1.0 ? (edit * pattern.length()) : edit);
        BitVector[] r = new BitVector[totalEdit + 1];
        for(int i = 0; i <= totalEdit; i++){
            r[i] = new BitVector(pattern.length() + 1).set(0);
        }
        
        for(int i = 0; i < text.length(); i++){
            BitVector old = new BitVector(pattern.length() + 1).or(r[0]);
            boolean found = false;
            for(int j = 0; j <= totalEdit; j++){
                if(j == 0){
                    if(text.charAt(i) != '#')
                        r[0].and(pm.get(Character.toUpperCase(a.charAt(i))));
                }else{
                    BitVector temp = new BitVector(b.length() + 1).or(r[j]);
                    (a.charAt(i) == '#' ? r[j] : r[j].and(pm.get(Character.toUpperCase(a.charAt(i))))).or(old);
                    old = temp;
                }
                r[j].leftShift().set(0);
                
                if(!found && r[j].get(b.length())){
                    int index = i - (b.length() - minOverlap);
                    int length = Math.min(index + 1, b.length());
                    if(j <= (edit < 0.0 ? (-edit * length) : edit) && length >= minOverlap){
                        if(!bestOnly || j <= min){
                            res.add(new Match(index, j, length));
                            min = j;
                        }
                    }
                    found = true;
                }
            }
        }
    }
}
