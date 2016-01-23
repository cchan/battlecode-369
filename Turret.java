package team369;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Signal;
import battlecode.common.Team;

public class Turret extends Robot {
	public Turret(RobotController rc){
		super(rc);
	}
	public void loop() throws Exception{
		sa.fetch();
		
        // If this robot type can attack, check for enemies within range and attack one
        if (rc.isWeaponReady()) {
            MapLocation enemyLoc = getBestAttackableHostileLocation(enemyTeam);
            if (enemyLoc != null){
            	rc.attackLocation(enemyLoc);
            	return;
            }
            
            enemyLoc = getBestAttackableHostileLocation(Team.ZOMBIE);
            if (enemyLoc != null) {
            	rc.attackLocation(enemyLoc);
                return;
            }
        }
	}
	public MapLocation getBestAttackableHostileLocation(Team team){
		RobotInfo[] visibleEnemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared,team);
		if(visibleEnemies.length > 0){
			double minHealth = Double.MAX_VALUE;
			int minIndex = -1;
			for(int i = 0; i < visibleEnemies.length; i++){
				if(visibleEnemies[i].health < minHealth && rc.canAttackLocation(visibleEnemies[i].location)){
					minHealth = visibleEnemies[i].health;
					minIndex = i;
				}
			}
			if(minIndex != -1)
				return visibleEnemies[minIndex].location;
		}
		
		if(team == enemyTeam){
			for(Signal s : sa.enemyMessageSignals)
				if(rc.canAttackLocation(s.getLocation()))
					return s.getLocation();
			
			for(Signal s : sa.enemyBasicSignals)
				if(rc.canAttackLocation(s.getLocation()))
					return s.getLocation();
		}
		
		for(Signal s : sa.allyMessageSignals){
			int[] msg = s.getMessage();
			if(team == enemyTeam && sa.getCommand(msg[0]) == SignalAdapter.CMD_ENEMY_POSITION || team == Team.ZOMBIE && sa.getCommand(msg[0]) == SignalAdapter.CMD_ZOMBIE_POSITION){
				MapLocation decodedLoc = new MapLocation(sa.getData1(msg[0]), msg[1]);
				if(rc.canAttackLocation(decodedLoc))
					return decodedLoc;
			}
		}
		
		return null;
	}
}
