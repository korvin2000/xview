package org.delafer.xanderView.common;

import net.j7.commons.strings.StringUtils;

public class DefaultNameIncrementer implements INameIncrementer{

    private CharSequence preffix;
    private NuValue incrementor;
    private int x = 0;
    private String original;
    public DefaultNameIncrementer(String name) {
        this.original = name;
        StringBuilder sb = new StringBuilder(name.length());
        StringBuilder preffix = new StringBuilder(name.length());
        NuValue value;

        char ch;
        for (int i = name.length()-1; i >= 0; i--) {
            ch = name.charAt(i);
            if (Character.isDigit(ch)) {
                sb.insert(0, ch);
            } else {
                preffix.append(name.substring(0, i+1));
                break;
            }
        }

        value = new NuValue(sb.toString());

        boolean isOk = preffix.length() >= 3 && value.get() < 200 && !sb.isEmpty();
        if (!isOk) {
            preffix.append(sb).append(chooseDivider(preffix));
            value = new NuValue(0);
        }

        this.preffix = preffix;
        this.incrementor = value;
    }

    private char chooseDivider(StringBuilder preffix) {
        if (preffix.indexOf("_")<0) return '_';
        if (preffix.indexOf("-")<0) return '-';
        if (preffix.indexOf("X")<0 || preffix.indexOf("x")<0) return 'X';
        if (preffix.indexOf("Z")<0 || preffix.indexOf("z")<0) return 'Z';
        if (preffix.indexOf("$")<0) return '$';
        if (preffix.indexOf("K")<0 || preffix.indexOf("k")<0) return 'K';
        if (preffix.indexOf(" ")<0) return ' ';
        return '_';
    }

    @Override
    public INameIncrementer increment() {
        incrementor.increment();
        x++;
        return this;
    }

    @Override
    public String build() {
        if (0 == x) return original;
        StringBuilder res = new StringBuilder(this.preffix);
        res.append(this.incrementor.value());
        return res.toString();
    }

    public static void main(String[] args) {
        INameIncrementer n = new DefaultNameIncrementer("_a-lex");
        System.out.println(n.build());
        for (int i = 0; i < 100 ; i++) {
            n.increment();
            System.out.println(n.build());
        }

    }

    private static class NuValue {
        private int minLen;
        private long value;
        final static int MIN = 2;

        public NuValue(String value) {
            if (value.isEmpty()) {
                this.value = 0L;
            } else {
                this.value = Long.parseLong(value);
            }
            this.minLen = Math.max(value.length(), MIN);
        }

        public NuValue(long value, int minLen) {
            this.value = value;
            this.minLen = minLen;
        }

        public NuValue(long value) {
            this(value, 2);
        }

        public void increment() {
            this.value++;
        }

        public long get() {
            return this.value;
        }

        public String value() {
           return StringUtils.fillString(Long.toString(value), minLen, '0');
        }

    }
}
