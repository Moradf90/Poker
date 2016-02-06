package apps.morad.com.poker.models;

import apps.morad.com.poker.R;

/**
 * Created by Morad on 1/8/2016.
 */
public enum Medal {
    Gold("gold", R.drawable.gold_medal),
    Silver("silver", R.drawable.silver_medal),
    Pronze("pronze", R.drawable.pronze_medal),
    Empty("", 0);

    String _name;
    int _drawable;

   Medal(String name, int drawable){
       _name = name;
       _drawable = drawable;
   }

    public int getDrawable(){
        return _drawable;
    }

    public static Medal getByName(String name){
        Medal res = Medal.Empty;

        for (Medal m :
                Medal.values()) {
            if (m._name.equals(name)){
                res = m;
                break;
            }
        }

        return res;
    }

}
