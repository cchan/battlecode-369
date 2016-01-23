package team369;

import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class Scout extends Robot{
	public Scout(RobotController rc){
		super(rc);
	}
	public void loop() throws Exception{
		RobotInfo[] enemyRobots = rc.senseNearbyRobots(60, enemyTeam);
		int i = 0;
		for(; i < GameConstants.MESSAGE_SIGNALS_PER_TURN && i < enemyRobots.length; i++)
			sa.broadcastEnemyPosition(enemyRobots[i].location, 50);
		
		RobotInfo[] zombieRobots = rc.senseNearbyRobots(60, Team.ZOMBIE);
		for(int j = 0; j + i < GameConstants.MESSAGE_SIGNALS_PER_TURN && j < zombieRobots.length; j++)
			sa.broadcastZombiePosition(zombieRobots[j].location, 50);
		
		
        if (rc.isCoreReady()) {
            // Choose a direction to try to move in
        		//todo - how would I bait a group of zombies to move with me toward the enemy team?
        	Direction dir = null;
        	
    		if(hostility() > 8) //too dangerous, go away
    			dir = hostileDirection().opposite();
        	
        	if(dir == null || (dir == Direction.NONE && rand.nextBoolean()))
        		dir = directions[rand.nextInt(8)];
        	
        	squishMove(dir); //using this to avoid random walk loopback
        }
	}
}
