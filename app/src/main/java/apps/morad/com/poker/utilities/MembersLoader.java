package apps.morad.com.poker.utilities;

import android.util.Log;

import com.activeandroid.query.Select;
import com.facebook.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import apps.morad.com.poker.models.Member;

/**
 * Created by Morad on 1/15/2016.
 */
public class MembersLoader {

    private static MembersLoader _loader;

    private static MembersLoader instance(){
        if(_loader == null){
            _loader = new MembersLoader();
        }

        return _loader;
    }

    private HashMap<String, Member> _members;

    MembersLoader(){
        _members = new HashMap<>();
    }

    public static void refresh(){

        List<Member> mems = new Select().from(Member.class).execute();

        for (Member m :
                mems) {
            instance()._members.put(m.getFbId(), m);
        }
    }

    public static Member getById(String id){

        if(instance()._members.containsKey(id)){
            return instance()._members.get(id);
        }

        return null;
    }

    public static List<Member> getAll(){
        return new ArrayList<>(instance()._members.values());
    }
}
