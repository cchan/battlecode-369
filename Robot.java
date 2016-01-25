package team369;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public abstract class Robot {
	Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	Random rand;
	int myAttackRange;
	Team myTeam, enemyTeam;
	RobotController rc;
	SignalAdapter sa;
	
	protected Robot(RobotController rc){
        rand = new Random(rc.getID());
        myAttackRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        sa = new SignalAdapter(rc);
        this.rc = rc;
        rc.emptySignalQueue(); //receives signals during construction, so get rid of those
	}
	public abstract void loop() throws Exception;
	
	public static final int estimationSenseRange = 100;
	public int hostility(MapLocation loc){
		return rc.senseHostileRobots(loc,estimationSenseRange).length;
	}
	public int hostility(){
		return hostility(rc.getLocation());
	}
	public int friendliness(MapLocation loc){
		return rc.senseNearbyRobots(loc, estimationSenseRange, rc.getTeam()).length;
	}
	public int friendliness(){
		return friendliness(rc.getLocation());
	}
	public RobotInfo nearestHostile(){ //todo - use this in Scout to avoid ALL enemy units as much as possible
		return nearest(rc.getLocation(),rc.senseNearbyRobots(rc.getLocation(),estimationSenseRange, enemyTeam));
	}
	public RobotInfo nearestFriendly(){
		return nearest(rc.getLocation(),rc.senseNearbyRobots(rc.getLocation(),estimationSenseRange, myTeam));
	}
	public Direction friendlyDirection(){
		return averageDirectionToRobots(rc.getLocation(), rc.senseNearbyRobots(estimationSenseRange, myTeam));
	}
	public Direction hostileDirection(){
		return averageDirectionToRobots(rc.getLocation(), rc.senseHostileRobots(rc.getLocation(), estimationSenseRange));
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
	
	
	public static final int[] possibleDirections = {0, 1, -1, 2, -2, 3, -3, 4};
	public ArrayList<MapLocation> pastLocations = new ArrayList<MapLocation>();
	public int patience = 30;
	public void squishMove(Direction dir) throws GameActionException{
		if(patience <= 0){
			if(!clearIfRubble(dir) && rc.canMove(dir)){
				rc.move(dir);
				patience = Math.min(patience + 10, 30);
			}
			return;
		}else{
			for(int d : possibleDirections){
				Direction candidateDirection = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(candidateDirection) && !pastLocations.contains(rc.getLocation().add(candidateDirection))){
					pastLocations.add(rc.getLocation().add(candidateDirection));
					if(pastLocations.size() > 10)
						pastLocations.remove(0);
					rc.move(candidateDirection);
					patience = Math.min(patience + 10, 30);
					return;
				}
			}
		}
		// in case it does something stupid and gets itself stuck
		patience -= 5;
		if(pastLocations.size() > 0)
			pastLocations.remove(0);
	}
	public static final double maxClearableRubble = 100000;
	public boolean clearIfRubble(Direction dir) throws GameActionException{
		double rubble = rc.senseRubble(rc.getLocation().add(dir));
		if(rubble > GameConstants.RUBBLE_OBSTRUCTION_THRESH
				&& rubble < maxClearableRubble){
			rc.clearRubble(dir);
			return true;
		}
		else
			return false;
	}
}
