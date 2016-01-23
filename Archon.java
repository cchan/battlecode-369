package team369;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Archon extends Robot {
	RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
            RobotType.TURRET, RobotType.TURRET, RobotType.TURRET, RobotType.TURRET};
	public Archon(RobotController rc){
		super(rc);
	}
	public void loop() throws Exception{
		{
			RobotInfo[] enemyRobots = rc.senseNearbyRobots(60, enemyTeam);
			int i = 0;
			for(; i < GameConstants.MESSAGE_SIGNALS_PER_TURN && i < enemyRobots.length; i++)
				sa.broadcastEnemyPosition(enemyRobots[i].location, 50);
			
			RobotInfo[] zombieRobots = rc.senseNearbyRobots(60, Team.ZOMBIE);
			for(int j = 0; j + i < GameConstants.MESSAGE_SIGNALS_PER_TURN && j < zombieRobots.length; j++)
				sa.broadcastZombiePosition(zombieRobots[j].location, 50);
		}
		
        // Check if this ARCHON's core is ready
        if (rc.isCoreReady()) {
        	RobotInfo[] hostiles = rc.senseHostileRobots(rc.getLocation(), 40);
        	if(hostiles.length > 0){
    			squishMove(nearest(rc.getLocation(),hostiles).location.directionTo(rc.getLocation()));
    			return;
        	}
        	
        	int moveable = 0;
        	for(Direction d : directions)
        		if(rc.canMove(d))
        			moveable++;
        	
        	RobotInfo[] neutralWithinRange = rc.senseNearbyRobots(2 /*adjacent squares*/,Team.NEUTRAL);
        	if(neutralWithinRange.length > 0){
        		rc.activate(neutralWithinRange[0].location);
        		return;
        	}
        	else if (moveable <= 2 || rand.nextInt(10) < (rc.getTeamParts()>300?9:6)) { //if over 300 parts, build more!
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
                    return;
                    // Check if I can move in this direction
                } else if (rc.canMove(dirToMove)) {
                    // Move
                    rc.move(dirToMove);
                    return;
                }
            } else {
                // Choose a random unit to build
            	RobotType typeToBuild;
            	//if(rc.getRoundNum() < 150)typeToBuild = RobotType.TURRET;
            	//else 
            		typeToBuild = robotTypes[rand.nextInt(robotTypes.length)];
                // Check for sufficient parts
                if (rc.isCoreReady() && rc.hasBuildRequirements(typeToBuild)) {
                    // Choose a random direction to try to build in
                    Direction dirToBuild = directions[rand.nextInt(8)];
                    for (int i = 0; i < 8; i++) {
                        // If possible, build in this direction
                        if (rc.canBuild(dirToBuild, typeToBuild)) {
                        	rc.build(dirToBuild, typeToBuild);
                            return;
                        } else {
                            // Rotate the direction to try
                            dirToBuild = dirToBuild.rotateLeft();
                        }
                    }
                }
            }
        }
        
        //Repair
        RobotInfo[] friendsWithinRange = rc.senseNearbyRobots(myAttackRange, myTeam);
        for(int i = 0; i < friendsWithinRange.length; i++)
        	if(friendsWithinRange[i].type!=RobotType.ARCHON && friendsWithinRange[i].health < friendsWithinRange[i].maxHealth){
        		rc.repair(friendsWithinRange[i].location);
        		break;
        	}
	}
}
