import API.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes =SpringAPI.class)
public class ChessTest {

    @LocalServerPort
    int port;

    private URI uri;
    private RestTemplate rest;

    @Before
    public void setUp() throws URISyntaxException {
        uri = new URI("http://localhost:" + port);
        rest = new RestTemplate();
    }

    //echo heartbeat test. Make sure API is running.
    @Test
    public void testHeartbeat(){
        URI path = uri.resolve("/echo");

        ResponseEntity<String> result = rest.getForEntity(path, String.class);

        Assert.assertEquals(200, result.getStatusCodeValue());
        Assert.assertEquals("empty", result.getBody());
    }

    //create new game test
    //Make sure API can access chess classes and create a new game.
    @Test
    public void testNewGame(){
        URI path = uri.resolve("/chess/newgame");
        NewGameRequest body = new NewGameRequest("white", 1);
        ResponseEntity<NewGameResponse> res = rest.postForEntity(path, body,NewGameResponse.class);

        Assert.assertNotNull(res.getBody());
        Assert.assertNotNull(res.getBody().id);
        Assert.assertEquals(res.getBody().boardFen, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    //play a game with same API
    //pit the api against itself. Start two games and this test is basically just an adapter for crosstalk.
    @Test
    public void selfPlay(){
        URI newGamePath = uri.resolve("/chess/newgame");
        URI moveFenPath = uri.resolve("/chess/movePgn");

        //create games
        NewGameRequest blackPlayerBody = new NewGameRequest("white", 1);
        NewGameRequest whitePlayerBody = new NewGameRequest("black", 3);

        ResponseEntity<NewGameResponse> blackPlayerNewReq = rest.postForEntity(newGamePath, blackPlayerBody, NewGameResponse.class);
        ResponseEntity<NewGameResponse> whitePlayerNewReq = rest.postForEntity(newGamePath, whitePlayerBody, NewGameResponse.class);

        //test games were created
        Assert.assertNotNull(blackPlayerNewReq.getBody());
        Assert.assertNotNull(whitePlayerNewReq.getBody());

        //setup request skeletons
        String whiteEndpointSession = whitePlayerNewReq.getBody().id;
        String blackEndpointSession = blackPlayerNewReq.getBody().id;

        MovePGNRequest whiteMoveReq = new MovePGNRequest(blackEndpointSession, whitePlayerNewReq.getBody().boardPGN);
        MovePGNRequest blackMoveReq = new MovePGNRequest();
        blackMoveReq.id = whiteEndpointSession;


        for(int i = 0; i < 1000; ++i){
            System.out.println("Iter: " + i);
            ResponseEntity<MoveResponse> blackMove = rest.postForEntity(moveFenPath, whiteMoveReq, MoveResponse.class);
            Assert.assertNotNull(blackMove.getBody());
            if(blackMove.getBody().status.contains("Win")){
                System.out.println("Winner");
                break;
            }
            blackMoveReq.pgn = blackMove.getBody().boardPGN;

            ResponseEntity<MoveResponse> whiteMove = rest.postForEntity(moveFenPath, blackMoveReq, MoveResponse.class);
            Assert.assertNotNull(whiteMove.getBody());
            if(whiteMove.getBody().status.contains("Win")){
                System.out.println("Winner");
                break;
            }
            whiteMoveReq.pgn = whiteMove.getBody().boardPGN;
        }

    }
}
