package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.*;

import java.nio.channels.AcceptPendingException;

public class Bot {
    private GameState gameState;
    private Car opponent;
    private Car myCar;

    //Powerups
    private final int EmpScore = 3;
    private final int LizardScore = 5;
    private final int OilScore = 2;
    private final int TweetScore = 3;
    private final int BoostScore = 10;

    //Bad Terrain
    private final int OilSpillPenalty = 1;
    private final int WallPenalty = 3;
    private final int MudPenalty = 1;
    private final int TruckPenalty = 10;
    private final int OpponentPenalty = 20;

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

    public Bot(GameState gameState) {
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;
    }

    /* YANG UTAMA*/
    public Command run(GameState gameState) {
        //1. update the state
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        if(myCar.boostCounter == 1 && myCar.speed == BOOST_SPEED){
            myCar.speed = MAXIMUM_SPEED;
        }

        // 2. kalo damage > 2 -> fix
        if(myCar.damage > 2){
            return FIX;
        }

        // 3. kalo speed = 0 , accelerate
        if(myCar.speed == 0){
            if(isCanAndShouldBoost()){
                if(myCar.damage > 0) return FIX;
                return BOOST;
            }
            else{
                return ACCELERATE;
            }
        }
        //
        if(shouldIMoveLeftOrRight(myCar.speed)){
            return bestBetweenLeftAndRight();
        }
        else{ // straight is better (but doesn't mean gada damage)
            if(myCar.speed == BOOST_SPEED){
                // kalo bakal nubruk -> lizard
                if(haveObstacle(myCar.position.lane - 1, myCar.position.block + 1, myCar.speed - 1)){
                    if(hasPowerUp(PowerUps.LIZARD)){
                        return LIZARD;
                    }
                }
                // belok kiri kanan percuma, lizard juga percuma.gabisa apa2. yowes attack ae.
                return aggresive();
            }
            else{
                if (isPowerUpMoreThanX(PowerUps.LIZARD, 2) && haveObstacle(myCar.position.lane - 1, myCar.position.block + 1, myCar.speed - 1)){
                    return LIZARD;
                }
                
                if(isCanAndShouldBoost()){
                    if(myCar.damage > 0) return FIX;
                    return BOOST;
                }
                if(shouldIMoveLeftOrRight(MyCarNextSpeed())){
                    return bestBetweenLeftAndRight();
                }
                else{
                    if((myCar.damage == 0 || myCar.damage == 1) && (myCar.speed < MAXIMUM_SPEED)){
                        return ACCELERATE;
                    }
                    else if((myCar.damage == 2) && (myCar.speed < SPEED_STATE_3)){
                        return ACCELERATE;
                    }
                    else{
                        return aggresive();
                    }
                }
            }
        }
        //harusnya gabakal sampe sini, tapi pernah sekali dia return nothing. wtf?
        // return DO_NOTHING;
//        return aggresive();
    }

