package playtracewriter;

import serialization.SerializableStateObservation;
import serialization.Types;

/**
 * Created by dockhorn on 28.11.2017.
 */
public abstract class PlaytraceTracker {

    public abstract void storeTick(SerializableStateObservation so, Types.ACTIONS action);

    public abstract TickInfo getLastStoredTick();

    public abstract Transaction getLatestTransaction();

    public abstract String getLatestTransactionString();

    public abstract void initNewLevel(SerializableStateObservation sso);

    public abstract void endLevel();
}
