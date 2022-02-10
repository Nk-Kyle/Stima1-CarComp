package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.*;
import java.util.*;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import java.security.SecureRandom;

public class Bot {

    private List<Command> directionList = new ArrayList<>();

    private final Random random;
    private final int PowerUpScale = 2;
    private final int  CollisionScale = -2;
    private final int MINIMUM_SPEED = 0;
    private final int SPEED_STATE_1 = 3;
    private final int INITIAL_SPEED = 5;
    private final int SPEED_STATE_2 = 6;
    private final int SPEED_STATE_3 = 8;
    private final int MAXIMUM_SPEED = 9;
    private final int BOOST_SPEED = 15;
    private final int FAR_DISTANCE = 40;
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command DO_NOTHING = new DoNothingCommand();
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private int lastRound_tweet = 0;

    public Bot(Random random, GameState gameState) {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }


    public Command run(GameState gameState){
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        //List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState,myCar.speed);
        //TODO EVALUATE SCORE USING MACRO?
        if (gameState.currentRound == 1) return ACCELERATE;
        if(myCar.boosting == true){
            //Conserve speed by using powerups
            if (haveObstacle( myCar.position.lane,myCar.position.block, gameState,myCar.speed)){
                //Need to manuveur around obstacle
                if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                }
                else {
                    return take_turn(myCar.position.lane,myCar.position.block,gameState,myCar.speed,true);
                }
            }
            return aggresive(myCar,opponent, gameState.currentRound);
        }

        //Greedy EMP offense

        if(EMP_ABLE(myCar,opponent)) return EMP;

        if(myCar.damage > 2) return FIX;

