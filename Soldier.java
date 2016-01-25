package team369;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

public class Soldier extends Robot{
	public Soldier(RobotController rc) throws Exception{
		super(rc);
	}
	MapLocation goalLoc;
	public void loop() throws Exception{
		sa.fetch();
		
		
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
            		return;
            	}
            	else if(rc.isCoreReady()){
            		double aggression = rc.getHealth()/rc.getType().maxHealth; //also account for infection
            		if(nearestEnemy.type == RobotType.ZOMBIEDEN || nearestEnemy.type == RobotType.FASTZOMBIE)
            			aggression = 1000;
            		
            		
            		Direction dir = null;
            		int distToNearestEnemy = rc.getLocation().distanceSquaredTo(nearestEnemy.location);
            		if(distToNearestEnemy <= nearestEnemy.type.attackRadiusSquared + 6 - aggression * 6) //too close, run away
            			dir = hostileDirection().opposite();
            		else if(distToNearestEnemy > myAttackRange - aggression * 2 && distToNearestEnemy > 2) //too far, be aggressive
            			dir = rc.getLocation().directionTo(nearestEnemy.location);
            		else //so it'll circle around and kinda surround the enemy?
            			if(rand.nextBoolean()) //or you can just wait for the weapon cooldown
            				return;
            			else
            				dir = rc.getLocation().directionTo(nearestEnemy.location).rotateLeft().rotateLeft();
            		
            		if(dir != null){
            			squishMove(dir);
            			return;
            		}
            	}
            }
        }
        
        if (rc.isCoreReady()) {
            // Choose a direction to try to move in
        		//todo - how would I bait a group of zombies to move with me toward the enemy team?
        	Direction dir = null;
        	
        	for(Signal s : sa.allyMessageSignals){
        		int[] msg = s.getMessage();
        		if(SignalAdapter.isCommand(msg,SignalAdapter.Cmd.MOVE_HERE)){
        			goalLoc = SignalAdapter.getLocation(msg);
        			break;
        		}
        	}

        	if(goalLoc != null){
        		if(rc.getLocation().distanceSquaredTo(goalLoc) < 15)
        			goalLoc = null;
        		else
        			dir = rc.getLocation().directionTo(goalLoc);
        	}
        	
        	if(dir == null)
	    		if(friendliness(rc.getLocation()) < 7 + 5 * rc.getHealth()/rc.getType().maxHealth) //too few friends, come back
	    			dir = friendlyDirection();
    		
        	if((dir == null || dir == Direction.NONE) && rand.nextBoolean()){
    			dir = directions[rand.nextInt(8)];
	        	if(rand.nextBoolean() || !clearIfRubble(dir)) //only try to clear the rubble half the time
	        		squishMove(dir);
    		}
        	else
        		squishMove(dir);
        	
        	return;
        }
	}
}
