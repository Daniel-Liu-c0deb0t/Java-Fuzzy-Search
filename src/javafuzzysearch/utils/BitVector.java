package javafuzzysearch.utils;

public class BitVector{
    private static final int chunk = 63;
    private long[] bits;
    private long shorter;
    private int length;
    private int usedLength;
    
    public BitVector(int length){
        this.usedLength = length;
        this.length = (length % chunk == 0) ? (length / chunk) : (length / chunk + 1);
        if(this.length == 1)
            this.shorter = 0L;
        else
            this.bits = new long[this.length];
    }
    
    public BitVector set(int i){
        if(length == 1)
            shorter |= (1L << i);
        else
            bits[i / chunk] |= (1L << (i % chunk));
        return this;
    }
    
    //inclusive, exclusive
    public BitVector set(int s, int e){
        if(length == 1){
            shorter |= (((1L << (e - s)) - 1L) << s);
        }else{
            int s2 = (s % chunk == 0) ? (s / chunk) : (s / chunk + 1);
            int e2 = e / chunk;
            for(int i = s2; i < e2; i++){
                bits[i] = Long.MAX_VALUE;
            }
            if(s % chunk != 0)
                bits[s2 - 1] |= (((1L << (chunk - (s % chunk))) - 1L) << (s % chunk));
            if(e % chunk != 0)
                bits[e2] |= ((1L << (e % chunk)) - 1L);
        }
        return this;
    }
    
    public boolean get(int i){
        if(length == 1)
            return (shorter & (1L << i)) != 0L;
        return (bits[i / chunk] & (1L << (i % chunk))) != 0L;
    }
    
    public long getChunk(int i){
        if(length == 1)
            return shorter;
        return bits[i];
    }
    
    public BitVector leftShift(){
        if(length == 1){
            shorter <<= 1L;
        }else{
            for(int i = 0; i < length; i++){
                bits[i] = (bits[i] << 1L) | (i > 0 && (bits[i - 1] & (1L << chunk)) != 0L ? 1L : 0L);
            }
        }
        return this;
    }
    
    public BitVector orLShift(BitVector o){
        if(length == 1){
            shorter |= (o.getChunk(0) << 1L);
        }else{
            for(int i = 0; i < length; i++){
                bits[i] |= (o.getChunk(i) << 1L) | (i > 0 && (o.getChunk(i - 1) & (1L << chunk)) != 0L ? 1L : 0L);
            }
        }
        return this;
    }
    
    public BitVector andLShift(BitVector o){
        if(length == 1){
            shorter &= (o.getChunk(0) << 1L);
        }else{
            for(int i = 0; i < length; i++){
                bits[i] &= (o.getChunk(i) << 1L) | (i > 0 && (o.getChunk(i - 1) & (1L << chunk)) != 0L ? 1L : 0L);
            }
        }
        return this;
    }
    
    public BitVector or(BitVector o){
        if(length == 1){
            shorter |= o.getChunk(0);
        }else{
            for(int i = 0; i < length; i++){
                bits[i] |= o.getChunk(i);
            }
        }
        return this;
    }
    
    public BitVector orNot(BitVector o){
        if(length == 1){
            shorter |= ~o.getChunk(0);
        }else{
            for(int i = 0; i < length; i++){
                bits[i] |= ~o.getChunk(i);
            }
        }
        return this;
    }
    
    public BitVector and(BitVector o){
        if(length == 1){
            shorter &= o.getChunk(0);
        }else{
            for(int i = 0; i < length; i++){
                bits[i] &= o.getChunk(i);
            }
        }
        return this;
    }
    
    public BitVector xor(BitVector o){
        if(length == 1){
            shorter ^= o.getChunk(0);
        }else{
            for(int i = 0; i < length; i++){
                bits[i] ^= o.getChunk(i);
            }
        }
        return this;
    }
    
    public BitVector not(){
        if(length == 1){
            shorter = ~shorter;
        }else{
            for(int i = 0; i < length; i++){
                bits[i] = ~bits[i];
            }
        }
        return this;
    }
    
    public BitVector add(BitVector o){
        if(length == 1){
            shorter &= ~(1L << chunk);
            long other = o.getChunk(0) & ~(1L << chunk);
            shorter += other;
        }else{
            long carry = 0L;
            for(int i = 0; i < length; i++){
                bits[i] &= ~(1L << chunk);
                long other = o.getChunk(i) & ~(1L << chunk);
                if(bits[i] + carry + other < 0L){
                    bits[i] = 0L;
                    carry = 1L;
                }else{
                    bits[i] += carry + other;
                    carry = 0L;
                }
            }
        }
        return this;
    }
    
    public int getLength(){
        return length;
    }
    
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < usedLength; i++){
            b.append(get(i) ? '1' : '0');
        }
        return b.toString();
    }
}
