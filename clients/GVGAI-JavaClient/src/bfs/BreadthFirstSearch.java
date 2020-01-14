package bfs;

import kbextraction.MultiAbstractionLevelKB;
import kbextraction.agents.GVGAIBot;
import serialization.SerializableStateObservation;
import serialization.Types;

import java.util.*;

/**
 * Created by dockhorn on 08.05.2018.
 */
public class BreadthFirstSearch {

    private BFSNode rootNode;
    private ArrayList<Types.ACTIONS> availableActions;
    private HashMap<GVGAIBot.KnowledgeType, MultiAbstractionLevelKB> metaKB;
    private LinkedList<BFSNode> openNodes = new LinkedList<>();
    private LinkedList<BFSNode> closedNodes = new LinkedList<>();
    private HashMap<Integer, Set<Integer>> visitedNodes;

    private Random random;

    public BreadthFirstSearch(SerializableStateObservation sso,
                              HashMap<GVGAIBot.KnowledgeType, MultiAbstractionLevelKB> metaKB){
        this.rootNode = new BFSNode(sso);
        this.openNodes.add(rootNode);
        this.metaKB = metaKB;
        random = new Random();

        //only use actions allowed in this game
        this.availableActions = sso.getAvailableActions();
    }

    public void runBFS(){
        this.runBFS(BFSPARAMETERS.MAXEXPANSIONS);
    }

    public void runBFS(int nr_expansions){
        if (BFSPARAMETERS.PRUNING){
            this.visitedNodes = new HashMap<>();
        }

        for (int i = 0; i < nr_expansions; i++){
            if (openNodes.isEmpty()){
                break;
            }

            BFSNode currentNode = openNodes.getFirst();
            ArrayList<BFSNode> newNodes = expandNode(currentNode);

            if (BFSPARAMETERS.PRUNING){
                for (BFSNode node : newNodes) {
                    if (this.visitedNodes.containsKey((int) node.gamestate.avatarPosition[0]) &&
                            this.visitedNodes.get((int) node.gamestate.avatarPosition[0]).contains((int) node.gamestate.avatarPosition[1])){
                        if (BFSPARAMETERS.BFS_DEBUG){
                            System.out.println("Ignore this node");
                            //todo decrease number of done expansions
                        }
                    } else {
                        if (this.visitedNodes.containsKey((int) node.gamestate.avatarPosition[0])){
                            this.visitedNodes.get((int) node.gamestate.avatarPosition[0]).add((int) node.gamestate.avatarPosition[1]);
                        } else {
                            this.visitedNodes.put((int) node.gamestate.avatarPosition[0],
                                    new HashSet<Integer>() {{
                                        add((int) node.gamestate.avatarPosition[1]);
                                    }}
                                );
                            openNodes.addLast(node);
                        }
                    }
                }
            } else {
                openNodes.addAll(newNodes);
            }
            openNodes.removeFirst();
            closedNodes.add(currentNode);
        }
    }

    public Types.ACTIONS getBestMove(){
        // openNodes does always contain at least one element (the root node)
        openNodes.addAll(closedNodes);
        BFSNode bestLeafNode = openNodes.getFirst();
        float bestScore = bestLeafNode.gamestate.gameScore;
        float worstScore = bestLeafNode.gamestate.gameScore;

        for (BFSNode leafNode : openNodes){
            if (bestScore <= leafNode.gamestate.gameScore){
                bestLeafNode = leafNode;
                bestScore = leafNode.gamestate.gameScore;
            }
            if (worstScore >= leafNode.gamestate.gameScore){
                worstScore = leafNode.gamestate.gameScore;
            }
        }

        // if nothing good can be found just return a random action
        if (bestScore == worstScore){
            if (BFSPARAMETERS.BFS_DEBUG){
                System.out.println("DEBUG: no best action, return random action");
            }
            return availableActions.get(random.nextInt(availableActions.size()));
        }

        BFSNode currentNode = bestLeafNode;
        while(true) {
            if (currentNode.parent != null && currentNode.parent.parent != null)
                currentNode = currentNode.parent;
            else
                break;
        }

        if (currentNode.action == null && BFSPARAMETERS.BFS_DEBUG)
        {
            System.out.println("DEBUG: no action set, return random action");
            return availableActions.get(random.nextInt(availableActions.size()));
        }
        return currentNode.action;
    }


    public ArrayList<BFSNode> expandNode(BFSNode node){
        ArrayList<Types.ACTIONS> actions;
        node.children = new ArrayList<>();

        for (int i = 0; i < availableActions.size(); i++){
            BFSNode newNode = new BFSNode(node);
            node.children.add(newNode);
            addInference(newNode, availableActions.get(i));
        }
        return node.children;
    }

    private void addInference(BFSNode node, Types.ACTIONS action){
        node.action = action;

        Set<String> stateWithAction = node.gamestate.getPerception();
        //stateWithAction.add("c_Action="+((GVGAIMove)m).action.toString());

        switch (action){
            case ACTION_DOWN: stateWithAction.add("c_Action5"); break;
            case ACTION_UP: stateWithAction.add("c_Action4"); break;
            case ACTION_LEFT: stateWithAction.add("c_Action3"); break;
            case ACTION_RIGHT: stateWithAction.add("c_Action2"); break;
            case ACTION_USE: stateWithAction.add("c_Action1"); break;
        }

        node.addInference(
                metaKB.get( GVGAIBot.KnowledgeType.SCORE ).reasoning(stateWithAction),
                metaKB.get( GVGAIBot.KnowledgeType.MOVE_REL ).reasoning( stateWithAction ),
                metaKB.get( GVGAIBot.KnowledgeType.MOVE_ABS ).reasoning( stateWithAction ),
                metaKB.get( GVGAIBot.KnowledgeType.WIN ).reasoning( stateWithAction )
        );
    }
}
