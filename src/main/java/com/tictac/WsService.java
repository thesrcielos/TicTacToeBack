package com.tictac;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

/**
 * WebSocket endpoint for handling real-time drawing messages.
 * <p>
 * This class manages connections to the "/bbService" WebSocket endpoint,
 * handles broadcasting of drawing events (points, clear actions, etc.),
 * and maintains a shared message history for new clients.
 * </p>
 *
 * <p>
 * It uses {@code @ServerEndpoint} to define the WebSocket route,
 * and maintains a static queue of sessions and a shared message list.
 * </p>
 *
 * Example message format:
 * <pre>
 *   {"x":123,"y":456,"color":"#FF0000","size":20}
 * </pre>
 *
 * Special command:
 * <pre>
 *   {"msg":"CLEAR"}
 * </pre>
 *
 * @author You
 */
@Component
@ServerEndpoint("/bbService")
public class WsService {

    private static final Logger logger = Logger.getLogger(WsService.class.getName());

    static Session player1 = null;
    static Session player2 = null;

    static String currentTurn = "X";

    // Lista de estados del tablero para cada ronda (paso a paso)
    static List<String[][]> rounds = new ArrayList<>();
    static String[][] currentBoard = new String[3][3]; // Tablero actual


    Session ownSession;

    @OnOpen
    public void openConnection(Session session) {
        try {
            if (player1 == null) {
                player1 = session;
                session.getBasicRemote().sendText("{\"playerTurn\":\"X\"}");
            } else if (player2 == null) {
                player2 = session;
                session.getBasicRemote().sendText("{\"playerTurn\":\"O\"}");
            } else {
                session.getBasicRemote().sendText("{\"error\":\"Only 2 players allowed\"}");
                session.close();
                return;
            }

            if (rounds.isEmpty()){
                rounds.add(deepCopy(currentBoard));
            }

            ownSession = session;
            logger.info("Player connected: " + session.getId());
            session.getBasicRemote().sendText("{\"status\":\"Connection established.\"}");

            session.getBasicRemote().sendText(serialize(currentBoard));

        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    @OnMessage
    public void processMessage(String message, Session session) {
        System.out.println("message = " + message);
        try {
            ObjectMapper mapper = new ObjectMapper();
            TicTacToeMsg msg = mapper.readValue(message, TicTacToeMsg.class);

            if (msg.stepBack != null) {
                if (msg.stepBack >= 0 && msg.stepBack < rounds.size()) {
                    currentBoard = deepCopy(rounds.get(msg.stepBack));
                    broadcast(serializeBoard(currentBoard, msg.stepBack));
                    rounds = rounds.subList(0, msg.stepBack + 1);
                    currentTurn = msg.stepBack % 2 == 0 ? "X" : "O";
                    broadcast(String.format("{\"turn\":\"%s\"}", currentTurn));
                } else {
                    session.getBasicRemote().sendText("{\"error\":\"Invalid round number.\"}");
                }
                return;
            }

            if (!isValidPlayer(session, msg.player)) {
                session.getBasicRemote().sendText("{\"error\":\"Invalid player.\"}");
                return;
            }

            if (!msg.player.equals(currentTurn)) {
                session.getBasicRemote().sendText("{\"error\":\"Not your turn.\"}");
                return;
            }

            if (msg.row < 0 || msg.row >= 3 || msg.col < 0 || msg.col >= 3) {
                session.getBasicRemote().sendText("{\"error\":\"Invalid move.\"}");
                return;
            }

            if (currentBoard[msg.row][msg.col] != null) {
                session.getBasicRemote().sendText("{\"error\":\"Cell already taken.\"}");
                return;
            }

            currentBoard[msg.row][msg.col] = msg.player;

            // Save board snapshot
            rounds.add(deepCopy(currentBoard));

            // Switch turn
            currentTurn = currentTurn.equals("X") ? "O" : "X";

            // Broadcast updated board to both players
            broadcast(serialize(currentBoard));
            broadcast(String.format("{\"turn\":\"%s\"}", currentTurn));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error processing message", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (session.equals(player1)) {
            player1 = null;
        } else if (session.equals(player2)) {
            player2 = null;
        }
        logger.info("Connection closed: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.severe("WebSocket Error: " + error.getMessage());
    }

    private void broadcast(String msg) throws IOException {
        if (player1 != null) player1.getBasicRemote().sendText(msg);
        if (player2 != null) player2.getBasicRemote().sendText(msg);
    }

    private boolean isValidPlayer(Session session, String player) {
        return (player.equals("X") && session.equals(player1)) ||
                (player.equals("O") && session.equals(player2));
    }

    private String serialize(String[][] board) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(board);
    }
    private String serializeBoard(String[][] board, int step) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("board", board);
        response.put("step", step);

        return mapper.writeValueAsString(response);
    }


    private String[][] deepCopy(String[][] original) {
        String[][] copy = new String[3][3];
        for (int i = 0; i < 3; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}

class TicTacToeMsg {
    public int col;
    public int row;
    public String player;
    public Integer stepBack;
}
