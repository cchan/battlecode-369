package team369;

import battlecode.common.*;

import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

public class RobotPlayer {
	public static final int estimationSenseRange = 100;
	public static int dangerosity(RobotController rc, MapLocation loc){
		return rc.senseHostileRobots(loc,estimationSenseRange).length;
	}
	public static int friendliness(RobotController rc, MapLocation loc){
		return rc.senseNearbyRobots(loc, estimationSenseRange, rc.getTeam()).length;
	}
	public static Direction averageDirection(Direction[] dirs){
		int totaldx = 0, totaldy = 0;
		for(Direction d : dirs){
			totaldx += d.dx;
			totaldy += d.dy;
		}
		return new MapLocation(0,0).directionTo(new MapLocation(totaldx, totaldy));
	}
	public static Direction averageDirectionToRobots(MapLocation here, RobotInfo[] robots){
		int totaldx = 0, totaldy = 0;
		for(RobotInfo r : robots){
			Direction d = here.directionTo(r.location);
			totaldx += d.dx;
			totaldy += d.dy;
		}
		return new MapLocation(0,0).directionTo(new MapLocation(totaldx, totaldy));
	}
	public static Direction friendlyDirection(RobotController rc){
		return averageDirectionToRobots(rc.getLocation(), rc.senseNearbyRobots(estimationSenseRange,rc.getTeam()));
	}
	public static Direction hostileDirection(RobotController rc){
		return averageDirectionToRobots(rc.getLocation(), rc.senseHostileRobots(rc.getLocation(), estimationSenseRange));
	}
	public static RobotInfo nearest(MapLocation here, RobotInfo[] others){
		if(others.length == 0)return null;
    	int shortestI = 0;
    	int shortestDistanceSquared = here.distanceSquaredTo(others[0].location);
    	for(int i = 1; i < others.length; i++){
    		int newdist = here.distanceSquaredTo(others[i].location);
    		if(shortestDistanceSquared > newdist){
    			shortestDistanceSquared = newdist;
    			shortestI = i;
    		}
    	}
    	return others[shortestI];
	}
	public static MapLocation nearest(MapLocation here, MapLocation[] others){
		if(others.length == 0)return null;
    	int shortestI = 0;
    	int shortestDistanceSquared = here.distanceSquaredTo(others[0]);
    	for(int i = 1; i < others.length; i++){
    		int newdist = here.distanceSquaredTo(others[i]);
    		if(shortestDistanceSquared > newdist){
    			shortestDistanceSquared = newdist;
    			shortestI = i;
    		}
    	}
    	return others[shortestI];
	}
	public static boolean squishMove(RobotController rc, Direction dir) throws GameActionException{
		if(rc.canMove(dir))
			rc.move(dir);
		else if(rc.canMove(dir.rotateLeft()))
			rc.move(dir.rotateLeft());
		else if(rc.canMove(dir.rotateRight()))
			rc.move(dir.rotateRight());
		else if(rc.canMove(dir.rotateLeft().rotateLeft()))
			rc.move(dir.rotateLeft().rotateLeft());
		else if(rc.canMove(dir.rotateRight().rotateRight()))
			rc.move(dir.rotateRight().rotateRight());
		else
			return false;
		return true;
	}
	public static boolean squishMoveWithRubble(RobotController rc, Direction dir) throws GameActionException{
		if(rc.canMove(dir))
			rc.move(dir);
		else if(rc.canMove(dir.rotateLeft()))
			rc.move(dir.rotateLeft());
		else if(rc.canMove(dir.rotateRight()))
			rc.move(dir.rotateRight());
		else if(rc.senseRubble(rc.getLocation().add(dir)) > GameConstants.RUBBLE_OBSTRUCTION_THRESH)
			rc.clearRubble(dir);
		else if(rc.senseRubble(rc.getLocation().add(dir.rotateLeft())) > GameConstants.RUBBLE_OBSTRUCTION_THRESH)
			rc.clearRubble(dir.rotateLeft());
		else if(rc.senseRubble(rc.getLocation().add(dir.rotateRight())) > GameConstants.RUBBLE_OBSTRUCTION_THRESH)
			rc.clearRubble(dir.rotateRight());
		else
			return false;
		return true;
	}
	
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) {
    	//todo - something based on ant behavior?
        // You can instantiate variables here.
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        RobotType[] robotTypes = {RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
                RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET, RobotType.TURRET};
        Random rand = new Random(rc.getID());
        int myAttackRange = rc.getType().attackRadiusSquared;
        Team myTeam = rc.getTeam();
        Team enemyTeam = myTeam.opponent();
        
        if (rc.getType() == RobotType.ARCHON) {
            try {
                // Any code here gets executed exactly once at the beginning of the game.
            } catch (Exception e) {
                // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
                // Caught exceptions will result in a bytecode penalty.
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                    int fate = rand.nextInt(1000);
                    boolean done = false;
                    
                    // Check if this ARCHON's core is ready
                    if (rc.isCoreReady()) {
                    	RobotInfo[] hostiles = rc.senseHostileRobots(rc.getLocation(), 40);
                    	if(hostiles.length > 0)
                			done = squishMove(rc, nearest(rc.getLocation(),hostiles).location.directionTo(rc.getLocation()));
                    	
                    	int moveable = 0;
                    	for(Direction d : directions)
                    		if(rc.canMove(d))
                    			moveable++;
                    	
                    	RobotInfo[] neutralWithinRange = rc.senseNearbyRobots(2 /*adjacent squares*/,Team.NEUTRAL);
                    	if(!done && neutralWithinRange.length > 0)
                    		rc.activate(neutralWithinRange[0].location);
                    	else if (!done && (moveable <= 2 || fate < (rc.getTeamParts()>300?900:600))) { //if over 300 parts, build more!
                            // Choose a random direction to try to move in, or look for parts
                        	MapLocation[] parts = rc.sensePartLocations(10);
                        	RobotInfo[] neutrals = rc.senseNearbyRobots(10,Team.NEUTRAL);
                        	MapLocation toMove = null;
                        	if(neutrals.length > 0)
                        		toMove = nearest(rc.getLocation(), neutrals).location;
                        	else if(parts.length > 0)
                        		toMove = nearest(rc.getLocation(), parts);
                        	
                        	Direction dirToMove;
                        	if(toMove != null)
                            	dirToMove = rc.getLocation().directionTo(toMove);
                        	else
                        		dirToMove = directions[rand.nextInt(8)];
                            // Check the rubble in that direction
                            double rubble = rc.senseRubble(rc.getLocation().add(dirToMove));
                            if (rubble > 5000){
                            	dirToMove = directions[rand.nextInt(8)];
                            	rubble = rc.senseRubble(rc.getLocation().add(dirToMove));
                            }
                            
                            if (rubble >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                // Too much rubble, so I should clear it
                                rc.clearRubble(dirToMove);
                                // Check if I can move in this direction
                            } else if (rc.canMove(dirToMove)) {
                                // Move
                                rc.move(dirToMove);
                            }
                        } else {
                            // Choose a random unit to build
                        	RobotType typeToBuild;
                        	//if(rc.getRoundNum() < 150)typeToBuild = RobotType.TURRET;
                        	//else 
                        		typeToBuild = robotTypes[fate % robotTypes.length];
                            // Check for sufficient parts
                            if (rc.isCoreReady() && rc.hasBuildRequirements(typeToBuild)) {
                                // Choose a random direction to try to build in
                                Direction dirToBuild = directions[rand.nextInt(8)];
                                for (int i = 0; i < 8; i++) {
                                    // If possible, build in this direction
                                    if (rc.canBuild(dirToBuild, typeToBuild)) {
                                    	rc.build(dirToBuild, typeToBuild);
                                        break;
                                    } else {
                                        // Rotate the direction to try
                                        dirToBuild = dirToBuild.rotateLeft();
                                    }
                                }
                            }
                        }
                    }
                    
                    RobotInfo[] friendsWithinRange = rc.senseNearbyRobots(myAttackRange, myTeam);
                    for(int i = 0; i < friendsWithinRange.length; i++)
                    	if(friendsWithinRange[i].type!=RobotType.ARCHON && friendsWithinRange[i].health < friendsWithinRange[i].maxHealth){
                    		rc.repair(friendsWithinRange[i].location);
                    		break;
                    	}
                    
                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        } else if (rc.getType() != RobotType.TURRET) {
        	//todo - since vipers infect, send viper raids
            try {
                // Any code here gets executed exactly once at the beginning of the game.
            } catch (Exception e) {
                // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
                // Caught exceptions will result in a bytecode penalty.
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            
            while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                    boolean done = false;

                    // If this robot type can attack, check for enemies within range and attack one
                    if (myAttackRange > 0) {
                        RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(1000,enemyTeam);
                        if(enemiesWithinRange.length == 0)
                        	enemiesWithinRange = rc.senseNearbyRobots(1000,Team.ZOMBIE);
                        
                        if (enemiesWithinRange.length > 0){
	                        RobotInfo nearestEnemy = nearest(rc.getLocation(), enemiesWithinRange);
                        	if(rc.isWeaponReady() && rc.canAttackLocation(nearestEnemy.location)){
                        		//todo - prioritize by low health
                        		rc.attackLocation(nearestEnemy.location);
    	                        done = true;
                        	}
                        	else if(rc.isCoreReady()){
                        		double aggression = rc.getHealth()/rc.getType().maxHealth; //also account for infection
                        		
                        		Direction dir = null;
                        		int distToNearestEnemy = rc.getLocation().distanceSquaredTo(nearestEnemy.location);
                        		if(distToNearestEnemy <= nearestEnemy.type.attackRadiusSquared + 6 + aggression * 6) //too close, run away
                        			dir = hostileDirection(rc).opposite();
                        		else if(distToNearestEnemy > myAttackRange - aggression * 2) //too far, be aggressive
                        			dir = rc.getLocation().directionTo(nearestEnemy.location);
                        		else //so it'll circle around and kinda surround the enemy?
                        			if(rc.getID()%2 == 0)
                        				dir = rc.getLocation().directionTo(nearestEnemy.location).rotateLeft().rotateLeft();
                        			else
                        				dir = rc.getLocation().directionTo(nearestEnemy.location).rotateRight().rotateRight();
                        		
                        		if(dir != null)
                        			done = squishMove(rc, dir);
                        	}
                        }
                    }
                    
                    if (!done && rc.isCoreReady()) {
                        // Choose a direction to try to move in
                    		//todo - how would I bait a group of zombies to move with me toward the enemy team?
                    	Direction dir = null;
                    	
                    	//todo - Adjust these friendliness parameters to be higher with low health
                    		//and also when a signal is heard, so people will group up
                    		//also move toward the signal
                    	
                		if(friendliness(rc,rc.getLocation()) > 15) //too many friends, go away
                			dir = friendlyDirection(rc).opposite();
                		else if(friendliness(rc,rc.getLocation()) < 8) //too few friends, come back
                			dir = friendlyDirection(rc);
                    	
                    	if(dir == null || dir == Direction.NONE && rand.nextBoolean())
                    		dir = directions[rand.nextInt(8)];
                    	
                		done = squishMoveWithRubble(rc, dir);
                    }
                    
                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        } else if (rc.getType() == RobotType.TURRET) {
            try {
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                    // If this robot type can attack, check for enemies within range and attack one
                    if (rc.isWeaponReady()) {
                        RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
                        RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
                        if (enemiesWithinRange.length > 0) {
                            for (RobotInfo enemy : enemiesWithinRange) {
                                // Check whether the enemy is in a valid attack range (turrets have a minimum range)
                                if (rc.canAttackLocation(enemy.location)) {
                                    rc.attackLocation(enemy.location);
                                    break;
                                }
                            }
                        } else if (zombiesWithinRange.length > 0) {
                            for (RobotInfo zombie : zombiesWithinRange) {
                                if (rc.canAttackLocation(zombie.location)) {
                                    rc.attackLocation(zombie.location);
                                    break;
                                }
                            }
                        }
                    }

                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}