package org.delafer.xanderView.gui;
import me.xuender.unidecode.Unidecode;

public class Transliterator {

    
    public static String transliterate3(String input) {
    	 return Unidecode.decode(input);
    }
}
