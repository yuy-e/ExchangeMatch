package Exchange.Matching.server;

import java.util.Map;

public abstract class XMLObject extends Object {
    protected String errorMessage;

    public abstract Map<String,String> getAttribute();
    
    public String getErrorMessage(){
        return errorMessage;
    }

    public void setErrorMessage(String msg){
        errorMessage=msg;
    }
}
