/**
 * The Network class handles all network related things common across all end points. This mainly includes
 * registering classes for serialisation.
 * @author Piotr Satała
 */

package app;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.HashSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import app.requests.*;
import app.responses.*;

public class Network {

    /**
     * Register classes for serialisation
     * @param endPoint end point of the connection
     */
    public static void register(EndPoint endPoint) {
        
        //get kryo
        Kryo kryo = endPoint.getKryo();
        //requests
        kryo.register(GameRequest.class);
        kryo.register(CreateRoomRequest.class);
        kryo.register(JoinRoomRequest.class);
        kryo.register(LeaveRoomRequest.class);
        kryo.register(GetRoomListRequest.class);
        kryo.register(GetRoomInfoRequest.class);
        //responses
        kryo.register(GameResponse.class);
        kryo.register(ControlResponse.class);
        kryo.register(RoomList.class);
        kryo.register(HashMap.class);
        kryo.register(GameRoom.class);
        kryo.register(HashSet.class);
        kryo.register(RoomCreatedResponse.class);
        kryo.register(RoomJoinedResponse.class);
        kryo.register(RoomClosedResponse.class);
        kryo.register(Inet4Address.class);    
    }
}