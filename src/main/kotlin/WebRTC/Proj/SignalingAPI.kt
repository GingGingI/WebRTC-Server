package WebRTC.Proj

import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket

import java.util.function.Predicate
import javax.inject.Singleton

/**
 * TODO
 *
 * 유저처리는 redis를 이용하여 굳이 db까지 이용할 필요 없이 처리
 * Signaling을 하여 유저간의 데이터 주고받기 제작.
 * user valid 제작하여 특정 user에게만 갈 수 있도록.
 * */

// Signaling WebSocket
@ServerWebSocket("/signaling/{channel}/{userid}")
class SignalingAPI(private val broadcaster: WebSocketBroadcaster) {

//    user의 signaling을 할 시에
//    ICE와 SDP정보를 담아 둘 데이터오브젝트도 제작해야 할 듯.

//    channel Name: RoomMasterID
    @Singleton
    var channels: HashMap<String, String> = HashMap()

    @OnOpen
    fun onOpen(channel: String, userid: String, session: WebSocketSession) {
        var msg: HashMap<String, Any> = HashMap()
//        val msg = "[$userid] connected to [$channel]"
//        채널확인
//        println(msg)
        println("socket open with Channel : [${channel}]")
        val isChannelExist: Boolean = channels[channel]?.isNotEmpty() ?: false
        if (!isChannelExist) {
            println("channel Created")
            msg.put("type", "created")
            channels.put(channel, userid)

            broadcaster.broadcastSync(msg, isValid(channel, session))
            return
        } else {
            println("user jointed to ${channel}")
            msg.put("type", "join")
            broadcaster.broadcastSync(msg, inChannel(channel, session))
        }
    }

//    session과 유저관리는 redis이용하여 빠르게 처리할 수 있도록.
    @OnMessage
    fun onMessage(channel: String, userid: String, message: String, session: WebSocketSession) {
        val msg = "[$userid] $message"
        println(msg)
//        추후 메시지에 넘어온 데이터를 구분하여 시그널링 데이터 전송.


        broadcaster.broadcastSync(msg, isValid(channel, session))
    }

    @OnClose
    fun OnClose(channel: String, userid: String, session: WebSocketSession) {
        val msg = "[$userid] disconnected from [$channel]"
//        채널에 사용자 확인 후 삭제
        broadcaster.broadcastSync(msg, isValid(channel, session))
    }

//    session 체크 및 channel이 접근할 때의 값과 같은지 체크
    private fun isValid(channel: String, session: WebSocketSession): Predicate<WebSocketSession> {
        return Predicate<WebSocketSession> {
            (it !== session && channel.equals(it.uriVariables.get("channel", String::class.java, null), ignoreCase = true))
        }
    }

    private fun inChannel(channel: String, session: WebSocketSession): Predicate<WebSocketSession> {
        return Predicate<WebSocketSession> {
            (channel.equals(it.uriVariables.get("channel", String::class.java, null), ignoreCase = true))
        }
    }

//    user Array 로 해서 contains로 비교도 가능할거 같기도?
    private fun isValidWithUser(channel: String, userid: String, session: WebSocketSession): Predicate<WebSocketSession> {
        return Predicate<WebSocketSession> {
//            유저 정보값을 가져와 비교 후 전송
            false
        }
    }
}