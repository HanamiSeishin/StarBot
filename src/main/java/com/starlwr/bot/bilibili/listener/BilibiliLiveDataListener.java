package com.starlwr.bot.bilibili.listener;

import com.starlwr.bot.bilibili.config.StarBotBilibiliProperties;
import com.starlwr.bot.bilibili.model.Room;
import com.starlwr.bot.bilibili.util.BilibiliApiUtil;
import com.starlwr.bot.core.enums.LivePlatform;
import com.starlwr.bot.core.event.datasource.change.StarBotDataSourceAddEvent;
import com.starlwr.bot.core.event.datasource.change.StarBotDataSourceUpdateEvent;
import com.starlwr.bot.core.event.datasource.other.StarBotDataSourceLoadCompleteEvent;
import com.starlwr.bot.core.model.PushUser;
import com.starlwr.bot.core.plugin.StarBotComponent;
import com.starlwr.bot.core.util.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bilibili 直播数据监听器
 */
@Profile("!core")
@Slf4j
@Order(-20000)
@StarBotComponent
public class BilibiliLiveDataListener {
    @Resource
    private StarBotBilibiliProperties properties;

    @Resource
    @Qualifier("bilibiliRedis")
    private RedisUtil redis;

    @Resource
    private BilibiliApiUtil bilibili;

    private boolean loadCompleted = false;

    /**
     * 更新 Redis 中的直播间信息
     * @param room 直播间信息
     */
    private void updateRedisRoomInfo(Room room) {
        Long uid = room.getUid();
        boolean status = room.getLiveStatus() == 1;

        Optional<Boolean> optionalLastStatus = redis.getLiveStatus(uid);
        if (optionalLastStatus.isEmpty()) {
            // 无状态记录，新增的主播，直接写入状态，若正在直播，写入开播时间
            redis.setLiveStatus(uid, status);
            if (status) {
                redis.setLiveStartTime(uid, room.getLiveStartTime());
            }
        } else {
            boolean lastStatus = optionalLastStatus.get();
            if (lastStatus) {
                if (status) {
                    Long lastStartTime = redis.getLiveStartTime(uid).orElseThrow();
                    if (!lastStartTime.equals(room.getLiveStartTime())) {
                        // 记录的状态与当前状态均为正在直播，但记录的开播时间与当前开播时间不一致，为程序退出期间下播再次开播，重置直播数据，写入开播时间，删除下播时间
                        redis.resetLiveData(uid);
                        redis.setLiveStartTime(uid, room.getLiveStartTime());
                        redis.deleteLiveEndTime(uid);
                    }
                } else {
                    // 记录的状态为正在直播，当前状态为未开播，为程序退出期间下播，更新状态
                    redis.setLiveStatus(uid, false);
                }
            } else {
                if (status) {
                    // 记录的状态为未开播，存在记录的开播时间，当前状态为正在直播，为程序退出期间开播，重置直播数据，更新状态，写入开播时间，删除下播时间（可能不存在）
                    redis.resetLiveData(uid);
                    redis.setLiveStatus(uid, true);
                    redis.setLiveStartTime(uid, room.getLiveStartTime());
                    redis.deleteLiveEndTime(uid);
                }
            }
        }
    }

    @EventListener
    public void handleAddEvent(StarBotDataSourceAddEvent event) {
        PushUser user = event.getUser();

        if (!LivePlatform.BILIBILI.getName().equals(user.getPlatform())) {
            return;
        }

        if (loadCompleted && user.getRoomId() != null && (!properties.getLive().isOnlyConnectNecessaryRooms() || user.hasEnabledLiveEvent())) {
            Room room = bilibili.getLiveInfoByRoomId(user.getRoomId());
            updateRedisRoomInfo(room);
        }
    }

    @EventListener
    public void handleUpdateEvent(StarBotDataSourceUpdateEvent event) {
        PushUser oldUser = event.getOldUser();
        PushUser user = event.getUser();

        if (!LivePlatform.BILIBILI.getName().equals(user.getPlatform())) {
            return;
        }

        boolean shouldUpdate = properties.getLive().isOnlyConnectNecessaryRooms()
                ? user.getRoomId() != null && ((!oldUser.hasEnabledLiveEvent() && user.hasEnabledLiveEvent()) || oldUser.getRoomId() == null)
                : oldUser.getRoomId() == null && user.getRoomId() != null;

        if (shouldUpdate) {
            Room room = bilibili.getLiveInfoByRoomId(user.getRoomId());
            updateRedisRoomInfo(room);
        }
    }

    @EventListener
    public void handleLoadCompleteEvent(StarBotDataSourceLoadCompleteEvent event) {
        loadCompleted = true;

        Set<Long> uids = event.getUsers().stream()
                .filter(user -> LivePlatform.BILIBILI.getName().equals(user.getPlatform()))
                .filter(PushUser::hasEnabledLiveEvent)
                .map(PushUser::getUid)
                .collect(Collectors.toSet());

        Map<Long, Room> liveInfos = bilibili.getLiveInfoByUids(uids);

        for (Room room : liveInfos.values()) {
            updateRedisRoomInfo(room);
        }
    }
}
