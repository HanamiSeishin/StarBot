package com.starlwr.bot.bilibili.listener;

import com.starlwr.bot.bilibili.config.StarBotBilibiliProperties;
import com.starlwr.bot.bilibili.model.Up;
import com.starlwr.bot.bilibili.service.BilibiliDynamicService;
import com.starlwr.bot.bilibili.service.BilibiliLiveRoomService;
import com.starlwr.bot.core.enums.LivePlatform;
import com.starlwr.bot.core.event.datasource.StarBotBaseDataSourceEvent;
import com.starlwr.bot.core.event.datasource.change.StarBotDataSourceAddEvent;
import com.starlwr.bot.core.event.datasource.change.StarBotDataSourceRemoveEvent;
import com.starlwr.bot.core.event.datasource.change.StarBotDataSourceUpdateEvent;
import com.starlwr.bot.core.event.datasource.other.StarBotDataSourceLoadCompleteEvent;
import com.starlwr.bot.core.model.PushUser;
import com.starlwr.bot.core.plugin.StarBotComponent;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;

/**
 * Bilibili 数据源事件监听器
 */
@Slf4j
@Order(-10000)
@StarBotComponent
public class BilibiliDataSourceEventListener implements ApplicationListener<StarBotBaseDataSourceEvent> {
    @Resource
    private StarBotBilibiliProperties properties;

    @Resource
    private BilibiliLiveRoomService liveRoomService;

    @Resource
    private BilibiliDynamicService dynamicService;

    private boolean loadCompleted = false;

    @Override
    public void onApplicationEvent(@NonNull StarBotBaseDataSourceEvent event) {
        if (event instanceof StarBotDataSourceAddEvent e) {
            onStarBotDataSourceAddEvent(e);
        } else if (event instanceof StarBotDataSourceRemoveEvent e) {
            onStarBotDataSourceRemoveEvent(e);
        } else if (event instanceof StarBotDataSourceUpdateEvent e) {
            onStarBotDataSourceUpdateEvent(e);
        } else if (event instanceof StarBotDataSourceLoadCompleteEvent) {
            onStarBotDataSourceLoadCompleteEvent();
        }
    }

    /**
     * 数据源推送用户添加事件
     * @param event 事件
     */
    private void onStarBotDataSourceAddEvent(StarBotDataSourceAddEvent event) {
        PushUser user = event.getUser();

        if (!LivePlatform.BILIBILI.getName().equals(user.getPlatform())) {
            return;
        }

        if (properties.getDynamic().isAutoFollow() && loadCompleted && user.hasEnabledDynamicEvent()) {
            dynamicService.followUp(new Up(user));
        }

        if (!properties.getLive().isEnableConnectLiveRoom()) {
            return;
        }

        if (properties.getLive().isOnlyConnectNecessaryRooms() && !user.hasEnabledLiveEvent()) {
            log.info("推送用户 (UID: {}, 昵称: {}, 房间号: {}, 平台: {}) 未监听直播事件, 跳过连接直播间", user.getUid(), user.getUname(), user.getRoomIdString(), user.getPlatform());
            return;
        }

        liveRoomService.addUp(new Up(user));
    }

    /**
     * 数据源推送用户移除事件
     * @param event 事件
     */
    private void onStarBotDataSourceRemoveEvent(StarBotDataSourceRemoveEvent event) {
        PushUser user = event.getUser();

        if (!LivePlatform.BILIBILI.getName().equals(user.getPlatform())) {
            return;
        }

        if (!properties.getLive().isEnableConnectLiveRoom()) {
            return;
        }

        if (properties.getLive().isOnlyConnectNecessaryRooms() && !user.hasEnabledLiveEvent()) {
            log.info("推送用户 (UID: {}, 昵称: {}, 房间号: {}, 平台: {}) 未监听直播事件, 无需断开直播间连接", user.getUid(), user.getUname(), user.getRoomIdString(), user.getPlatform());
            return;
        }

        liveRoomService.removeUp(new Up(user));
    }

    /**
     * 数据源推送用户移除事件
     * @param event 事件
     */
    private void onStarBotDataSourceUpdateEvent(StarBotDataSourceUpdateEvent event) {
        PushUser oldUser = event.getOldUser();
        PushUser user = event.getUser();

        if (!LivePlatform.BILIBILI.getName().equals(user.getPlatform())) {
            return;
        }

        if (properties.getDynamic().isAutoFollow() && loadCompleted && user.hasEnabledDynamicEvent()) {
            dynamicService.followUp(new Up(user));
        }

        if (!properties.getLive().isEnableConnectLiveRoom()) {
            return;
        }

        if (properties.getLive().isOnlyConnectNecessaryRooms()) {
            if (!oldUser.hasEnabledLiveEvent() && user.hasEnabledLiveEvent()) {
                log.info("推送用户 (UID: {}, 昵称: {}, 房间号: {}, 平台: {}) 监听了直播事件, 准备连接到直播间", user.getUid(), user.getUname(), user.getRoomIdString(), user.getPlatform());
                liveRoomService.addUp(new Up(user));
            } else if (oldUser.hasEnabledLiveEvent() && !user.hasEnabledLiveEvent()) {
                log.info("推送用户 (UID: {}, 昵称: {}, 房间号: {}, 平台: {}) 未监听直播事件, 准备断开直播间连接", user.getUid(), user.getUname(), user.getRoomIdString(), user.getPlatform());
                liveRoomService.removeUp(new Up(user));
            }
        } else {
            if (oldUser.getRoomId() == null && user.getRoomId() != null) {
                log.info("推送用户 (UID: {}, 昵称: {}, 房间号: {}, 平台: {}) 已开通直播间, 准备连接到直播间", user.getUid(), user.getUname(), user.getRoomIdString(), user.getPlatform());
                liveRoomService.addUp(new Up(user));
            }
        }
    }

    /**
     * 数据源加载完毕事件
     */
    private void onStarBotDataSourceLoadCompleteEvent() {
        loadCompleted = true;
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
