package team369;

import battlecode.common.*;

import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

public class RobotPlayer {
	public static MapLocation nearest(RobotController rc, RobotInfo[] others){
    	MapLocation nearestLocation = others[0].location;
    	int shortestDistanceSquared = rc.getLocation().distanceSquaredTo(others[0].location);
    	for(int i = 1; i < others.length; i++){
    		int newdist = rc.getLocation().distanceSquaredTo(others[i].location);
    		if(shortestDistanceSquared > newdist){
    			shortestDistanceSquared = newdist;
    			nearestLocation = others[i].location;
    		}
    	}
    	return nearestLocation;
	}
	
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) {
        // You can instantiate variables here.
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        RobotType[] robotTypes = {RobotType.SOLDIER,
                RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, 
                RobotType.TURRET, RobotType.TURRET, RobotType.TURRET, RobotType.TURRET};
        Random rand = new Random(rc.getID());
        int myAttackRange = 0;
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
                    
                    // Check if this ARCHON's core is ready
                    if (rc.isCoreReady()) {
                    	RobotInfo[] neutralWithinRange = rc.senseNearbyRobots(2 /*adjacent squares*/,Team.NEUTRAL);
                    	if(neutralWithinRange.length > 0)
                    		rc.activate(neutralWithinRange[0].location);
                    	
                    	int moveable = 0;
                    	for(Direction d : directions)
                    		if(rc.canMove(d))
                    			moveable++;
                    	
                    	//RobotInfo[] hostiles = rc.senseHostileRobots(rc.getLocation(), 10);
                    	//for(RobotInfo r : hostiles);//Of all moveable locations, which has the fewest nearby hostiles? Always run away.
                    		//If can't run away, ... build turrets
                    	
                    	
                        if (moveable <= 2 || fate < 800) {
                            // Choose a random direction to try to move in, or look for parts
                        	MapLocation[] locs1 = rc.sensePartLocations(10);
                        	RobotInfo[] locs2 = rc.senseNearbyRobots(10,Team.NEUTRAL);
                        	MapLocation minLoc = null;
                        	int minDist = 12345;
                        	for(MapLocation loc : locs1){
                        		if(minDist > rc.getLocation().distanceSquaredTo(loc)){
                        			minDist = rc.getLocation().distanceSquaredTo(loc);
                        			minLoc = loc;
                        		}
                        	}
                        	for(RobotInfo loc : locs2){
                        		if(minDist > rc.getLocation().distanceSquaredTo(loc.location)){
                        			minDist = rc.getLocation().distanceSquaredTo(loc.location);
                        			minLoc = loc.location;
                        		}
                        	}
                        	
                        	
                            Direction dirToMove;
                            if(minDist != 12345)
                            	dirToMove = rc.getLocation().directionTo(minLoc);
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
                            if (rc.hasBuildRequirements(typeToBuild)) {
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
                    if(friendsWithinRange.length > 0)
                    	rc.repair(friendsWithinRange[0].location);
                    
                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        } else if (rc.getType() != RobotType.TURRET) {
            try {
                // Any code here gets executed exactly once at the beginning of the game.
                myAttackRange = rc.getType().attackRadiusSquared;
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
                    
                    if (fate % 5 == 3) {
                        // Send a normal signal
                        rc.broadcastSignal(80);
                    }

                    boolean done = false;

                    // If this robot type can attack, check for enemies within range and attack one
                    if (myAttackRange > 0) {
                        RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(1000,enemyTeam);
                        if(enemiesWithinRange.length == 0)enemiesWithinRange = rc.senseNearbyRobots(1000,Team.ZOMBIE);
                        
                        if (enemiesWithinRange.length > 0){
	                        MapLocation nearestEnemy = nearest(rc, enemiesWithinRange);
                        	if(rc.isWeaponReady() && rc.canAttackLocation(nearestEnemy)){
                        		rc.attackLocation(nearestEnemy);
    	                        done = true;
                        	}
                        	else if(rc.isCoreReady()){
                        		Direction dir;
                        		if(rc.getLocation().distanceSquaredTo(nearestEnemy) >= myAttackRange - 5)
                        			dir = rc.getLocation().directionTo(nearestEnemy);
                        		else
                        			dir = nearestEnemy.directionTo(rc.getLocation());
                        		
                        		if(rc.canMove(dir)){
                        			rc.move(dir);
                        			done = true;
                        		}
                        	}
                        }
                    }

                    if (!done) {
                        if (rc.isCoreReady()) {
                        	boolean alreadyMoved = false;
                        	if(rc.getHealth() < 100){
                        		RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
                        		if(enemiesWithinRange.length > 0){
                        			Direction dirToMove = rc.getLocation().directionTo(enemiesWithinRange[rand.nextInt(enemiesWithinRange.length)].location);
                                    if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                        // Too much rubble, so I should clear it
                                        rc.clearRubble(dirToMove);
                                        // Check if I can move in this direction
                                    } else if (rc.canMove(dirToMove)) {
                                        // Move
                                        rc.move(dirToMove);
                                        alreadyMoved = true;
                                    }
                        		}
                        	}
                            if (!alreadyMoved && fate < 600) {
                                // Choose a random direction to try to move in
                                Direction dirToMove = directions[fate % 8];
                                // Check the rubble in that direction
                                if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                    // Too much rubble, so I should clear it
                                    rc.clearRubble(dirToMove);
                                    // Check if I can move in this direction
                                } else if (rc.canMove(dirToMove)) {
                                    // Move
                                    rc.move(dirToMove);
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
        } else if (rc.getType() == RobotType.TURRET) {
            try {
                myAttackRange = rc.getType().attackRadiusSquared;
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