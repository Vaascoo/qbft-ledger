package pt.ulisboa.tecnico.hdsledger.service.models;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.text.MessageFormat;

import pt.ulisboa.tecnico.hdsledger.communication.NodeMessage;
import pt.ulisboa.tecnico.hdsledger.utilities.CustomLogger;

public class MessageBucket {

    private static final CustomLogger LOGGER = new CustomLogger(MessageBucket.class.getName());
    // Quorum size
    private final int quorumSize;
    // Map of consensus instance to round to messages
    private final Map<Integer, Map<Integer, List<NodeMessage>>> bucket = new ConcurrentHashMap<>();

    public MessageBucket(int nodeCount) {
        int f = Math.floorDiv(nodeCount - 1, 3);
        quorumSize = Math.floorDiv(nodeCount + f, 2) + 1;
    }

    /*
     * Add a message to the bucket
     * 
     * @param consensusInstance
     * 
     * @param message
     */
    public void addMessage(NodeMessage message) {
        int consensusInstance = Integer.parseInt(message.getArgs().get(0));
        int round = Integer.parseInt(message.getArgs().get(1));

        bucket.putIfAbsent(consensusInstance, new ConcurrentHashMap<>());
        bucket.get(consensusInstance).putIfAbsent(round, new ArrayList<>());
        bucket.get(consensusInstance).get(round).add(message);
    }

    /*
     * A quorum of valid messages is a set of floor[(n+f)/2] + 1 messages that agree
     * on the same consensus instance, round and value.
     * 
     * @param instance
     * 
     * @param round
     */
    public Optional<String> hasValidQuorum(String nodeId, int instance, int round) {

        // Create mapping of value to frequency
        HashMap<String, Integer> frequency = new HashMap<String, Integer>();
        bucket.get(instance).get(round).forEach((message) -> {
            String value = message.getArgs().get(message.getArgs().size() - 1);
            frequency.put(value, frequency.getOrDefault(value, 0) + 1);
        });

        // Only one value (if any, thus the optional) will have a frequency
        // greater than or equal to the quorum size
        return frequency.entrySet().stream().filter((Map.Entry<String, Integer> entry) -> {
            return entry.getValue() >= quorumSize;
        }).map((Map.Entry<String, Integer> entry) -> {
            return entry.getKey();
        }).findFirst();
    }

    /*
     * Verify if all received messages have the same value
     * Report if they don't
     * 
     * @param value The quorum value
     * 
     * @param instance The consensus instance
     * 
     * @param round The round
     */
    public void verifyReceivedMessages(String value, int instance, int round) {
        bucket.get(instance).get(round).forEach((message) -> {
            if (!message.getArgs().get(message.getArgs().size() - 1).equals(value))
                LOGGER.log(Level.INFO, MessageFormat.format(
                        "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                                + "@      WARNING: DIFFERENT VALUES RECEIVED!      @\n"
                                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                                + "IT IS POSSIBLE THAT NODE {0} IS DOING SOMETHING NASTY!",
                        message.getSenderId()));
        });
    }
}