package team369;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Signal;

public class SignalAdapter {
	private static final int INT_LENGTH = 32; //bits
	private static final int COMMAND_LENGTH = 4; //bits
	private static final int COMMAND_MASK = 0xf0000000;
	public static final int COMMAND_REMAINING = INT_LENGTH - COMMAND_LENGTH; // bits
	
	public static final int CMD_ENEMY_POSITION = 0b0000;
	//broadcastCommand(CMD_ENEMY_POSITION, MapLocation.x, MapLocation.y)
	
	public static final int CMD_ZOMBIE_POSITION = 0b0001;
	//broadcastCommand(CMD_ZOMBIE_POSITION, MapLocation.x, MapLocation.y)
	
	
	private RobotController rc;
	public ArrayList<Signal> allyMessageSignals = new ArrayList<>(), 
			allyBasicSignals = new ArrayList<>(), 
			enemyMessageSignals = new ArrayList<>(), 
			enemyBasicSignals = new ArrayList<>();
	
	public SignalAdapter(RobotController rc){
		this.rc = rc;
	}
	public void fetch(){
		Signal[] raw = rc.emptySignalQueue();
		allyMessageSignals.clear();
		allyBasicSignals.clear();
		enemyMessageSignals.clear();
		enemyBasicSignals.clear();
		
		for(Signal s : raw){
			if(s.getTeam() == rc.getTeam()){
				if(s.getMessage() != null)
					allyMessageSignals.add(s);
				else
					allyBasicSignals.add(s);
			}
			else{
				if(s.getMessage() != null)
					enemyMessageSignals.add(s);
				else
					allyBasicSignals.add(s);
			}
		}
	}
	
	public void broadcastEnemyPosition(MapLocation loc, int broadcastRange) throws GameActionException{
		broadcastCommand(CMD_ENEMY_POSITION, loc.x, loc.y, broadcastRange);
	}	
	public void broadcastZombiePosition(MapLocation loc, int broadcastRange) throws GameActionException{
		broadcastCommand(CMD_ZOMBIE_POSITION, loc.x, loc.y, broadcastRange);
	}
	public void broadcastCommand(final int command, int data1, int data2, int broadcastRange) throws GameActionException{
		rc.broadcastMessageSignal((data1 | COMMAND_MASK) & ~(~command << COMMAND_REMAINING), data2, broadcastRange);
	}
	public int getCommand(int rawdata1){
		return rawdata1 >>> COMMAND_REMAINING;
	}
	public int getData1(int rawdata1){
		return rawdata1 & ~COMMAND_MASK;
	}
	public MapLocation[] getSignalingEnemyLocations(){
		MapLocation[] locs = new MapLocation[enemyMessageSignals.size() + enemyBasicSignals.size()];
		int i = 0;
		for(; i < enemyMessageSignals.size(); i++)
			locs[i] = enemyMessageSignals.get(i).getLocation();
		for(int j = 0; j < enemyBasicSignals.size(); j++)
			locs[i+j] = enemyBasicSignals.get(j).getLocation();
		return locs;
	}
}
