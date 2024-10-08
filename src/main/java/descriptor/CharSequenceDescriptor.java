package descriptor;

import java.util.LinkedList;
import java.util.List;

import syntax.SyntaxElement;
import util.CharSequenceInterval;

public class CharSequenceDescriptor implements SyntaxElement {

    private OneCharDesc[] ocd;

    private String dsc;

    public CharSequenceDescriptor(String dsc) {

        this.dsc = dsc;
        BooleanResult r = DescriptorValidator.check(dsc);
        if (r.getBooleanResult() == false) {
            throw new RuntimeException(r.getMessage());
        }

        List<String> oneCharDescriptorsAsString = sliceDescriptor(dsc);
        ocd = new OneCharDesc[oneCharDescriptorsAsString.size()];
        int i = 0;
        for (String s : oneCharDescriptorsAsString) {
            ocd[i] = new OneCharDesc(s);
            i++;
        }
    }

    public CharSequenceDescriptor(OneCharDesc[] ocd) {
        this.ocd = ocd;
    }

//    @Override
//    public boolean equals(Object other) {
//        if (!(other instanceof CharSequenceDescriptor)) return false;
//        CharSequenceDescriptor o2 = (CharSequenceDescriptor) other;
//        OneCharDesc[] a = this.ocd;
//        OneCharDesc[] b = o2.ocd;
//
//        if (a.length != b.length) return false;
//
//        for (int i = 0; i < a.length; i++) {
//            if (!(a[i].equals(b[i]))) return false;
//        }
//
//        return true;
//
//    }

    static List<String> sliceDescriptor(String dsc) {
        List<String> result = new LinkedList<>();
        int end;
        for (int begin = 0; begin < dsc.length(); begin++) {
            String s;
            char c = dsc.charAt(begin);
            if (c == '\\') {
                end = begin + 1;
                s = dsc.substring(begin, end + 1);
                begin = end;
            } else if (c == '(') {
                end = dsc.indexOf(")", begin);
                s = dsc.substring(begin + 1, end);
                begin = end;
            } else {
                s = "" + c;
            }

            result.add(s);

        }

        return result;
    }

    public boolean matches(CharSequence source) {
        if (source.length() != ocd.length) return false;
        for (int i = 0; i < source.length(); i++) {
            if (!ocd[i].matches(source.charAt(i))) return false;

        }
        return true;
    }

    public boolean matchesInFrom(CharSequence cs, int firstIndex) {
        if (firstIndex > cs.length() - ocd.length) return false;
        return matches(new CharSequenceInterval(cs, firstIndex, firstIndex + ocd.length - 1));
    }

    public int getDescribedLength() {
        return ocd.length;
    }

    @Override
    public String toString() {
        return "\'"+this.dsc;

    }

    public OneCharDesc[] getOcdArray() {
        return ocd;
    }

    @Override
    public boolean isDescriptor() {
        return true;
    }

    @Override
    public String getReferencedGroup() {

        return null;
    }

    @Override
    public CharSequenceDescriptor getCsd() {
        return this;
    }

    @Override
    public SyntaxElement copy() {
        return new CharSequenceDescriptor(dsc);
    }

}