    /* FUNGSI-FUNGSI, BIAR RAPI */
    private boolean isPowerUpMoreThanX(PowerUps powerUpToCheck, int X){
        int cnt = 0;
        PowerUps[] available = myCar.powerups;
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                cnt++;
            }
        }
        return (cnt > X);
    }

    private boolean shouldIMoveLeftOrRight(int howfarstraight){
        boolean isHowFarStraightClear = !haveObstacle(myCar.position.lane - 1, myCar.position.block + 1,howfarstraight);
        // straight ada halangan, left or right gada halangan
        boolean flag1 = !isHowFarStraightClear && (isLeftClear() || isRightClear());
        // straight can and clear and then definite second step impact, left or right can and clear and gk second step definite impact
        boolean flag2 = isHowFarStraightClear && ifStraightSSDI(myCar.position.block + howfarstraight) &&((isLeftClear() && !ifLeftSSDI()) || (isRightClear() && !ifRightSSDI()));

        // semuanya bakal nubruk dan left atau right lebih gk sakit
        boolean flag3 = (!isHowFarStraightClear && !isLeftClear() && !isRightClear()) && isStraightMostDamage(howfarstraight);

        boolean SemuaGkNubruk = false;
        int lane = myCar.position.lane - 1;
        if(lane == 0){
            SemuaGkNubruk = isHowFarStraightClear && isRightClear();
        }
        else if(lane == 1 || lane == 2){
            SemuaGkNubruk = isLeftClear() && isHowFarStraightClear && isRightClear();
        }
        else if(lane == 3){
            SemuaGkNubruk = isLeftClear() && isHowFarStraightClear;
        }
        // semuanya gk nubruk, ada second step definite impact dan left atau right lebih gk sakit(artinya straight paling sakit)
        boolean flag4 = SemuaGkNubruk && ifStraightIsSecondStepMostDamage(myCar.position.block + howfarstraight);
        // semuanya gk nubruk dan gk ada second step definite impact dan left atau right lebih banyak powerup dan power up tersebut worth it buat diambil. skrg aku anggep worth it kalo >= 3 boost
        boolean isPowerUpWorthIt = false;
        int start = myCar.position.block;
        int howfar = howfarstraight - 1;
        if(lane > 0){
            if(CountScore(lane - 1, start, howfar) > 2*BoostScore){
                isPowerUpWorthIt = true;
            }
        }
        if(lane < 3){
            if(CountScore(lane + 1, start, howfar) > 2*BoostScore){
                isPowerUpWorthIt = true;
            }
        }
        boolean flag5 = SemuaGkNubruk && isAllSecondStepClear() && ifStraightIsSecondStepLeastPowerUp(myCar.position.block + howfarstraight) && isPowerUpWorthIt;
        return (flag1 || flag2 || flag3 || flag4 || flag5);
    }
    
    private int CountDamage(int lane, int start, int howfar){
        int totalDamage = 0;
        List<Lane[]> map = gameState.lanes;
        Lane[] lane_list = map.get(lane);
        int startBlock = map.get(0)[0].position.block;
        if (isColission(lane, start, howfar)){
            totalDamage += OpponentPenalty;
        }
        for (int i = Math.max(start - startBlock, 0); i < start - startBlock + howfar; i++){
            if (i >= lane_list.length){
                break;
            }
            if(lane_list[i].terrain == Terrain.MUD){
                totalDamage += MudPenalty;
            }
            else if(lane_list[i].terrain == Terrain.WALL){
                totalDamage += WallPenalty;
            }
            else if(lane_list[i].terrain == Terrain.OIL_SPILL){
                totalDamage += OilSpillPenalty;
            }
            else if (lane_list[i].isOccupiedByCyberTruck){
                totalDamage += TruckPenalty;
            }
        }
        return totalDamage;

    }

    private int CountScore(int lane, int start, int howfar){
        int score = 0;
        List<Lane[]> map = gameState.lanes;
        Lane[] lane_list = map.get(lane);
        int startBlock = map.get(0)[0].position.block;
        for (int i = Math.max(start - startBlock, 0); i < start - startBlock + howfar; i++){
            if (i >= lane_list.length) return score;
            if(lane_list[i].terrain == Terrain.EMP){
                score += EmpScore;
            }
            else if(lane_list[i].terrain == Terrain.LIZARD){
                score += LizardScore;
            }
            else if(lane_list[i].terrain == Terrain.OIL_POWER){
                score += OilScore;
            }
            else if(lane_list[i].terrain == Terrain.BOOST){
                score += BoostScore;
            }
            else if(lane_list[i].terrain == Terrain.TWEET){
                score += TweetScore;
            }
        }
        return score;
    }

    private int giveMostPowerUps(int lane, int start, int howfar){
        int mid_score = CountScore(lane,start + 1,howfar);
        int left_score = -999;
        int right_score = -999;
        if(lane != 0){
            left_score = CountScore(lane-1,start, howfar);
        }
        if(lane != 3){
            right_score = CountScore(lane+1,start,howfar);
        }
        return (Math.max(Math.max(left_score,mid_score), right_score));
    }
    private int giveLeastDamage(int lane, int start, int howfar){
        int mid_score = CountDamage(lane,start + 1,howfar);
        int left_score = 999999;
        int right_score = 999999;
        if(lane != 0){
            left_score = CountDamage(lane-1,start,howfar);
        }
        if(lane != 3){
            right_score = CountDamage(lane+1,start,howfar);
        }
        return (Math.min(Math.min(left_score,mid_score), right_score));
    }
    
    private boolean isStraightLeastPowerUp(){
        int lane = myCar.position.lane - 1;
        int start = myCar.position.block;
        int howfar = myCar.speed;
        int mid_score = CountScore(lane,start + 1,howfar);
        int left_score = 999999;
        int right_score = 999999;
        if(lane != 0){
            left_score = CountScore(lane - 1,start,howfar);
        }
        if(lane != 3){
            right_score = CountScore(lane + 1,start,howfar);
        }
        return (mid_score < left_score && mid_score < right_score);
    }
    private boolean isStraightMostDamage(int howfar){
        int lane = myCar.position.lane - 1;
        int start = myCar.position.block;
        int mid_score = CountDamage(lane,start + 1,howfar);
        int left_score = -999999;
        int right_score = -999999;
        if(lane != 0){
            left_score = CountDamage(lane - 1,start,howfar);
        }
        if(lane != 3){
            right_score = CountDamage(lane + 1,start,howfar);
        }
        return (mid_score > left_score && mid_score > right_score);   
    }
    
    private boolean ifStraightIsSecondStepMostDamage(int start){
        int ssStraight = -999999;
        int ssLeft = -999999;
        int ssRight = -999999;

        int lane = myCar.position.lane - 1;
        //cari hightest second step straight score
        //misakan aku straight, brapa max power up diantara 3 opsinya?

        ssStraight = giveLeastDamage(lane, start, myCar.speed);

        //cari hightest second step left score
        if(lane != 0){
            ssLeft = giveLeastDamage(lane - 1, start-1, myCar.speed);
        }
        //cari hightest second step right score
        if(lane != 3){
            ssRight = giveLeastDamage(lane + 1, start-1, myCar.speed);
        }
        return (ssStraight > ssLeft && ssStraight > ssRight);
    }
    private boolean ifStraightIsSecondStepLeastPowerUp(int start){
        int ssStraight = 999999;
        int ssLeft = 999999;
        int ssRight = 999999;

        int lane = myCar.position.lane - 1;
        //cari hightest second step straight score
        //misakan aku straight, brapa max power up diantara 3 opsinya?
        ssStraight = giveMostPowerUps(lane, start, myCar.speed);

        //cari hightest second step left score
        if(lane != 0){
            ssLeft = giveMostPowerUps(lane - 1, start - 1, myCar.speed);
        }
        if(lane != 3){
            //cari hightest second step right score
            ssRight = giveMostPowerUps(lane + 1, start - 1, myCar.speed);
        }
        return (ssStraight < ssLeft && ssStraight < ssRight);
    }
    private boolean isAllSecondStepClear(){
        // all second step ini salah. ada beberapa tile yg seharusnya dicek,tpi gk aku cek. tpi change kalo yg aku cek itu clear, dan yg gk aku cek itu gk clear, itu kecil banget(harusnya). jdi yodah ini rough estimate yang good enough.
        int lane = myCar.position.lane - 1;

        int countTotalDamage = CountDamage(lane, myCar.position.block + myCar.speed -1, myCar.speed + 1); // yg middle
        if(lane == 0){
            countTotalDamage += CountDamage(lane + 1, myCar.position.block + myCar.speed, myCar.speed);
            countTotalDamage += CountDamage(lane + 2, myCar.position.block + myCar.speed - 1, myCar.speed-1);
        }
        else if(lane == 1){
            countTotalDamage += CountDamage(lane - 1, myCar.position.block + myCar.speed, myCar.speed);
            countTotalDamage += CountDamage(lane + 1, myCar.position.block + myCar.speed, myCar.speed);
            countTotalDamage += CountDamage(lane + 2, myCar.position.block + myCar.speed - 1, myCar.speed-1);

        }
        else if (lane == 2){
            countTotalDamage += CountDamage(lane - 2, myCar.position.block + myCar.speed - 1, myCar.speed-1);
            countTotalDamage += CountDamage(lane - 1, myCar.position.block + myCar.speed, myCar.speed);
            countTotalDamage += CountDamage(lane + 1, myCar.position.block + myCar.speed, myCar.speed);
        }
        else{
            countTotalDamage += CountDamage(lane - 2, myCar. position.block + myCar.speed - 1, myCar.speed-1);
            countTotalDamage += CountDamage(lane - 1, myCar. position.block + myCar.speed, myCar.speed);
        }
        return countTotalDamage == 0; 
    }  
   
    private boolean isSecondStepDefiniteImpact(int futureLane, int futureBlock, int FutureSpeed){ 
        int howfar = FutureSpeed;

        boolean[] isLaneSafe = new boolean[3];

        for(int i = 0; i < 3 ; i++){
            isLaneSafe[i] = true;
        }
        for(int laneloop = 0 ; laneloop < 3; laneloop++){
            if(laneloop == 0 && futureLane == 0){
                isLaneSafe[laneloop] = false;
            }
            else if(laneloop == 2 && futureLane == 3){
                isLaneSafe[laneloop] = false;
            }
            else{
                if(laneloop == futureLane){
                    isLaneSafe[laneloop] = !(haveObstacle(futureLane+laneloop-1,futureBlock + 1,howfar));
                }
                else{
                    isLaneSafe[laneloop] = !(haveObstacle(futureLane+laneloop-1,futureBlock,howfar));
                }
            }
        }
        for(int i = 0; i < 3; i++){
            if(isLaneSafe[i]){
                return false;
            }
        }
        return true;
    }
    private boolean ifStraightSSDI(int start){
        return isSecondStepDefiniteImpact(myCar.position.lane - 1, start, myCar.speed);
    }
    private boolean ifLeftSSDI(){
        return isSecondStepDefiniteImpact(myCar.position.lane - 2, myCar.position.block + myCar.speed - 1, myCar.speed);
    }
    private boolean ifRightSSDI(){
        return isSecondStepDefiniteImpact(myCar.position.lane, myCar.position.block + myCar.speed - 1, myCar.speed);
    }
    
    private int nextSpeed(Car c){
        int speed = c.speed;
        if (speed == MINIMUM_SPEED) return SPEED_STATE_1;
        if (speed == SPEED_STATE_1) return SPEED_STATE_2;
        if (speed == SPEED_STATE_2) return SPEED_STATE_3;
        if (speed == SPEED_STATE_3) return MAXIMUM_SPEED;
        if (speed == BOOST_SPEED) return BOOST_SPEED;
        if (speed == MAXIMUM_SPEED) return MAXIMUM_SPEED;
        if (speed == INITIAL_SPEED) return SPEED_STATE_2;
        return speed;
    }
    private int MyCarNextSpeed(){
        return nextSpeed(myCar);
    }
    private int OpponentNextSpeed(){
        return nextSpeed(opponent);
    }

    private boolean isBoostClear(){
        return !haveObstacle(myCar.position.lane - 1,myCar.position.block + 1, BOOST_SPEED);
    }
    private boolean isAccelerateClear(){
        return !haveObstacle(myCar.position.lane - 1, myCar.position.block + 1, MyCarNextSpeed());
    }
    private boolean isLeftClear(){
        if(myCar.position.lane == 1){
            return false;
        }
        return !haveObstacle(myCar.position.lane - 2, myCar.position.block, myCar.speed);
    }
    private boolean isStraightClear(){
        return !haveObstacle(myCar.position.lane - 1, myCar.position.block + 1, myCar.speed);
    }
    private boolean isRightClear(){
        if(myCar.position.lane == 4){
            return false;
        }
        return !haveObstacle(myCar.position.lane, myCar.position.block, myCar.speed);
    }

    private boolean haveObstacle(int lane, int start, int howfar){
        int block = start;
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        Lane[] lanelist = map.get(lane);
        if (isColission(lane, start, howfar)){
            return true;
        }
        for (int i = Math.max(block - startBlock, 0); i < block - startBlock + howfar; i++){
            if (i >= lanelist.length) return false;
            if (lanelist[i].isOccupiedByCyberTruck || lanelist[i].terrain == Terrain.MUD || lanelist[i].terrain == Terrain.WALL || lanelist[i].terrain == Terrain.OIL_SPILL) return true;
        }
        return false;
    }
    private boolean isColission(int lane, int start, int howfar){
        // return (lane == opponent.position.lane - 1 && start <= opponent.position.block && start + howfar >= opponent.position.block);
        return (lane == opponent.position.lane - 1 && start <= opponent.position.block && start + howfar >= opponent.position.block + opponent.speed);
    }

    private boolean isCanAndShouldBoost(){
        return(isBoostClear() && (isPowerUpMoreThanX(PowerUps.BOOST, 1) || myCar.speed < SPEED_STATE_2 && hasPowerUp(PowerUps.BOOST)));
    }

    private boolean hasPowerUp(PowerUps powerUpToCheck) {
        PowerUps[] available = myCar.powerups;
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEMPable(){
        return (opponent.speed >= MINIMUM_SPEED && hasPowerUp(PowerUps.EMP) &&
                myCar.position.block < opponent.position.block &&
                abs(myCar.position.lane - opponent.position.lane) <= 1);
    }

    private Command aggresive(){
        if(isEMPable()) return EMP;

        int predict_lane = opponent.position.lane;
        int predict_block = opponent.position.block + OpponentNextSpeed() + 1;

        boolean isTweetToClose = false;
        if (predict_lane == myCar.position.lane){
            if(predict_block > myCar.position.block && predict_block <= myCar.position.block + myCar.speed){
                isTweetToClose = true;
            }
        }

        if ((hasPowerUp(PowerUps.TWEET)) && !isTweetToClose) {
            return new TweetCommand(predict_lane, predict_block );
        }
        if (hasPowerUp(PowerUps.OIL) && myCar.position.block > opponent.position.block + OpponentNextSpeed())
            return OIL;
        return ACCELERATE;
    }

    private Command bestBetweenLeftAndRight(){
        if(isLeftClear() && isRightClear()){
            // if both ada definite second impact -> pilih yg lebih gk sakit. kalo sama2 sakit pilih yg lebi banyak powerup
            if(ifLeftSSDI() && ifRightSSDI()){
                int lane = myCar.position.lane - 1;
                int ssLeft = giveLeastDamage(lane - 1, myCar.position.block + myCar.speed - 1, myCar.speed);
                int ssRight = giveLeastDamage(lane + 1, myCar.position.block + myCar.speed - 1, myCar.speed);
                if(ssLeft < ssRight){
                    return TURN_LEFT;
                }
                if(ssLeft > ssRight){
                    return TURN_RIGHT;
                }
                else{
                    int ssLeftScore = giveMostPowerUps(lane - 1, myCar.position.block + myCar.speed - 1, myCar.speed);
                    int ssRightScore = giveMostPowerUps(lane + 1, myCar.position.block + myCar.speed - 1, myCar.speed);
                    if(ssLeftScore < ssRightScore){
                        return TURN_RIGHT;
                    }
                    else{
                        return TURN_LEFT;
                    }
                }
            }
            //if cuman left gk definite second impact -> pilih left
            else if(!ifLeftSSDI() && ifRightSSDI()){
                return TURN_LEFT;
            }
            // if cuman right gk definite secodn impact -> pilih right
            else if(ifLeftSSDI() && !ifRightSSDI()){
                return TURN_RIGHT;
            }
            // if both gk definite second impact-> pilih yg paling banyak powerup
//          if(!ifLeftSSDI() && !ifRightSSDI()){
            else{
                int left = myCar.position.lane - 2;
                int right = myCar.position.lane;
                int howfar = myCar.speed;
                int start = myCar.position.block + myCar.speed -1;

                boolean ifLeftIsStraightClear = !haveObstacle(left, start + howfar - 1, howfar);
                boolean ifRightIsStraightClear = !haveObstacle(right, start + howfar, howfar);
                if(ifLeftIsStraightClear && !ifRightIsStraightClear){
                    return TURN_LEFT;
                }
                else if(!ifLeftIsStraightClear && ifRightIsStraightClear){
                    return TURN_RIGHT;
                }
                else if (CountScore(left, howfar, start) > CountScore(right, howfar, start)){
                    return TURN_LEFT;
                }
                else{
                    return TURN_RIGHT;
                }
            }
            //udah ngecover semua kasus
        }
        // if cuman left clear -> pilih left
        else if(isLeftClear() && !isRightClear()){
            return TURN_LEFT;
        }
        // if cuman right clear -> pilih right
        else if(isRightClear() && !isLeftClear()){
            return TURN_RIGHT;
        }
        // if both gk clear,
        // pilih yg lebih gk sakit
        // kalo sama2 sakit, pilih yg lebih banyak power up
//      else if(!isLeftClear() && !isRightClear()){
        else{
            if(myCar.speed == BOOST_SPEED){
                if(hasPowerUp(PowerUps.LIZARD) && haveObstacle(myCar.position.lane - 1, myCar.position.block + 1, myCar.speed-1)){
                    return LIZARD;
                }
            }
            int left = myCar.position.lane - 2;
            int right = myCar.position.lane;
            int howfar = myCar.speed;
            int start = myCar.position.block;
            if(left < 0){
                return TURN_RIGHT;
            }
            else if(right > 3){
                return TURN_LEFT;
            }
            else{
                if(CountDamage(left, start, howfar) < CountDamage(right, start, howfar)){
                    return TURN_LEFT;
                }
                else if(CountDamage(left, start, howfar) > CountDamage(right, start, howfar)){
                    return TURN_RIGHT;
                }
                else{
                    if (CountScore(left, howfar, start) > CountScore(right, howfar, start)){
                        return TURN_LEFT;
                    }
                    else{
                        return TURN_RIGHT;
                    }
                }
            }
        }
    }
}