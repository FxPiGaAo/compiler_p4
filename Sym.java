import java.util.*;
public class Sym {
    private String type;
    private List<String> param=null;
    private HashMap<String, Sym> table;
    public Sym(String type) {
        this.type = type;
    }
    public Sym(HashMap<String, Sym> table) {
        this.table = table;
        this.type = "struct";
    }
    public Sym(List<String> list, String type) {
        this.type = type;
        param = list;
    }
    
    public String getType() {
        return type;
    }

    public HashMap<String, Sym> getTable() {
        return table;
    }
    
    public String toString() {
        if(param==null){
            return type;
        }else{
            String temp = "";
            Iterator it = param.iterator();
            if(it.hasNext()){
                temp += it.next();
            }
            while (it.hasNext()) {
                temp += ",";
                temp += it.next();
            }
            return temp+"->"+type;
        }        
    }

    public List<String> getparam(){
        return param;
    }
}