        // Greedy always boost
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)){
            if(myCar.damage > 0){
                return FIX;
            }
            if (haveObstacle(myCar.position.lane,myCar.position.block, gameState,myCar.speed)){
                return take_turn(myCar.position.lane,myCar.position.block,gameState,myCar.speed,true);
            }
            return BOOST;
        }


        if (haveObstacle( myCar.position.lane,myCar.position.block, gameState,myCar.speed)){
            return take_turn(myCar.position.lane,myCar.position.block,gameState,myCar.speed,false);
        }
        if(myCar.speed >= SPEED_STATE_3) return aggresive(myCar, opponent, gameState.currentRound);
        return take_turn(myCar.position.lane,myCar.position.block,gameState,myCar.speed,false);
    }

    private boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private Command aggresive(Car myCar, Car opponent, int current_round){
        if(hasPowerUp(PowerUps.EMP, myCar.powerups) && myCar.position.block < opponent.position.block &&
                abs(myCar.position.lane - opponent.position.lane) <= 1) return EMP;
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups) && lastRound_tweet != current_round-1) {
            lastRound_tweet = current_round;
            return new TweetCommand(opponent.position.lane, opponent.position.block + nextSpeed(opponent.speed) + 1);
        }
        if (hasPowerUp(PowerUps.OIL, myCar.powerups) && myCar.position.block > opponent.position.block +nextSpeed(opponent.speed))
            return OIL;
        return ACCELERATE;
    }

    private Command take_turn(int curr_lane,int block, GameState gameState, int speed, boolean safemode){
        int left_lane;
        int right_lane;
        int[] left_data = new int[3];
        int[] right_data = new int[3];
        int[] mid_data = new int[3];
        left_lane = curr_lane-1;
        right_lane = curr_lane+1;
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        int left_score;
        int mid_score;
        int right_score;
        int corrector = 1;
        if (speed < SPEED_STATE_2) return ACCELERATE;
        if (left_lane != 0){
            Lane[] llane_list = map.get(left_lane-1);
            left_data = evaluateLane(llane_list, block, startBlock, speed-1);
        }
        if (right_lane != 5){
            Lane[] rlane_list = map.get(right_lane-1);
            right_data = evaluateLane(rlane_list,block,startBlock,speed-1);
        }
        Lane[] lane_list = map.get(curr_lane-1);
        mid_data = evaluateLane(lane_list,block,startBlock,nextSpeed(speed));

        // Score to turn or not
        if (safemode) { //Always find non-collision course (known mid always collission)
            corrector = 0;
            /** Both lane no collision **/
            if (right_data[2] == 1 && right_data[0] == 0 && left_data[2] == 1 && left_data[0] == 0) {
                if (left_data[1] > right_data[1]) return TURN_LEFT;
                else return TURN_RIGHT;
            }
            /** Left Lane only collison **/
            if (right_data[2] == 1 && right_data[0] == 0) {
                return TURN_RIGHT;
            }
            /** Right Lane only collison **/
            if (left_data[2] == 1 && left_data[0] == 0) {
                return TURN_LEFT;
            }
        }

        // Evaluate score based (fall through from safemode => all lane has collisions)
        left_score = left_data[0]* CollisionScale + left_data[1]*PowerUpScale + (left_data[2]-1) * 1000 + speed-1;
        right_score = right_data[0]* CollisionScale + right_data[1]*PowerUpScale + (right_data[2]-1) * 1000 + speed-1;
        mid_score = mid_data[0]* CollisionScale + mid_data[1]*PowerUpScale + (mid_data[2]-1) * 1000 + nextSpeed(speed);
        if(right_data[2] == 1 && left_data[2] == 1 && mid_data[2] == 1){
            if (mid_score >= left_score && mid_score >= right_score) return chooseAggAcc(gameState.player,gameState.opponent, gameState.currentRound);
            if (left_score >= mid_score && left_score >= right_score) return TURN_LEFT;
            return TURN_RIGHT;
        }
        if(right_data[2] == 1 && left_data[2] == 1 && mid_data[2] == 0){
            if (right_score >= left_score) return TURN_RIGHT;
            return  TURN_LEFT;
        }
        if(right_data[2] == 1 && left_data[2] == 0 && mid_data[2] == 1){
            if (mid_score >= right_score) return ACCELERATE;
            return TURN_RIGHT;
        }
        if(right_data[2] == 0 && left_data[2] == 1 && mid_data[2] == 1){
            if (mid_score >= left_score) return ACCELERATE;
            return TURN_LEFT;
        }
        if(mid_data[2] == 1) return chooseAggAcc(gameState.player, gameState.opponent, gameState.currentRound);
        if(right_data[2] == 1) return TURN_RIGHT;
        if(left_data[2] == 1) return TURN_LEFT;
        return chooseAggAcc(gameState.player,gameState.opponent, gameState.currentRound);
    }

    private int nextSpeed(int speed){
        if (speed == MINIMUM_SPEED) return SPEED_STATE_1;
        if (speed == SPEED_STATE_1) return SPEED_STATE_2;
        if (speed == SPEED_STATE_2) return SPEED_STATE_3;
        if (speed == SPEED_STATE_3) return MAXIMUM_SPEED;
        if (speed == BOOST_SPEED) return BOOST_SPEED;
        if (speed == MAXIMUM_SPEED) return MAXIMUM_SPEED;
        if (speed == INITIAL_SPEED) return SPEED_STATE_2;
        return speed;
    }

    /** returns array of 3 values : collision_value, n_powerups, nothave_Cybertruck **/
    private int[] evaluateLane(Lane[] lane_list, int block, int startBlock, int speed){
        int[] res = new int[3];
        res[0] = 0;
        res[1] = 0;
        res[2] = 1;
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++){
            if (i == lane_list.length) break;
            if (lane_list[i].isOccupiedByCyberTruck){
                res[2] = 0;
                break;
            }
            else if (lane_list[i].terrain == Terrain.MUD || lane_list[i].terrain == Terrain.OIL_SPILL){
                res[0] += 1;
            }
            else if (lane_list[i].terrain == Terrain.WALL){
                res[0] += 3;
            }
            else if (lane_list[i].terrain == Terrain.EMPTY || lane_list[i].terrain == Terrain.FINISH){}
            else{
                res[1] += 1;
            }
        }
        return res;
    }

    private Command chooseAggAcc(Car myCar, Car opponent, int current_round){
        if (myCar.speed >= SPEED_STATE_3){
            if (opponent.position.block - myCar.position.block > FAR_DISTANCE){
                if(EMP_ABLE(myCar,opponent)) return EMP;
                return GO_EMPLANE(myCar);
            }
            return aggresive(myCar, opponent, current_round);
        }
        return ACCELERATE;
    }

    private boolean EMP_ABLE(Car myCar, Car opponent){
        return (opponent.speed >= SPEED_STATE_3 && hasPowerUp(PowerUps.EMP, myCar.powerups) &&
                myCar.position.block < opponent.position.block &&
                abs(myCar.position.lane - opponent.position.lane) <= 1);
    }

    private Command GO_EMPLANE(Car myCar){
        if (myCar.position.lane == 1) return TURN_RIGHT;
        return TURN_LEFT;
    }

    /** Returns all lanes from current position that has an obstacle **/
    private boolean haveObstacle(int lane, int block, GameState gameState, int speed){
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        Lane[] lanelist = map.get(lane-1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++){
            if (i== lanelist.length) return false;
            if (lanelist[i].isOccupiedByCyberTruck || lanelist[i].terrain == Terrain.MUD || lanelist[i].terrain == Terrain.WALL
            || lanelist[i].terrain == Terrain.OIL_SPILL) return true;
        }
        return false;
    }
    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at reachable (speed) in length.
     **/
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState, int speed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

}
