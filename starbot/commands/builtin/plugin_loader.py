import asyncio
from graia.ariadne.app import Ariadne
from graia.ariadne.event.message import FriendMessage
from graia.ariadne.message.parser.twilight import Twilight, FullMatch, UnionMatch
from graia.ariadne.model import Friend

from graia.saya import Channel, Saya
from graia.saya.builtins.broadcast.schema import ListenerSchema

from ...utils import config
from loguru import logger

prefix = config.get("COMMAND_PREFIX")
master_qq = config.get("MASTER_QQ")
custom_commands = config.get("CUSTOM_COMMANDS_PACKAGE")

channel = Channel.current()
saya = Saya.current()


@channel.use(
    ListenerSchema(
        listening_events=[FriendMessage],
        inline_dispatchers=[Twilight(
            FullMatch(prefix),
            UnionMatch("重载插件", "reloadplugin")
        )]
    )
)
async def _ReloadPlugins(app: Ariadne, friend: Friend):
    logger.info(f"触发命令：重载插件 qq[{friend.id}]({friend.nickname})")
    if master_qq != "" and friend.id != master_qq:
        logger.warning(f"重载插件命令主人专用，其他人不可触发")
        return
    if custom_commands is None or custom_commands == "":
        logger.info(f"不存在自定义命令包，无需重载")
        return
    try:
        _channel = saya.channels.get(custom_commands)
        logger.debug(f"卸载{custom_commands}")
        saya.uninstall_channel(_channel)
        channel_list = []
        for saya_channel_name, saya_channel in saya.channels.items():
            if saya_channel_name.startswith(f"{custom_commands}."):
                logger.debug(f"卸载{saya_channel_name}")
                channel_list.append(saya_channel)
        for c in channel_list:
            saya.uninstall_channel(c)
        await asyncio.sleep(1)
        with saya.module_context():
            logger.debug(f"安装{custom_commands}")
            saya.require(custom_commands)
    except Exception as e:
        logger.error(f"自定义命令({custom_commands})重载失败\n{e}")
        await app.send_message(friend, f"自定义命令({custom_commands})重载失败\n{e}")
        raise e

    logger.success(f"自定义命令({custom_commands})重载成功")
    await app.send_message(friend, f"自定义命令({custom_commands})重载成功")
