#HW4 - Jake TerHark
##Design
I wanted to make things easy should I have chosen to implement a mysql session store, so my design tries to be as stateless as possible.
Upon a new game request, a unique session id is generated and a new game is built according to the ```newgame``` request parameters.
These parameters and the FEN representation of the board is saved in the Session Store.
When each subsequent request comes in, the game is rebuilt from the FEN string in the session store, thus decoupling the session store from the game.