package playtracewriter;


import serialization.Observation;
import serialization.SerializableStateObservation;
import serialization.Types;

import java.util.*;

public class TickInfo {

	//Level variables
	double blockSize;
    Observation[][][] observations;
    Observation[][] fromAvatarSprites;

    //Game variables
    int tick;
    public float score;
    float scorechange = -9999;
    Types.WINNER winner;

    //Player variables
	double[] orientation;
	double[] player_pos;
	Types.ACTIONS action;

    TickInfo previousTick;
    List<String> tokenlist;

	public TickInfo(SerializableStateObservation sso, Types.ACTIONS action, TickInfo previousTick)
	{
		tokenlist = null;
		blockSize = sso.getBlockSize();
		this.storePlayerInformation(sso, action);
		this.storeLevelInformation(sso, action);
		this.storeGameInformation(sso, action);
		this.tick = sso.gameTick;
		this.previousTick = previousTick;
	}

	private void storePlayerInformation(SerializableStateObservation stateObs, Types.ACTIONS action){
		this.player_pos = stateObs.getAvatarPosition();
		this.orientation = stateObs.getAvatarOrientation();

		if (action != null)
			this.action = action;

		//stringbuilder.append("Health="+String.valueOf(stateObs.getAvatarHealthPoints()) + " ");
		//stringbuilder.append("Pos="+posToGrid(stateObs, stateObs.getAvatarPosition()) + " ");
		//stringbuilder.append("PosX="+posToGrid(stateObs, stateObs.getAvatarPosition()).split("/")[0] + " ");
		//stringbuilder.append("PosY="+posToGrid(stateObs, stateObs.getAvatarPosition()).split("/")[1] + " ");
		// stringbuilder.append("Resources="+stateObs.getAvatarResources().toString() + " ");
	}

    private void storeLevelInformation(SerializableStateObservation stateObs, Types.ACTIONS action){
        this.observations = stateObs.getObservationGrid();
        this.fromAvatarSprites = stateObs.getFromAvatarSpritesPositions();
    }

    private void storeGameInformation(SerializableStateObservation stateObs, Types.ACTIONS action){
        this.score = stateObs.getGameScore();
        if (previousTick != null)
        {
            this.scorechange = this.score - previousTick.score;
        }
        this.winner = stateObs.getGameWinner();
    }

    public List<String> getTokenList(){
		if (this.tokenlist == null){
			this.tokenlist = new LinkedList<>();
			this.tokenlist.add("Tick="+this.tick);
			playerPosToTokenlist(player_pos, tokenlist);
			this.tokenlist.add("Orientation=" + orientationToString(orientation));

			if (action != null)
				this.tokenlist.add("Action=" + this.action.toString());

            this.saveGameInformation(tokenlist);
			this.saveSurroundingObservations(tokenlist);
			this.savePlayerCreatedSprites(tokenlist);
		}
		return this.tokenlist;
	}

	public static String orientationToString(double[] orientation){
		if (orientation[0] == 0 && orientation[1] == 1)
			return "Up";
		if (orientation[0] == 0 && orientation[1] == -1)
			return "Down";
		if (orientation[0] == 1 && orientation[1] == 0)
			return "Right";
		if (orientation[0] == -1 && orientation[1] == 0)
			return "Left";

		return "" + orientation[0] + "," + orientation[1];
	}

	public void playerPosToTokenlist(double[] pos, List<String> tokenlist)
	{
		int x = (int) (pos[0]/blockSize);
		int y = (int) (pos[1]/blockSize);
		tokenlist.add("PosX="+x);
		tokenlist.add("PosY="+y);
	}

	private void saveSurroundingObservations(List<String> tokenlist)
	{

		double[] pos = this.player_pos;
		int x = (int) (pos[0]/this.blockSize);
		int y = (int) (pos[1]/this.blockSize);


		Observation[][][] obs = this.observations;

		if (x < 0 || x >= obs.length || y < 0 || y >= obs[0].length)
			return;

		//UP
		if(y - 1 >= 0)
		{
			for(Observation ob : obs[x][y-1])
			{
				tokenlist.add("AboveOfP=" + ob.itype);
			}
		}
		//UP
		if(y - 2 >= 0)
		{
			for(Observation ob : obs[x][y-2])
			{
				tokenlist.add("TwoAboveOfP=" + ob.itype);
			}
		}

		//UPLeft
		if(y - 1 >= 0 && x -1 >= 0 )
		{
			for(Observation ob : obs[x-1][y-1])
			{
				tokenlist.add("AboveLeftOfP=" + ob.itype);
			}
		}
		//UPRight
		if(y - 1 >= 0 && x +1 < obs.length )
		{
			for(Observation ob : obs[x+1][y-1])
			{
				tokenlist.add("AboveRightOfP=" + ob.itype);
			}
		}

		//Down
		if(y + 1 < obs[x].length)
		{
			for(Observation ob : obs[x][y+1])
			{
				tokenlist.add("BelowOfP=" + ob.itype);
			}
		}
		//2Down
		if(y + 2 < obs[x].length)
		{
			for(Observation ob : obs[x][y+2])
			{
				tokenlist.add("TwoBelowOfP=" + ob.itype);
			}
		}

		//BelowLeft
		if(y+1 < obs[x].length && x-1 >= 0 )
		{
			for(Observation ob : obs[x-1][y+1])
			{
				tokenlist.add("BelowLeftOfP=" + ob.itype);
			}
		}

		//BelowRight
		if(y+1 < obs[x].length && x+1 < obs.length )
		{
			for(Observation ob : obs[x+1][y+1])
			{
				tokenlist.add("BelowRightOfP=" + ob.itype);
			}
		}

		//Left
		if(x - 1 >= 0)
		{
			for(Observation ob : obs[x-1][y])
			{
				tokenlist.add("LeftOfP=" + ob.itype);
			}
		}
		//2Left
		if(x - 2 >= 0)
		{
			for(Observation ob : obs[x-2][y])
			{
				tokenlist.add("TwoLeftOfP=" + ob.itype);
			}
		}

		//Right
		if(x + 1 < obs.length)
		{
			for(Observation ob : obs[x+1][y])
			{
				tokenlist.add("RightOfP=" + ob.itype);
			}
		}
		//2Right
		if(x + 2 < obs.length)
		{
			for(Observation ob : obs[x+2][y])
			{
				tokenlist.add("TwoRightOfP=" + ob.itype);
			}
		}

		//Same
		for(Observation ob : obs[x][y])
		{
			tokenlist.add("SameAsP=" + ob.itype);
		}

	}

    private void saveGameInformation(List<String> tokenlist){
        tokenlist.add("Score=" + String.valueOf(this.score));
        if (scorechange != -9999)
            tokenlist.add("ScoreInc=" + this.scorechange);
        tokenlist.add("GameState="+this.winner.toString());
    }

	private void savePlayerCreatedSprites(List<String> tokenlist)
	{
		Observation[][] sprites = this.fromAvatarSprites;

		if(sprites == null) return;

		for(int i = 0; i < sprites.length; i++)
		{
			for(Observation obs : sprites[i])
			{
				tokenlist.add("PlayerSprite_atX="+obs.position.x);
				tokenlist.add("PlayerSprite_atY="+obs.position.y);
			}
		}
	}

}
