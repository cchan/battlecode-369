package team369;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Soldier extends Robot{
	public Soldier(RobotController rc) throws Exception{
		super(rc);
	}
	public void loop() throws Exception{
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
            		
            		Direction dir = null;
            		int distToNearestEnemy = rc.getLocation().distanceSquaredTo(nearestEnemy.location);
            		if(distToNearestEnemy <= nearestEnemy.type.attackRadiusSquared + 6 + aggression * 6) //too close, run away
            			dir = hostileDirection().opposite();
            		else if(distToNearestEnemy > myAttackRange - aggression * 2) //too far, be aggressive
            			dir = rc.getLocation().directionTo(nearestEnemy.location);
            		else //so it'll circle around and kinda surround the enemy?
            			if(rc.getID()%2 == 0)
            				dir = rc.getLocation().directionTo(nearestEnemy.location).rotateLeft().rotateLeft();
            			else
            				dir = rc.getLocation().directionTo(nearestEnemy.location).rotateRight().rotateRight();
            		
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
        	
        	//todo - Adjust these friendliness parameters to be higher with low health
        		//and also when a signal is heard, so people will group up
        		//also move toward the signal
        	
    		if(friendliness(rc.getLocation()) > 15) //too many friends, go away
    			dir = friendlyDirection().opposite();
    		else if(friendliness(rc.getLocation()) < 8) //too few friends, come back
    			dir = friendlyDirection();
        	
        	if(dir == null || (dir == Direction.NONE && rand.nextBoolean()))
        		dir = directions[rand.nextInt(8)];
        	
        	if(rand.nextBoolean() || !clearIfRubble(dir)) //only try to clear the rubble half the time
        		squishMove(dir);
        	
        	return;
        }
	}
}
