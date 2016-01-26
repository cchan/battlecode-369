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
		
        RobotInfo[] enemiesWithinRange = senseEnemyRobotsCached();
        if(enemiesWithinRange.length == 0)
        	enemiesWithinRange = senseZombieRobotsCached();
        
        
		double aggression = rc.getHealth()/rc.getType().maxHealth; //also account for infection
		rc.setIndicatorString(1,"areZombiesSpawning: "+areZombiesSpawning()+" Current: "+rc.getRoundNum());
        RobotInfo nearestEnemy = nearest(rc.getLocation(), enemiesWithinRange);
		if(hostileCount() == 1 && (nearestEnemy.type == RobotType.ZOMBIEDEN || nearestEnemy.type == RobotType.FASTZOMBIE))
			aggression = 100;
		if(areZombiesSpawning()){ //nearby (in terms of round#) a zombie spawn, run away from zombies
			aggression = -1;
			rc.setIndicatorString(2, "Scared! Round "+rc.getRoundNum());
		}
		else{
			aggression = Math.max(aggression, 0);
			rc.setIndicatorString(2, "Not scared.");
		}
		rc.setIndicatorString(0, "Aggression: "+aggression);
        
        if (enemiesWithinRange.length > 0){
        	if(rc.isWeaponReady() && rc.canAttackLocation(nearestEnemy.location)){
        		//todo - prioritize by low health
        		rc.attackLocation(nearestEnemy.location);
        		return;
        	}
        	else if(rc.isCoreReady()){
        		Direction dir = null;
        		int distToNearestEnemy = rc.getLocation().distanceSquaredTo(nearestEnemy.location);
        		if(hostileCount() > 1 && hostileHealthTotal() > friendlyHealthTotal()) //overwhelming power, run away
        			dir = hostileDirection().opposite();
        		else if(distToNearestEnemy > myAttackRange - 4 - aggression * 2 && distToNearestEnemy > 2) //too far, be aggressive
        			dir = rc.getLocation().directionTo(nearestEnemy.location);
        		else{ //so it'll circle around and kinda surround the enemy?
        			if(rand.nextBoolean()) //or you can just wait for the weapon cooldown
        				return;
        			else if(rand.nextBoolean())
        				dir = rc.getLocation().directionTo(nearestEnemy.location).rotateLeft().rotateLeft();
        			else
        				dir = rc.getLocation().directionTo(nearestEnemy.location).rotateRight().rotateRight();
        		}
        		
        		if(dir != null){
        			squishMove(dir);
        			return;
        		}
        	}
        }
        
        if (rc.isCoreReady()) {
            // Choose a direction to try to move in
        		//todo - how would I bait a group of zombies to move with me toward the enemy team?
        	Direction dir = null;
        	
        	for(Signal s : sa.allyMessageSignals){
        		int[] msg = s.getMessage();
        		if(SignalAdapter.isCommand(msg,SignalAdapter.Cmd.ZOMBIE_HOME)){
        			goalLoc = SignalAdapter.getLocation(msg);
        			break;
        		}
        		else if(SignalAdapter.isCommand(msg,SignalAdapter.Cmd.MOVE_HERE)){
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
	    		if(friendlyCount() < 15 - 10 * aggression) //too few friends, come back
	    			dir = friendlyDirection();
    		
        	if((dir == null || dir == Direction.NONE) && rand.nextBoolean()){
    			dir = directions[rand.nextInt(8)];
	        	if(rand.nextBoolean() || !clearIfRubble(dir)) //only try to clear the rubble half the time
	        		squishMove(dir);
    		}
        	else if(!(dir == null || dir == Direction.NONE))
        		squishMove(dir);
        	
        	return;
        }
	}
}
