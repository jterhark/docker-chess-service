package API;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

//This is just a test controller
@RestController
public class EchoController {

    @RequestMapping(path = "/echo", method=GET)
    public String echo(@RequestParam(value="content", defaultValue = "empty") String s){
        return s;
    }

    @RequestMapping(path="/class-test", method = POST)
    public ResponseClass requestTest(@RequestBody RequestClass request){
        return new ResponseClass(request.str, request.i);
    }
}

class RequestClass{
    public String str;
    public Integer i;
}

class ResponseClass{
    private final String s;
    private final int i;

    ResponseClass(String s, int i){
        this.s = s;
        this.i = i;
    }

    public String getS(){return s;}
    public int getI(){return i;}
}
