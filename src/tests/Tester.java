package tests;

public class Tester{
    private int counter;
    
    public Tester(String s){
        System.out.println();
        System.out.println(s);
        System.out.println();
        counter = 0;
    }
    
    public void testStrEquals(Object a, Object b){
        String res;
        String s1 = String.valueOf(a);
        String s2 = String.valueOf(b);
        
        if(s1.equals(s2))
            res = String.format("PASSED \"%s\"", s1);
        else
            res = String.format("*** FAILED \"%s\" != \"%s\"", s1, s2);
        
        System.out.println(String.format("TEST %d %s", counter, res));
        
        counter++;
    }
}
