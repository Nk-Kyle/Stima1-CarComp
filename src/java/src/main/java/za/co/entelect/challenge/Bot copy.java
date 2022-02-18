// package za.co.entelect.challenge;

// import za.co.entelect.challenge.command.*;
// import za.co.entelect.challenge.entities.*;
// import za.co.entelect.challenge.enums.PowerUps;
// import za.co.entelect.challenge.enums.Terrain;

// import java.util.*;

// import static java.lang.Math.max;

// public class Bot {

//     private static final int maxSpeed = 9;
//     private List<Command> directionList = new ArrayList<>();

//     private Random random;
//     private GameState gameState;
//     private Car opponent;
//     private Car myCar;

//     private final static Command ACCELERATE = new AccelerateCommand();
//     private final static Command LIZARD = new LizardCommand();
//     private final static Command OIL = new OilCommand();
//     private final static Command BOOST = new BoostCommand();
//     private final static Command EMP = new EmpCommand();
//     private final static Command FIX = new FixCommand();

//     private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
//     private final static Command TURN_LEFT = new ChangeLaneCommand(-1);


//     public Bot(Random random, GameState gameState) {
// //        this.random = new SecureRandom();
//         directionList.add(TURN_LEFT);
//         directionList.add(TURN_RIGHT);
//     }

//     /*
//     priority:
//     1. kalo rusak,benerin
//     2. kalo ada cyber truck, hindari
//     3. kalo ada wall, hindari
//     4. kalo bisa nyerang, serang.
//     5. kalo ada mud, hindari.
//     6. kalo ada boost, pake.
//     7. accelerate

//     pergi : kalo ada lizard, pake lizard. kalo gada, turn left turn right.
//     serang :
//         kalo punya cybertruck, drop.
//         kalo lawanku ada di belakangku, drop oil
//         kalo lawanku didepanku dan deket, emp

//     next coba lagi:
//     gimana kalo step 7 itu yg lebih aneh2. let's say dia ngecek di depannya ada berapa 'halangan'
//     kalo banyak, pindah lane aja.
//     * */


//     public Command run(GameState gameState) {
//         this.gameState = gameState;
//         this.myCar = gameState.player;
//         this.opponent = gameState.opponent;

//         Car myCar = gameState.player;
//         Car opponent = gameState.opponent;
        
//         List<Lane[]> map = gameState.lanes;
//         Lane[] lanelist = map.get(myCar.position.lane-1);
        
//         List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
//         List<Object> nextBlocks = blocks.subList(0,1);





//         // 1
//         if (hasPowerUp(PowerUps.TWEET, myCar.powerups)){
//             return new TweetCommand(opponent.position.lane,opponent.position.block + opponent.speed + 5);
//         }
//         //Fix first if too damaged to move
//         if(myCar.damage >= 5) {
//             return FIX;
//         }

//         if(myCar.speed <= 1){
//             return ACCELERATE;
//         }
//         // 2
//         //has
//         if(hasCyberTruck()){
//             return goAway();
//         }
//         // 3
//         if(nextBlocks.contains(Terrain.WALL)){
//             return goAway();
//         }

//         // 4
//         if(canAttack()){
//             return attack();
//         }
//         // 5
//         if(nextBlocks.contains(Terrain.MUD)){
//             return goAway();
//         }
//         // 6
//         //Basic improvement logic
//         if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
//             return BOOST;
//         }

//         // 7
//         // kalo gabisa apa2, accelerate ae
//         return ACCELERATE;
//     }

//     /**
//      * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
//      * traversed at max speed.
//      **/

//     /* FUNGSI-FUNGSI, BIAR RAPI */


//     private boolean hasCyberTruck() {


//         int block = myCar.position.block;
//         List<Lane[]> map = gameState.lanes;
//         int startBlock = map.get(0)[0].position.block;
//         Lane[] lanelist = map.get(myCar.position.lane-1);
//         for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed; i++) {
//             if (i == lanelist.length) return false;
//             if (lanelist[i].isOccupiedByCyberTruck) return true;
//         }
//         return false;
//     }
//     private Command attack(){
//         //kalo punya cybertruck, drop
//         if (hasPowerUp(PowerUps.TWEET, myCar.powerups)){
//             return new TweetCommand(opponent.position.lane,opponent.position.block + opponent.speed + 5);
//         }
//         //kalo punya oil dan ada lawan di belakangku, drop
//         if (hasPowerUp(PowerUps.OIL, myCar.powerups) && (opponent.position.lane == myCar.position.lane) && (opponent.position.block < myCar.position.block)){
//             return OIL;
//         }
//         // kalo punya emp dan ada lawan di depanku, emp
//         if (hasPowerUp(PowerUps.EMP, myCar.powerups) && (opponent.position.lane == myCar.position.lane) && (opponent.position.block - myCar.position.block < myCar.speed)){
//             return EMP;
//         }
//         return ACCELERATE;
//     }
//     private Command goAway(){
//         // kalo ada lizard, pake
//         if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
//             return LIZARD;
//         }

//         if(myCar.position.lane == 1){
//             return TURN_RIGHT;
//         }
//         else{
//             return TURN_LEFT;
//         }
// //        return ACCELERATE;
//     }
//     private boolean canAttack(){
//         //kalo punya cybertruck, drop
//        boolean attack1 = hasPowerUp(PowerUps.TWEET,myCar.powerups);
//         //kalo punya oil dan ada lawan di belakangku, drop
//         boolean attack2 = hasPowerUp(PowerUps.OIL, myCar.powerups) && (opponent.position.lane == myCar.position.lane) && (opponent.position.block < myCar.position.block);
//         // kalo punya emp dan ada lawan di depanku, emp
//         boolean attack3 = hasPowerUp(PowerUps.EMP, myCar.powerups) && (opponent.position.lane == myCar.position.lane) && (opponent.position.block - myCar.position.block < myCar.speed);
//         return  attack1 && attack2 && attack3;
//     }
//     private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
//         for (PowerUps powerUp: available) {
//             if (powerUp.equals(powerUpToCheck)) {
//                 return true;
//             }
//         }
//         return false;
//     }

//     private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
//         List<Lane[]> map = gameState.lanes;
//         List<Object> blocks = new ArrayList<>();
//         int startBlock = map.get(0)[0].position.block;

//         Lane[] laneList = map.get(lane - 1);
//         for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
//             if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
//                 break;
//             }

//             blocks.add(laneList[i].terrain);

//         }
//         return blocks;
//     }

// }
